package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.monstrous.tut3d.views.GameView;
import com.monstrous.tut3d.views.GridView;
import com.monstrous.tut3d.views.PhysicsView;

public class GameScreen extends ScreenAdapter {

    private GameView gameView;
    private GridView gridView;
    private PhysicsView physicsView;
    private World world;
    private boolean debugRender = false;

    @Override
    public void show() {

        world = new World(Settings.GLTF_FILE);
        Populator.populate(world);
        gameView = new GameView(world);
        gridView = new GridView();
        physicsView = new PhysicsView(world);

        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        im.addProcessor(gameView.getCameraController());
        im.addProcessor(world.getPlayerController());

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

        Gdx.input.setCatchKey(Input.Keys.F1, true);
        Gdx.input.setCatchKey(Input.Keys.F2, true);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        if (Gdx.input.isKeyJustPressed(Input.Keys.R))
            Populator.populate(world);
        if (Gdx.input.isKeyJustPressed(Input.Keys.F))
            world.shootBall();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            debugRender = !debugRender;

        world.update(delta);

        gameView.render(delta);
        if(debugRender) {
            gridView.render(gameView.getCamera());
            physicsView.render(gameView.getCamera());
        }
    }

    @Override
    public void resize(int width, int height) {
        gameView.resize(width, height);
    }


    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        gameView.dispose();
        gridView.dispose();
        physicsView.dispose();
        world.dispose();
    }
}
