package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.behaviours.CookBehaviour;
import com.monstrous.tut3d.inputs.PlayerController;
import com.monstrous.tut3d.physics.*;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {

    private final Array<GameObject> gameObjects;
    private GameObject player;
    public GameStats stats;
    private final SceneAsset sceneAsset;
    private boolean isDirty;
    private final PhysicsWorld physicsWorld;
    private final PhysicsBodyFactory factory;
    private final PlayerController playerController;
    private final PhysicsRayCaster rayCaster;

    public World(String modelFileName) {

        gameObjects = new Array<>();
        stats = new GameStats();
        sceneAsset = new GLTFLoader().load(Gdx.files.internal(modelFileName));
        for(Node node : sceneAsset.scene.model.nodes){  // print some debug info
            Gdx.app.log("Node ", node.id);
        }
        isDirty = true;
        physicsWorld = new PhysicsWorld(this);
        factory = new PhysicsBodyFactory(physicsWorld);
        rayCaster = new PhysicsRayCaster(physicsWorld);
        playerController = new PlayerController(rayCaster);
    }

    public boolean isDirty(){
        return isDirty;
    }

    public void clear() {
        physicsWorld.reset();
        playerController.reset();
        stats.reset();

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

    public GameObject getPlayer() {
        return player;
    }

    public void setPlayer( GameObject player ){
        this.player = player;
        player.body.setCapsuleCharacteristics();
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public GameObject spawnObject(GameObjectType type, String name, String proxyName, CollisionShapeType shapeType, boolean resetPosition, Vector3 position, float mass){
        Scene scene = loadNode( name, resetPosition, position );
        ModelInstance collisionInstance = scene.modelInstance;
        if(proxyName != null) {
            Scene proxyScene = loadNode( proxyName, resetPosition, position );
            collisionInstance = proxyScene.modelInstance;
        }
        PhysicsBody body = factory.createBody(collisionInstance, shapeType, mass, type.isStatic);
        GameObject go = new GameObject(type, scene, body);
        gameObjects.add(go);
        isDirty = true;         // list of game objects has changed
        if(go.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies++;
        if(go.type == GameObjectType.TYPE_PICKUP_COIN)
           stats.numCoins++;
        return go;
    }

    private Scene loadNode( String nodeName, boolean resetPosition, Vector3 position ) {
        Scene scene = new Scene(sceneAsset.scene, nodeName);
        if(scene.modelInstance.nodes.size == 0)
            throw new RuntimeException("Cannot find node in GLTF file: " + nodeName);
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first());         // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        return scene;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node ){
        if(!resetPosition)
            modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0,0,0);
        node.scale.set(1,1,1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

    public void removeObject(GameObject gameObject){
        gameObject.health = 0;
        if(gameObject.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies--;
        gameObjects.removeValue(gameObject, true);
        gameObject.dispose();
        isDirty = true;     // list of game objects has changed
    }

    public void update( float deltaTime ) {
        stats.gameTime += deltaTime;
        //Gdx.app.log(""+stats.gameTime, "Enemies:"+stats.numEnemies+" Coins:"+stats.coinsCollected+"/"+stats.numCoins+" health:"+(player.health*100));
        playerController.update(player, deltaTime);
        physicsWorld.update();
        syncToPhysics();
        for(GameObject go : gameObjects)
            go.update(this, deltaTime);
    }

    private void syncToPhysics() {
        for(GameObject go : gameObjects){
            if( go.body.geom.getBody() != null) {
                if(go.type == GameObjectType.TYPE_PLAYER){
                    // use information from the player controller, since the rigid body is not rotated.
                    player.scene.modelInstance.transform.setToRotation(Vector3.Z, playerController.getForwardDirection());
                    player.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else if(go.type == GameObjectType.TYPE_ENEMY){
                    CookBehaviour cb = (CookBehaviour) go.behaviour;
                    go.scene.modelInstance.transform.setToRotation(Vector3.Z, cb.getDirection());
                    go.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else
                    go.scene.modelInstance.transform.set(go.body.getPosition(), go.body.getOrientation());
            }
        }
    }

    private final Vector3 dir = new Vector3();
    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootDirection = new Vector3();

    public void shootBall() {
        if(player.isDead())
            return;
        dir.set( playerController.getViewingDirection() );
        spawnPos.set(dir);
        spawnPos.add(player.getPosition()); // spawn from 1 unit in front of the player
        GameObject ball = spawnObject(GameObjectType.TYPE_FRIENDLY_BULLET, "ball", null, CollisionShapeType.SPHERE, true, spawnPos, Settings.ballMass );
        shootDirection.set(dir);        // shoot forward
        shootDirection.y += 0.5f;       // and slightly up
        shootDirection.scl(Settings.ballForce);   // scale for speed
        ball.body.geom.getBody().setDamping(0.0f, 0.0f);
        ball.body.applyForce(shootDirection);
    }

    public void onCollision(GameObject go1, GameObject go2){
        // try either order
        if(go1.type.isStatic || go2.type.isStatic)
            return;

        Gdx.app.log("onCollision", go1.type.typeName+" x "+go2.type.typeName);
        handleCollision(go1, go2);
        handleCollision(go2, go1);
    }

    private void handleCollision(GameObject go1, GameObject go2){
        if(go1.type.isPlayer && go2.type.canPickup){
            pickup(go1, go2);
        }
        if(go1.type.isPlayer && go2.type.isEnemyBullet)
            bulletHit(go1, go2);

        if(go1.type.isEnemy && go2.type.isFriendlyBullet)
            bulletHit(go1, go2);
    }

    private void pickup(GameObject character, GameObject pickup){

        removeObject(pickup);
        if(pickup.type == GameObjectType.TYPE_PICKUP_COIN)
            stats.coinsCollected++;
        if(pickup.type == GameObjectType.TYPE_PICKUP_HEALTH)
            character.health = Math.min(character.health + 0.5f, 1f);   // +50% health
    }

    private void bulletHit(GameObject character, GameObject bullet) {
        removeObject(bullet);
        character.health -= 0.25f;      // - 25% health
        if(character.isDead())
            removeObject(character);
    }

    @Override
    public void dispose() {

        sceneAsset.dispose();
        physicsWorld.dispose();
        rayCaster.dispose();
    }
}
