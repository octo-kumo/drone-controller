package me.kumo.drone.logic;

import com.jme3.math.Vector2f;

public class GPSMapper {
    // Ground station GPS coordinate, used as the origin (0,0)
    private double originLatitude;
    private double originLongitude;

    public GPSMapper(double originLatitude, double originLongitude) {
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
    }

    public void setOrigin(double latitude, double longitude) {
        this.originLatitude = latitude;
        this.originLongitude = longitude;
    }

    /**
     * Converts a GPS coordinate to scene XY coordinate.
     * Note: The conversion must account for the curvature of the Earth and the fact that
     * lines of latitude and longitude are not parallel.
     * This is a placeholder method for future implementation.
     *
     * @param latitude  The GPS latitude.
     * @param longitude The GPS longitude.
     * @return a 2D vector (or a custom class) representing the XY position.
     */
    public Vector2f gpsToXY(double latitude, double longitude) {
        // TODO: implement proper conversion logic. For now, a naive offset:
        float x = (float) ((longitude - originLongitude) * 1); // scaling factor placeholder
        float y = (float) ((latitude - originLatitude) * 1);
        return new Vector2f(x, y);
    }
}
