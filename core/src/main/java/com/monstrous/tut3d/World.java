package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.tut3d.behaviours.CookBehaviour;
import com.monstrous.tut3d.inputs.PlayerController;
import com.monstrous.tut3d.nav.NavMesh;
import com.monstrous.tut3d.nav.NavNode;
import com.monstrous.tut3d.physics.*;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {

    private final Array<GameObject> gameObjects;
    private GameObject player;
    public GameObject theCook;  // TMP
    public GameStats stats;
    private final SceneAsset sceneAsset;
    private final PhysicsWorld physicsWorld;
    private final PhysicsBodyFactory factory;
    private final PlayerController playerController;
    public final PhysicsRayCaster rayCaster;
    public final WeaponState weaponState;
    public NavMesh navMesh;
    public NavNode navNode;
    public NavNode targetNavNode;
    public Array<NavNode> path;
    public Vector3 targetPosition;
    private int prevNode = -1;

    public World() {
        gameObjects = new Array<>();
        stats = new GameStats();
        sceneAsset = Main.assets.sceneAsset;
//        for(Node node : sceneAsset.scene.model.nodes){  // print some debug info
//            Gdx.app.log("Node ", node.id);
//        }
        physicsWorld = new PhysicsWorld(this);
        factory = new PhysicsBodyFactory(physicsWorld);
        rayCaster = new PhysicsRayCaster(physicsWorld);
        playerController = new PlayerController(this);
        weaponState = new WeaponState();
    }

    public void clear() {
        physicsWorld.reset();
        playerController.reset();
        stats.reset();
        weaponState.reset();

        gameObjects.clear();
        player = null;
        navMesh = null;
        prevNode = -1;
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

    public GameObject spawnObject(GameObjectType type, String name, String proxyName, CollisionShapeType shapeType, boolean resetPosition, Vector3 position){
        Scene scene = loadNode( name, resetPosition, position );
        ModelInstance collisionInstance = scene.modelInstance;
        if(proxyName != null) {
            Scene proxyScene = loadNode( proxyName, resetPosition, position );
            collisionInstance = proxyScene.modelInstance;
        }
        if(type == GameObjectType.TYPE_NAVMESH){
            navMesh = new NavMesh(scene.modelInstance);
        }
        PhysicsBody body = factory.createBody(collisionInstance, shapeType, type.isStatic);
        GameObject go = new GameObject(type, scene, body);
        gameObjects.add(go);
        if(go.type == GameObjectType.TYPE_ENEMY)
            stats.numEnemies++;
        if(go.type == GameObjectType.TYPE_PICKUP_COIN)
           stats.numCoins++;
        if(go.type == GameObjectType.TYPE_PICKUP_COIN) {      // TMP
            targetNavNode = navMesh.findNode(go.getPosition(),  1f);
            Gdx.app.log("target is in nav node:", ""+targetNavNode.id+" pos:"+ go.getPosition().toString());
            targetPosition = new Vector3(go.getPosition());
        }

        if(go.type == GameObjectType.TYPE_PLAYER) {      // TMP
            navNode = navMesh.findNode( go.getPosition(), Settings.groundRayLength );
            if(navNode == null)
                Gdx.app.error("** NO NAV NODE player is in nav node:", " pos:"+ go.getPosition().toString());
            else
                Gdx.app.log("player is in nav node:", ""+navNode.id+" pos:"+ go.getPosition().toString());
        }
        if(go.type == GameObjectType.TYPE_ENEMY) {      // TMP
            theCook = go;
        }
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
    }



    public void update( float deltaTime ) {
        if(stats.numEnemies > 0 || stats.coinsCollected < stats.numCoins)
            stats.gameTime += deltaTime;
        else {
            if(!stats.levelComplete)
                Main.assets.sounds.GAME_COMPLETED.play();
            stats.levelComplete = true;
        }
        weaponState.update(deltaTime);
        playerController.update(player, deltaTime);
        physicsWorld.update();
        syncToPhysics();
        for(GameObject go : gameObjects) {
            if(go.getPosition().y < -10)        // delete objects that fell off the map
                removeObject(go);
            go.update(this, deltaTime);
        }


        navNode = navMesh.findNode( player.getPosition(), Settings.groundRayLength );
        if(navNode == null)
            Gdx.app.error("** NO NAV NODE player is in nav node:", " pos:"+ player.getPosition().toString());
        else if (navNode.id != prevNode) {
            Gdx.app.log("player is in nav node:", "" + navNode.id + " pos:" + player.getPosition().toString());
            prevNode = navNode.id;

            navMesh.updateDistances(player.getPosition());
        }

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


    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootForce = new Vector3();
    private final Vector3 impulse = new Vector3();

    // fire current weapon
    public void fireWeapon(Vector3 viewingDirection,  PhysicsRayCaster.HitPoint hitPoint) {
        if(player.isDead())
            return;
        if(!weaponState.isWeaponReady())  // to give delay between shots
            return;
        weaponState.firing = true;    // set state to firing (triggers gun animation in GameScreen)

        switch(weaponState.currentWeaponType) {
            case BALL:
                spawnPos.set(viewingDirection);
                spawnPos.add(player.getPosition()); // spawn from 1 unit in front of the player
                GameObject ball = spawnObject(GameObjectType.TYPE_FRIENDLY_BULLET, "ball", null, CollisionShapeType.SPHERE, true, spawnPos );
                shootForce.set(viewingDirection);        // shoot in viewing direction (can be up or down from player direction)
                shootForce.scl(Settings.ballForce);   // scale for speed
                ball.body.geom.getBody().setDamping(0.0f, 0.0f);
                ball.body.applyForce(shootForce);
                break;
            case GUN:
                Main.assets.sounds.GUN_SHOT.play();
                if(hitPoint.hit) {
                    GameObject victim = hitPoint.refObject;
                    Gdx.app.log("gunshot hit", victim.scene.modelInstance.nodes.first().id);
                    if(victim.type.isEnemy)
                        bulletHit(victim);

                    impulse.set(victim.getPosition()).sub(player.getPosition()).nor().scl(Settings.gunForce);
                    if(victim.body.geom.getBody() != null ) {
                        victim.body.geom.getBody().enable();
                        victim.body.applyForceAtPos(impulse, hitPoint.worldContactPoint);
                    }
                }
                break;
        }
    }




    public void onCollision(GameObject go1, GameObject go2){
        // try either order
        if(go1.type.isStatic || go2.type.isStatic)
            return;

        handleCollision(go1, go2);
        handleCollision(go2, go1);
    }

    private void handleCollision(GameObject go1, GameObject go2) {
        if (go1.type.isPlayer && go2.type.canPickup) {
            pickup(go1, go2);
        }
        if (go1.type.isPlayer && go2.type.isEnemyBullet) {
            removeObject(go2);
            bulletHit(go1);
        }

        if(go1.type.isEnemy && go2.type.isFriendlyBullet) {
            removeObject(go2);
            bulletHit(go1);
        }
    }

    private void pickup(GameObject character, GameObject pickup){

        removeObject(pickup);
        if(pickup.type == GameObjectType.TYPE_PICKUP_COIN) {
            stats.coinsCollected++;
            Main.assets.sounds.COIN.play();
        }
        else if(pickup.type == GameObjectType.TYPE_PICKUP_HEALTH) {
            character.health = Math.min(character.health + 0.5f, 1f);   // +50% health
            Main.assets.sounds.UPGRADE.play();
        }
        else if(pickup.type == GameObjectType.TYPE_PICKUP_GUN) {
            weaponState.haveGun = true;
            weaponState.currentWeaponType = WeaponType.GUN;
            Main.assets.sounds.UPGRADE.play();
        }
    }

    private void bulletHit(GameObject character) {
        character.health -= 0.25f;      // - 25% health
        Main.assets.sounds.HIT.play();
        if(character.isDead()) {
            removeObject(character);
            if (character.type.isPlayer)
                Main.assets.sounds.GAME_OVER.play();
        }
    }

    @Override
    public void dispose() {
        physicsWorld.dispose();
        rayCaster.dispose();
    }
}
