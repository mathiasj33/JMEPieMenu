/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author Mathias
 */
public class FadeControl extends AbstractControl {

    private FadeFinishedListener listener;
    private float speed;
    private boolean up;
    private Material material;
    private float time;
    
    public FadeControl(boolean up, float speed) {
        this.up = up;
        this.speed = speed;
    }
    
    public void setFadeFinishedListener(FadeFinishedListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void setSpatial(Spatial s) {
        super.setSpatial(s);
        if(s instanceof Node) {
            throw new IllegalArgumentException("The spatial is not a Geometry");
        }
        if(s != null) {
            material = ((Geometry) s).getMaterial();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        time += tpf * speed;
        float alpha = 1;
        if(up) {
           alpha = time;
        } else {
            alpha = 1 - time;
        }
        if(alpha > 1 || alpha < 0) {
            if(listener != null) {
                listener.fadeFinished(spatial);
            }
            spatial.removeControl(this);
            return;
        }
        material.setColor("Color", new ColorRGBA(1,1,1,alpha));
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        FadeControl control = new FadeControl(up, speed);
        control.listener = listener;
        control.time = time;
        return control;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
