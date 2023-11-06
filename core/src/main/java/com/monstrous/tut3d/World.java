package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {


    private final Array<GameObject> gameObjects;
    public GameObject player;
    private final SceneAsset sceneAsset;
    private boolean isDirty;

    public World(String modelFileName) {

        gameObjects = new Array<>();
        sceneAsset = new GLTFLoader().load(Gdx.files.internal(modelFileName));
        for(Node node : sceneAsset.scene.model.nodes){  // print some debug info
            Gdx.app.log("Node ", node.id);
        }
        isDirty = true;
    }

    public boolean isDirty(){
        return isDirty;
    }
    public void clear() {
        gameObjects.clear();
        player = null;
        isDirty = true;
    }
    public int getNumGameObjects() {
        return gameObjects.size;
    }

    public GameObject getGameObject(int index) {
        return gameObjects.get(index);
    }

    public GameObject spawnObject(String name, Vector3 position){
        Scene scene = new Scene(sceneAsset.scene, name);
        if(scene.modelInstance.nodes.size == 0){
            Gdx.app.error("Cannot find node in GLTF", name);
            return null;
        }
        scene.modelInstance.transform.translate(position);
        GameObject go = new GameObject(scene);
        gameObjects.add(go);
        isDirty = true;
        return go;
    }

    public void removeObject(GameObject gameObject){
        gameObjects.removeValue(gameObject, true);
        isDirty = true;
    }

    public void update( float deltaTime ) {
        // to be written
    }

    @Override
    public void dispose() {
        sceneAsset.dispose();
    }
}
