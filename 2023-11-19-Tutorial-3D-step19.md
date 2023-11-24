# 3D Tutorial - Step 19 - Navigation Mesh
by Monstrous Software


# Step 19 - Navigation Mesh

Up to now the enemy characters have used a very simplistic routing logic.  They try to move in a straight line towards the player position. They stop at some specific distance from the player so they can throw their pans from there.  If they encounter an obstacle they will probably get stuck, although sometimes the collision response between a capsule shape and a wall will help the enemy slide along the wall towards a corner.

Even this simplistic logic works surprisingly well, but this in part because the map is very open and the player is probably moving around a lot so the enemies never get stuck for very long.

But it can be improved by making use of a so-called navigation mesh, navmesh for short.  This is a data structure that can be used by navigation actors, such as the enemies, to find a smart path from A to B.
In our case we will use it for the enemies to find a smart path towards the player.  In other words, all the navigation actors will have the same target. If different actors have different targets, e.g. an enemy will look for a health pack when they are low on health, it would need to be adapted.

To determine a smart route for the enemies we will follow four steps:
1. Construct a navigation mesh for the environment to define which areas are reachable and how they are connected.
2. Calculate for each node in the navigation mesh the distance to the target, i.e. the player position.
3. Determine for each enemy the shorted sequence of nodes to reach the player’s node making using the distance values.
4. For each enemy, construct an efficient sequence of way points through the node list so that the route never goes outside the node list and the path goes as close to inside corners as possible.  This is sometimes known as a string pulling algorithm.


A navigation mesh is a data structure that represents areas in the map (called ‘nodes’) and the connections between them.  In general, nodes can be any convex polygon. In our case, we will use triangles because we can conveniently extract triangles from a 3d model.


In fact, in a pinch we can construct the navmesh by hand in 3d modelling software such as Blender.

It is a little bit tedious but not so difficult in Blender because our map is quite simple.  Add a plane and call it “NAVMESH”. Select it and switch to Edit mode and select Top view.  Then select edges and extrude them to fit the open walkable space.  You can select vertices and reposition them.  Leave some space from the walls so that the characters don’t get stuck in walls or on corners.  These gaps should be roughly equivalent to the radius of your character.  Try to make sure that vertices which are at the same location are merged and keep the mesh as simple as possible. 

![nav mesh picture]

Of course doing this by hand is not ideal. And it needs to be redone whenever you change the map.  There are techniques to generate a navmesh automatically, but we’ll leave these for now.

Let us write some code to create a list of nodes from a mesh.  We already saw earlier some code to go through each triangle of a ModelInstance.  This code will be similar.

Let us first define a class for a node in the navigation mesh, a simple triangle with an id value for aid in debugging.

```java
        public class NavNode {
            public final int id;
            public final Vector3 p0, p1, p2;
        
            public NavNode( int id, Vector3 a, Vector3 b, Vector3 c) {
                this.id = id;
                p0 = new Vector3(a);
                p1 = new Vector3(b);
                p2 = new Vector3(c);
            }
        }
```

Now we can create a class to represent a navigation mesh. Its constructor will take a ModelInstance, decompose it into the vertices and indices of its mesh,
and generate an array of NavNode objects from this.


```java
public class NavMesh {
    public Array<NavNode> navNodes;         // node in nav mesh (triangles)


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
        Vector3 a = new Vector3();
        Vector3 b = new Vector3();
        Vector3 c = new Vector3();
        int numTriangles = numIndices/3;

        for(int i = 0; i < numIndices; i+=3) {

            int index = indices[i];
            float x = vertices[stride*index+posOffset];
            float y = vertices[stride*index+1+posOffset];
            float z = vertices[stride*index+2+posOffset];
            a.set(x, y, z);

            index = indices[i+1];
            x = vertices[stride*index+posOffset];
            y = vertices[stride*index+1+posOffset];
            z = vertices[stride*index+2+posOffset];
            b.set(x, y, z);

            index = indices[i+2];
            x = vertices[stride*index+posOffset];
            y = vertices[stride*index+1+posOffset];
            z = vertices[stride*index+2+posOffset];
            c.set(x, y, z);

            NavNode node = new NavNode(i/3, a, b, c);
            navNodes.add(node);
        }
        Gdx.app.log("Nav Nodes:", ""+navNodes.size);
}
```

This extracts the triangles from the mesh and stores it as an array of nodes. Now to add the connections between node.
Two triangles are connected if they share an edge. Let us update the NavNode class to keep a list of neighbours.  A triangle can have a maximum of three neighbours so we
can define the Array capacity as 3 to save memory. (The default Array capacity is 16 and is expanded as necessary). 


```java
public class NavNode {
    public final int id;
    public final Vector3 p0, p1, p2;
    public Array<NavNode> neighbours;

    public NavNode( int id, Vector3 a, Vector3 b, Vector3 c) {
        this.id = id;
        neighbours = new Array<>(3);
        p0 = new Vector3(a);
        p1 = new Vector3(b);
        p2 = new Vector3(c);
    }

    public void addNeighbour( NavNode nbor  ){
        neighbours.add(nbor);
    }
```

Having added storage for connectivity in the node class, we can now add the following code to the NavNesh constructore to determine which triangles are connected:

A first attempt of this code looked if two triangles shared the same index values.  However, it turned out there were some duplicate vertices in the handcrafted mesh. So a more
robust approach is to compare the positions of vertices.  And to allow for floating point errors or vertices which are very close together, we use `Vector3.epsilonEquals()` to compare them.

(Another approach would be to process the index and vertex arrays first to remove duplicate vertices).

In this code we check each pair of triangles and consider them connected if exactly two vertices of the first triangle appear also in the second triangle.


```java
        // now determine connectivity between triangles, i.e. which triangles share two vertices?
        int links = 0;
        for(int i = 0; i < numTriangles; i++) {
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
```

This concludes step 18.

This concludes the tutorial, although there are a lot of things to improve and to polish on the game, the principles of coding a basic 3d game should now hopefully be clear.
Thank you for reading up to here and if you intend to apply some of these lessons I wish you the best of luck.
