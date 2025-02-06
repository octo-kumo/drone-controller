package me.kumo.drone.logic;

public class DroneData {
    // IMU Data
    public double roll;
    public double pitch;
    public double yaw;

    // GPS Data
    public double latitude;
    public double longitude;
    public double altitude;
    public float groundSpeed;
    public int satellites;
    public boolean gpsFix;

    // Barometer Data
    public float temperature;
    public double pressure;
    public float baroAltitude;
    public float seaLevelPressure;

    // Status Data
    public boolean armed;
    public int battery;
    public int checksum;
}