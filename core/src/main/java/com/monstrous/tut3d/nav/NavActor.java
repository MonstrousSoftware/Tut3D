package com.monstrous.tut3d.nav;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NavActor {
    private NavMesh navMesh;
    public Vector3 startPoint;
    public Vector3 targetPoint;
    public Array<Vector3> path;
    private int wayPointIndex;
    private Vector3 wayPoint;
    public Array<NavNode> navNodePath = new Array<>();
    public boolean pathRebuilt;

    public NavActor(NavMesh navMesh) {
        this.navMesh = navMesh;

        path = new Array<>();
        navNodePath = new Array<>();
        pathRebuilt = true;
    }

    public void setTargetPoint( Vector3 target ){
        targetPoint = target;
    }

    public void setStartPoint( Vector3 start ){
        startPoint = start;
    }

    private Array<Vector3> getPath() {
        boolean rebuilt = navMesh.makePath(startPoint, targetPoint, navNodePath, path);
        if(rebuilt)
            wayPointIndex = 1;
        return path;
    }

    public Vector3 getWayPoint() {
        boolean rebuilt = navMesh.makePath(startPoint, targetPoint, navNodePath, path);
        if(rebuilt) {
            wayPointIndex = 1;
            Gdx.app.log("Cook path rebuilt", "" );
        }
        if(path.size > 1) {
            wayPoint = path.get(wayPointIndex);
            if (wayPointIndex < path.size - 1 && wayPoint.dst(startPoint) < 1) {     // reached a waypoint, move to next one
                wayPointIndex++;
                wayPoint = path.get(wayPointIndex);
                Gdx.app.log("Cook going to next waypoint", "WP:"+wayPoint.toString());
            }
        }
        return wayPoint;
    }
}
