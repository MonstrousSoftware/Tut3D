package com.monstrous.tut3d.physics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.github.antzGames.gdx.ode4j.math.DVector3;
import com.github.antzGames.gdx.ode4j.ode.*;
import com.monstrous.tut3d.GameObject;

public class PhysicsRayCaster implements Disposable {

    private final PhysicsWorld physicsWorld;       // reference only
    private final DRay groundRay;
    private final DRay shootRay;
    private GameObject player;

    public PhysicsRayCaster(PhysicsWorld physicsWorld) {
        this.physicsWorld = physicsWorld;
        groundRay = OdeHelper.createRay(1);        // length gets overwritten when ray is used
        shootRay = OdeHelper.createRay(1);        // length gets overwritten when ray is used
    }

    public boolean isGrounded(GameObject player, Vector3 playerPos, float rayLength, Vector3 groundNormal ) {
        this.player = player;
        groundRay.setLength(rayLength);
        groundRay.set(playerPos.x, playerPos.y, playerPos.z, 0, -1, 0); // point ray downwards
        groundRay.setFirstContact(true);
        groundRay.setBackfaceCull(true);

        groundNormal.set(0,0,0);    // set to invalid value
        OdeHelper.spaceCollide2(physicsWorld.space, groundRay, groundNormal, callback);
        return !groundNormal.isZero();
    }

    private final DGeom.DNearCallback callback = new DGeom.DNearCallback() {

        @Override
        public void call(Object data, DGeom o1, DGeom o2) {
            GameObject go;
            final int N = 1;
            DContactBuffer contacts = new DContactBuffer(N);
            int n = OdeHelper.collide (o1,o2,N,contacts.getGeomBuffer());
            if (n > 0) {

                float sign = 1;
                if(o2 instanceof DRay ) {
                    go = (GameObject) o1.getData();
                    sign = -1f;
                }
                else
                    go = (GameObject) o2.getData();
                //Gdx.app.log("ray cast",""+go.scene.modelInstance.nodes.first().id);
                if(go == player)      // ignore collision with player itself
                    return;

                DVector3 normal = contacts.get(0).getContactGeom().normal;
                ((Vector3)data).set((float) (sign*normal.get(0)), (float)(sign*normal.get(1)), (float)(sign*normal.get(2)));
            }
        }
    };



    // class to contain details of hit point
    public static class HitPoint {
        public boolean hit;
        public float distance;
        public GameObject refObject;
        public Vector3 normal;
        public Vector3 worldContactPoint;

        public HitPoint() {
            normal = new Vector3();
            worldContactPoint = new Vector3();
        }
    }


    // use ray casting to see if cross-hair is over a target game object
    //
    public boolean findTarget(Vector3 playerPos, Vector3 viewDir, HitPoint hitPoint) {
        shootRay.setLength(100);    // shooting distance
        shootRay.set(playerPos.x, playerPos.y, playerPos.z, viewDir.x, viewDir.y, viewDir.z); // point ray in viewing direction, starting at player's centre

        // the following settings are only relevant to triMesh collisions which can be expensive
        // they do NOT mean only the first or closest geom is returned,

        shootRay.setFirstContact(true);     // use first contact when colliding with a triMesh
        shootRay.setBackfaceCull(true);     // ignore back faces when colliding with a triMesh
        shootRay.setClosestHit(false);      // when colliding with triMesh, dont search for closest hit, just use the first one

        hitPoint.hit = false;   // reset hit point
        hitPoint.distance = Float.MAX_VALUE;
        OdeHelper.spaceCollide2(physicsWorld.space, shootRay, hitPoint, shootCallback);
        return hitPoint.hit;
    }

    private final DGeom.DNearCallback shootCallback = new DGeom.DNearCallback() {

        @Override
        public void call(Object data, DGeom o1, DGeom o2) {
            HitPoint hitPoint = (HitPoint)data;

            final int N = 1;    // the ray will make only one contact with this geom
            DContactBuffer contacts = new DContactBuffer(N);
            if( OdeHelper.collide (o1,o2,N,contacts.getGeomBuffer()) > 0 ) {    // collision?
                // which DGeom is not the ray?
                GameObject go;
                if (o2 instanceof DRay)
                    go = (GameObject) o1.getData();
                else
                    go = (GameObject) o2.getData();
                if(go.type.isPlayer)       // ignore ray hitting player himself
                    return;
                double d = contacts.get(0).getContactGeom().depth;
                // keep the closest contact
                if(d < hitPoint.distance) {
                    hitPoint.hit = true;
                    hitPoint.distance = (float)d;
                    hitPoint.refObject = go;
                    DVector3 normal = contacts.get(0).getContactGeom().normal;
                    hitPoint.normal.set((float) normal.get(0),(float) normal.get(1), (float) normal.get(2));
                    DVector3 pos = contacts.get(0).getContactGeom().pos;
                    hitPoint.worldContactPoint.set((float) pos.get(0), (float) pos.get(1), (float) pos.get(2));
                }
            }
        }
    };

    @Override
    public void dispose() {
        groundRay.destroy();
        shootRay.destroy();
    }
}
