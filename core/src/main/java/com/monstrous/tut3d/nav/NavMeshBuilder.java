package com.monstrous.tut3d.nav;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class NavMeshBuilder {

    // create a navigation mesh from the mesh of a model instance
    //
    public static NavMesh build(ModelInstance instance ) {

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

        NavMesh navMesh = new NavMesh();
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
                navMesh.navNodes.add(node);
            }
        }
        Gdx.app.log("Nav Nodes:", ""+navMesh.navNodes.size);

        // now determine connectivity between triangles, i.e. which triangles share two vertices?
        int links = 0;
        for(int i = 0; i < navMesh.navNodes.size; i++) {
            // note: multiple vertices can be equivalent, i.e. have same position, so check on position equivalence
            // not just if triangles use the same indices.
            // (Alternative would be to deduplicate such vertices before to share the same index)
            Vector3 p0 = navMesh.navNodes.get(i).p0;
            Vector3 p1 = navMesh.navNodes.get(i).p1;
            Vector3 p2 = navMesh.navNodes.get(i).p2;
            for(int j = 0; j < i; j++) {
                Vector3 q0 = navMesh.navNodes.get(j).p0;
                Vector3 q1 = navMesh.navNodes.get(j).p1;
                Vector3 q2 = navMesh.navNodes.get(j).p2;
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
                    navMesh.navNodes.get(i).addNeighbour(navMesh.navNodes.get(j));
                    navMesh.navNodes.get(j).addNeighbour(navMesh.navNodes.get(i));
                    //Gdx.app.log("connection", ""+i+" & "+j);
                    links++;
                }
            }
        }
        Gdx.app.log("Nav Connections:", ""+links);

//        StringBuilder sb = new StringBuilder();
//        for(int i = 0; i < navMesh.navNodes.size; i++) {
//            NavNode node = navMesh.navNodes.get(i);
//            sb.setLength(0);
//            for(int j = 0; j < node.neighbours.size; j++) {
//                sb.append(" ");
//                sb.append(node.neighbours.get(j).id);
//            }
//            sb.append("\t[");
//            sb.append(node.p0.toString());
//            sb.append(node.p1.toString());
//            sb.append(node.p2.toString());
//            sb.append("]");
//            Gdx.app.log("triangle", ""+i+" nbors: "+node.neighbours.size+" = "+sb.toString());
//
//        }
//        sb.setLength(0);

        return navMesh;
    }

}
