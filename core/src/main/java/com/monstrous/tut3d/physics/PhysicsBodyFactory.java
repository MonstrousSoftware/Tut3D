package com.monstrous.tut3d.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CapsuleShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.antzGames.gdx.ode4j.math.DQuaternion;
import com.github.antzGames.gdx.ode4j.math.DQuaternionC;
import com.github.antzGames.gdx.ode4j.math.DVector3;
import com.github.antzGames.gdx.ode4j.ode.*;


public class PhysicsBodyFactory implements Disposable {

    public static final long CATEGORY_STATIC  = 1;      // collision flags
    public static final long CATEGORY_DYNAMIC  = 2;     // collision flags

    private final PhysicsWorld physicsWorld;
    private final DMass massInfo;
    private final Vector3 position;
    private final Quaternion q;
    private final ModelBuilder modelBuilder;
    private final Material material;
    private final Array<Disposable> disposables;


    public PhysicsBodyFactory(PhysicsWorld physicsWorld) {
        this.physicsWorld = physicsWorld;
        massInfo = OdeHelper.createMass();
        position = new Vector3();
        q = new Quaternion();
        modelBuilder = new ModelBuilder();
        material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        disposables = new Array<>();
    }

    public PhysicsBody createBody( ModelInstance collisionInstance, CollisionShapeType shapeType, boolean isStatic ) {
        BoundingBox bbox = new BoundingBox();
        Node node = collisionInstance.nodes.first();
        node.calculateBoundingBox(bbox, false); // bounding box without the transform
        float w = bbox.getWidth();
        float h = bbox.getHeight();
        float d = bbox.getDepth();

        DGeom geom;
        ModelInstance instance;
        float diameter = 0;
        float radius = 0;
        float len;

        switch(shapeType) {
            case BOX:
                geom = OdeHelper.createBox(physicsWorld.space, w, h, d);
                massInfo.setBox(1, w, h, d);
                break;
            case SPHERE:
                diameter = Math.max(Math.max(w, d), h);
                radius = diameter/2f;
                geom = OdeHelper.createSphere(physicsWorld.space, radius);
                massInfo.setSphere(1, radius);
                break;
            case CAPSULE:
                diameter = Math.max(w, d);
                radius = diameter/2f; // radius of the cap
                len = h - 2*radius;     // height of the cylinder between the two end caps
                geom = OdeHelper.createCapsule(physicsWorld.space, radius, len);
                massInfo.setCapsule(1, 2, radius, len);
                break;
            case CYLINDER:
                diameter = Math.max(w, d);
                radius = diameter/2f; // radius of the cap
                len = h;     // height of the cylinder between the two end caps
                geom = OdeHelper.createCylinder(physicsWorld.space, radius, len);
                massInfo.setCylinder(1, 2, radius, len);
                break;
            case MESH:
                // create a TriMesh from the provided modelInstance
                DTriMeshData triData = OdeHelper.createTriMeshData();
                fillTriData(triData, collisionInstance);
                geom = OdeHelper.createTriMesh(physicsWorld.space, triData, null, null, null);
                massInfo.setBox(1, w, h, d);
                break;

            default:
                throw new RuntimeException("Unknown shape type");
        }

        if(isStatic) {
            geom.setCategoryBits(CATEGORY_STATIC);   // which category is this object?
            geom.setCollideBits(0);                  // which categories will it collide with?
            // note: geom for static object has no rigid body attached
        } else {
            DBody rigidBody = OdeHelper.createBody(physicsWorld.world);
            rigidBody.setMass(massInfo);
            rigidBody.enable();
            rigidBody.setAutoDisableDefaults();
            rigidBody.setGravityMode(true);
            rigidBody.setDamping(0.01, 0.1);

            geom.setBody(rigidBody);
            geom.setCategoryBits(CATEGORY_DYNAMIC);
            geom.setCollideBits(CATEGORY_DYNAMIC|CATEGORY_STATIC);

            if(shapeType == CollisionShapeType.CYLINDER || shapeType == CollisionShapeType.CAPSULE) {
                // rotate geom 90 degrees around X because ODE geom cylinders and capsules shapes are created using Z as long axis
                // and we want the shape to be oriented along the Y axis which is up.
                DQuaternion Q = DQuaternion.fromEulerDegrees(90, 0, 0);     // rotate 90 degrees around X
                geom.setOffsetQuaternion(Q);    // set standard rotation from rigid body to geom
            }
        }


        // create a debug model matching the collision geom shape
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;
        meshBuilder = modelBuilder.part("part", GL20.GL_LINES, VertexAttributes.Usage.Position , material);
        switch(shapeType) {
            case BOX:
                BoxShapeBuilder.build(meshBuilder, w, h, d);
                break;
            case SPHERE:
                SphereShapeBuilder.build(meshBuilder, diameter, diameter, diameter , 8, 8);
                break;
            case CAPSULE:
                CapsuleShapeBuilder.build(meshBuilder, radius, h, 12);
                break;
            case CYLINDER:
                CylinderShapeBuilder.build(meshBuilder, diameter, h, diameter, 12);
                break;
            case MESH:
                buildLineMesh(meshBuilder, collisionInstance);
                break;
        }
        Model modelShape = modelBuilder.end();
        disposables.add(modelShape);
        instance = new ModelInstance(modelShape, Vector3.Zero);

        PhysicsBody body = new PhysicsBody(geom, instance);

        // copy position and orientation from modelInstance to body
        collisionInstance.transform.getTranslation(position);
        collisionInstance.transform.getRotation(q);
        body.setPosition(position);
        body.setOrientation(q);
        return body;
    }

    // create a wire frame mesh of the collision model instance
    private void buildLineMesh(MeshPartBuilder meshBuilder, ModelInstance instance) {
        Mesh mesh = instance.nodes.first().parts.first().meshPart.mesh;

        int numVertices = mesh.getNumVertices();
        int numIndices = mesh.getNumIndices();
        int stride = mesh.getVertexSize()/4;        // floats per vertex in mesh, e.g. for position, normal, textureCoordinate, etc.

        float[] origVertices = new float[numVertices*stride];
        short[] origIndices = new short[numIndices];
        // find offset of position floats per vertex, they are not necessarily the first 3 floats
        int posOffset = mesh.getVertexAttributes().findByUsage(VertexAttributes.Usage.Position).offset / 4;

        mesh.getVertices(origVertices);
        mesh.getIndices(origIndices);

        meshBuilder.ensureVertices(numVertices);
        for(int v = 0; v < numVertices; v++) {
            float x = origVertices[stride*v+posOffset];
            float y = origVertices[stride*v+1+posOffset];
            float z = origVertices[stride*v+2+posOffset];
            meshBuilder.vertex(x, y, z);
        }
        meshBuilder.ensureTriangleIndices(numIndices/3);
        for(int i = 0; i < numIndices; i+=3) {
            meshBuilder.triangle(origIndices[i], origIndices[i+1], origIndices[i+2]);
        }
    }


    // convert a libGDX mesh to ODE TriMeshData
    private void fillTriData(DTriMeshData triData, ModelInstance instance ) {
        Mesh mesh = instance.nodes.first().parts.first().meshPart.mesh;

        int numVertices = mesh.getNumVertices();
        int numIndices = mesh.getNumIndices();
        int stride = mesh.getVertexSize()/4;        // floats per vertex in mesh, e.g. for position, normal, textureCoordinate, etc.

        float[] origVertices = new float[numVertices*stride];
        short[] origIndices = new short[numIndices];
        // find offset of position floats per vertex, they are not necessarily the first 3 floats
        int posOffset = mesh.getVertexAttributes().findByUsage(VertexAttributes.Usage.Position).offset / 4;

        mesh.getVertices(origVertices);
        mesh.getIndices(origIndices);

        // data for the trimesh
        float[] vertices = new float[3*numVertices];
        int[] indices = new int[numIndices];

        for(int v = 0; v < numVertices; v++) {
            // x, y, z
            vertices[3*v] = origVertices[stride*v+posOffset];
            vertices[3*v+1] = origVertices[stride*v+1+posOffset];
            vertices[3*v+2] = origVertices[stride*v+2+posOffset];
        }
        for(int i = 0; i < numIndices; i++)         // convert shorts to ints
            indices[i] = origIndices[i];

        triData.build(vertices, indices);
        triData.preprocess();
    }


    @Override
    public void dispose() {
        for(Disposable d : disposables)
            d.dispose();
    }
}
