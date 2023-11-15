package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

// overlay a full screen scope graphic (2d texture)

public class ScopeOverlay implements Disposable {
    private final SpriteBatch batch;
    private final Texture scopeTexture;
    private float recoilTimer;              // > 0 for recoil effect

    public ScopeOverlay() {
        this.batch = new SpriteBatch();
        scopeTexture = Main.assets.scopeImage;
        recoilTimer = 0;
    }

    public void startRecoilEffect(){
        recoilTimer = 0.5f; // start timer for 0.5 s
    }

    public void render(float delta) {
        float effect = 0;

        recoilTimer -= delta;
        if(recoilTimer > 0)     // do recoil effect
            effect = recoilTimer * 50f;     // scale image when firing

        batch.begin();
        batch.draw(scopeTexture, -effect, -effect, Gdx.graphics.getWidth()+2* effect, Gdx.graphics.getHeight()+2* effect);
        batch.end();
    }

    public void resize(int width, int height){
        batch.getProjectionMatrix().setToOrtho2D(0,0, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
