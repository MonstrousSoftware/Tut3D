package com.monstrous.tut3d;

public class GameObjectType {
    public final static GameObjectType TYPE_STATIC = new GameObjectType("static", true, false, false, false, false, false);
    public final static GameObjectType TYPE_PLAYER = new GameObjectType("player", false, true, false, false, false, false);
    public final static GameObjectType TYPE_PICKUP_COIN = new GameObjectType("coin", false, false, true, false , false, false);
    public final static GameObjectType TYPE_PICKUP_HEALTH = new GameObjectType("healthpack", false, false, true, false , false, false);
    public final static GameObjectType TYPE_PICKUP_GUN = new GameObjectType("gun", false, false, true, false , false, false);
    public final static GameObjectType TYPE_DYNAMIC = new GameObjectType("dynamic", false, false, false, false, false, false);
    public final static GameObjectType TYPE_ENEMY = new GameObjectType("enemy", false, false, false, true, false, false);
    public final static GameObjectType TYPE_FRIENDLY_BULLET = new GameObjectType("bullet", false, false, false, false, true,false);
    public final static GameObjectType TYPE_ENEMY_BULLET = new GameObjectType("bullet", false, false, false, false,false, true);


    public String typeName;
    public boolean isStatic;
    public boolean isPlayer;
    public boolean canPickup;
    public boolean isEnemy;
    public boolean isFriendlyBullet;
    public boolean isEnemyBullet;


    public GameObjectType(String typeName, boolean isStatic, boolean isPlayer, boolean canPickup, boolean isEnemy, boolean isFriendlyBullet, boolean isEnemyBullet) {
        this.typeName = typeName;
        this.isStatic = isStatic;
        this.isPlayer = isPlayer;
        this.canPickup = canPickup;
        this.isEnemy = isEnemy;
        this.isFriendlyBullet = isFriendlyBullet;
        this.isEnemyBullet = isEnemyBullet;
    }
}
