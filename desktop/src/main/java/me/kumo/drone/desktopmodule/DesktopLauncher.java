package me.kumo.drone.desktopmodule;

import com.jme3.system.AppSettings;
import me.kumo.drone.game.DroneGroundStation;

/**
 * Used to launch a jme application in desktop environment
 *
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        DroneGroundStation mainStage = new DroneGroundStation();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Drone Ground Station");
        settings.setResolution(1280, 720);
        settings.setSamples(4);
        settings.setVSync(true);
        settings.setResizable(true);
        mainStage.setSettings(settings);
        mainStage.setShowSettings(false);
        mainStage.start();
    }
}
