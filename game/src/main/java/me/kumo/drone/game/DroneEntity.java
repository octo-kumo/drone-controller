package me.kumo.drone.game;

import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import me.kumo.drone.logic.DroneData;
import me.kumo.drone.logic.GPSMapper;

public class DroneEntity {

    private final Node modelNode;

    public DroneEntity(AssetManager assetManager) {
        modelNode = (Node) assetManager.loadModel("Models/Drone.glb");
        modelNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        addOrientationHelpers();
    }

    private void addOrientationHelpers() {
    }

    public Node getModelNode() {
        return modelNode;
    }

    public void updateFromData(DroneData data, GPSMapper mapper) {
        Vector2f pos2d = mapper.gpsToXY(data.latitude, data.longitude);
        modelNode.setLocalTranslation(pos2d.x, (float) data.altitude, pos2d.y);
        Quaternion orientation = new Quaternion();
        orientation.fromAngles((float) Math.toRadians(data.pitch), (float) Math.toRadians(data.yaw), (float) Math.toRadians(data.roll));
        modelNode.setLocalRotation(orientation);
    }
}
