package me.kumo.drone.desktopmodule;


import com.fazecast.jSerialComm.SerialPort;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TelemetryReader {
    // Constants from original code
    private static final byte[] HEADER = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
    private static final int MAX_BUFFER_SIZE = 1024;
    private static final int TELEMETRY_SIZE = 32; // 7 floats (28) + 1 int (4) + 2 bytes + 2 bytes = 36 bytes
    private static final int BARO_SIZE = 16; // 4 floats = 16 bytes
    private static final double UPDATE_INTERVAL = 0.5; // Seconds between updates

    private SerialPort serialPort;

    public TelemetryReader(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(57600); // Set your baud rate here
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);
    }

    private byte[] readPacket(int expectedSize) {
        int headerIndex = 0;
        long startTime = System.currentTimeMillis();

        // Search for header
        while (headerIndex < HEADER.length) {
            if (System.currentTimeMillis() - startTime > 2000) { // 2 second timeout
                return null;
            }

            byte[] buffer = new byte[1];
            int bytesRead = serialPort.readBytes(buffer, 1);

            if (bytesRead < 1) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                continue;
            }

            if (buffer[0] == HEADER[headerIndex]) {
                headerIndex++;
            } else {
                headerIndex = (buffer[0] == HEADER[0]) ? 1 : 0;
            }

            if (headerIndex > MAX_BUFFER_SIZE) {
                System.out.println("Warning: Buffer overflow prevented");
                return null;
            }
        }

        // Read packet data
        byte[] packetBuffer = new byte[expectedSize];
        int bytesRead = serialPort.readBytes(packetBuffer, expectedSize);

        if (bytesRead != expectedSize) {
            System.out.printf("Warning: Invalid packet size %d != %d%n", bytesRead, expectedSize);
            return null;
        }

        return packetBuffer;
    }

    public TelemetryData readTelemetryPacket() {
        byte[] packetBytes = readPacket(TELEMETRY_SIZE);
        if (packetBytes == null) return null;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(packetBytes).order(ByteOrder.LITTLE_ENDIAN);

            TelemetryData data = new TelemetryData();
            data.roll = buffer.getFloat();
            data.pitch = buffer.getFloat();
            data.yaw = buffer.getFloat();
            data.altitude = buffer.getFloat();
            data.latitude = buffer.getFloat();
            data.longitude = buffer.getFloat();
            data.groundspeed = buffer.getFloat();
            data.satellites = buffer.getInt();
            data.armed = buffer.get() != 0;
            data.gpsFix = buffer.get() != 0;
            data.checksum = buffer.getShort() & 0xFFFF;

            return data;
        } catch (Exception e) {
            System.out.println("Warning: Telemetry packet parsing error: " + e.getMessage());
            return null;
        }
    }

    public BarometerData readBaroPacket() {
        byte[] packetBytes = readPacket(BARO_SIZE);
        if (packetBytes == null) return null;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(packetBytes).order(ByteOrder.LITTLE_ENDIAN);

            BarometerData data = new BarometerData();
            data.temperature = buffer.getFloat();
            data.pressure = buffer.getFloat();
            data.altitude = buffer.getFloat();
            data.seaLevelPressure = buffer.getFloat();

            return data;
        } catch (Exception e) {
            System.out.println("Warning: Barometer packet parsing error: " + e.getMessage());
            return null;
        }
    }

    public void start() {
        if (!serialPort.openPort()) {
            System.out.println("Failed to open port");
            return;
        }

        System.out.println("Starting telemetry receiver...");
        long lastUpdate = 0;

        try {
            while (!Thread.currentThread().isInterrupted()) {
                TelemetryData telemetry = readTelemetryPacket();
                BarometerData baro = readBaroPacket();
                long currentTime = System.currentTimeMillis();

                if (telemetry != null && baro != null &&
                        (currentTime - lastUpdate) >= (UPDATE_INTERVAL * 1000)) {
                    lastUpdate = currentTime;

                    System.out.println("=== Received Telemetry ===");

                    System.out.println("--- IMU Data ---");
                    System.out.printf("Roll: %.2f°  Pitch: %.2f°  Yaw: %.2f°%n",
                            telemetry.roll, telemetry.pitch, telemetry.yaw);

                    System.out.println("\n--- GPS Data ---");
                    System.out.printf("Position: %.6f°N, %.6f°E%n",
                            telemetry.latitude, telemetry.longitude);
                    System.out.printf("Altitude (Telemetry): %.1f m%n", telemetry.altitude);
                    System.out.printf("Ground Speed: %.1f km/h%n", telemetry.groundspeed);
                    System.out.printf("Satellites: %d  GPS Fix: %s%n",
                            telemetry.satellites, telemetry.gpsFix ? "YES" : "NO");

                    System.out.println("\n--- Flight Status ---");
                    System.out.printf("Armed: %s%n", telemetry.armed ? "YES" : "NO");

                    System.out.println("\n=== Barometer Data (BMP180) ===");
                    System.out.printf("Temperature: %.2f °C%n", baro.temperature);
                    System.out.printf("Pressure: %.2f mbar%n", baro.pressure);
                    System.out.printf("Calculated Altitude: %.2f m%n", baro.altitude);
                    System.out.printf("Calibrated Sea Level Pressure: %.2f mbar%n",
                            baro.seaLevelPressure);

                    System.out.println("=====================");
                }
            }
        } finally {
            serialPort.closePort();
        }
    }

    public static void main(String[] args) {

        TelemetryReader reader = new TelemetryReader("COM8");
        reader.start();
    }

    // Data classes
    public static class TelemetryData {
        public float roll, pitch, yaw, altitude, latitude, longitude, groundspeed;
        public int satellites;
        public boolean armed, gpsFix;
        public int checksum;
    }

    public static class BarometerData {
        public float temperature, pressure, altitude, seaLevelPressure;
    }
}