package com.monstrous.tut3d;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.behaviours.Behaviour;
import com.monstrous.tut3d.physics.PhysicsBody;
import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject implements Disposable {

    public final GameObjectType type;
    public final Scene scene;
    public final PhysicsBody body;
    public final Vector3 direction;
    public boolean visible;
    public float health;
    public Behaviour behaviour;

    public GameObject(GameObjectType type, Scene scene, PhysicsBody body) {
        this.type = type;
        this.scene = scene;
        this.body = body;
        body.geom.setData(this);            // the geom has user data to link back to GameObject for collision handling
        visible = true;
        direction = new Vector3();
        health = 1f;
        behaviour = Behaviour.createBehaviour(this);
    }

    public void update(World world, float deltaTime ){
        if(behaviour != null)
            behaviour.update(world, deltaTime);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public Vector3 getPosition() {
        return body.getPosition();
    }

    public Vector3 getDirection() {
        direction.set(0,0,1);
        direction.mul(body.getOrientation());
        return direction;
    }

    @Override
    public void dispose() {
        body.destroy();
    }
}
