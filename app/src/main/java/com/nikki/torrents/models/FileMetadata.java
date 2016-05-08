package com.nikki.torrents.models;


import com.frostwire.jlibtorrent.Priority;

import java.io.Serializable;

/**
 * Created by Nishi Sahlot on 3/26/2016.
 */
public class FileMetadata implements Serializable{
    public int fileIndex;
    public String fileName;
    public long size;
    public Priority priority=Priority.NORMAL;

}
