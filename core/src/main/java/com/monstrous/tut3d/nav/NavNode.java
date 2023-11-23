package com.monstrous.tut3d.nav;


// node of the navigation mesh
// i.e. a triangle

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NavNode {
    public final int id;
    public final Vector3 p0, p1, p2;
    public Vector3 normal;
    private float d;        // for place equation
    public Array<NavNode> neighbours;
    private Vector3 p = new Vector3();      // tmp var
    public boolean degenerate = false;
    public int steps;
    public NavNode prev;


    public NavNode( int id, Vector3 a, Vector3 b, Vector3 c) {
        this.id = id;
        neighbours = new Array<>(3);
        p0 = new Vector3(a);
        p1 = new Vector3(b);
        p2 = new Vector3(c);

        float eps = 1e-16f;
        if(p0.epsilonEquals(p1, eps) || p1.epsilonEquals(p2, eps) || p2.epsilonEquals(p0, eps)) {
            // if this is not really a triangle because 2 verts are identical, or very close
            // mark it as degenerate and always fail isPointInTriangle() because the calculations won't work
            Gdx.app.log("degenerate triangle: "+id, "will be ignored");
            degenerate = true;
        }
        normal = new Vector3();

        Vector3 t1 = new Vector3(a);
        t1.sub(b);
        Vector3 t2 = new Vector3(c);
        t2.sub(b);
        normal.set(t2.crs(t1)).nor();      // use cross product of two edges to get normal vector (direction depends on winding order)

        // use a point on the plane (a) to find the distance value d of the plane equation: Ax + By + Cz + d = 0, where (A,B,C) is the normal
        d = -(normal.x*a.x + normal.y*a.y + normal.z*a.z);

    }

    public void addNeighbour( NavNode nbor  ){
        neighbours.add(nbor);
    }

    // https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
    //
    public boolean isPointInTriangle(Vector3 point, float maxDist)
    {
        if(degenerate)
            return false;

        // project point onto plane of the triangle
        float distanceToPlane = point.dot(normal)+d;
        if(distanceToPlane < 0) // point needs to be above the plane
            return false;
        if(distanceToPlane > maxDist)       // triangle too far below point, discard
            return false;
        p.set(normal).scl(-distanceToPlane);                // vector from point to plane, subtract this from point
        p.add(point);

        return Intersector.isPointInTriangle(p, p0, p1, p2);
    }
}
