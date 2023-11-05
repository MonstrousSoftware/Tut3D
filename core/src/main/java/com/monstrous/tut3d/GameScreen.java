package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;

public class GameScreen extends ScreenAdapter {

    private GameView gameView;
    private World world;
    private CamController camController;

    @Override
    public void show() {
        gameView = new GameView();
        world = new World(gameView);
        Populator.populate(world);

        camController = new CamController (gameView.getCamera());
        Gdx.input.setInputProcessor(camController);

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();

        // update
        camController.update(Gdx.graphics.getDeltaTime());
        world.update(delta);
        gameView.render(delta);
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
        world.dispose();
    }
}
