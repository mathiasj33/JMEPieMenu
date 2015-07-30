/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author Mathias
 */
public class ScaleControl extends AbstractControl {

    private boolean up;
    private float speed;
    private float time = 0;
    private ScaleFinishedListener listener;

    public ScaleControl(boolean up, float speed) {
        this.up = up;
        this.speed = speed;
    }

    public void setScaleFinishedListener(ScaleFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void controlUpdate(float tpf) {
        time += speed * tpf;
        if (time > 1) {
            if (up) {
                time = 1;
            } else {
                time = 0;
            }
            spatial.setLocalScale(time);
            if (listener != null) {
                listener.scaleFinished(spatial);
            }
            spatial.removeControl(this);
            return;
        }
        if (up) {
            spatial.setLocalScale(time);
        } else {
            spatial.setLocalScale(1 - time);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        ScaleControl control = new ScaleControl(up, speed);
        control.listener = listener;
        control.time = time;
        return control;
    }
}
