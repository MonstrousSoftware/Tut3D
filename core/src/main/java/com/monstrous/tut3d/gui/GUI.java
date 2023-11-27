package com.monstrous.tut3d.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.tut3d.GameScreen;
import com.monstrous.tut3d.Main;
import com.monstrous.tut3d.World;


public class GUI implements Disposable {

    public Stage stage;
    private final Skin skin;
    private final World world;
    private final GameScreen screen;
    private Label healthLabel;
    private Label timeLabel;
    private Label enemiesLabel;
    private Label coinsLabel;
    private Label gameOverLabel;
    private Label crossHairLabel;
    private Label fpsLabel;
    private TextButton restartButton;
    private final StringBuffer sb;

    public GUI(World world, GameScreen screen) {
        this.world = world;
        this.screen = screen;
        stage = new Stage(new ScreenViewport());
        skin = Main.assets.skin;
        sb = new StringBuffer();
    }

    private void rebuild() {

        stage.clear();

        BitmapFont bitmapFont= Main.assets.uiFont;
        Label.LabelStyle labelStyle = new Label.LabelStyle(bitmapFont, Color.BLUE);

        Table screenTable = new Table();
        screenTable.setFillParent(true);

        healthLabel = new Label("100%", labelStyle);
        timeLabel = new Label("00:00", labelStyle);
        enemiesLabel = new Label("2", labelStyle);
        coinsLabel = new Label("0", labelStyle);
        fpsLabel = new Label("00", labelStyle);
        gameOverLabel = new Label("GAME OVER", labelStyle);
        restartButton = new TextButton("RESTART", skin);

        //screenTable.debug();

        screenTable.top();
        // 4 columns: 2 at the left, 2 at the right
        // row 1
        screenTable.add(new Label("Health: ", labelStyle)).padLeft(5);
        screenTable.add(healthLabel).left().expandX();

        screenTable.add(new Label("Time: ",  labelStyle));
        screenTable.add(timeLabel).padRight(5);
        screenTable.row();

        // row 2
        screenTable.add(new Label("Enemies: ", labelStyle)).colspan(3).right();
        screenTable.add(enemiesLabel).padRight(5);
        screenTable.row();

        // row 3
        screenTable.add(new Label("Coins: ",  labelStyle)).colspan(3).right();
        screenTable.add(coinsLabel).padRight(5);
        screenTable.row();

        // row 4
        screenTable.add(gameOverLabel).colspan(4).row();
        gameOverLabel.setVisible(false);            // hide until needed

        // row 5
        screenTable.add(restartButton).colspan(4).pad(20);
        restartButton.setVisible(false);            // hide until needed
        screenTable.row();

        // row 6 at bottom of screen
        screenTable.add(new Label("FPS: ", labelStyle)).expandY().bottom().padLeft(5);
        screenTable.add(fpsLabel).left().expandX().bottom();

        screenTable.pack();

        stage.addActor(screenTable);

        // put cross-hair centre screen
        Table crossHairTable = new Table();
        crossHairTable.setFillParent(true);
        crossHairLabel = new Label("+", skin);
        crossHairTable.add(crossHairLabel);
        stage.addActor(crossHairTable);


        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                screen.restart();
                // hide restart button, game-over label and hide mouse cursor
                restartButton.setVisible(false);
                gameOverLabel.setVisible(false);
                Gdx.input.setCursorCatched(true);
            }
        });
    }

    private void updateLabels() {
        sb.setLength(0);
        sb.append((int)(world.getPlayer().health*100));
        sb.append("%");
        healthLabel.setText(sb.toString());

        sb.setLength(0);
        sb.append(Gdx.graphics.getFramesPerSecond());
        sb.append(" player at [x=");
        sb.append((int)world.getPlayer().getPosition().x);
        sb.append(", z=");
        sb.append((int)world.getPlayer().getPosition().z);
        sb.append("]");
        fpsLabel.setText(sb.toString());

        sb.setLength(0);
        int mm = (int) (world.stats.gameTime/60);
        int ss = (int)( world.stats.gameTime - 60*mm);
        if(mm <10)
            sb.append("0");
        sb.append(mm);
        sb.append(":");
        if(ss <10)
            sb.append("0");
        sb.append(ss);
        timeLabel.setText(sb.toString());

        sb.setLength(0);
        sb.append(world.stats.numEnemies);
        enemiesLabel.setText(sb.toString());

        sb.setLength(0);
        sb.append(world.stats.coinsCollected);
        coinsLabel.setText(sb.toString());

        if(world.stats.levelComplete){
            gameOverLabel.setText("LEVEL COMPLETED IN "+timeLabel.getText());
            gameOverLabel.setVisible(true);
            restartButton.setVisible(true);
            Gdx.input.setCursorCatched(false);
        }
        else  if(world.getPlayer().isDead()) {
            gameOverLabel.setText("GAME OVER");
            gameOverLabel.setVisible(true);
            restartButton.setVisible(true);
            Gdx.input.setCursorCatched(false);
        }
        else {
            gameOverLabel.setVisible(false);
            restartButton.setVisible(false);
            Gdx.input.setCursorCatched(true);
        }
    }

    public void showCrossHair( boolean show ){
        crossHairLabel.setVisible(show);
    }

    public void render(float deltaTime) {
        updateLabels();

        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        rebuild();
    }


    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
