package me.kumo.drone.io;

import com.fazecast.jSerialComm.SerialPort;
import me.kumo.drone.logic.DroneData;

import java.io.InputStream;

public class SerialDataHandler {
    private SerialPort port;
    private boolean debugMode = false;
    private final byte[] buffer = new byte[1024];
    private final DroneData latestData = new DroneData();

    public void openPort(String portName) {
        closePort();
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(115200);
        if (port.openPort()) System.out.println("Port opened: " + portName);
        else System.out.println("Failed to open port: " + portName);
    }

    public void closePort() {
        if (port != null && port.isOpen()) {
            port.closePort();
            port = null;
        }
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
            latestData.altitude = 2 + 1 * Math.sin(System.currentTimeMillis() / 1000.0);

            latestData.latitude = 1 * Math.sin(System.currentTimeMillis() / 1700.0);
            latestData.longitude = 1 * Math.sin(System.currentTimeMillis() / 2100.0);

            latestData.roll = 100 * Math.sin(System.currentTimeMillis() / 3000.0);
            latestData.pitch = 100 * Math.sin(System.currentTimeMillis() / 4000.0);
            latestData.yaw = 100 * Math.sin(System.currentTimeMillis() / 5000.0);
            latestData.pressure = 1013.25;
            latestData.battery = (int) (50 + 50 * Math.sin(System.currentTimeMillis() / 10000.0));
        } else {
            try {
                InputStream in = port.getInputStream();
                int numRead = in.read(buffer);
                parseBuffer(buffer, numRead);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return latestData;
    }

    private void parseBuffer(byte[] buffer, int length) {
        // TODO: Implement parsing of incoming serial data
    }
}
