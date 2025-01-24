package me.kumo.drone.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;
import me.kumo.drone.io.Radio;

public class MainStage extends SimpleApplication {
    private final Radio radio = new Radio();

    public MainStage() {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Drone Control");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setVSync(true);
        settings.setResizable(true);

        setSettings(settings);
    }

    @Override
    public void simpleInitApp() {
        createGUI();
        createStage();
    }

    private void createGUI() {
        GuiGlobals.initialize(this);
//        BaseStyles.loadStyleResources("me/kumo/drone/styles/styles.groovy");
//        GuiGlobals.getInstance().getStyles().setDefaultStyle("kumo");
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        Container gui = new Container();
        guiNode.attachChild(gui);
        gui.setLocalTranslation(0, settings.getHeight(), 0);
        gui.addChild(new Label("Drone Control"));
        Button clickMe = gui.addChild(new Button("Radio Control"));
    }

    private void createStage() {
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "me/kumo/drone/textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/reflect.j3md");
//        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        flyCam.setDragToRotate(true);
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}