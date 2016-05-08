package com.nikki.torrents.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.QueueAdapter;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.downloadService.ListenerSingleton;
import com.nikki.torrents.downloadService.TorrentService;
import com.nikki.torrents.enums.DownloadFinish;
import com.nikki.torrents.enums.DownloadState;
import com.nikki.torrents.enums.TorrentHandleState;
import com.nikki.torrents.enums.UploadDownload;
import com.nikki.torrents.interfaces.LimitInterface;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.interfaces.TorrentServiceListener;
import com.nikki.torrents.utils.Constant;
import com.nikki.torrents.utils.ShowUploadLimit;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/2/2016.
 */
public class Downloads extends Fragment {

    public Downloads() {
    }

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.errorText)
    TextView errorText;
    DownloadFinish downloadFinish;


    public static Fragment newInstance(Bundle bundle) {
        Downloads downloads = new Downloads();
        if (bundle != null)
            downloads.setArguments(bundle);

        return downloads;
    }

    QueueAdapter queueAdapter;


    AppCompatActivity appCompatActivity;
    Context context;
    String key;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        key = getClass().getName();

        Bundle bundle = getArguments();
        if (bundle != null) {
            key += bundle.getInt("position");
        }

    }


    TorrentServiceListener torrentServiceListener = new TorrentServiceListener() {
        @Override
        public void updateBlockProgress(TorrentModel torrentModel) {
            updateTorrentModel(torrentModel);
        }

        @Override
        public void downloadFinish(TorrentModel torrentModel) {
            updateTorrentModel(torrentModel);
        }

        @Override
        public void torrentAdded(TorrentModel torrentModel) {
            showList();
        }
    };

    private void updateTorrentModel(TorrentModel torrentModel) {
        try {
            if (queueAdapter != null && downloadFinish != null) {
                if (downloadFinish == DownloadFinish.DOWNLOADING || (
                        downloadFinish == DownloadFinish.FINISHED && torrentModel.DownloadState == DownloadState.COMPLTE_DOWNLOADED)) {

                    Pair<Integer, TorrentModel> modelPair = queueAdapter.torrentModelMap.get(torrentModel.FilePath);

                    if (modelPair != null) {
//                        if(modelPair.second.getId()!=null&&modelPair.second.getId().equals(torrentModel.getId()))
//                        queueAdapter.replaceObject(torrentModel, modelPair.first);
//                        queueAdapter.notifyDataSetChanged();h
                        if (torrentModel.refreshInterface != null)
                            torrentModel.refreshInterface.refreshView();
                    } else {
                        if ((downloadFinish == DownloadFinish.DOWNLOADING && torrentModel.DownloadState == DownloadState.PARTIAL_DOWNLOADED) ||
                                (downloadFinish == DownloadFinish.FINISHED && torrentModel.DownloadState == DownloadState.COMPLTE_DOWNLOADED)) {
                            int index = queueAdapter.addObject(torrentModel);
                            queueAdapter.notifyItemInserted(index);
                            queueAdapter.notifyItemRangeChanged(index, queueAdapter.torrentModels.size());
                        }

                    }
                    if (downloadFinish == DownloadFinish.DOWNLOADING && torrentModel.DownloadState == DownloadState.COMPLTE_DOWNLOADED) {
                        int index = queueAdapter.removeObject(torrentModel);
                        if (index != -1) {
                            queueAdapter.notifyItemRemoved(index);
                            queueAdapter.notifyItemRangeChanged(index, queueAdapter.torrentModels.size());
                        }

                    } else if (torrentModel.torrentHandleState != null && torrentModel.torrentHandleState == TorrentHandleState.DELETE) {
                        torrentModel.delete();
                        int index = queueAdapter.removeObject(torrentModel);
                        if (index != -1) {
                            queueAdapter.notifyItemRemoved(index);
                            queueAdapter.notifyItemRangeChanged(index, queueAdapter.torrentModels.size());
                        }
                    }
                    showErrorText(queueAdapter.torrentModels);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloads_fragment, container, false);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        setListener(torrentServiceListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListener(null);
    }

    private void setListener(TorrentServiceListener torrentServiceListener) {
        try {
            if (torrentServiceListener == null) {
                ListenerSingleton.getInstance().serviceListenerMap.remove(key);
            } else {
                ListenerSingleton.getInstance().serviceListenerMap.put(key, torrentServiceListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showList();

    }

    private void showErrorText(List<TorrentModel> torrentModels) {
        errorText.setVisibility(View.GONE);
        if (downloadFinish != null) {
            switch (downloadFinish) {
                case DOWNLOADING:
                    errorText.setText(getString(R.string.no_queue_torrents));
                    break;
                case FINISHED:
                    errorText.setText(getString(R.string.no_finished));
            }
            if (torrentModels != null && torrentModels.size() == 0) {
                errorText.setVisibility(View.VISIBLE);
            }
        }
    }

    void sendBroadCast(TorrentModel torrentModel, UploadDownload uploadDownload) {
        Intent intent = new Intent(TorrentService.UPDATE_SPEED);
        intent.putExtra("uploadDownload", uploadDownload);
        intent.putExtra("torrent", torrentModel.FilePath);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private void showList() {
        try {
            Bundle bundle = getArguments();
            if (bundle != null) {
                downloadFinish = (DownloadFinish) bundle.getSerializable("downloadFinish");
            }


            List<TorrentModel> torrentModels = null;

            if (downloadFinish != null) {
                switch (downloadFinish) {
                    case DOWNLOADING:
                        torrentModels = TorrentModel.getPartiallyDownloads();
                        break;
                    case FINISHED:
                        torrentModels = TorrentModel.getCompletedDownloads();

                }
            }

            showErrorText(torrentModels);
            if (queueAdapter == null) {
                queueAdapter = new QueueAdapter(torrentModels, new OnItemClickRecyclerListener() {
                    @Override
                    public void onItemClicked(View view, int position) {
                        final TorrentModel torrentModel = queueAdapter.torrentModels.get(position);

                        int id = view.getId();
                        if (id == R.id.pauseResume) {
                            Constant.startTorrent(appCompatActivity, new File(torrentModel.FilePath),
                                    (torrentModel.torrentHandleState == null ||
                                            torrentModel.torrentHandleState == TorrentHandleState.PAUSE) ? TorrentHandleState.RESUME :
                                            TorrentHandleState.PAUSE);
                        } else if (id == R.id.moreMenu) {
                            PopupMenu popupMenu = new PopupMenu(context, view);
                            popupMenu.getMenuInflater().inflate(R.menu.torrent_menu, popupMenu.getMenu());
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    int id = item.getItemId();
                                    if (id == R.id.delete) {
                                        Constant.startTorrent(appCompatActivity, new File(torrentModel.FilePath),
                                                TorrentHandleState.DELETE);
                                    } else if (id == R.id.uploadLimit) {
                                        new ShowUploadLimit(context, torrentModel, UploadDownload.UPLOAD_LIMIT, new LimitInterface() {
                                            @Override
                                            public void selectedSpeedInBytes(int selectedSpeed) {
                                                if (selectedSpeed == 0 && torrentModel.UploadLimit != null) {
                                                    torrentModel.UploadLimit = null;
                                                    torrentModel.save();
                                                    sendBroadCast(torrentModel, UploadDownload.UPLOAD_LIMIT);
                                                } else if (selectedSpeed > 0) {
                                                    torrentModel.UploadLimit = selectedSpeed;
                                                    torrentModel.save();
                                                    sendBroadCast(torrentModel, UploadDownload.UPLOAD_LIMIT);
                                                }
                                            }
                                        });
                                    } else if (id == R.id.downloadLimit) {
                                        new ShowUploadLimit(context, torrentModel, UploadDownload.DOWNLOAD_LIMIT, new LimitInterface() {
                                            @Override
                                            public void selectedSpeedInBytes(int selectedSpeed) {
                                                if (selectedSpeed == 0 && torrentModel.DownloadLimit != null) {
                                                    torrentModel.DownloadLimit = null;
                                                    torrentModel.save();
                                                    sendBroadCast(torrentModel, UploadDownload.DOWNLOAD_LIMIT);
                                                } else if (selectedSpeed > 0) {
                                                    torrentModel.DownloadLimit = selectedSpeed;
                                                    torrentModel.save();
                                                    sendBroadCast(torrentModel, UploadDownload.DOWNLOAD_LIMIT);
                                                }
                                            }
                                        });
                                    } else if (id == R.id.openFoler) {
                                        Constant.openFolder(appCompatActivity,new File(torrentModel.FilePath).getParent());
                                    }

                                    return false;
                                }
                            });
                            popupMenu.show();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("torrentModel", torrentModel);
                            MainActivity.bringFragment(appCompatActivity, TorrentDetailsBase.newInstance(bundle), true);
                        }


                    }
                }, downloadFinish);

            } else {
                queueAdapter.setTorrentModels(torrentModels);
                queueAdapter.notifyDataSetChanged();
            }
            recyclerView.setAdapter(queueAdapter);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
