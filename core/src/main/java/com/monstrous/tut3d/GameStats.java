package com.monstrous.tut3d;

public class GameStats {
    public float gameTime;
    public int numCoins;
    public int coinsCollected;
    public int numEnemies;
    public boolean levelComplete;

    public GameStats() {
        reset();
    }

    public void reset() {
        gameTime = 0;
        numCoins = 0;
        coinsCollected = 0;
        numEnemies = 0;
        levelComplete = false;
    }
}
