package com.nikki.torrents.downloadService;

import com.nikki.torrents.interfaces.TorrentServiceListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nishi Sahlot on 3/9/2016.
 */
public class ListenerSingleton {
    public Map<String, TorrentServiceListener> serviceListenerMap = new HashMap<>();
    private static ListenerSingleton ourInstance = new ListenerSingleton();

    public static ListenerSingleton getInstance() {
        return ourInstance;
    }

    private ListenerSingleton() {
    }
}
