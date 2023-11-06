package com.monstrous.tut3d.physics;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.antzGames.gdx.ode4j.math.DQuaternionC;
import com.github.antzGames.gdx.ode4j.math.DVector3C;
import com.github.antzGames.gdx.ode4j.ode.DGeom;

public class PhysicsBody {

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
        position.y = (float) pos.get1();
        position.z = (float) pos.get2();
        return position;
    }

    public Quaternion getOrientation() {
        DQuaternionC odeQ = geom.getQuaternion();
        quaternion.set((float)odeQ.get1(), (float)odeQ.get2(), (float)odeQ.get3(), (float) odeQ.get0());
        return quaternion;
    }

    public void render(ModelBatch batch) {
        // move & orient debug modelInstance in line with geom
        debugInstance.transform.set(getPosition(), getOrientation());
        batch.render(debugInstance);
    }
}
