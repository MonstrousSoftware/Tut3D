package com.monstrous.tut3d.views;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

// 3d grid view overlay for debug purposes
public class GridView implements Disposable {
    private final ModelBatch modelBatch;
    private final Array<ModelInstance> instances;
    private final Model arrowModel;
    private final Model gridModel;

    public GridView() {
        instances = new Array<>();
        modelBatch = new ModelBatch();
        ModelBuilder modelBuilder = new ModelBuilder();

        arrowModel = modelBuilder.createXYZCoordinates(5f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(arrowModel, Vector3.Zero));

        gridModel = modelBuilder.createLineGrid(100, 100, 1f, 1f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(gridModel, Vector3.Zero));
    }

    public void render( Camera cam ) {
        modelBatch.begin(cam);
        modelBatch.render(instances);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        arrowModel.dispose();
        gridModel.dispose();
    }
}
