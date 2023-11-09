package com.monstrous.tut3d;

import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.physics.CollisionShapeType;

public class Populator {

    public static void populate(World world) {
        world.clear();

        world.spawnObject(true, "brickcube", null, CollisionShapeType.BOX, false, Vector3.Zero, 1);
        world.spawnObject(true, "groundbox", null, CollisionShapeType.BOX, false, Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.001", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.002", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.003", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.004", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);
        world.spawnObject(true, "wall", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);
        world.spawnObject(true, "wall.001", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);
        world.spawnObject(true, "wall.002", null, CollisionShapeType.BOX,false, Vector3.Zero, 1f);

        world.spawnObject(true,"arch",  null, CollisionShapeType.MESH, false, Vector3.Zero, 1f);

        world.spawnObject(true,"stairs", "stairsProxy",  CollisionShapeType.MESH, false, Vector3.Zero, 1f);

        world.spawnObject(true,"ramp",  null, CollisionShapeType.MESH, false, Vector3.Zero, 1f);

        world.spawnObject(false, "ball", null, CollisionShapeType.SPHERE, true, new Vector3(0,4,-2), Settings.ballMass);
        world.spawnObject(false, "ball", null, CollisionShapeType.SPHERE, true, new Vector3(-1,5,-2), Settings.ballMass);
        world.spawnObject(false, "ball", null, CollisionShapeType.SPHERE, true, new Vector3(-2,6,-2), Settings.ballMass);

        GameObject go = world.spawnObject(false, "ducky",null, CollisionShapeType.CAPSULE, true, new Vector3(0,1,0), Settings.playerMass);
        world.setPlayer(go);
    }
}
