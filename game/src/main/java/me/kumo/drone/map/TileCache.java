package me.kumo.drone.map;

import java.util.concurrent.ConcurrentHashMap;

public class TileCache {
    private static TileCache instance;
    private final ConcurrentHashMap<String, byte[]> cache;

    private TileCache() {
        cache = new ConcurrentHashMap<>();
    }

    public static synchronized TileCache getInstance() {
        if (instance == null) {
            instance = new TileCache();
        }
        return instance;
    }

    public byte[] getTile(String key) {
        return cache.get(key);
    }

    public void putTile(String key, byte[] data) {
        cache.put(key, data);
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }
}
