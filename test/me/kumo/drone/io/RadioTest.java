package me.kumo.drone.io;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.Test;

public class RadioTest {
    @Test
    public void testRadio() throws InterruptedException {
        for (SerialPort commPort : SerialPort.getCommPorts()) {
            System.out.println(commPort.getPortDescription());
            if ("FT231X USB UART".equals(commPort.getPortDescription())) {
                System.out.println("Found the radio");
                System.out.println(commPort.getPortDescription());
                System.out.println(commPort.openPort());
                commPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
                commPort.writeBytes("Hello, world!".getBytes(), "Hello, world!".length());
                while (commPort.isOpen()) {
                    byte[] readBuffer = new byte[8];
                    int numRead = commPort.readBytes(readBuffer, readBuffer.length);
                    if (numRead > 0) {
                        System.out.println("Read " + numRead + " bytes");
                        System.out.println(new String(readBuffer, 0, numRead));
                    }
                    Thread.sleep(100);
                    commPort.writeBytes("Hello, world!".getBytes(), "Hello, world!".length());
                }
            }
        }
    }
}
