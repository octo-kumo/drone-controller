package me.kumo.drone.map;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapTileLoader {
    private static final String TILE_URL_TEMPLATE = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png";

    public static byte[] loadTileData(int tileX, int tileY, int zoom) throws Exception {
        String urlStr = TILE_URL_TEMPLATE
                .replace("{z}", Integer.toString(zoom))
                .replace("{x}", Integer.toString(tileX))
                .replace("{y}", Integer.toString(tileY));
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Kumo-Drone-Control-APP v0.0.1");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        in.close();
        return buffer.toByteArray();
    }
}
