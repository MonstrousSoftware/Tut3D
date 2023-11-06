package com.monstrous.tut3d;

import com.monstrous.tut3d.physics.PhysicsBody;
import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject {

    public final Scene scene;
    public final PhysicsBody body;

    public GameObject(Scene scene, PhysicsBody body) {
        this.scene = scene;
        this.body = body;
        body.geom.setData(this);            // the geom has user data to link back to GameObject for collision handling
    }
}
