package com.monstrous.tut3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class Main extends Game {

    public static Assets assets;

    @Override
    public void create() {
        Settings.supportControllers = (Gdx.app.getType() == Desktop);

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
