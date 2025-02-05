package me.kumo.drone;

import com.jme3.system.AppSettings;
import me.kumo.drone.scene.GroundStationApp;

public class Main {
    public static void main(String[] args) {
        GroundStationApp mainStage = new GroundStationApp();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Drone Ground Station");
        settings.setResolution(1280, 720);
        settings.setSamples(4);
        settings.setVSync(true);
        settings.setResizable(true);
        mainStage.setSettings(settings);
        mainStage.start();
    }
}
