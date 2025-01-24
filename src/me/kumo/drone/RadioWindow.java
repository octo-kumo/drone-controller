package me.kumo.drone;

import me.kumo.drone.io.Radio;
import net.codecrete.usb.UsbDevice;
import net.codecrete.usb.UsbException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class RadioWindow extends JFrame {
    private final Radio radio;
    private final JComboBox<UsbDevice> selector;
    private final JTextArea logArea;
    private UsbDevice selectedDevice;

    public RadioWindow(Radio radio) {
        this.radio = radio;
        setTitle("Radio Control");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        selector = new JComboBox<>();
        selector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UsbDevice device) {
                    setText(device.getManufacturer() + " " + device.getProduct());
                }
                return this;
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selector, gbc);

        JSpinner interfaceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        gbc.gridx = 1;
        panel.add(interfaceSpinner, gbc);

        JButton connect = getConnectBTN(interfaceSpinner);
        gbc.gridx = 2;
        panel.add(connect, gbc);


        // Separator
        JSeparator separator = new JSeparator();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(separator, gbc);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setRows(10);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(logArea, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton button1 = new JButton("OK");
        JButton button2 = new JButton("Cancel");
        buttonPanel.add(button1);
        buttonPanel.add(button2);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(buttonPanel, gbc);
        add(panel);
        new Thread(this::readFromUSB).start();
    }

    private @NotNull JButton getConnectBTN(JSpinner interfaceSpinner) {
        JButton connect = new JButton("Connect");
        connect.addActionListener(e -> {
            UsbDevice device = (UsbDevice) selector.getSelectedItem();
            if (device != null) {
                if (selectedDevice != null) selectedDevice.close();
                selectedDevice = null;
                try {
                    device.open();
//                    device.claimInterface((Integer) interfaceSpinner.getValue());
                    selectedDevice = device;
                } catch (UsbException ex) {
                    device.close();
                    JOptionPane.showMessageDialog(this, "USB Stall\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        return connect;
    }

    private void readFromUSB() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (selectedDevice == null) {
                continue;
            }
            byte[] data = selectedDevice.transferIn(3); // Assuming endpoint 3 is for IN transfers
            if (data.length > 0) {
                String message = new String(data);
                logArea.append(message + "\n");
            }
        }
    }

    public void refresh() {
        selector.removeAllItems();
        for (UsbDevice device : radio.getDevices()) {
            selector.addItem(device);
        }
    }
}
