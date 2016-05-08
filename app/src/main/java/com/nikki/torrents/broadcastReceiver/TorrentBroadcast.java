package com.nikki.torrents.broadcastReceiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.nikki.torrents.downloadService.TorrentService;

/**
 * Created by Nishi Sahlot on 3/6/2016.
 */
public class TorrentBroadcast extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if(intent.getAction()!=null&&intent.getAction().
                    equals(TorrentService.PAUSE_DOWNLOAD)){
                int defaultId= TorrentService.defaultId;
                int notifyId = intent.getIntExtra("notifyId", defaultId);
                if (notifyId != defaultId) {
                    NotificationManager  mNotifyManager =
                            (NotificationManager)
                                    context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.cancel(notifyId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
