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
import com.github.antzGames.gdx.ode4j.math.DQuaternion;
import com.github.antzGames.gdx.ode4j.ode.DBody;
import com.github.antzGames.gdx.ode4j.ode.DGeom;
import com.github.antzGames.gdx.ode4j.ode.DMass;
import com.github.antzGames.gdx.ode4j.ode.OdeHelper;


public class PhysicsBodyFactory implements Disposable {

    public static final long CATEGORY_STATIC  = 1;
    public static final long CATEGORY_DYNAMIC  = 2;

    private final PhysicsWorld physicsWorld;
    private final DMass massInfo;
    private final Quaternion q;
    private final ModelBuilder modelBuilder;
    private final Material material;
    private final Array<Disposable> disposables;

    public PhysicsBodyFactory(PhysicsWorld physicsWorld) {
        this.physicsWorld = physicsWorld;
        massInfo = OdeHelper.createMass();
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

        Vector3 pos = new Vector3();
        collisionInstance.transform.getTranslation(pos);
        DGeom geom;
        ModelInstance instance;
        float radius = 0;
        float len;

        switch(shapeType) {
            case BOX:
                geom = OdeHelper.createBox(physicsWorld.space, w, h, d);    // create a geom
                break;
            case SPHERE:
                radius = Math.max(w, d);
                radius = Math.max(h, radius);
                radius /= 2f;
                geom = OdeHelper.createSphere(physicsWorld.space, radius);
                break;
            case CAPSULE:
                radius = 0.5f*Math.max(w, d); // radius of the cap
                len = h - 2*radius;     // height of the cylinder between the two end caps
                geom = OdeHelper.createCapsule(physicsWorld.space, radius, len);
                break;
            case CYLINDER:
                radius = 0.5f*Math.max(w, d); // radius of the cap
                len = h;     // height of the cylinder between the two end caps
                geom = OdeHelper.createCylinder(physicsWorld.space, radius, len);
                break;
            default:
                throw new RuntimeException("Unknown shape type");
        }


        // copy position and orientation from modelInstance to geom
        geom.setPosition(pos.x, pos.y, pos.z);
        collisionInstance.transform.getRotation(q);
        DQuaternion quaternion = new DQuaternion(q.w, q.x, q.y, q.z);       // convert to ODE quaternion
        geom.setQuaternion(quaternion);


        if(isStatic) {
            geom.setCategoryBits(CATEGORY_STATIC);   // which category is this object?
            geom.setCollideBits(0);                  // which categories will it collide with?
            // note: geom for static object has no rigid body attached
        }
        else {

            DBody rigidBody = OdeHelper.createBody(physicsWorld.world);
            rigidBody.setPosition(pos.x, pos.y, pos.z);
            massInfo.setBox(1,w, h, d);
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
                float diam = Math.max(w, d);
                diam = Math.max(h, diam);
                SphereShapeBuilder.build(meshBuilder, diam, diam, diam , 8, 8);
                break;
            case CAPSULE:
                CapsuleShapeBuilder.build(meshBuilder, radius, h, 12);
                break;
            case CYLINDER:
                diam = Math.max(w, d);
                CylinderShapeBuilder.build(meshBuilder, diam, h, diam, 12);
                break;
        }
        Model modelShape = modelBuilder.end();
        disposables.add(modelShape);
        instance = new ModelInstance(modelShape, pos);

        instance.transform.set(new Quaternion().setFromAxis(Vector3.Y, 90));

        return new PhysicsBody(geom, instance);
    }

    @Override
    public void dispose() {
        for(Disposable d : disposables)
            d.dispose();
    }
}
