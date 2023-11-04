package com.monstrous.tut3d;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

public class CamController extends InputAdapter {
    public int forwardKey = Input.Keys.W;
    public int backwardKey = Input.Keys.S;
    public int strafeLeftKey = Input.Keys.A;
    public int strafeRightKey = Input.Keys.D;
    public int turnLeftKey = Input.Keys.Q;
    public int turnRightKey = Input.Keys.E;
    public int runShiftKey = Input.Keys.SHIFT_LEFT;

    protected final Camera camera;
    protected final IntIntMap keys = new IntIntMap();

    public CamController(Camera camera) {
        this.camera = camera;
    }

    @Override
    public boolean keyDown (int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    public void update (float deltaTime) {

        float moveSpeed = Settings.walkSpeed;
        if(keys.containsKey(runShiftKey))       // go faster if SHIFT is held down
            moveSpeed *= Settings.runFactor;

        if (keys.containsKey(forwardKey))
            moveForward(deltaTime * moveSpeed);

        if (keys.containsKey(backwardKey))
            moveForward(-deltaTime * moveSpeed);

        if (keys.containsKey(strafeLeftKey))
            strafe(-deltaTime * Settings.walkSpeed);

        if (keys.containsKey(strafeRightKey))
            strafe(deltaTime * Settings.walkSpeed);

        if (keys.containsKey(turnLeftKey))
            rotateView(deltaTime*Settings.turnSpeed );
        else if (keys.containsKey(turnRightKey))
            rotateView(-deltaTime*Settings.turnSpeed );


        camera.update(true);
    }

    protected final Vector3 fwdHorizontal = new Vector3();

    private void moveForward( float distance ){
        fwdHorizontal.set(camera.direction).y = 0;
        fwdHorizontal.nor();
        fwdHorizontal.scl(distance);
        camera.position.add(fwdHorizontal);
    }

    protected final Vector3 tmp = new Vector3();

    private void strafe( float distance ){
        fwdHorizontal.set(camera.direction).y = 0;
        fwdHorizontal.nor();
        tmp.set(fwdHorizontal).crs(camera.up).nor().scl(distance);
        camera.position.add(tmp);
    }

    private void rotateView(float deltaX) {
        camera.direction.rotate(camera.up, deltaX);
        camera.up.set(Vector3.Y);
    }

}
