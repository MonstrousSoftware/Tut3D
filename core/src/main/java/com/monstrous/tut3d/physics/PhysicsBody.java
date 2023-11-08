package com.monstrous.tut3d.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.antzGames.gdx.ode4j.math.DQuaternion;
import com.github.antzGames.gdx.ode4j.math.DQuaternionC;
import com.github.antzGames.gdx.ode4j.math.DVector3C;
import com.github.antzGames.gdx.ode4j.ode.DBody;
import com.github.antzGames.gdx.ode4j.ode.DGeom;
import com.monstrous.tut3d.Settings;

public class PhysicsBody {
    // colours to use for active vs. sleeping geoms
    static private final Color COLOR_ACTIVE = Color.GREEN;
    static private final Color COLOR_SLEEPING = Color.TEAL;
    static private final Color COLOR_STATIC = Color.GRAY;

    public final DGeom geom;
    private final Vector3 position;               // for convenience, matches geom.getPosition() but converted to Vector3
    private final Quaternion quaternion;          // for convenience, matches geom.getQuaternion() but converted to LibGDX Quaternion
    private final ModelInstance debugInstance;    // visualisation of collision shape for debug view

    public PhysicsBody(DGeom geom, ModelInstance debugInstance) {
        this.geom = geom;
        this.debugInstance = debugInstance;
        position = new Vector3();
        quaternion = new Quaternion();
    }

    public Vector3 getPosition() {
        DVector3C pos = geom.getPosition();
        position.x = (float) pos.get0();
        position.y = (float) pos.get2();        // note: swap Y and Z
        position.z = (float) pos.get1();
        return position;
    }

    public void setPosition( Vector3 pos ) {
        geom.setPosition(pos.x, pos.z, pos.y);  // swap Y and Z
        DBody rigidBody = geom.getBody();
        if(rigidBody != null)
            rigidBody.setPosition(pos.x, pos.z, pos.y);  // swap Y and Z
    }

    public Quaternion getOrientation() {
        DQuaternionC odeQ = geom.getQuaternion();
        // Convert from ODE to LibGDX
        quaternion.set(-(float)odeQ.get1(), (float)odeQ.get3(), -(float)odeQ.get2(), (float) odeQ.get0());   // x,y,z,w
        return quaternion;
    }

    public void setOrientation( Quaternion q ){
        DQuaternion odeQ = new DQuaternion(q.w, -q.x, -q.z, q.y);       // convert to ODE quaternion
        geom.setQuaternion(odeQ);
        DBody rigidBody = geom.getBody();
        if(rigidBody != null)
            rigidBody.setQuaternion(odeQ);
    }

    public void applyForce( Vector3 force ){
        DBody rigidBody = geom.getBody();
        rigidBody.addForce(force.x, force.z, force.y);  // swap z & y
    }

    public void render(ModelBatch batch) {
        // move & orient debug modelInstance in line with geom
        debugInstance.transform.set(getPosition(), getOrientation());

        // use different colour for static/sleeping/active objects and for active ones
        Color color = COLOR_STATIC;
        if (geom.getBody() != null) {
            if (geom.getBody().isEnabled())
                color = COLOR_ACTIVE;
            else
                color = COLOR_SLEEPING;
        }
        debugInstance.materials.first().set(ColorAttribute.createDiffuse(color));   // set material colour

        batch.render(debugInstance);
    }

    public void setPlayerCharacteristics() {
        DBody rigidBody = geom.getBody();
        rigidBody.setDamping(Settings.playerLinearDamping, Settings.playerAngularDamping);
        rigidBody.setAutoDisableFlag(false);       // never allow player to get disabled
        // keep capsule upright by not allowing rotations
        rigidBody.setMaxAngularSpeed(0);
    }
}
