package com.nikki.torrents.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.google.common.io.ByteStreams;
import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.models.DownloadLocationModel;
import com.nikki.torrents.models.FileMetadata;
import com.nikki.torrents.utils.CheckConnection;
import com.nikki.torrents.utils.Constant;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 3/18/2016.
 */
public class TorrentDialogProgress extends Fragment {

    String torrent;
    Context context;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.torrentName)
    TextView torrentName;
    @InjectView(R.id.downloadLocation)
    TextView downloadLocation;
    @InjectView(R.id.numberOfFiles)
    TextView numberOfFiles;
    @InjectView(R.id.totalSize)
    TextView totalSize;
    @InjectView(R.id.freeSize)
    TextView freeSize;
    List<FileMetadata> fileMetadataList;
    DownloadLocationModel downloadLocationModel;
    byte[] torrentBytes;
    TorrentInfo torrentInfo;
    AppCompatActivity appCompatActivity;


    public static Fragment newInstance(Bundle bundle) {
        TorrentDialogProgress torrentDialogProgress = new TorrentDialogProgress();
        if (bundle != null)
            torrentDialogProgress.setArguments(bundle);

        return torrentDialogProgress;
    }

    enum TorrentMagnet {
        MAGNET, TORRENT
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.torrent_dialog_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        toolbar.setTitle(getString(R.string.add_torrent));
        if (torrentBytes == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                torrent = bundle.getString("torrent");

                if (torrent != null) {
                    if (torrent.contains(".torrent")) {
                        parsetTorrent();
                    } else if (torrent.contains("magnet:?xt")) {
                        parseMagnet();
                    } else {
                        MainActivity.goBack(appCompatActivity);
                    }
                }
            }

        } else {
            fillTorrentEntry(torrentBytes);
        }


    }

    private void parseMagnet() {
        if (CheckConnection.checkConnection(context)) {
            new DownloadTorrentAsync(TorrentMagnet.MAGNET).execute();
        } else {
            Toast.makeText(context, getString(R.string.internet_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void parsetTorrent() {
        if (CheckConnection.checkConnection(context)) {
            new DownloadTorrentAsync(TorrentMagnet.TORRENT).execute();
        } else {
            Toast.makeText(context, getString(R.string.internet_message), Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadTorrentAsync extends AsyncTask<Void, Void, byte[]> {

        TorrentMagnet torrentMagnet;

        public DownloadTorrentAsync(TorrentMagnet torrentMagnet) {
            this.torrentMagnet = torrentMagnet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setIndeterminate(true);
            torrentName.setText(getString(R.string.downloading_file));

        }

        @Override
        protected byte[] doInBackground(Void... params) {
            try {
                if (torrentMagnet == TorrentMagnet.TORRENT)
                    return ByteStreams.toByteArray(new URL(torrent).
                            openConnection().getInputStream());
                else
                    return Constant.getTorrentFromMagnet(torrent);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            try {
                progressBar.setIndeterminate(false);
                if (bytes != null) {
                    fillTorrentEntry(bytes);
                } else {
                    torrentName.setText(getString(R.string.couldnot_add_torrent));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFilesMetada() {
        if (fileMetadataList != null) {
            Pair<Integer, String> integerStringPair = Constant.getSelecteWithSize(fileMetadataList);
            numberOfFiles.setText(String.format("%d %s", integerStringPair.first, getString(R.string.files)));
            totalSize.setText(integerStringPair.second);
        }
    }


    private void fillTorrentEntry(byte[] bytes) {
        try {
            torrentBytes = bytes;
            torrentInfo = TorrentInfo.bdecode(bytes);
            torrentName.setText(torrentInfo.getName());

            if (downloadLocationModel == null) {
                downloadLocationModel = new DownloadLocationModel();
                downloadLocation.setText(downloadLocationModel.downloadLocationPath = String.format("%s%s%s", Constant.getFileDirectory(),
                        torrentInfo.getName(), File.separator));
                downloadLocationModel.basePath = Environment.getExternalStorageDirectory().getPath();
            }


            downloadLocation.setText(downloadLocationModel.downloadLocationPath);
            freeSize.setText(String.format("%s %s", Constant.getFreeSpace(downloadLocationModel.basePath),
                    getString(R.string.free)));

            if (fileMetadataList == null) {
                fileMetadataList = new ArrayList<>();
                FileStorage fileStorage = torrentInfo.getFiles();
                int count = fileStorage.getNumFiles();
                for (int i = 0; i < count; i++) {
                    FileMetadata fileMetadata = new FileMetadata();
                    fileMetadata.fileIndex = i;
                    fileMetadata.fileName = fileStorage.getFileName(i);
                    fileMetadata.size = fileStorage.getFileSize(i);
                    fileMetadataList.add(fileMetadata);
                }
            }

            writeFilesMetada();

        } catch (Exception e) {
            e.printStackTrace();
            torrentBytes = null;
            torrentName.setText(getString(R.string.couldnot_add_torrent));
        }

    }

    private void showHintDialog(){
        new AlertDialog.Builder(context).setMessage(getString(R.string.download_hint)).
                setPositiveButton(android.R.string.ok, null).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                appCompatActivity.finish();
            }
        }).create().show();
    }

    @OnClick({R.id.fab,
            R.id.downloadLocationContainer,
            R.id.fileDownloadContainer})
    public void onViewsClicked(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
            if (torrentInfo != null && downloadLocationModel != null) {
                try {
                    String directory = downloadLocationModel.downloadLocationPath;

                    if (!directory.
                            substring(directory.length() - 1).equals("/"))
                        directory += File.separator;

                    Constant.createDirectory(directory);
                    String file = directory + torrentInfo.getInfoHash() + ".torrent";
                    Constant.createFile(file);
                    File torrentFile = new File(file);
                    Constant.writeBytesToFile(torrentBytes, torrentFile);


                    TorrentModel torrentModel = new TorrentModel();
                    torrentModel.FilePath = file;
                    torrentModel.fileMetadataList = fileMetadataList;
                    torrentModel.serializeFileMetaData();
                    torrentModel.save();



                    Constant.startTorrent(appCompatActivity, torrentFile, null);
                    showHintDialog();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (id == R.id.downloadLocationContainer) {
            DirectoryChooser directoryChooser = DirectoryChooser.newInstance(null);
            directoryChooser.pathInterface = new DirectoryChooser.PathInterface() {
                @Override
                public void onPathSelected(DownloadLocationModel downloadLocationModel) {
                    TorrentDialogProgress.this.downloadLocationModel = downloadLocationModel;
                }
            };
            MainActivity.bringFragment(appCompatActivity,
                    directoryChooser, true);
        } else if (id == R.id.fileDownloadContainer) {
            if (fileMetadataList != null) {
                FilesSelectFragment filesSelectFragment = FilesSelectFragment.newInstance(null);
                filesSelectFragment.fileMetadataList = fileMetadataList;
                MainActivity.bringFragment(appCompatActivity,
                        filesSelectFragment, true);
            }

        }
    }


}
