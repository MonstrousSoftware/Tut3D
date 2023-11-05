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

    private GameView gameView;
    private SceneAsset sceneAsset;
    public Array<GameObject> gameObjects;
    public GameObject player;

    public World(GameView gameView) {
        this.gameView = gameView;
        gameObjects = new Array<>();
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/step4a.gltf"));
        for(Node node : sceneAsset.scene.model.nodes){  // print some debug info
            Gdx.app.log("Node ", node.id);
        }
    }

    public void clear() {
        gameObjects.clear();
        gameView.clear();
        player = null;
    }

    public GameObject spawnObject(String name, Vector3 position){
        Scene scene = new Scene(sceneAsset.scene, name);
        if(scene.modelInstance.nodes.size == 0){
            Gdx.app.error("Cannot find node in GLTF", name);
            return null;
        }
        gameView.add(scene);
        scene.modelInstance.transform.translate(position);
        GameObject go = new GameObject(scene);
        gameObjects.add(go);
        return go;
    }

    public void removeObject(GameObject gameObject){
        gameObjects.removeValue(gameObject, true);
        gameView.remove(gameObject.scene);
    }

    public void update( float deltaTime ) {
        // to be written
    }

    @Override
    public void dispose() {
        sceneAsset.dispose();
    }
}
