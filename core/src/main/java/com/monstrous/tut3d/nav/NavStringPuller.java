package com.monstrous.tut3d.nav;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.tut3d.Settings;

public class NavStringPuller {

    public static Array<Portal> portals = new Array<>();

    // a portal is an edge between two adjacent nodes (i.e. triangles) on the path
    public static class Portal {
        public Vector3 left;
        public Vector3 right;
        public boolean slopeChange;     // indicator if portal connects nodes with different slope

        public Portal(Vector3 left, Vector3 right, boolean slopeChange ) {
            this.left = new Vector3(left);
            this.right = new Vector3(right);
            this.slopeChange = slopeChange;
        }
    }


    // string pulling algo:
    // "simple stupid funnel algorithm" by Mikko Mononen
    //

    public static void makePath(Vector3 startPoint, Vector3 targetPoint, Array<NavNode> nodePath, Array<Vector3> pointPath ) {

        //Array<Portal> portals = new Array<>();
        Vector3 edgeStart = new Vector3();
        Vector3 edgeEnd = new Vector3();

        // build a list of portals, i.e. edges between triangles on the node path to the goal
//        NavNode startNode = findNode(startPoint, Settings.navHeight);
//        if(startNode != null)
//            startPoint.y = startNode.p0.y;                                          // make sure to use node (floor) height, not character height
        portals.clear();
        portals.add(new Portal(startPoint, startPoint, false));

        for (int i = 0; i < nodePath.size - 1; i++) {
            NavNode node = nodePath.get(i);
            NavNode nextNode = nodePath.get(i + 1);
            getEdge(node, nextNode, edgeStart, edgeEnd);
            boolean slopeChange = ( node.normal.dot(nextNode.normal) < 0.99f ); // use dot product of normals to detect slope change
            portals.add(new Portal(edgeEnd, edgeStart, slopeChange));
        }
        Vector3 endPoint = new Vector3(targetPoint);
//        NavNode endNode = findNode(endPoint, Settings.navHeight);
//        if(endNode == null) {
//            endNode = findClosestNode(endPoint);        // we cannot get to the target, go to the centre of the closest node
//            endPoint.set(endNode.centre);
//        }
//        endPoint.y = endNode.p0.y;
        portals.add(new Portal(endPoint, endPoint, false));

        // use the portals to create a list of way points
        pointPath.clear();
        pointPath.add(new Vector3(startPoint));

        // define a funnel with an apex, a left foot and a right foot
        Vector3 apex = startPoint;
        Vector3 leftFoot = startPoint;
        Vector3 rightFoot = startPoint;
        int apexIndex = 0, leftIndex = 0, rightIndex = 0;

        for (int i = 1; i < portals.size; i++) {
            Portal portal = portals.get(i);

            // update right leg
            if ( area(apex, rightFoot, portal.right) <= 0) {
                if (apex.epsilonEquals(rightFoot) || area(apex, leftFoot, portal.right) > 0f) {
                    // tighten the funnel
                    rightFoot = portal.right;
                    rightIndex = i;
                } else {
                    // right over left,insert left into path and restart scan from left foot
                    pointPath.add(new Vector3(leftFoot));
                    apex = leftFoot;
                    apexIndex = leftIndex;
                    // reset portal
                    leftFoot = apex;
                    rightFoot = apex;
                    leftIndex = apexIndex;
                    rightIndex = apexIndex;
                    i = apexIndex;
                    continue;
                }
            }
            // update left leg
            if (area(apex, leftFoot, portal.left) >= 0) {
                if (apex.epsilonEquals(leftFoot) || area(apex, rightFoot, portal.left) < 0f) {
                    // tighten the funnel
                    leftFoot = portal.left;
                    leftIndex = i;
                } else {
                    // left over right, insert right into path and restart scan from right foot
                    pointPath.add(new Vector3(rightFoot));
                    apex = rightFoot;
                    apexIndex = rightIndex;
                    // reset portal
                    leftFoot = apex;
                    rightFoot = apex;
                    leftIndex = apexIndex;
                    rightIndex = apexIndex;
                    i = apexIndex;
                    continue;
                }
            }

            // force a way point on a slope change so that the path follows the slopes (e.g. over a bridge)
            // this is an addition to SSFA
            if(portal.slopeChange){
                Vector3 wayPoint = new Vector3(portal.left).add(portal.right).scl(0.5f);    // mid point of portal
                pointPath.add( wayPoint );
                apex = wayPoint;
                apexIndex = i;
                // reset portal
                leftFoot = apex;
                rightFoot = apex;
                leftIndex = apexIndex;
                rightIndex = apexIndex;
                continue;
            }

        }
        pointPath.add(endPoint);
    }

    // make a path by going through the mid-point of connecting edges
    // looks rubbish
    public static void makePathMidPoints( Vector3 startPoint, Vector3 targetPoint, Array<NavNode> nodePath, Array<Vector3> pointPath ) {

        Vector3 edgeStart = new Vector3();
        Vector3 edgeEnd = new Vector3();
        pointPath.clear();
        pointPath.add(new Vector3(startPoint));
        for(int i = 0; i < nodePath.size-1; i++ ) {
            NavNode node = nodePath.get(i);
            NavNode nextNode = nodePath.get(i+1);
            getEdge(node, nextNode, edgeStart, edgeEnd);

            Vector3 midPoint = new Vector3(edgeStart);
            midPoint.add(edgeEnd);
            midPoint.scl(1/2f);
            pointPath.add(  midPoint );
        }
        pointPath.add(  targetPoint );
    }

    // get the edge between two triangles that we know are connected
    private static void getEdge(NavNode a, NavNode b, Vector3 start, Vector3 end ){

        boolean p0matches = (a.p0.epsilonEquals(b.p0) || a.p0.epsilonEquals(b.p1)|| a.p0.epsilonEquals(b.p2) );
        boolean p1matches = (a.p1.epsilonEquals(b.p0) || a.p1.epsilonEquals(b.p1)|| a.p1.epsilonEquals(b.p2) );
        boolean p2matches = (a.p2.epsilonEquals(b.p0) || a.p2.epsilonEquals(b.p1)|| a.p2.epsilonEquals(b.p2) );

        if(p0matches && p1matches){
            start.set(a.p0);
            end.set(a.p1);
        }
        else if(p1matches && p2matches){
            start.set(a.p1);
            end.set(a.p2);
        }
        else if(p2matches && p0matches){
            start.set(a.p2);
            end.set(a.p0);
        }
        else
            throw new RuntimeException("Cannot match edges");
    }


    // 2d function to test if triangle a,b,c has positive or negative area, negative means the funnel legs are crossed
    private static float area(Vector3 a, Vector3 b, Vector3 c) {
        float ax = b.x - a.x;
        float az = b.z - a.z;
        float bx = c.x - a.x;
        float bz = c.z - a.z;
        return - (bx*az - ax*bz);
    }
}
