package me.kumo.drone.io;

import me.kumo.drone.RadioWindow;
import net.codecrete.usb.Usb;
import net.codecrete.usb.UsbDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.swing.*;
import java.util.Collection;

public class Radio {
    public final RadioWindow window;

    public Radio() {
        this.window = new RadioWindow(this);
    }

    public @NotNull @Unmodifiable Collection<UsbDevice> getDevices() {
        return Usb.getDevices();
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            window.refresh();
            window.setVisible(true);
            window.toFront();
        });
    }
}
