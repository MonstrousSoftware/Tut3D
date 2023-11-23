package com.monstrous.tut3d.views;

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
import com.monstrous.tut3d.World;
import com.monstrous.tut3d.nav.NavMesh;
import com.monstrous.tut3d.nav.NavNode;
import com.monstrous.tut3d.physics.PhysicsBody;

public class NavMeshView implements Disposable {



    private final ModelBatch modelBatch;
    private final World world;      // reference
    private ModelBuilder modelBuilder;
    private ModelInstance instance;
    private ModelInstance pathInstance;
    private Model model;
    private Model pathModel;


    public NavMeshView(World world) {
        this.world = world;
        modelBatch = new ModelBatch();
    }

    public void render( Camera cam ) {
        modelBatch.begin(cam);
        if(instance != null)
            modelBatch.render(instance);
        if(pathInstance != null)
            modelBatch.render(pathInstance);
        modelBatch.end();
    }



    public void buildShape( Array<NavNode> path ) {
        if (path == null || path.size == 0) {
            instance = null;
            return;
        }

        //Material material = new Material(ColorAttribute.createDiffuse(Color.ORANGE));
        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;
        //meshBuilder = modelBuilder.part("part", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position, material);

        for(NavNode navNode : path ) {

            Material material = new Material(ColorAttribute.createDiffuse((float)(16-navNode.steps)/16f, 0, .5f, 1));
            meshBuilder = modelBuilder.part("part", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position, material);


            meshBuilder.ensureVertices(3);
            short v0 = meshBuilder.vertex(navNode.p0.x, navNode.p0.y, navNode.p0.z);
            short v1 = meshBuilder.vertex(navNode.p1.x, navNode.p1.y, navNode.p1.z);
            short v2 = meshBuilder.vertex(navNode.p2.x, navNode.p2.y, navNode.p2.z);
            meshBuilder.ensureTriangleIndices(1);
            meshBuilder.triangle(v0, v1, v2);
        }

        if(model != null)
            model.dispose();
        model = modelBuilder.end();
        instance = new ModelInstance(model, Vector3.Zero);
    }

    public void buildPath( Array<Vector3> path, Array<NavMesh.Portal> portals ) {
        if (path == null || path.size == 0) {
            pathInstance = null;
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

        material = new Material(ColorAttribute.createDiffuse(Color.GRAY));
        meshBuilder = modelBuilder.part("line", GL20.GL_LINES, VertexAttributes.Usage.Position, material);
        for(NavMesh.Portal portal : portals ) {

            Vector3 v0 = portal.left;
            Vector3 v1 = portal.right;
            short i0 = meshBuilder.vertex(v0.x, v0.y+0.1f, v0.z);      // raise a bit above ground
            short i1 = meshBuilder.vertex(v1.x, v1.y+0.1f, v1.z);
            meshBuilder.line(i0, i1);
        }

        if(pathModel != null)
            pathModel.dispose();
        pathModel = modelBuilder.end();
        pathInstance = new ModelInstance(pathModel, Vector3.Zero);
    }


    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
    }
}
