package me.kumo.drone.io;

import com.fazecast.jSerialComm.SerialPort;
import me.kumo.drone.logic.DroneData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SerialDataHandler {
    // Packet format constants
    private static final byte[] HEADER = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
    private static final int MAX_BUFFER_SIZE = 1024;

    // The struct in Arduino is 36 bytes (7 floats + 1 int32 + 2 uint8 + 1 uint16)
    // 7 * 4 + 4 + 1 + 1 + 2 = 36 bytes
    private static final int TELEMETRY_SIZE = 36;
    private static final int FREQUENCY_WINDOW_SIZE = 10;
    private final long[] telemetryTimestamps = new long[FREQUENCY_WINDOW_SIZE];
    private int telemetryTimestampIndex = 0;
    public double telemetryFrequency = 0.0;
    public double baroFrequency = 0.0; // Keep for compatibility

    private SerialPort port;
    private boolean debugMode = false;
    private final byte[] buffer = new byte[MAX_BUFFER_SIZE];
    private final DroneData latestData = new DroneData();
    private int headerIndex = 0;
    private long lastReadAttempt = 0;
    private static final long READ_TIMEOUT = 2000; // 2 second timeout

    public void openPort(String portName) {
        debugMode = false;
        closePort();
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(57600);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);
        if (port.openPort()) System.out.println("Port opened: " + portName);
        else System.out.println("Failed to open port: " + portName);
    }

    public void closePort() {
        if (port != null && port.isOpen()) {
            port.closePort();
            port = null;
        }
        headerIndex = 0;
    }

    public void enableDebugMode() {
        closePort();
        debugMode = true;
        System.out.println("Debug mode enabled. Using dummy data.");
    }

    public boolean hasNewData() {
        return debugMode || (port != null && port.bytesAvailable() > 0);
    }

    public DroneData getLatestData() {
        if (debugMode) {
            generateDebugData();
        } else if (port != null && port.isOpen()) {
            readSerialData();
        }
        return latestData;
    }

    private void generateDebugData() {
        long currentTime = System.currentTimeMillis();
        latestData.altitude = 2 + 1 * Math.sin(currentTime / 1000.0);
        latestData.latitude = 1 * Math.sin(currentTime / 1700.0);
        latestData.longitude = 1 * Math.sin(currentTime / 2100.0);
        latestData.roll = 100 * Math.sin(currentTime / 3000.0);
        latestData.pitch = 100 * Math.sin(currentTime / 4000.0);
        latestData.yaw = 100 * Math.sin(currentTime / 5000.0);
        latestData.pressure = 1013.25;

        // Provide default values for barometer data
        latestData.baroAltitude = (float) latestData.altitude;
        latestData.temperature = 20.0F;
        latestData.seaLevelPressure = 1013.25F;
    }

    private void readSerialData() {
        try {
            // Only read telemetry packet now
            byte[] telemetryPacket = readPacket(TELEMETRY_SIZE);
            if (telemetryPacket != null) {
                // Debug: print received packet size and first few bytes
                System.out.println("Received packet of size: " + telemetryPacket.length +
                        " bytes, first bytes: " + bytesToHex(telemetryPacket, 0, Math.min(16, telemetryPacket.length)));

                parseTelemetryPacket(telemetryPacket);

                // Set default values for barometer data
                latestData.baroAltitude = (float) latestData.altitude; // Use telemetry altitude
                latestData.temperature = 20.0F; // Default temperature
                latestData.pressure = 1013.25; // Default pressure
                latestData.seaLevelPressure = 1013.25F; // Default sea level pressure

                // Update barometer frequency to match telemetry for compatibility
                baroFrequency = telemetryFrequency;
            }
        } catch (Exception e) {
            System.err.println("Error in readSerialData: " + e);
            e.printStackTrace();
            headerIndex = 0;
        }
    }

    private byte[] readPacket(int expectedSize) {
        if (System.currentTimeMillis() - lastReadAttempt > READ_TIMEOUT) {
            headerIndex = 0;
        }
        lastReadAttempt = System.currentTimeMillis();

        // Search for header
        while (headerIndex < HEADER.length) {
            byte[] singleByte = new byte[1];
            int bytesRead = port.readBytes(singleByte, 1);

            if (bytesRead < 1) {
                return null;
            }

            if (singleByte[0] == HEADER[headerIndex]) {
                headerIndex++;
            } else {
                headerIndex = (singleByte[0] == HEADER[0]) ? 1 : 0;
            }

            if (headerIndex > MAX_BUFFER_SIZE) {
                System.out.println("Warning: Buffer overflow prevented");
                headerIndex = 0;
                return null;
            }
        }

        // Reset header index for next packet
        headerIndex = 0;

        // Read packet data
        byte[] packetBuffer = new byte[expectedSize];
        int bytesRead = port.readBytes(packetBuffer, expectedSize);

        if (bytesRead != expectedSize) {
            System.out.printf("Warning: Invalid packet size %d != %d%n", bytesRead, expectedSize);
            return null;
        }

        return packetBuffer;
    }

    private void parseTelemetryPacket(byte[] packetData) {
        if (packetData == null || packetData.length < 1) {
            System.err.println("Error: Empty packet data received");
            return;
        }

        if (packetData.length < TELEMETRY_SIZE) {
            System.err.println("Telemetry packet size mismatch: expected " + TELEMETRY_SIZE + ", got " + packetData.length);
            System.err.println("Packet data: " + bytesToHex(packetData));
            return;
        }

        // Update frequency calculation
        telemetryTimestamps[telemetryTimestampIndex] = System.currentTimeMillis();
        telemetryTimestampIndex = (telemetryTimestampIndex + 1) % FREQUENCY_WINDOW_SIZE;
        telemetryFrequency = calculateFrequency(telemetryTimestamps);

        try {
            ByteBuffer buffer = ByteBuffer.wrap(packetData).order(ByteOrder.LITTLE_ENDIAN);

            // Safely read data from buffer with checks
            if (buffer.remaining() >= 4) latestData.roll = buffer.getFloat();
            if (buffer.remaining() >= 4) latestData.pitch = buffer.getFloat();
            if (buffer.remaining() >= 4) latestData.yaw = buffer.getFloat();
            if (buffer.remaining() >= 4) latestData.altitude = buffer.getFloat();

            float lat = 0, lon = 0;
            if (buffer.remaining() >= 4) lat = buffer.getFloat();
            if (buffer.remaining() >= 4) lon = buffer.getFloat();

            // Validate GPS data before using it
            if (!Float.isNaN(lat) && !Float.isNaN(lon) &&
                    lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                latestData.latitude = lat;
                latestData.longitude = lon;
            }

            if (buffer.remaining() >= 4) latestData.groundSpeed = buffer.getFloat();
            if (buffer.remaining() >= 4) latestData.satellites = buffer.getInt();
            if (buffer.remaining() >= 1) latestData.armed = buffer.get() != 0;
            if (buffer.remaining() >= 1) latestData.gpsFix = buffer.get() != 0;
            if (buffer.remaining() >= 2) latestData.checksum = buffer.getShort() & 0xFFFF;

            // Debug output for valid data
            System.out.printf("Valid telemetry data received - Alt: %.2f, Roll: %.2f, Pitch: %.2f, Yaw: %.2f%n",
                    latestData.altitude, latestData.roll, latestData.pitch, latestData.yaw);
        } catch (Exception e) {
            System.err.println("Error parsing telemetry packet: " + e);
            System.err.println("Packet data: " + bytesToHex(packetData));
            e.printStackTrace();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    private static String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            hex.append(String.format("%02X ", bytes[i]));
        }
        return hex.toString();
    }

    private double calculateFrequency(long[] timestamps) {
        long newest = timestamps[0];
        long oldest = timestamps[0];

        // Find newest and oldest timestamps
        for (long timestamp : timestamps) {
            if (timestamp > newest) newest = timestamp;
            if (timestamp < oldest && timestamp != 0) oldest = timestamp;
        }

        // If we don't have enough samples yet
        if (oldest == 0 || newest == oldest) {
            return 0.0;
        }

        // Calculate frequency in Hz
        double timespan = (newest - oldest) / 1000.0; // Convert to seconds
        int validSamples = 0;
        for (long timestamp : timestamps) {
            if (timestamp != 0) validSamples++;
        }

        return (validSamples - 1) / timespan; // -1 because we need gaps between samples
    }

    public double getTelemetryFrequency() {
        return telemetryFrequency;
    }

    public double getBaroFrequency() {
        return baroFrequency;
    }
}