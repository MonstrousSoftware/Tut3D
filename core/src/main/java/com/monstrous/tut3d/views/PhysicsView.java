package com.monstrous.tut3d.views;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.World;

public class PhysicsView implements Disposable {

    private final ModelBatch modelBatch;
    private final World world;      // reference

    public PhysicsView(World world) {
        this.world = world;
        modelBatch = new ModelBatch();
    }

    public void render( Camera cam ) {
        modelBatch.begin(cam);
        int num = world.getNumGameObjects();
        for(int i = 0; i < num; i++)
            world.getGameObject(i).body.render(modelBatch);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }
}
