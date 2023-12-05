package com.monstrous.tut3d.behaviours;

import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.GameObject;
import com.monstrous.tut3d.GameObjectType;
import com.monstrous.tut3d.Settings;
import com.monstrous.tut3d.World;
import com.monstrous.tut3d.nav.NavActor;
import com.monstrous.tut3d.physics.CollisionShapeType;

public class CookBehaviour extends Behaviour {

    private static final float SHOOT_INTERVAL = 2f;     // seconds between shots

    private float shootTimer;
    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootDirection = new Vector3();
    private final Vector3 direction = new Vector3();
    private final Vector3 targetDirection = new Vector3();
    private final Vector3 playerVector = new Vector3();
    public NavActor navActor;


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
        if (go.health <= 0)   // don't do anything when dead
            return;

        playerVector.set(world.getPlayer().getPosition()).sub(go.getPosition());    // vector to player in a straight line
        float distance = playerVector.len();

        if (navActor == null) {   // lazy init because we need world.navMesh
            navActor = new NavActor(world.navMesh);
        }

        Vector3 wayPoint = navActor.getWayPoint(go.getPosition(), world.getPlayer().getPosition());  // next point to aim for on the route to target

        float climbFactor = 1f;
        if (navActor.getSlope() > 0.1f) {    // if we need to climb up, disable the gravity
            go.body.geom.getBody().setGravityMode(false);
            climbFactor = 2f;       // and apply some extra force
        } else
            go.body.geom.getBody().setGravityMode(true);

        // move towards waypoint
        targetDirection.set(wayPoint).sub(go.getPosition());  // vector towards way point
        if (targetDirection.len() > 1f) {    // if we're at the way point, stop turning to avoid nervous jittering
            targetDirection.y = 0;  // consider only vector in horizontal plane
            targetDirection.nor();      // make unit vector
            direction.slerp(targetDirection, 0.02f);            // smooth rotation towards target direction

            if(distance > 5f)   // move unless quite close
                go.body.applyForce(targetDirection.scl(deltaTime * 60f * Settings.cookForce * climbFactor));
        }


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
