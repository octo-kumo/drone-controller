package me.kumo.drone.desktopmodule.map;// DesktopImageLoader.java

import com.jme3.texture.Image;
import com.jme3.texture.plugins.AWTLoader;
import me.kumo.drone.map.IImageLoader;

import java.io.ByteArrayInputStream;

public class DesktopImageLoader implements IImageLoader {
    @Override
    public Image loadImage(byte[] data, boolean flipY) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        AWTLoader loader = new AWTLoader();
        return loader.load(bais, flipY);
    }
}
