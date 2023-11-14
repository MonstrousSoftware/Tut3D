package com.monstrous.tut3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Assets implements Disposable {

    public class AssetSounds {

        // constants for sounds in game
        public final Sound COIN;
        public final Sound FALL;
        public final Sound GAME_OVER;
        public final Sound HIT;
        public final Sound JUMP;
        public final Sound GAME_COMPLETED;
        public final Sound UPGRADE;
        public final Sound GUN_SHOT;

        public AssetSounds() {
            COIN = assets.get("sound/coin1.ogg");
            FALL = assets.get ("sound/fall1.ogg");
            GAME_OVER = assets.get ("sound/gameover1.ogg");
            HIT  = assets.get("sound/hit1.ogg");
            JUMP  = assets.get("sound/jump1.ogg");
            GAME_COMPLETED = assets.get("sound/secret1.ogg");
            UPGRADE = assets.get ("sound/upgrade1.ogg");
            GUN_SHOT = assets.get ("sound/9mm-pistol-shoot-short-reverb-7152.mp3");
        }
    }

    public AssetSounds sounds;
    public Skin skin;
    public BitmapFont uiFont;
    public SceneAsset sceneAsset;
    public Texture scopeImage;

    private AssetManager assets;

    public Assets() {
        Gdx.app.log("Assets constructor", "");
        assets = new AssetManager();

        assets.load("ui/uiskin.json", Skin.class);

        assets.load("font/Amble-Regular-26.fnt", BitmapFont.class);

        assets.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assets.load( Settings.GLTF_FILE, SceneAsset.class);

        assets.load("sound/coin1.ogg", Sound.class);
        assets.load("sound/fall1.ogg", Sound.class);
        assets.load("sound/gameover1.ogg", Sound.class);
        assets.load("sound/hit1.ogg", Sound.class);
        assets.load("sound/jump1.ogg", Sound.class);
        assets.load("sound/secret1.ogg", Sound.class);
        assets.load("sound/upgrade1.ogg", Sound.class);
        assets.load("sound/9mm-pistol-shoot-short-reverb-7152.mp3", Sound.class);


        assets.load("images/scope.png", Texture.class);
    }

    public void finishLoading() {
        assets.finishLoading();
        initConstants();
    }

    private void initConstants() {
        sounds = new AssetSounds();
        skin = assets.get("ui/uiskin.json");
        uiFont = assets.get("font/Amble-Regular-26.fnt");
        sceneAsset = assets.get(Settings.GLTF_FILE);
        scopeImage = assets.get("images/scope.png");
    }

    public <T> T get(String name ) {
        return assets.get(name);
    }

    @Override
    public void dispose() {
        Gdx.app.log("Assets dispose()", "");
        assets.dispose();
        assets = null;
    }
}
