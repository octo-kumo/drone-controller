package me.kumo.drone.game;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.Updater;

public class EnvironmentManager {
    private static final int SHADOWMAP_SIZE = 1024;
    private final Node rootNode;
    private final Camera cam;
    private final ViewPort viewPort;
    private final AssetManager assetManager;
    SkyControl skyControl;

    public EnvironmentManager(AssetManager assetManager, Node rootNode, Camera cam, ViewPort viewPort) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.cam = cam;
        this.viewPort = viewPort;

        createGround();
        initializeLights();
        initializeSky();
    }

    private void createGround() {
        Box groundBox = new Box(1000f, 0, 1000f);
        Geometry ground = new Geometry("Ground", groundBox);
        Material groundMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        groundMat.setBoolean("UseMaterialColors", true);
        groundMat.setColor("Ambient", ColorRGBA.Green.interpolateLocal(ColorRGBA.Black, 0.8f));
        groundMat.setColor("Diffuse", ColorRGBA.Green.interpolateLocal(ColorRGBA.Black, 0.8f));
        ground.setMaterial(groundMat);
        ground.setLocalTranslation(0, 0, 0);
        ground.setShadowMode(RenderQueue.ShadowMode.Receive);
        rootNode.attachChild(ground);
    }

    private void initializeLights() {
        DirectionalLight mainLight = new DirectionalLight();
        Vector3f lightDirection = new Vector3f(-2f, -5f, 4f).normalize();
        mainLight.setColor(ColorRGBA.White.mult(1f));
        mainLight.setDirection(lightDirection);
        mainLight.setName("main");

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(mainLight);
        viewPort.addProcessor(dlsr);
        rootNode.addLight(mainLight);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
        dlsf.setLight(mainLight);
        dlsf.setEnabled(true);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.2f));
        ambientLight.setName("ambient");
        rootNode.addLight(ambientLight);

        // water filter
//        WaterFilter water = new WaterFilter(rootNode, lightDirection);
//        water.setCausticsIntensity(0.4f);
//        water.setColorExtinction(new Vector3f(30f, 50f, 70f));
//        ColorRGBA deepWaterColor = new ColorRGBA()
//                .setAsSrgb(0.0039f, 0.00196f, 0.145f, 1f);
//        water.setDeepWaterColor(deepWaterColor);
//        water.setFoamExistence(new Vector3f(0.8f, 8f, 1f));
//        water.setFoamHardness(0.3f);
//        water.setFoamIntensity(0.04f);
//        water.setMaxAmplitude(0.3f);
//        water.setReflectionDisplace(1f);
//        water.setRefractionConstant(0.25f);
//        water.setRefractionStrength(0.2f);
//        water.setUnderWaterFogDistance(80f);
//        ColorRGBA waterColor
//                = new ColorRGBA().setAsSrgb(0.0078f, 0.3176f, 0.5f, 1f);
//        water.setWaterColor(waterColor);
//        water.setWaterHeight(10f);
//        water.setWaterTransparency(0.4f);
//        water.setWaveScale(0.03f);
//        Texture2D foamTexture = (Texture2D) assetManager.loadTexture(
//                "Common/MatDefs/Water/Textures/foam2.jpg");
//        water.setFoamTexture(foamTexture);
//
//        int numSamples = getContext().getSettings().getSamples();
//        FilterPostProcessor fpp = Heart.getFpp(viewPort, assetManager, numSamples);
//        fpp.addFilter(water);
//        viewPort.addProcessor(fpp);
    }

    private void initializeSky() {
        float cloudFlattening = 0.8f;
        boolean bottomDome = true;
        skyControl = new SkyControl(assetManager, cam,
                cloudFlattening, StarsOption.Cube, bottomDome);
        rootNode.addControl(skyControl);
        skyControl.setCloudiness(0.2f);
        skyControl.setCloudsYOffset(0.4f);
        skyControl.getSunAndStars().setHour(12f);
        skyControl.setEnabled(true);

        Updater updater = skyControl.getUpdater();
        for (Light light : rootNode.getLocalLightList()) {
            String lightName = light.getName();
            switch (lightName) {
                case "ambient":
                    updater.setAmbientLight((AmbientLight) light);
                    break;
                case "main":
                    updater.setMainLight((DirectionalLight) light);
                    break;
                default:
            }
        }
    }
}
