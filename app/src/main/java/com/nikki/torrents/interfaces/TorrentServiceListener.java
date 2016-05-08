package com.nikki.torrents.interfaces;

import com.nikki.torrents.databaseModels.TorrentModel;

/**
 * Created by Nishi Sahlot on 3/6/2016.
 */
public interface TorrentServiceListener {

    void updateBlockProgress(TorrentModel torrentModel);
    void downloadFinish(TorrentModel torrentModel);
    void torrentAdded(TorrentModel torrentModel);

}
