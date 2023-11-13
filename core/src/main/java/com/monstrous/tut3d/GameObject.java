package com.monstrous.tut3d;

import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.physics.PhysicsBody;
import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject {

    public final GameObjectType type;
    public final Scene scene;
    public final PhysicsBody body;
    public final Vector3 direction;
    public boolean visible;

    public GameObject(GameObjectType type, Scene scene, PhysicsBody body) {
        this.type = type;
        this.scene = scene;
        this.body = body;
        body.geom.setData(this);            // the geom has user data to link back to GameObject for collision handling
        visible = true;
        direction = new Vector3();
    }

    public Vector3 getPosition() {
        return body.getPosition();
    }

    public Vector3 getDirection() {
        direction.set(0,0,1);
        direction.mul(body.getOrientation());
        return direction;
    }
}
