package me.kumo.drone.game;

import com.fazecast.jSerialComm.SerialPort;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.BaseStyles;
import jme3utilities.MyCamera;
import me.kumo.drone.io.SerialDataHandler;
import me.kumo.drone.logic.DroneData;
import me.kumo.drone.logic.GPSMapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class DroneGroundStation extends SimpleApplication {

    private SerialDataHandler serialHandler;
    private GPSMapper gpsMapper;
    private DroneEntity drone;
    private MiniDroneDisplay miniDroneDisplay;
    private EnvironmentManager envManager;
    private VersionedReference<Double> skyHourRef;

    private Label gps, altitude, pressure, speed, satellites, temperature, baroAltitude, telemetryFrequency, baroFrequency;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        setDisplayStatView(false);

        gpsMapper = new GPSMapper(0.0, 0.0);
        serialHandler = new SerialDataHandler();
        envManager = new EnvironmentManager(assetManager, rootNode, cam, viewPort);
        drone = new DroneEntity(assetManager);
        rootNode.attachChild(drone.getModelNode());
        miniDroneDisplay = new MiniDroneDisplay(guiNode, assetManager);

        setupUI();
        setupCamera();

        MiniMapState minimap = new MiniMapState(rootNode, 64f, 200);
        stateManager.attach(minimap);
    }

    private void setupCamera() {
        cam.setLocation(new Vector3f(0, 2, -10));
        MyCamera.look(cam, new Vector3f(0, -1, 5));
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(10);
        flyCam.setRotationSpeed(2);
    }

    private void setupUI() {
        Container hud = new Container();
        TbtQuadBackgroundComponent c = (TbtQuadBackgroundComponent) hud.getBackground();
        c.setColor(new ColorRGBA(0, 0, 0, 1f));
        hud.setLocalTranslation(settings.getWidth() - 300, settings.getHeight() - 300, 0);

        altitude = hud.addChild(new Label("Altitude: 0 m"));
        gps = hud.addChild(new Label("GPS: 0, 0"));
        pressure = hud.addChild(new Label("Pressure: 0 hPa"));
        speed = hud.addChild(new Label("Speed: 0 m/s"));
        satellites = hud.addChild(new Label("Satellites: 0"));
        temperature = hud.addChild(new Label("Temperature: 0 C"));
        baroAltitude = hud.addChild(new Label("Baro Altitude: 0 m"));
        telemetryFrequency = hud.addChild(new Label("Telemetry Frequency: 0 Hz"));
        baroFrequency = hud.addChild(new Label("Baro Frequency: 0 Hz"));

        guiNode.attachChild(hud);

        Container portSelection = new Container();
        portSelection.setLocalTranslation(settings.getWidth() - 300, 100, 0);
        portSelection.addChild(new Label("Select Serial Port:"));
        portSelection.addChild(getPorts());

        Button debugMode = portSelection.addChild(new Button("Debug Mode"));
        debugMode.addClickCommands(source -> serialHandler.enableDebugMode());
        guiNode.attachChild(portSelection);

        Container controls = new Container();
        controls.setLocalTranslation(20, settings.getHeight() - 20, 0);
        Slider time = controls.addChild(new Slider(new DefaultRangedValueModel(0, 24, 12)));
        time.setPreferredSize(new Vector3f(200, 20, 0));
        skyHourRef = time.getModel().createReference();
        guiNode.attachChild(controls);
    }

    private Node getPorts() {
        Container ports = new Container();
        Container portList = new Container();
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        for (SerialPort port : serialPorts) {
            Button portButton = portList.addChild(new Button(port.getSystemPortName() + " (" + port.getPortDescription() + ")"));
            portButton.addClickCommands(source -> serialHandler.openPort(port.getSystemPortName()));
        }
        ports.addChild(portList);
        ports.addChild(new Button("Refresh")).addClickCommands(s -> {
            portList.clearChildren();
            for (SerialPort port : SerialPort.getCommPorts()) {
                Button portButton = portList.addChild(new Button(port.getSystemPortName() + " (" + port.getPortDescription() + ")"));
                portButton.addClickCommands(source -> serialHandler.openPort(port.getSystemPortName()));
            }
        });
        return ports;
    }

    @Override
    public void simpleUpdate(float tpf) {
        

        if (skyHourRef.update()) {
            envManager.skyControl.getSunAndStars().setHour(skyHourRef.get().floatValue());
        }

        executor.execute(() -> {
            if (serialHandler.hasNewData()) {
                DroneData data = serialHandler.getLatestData();
                enqueue(() -> {
                    drone.updateFromData(data, gpsMapper);
                    miniDroneDisplay.updateOrientation((float) data.pitch, (float) data.yaw, (float) data.roll);
        
                    gps.setText(String.format("GPS: %.6f, %.6f", data.latitude, data.longitude));
                    altitude.setText(String.format("Altitude: %.2f m", data.altitude));
                    pressure.setText(String.format("Pressure: %.2f hPa", data.pressure));
                    speed.setText(String.format("Speed: %.2f m/s", data.groundSpeed));
                    satellites.setText("Satellites: " + data.satellites);
                    temperature.setText(String.format("Temperature: %.2f C", data.temperature));
                    baroAltitude.setText(String.format("Baro Altitude: %.2f m", data.baroAltitude));
                    telemetryFrequency.setText(String.format("Telemetry Frequency: %.1f Hz", serialHandler.telemetryFrequency));
                    baroFrequency.setText(String.format("Baro Frequency: %.1f Hz", serialHandler.baroFrequency));
                });
            }
        });
    }
}
