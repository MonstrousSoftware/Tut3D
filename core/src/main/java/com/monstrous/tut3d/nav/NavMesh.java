package com.monstrous.tut3d.nav;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.tut3d.Settings;

public class NavMesh {
    public Array<NavNode> navNodes;         // node in nav mesh (triangles)
    //public Array<NavNode> navNodePath = new Array<>();

    // create a navigation mesh from the mesh of a model instance
    //
    public NavMesh( ModelInstance instance ) {
        Mesh mesh = instance.nodes.first().parts.first().meshPart.mesh;
        int primitiveType = instance.nodes.first().parts.first().meshPart.primitiveType;
        if(primitiveType != GL20.GL_TRIANGLES)
            throw new RuntimeException("Nav mesh must be GL_TRIANGLES");

        int numVertices = mesh.getNumVertices();
        int numIndices = mesh.getNumIndices();
        int stride = mesh.getVertexSize()/4;        // floats per vertex in mesh, e.g. for position, normal, textureCoordinate, etc.

        float[] vertices = new float[numVertices*stride];
        short[] indices = new short[numIndices];
        // find offset of position floats per vertex, they are not necessarily the first 3 floats
        int posOffset = mesh.getVertexAttributes().findByUsage(VertexAttributes.Usage.Position).offset / 4;

        mesh.getVertices(vertices);
        mesh.getIndices(indices);

        navNodes = new Array<>();
        Vector3 corners[] = new Vector3[3];
        for(int i = 0; i < 3; i++)
            corners[i] = new Vector3();

        int id = 0;
        for(int i = 0; i < numIndices; i+=3) {
            for(int j = 0; j < 3; j++) {
                int index = indices[i+j];
                float x = vertices[stride * index + posOffset];
                float y = vertices[stride * index + 1 + posOffset];
                float z = vertices[stride * index + 2 + posOffset];
                corners[j].set(x, y, z);
            }
            // skip degenerate triangles (i.e. where two corners are the same)
            if(corners[0].epsilonEquals(corners[1]) ||
               corners[1].epsilonEquals(corners[2]) ||
               corners[2].epsilonEquals(corners[0])) {
                // if this is not really a triangle because 2 verts are identical, or very close
                // mark it as degenerate and always fail isPointInTriangle() because the calculations won't work
                Gdx.app.log("degenerate triangle: "+i/3, "will be ignored");
            }
            else {
                NavNode node = new NavNode(id++, corners[0], corners[1], corners[2]);
                navNodes.add(node);
            }
        }
        Gdx.app.log("Nav Nodes:", ""+navNodes.size);

        // now determine connectivity between triangles, i.e. which triangles share two vertices?
        int links = 0;
        for(int i = 0; i < navNodes.size; i++) {
            // note: multiple vertices can be equivalent, i.e. have same position, so check on position equivalence
            // not just if triangles use the same indices.
            // (Alternative would be to deduplicate such vertices before to share the same index)
            Vector3 p0 = navNodes.get(i).p0;
            Vector3 p1 = navNodes.get(i).p1;
            Vector3 p2 = navNodes.get(i).p2;
            for(int j = 0; j < i; j++) {
                Vector3 q0 = navNodes.get(j).p0;
                Vector3 q1 = navNodes.get(j).p1;
                Vector3 q2 = navNodes.get(j).p2;
                int matches = 0;
                if(p0.epsilonEquals(q0) || p0.epsilonEquals(q1) || p0.epsilonEquals(q2))
                    matches++;
                if(p1.epsilonEquals(q0) || p1.epsilonEquals(q1) || p1.epsilonEquals(q2))
                    matches++;
                if(p2.epsilonEquals(q0) || p2.epsilonEquals(q1) || p2.epsilonEquals(q2))
                    matches++;
                if(matches == 3)
                    throw new RuntimeException("Duplicate triangles");
                if(matches == 2){
                    navNodes.get(i).addNeighbour(navNodes.get(j));
                    navNodes.get(j).addNeighbour(navNodes.get(i));
                    Gdx.app.log("connection", ""+i+" & "+j);
                    links++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        Gdx.app.log("Nav Connections:", ""+links);
        for(int i = 0; i < navNodes.size; i++) {
            NavNode node = navNodes.get(i);
            sb.setLength(0);
            for(int j = 0; j < node.neighbours.size; j++) {
                sb.append(" ");
                sb.append(node.neighbours.get(j).id);
            }
            sb.append("\t[");
            sb.append(node.p0.toString());
            sb.append(node.p1.toString());
            sb.append(node.p2.toString());
            sb.append("]");
            Gdx.app.log("triangle", ""+i+" nbors: "+node.neighbours.size+" = "+sb.toString());

        }
        sb.setLength(0);
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

//    // update distance for each node to the target point
//    public void updateDistances( Vector3 targetPoint ) {
//
//        NavNode target = findNode( targetPoint, Settings.navHeight );
//        if(target == null) {
//              // Gdx.app.error("warning: updateDistances: start not in node", "");
//               target = findClosestNode(targetPoint);      // navigate towards centre of closest node to get back in the game
//        }
//
//        // Dijkstra's algorithm
//        //   here we are not looking for the shortest path, but to update each node with distance value to the target
//        Array<NavNode> Q = new Array<>();
//
//        for(int i = 0; i < navNodes.size; i++){
//            NavNode node = navNodes.get(i);
//            node.steps = Integer.MAX_VALUE;
//            node.prev = null;
//            Q.add(node);
//        }
//        target.steps = 0;
//
//        while(Q.size > 0) {
//            int minSteps = Integer.MAX_VALUE;
//            NavNode u = null;
//            for(NavNode n : Q){
//                if(n.steps < minSteps){
//                    minSteps = n.steps;
//                    u = n;
//                }
//            }
//            Q.removeValue(u, true);
//            for(int i = 0; i < u.neighbours.size; i++){
//                NavNode nbor = u.neighbours.get(i);
//                if(Q.contains(nbor, true)){
//                    int alt = u.steps + 1;
//                    if(alt < nbor.steps){
//                        nbor.steps = alt;
//                        nbor.prev = u;
//                    }
//                }
//            }
//        } // while
//
//    }
//
//    private void  findPath( Vector3 startPoint, Array<NavNode> path ) {
//        NavNode node = findNode(startPoint, Settings.navHeight);
//        path.clear();
//        while (node != null) {
//            path.add(node);
//            node = node.prev;
//        }
//    }

    // find the shortest node path from start to end node
    public void findNodePath( NavNode startNode, NavNode endNode, Array<NavNode> nodePath ) {

        // Dijkstra's algorithm
        //   here we are not looking for the shortest path, but to update each node with distance value to the target
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

    // returns true if path was rebuilt
    public boolean  makePath( Vector3 startPoint, Vector3 targetPoint, Array<NavNode> navNodePath, Array<Vector3> pointPath ) {

        NavNode startNode = findNode(startPoint, Settings.navHeight);
        NavNode endNode = findNode(targetPoint, Settings.navHeight);

        if(startNode == null)
            startNode = findClosestNode(startPoint);

        if(endNode == null)
            endNode = findClosestNode(targetPoint);

//        if(pointPath.size > 0) {
//            NavNode endNode = findNode(endPoint, Settings.navHeight);
//            if(endNode == null) {
//                endNode = findClosestNode(endPoint);        // we cannot get to the target, go to the centre of the closest node
//                endPoint.set(endNode.centre);
//            }
//            endPoint.y = endNode.p0.y;
//            Vector3 end = pointPath.get(pointPath.size - 1);
//            if (end.dst(endPoint) < 1f)      // if existing path leads (close to) to target point, we don't need to recalculate
//                return false;
//
//            NavNode startNode = findNode(startPoint, Settings.navHeight);
//            if(startNode == null) {
//                startNode = findClosestNode(startPoint);        // we cannot get to the target, go to the centre of the closest node
//            }
//            start.y = startNode.p0.y;
//        }

        // recalculate node path if target node has changed
        if(navNodePath.size == 0 || navNodePath.get(navNodePath.size-1) != endNode) {
            Gdx.app.log("find path", "recalculating path");
            findNodePath(startNode, endNode, navNodePath);
        }
        NavStringPuller.makePath(startPoint, targetPoint, navNodePath, pointPath);
        return true;
    }

//    // returns true if path was rebuilt
//    public boolean  makePathOld( Vector3 startPoint, Vector3 targetPoint, Array<Vector3> pointPath ) {
//
//        Vector3 start = new Vector3(startPoint);
//        Vector3 endPoint = new Vector3(targetPoint);
//        if(pointPath.size > 0) {
//            NavNode endNode = findNode(endPoint, Settings.navHeight);
//            if(endNode == null) {
//                endNode = findClosestNode(endPoint);        // we cannot get to the target, go to the centre of the closest node
//                endPoint.set(endNode.centre);
//            }
//            endPoint.y = endNode.p0.y;
//            Vector3 end = pointPath.get(pointPath.size - 1);
//            if (end.dst(endPoint) < 1f)      // if existing path leads (close to) to target point, we don't need to recalculate
//                return false;
//
//            NavNode startNode = findNode(startPoint, Settings.navHeight);
//            if(startNode == null) {
//                startNode = findClosestNode(startPoint);        // we cannot get to the target, go to the centre of the closest node
//            }
//            start.y = startNode.p0.y;
//        }
//
//        Array<NavNode> navNodePath = new Array<>();
//        Gdx.app.log("find path", "recalculating path");
//        findPath(start, navNodePath);
//        NavStringPuller.makePath(start, endPoint, navNodePath, pointPath);
//        return true;
//    }
}
