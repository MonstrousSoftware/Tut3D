package com.monstrous.tut3d;

import com.badlogic.gdx.math.Vector3;

public class Populator {

    public static void populate(World world) {
        world.clear();
        world.spawnObject("groundbox", Vector3.Zero);
        world.spawnObject("brickcube", Vector3.Zero);
        world.spawnObject("brickcube.001", Vector3.Zero);
        world.spawnObject("brickcube.002", Vector3.Zero);
        world.spawnObject("brickcube.003", Vector3.Zero);
        world.spawnObject("wall", Vector3.Zero);
        world.spawnObject("ball", Vector3.Zero);
        world.spawnObject("ball", new Vector3(0,1,0));
        world.spawnObject("ball", new Vector3(0,2,0));
        world.player = world.spawnObject("ducky", Vector3.Zero);
    }
}
