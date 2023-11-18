package com.monstrous.tut3d.physics;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.antzGames.gdx.ode4j.math.DQuaternion;
import com.github.antzGames.gdx.ode4j.math.DQuaternionC;
import com.github.antzGames.gdx.ode4j.math.DVector3;
import com.github.antzGames.gdx.ode4j.math.DVector3C;
import com.github.antzGames.gdx.ode4j.ode.DBody;
import com.github.antzGames.gdx.ode4j.ode.DGeom;
import com.monstrous.tut3d.Settings;

public class PhysicsBody {

    public final DGeom geom;
    private final Vector3 position;               // for convenience, matches geom.getPosition() but converted to Vector3
    private final Quaternion quaternion;          // for convenience, matches geom.getQuaternion() but converted to LibGDX Quaternion
    public final ModelInstance debugInstance;    // visualisation of collision shape for debug view
    private final DQuaternion tmpQ;
    private final Vector3 linearVelocity;

    public PhysicsBody(DGeom geom, ModelInstance debugInstance) {
        this.geom = geom;
        this.debugInstance = debugInstance;
        position = new Vector3();
        linearVelocity = new Vector3();
        quaternion = new Quaternion();
        tmpQ = new DQuaternion();
    }

    public Vector3 getPosition() {
        DVector3C pos = geom.getPosition();
        position.x = (float) pos.get0();
        position.y = (float) pos.get1();
        position.z = (float) pos.get2();
        return position;
    }

    public void setPosition( Vector3 pos ) {
        geom.setPosition(pos.x, pos.y, pos.z);
        // if the geom is attached to a rigid body it's position will also be changed
    }

    public Quaternion getOrientation() {
        DQuaternionC odeQ = geom.getQuaternion();
        float ow = (float) odeQ.get0();
        float ox = (float) odeQ.get1();
        float oy = (float) odeQ.get2();
        float oz = (float) odeQ.get3();
        quaternion.set(ox, oy, oz, ow);
        return quaternion;
    }

    // get orientation of rigid body, i.e. without any geom offset rotation
    public Quaternion getBodyOrientation() {
        DQuaternionC odeQ;
        if(geom.getBody() == null)      // if geom does not have a body attached, fall back to geom orientation
            odeQ = geom.getQuaternion();
        else
            odeQ = geom.getBody().getQuaternion();
        float ow = (float) odeQ.get0();
        float ox = (float) odeQ.get1();
        float oy = (float) odeQ.get2();
        float oz = (float) odeQ.get3();
        quaternion.set(ox, oy, oz, ow);
        return quaternion;
    }

    public void setOrientation( Quaternion q ){
        tmpQ.set(q.w, q.x, q.y, q.z);       // convert to ODE quaternion
        geom.setQuaternion(tmpQ);
        // if the geom is attached to a rigid body it's rotation will also be changed
    }

    public void applyForce( Vector3 force ){
        DBody rigidBody = geom.getBody();
        rigidBody.addForce(force.x, force.y, force.z);
    }

    public void applyForceAtPos( Vector3 force, Vector3 pos ){
        DBody rigidBody = geom.getBody();
        rigidBody.addForceAtPos(force.x, force.y, force.z, pos.x, pos.y, pos.z);
    }

    public void applyTorque( Vector3 torque ){
        DBody rigidBody = geom.getBody();
        rigidBody.addTorque(torque.x, torque.y, torque.z);
    }

    public Vector3 getVelocity() {
        if(geom.getBody() == null)
            linearVelocity.set(Vector3.Zero);
        else {
            DVector3C v = geom.getBody().getLinearVel();
            linearVelocity.set((float) v.get0(), (float) v.get1(), (float) v.get2());
        }
        return linearVelocity;
    }

    // used for player and enemy characters that have capsules for collision geometry
    public void setCapsuleCharacteristics() {
        DBody rigidBody = geom.getBody();
        rigidBody.setDamping(Settings.playerLinearDamping, Settings.playerAngularDamping);
        rigidBody.setAutoDisableFlag(false);       // never allow player to get disabled
        rigidBody.setMaxAngularSpeed(0);    // keep capsule upright by not allowing rotations
    }

    public void destroy() {
        if(geom.getBody() != null)
            geom.getBody().destroy();
        geom.destroy();
    }
}
