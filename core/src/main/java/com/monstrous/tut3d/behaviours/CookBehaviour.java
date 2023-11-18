package com.monstrous.tut3d.behaviours;

import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.GameObject;
import com.monstrous.tut3d.GameObjectType;
import com.monstrous.tut3d.Settings;
import com.monstrous.tut3d.World;
import com.monstrous.tut3d.physics.CollisionShapeType;

public class CookBehaviour extends Behaviour {

    private static final float SHOOT_INTERVAL = 2f;     // seconds between shots

    private float shootTimer;
    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootDirection = new Vector3();
    private final Vector3 direction = new Vector3();
    private final Vector3 targetDirection = new Vector3();
    private final Vector3 angularVelocity = new Vector3();

    public CookBehaviour(GameObject go) {
        super(go);
        shootTimer = SHOOT_INTERVAL;
        go.body.setCapsuleCharacteristics();
    }

    public Vector3 getDirection() {
        return direction;
    }

    @Override
    public void update(World world, float deltaTime ) {
        if(go.health <= 0)   // don't do anything when dead
            return;

        // move towards player
        targetDirection.set(world.getPlayer().getPosition()).sub(go.getPosition());  // vector towards player
        targetDirection.y = 0;  // consider only vector in horizontal plane
        float distance = targetDirection.len();
        targetDirection.nor();      // make unit vector
        direction.set(targetDirection);
        if(distance > 5f)   // move unless quite close
            go.body.applyForce(targetDirection.scl(1.5f));


        // rotate to follow player
        angularVelocity.set(0,0,0);
        targetDirection.nor();      // make unit vector
        Vector3 facing = go.getDirection();                                             // vector we're facing now
        float dot = targetDirection.dot(facing);                                        // dot product = cos of angle between the vectors
        float cross = Math.signum(targetDirection.crs(facing).y);                       // cross product to give direction to turn
        if(dot < 0.99f)                         // if not facing player
            angularVelocity.y = -cross;         // turn towards player
        go.body.applyTorque(angularVelocity);

        // every so often shoot a pan
        shootTimer -= deltaTime;
        if(shootTimer <= 0 && distance < 20f && world.getPlayer().health > 0) {
            shootTimer = SHOOT_INTERVAL;
            shootPan(world);
        }
    }

    private void shootPan(World world) {
        spawnPos.set(direction);
        spawnPos.nor().scl(1f);
        spawnPos.add(go.getPosition()); // spawn from 1 unit in front of the character
        spawnPos.y += 1f;
        GameObject pan = world.spawnObject(GameObjectType.TYPE_ENEMY_BULLET, "pan", "panProxy", CollisionShapeType.MESH, true, spawnPos );
        shootDirection.set(direction);        // shoot forward
        shootDirection.y += 0.5f;       // and slightly up
        shootDirection.scl(Settings.panForce);   // scale for speed
        pan.body.geom.getBody().setDamping(0.0f, 0.0f);
        pan.body.applyForce(shootDirection);
        pan.body.applyTorque(Vector3.Y);    // add some spin
    }
}
