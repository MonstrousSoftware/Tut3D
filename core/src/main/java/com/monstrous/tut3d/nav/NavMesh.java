package com.monstrous.tut3d.nav;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.tut3d.Settings;

public class NavMesh {
    public final static float TARGET_MARGIN = 1f;       // target movement allowed before path recalculation

    public Array<NavNode> navNodes;         // node in nav mesh (triangles)

    // create a navigation mesh from the mesh of a model instance
    //
    public NavMesh() {
        navNodes = new Array<>();
    }

    public NavNode findNode( Vector3 point, float maxDist ){
        for(NavNode node : navNodes ) {
            if(node.isPointInTriangle(point, maxDist)) {
                return node;
            }
        }
        return null;
    }

    // find closest node - in case point is not in a node (off-piste)
    public NavNode findClosestNode( Vector3 point ){
        float minDist = Float.MAX_VALUE;
        NavNode closest = null;
        for(NavNode node : navNodes ) {
            float len2 = point.dst2(node.centre);
            if(len2 < minDist) {
                minDist = len2;
                closest = node;
            }
        }
        return closest;
    }

    // find the shortest node path from start to end node
    public void findNodePath( NavNode startNode, NavNode endNode, Array<NavNode> nodePath ) {

        // Dijkstra's algorithm
        //
        Array<NavNode> Q = new Array<>();

        for(int i = 0; i < navNodes.size; i++){
            NavNode node = navNodes.get(i);
            node.steps = Integer.MAX_VALUE;
            node.prev = null;
            Q.add(node);
        }
        startNode.steps = 0;
        startNode.prev = null;
        NavNode node = null;

        while(Q.size > 0) {
            // find node in Q with minimal distance
            int minSteps = Integer.MAX_VALUE;
            node = null;
            for(NavNode n : Q){
                if(n.steps < minSteps){
                    minSteps = n.steps;
                    node = n;
                }
            }
            if(node == endNode)    // arrived at end node, we can stop now
                break;
            Q.removeValue(node, true);
            for(NavNode nbor :  node.neighbours){
                if(Q.contains(nbor, true)){
                    int alt = node.steps + 1;
                    if(alt < nbor.steps){
                        nbor.steps = alt;
                        nbor.prev = node;
                    }
                }
            }
        } // while


        nodePath.clear();
        while (node != null) {
            nodePath.add(node);
            node = node.prev;
        }
        nodePath.reverse();
    }

    private Vector3 start = new Vector3();
    private Vector3 destination = new Vector3();

    // returns true if path was rebuilt
    public boolean  makePath( Vector3 startPoint, Vector3 targetPoint, Array<NavNode> navNodePath, Array<Vector3> pointPath ) {
        NavNode startNode = findNode(startPoint, Settings.navHeight);
        NavNode endNode = findNode(targetPoint, Settings.navHeight);

        start.set(startPoint);
        if(startNode == null) {
            startNode = findClosestNode(startPoint);        // use a reachable start, since the nav actor is outside the nav mesh
            start.set(startNode.centre);
        }

        destination.set(targetPoint);
        if(endNode == null) {
            endNode = findClosestNode(targetPoint);
            destination.set(endNode.centre);               // use a reachable destination, since the target is outside the nav mesh
        }

        // put start and end points at node height (on a slope this will be an approximation)
        start.y = startNode.centre.y;
        destination.y = endNode.centre.y;

        // if the target has moved (more than a margin), we need to recalculate
        // we assume the start node is following the node path, so we only check the end of the path versus the target
        if(navNodePath.size == 0 || pointPath.size == 0 || !destination.epsilonEquals(pointPath.get(pointPath.size-1), TARGET_MARGIN)) {
            findNodePath(startNode, endNode, navNodePath);
            NavStringPuller.makePath(start, destination, navNodePath, pointPath);
            return true;
        }
        return false;
    }
}
