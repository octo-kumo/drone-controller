package me.kumo.drone.map;

import com.jme3.texture.Image;

public interface IImageLoader {
    /**
     * Decodes the provided JPEG data into a jME Image.
     *
     * @param data  raw JPEG bytes
     * @param flipY whether to flip the image vertically
     * @return a jME Image
     * @throws Exception if decoding fails
     */
    Image loadImage(byte[] data, boolean flipY) throws Exception;
}
