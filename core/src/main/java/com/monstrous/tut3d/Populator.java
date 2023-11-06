package com.monstrous.tut3d;

import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.physics.CollisionShapeType;

public class Populator {

    public static void populate(World world) {
        world.clear();

        world.spawnObject(true, "brickcube", CollisionShapeType.BOX, Vector3.Zero, 1);
        world.spawnObject(true, "groundbox", CollisionShapeType.BOX, Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.001", CollisionShapeType.BOX,Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.002", CollisionShapeType.BOX,Vector3.Zero, 1f);
        world.spawnObject(true, "brickcube.003", CollisionShapeType.BOX,Vector3.Zero, 1f);
        world.spawnObject(true, "wall", CollisionShapeType.BOX,Vector3.Zero, 1f);
        world.spawnObject(false, "ball", CollisionShapeType.SPHERE, new Vector3(0,4,0), 1f);
        world.spawnObject(false, "ball", CollisionShapeType.SPHERE,new Vector3(-1,5,0), 1f);
        world.spawnObject(false, "ball", CollisionShapeType.SPHERE, new Vector3(-2,6,0), 1f);
        world.player = world.spawnObject(false, "ducky",CollisionShapeType.SPHERE, Vector3.Zero, 1f);
    }
}
