package com.monstrous.tut3d;

import com.badlogic.gdx.Game;

public class Main extends Game {

    public static Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        assets.finishLoading();
        setScreen( new GameScreen() );
    }

    @Override
    public void dispose() {
        assets.dispose();
        super.dispose();
    }
}
