package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.tut3d.behaviours.CookBehaviour;
import com.monstrous.tut3d.gui.GUI;
import com.monstrous.tut3d.inputs.MyControllerAdapter;
import com.monstrous.tut3d.physics.CollisionShapeType;
import com.monstrous.tut3d.views.GameView;
import com.monstrous.tut3d.views.GridView;
import com.monstrous.tut3d.nav.NavMeshView;
import com.monstrous.tut3d.physics.PhysicsView;

public class GameScreen extends ScreenAdapter {

    private GameView gameView;
    private GridView gridView;
    private GameView gunView;
    private PhysicsView physicsView;
    private NavMeshView navMeshView;
    private ScopeOverlay scopeOverlay;
    private World world;
    private World gunWorld;
    private GameObject gun;
    private GUI gui;
    private boolean debugRender = false;
    private boolean thirdPersonView = false;
    private boolean navScreen = false;
    private boolean lookThroughScope = false;
    private int windowedWidth, windowedHeight;

    @Override
    public void show() {

        world = new World();
        gui = new GUI(world, this);
        Populator.populate(world);
        gameView = new GameView(world,false, 0.1f, 300f, 1f);
        gameView.getCameraController().setThirdPersonMode(thirdPersonView);
        world.getPlayer().visible = thirdPersonView;            // hide player mesh in first person
        gridView = new GridView();
        physicsView = new PhysicsView(world);
        scopeOverlay = new ScopeOverlay();
        navMeshView = new NavMeshView(world);

        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        im.addProcessor(gui.stage);
        im.addProcessor(gameView.getCameraController());
        im.addProcessor(world.getPlayerController());

        if (Settings.supportControllers &&  Controllers.getCurrent() != null) {
            MyControllerAdapter controllerAdapter = new MyControllerAdapter(world.getPlayerController(), this);
            Gdx.app.log("Controller enabled", Controllers.getCurrent().getName());
            Controllers.addListener(controllerAdapter);
        }
        else
            Gdx.app.log("No Controller enabled", "");

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

        Gdx.input.setCatchKey(Input.Keys.F1, true);
        Gdx.input.setCatchKey(Input.Keys.F2, true);
        Gdx.input.setCatchKey(Input.Keys.F3, true);
        Gdx.input.setCatchKey(Input.Keys.F11, true);

        // load gun model
        gunWorld = new World();
        gunWorld.clear();
        gun = gunWorld.spawnObject(GameObjectType.TYPE_STATIC, "GunArmature", null,
            CollisionShapeType.BOX, true, new Vector3(0,0,0));
        gun.scene.animationController.allowSameAnimation = true;
        gun.scene.modelInstance.transform.setToScaling(Settings.gunScale, Settings.gunScale, Settings.gunScale);
        gun.scene.modelInstance.transform.setTranslation(Settings.gunPosition);

        // create an overlay view and add gun model
        gunView = new GameView(gunWorld, true, 0.01f, 5f, 0.2f);
    }


    public void restart() {
        Populator.populate(world);
        world.getPlayer().visible = thirdPersonView;            // hide player mesh in first person
    }

    private void setScopeMode( boolean scopeView ){
        // scope view is only activated if player is holding gun
        // and we're in first person view
        //
        boolean sv = scopeView && !thirdPersonView && world.weaponState.currentWeaponType == WeaponType.GUN;
        if(sv == this.lookThroughScope) // no change
            return;
        this.lookThroughScope = sv;
        if(sv)  // entering scope view
            gameView.setFieldOfView(20f);        // very narrow field of view
        else   // leaving scope view, back to normal view
            gameView.setFieldOfView(67f);
    }

    public void toggleViewMode() {
        thirdPersonView = !gameView.getCameraController().getThirdPersonMode();
        gameView.getCameraController().setThirdPersonMode(thirdPersonView);
        world.getPlayer().visible = thirdPersonView;            // hide player mesh in first person
        gameView.refresh();
    }

    private void toggleFullScreen() {        // toggle full screen / windowed screen
        if (!Gdx.graphics.isFullscreen()) {
            windowedWidth = Gdx.graphics.getWidth();        // remember current width & height
            windowedHeight = Gdx.graphics.getHeight();
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } else {
            Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
            resize(windowedWidth, windowedHeight);
        }
    }

    @Override
    public void render(float delta) {
        setScopeMode(world.weaponState.scopeMode);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        if (Gdx.input.isKeyJustPressed(Input.Keys.R))
            restart();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            debugRender = !debugRender;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2) )
            toggleViewMode();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) )
            navScreen = !navScreen;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11))
            toggleFullScreen();

        if(world.weaponState.firing){
            world.weaponState.firing = false;
            if(world.weaponState.currentWeaponType == WeaponType.GUN && !thirdPersonView && !lookThroughScope)
                gun.scene.animationController.setAnimation("Fire", 1);   // run the fire weapon animation once
            scopeOverlay.startRecoilEffect();
        }

        world.update(delta);

        float moveSpeed = world.getPlayer().body.getVelocity().len();
        gameView.render(delta, moveSpeed);
        if(debugRender) {
            gridView.render(gameView.getCamera());
            physicsView.render(gameView.getCamera());
        }
        if(navScreen) {
            navMeshView.update(world);
            navMeshView.render(gameView.getCamera());
        }

        if(!thirdPersonView && world.weaponState.currentWeaponType == WeaponType.GUN && !lookThroughScope) {
            gunView.render(delta, moveSpeed);
        }
        if(lookThroughScope)
            scopeOverlay.render(delta);
        gui.showCrossHair( !thirdPersonView && !lookThroughScope );
        gui.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        gameView.resize(width, height);
        gui.resize(width, height);
        scopeOverlay.resize(width, height);
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
        gui.dispose();
        world.dispose();
        scopeOverlay.dispose();
        navMeshView.dispose();
    }
}
