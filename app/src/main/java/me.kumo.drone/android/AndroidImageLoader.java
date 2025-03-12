package me.kumo.drone.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;

import java.nio.ByteBuffer;

public class AndroidImageLoader implements IImageLoader {
    @Override
    public Image loadImage(byte[] data, boolean flipY) throws Exception {
        // Decode the JPEG data into a Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        if (bitmap == null) {
            throw new Exception("Failed to decode JPEG data");
        }

        // Flip the image vertically if required
        if (flipY) {
            Matrix matrix = new Matrix();
            matrix.postScale(1, -1);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        // Convert the Bitmap to a format jME can use
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();

        // Create and return the jME Image
        return new Image(Format.RGBA8, width, height, buffer);
    }
}
