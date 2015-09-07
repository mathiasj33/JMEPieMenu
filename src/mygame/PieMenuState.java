/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.state.AbstractAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathias
 */
public class PieMenuState extends AbstractAppState implements AnalogListener, ScaleFinishedListener, FadeFinishedListener {

    private boolean activated = false;
    private final static int MOUSE_RADIUS = 30;
    private float animSpeed = 2;
    private Vector3f mousePosition = new Vector3f(0, 0, 0);
    private Vector3f center;
    private final AssetManager assetManager;
    private final InputManager inputManager;
    private Light ambientLight;
    private Light selectionLight;
    private final Node guiNode;
    private final Camera camera;
    private Node innerWheelPivot;
    private Picture outerWheel;
    private Picture wheel;
    private boolean rotatingWheel;
    private float nodeAngle;
    private final List<Node> nodes;
    private String[] names;
    private List<String> texturePaths = new ArrayList<>();
    private int numNodes = 4;
    private float nodeSize = 0;
    private float radius = 0;
    private float defaultCamHeight = 1080;
    private boolean scaled;

    public PieMenuState(AssetManager assetManager, InputManager inputManager, Camera camera, Node guiNode) {
        this.nodes = new ArrayList<>();
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.camera = camera;
        this.guiNode = guiNode;
    }

    public void show() {
        initInputs();
        if (names == null) {
            throw new IllegalArgumentException("No names specified (Needed to find the selected Geometry)");
        }
        if (!rotatingWheel) {
            if (wheel == null) {
                throw new IllegalArgumentException("No wheel image specified");
            }
            guiNode.attachChild(wheel);
        } else {
            if (innerWheelPivot == null || outerWheel == null) {
                throw new IllegalArgumentException("No inner and outer wheel image specified");
            }
            guiNode.attachChild(innerWheelPivot);
            guiNode.attachChild(outerWheel);
        }

        if (!scaled) {
            scaleRadiusAndSize();
        }

        nodeAngle = FastMath.PI * 2 / numNodes;
        center = new Vector3f(camera.getWidth() / 2, camera.getHeight() / 2, 0);

        attachGeometries();
        initLights();
        playShowAnim();
        activated = true;
    }

    private void playShowAnim() {
        for (Node n : nodes) {
            n.addControl(new ScaleControl(true, animSpeed));
        }
        if (rotatingWheel) {
            innerWheelPivot.addControl(new ScaleControl(true, animSpeed));
            outerWheel.addControl(new FadeControl(true, animSpeed));
        } else {
            wheel.addControl(new FadeControl(true, animSpeed));
        }
    }

    private void playHideAnim() {
        ScaleControl sc = new ScaleControl(false, animSpeed);
        sc.setScaleFinishedListener(this);
        for (Node n : nodes) {
            n.addControl(sc.cloneForSpatial(n));
        }
        if (rotatingWheel) {
            innerWheelPivot.addControl(sc.cloneForSpatial(innerWheelPivot));
        }
        FadeControl fc = new FadeControl(false, animSpeed);
        fc.setFadeFinishedListener(this);
        if (rotatingWheel) {
            outerWheel.addControl(fc);
        } else {
            wheel.addControl(fc);
        }
    }

    private void scaleRadiusAndSize() {
        float proportion = (float) camera.getHeight() / defaultCamHeight;
        radius *= proportion;
        nodeSize *= proportion;
        scaled = true;
    }

    private Material generateMaterial(int index) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture(texturePaths.get(index)));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        return mat;
    }

    private void initInputs() {
        String[] mappings = new String[]{
            "Left",
            "Right",
            "Up",
            "Down",};

        inputManager.addMapping("Left", new MouseAxisTrigger(0, true));
        inputManager.addMapping("Right", new MouseAxisTrigger(0, false));
        inputManager.addMapping("Up", new MouseAxisTrigger(1, false));
        inputManager.addMapping("Down", new MouseAxisTrigger(1, true));

        inputManager.addListener(this, mappings);
    }

    private void initLights() {
        ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White);
        guiNode.addLight(ambientLight);

        selectionLight = new AmbientLight();
        selectionLight.setColor(ColorRGBA.Orange.mult(5));
    }

    public void setInnerWheelImage(String path) {
        Picture pic = getPicture(path);
        pic.setLocalTranslation(0, 0, 0);
        innerWheelPivot = new Node();
        innerWheelPivot.setLocalTranslation(camera.getWidth() / 2, camera.getHeight() / 2, -1);
        innerWheelPivot.attachChild(pic);
        pic.center();
    }

    public void setOuterWheelImage(String path) {
        outerWheel = getPicture(path);
        outerWheel.setLocalTranslation(camera.getWidth() / 2 - camera.getHeight() / 2, 0, -1);
    }

    public void setWheelImage(String path) {
        wheel = getPicture(path);
        wheel.setLocalTranslation(camera.getWidth() / 2 - camera.getHeight() / 2, 0, -1);
    }

    private Picture getPicture(String path) {
        Picture pic = new Picture("Picture");
        pic.setImage(assetManager, path, true);
        pic.setWidth(camera.getHeight());
        pic.setHeight(camera.getHeight());
        return pic;
    }

    private void attachGeometries() {
        Quaternion rotation = new Quaternion().fromAngleAxis(-nodeAngle, Vector3f.UNIT_Z.clone());
        Vector3f last = center.add(0, radius, 0);
        for (int i = 0; i < numNodes; i++) {
            attachGeometry(last.clone());
            if (texturePaths.size() > 0) {
                Material material = generateMaterial(i);
                nodes.get(i).setMaterial(material);
            }
            nodes.get(i).setName(names[i]);
            Vector3f dir = last.subtract(center);
            Vector3f newDir = rotation.mult(dir);
            last = center.add(newDir);
        }
    }

    private void attachGeometry(Vector3f vector) {
        Quad quad = new Quad(nodeSize, nodeSize);
        Geometry geom = new Geometry("Geom", quad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        Node quadNode = new Node();
        geom.setLocalTranslation(-nodeSize / 2, -nodeSize / 2, 0);
        quadNode.attachChild(geom);
        quadNode.setLocalTranslation(vector);
        nodes.add(quadNode);
        guiNode.attachChild(quadNode);
    }

    @Override
    public void update(float tpf) {
        if (!activated) {
            return;
        }
        calculateMousePosition();
        if (rotatingWheel) {
            rotateInnerWheel();
        }
        selectCurrentNode();
    }

    private void calculateMousePosition() {
        if (center.distance(mousePosition) > MOUSE_RADIUS) {
            Vector3f dir = mousePosition.subtract(center);
            float length = dir.length();
            float proportion = MOUSE_RADIUS / length;
            mousePosition = center.clone().interpolateLocal(mousePosition, proportion);
        }
    }

    private void rotateInnerWheel() {
        float currentAngle = getCurrentAngle();
        innerWheelPivot.setLocalRotation(new Quaternion().fromAngles(0, 0, -currentAngle));
    }

    private void selectCurrentNode() {
        for (Node node : nodes) {
            node.removeLight(selectionLight);
        }
        Node currentNode = getCurrentNode();
        currentNode.addLight(selectionLight);
    }

    private Node getCurrentNode() {
        return getNode(getCurrentAngle());
    }
    
    public String getSelectedItemName() {
        return getCurrentNode().getName();
    }

    private float getCurrentAngle() {
        Vector3f pos = mousePosition.clone();
        Vector3f dir = pos.subtract(center);
        float angle = Vector3f.UNIT_Y.clone().angleBetween(dir.normalize());
        if (isOnLeftSide()) {
            angle = 2 * FastMath.PI - angle;
        }
        return angle;
    }

    private boolean isOnLeftSide() {
        return mousePosition.x < camera.getWidth() / 2;
    }

    private Node getNode(float angle) {
        if (angleCloserToFirstNodeThanLastNode(angle)) {
            return nodes.get(0);
        }
        Node closestNode = null;
        float closestDifference = Float.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            float currentNodeAngle = nodeAngle * i;
            if (Math.abs(currentNodeAngle - angle) < closestDifference) {
                closestDifference = Math.abs(currentNodeAngle - angle);
                closestNode = nodes.get(i);
            }
        }
        return closestNode;
    }

    private boolean angleCloserToFirstNodeThanLastNode(float angle) {
        return angle > nodeAngle * (nodes.size() - 1) + nodeAngle / 2;
    }

    public void setTextures(List<String> texturePaths) {
        if (texturePaths.size() != numNodes) {
            throw new IllegalArgumentException("The number of textures does not match the number of nodes");
        }
        this.texturePaths = texturePaths;
    }

    public void setNames(String... names) {
        if (names.length != numNodes) {
            throw new IllegalArgumentException("The number of names does not match the number of nodes");
        }
        this.names = names;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        deleteMappings();
        playHideAnim();
        nodes.clear();
    }

    private void deleteMappings() {
        String[] mappings = new String[]{
            "Left",
            "Right",
            "Up",
            "Down",};
        for (int i = 0; i < mappings.length; i++) {
            inputManager.deleteMapping(mappings[i]);
        }
    }

    public boolean isRotatingWheel() {
        return rotatingWheel;
    }

    public void setRotatingWheel(boolean rotatingWheel) {
        this.rotatingWheel = rotatingWheel;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public float getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(float nodeSize) {
        this.nodeSize = nodeSize;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getDefaultCamHeight() {
        return defaultCamHeight;
    }

    public void setDefaultCamHeight(float defaultCamHeight) {
        this.defaultCamHeight = defaultCamHeight;
    }

    public float getAnimSpeed() {
        return animSpeed;
    }

    public void setAnimSpeed(float animSpeed) {
        this.animSpeed = animSpeed;
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        value *= 1000;
        switch (name) {
            case "Left":
                mousePosition.addLocal(-value, 0, 0);
                break;
            case "Right":
                mousePosition.addLocal(value, 0, 0);
                break;
            case "Up":
                mousePosition.addLocal(0, value, 0);
                break;
            case "Down":
                mousePosition.addLocal(0, -value, 0);
                break;
        }
    }

    @Override
    public void scaleFinished(Spatial spatial) {
        guiNode.detachChild(spatial);
    }

    @Override
    public void fadeFinished(Spatial s) {
        guiNode.detachChild(s);
        guiNode.removeLight(ambientLight);
    }
}
