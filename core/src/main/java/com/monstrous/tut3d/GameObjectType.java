package com.monstrous.tut3d;

public class GameObjectType {
    public final static GameObjectType TYPE_STATIC = new GameObjectType("static", true, false, false);
    public final static GameObjectType TYPE_PLAYER = new GameObjectType("player", false, true, false);
    public final static GameObjectType TYPE_PICKUP_COIN = new GameObjectType("coin", false, false, true);
    public final static GameObjectType TYPE_PICKUP_HEALTH = new GameObjectType("health", false, false, true);
    public final static GameObjectType TYPE_DYNAMIC = new GameObjectType("dynamic", false, false, false);


    public String typeName;
    public boolean isStatic;
    public boolean isPlayer;
    public boolean canPickup;

    public GameObjectType(String typeName, boolean isStatic, boolean isPlayer, boolean canPickup) {
        this.typeName = typeName;
        this.isStatic = isStatic;
        this.isPlayer = isPlayer;
        this.canPickup = canPickup;
    }
}
