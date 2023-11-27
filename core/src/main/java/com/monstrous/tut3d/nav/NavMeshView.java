package com.monstrous.tut3d.nav;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.GameObject;
import com.monstrous.tut3d.GameObjectType;
import com.monstrous.tut3d.World;
import com.monstrous.tut3d.behaviours.CookBehaviour;

public class NavMeshView implements Disposable {

    private final ModelBatch modelBatch;
    private ModelBuilder modelBuilder;
    private Array<ModelInstance> instances;
    private Array<Model> models;


    public NavMeshView() {
        modelBatch = new ModelBatch();
        instances = new Array<>();
        models = new Array<>();
    }

    public void render( Camera cam ) {
        modelBatch.begin(cam);
        modelBatch.render(instances);
        modelBatch.end();
    }

    public void update( World world ) {
        for(Model model : models)
            model.dispose();
        models.clear();
        instances.clear();

        buildNavNodes(world.navMesh.navNodes);

        //buildPortals(NavStringPuller.portals);

        int numObjects = world.getNumGameObjects();
        for(int i = 0; i < numObjects; i++) {
            GameObject go = world.getGameObject(i);
            if(go.type != GameObjectType.TYPE_ENEMY)
                continue;
            NavActor actor = ((CookBehaviour)go.behaviour).navActor;
            buildNavNodePath(actor.navNodePath);
            buildPath(actor.path);
        }
    }

    public void buildNavNodes(Array<NavNode> path ) {
        if (path == null || path.size == 0) {
            return;
        }

        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;

        for(NavNode navNode : path ) {

            Material material = new Material(ColorAttribute.createDiffuse(Color.GRAY));
            meshBuilder = modelBuilder.part("part", GL20.GL_LINES, VertexAttributes.Usage.Position, material);


            meshBuilder.ensureVertices(3);
            short v0 = meshBuilder.vertex(navNode.p0.x, navNode.p0.y, navNode.p0.z);
            short v1 = meshBuilder.vertex(navNode.p1.x, navNode.p1.y, navNode.p1.z);
            short v2 = meshBuilder.vertex(navNode.p2.x, navNode.p2.y, navNode.p2.z);
            meshBuilder.ensureTriangleIndices(1);
            meshBuilder.triangle(v0, v1, v2);
        }
        Model model = modelBuilder.end();
        ModelInstance instance = new ModelInstance(model, Vector3.Zero);
        models.add(model);
        instances.add(instance);
    }



    public void buildNavNodePath(Array<NavNode> path ) {
        if (path == null || path.size == 0) {
            return;
        }

        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;

        for(NavNode navNode : path ) {

            Material material = new Material(ColorAttribute.createDiffuse((float)(16-navNode.steps)/16f, 0, .5f, 1));   // colour shade depends on distance to target
            meshBuilder = modelBuilder.part("part", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position, material);


            meshBuilder.ensureVertices(3);
            short v0 = meshBuilder.vertex(navNode.p0.x, navNode.p0.y, navNode.p0.z);
            short v1 = meshBuilder.vertex(navNode.p1.x, navNode.p1.y, navNode.p1.z);
            short v2 = meshBuilder.vertex(navNode.p2.x, navNode.p2.y, navNode.p2.z);
            meshBuilder.ensureTriangleIndices(1);
            meshBuilder.triangle(v0, v1, v2);
        }
        Model model = modelBuilder.end();
        ModelInstance instance = new ModelInstance(model, Vector3.Zero);
        models.add(model);
        instances.add(instance);
    }


    public void buildPath( Array<Vector3> path ) {
        if (path == null || path.size == 0) {
            return;
        }

        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;
        Material material = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        meshBuilder = modelBuilder.part("line", GL20.GL_LINES, VertexAttributes.Usage.Position, material);
        meshBuilder.ensureVertices(path.size*2);
        meshBuilder.ensureIndices(path.size*2);
        for(int i = 0; i < path.size-1; i++ ) {
            Vector3 v0 = path.get(i);
            Vector3 v1 = path.get(i+1);
            short i0 = meshBuilder.vertex(v0.x, v0.y+0.2f, v0.z);      // raise a bit above ground
            short i1 = meshBuilder.vertex(v1.x, v1.y+0.2f, v1.z);
            meshBuilder.line(i0, i1);
        }
        Model model = modelBuilder.end();
        ModelInstance instance = new ModelInstance(model, Vector3.Zero);
        models.add(model);
        instances.add(instance);
    }

    public void buildPortals( Array<NavStringPuller.Portal> portals ) {
        if (portals.size == 0) {
            return;
        }

        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;

        Material material = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        meshBuilder = modelBuilder.part("line", GL20.GL_LINES, VertexAttributes.Usage.Position, material);
        for(NavStringPuller.Portal portal : portals ) {

            Vector3 v0 = portal.left;
            Vector3 v1 = portal.right;
            short i0 = meshBuilder.vertex(v0.x, v0.y+0.1f, v0.z);      // raise a bit above ground
            short i1 = meshBuilder.vertex(v1.x, v1.y+0.1f, v1.z);
            meshBuilder.line(i0, i1);
        }
        Model model = modelBuilder.end();
        ModelInstance instance = new ModelInstance(model, Vector3.Zero);
        models.add(model);
        instances.add(instance);
    }


    @Override
    public void dispose() {
        modelBatch.dispose();
        for(Model model : models)
            model.dispose();
    }
}
