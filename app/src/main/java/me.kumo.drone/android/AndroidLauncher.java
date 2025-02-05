package me.kumo.drone.android;

import com.jme3.app.AndroidHarness;
import me.kumo.drone.game.DroneGroundStation;


public class AndroidLauncher extends AndroidHarness {

    public AndroidLauncher() {
        appClass = DroneGroundStation.class.getCanonicalName();
    }
}
