package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.monstrous.tut3d.views.GameView;
import com.monstrous.tut3d.views.GridView;
import com.monstrous.tut3d.views.PhysicsView;

public class GameScreen extends ScreenAdapter {

    private GameView gameView;
    private GridView gridView;
    private PhysicsView physicsView;
    private World world;
    private CamController camController;

    @Override
    public void show() {

        world = new World("models/step4a.gltf");
        Populator.populate(world);
        gameView = new GameView(world);
        gridView = new GridView();
        physicsView = new PhysicsView(world);

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.R))
            Populator.populate(world);

        // update
        camController.update(delta);
        world.update(delta);
        gameView.render(delta);
        gridView.render(gameView.getCamera());
        physicsView.render(gameView.getCamera());
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
