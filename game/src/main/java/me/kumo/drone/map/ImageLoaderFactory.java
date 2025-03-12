package me.kumo.drone.map;

public class ImageLoaderFactory {
    private static IImageLoader instance;

    public static IImageLoader getImageLoader() {
        if (instance == null) {
            if (isAndroid()) {
                instance = createAndroidImageLoader();
            } else {
                instance = createDesktopImageLoader();
            }
        }
        return instance;
    }

    private static IImageLoader createAndroidImageLoader() {
        try {
            Class<?> androidLoaderClass = Class.forName("me.kumo.drone.android.AndroidImageLoader");
            return (IImageLoader) androidLoaderClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create AndroidImageLoader", e);
        }
    }

    private static IImageLoader createDesktopImageLoader() {
        try {
            Class<?> androidLoaderClass = Class.forName("me.kumo.drone.desktopmodule.map.DesktopImageLoader");
            return (IImageLoader) androidLoaderClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create AndroidImageLoader", e);
        }
    }

    private static boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
