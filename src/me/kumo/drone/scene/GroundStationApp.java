package me.kumo.drone.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.BaseStyles;
import jme3utilities.MyCamera;
import me.kumo.drone.io.SerialDataHandler;
import me.kumo.drone.logic.DroneData;
import me.kumo.drone.logic.GPSMapper;

public class GroundStationApp extends SimpleApplication {

    private SerialDataHandler serialHandler;
    private GPSMapper gpsMapper;
    private DroneEntity drone;
    private MiniDroneDisplay miniDroneDisplay;
    private EnvironmentManager envManager;
    private VersionedReference<Double> skyHourRef;

    private ProgressBar battery;
    private Label gps;
    private Label pressure;
    private Label altitude;

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
        System.out.println(hud.getBackground());
        hud.setLocalTranslation(settings.getWidth() - 300, settings.getHeight() - 20, 0);
        altitude = hud.addChild(new Label("Altitude: 0 m"));
        gps = hud.addChild(new Label("GPS: 0, 0"));
        pressure = hud.addChild(new Label("Pressure: 0 hPa"));
        hud.addChild(new Label("Battery"));
        battery = hud.addChild(new ProgressBar(new DefaultRangedValueModel(0, 100, 100)));
        battery.setPreferredSize(new Vector3f(200, 20, 0));
        guiNode.attachChild(hud);

        Container portSelection = new Container();
        portSelection.setLocalTranslation(settings.getWidth() - 300, 100, 0);
        portSelection.addChild(new Label("Select Serial Port:"));
        Button port1 = portSelection.addChild(new Button("COM1"));
        port1.addClickCommands(source -> serialHandler.openPort("COM1"));
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

    @Override
    public void simpleUpdate(float tpf) {
        if (serialHandler.hasNewData()) {
            DroneData data = serialHandler.getLatestData();
            drone.updateFromData(data, gpsMapper);
            miniDroneDisplay.updateOrientation((float) data.pitch, (float) data.yaw, (float) data.roll);

            battery.getModel().setValue(data.battery);
            gps.setText(String.format("GPS: %.6f, %.6f", data.latitude, data.longitude));
            altitude.setText(String.format("Altitude: %.2f m", data.altitude));
            pressure.setText(String.format("Pressure: %.2f hPa", data.pressure));
        }

        if (skyHourRef.update()) {
            envManager.skyControl.getSunAndStars().setHour(skyHourRef.get().floatValue());
        }
    }
}
