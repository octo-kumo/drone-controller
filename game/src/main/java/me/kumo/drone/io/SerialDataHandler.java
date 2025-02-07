package me.kumo.drone.io;

import com.fazecast.jSerialComm.SerialPort;
import me.kumo.drone.logic.DroneData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SerialDataHandler {
    // Packet format constants
    private static final byte[] HEADER = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
    private static final int MAX_BUFFER_SIZE = 1024;
    private static final int TELEMETRY_SIZE = 32; // 7 floats + 1 int + 2 bytes + 2 bytes
    private static final int BARO_SIZE = 16; // 4 floats

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
    }

    private void readSerialData() {
        try {
            
            // Read barometer packet
            byte[] baroPacket = readPacket(BARO_SIZE);
            if (baroPacket != null) {
                parseBaroPacket(baroPacket);
            }

            // Read telemetry packet
            byte[] telemetryPacket = readPacket(TELEMETRY_SIZE);
            if (telemetryPacket != null) {
                parseTelemetryPacket(telemetryPacket);
            }

        } catch (Exception e) {
            System.err.println("Error reading serial data: " + e);
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
        if (packetData.length < TELEMETRY_SIZE) {
            System.err.println("Telemetry packet size mismatch: expected " + TELEMETRY_SIZE + ", got " + packetData.length);
            return;
        }
    
        ByteBuffer buffer = ByteBuffer.wrap(packetData).order(ByteOrder.LITTLE_ENDIAN);
        // print in hex
        System.out.println("Telemetry Packet: " + bytesToHex(packetData));
        latestData.roll = buffer.getFloat();
        latestData.pitch = buffer.getFloat();
        latestData.yaw = buffer.getFloat();
        latestData.altitude = buffer.getFloat();
        latestData.latitude = buffer.getFloat();
        latestData.longitude = buffer.getFloat();
        latestData.groundSpeed = buffer.getFloat();
        latestData.satellites = buffer.getInt();
        latestData.armed = buffer.get() != 0;
        latestData.gpsFix = buffer.get() != 0;
        latestData.checksum = buffer.getShort() & 0xFFFF;
    }
    
    private void parseBaroPacket(byte[] packetData) {
        if (packetData.length < BARO_SIZE) {
            System.err.println("Barometer packet size mismatch: expected " + BARO_SIZE + ", got " + packetData.length);
            return;
        }
    
        ByteBuffer buffer = ByteBuffer.wrap(packetData).order(ByteOrder.LITTLE_ENDIAN);
    
        System.out.println("Telemetry Packet: " + bytesToHex(packetData));
        latestData.temperature = buffer.getFloat();
        latestData.pressure = buffer.getFloat();
        latestData.baroAltitude = buffer.getFloat();
        latestData.seaLevelPressure = buffer.getFloat();
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X ", b));
        }
        return hex.toString();
    }
}