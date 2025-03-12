package me.kumo.drone.map;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapLayer extends Node {

    private final AssetManager assetManager;
    private final Camera camera;
    private final SimpleApplication app;
    private final Vector3f gpsOrigin;
    private final float worldUnitsPerGpsUnit;
    private final float viewingDistance;
    private int currentZoom;
    private final ExecutorService executor;
    private final ConcurrentHashMap<String, MapTile> activeTiles = new ConcurrentHashMap<>();

    public MapLayer(AssetManager assetManager, Camera cam, SimpleApplication app, Vector3f gpsOrigin,
                    float worldUnitsPerGpsUnit, float viewingDistance) {
        this.assetManager = assetManager;
        this.camera = cam;
        this.app = app;
        this.gpsOrigin = gpsOrigin;
        this.worldUnitsPerGpsUnit = worldUnitsPerGpsUnit;
        this.viewingDistance = viewingDistance;
        this.currentZoom = computeZoomFromCameraHeight();
        executor = Executors.newFixedThreadPool(4);
    }

    public Vector3f worldToGPS(Vector3f worldPos) {
        float deltaLon = worldPos.x / worldUnitsPerGpsUnit;
        float deltaLat = -worldPos.z / worldUnitsPerGpsUnit;
        return new Vector3f(gpsOrigin.x + deltaLon, 0, gpsOrigin.z + deltaLat);
    }

    /**
     * Converts GPS coordinates to Google tile indices at the given zoom level.
     */
    public int[] gpsToTile(Vector3f gps, int zoom) {
        double lat = gps.z;
        double lon = gps.x;
        int tileCount = 1 << zoom;
        int tileX = (int) Math.floor((lon + 180.0) / 360.0 * tileCount);
        double latRad = Math.toRadians(lat);
        int tileY = (int) Math.floor((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * tileCount);
        return new int[]{tileX, tileY};
    }

    /**
     * Chooses a zoom level based on the camera height (heuristic).
     */
    private int computeZoomFromCameraHeight() {
        float height = camera.getLocation().y;
        if (height < 50) return 18;
        else if (height < 100) return 16;
        else if (height < 200) return 14;
        else if (height < 400) return 12;
        else return 10;
    }

    /**
     * Called every frame to update the loaded tiles.
     */
    public void updateLayer(float tpf) {
        int zoom = computeZoomFromCameraHeight();
        if (zoom != currentZoom) {
            detachAllTiles();
            currentZoom = zoom;
        }

        Vector3f camPos = camera.getLocation();
        Vector3f camGps = worldToGPS(camPos);
        int[] centerTile = gpsToTile(camGps, currentZoom);
        int tilesAround = (int) Math.ceil((viewingDistance / worldUnitsPerGpsUnit) / 256);

        for (int dx = -tilesAround; dx <= tilesAround; dx++) {
            for (int dy = -tilesAround; dy <= tilesAround; dy++) {
                int tileX = centerTile[0] + dx;
                int tileY = centerTile[1] + dy;
                String key = currentZoom + "/" + tileX + "/" + tileY;
                if (!activeTiles.containsKey(key)) {
                    MapTile tile = new MapTile(tileX, tileY, currentZoom);
                    activeTiles.put(key, tile);
                    loadTileAsync(tile, key);
                }
            }
        }

        // Unload tiles that are too far from the camera.
        activeTiles.forEach((key, tile) -> {
            Vector3f tileWorldPos = tileToWorldPosition(tile.tileX, tile.tileY, tile.zoom);
            if (tileWorldPos.distance(camPos) > viewingDistance * 1.5f) {
                this.detachChild(tile.geometry);
                activeTiles.remove(key);
            }
        });
    }

    private void loadTileAsync(MapTile tile, String key) {
        executor.submit(() -> {
            try {
                TileCache cache = TileCache.getInstance();
                byte[] data;
                if (cache.contains(key)) {
                    data = cache.getTile(key);
                } else {
                    data = MapTileLoader.loadTileData(tile.tileX, tile.tileY, tile.zoom);
                    cache.putTile(key, data);
                }
                tile.rawImageData = data;
                app.enqueue(() -> {
                    try {
                        IImageLoader loader = ImageLoaderFactory.getImageLoader();
                        com.jme3.texture.Image img = loader.loadImage(tile.rawImageData, true);
                        tile.texture = new Texture2D(img);
                        Quad quad = new Quad(1, 1);
                        tile.geometry = new Geometry("Tile_" + key, quad);
                        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//                        mat.getAdditionalRenderState().setWireframe(true);
                        mat.setTexture("ColorMap", tile.texture);
                        tile.geometry.setMaterial(mat);
                        int tileCount = 1 << tile.zoom;
                        float tileSize = worldUnitsPerGpsUnit * (360f / tileCount);
                        Vector3f pos = tileToWorldPosition(tile.tileX, tile.tileY, tile.zoom);
                        Vector3f posNext = tileToWorldPosition(tile.tileX, tile.tileY + 1, tile.zoom);
                        tile.geometry.rotate(-FastMath.HALF_PI, 0, 0);
                        tile.geometry.scale(tileSize, posNext.z - pos.z, 1);
                        tile.geometry.setLocalTranslation(pos);
                        tile.geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                        this.attachChild(tile.geometry);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load tile: " + key);
                e.printStackTrace();
            }
        });
    }

    private Vector3f tileToWorldPosition(int tileX, int tileY, int zoom) {
        int tileCount = 1 << zoom;
        double lon = tileX / (double) tileCount * 360.0 - 180.0;
        double n = Math.PI - 2.0 * Math.PI * tileY / tileCount;
        double lat = Math.toDegrees(Math.atan(Math.sinh(n)));
        float deltaLon = (float) (lon - gpsOrigin.x);
        float deltaLat = (float) (lat - gpsOrigin.z);
        return new Vector3f(deltaLon * worldUnitsPerGpsUnit, 0, -deltaLat * worldUnitsPerGpsUnit);
    }

    private void detachAllTiles() {
        activeTiles.forEach((key, tile) -> {
            if (tile.geometry != null) this.detachChild(tile.geometry);
        });
        activeTiles.clear();
    }

    public void cleanup() {
        executor.shutdownNow();
    }
}
