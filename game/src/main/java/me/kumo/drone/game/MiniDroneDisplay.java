package me.kumo.drone.game;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class MiniDroneDisplay {
    private final Node hudNode;
    private Node miniDroneX;
    private Node miniDroneY;
    private Node miniDroneZ;

    public MiniDroneDisplay(Node guiNode, AssetManager assetManager) {
        hudNode = new Node("MiniDroneHUD");
        hudNode.setLocalTranslation(100, 100, 0);
        initMiniDrones(assetManager);
        guiNode.attachChild(hudNode);
    }

    private void initMiniDrones(AssetManager assetManager) {
        Node baseDrone = (Node) assetManager.loadModel("Models/Drone.glb");
        baseDrone.setLocalScale(100);
        miniDroneX = baseDrone.clone(false);
        miniDroneY = baseDrone.clone(false);
        miniDroneZ = baseDrone.clone(false);
        miniDroneX.setLocalRotation(new Quaternion().fromAngles(0, FastMath.HALF_PI, 0));
        miniDroneY.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
        miniDroneZ.setLocalRotation(Quaternion.IDENTITY);
        miniDroneX.setLocalTranslation(0, 0, 0);
        miniDroneY.setLocalTranslation(100, 0, 0);
        miniDroneZ.setLocalTranslation(200, 0, 0);
        hudNode.attachChild(miniDroneX);
        hudNode.attachChild(miniDroneY);
        hudNode.attachChild(miniDroneZ);
        hudNode.addLight(new DirectionalLight(new Vector3f(-1, -1, -1)));
    }

    public void updateOrientation(float pitch, float yaw, float roll) {
        Quaternion qpitch = new Quaternion().fromAngles(0, 0, FastMath.DEG_TO_RAD * pitch);
        Quaternion qyaw = new Quaternion().fromAngles(0, 0, FastMath.DEG_TO_RAD * yaw);
        Quaternion qroll = new Quaternion().fromAngles(0, 0, FastMath.DEG_TO_RAD * roll);
        miniDroneX.setLocalRotation(qpitch.mult(new Quaternion().fromAngles(0, FastMath.HALF_PI, 0)));
        miniDroneY.setLocalRotation(qyaw.mult(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0)));
        miniDroneZ.setLocalRotation(qroll);
    }
}
