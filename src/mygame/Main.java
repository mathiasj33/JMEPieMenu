package mygame;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import java.util.ArrayList;
import java.util.List;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener {

    private PieMenuState pms;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        stateManager.detach(stateManager.getState(FlyCamAppState.class));

        inputManager.addMapping("TAB", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, "TAB");

        initPieMenu();

        viewPort.setBackgroundColor(ColorRGBA.White);
        inputManager.setCursorVisible(false);
    }

    private void initPieMenu() {
        pms = new PieMenuState(assetManager, inputManager, cam, guiNode);
        pms.setRotatingWheel(true);
        pms.setOuterWheelImage("Textures/outerWheel.png");
        pms.setInnerWheelImage("Textures/innerWheel.png");
        pms.setWheelImage("Textures/wheel.png");
        pms.setNumNodes(3);
        pms.setRadius(280);
        pms.setNodeSize(100);
        pms.setDefaultCamHeight(720);
        pms.setAnimSpeed(4);
        List<String> paths = new ArrayList<>();
        paths.add("Textures/flame.png");
        paths.add("Textures/snowflake.png");
        paths.add("Textures/lightning.png");
        pms.setTextures(paths);
        pms.setNames("Fire", "Ice", "Lightning");
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("TAB")) {
            if (isPressed) {
                stateManager.attach(pms);
                pms.show();
            } else {
                stateManager.detach(pms);
                System.out.println("Selected item: " + pms.getSelectedItemName());
            }
        }
    }
}
