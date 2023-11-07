package com.monstrous.tut3d.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.github.antzGames.gdx.ode4j.ode.DBody;
import com.github.antzGames.gdx.ode4j.ode.DGeom;
import com.github.antzGames.gdx.ode4j.ode.DMass;
import com.github.antzGames.gdx.ode4j.ode.OdeHelper;


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

    public PhysicsBody createBody( ModelInstance collisionInstance, CollisionShapeType shapeType, float mass, boolean isStatic) {
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
                geom = OdeHelper.createBox(physicsWorld.space, w, d, h);    // swap d & h
                break;
            case SPHERE:
                diameter = Math.max(Math.max(w, d), h);
                radius = diameter/2f;
                geom = OdeHelper.createSphere(physicsWorld.space, radius);
                break;
            case CAPSULE:
                diameter = Math.max(w, d);
                radius = diameter/2f; // radius of the cap
                len = h - 2*radius;     // height of the cylinder between the two end caps
                geom = OdeHelper.createCapsule(physicsWorld.space, radius, len);
                break;
            case CYLINDER:
                diameter = Math.max(w, d);
                radius = diameter/2f; // radius of the cap
                len = h;     // height of the cylinder between the two end caps
                geom = OdeHelper.createCylinder(physicsWorld.space, radius, len);
                break;
            default:
                throw new RuntimeException("Unknown shape type");
        }

        if(isStatic) {
            geom.setCategoryBits(CATEGORY_STATIC);   // which category is this object?
            geom.setCollideBits(0);                  // which categories will it collide with?
            // note: geom for static object has no rigid body attached
        }
        else {
            DBody rigidBody = OdeHelper.createBody(physicsWorld.world);
            massInfo.setBox(1, w, d, h);    // swap d & h
            massInfo.setMass(mass);
            rigidBody.setMass(massInfo);
            rigidBody.enable();
            rigidBody.setAutoDisableDefaults();
            rigidBody.setGravityMode(true);
            rigidBody.setDamping(0.01, 0.1);

            geom.setBody(rigidBody);
            geom.setCategoryBits(CATEGORY_DYNAMIC);
            geom.setCollideBits(CATEGORY_DYNAMIC|CATEGORY_STATIC);
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

    @Override
    public void dispose() {
        for(Disposable d : disposables)
            d.dispose();
    }
}
