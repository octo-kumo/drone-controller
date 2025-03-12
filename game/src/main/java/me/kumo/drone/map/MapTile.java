package me.kumo.drone.map;

import com.jme3.scene.Geometry;
import com.jme3.texture.Texture2D;

public class MapTile {
    public final int tileX, tileY, zoom;
    public byte[] rawImageData;   // Raw JPEG data
    public Texture2D texture;     // Texture created from the image
    public Geometry geometry;     // Geometry displaying the tile

    public MapTile(int tileX, int tileY, int zoom) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.zoom = zoom;
    }

    public String getKey() {
        return zoom + "/" + tileX + "/" + tileY;
    }
}
