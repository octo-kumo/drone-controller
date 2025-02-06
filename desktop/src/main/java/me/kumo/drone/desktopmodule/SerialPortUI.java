package me.kumo.drone.desktopmodule;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Objects;

public class SerialPortUI extends JFrame {

    private final JTextField sendField1;
    private final JTextField sendField2;
    private SerialPort selectedPort1;
    private SerialPort selectedPort2;

    public SerialPortUI() {

        setTitle("Serial Port Communication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        SerialPort[] ports = SerialPort.getCommPorts();


        JComboBox<String> portSelector1 = new JComboBox<>();
        JComboBox<String> portSelector2 = new JComboBox<>();
        portSelector1.addItem(null);
        portSelector2.addItem(null);
        for (SerialPort port : ports) {
            portSelector1.addItem(port.getSystemPortName());
            portSelector2.addItem(port.getSystemPortName());
        }


        JTextArea textArea1 = new JTextArea();
        textArea1.setRows(10);
        textArea1.setEditable(false);
        JTextArea textArea2 = new JTextArea();
        textArea2.setRows(10);
        textArea2.setEditable(false);


        sendField1 = new JTextField();
        sendField1.setEnabled(false);
        sendField1.addKeyListener(new SendFieldKeyListener(1));

        sendField2 = new JTextField();
        sendField2.setEnabled(false);
        sendField2.addKeyListener(new SendFieldKeyListener(2));


        JPanel leftPanel = createVBoxPanel("COM Port 1:", portSelector1, textArea1, sendField1);
        JPanel rightPanel = createVBoxPanel("COM Port 2:", portSelector2, textArea2, sendField2);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SerialPortUI::new);
    }

    private JPanel createVBoxPanel(String labelText, JComboBox<String> portSelector, JTextArea textArea, JTextField sendField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel(labelText));
        panel.add(portSelector);
        panel.add(new JScrollPane(textArea));
        panel.add(sendField);

        portSelector.addActionListener(e -> {
            String selectedPortName = (String) portSelector.getSelectedItem();
            SerialPort selectedPort = (labelText.contains("1")) ? selectedPort1 : selectedPort2;
            if (selectedPort != null) {
                selectedPort.closePort();
                selectedPort = null;
            }
            if (selectedPortName != null) {
                selectedPort = SerialPort.getCommPort(selectedPortName);
                selectedPort.setBaudRate(57600);
                selectedPort.openPort();
                selectedPort.addDataListener(new SerialPortDataListener(textArea));
                System.out.println("Opened port: " + selectedPortName + " " + selectedPort);
                sendField.setEnabled(true);
                if (labelText.contains("1")) {
                    selectedPort1 = selectedPort;
                } else {
                    selectedPort2 = selectedPort;
                }
            } else {
                sendField.setEnabled(false);
            }
        });

        return panel;
    }

    private static final class SerialPortDataListener implements com.fazecast.jSerialComm.SerialPortDataListener {
        private final JTextArea textArea;

        private SerialPortDataListener(
                JTextArea textArea) {
            this.textArea = textArea;
        }

        public JTextArea textArea() {
            return textArea;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            SerialPortDataListener that = (SerialPortDataListener) obj;
            return Objects.equals(this.textArea, that.textArea);
        }

        @Override
        public int hashCode() {
            return Objects.hash(textArea);
        }

        @Override
        public String toString() {
            return "SerialPortDataListener[" +
                    "textArea=" + textArea + ']';
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
            SerialPort port = (SerialPort) event.getSource();
            byte[] buffer = new byte[port.bytesAvailable()];
            port.readBytes(buffer, buffer.length);
            System.out.println(Arrays.toString(buffer));
            String data = new String(buffer);
            SwingUtilities.invokeLater(() -> textArea.append(data));
        }
    }

    private class SendFieldKeyListener implements KeyListener {
        private final int portNumber;

        public SendFieldKeyListener(int portNumber) {
            this.portNumber = portNumber;
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                SerialPort port = (portNumber == 1) ? selectedPort1 : selectedPort2;
                JTextField sendField = (portNumber == 1) ? sendField1 : sendField2;
                if (port != null) {
                    String text = sendField.getText();
                    port.writeBytes(text.getBytes(), text.getBytes().length);
                    sendField.setText("");
                }
            }
        }
    }
}