package com.nikki.torrents.downloadService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentAlertAdapter;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;
import com.frostwire.jlibtorrent.swig.libtorrent;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.broadcastReceiver.TorrentBroadcast;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.enums.DownloadState;
import com.nikki.torrents.enums.TorrentHandleState;
import com.nikki.torrents.enums.UploadDownload;
import com.nikki.torrents.interfaces.TorrentServiceListener;
import com.nikki.torrents.models.FileMetadata;
import com.nikki.torrents.models.LimitSettingsPreference;
import com.nikki.torrents.utils.Constant;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nishi Sahlot on 3/2/2016.
 */
public class TorrentService extends Service {

    final public ThreadHelper threadHelper = new ThreadHelper();
    public static String PAUSE_DOWNLOAD = "pause_download";
    public static String FOREGROUND = "foreground_action";
    public static String UPDATE_SPEED = "update_speed";
    public static String UPDATE_GLOBAL_SPEED="update_global_speed";
    private int uniquePendingIntent;


    private Map<String, TorrentServiceListener> serviceListenerMap = ListenerSingleton.getInstance().serviceListenerMap;
    private static final Object lock = new Object();
    Context context;
    NotificationManager mNotifyManager;
    final Session session = new Session();
    private int helperId = 100000;
    public static int defaultId = -1;
    Handler handler = new Handler();

    public enum NotificationAction implements Serializable {
        PAUSE, RESUME, FOREGROUND
    }


    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        TaskInFront.isTaskInFront = false;
        for (Iterator<Map.Entry<String, ServiceHandler>> it = threadHelper.handlerHashMap.
                entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ServiceHandler> entry = it.next();
            ServiceHandler serviceHandler = entry.getValue();

            if (serviceHandler.torrentHandleState == TorrentHandleState.PAUSE) {
                serviceHandler.signal.countDown();
                it.remove();
            }
        }

        stopServiceCustom();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mNotifyManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.
                getInstance(context);
        localBroadcastManager.
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, final Intent intent) {

                        String torrentFilePath = intent.getStringExtra("torrent");
                        NotificationAction notificationAction = (NotificationAction) intent.
                                getSerializableExtra("notificationAction");

                        if (notificationAction != null) {
                            switch (notificationAction) {
                                case PAUSE:
                                    ServiceHandler serviceHandler = threadHelper.
                                            getExistingThread(torrentFilePath);
                                    if (serviceHandler != null) {

                                        createResumeNotification(serviceHandler);
                                    } else {
                                        int notifyId = intent.getIntExtra("notifyId", defaultId);
                                        if (notifyId != defaultId) {
                                            mNotifyManager.cancel(notifyId);
                                        }
                                        stopServiceCustom();
                                    }

                            }
                        }


                    }
                }, new IntentFilter(PAUSE_DOWNLOAD));
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    for (Map.Entry<String, ServiceHandler> entry : threadHelper.handlerHashMap.entrySet()) {
                        ServiceHandler serviceHandler = entry.getValue();
                        serviceHandler.pauseTorrent();
                        serviceHandler.builder = null;
                    }
                    threadHelper.handlerHashMap.clear();
                    stopServiceCustom();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new IntentFilter(FOREGROUND));
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                try {
                    UploadDownload uploadDownload = (UploadDownload) intent.getSerializableExtra("uploadDownload");
                    String torrent = intent.getStringExtra("torrent");
                    if (threadHelper != null) {
                        ServiceHandler serviceHandler = threadHelper.getExistingThread(torrent);
                        TorrentModel torrentModel = TorrentModel.getMe("FilePath=?", torrent);

                        if (serviceHandler != null && torrentModel != null && serviceHandler.torrentModel != null) {
                            if (uploadDownload == UploadDownload.UPLOAD_LIMIT) {
                                serviceHandler.torrentModel.UploadLimit = torrentModel.UploadLimit;
                                serviceHandler.setUploadLimit();
                            } else if (uploadDownload == UploadDownload.DOWNLOAD_LIMIT) {
                                serviceHandler.torrentModel.DownloadLimit = torrentModel.DownloadLimit;
                                serviceHandler.setDownloadLimit();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new IntentFilter(UPDATE_SPEED));
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                applySettings();
            }
        },new IntentFilter(UPDATE_GLOBAL_SPEED));

        applySettings();

        // Toast.makeText(context,"started",Toast.LENGTH_SHORT).show();
    }

    private void applySettings() {
        try {
            LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);

            SettingsPack settingsPack = session.getSettingsPack();


            settingsPack.setDownloadRateLimit(limitSettingsPreference.downloadLimit);
            settingsPack.setUploadRateLimit(limitSettingsPreference.uploadLimit);
            if (limitSettingsPreference.enableShareRatio)
                settingsPack.setInteger(settings_pack.int_types.share_ratio_limit.swigValue(), (int) limitSettingsPreference.shareRatioLimit);
            if (limitSettingsPreference.stopSeedingAfter)
                settingsPack.setInteger(settings_pack.int_types.seed_time_limit.swigValue(), limitSettingsPreference.seedingTimeLimit * 60);
            session.applySettings(settingsPack);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private final class ServiceHandler extends Handler {
        String id;
        private ScheduledFuture<?> mScheduleFuture;
        private final ScheduledExecutorService mExecutorService =
                Executors.newSingleThreadScheduledExecutor();

        TorrentHandleState torrentHandleState = TorrentHandleState.PAUSE;
        //int startId = -1;
        public TorrentHandle torrentHandle;
        CountDownLatch signal = new CountDownLatch(1);
        NotificationCompat.Builder builder;
        String torrent;
        TorrentModel torrentModel;
        int notificationId;
        int secondsElapsed;

        public ServiceHandler(Looper looper, String id) {
            super(looper);
            this.id = id;
            signal.countDown();
        }

        private void setUploadLimit() {
            try {
                if (torrentModel.UploadLimit != null) {
                    torrentHandle.setUploadLimit(torrentModel.UploadLimit);
                } else {
                    torrentHandle.setUploadLimit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setDownloadLimit() {
            try {
                if (torrentModel.DownloadLimit != null) {
                    torrentHandle.setDownloadLimit(torrentModel.DownloadLimit);
                } else {
                    torrentHandle.setDownloadLimit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void scheduleThread() {
            try {
                cancelScheduledThread();
                if (mScheduleFuture != null && !mScheduleFuture.isDone())
                    return;

                if (!mExecutorService.isShutdown()) {
                    mScheduleFuture = mExecutorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                try {
                                    if (threadHelper.getExistingThread(torrent) != null) {

                                        boolean stopThread = false;
                                        if (torrentHandleState == TorrentHandleState.RESUME) {
                                            // if(torrentHandle.getStatus().isSeedMode())
                                            String downloadingSpeed = getDownloadingSpeed(torrentHandle) + "/s";
                                            String uploadingSpeed = getUploadingSpeed(torrentHandle) + "/s";
                                            torrentModel.downloadingSpeed = downloadingSpeed;
                                            torrentModel.uploadingSpeed = uploadingSpeed;

                                            if (builder != null) {
                                                String message = "";
                                                if (torrentHandle.getStatus().isFinished()) {
                                                    torrentModel.progress = 1;
                                                    secondsElapsed++;
                                                    if (torrentModel.DownloadState != DownloadState.COMPLTE_DOWNLOADED) {
                                                        torrentModel.DownloadState = DownloadState.COMPLTE_DOWNLOADED;
                                                        saveTorrentModel(torrentModel);
                                                    }
                                                    LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.
                                                            getMe(LimitSettingsPreference.class);
                                                    if (limitSettingsPreference.stopSeedingAfter) {
                                                        if (secondsElapsed >= limitSettingsPreference.seedingTimeLimit * 60) {
                                                            stopThread = true;
                                                        }
                                                    }
                                                    torrentModel.indeterminateProgressBar = false;

                                                    message = "Seeding ";
                                                }
                                                message += "Down " + downloadingSpeed +
                                                        " Up " + uploadingSpeed;
                                                builder.setContentText(message);

                                                mNotifyManager.notify(notificationId, builder.build());
                                            }
                                            updateFileSizeDownload();
                                            updateTorrentModelProgress();
                                            if (stopThread) {
                                                pauseAndRemoveThread(ServiceHandler.this, true);
                                            }
                                            scheduleThread();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }, 1, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void cancelScheduledThread() {
            try {
                if (mScheduleFuture != null) {
                    mScheduleFuture.cancel(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        private void downloadCompleteAndStopThread() {
            downloadCompletedNotification(ServiceHandler.this);
            torrentModel.indeterminateProgressBar = false;

        }


        @Override
        public void handleMessage(Message msg) {
//            if (startId == -1)
//                this.startId = msg.arg1;


            torrentModel = TorrentModel.getMe("FilePath=?", id);


            Bundle bundle;

            if (torrentModel != null && (bundle = msg.getData()) != null && (torrent = bundle.getString("torrent")) != null) {

                try {
                    torrentModel.initializeSpeeds();
                    File torrentFile = new File(torrent);
                    if (!torrentFile.exists())
                        throw new Exception();

                    if (torrentHandle == null) {

                        File file = null;
                        if (!TextUtils.isEmpty(torrentModel.FastResume)) {
                            file = new File(torrentModel.FastResume);
                            if (!file.exists()) {
                                file = null;
                            }
                        }

                        torrentHandle = session.addTorrent(torrentFile, torrentFile.getParentFile(), file);

                    }


                    setUploadLimit();
                    setDownloadLimit();


                    TorrentHandleState torrentHandleStateLocal = (TorrentHandleState) bundle.getSerializable("torrentHandleState");

                    session.addListener(new TorrentAlertAdapter(torrentHandle) {


                        long time = Calendar.getInstance().getTimeInMillis();

                        @Override
                        public void blockFinished(BlockFinishedAlert alert) {
                            float localProgress = th.getStatus().getProgress();
                            int progressIn100 = (int) (localProgress * 100);
                            boolean showNotification = false;
                            long breakTime = Calendar.getInstance().getTimeInMillis();
                            if (breakTime - time > 1000) {
                                time = breakTime;
                                showNotification = true;
                            }
                            torrentModel.indeterminateProgressBar = false;
                            System.out.println("Progress: " + progressIn100 + " for torrent name: " + alert.torrentName());
                            updateFileSizeDownload();


                            if (showNotification && builder != null) {
                                builder.setProgress(100, progressIn100, false);
                                mNotifyManager.notify(notificationId, builder.build());
                            }

                            if (torrentModel.progress < localProgress)
                                torrentModel.progress = localProgress;

                            updateTorrentModelProgress();

                            if (progressIn100 == 100) {
                                downloadCompleteAndStopThread();
                            }

                        }

                        @Override
                        public void torrentFinished(TorrentFinishedAlert alert) {
                            System.out.print("Torrent finished");
                            torrentModel.progress = 1;

                            downloadCompleteAndStopThread();


                        }

                        @Override
                        public void saveResumeData(SaveResumeDataAlert alert) {
                            super.saveResumeData(alert);
                            try {
                                byte[] bytes = alert.resumeData().bencode();
                                if (bytes != null) {
                                    File file = new File(torrentModel.FilePath);
                                    if (file.exists()) {
                                        String newPath = file.getParent();
                                        newPath += File.separator + torrentHandle.getName() + ".fastresume";
                                        file = new File(newPath);
                                        if (file.exists())
                                            file.delete();
                                        if (file.createNewFile()) {
                                            Constant.writeBytesToFile(bytes, file);
                                            torrentModel.FastResume = file.getPath();
                                            torrentModel.save();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    if (!TextUtils.isEmpty(torrentModel.fileMetaData)) {
                        torrentModel.deserializeFileMetaData();
                        if (torrentModel.fileMetadataList != null) {
                            int size = torrentModel.fileMetadataList.size();
                            for (int i = 0; i < size; i++) {
                                FileMetadata fileMetadata = torrentModel.fileMetadataList.get(i);
                                torrentHandle.setFilePriority(fileMetadata.fileIndex,
                                        fileMetadata.priority);
                            }
                        }
                    }


                    if (torrentHandleStateLocal == null || torrentHandleStateLocal == TorrentHandleState.RESUME) {
                        resumeTorrent();
                        builder = makeProgressNotification(notificationId);
                        builder.setContentTitle(torrentHandle.getName());

                        getPendingIntent(builder, torrent, NotificationAction.PAUSE, notificationId);

                        mNotifyManager.notify(notificationId, builder.build());
                        scheduleThread();
                        torrentModel.indeterminateProgressBar = true;

                    } else if (torrentHandleStateLocal == TorrentHandleState.PAUSE) {
                        pauseTorrent();
                        createResumeNotification(this);
                        torrentModel.indeterminateProgressBar = false;
                    }


                    if (torrentModel.CreatedDate == null)
                        torrentModel.CreatedDate = System.currentTimeMillis();

                    saveTorrentToDatabase();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                for (Map.Entry<String, TorrentServiceListener> entry : serviceListenerMap.entrySet()) {
                                    if (entry.getValue() != null) {
                                        entry.getValue().torrentAdded(torrentModel);
                                    }
                                }
                            }
                        }
                    });

                    if (torrentHandleStateLocal == null || torrentHandleStateLocal != TorrentHandleState.DELETE) {
                        signal.await();
                        libtorrent.default_storage_disk_write_access_log(false);
                    } else {
                        pauseTorrent();
                        session.removeTorrent(torrentHandle);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (lock) {
                                    torrentModel.torrentHandleState = TorrentHandleState.DELETE;
                                    for (Map.Entry<String, TorrentServiceListener> entry : serviceListenerMap.entrySet()) {
                                        if (entry.getValue() != null) {
                                            entry.getValue().updateBlockProgress(torrentModel);
                                        }
                                    }
                                    removeNotificationAndStopDownload(ServiceHandler.this, true);
                                }

                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    removeNotificationAndStopDownload(this, true);
                }

            } else {
                removeNotificationAndStopDownload(this, true);
            }

        }


        private void updateTorrentModelProgress() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        for (Map.Entry<String, TorrentServiceListener> entry : serviceListenerMap.entrySet()) {
                            if (entry.getValue() != null) {
                                entry.getValue().updateBlockProgress(torrentModel);
                            }
                        }
                    }
                }
            });
        }


        private void saveTorrentToDatabase() {
            torrentModel.FilePath = torrent;
            if (torrentModel.DownloadState == null || torrentModel.DownloadState != DownloadState.COMPLTE_DOWNLOADED)
                torrentModel.DownloadState = DownloadState.PARTIAL_DOWNLOADED;
            torrentModel.TorrentName = torrentHandle.getName();
            torrentModel.FileSize = torrentHandle.getStatus().getTotalWanted();
            updateFileSizeDownload();
        }

        private void updateFileSizeDownload() {
            TorrentStatus torrentStatus = torrentHandle.getStatus();
            long done = torrentStatus.getTotalDone();
            long uploaded = torrentStatus.getAllTimeUpload();
            long time = torrentStatus.getAddedTime();
            if (torrentModel.FileDownloaded < done) {
                torrentModel.FileDownloaded = done;
                torrentModel.indeterminateProgressBar = false;
            }
            if (torrentModel.FileUploaded < uploaded)
                torrentModel.FileUploaded = uploaded;
            if (torrentModel.AddedTime < time)
                torrentModel.AddedTime = time;

            torrentModel.TotalSeeds = torrentStatus.getListSeeds();
            torrentModel.TotalPeers = torrentStatus.getListPeers();
            if (torrentHandleState == TorrentHandleState.RESUME) {
                torrentModel.connectedSeeders = torrentStatus.getNumSeeds();
                torrentModel.connectedPeers = torrentStatus.getNumPeers();
            }

            saveTorrentModel(torrentModel);
        }

        private int getProgress(TorrentHandle torrentHandle) {
            return (int) (torrentHandle.getStatus().getProgress() * 100);
        }

        private String getDownloadingSpeed(TorrentHandle torrentHandle) {
            return Constant.byteCountToDisplaySize(torrentHandle.getStatus().getDownloadRate());
        }

        private String getUploadingSpeed(TorrentHandle torrentHandle) {
            return Constant.byteCountToDisplaySize(torrentHandle.getStatus().getUploadRate());
        }


        private void resumeTorrent() {
            torrentHandleState = TorrentHandleState.RESUME;
            if (torrentHandle != null) {
                torrentHandle.setAutoManaged(true);
                torrentHandle.resume();
            }

            saveTorrentModelPauseResumeState();
            torrentModel.initializeSpeeds();
            updateTorrentModelProgress();
        }

        private void pauseTorrent() {
            torrentHandleState = TorrentHandleState.PAUSE;
            if (torrentHandle != null) {
                torrentHandle.setAutoManaged(false);
                torrentHandle.pause();
                torrentHandle.saveResumeData();
            }


            saveTorrentModelPauseResumeState();
            torrentModel.initializeSpeeds();
            updateTorrentModelProgress();
        }

        private void saveTorrentModelPauseResumeState() {
            torrentModel.torrentHandleState = torrentHandleState;
            saveTorrentModel(torrentModel);
        }

    }

    private void saveTorrentModel(TorrentModel torrentModel) {
        if (torrentModel.FilePath != null)
            torrentModel.save();
    }

    private void getPendingIntent(NotificationCompat.Builder builder, String torrent,
                                  NotificationAction notificationAction, int notifyId) {


        switch (notificationAction) {
            case PAUSE:
                Intent intent = new Intent(context, TorrentBroadcast.class);
                intent.putExtra("torrent", torrent);
                intent.putExtra("notificationAction", notificationAction);
                intent.putExtra("notifyId", notifyId);
                intent.setAction(PAUSE_DOWNLOAD);
                builder.addAction(android.R.drawable.ic_media_pause,
                        getString(R.string.pause),
                        PendingIntent.getBroadcast(context, uniquePendingIntent++, intent, PendingIntent.FLAG_CANCEL_CURRENT));
                setContentIntent(builder, torrent);
                break;
            case RESUME:
                intent = new Intent(getBaseContext(), TorrentService.class);
                intent.putExtra("torrent", torrent);
                intent.putExtra("notificationAction", notificationAction);
                intent.putExtra("notifyId", notifyId);
                builder.addAction(android.R.drawable.ic_media_play,
                        getString(R.string.resume),
                        PendingIntent.getService(getBaseContext(), uniquePendingIntent++, intent, PendingIntent.FLAG_CANCEL_CURRENT));
                setContentIntent(builder, torrent);
                break;
            case FOREGROUND:
                intent = new Intent(context, TorrentBroadcast.class);
                intent.setAction(FOREGROUND);
                intent.putExtra("notificationAction", notificationAction);
                builder.addAction(R.mipmap.shut_down,
                        getString(R.string.shut_down),
                        PendingIntent.getBroadcast(context, uniquePendingIntent++, intent, PendingIntent.FLAG_CANCEL_CURRENT));

        }


    }

    private void setContentIntent(NotificationCompat.Builder builder, String torrent) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("torrent", torrent);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private void setLargeIcon(NotificationCompat.Builder mBuilder){
        if(Build.VERSION.SDK_INT>=21){
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                    R.mipmap.app_72x72));
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mBuilder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
        }
    }

    private void downloadCompletedNotification(ServiceHandler serviceHandler) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(serviceHandler.torrentHandle.getName())
                .setContentText("Download Completed")
                .setSmallIcon(getNotificationIcon());

        setLargeIcon(mBuilder);

        mNotifyManager.notify(serviceHandler.notificationId + helperId, mBuilder.build());
        serviceHandler.torrentModel.DownloadState = DownloadState.COMPLTE_DOWNLOADED;
        saveTorrentModel(serviceHandler.torrentModel);
        runOnUiThread(new UpdateRunnable(serviceHandler.torrentModel));
    }

    private int getNotificationIcon(){
        if(Build.VERSION.SDK_INT>=21)
            return  R.mipmap.app_icon_32;
        else
            return  R.mipmap.app_icon_40;
    }
    class UpdateRunnable implements Runnable {
        TorrentModel torrentModel;

        UpdateRunnable(TorrentModel torrentModel) {
            this.torrentModel = torrentModel;
        }

        @Override
        public void run() {
            synchronized (lock) {
                for (Map.Entry<String, TorrentServiceListener> entry : serviceListenerMap.entrySet()) {
                    if (entry.getValue() != null) {
                        entry.getValue().downloadFinish(torrentModel);
                    }
                }
            }

        }
    }

    private void pauseAndRemoveThread(ServiceHandler serviceHandler, boolean completed) {
        serviceHandler.pauseTorrent();
        removeNotificationAndStopDownload(serviceHandler, completed);
    }


    private void createResumeNotification(ServiceHandler serviceHandler) {


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(serviceHandler.torrentHandle.getName())
                .setContentText("Resume Torrent " + Constant.byteCountToDisplaySize(
                        serviceHandler.torrentHandle.getStatus().getTotalDone()) + " downloaded")
                .setSmallIcon(getNotificationIcon()).

                setAutoCancel(true);
        setLargeIcon(mBuilder);
        mBuilder.setProgress(100, serviceHandler.getProgress(serviceHandler.torrentHandle), false);
        int notifyId = serviceHandler.notificationId + helperId;

        getPendingIntent(mBuilder, serviceHandler.torrent, NotificationAction.RESUME, notifyId);


        mNotifyManager.notify(notifyId, mBuilder.build());

        pauseAndRemoveThread(serviceHandler, false);
    }

    private int getUniqueNotificationId(String torrent) {
        TorrentModel torrentModel = TorrentModel.getMe("FilePath=?", torrent);
        return (int) (long) torrentModel.getId();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            try {
                String torrentFilePath = intent.getStringExtra("torrent");
                TorrentHandleState torrentHandleState = (TorrentHandleState) intent.getSerializableExtra("torrentHandleState");

                removeReusmeNotification(intent);

                ServiceHandler mServiceHandler = threadHelper.getNewThread(torrentFilePath);

                Message msg = mServiceHandler.obtainMessage();
                msg.arg1 = startId;

                if (torrentFilePath != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("torrent", torrentFilePath);
                    bundle.putSerializable("torrentHandleState", torrentHandleState);
                    msg.setData(bundle);
                }
                startForeGroundNotification();
                makeProgressNotification(mServiceHandler.notificationId);
                mNotifyManager.cancel(mServiceHandler.notificationId + helperId);
                mServiceHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();

            }

        }


        return START_STICKY;
    }

    private void removeReusmeNotification(Intent intent) {
        int notifyId;
        if ((notifyId = intent.getIntExtra("notifyId", defaultId)) != defaultId) {
            mNotifyManager.cancel(notifyId);
        }
    }


    private void removeNotificationAndStopDownload(ServiceHandler serviceHandler, boolean completed) {
        serviceHandler.builder = null;

        if (completed || !TaskInFront.isTaskInFront) {
            countDownAndResmove(serviceHandler);
        }

        mNotifyManager.cancel(serviceHandler.notificationId);


        stopServiceCustom();
    }

    private void countDownAndResmove(ServiceHandler serviceHandler) {
        serviceHandler.signal.countDown();
        threadHelper.removeThread(serviceHandler.id);
    }

    private void stopServiceCustom() {
        if (threadHelper.handlerHashMap.size() == 0) {
            stopForeground(true);
            mNotifyManager.cancelAll();
            stopSelf();

        }

    }

    private void startForeGroundNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name) + " " + getString(R.string.running)).
                setSmallIcon(getNotificationIcon()).setPriority(NotificationCompat.PRIORITY_MAX);
        setLargeIcon(mBuilder);

        getPendingIntent(mBuilder, null, NotificationAction.FOREGROUND, defaultId);
        startForeground(10000000, mBuilder.build());
    }

    private NotificationCompat.Builder makeProgressNotification(int startId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.please_wait))
                .setContentText(getString(R.string.connecting))
                .setSmallIcon(getNotificationIcon());
        mBuilder.setProgress(0, 0, true).setOngoing(true);
        setLargeIcon(mBuilder);

        mNotifyManager.notify(startId, mBuilder.build());

        return mBuilder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Toast.makeText(context,"Destroyed",Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class ThreadHelper {
        public Map<String, ServiceHandler> handlerHashMap = new LinkedHashMap<>();

        public ServiceHandler getNewThread(String id) {

            ServiceHandler serviceHandler = getExistingThread(id);
            if (serviceHandler == null) {
                HandlerThread thread = new HandlerThread("ServiceStartArguments",
                        android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();

                // Get the HandlerThread's Looper and use it for our Handler
                Looper mServiceLooper = thread.getLooper();
                serviceHandler = new ServiceHandler(mServiceLooper, id);
                serviceHandler.notificationId = getUniqueNotificationId(id);
                handlerHashMap.put(id, serviceHandler);
            } else {
                serviceHandler.signal.countDown();
            }

            return serviceHandler;
        }

        public void removeThread(String id) {
            handlerHashMap.remove(id);
        }

        public ServiceHandler getExistingThread(String id) {
            if (handlerHashMap.containsKey(id))
                return handlerHashMap.get(id);

            return null;
        }

    }
}
