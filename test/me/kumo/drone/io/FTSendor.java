package me.kumo.drone.io;

import com.ftdichip.usb.FTDI;

import javax.usb3.IUsbDevice;
import javax.usb3.UsbHostManager;
import javax.usb3.exception.UsbException;
import java.util.Arrays;
import java.util.List;

import static com.ftdichip.usb.FTDIUtility.VENDOR_ID;

public class FTSendor {
    public static void main(String[] args) throws UsbException, InterruptedException {
        List<IUsbDevice> devices = UsbHostManager.getUsbDeviceList(VENDOR_ID, (short) 0x6015);
        System.out.println(devices);
        IUsbDevice usbDevice = devices.get(1);

        FTDI ftdiDevice = FTDI.getInstance(usbDevice);
        while (true) {
            ftdiDevice.write("A".getBytes());
            Thread.sleep(100);
            byte[] read = ftdiDevice.read();
            if (read.length > 0) {
                System.out.println(Arrays.toString(read));
            }
        }
    }
}
