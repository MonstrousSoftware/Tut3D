package com.monstrous.tut3d.nav;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NavActor {
    public static float CLOSE = 1f;

    private NavMesh navMesh;
    public Array<NavNode> navNodePath;
    public Array<Vector3> path;
    private int wayPointIndex;
    private Vector3 wayPoint;

    public NavActor(NavMesh navMesh) {
        this.navMesh = navMesh;

        path = new Array<>();
        navNodePath = new Array<>();
    }

    // get next point to aim for
    public Vector3 getWayPoint( Vector3 actorPosition, Vector3 targetPosition ) {
        boolean rebuilt = navMesh.makePath(actorPosition, targetPosition, navNodePath, path);
        if(rebuilt) {
            wayPointIndex = 1;  // path[0] is currentPosition
        }
        wayPoint = path.get(wayPointIndex);
        if (wayPointIndex < path.size - 1 && wayPoint.dst(actorPosition) < CLOSE) {     // reached a waypoint, move to next one
            wayPointIndex++;
            wayPoint = path.get(wayPointIndex);
        }
        return wayPoint;
    }

    // get a slope value up to next way point: > 0 we have to climb, == 0 horizontal surface
    // assumes you called getWayPoint() before
    public float getSlope() {
        return wayPoint.y - path.get(wayPointIndex-1).y;
    }
}
