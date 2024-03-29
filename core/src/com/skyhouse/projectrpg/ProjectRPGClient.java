package com.skyhouse.projectrpg;

import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;
import com.kotcrab.vis.ui.VisUI;
import com.skyhouse.projectrpg.input.InputMapper.keyboard;
import com.skyhouse.projectrpg.manager.GameManager;
import com.skyhouse.projectrpg.manager.InputManager;
import com.skyhouse.projectrpg.manager.SceneManager;
import com.skyhouse.projectrpg.map.Map;
import com.skyhouse.projectrpg.net.listeners.DisconnectListener;
import com.skyhouse.projectrpg.net.listeners.GameDataListener;
import com.skyhouse.projectrpg.net.listeners.LoginListener;
import com.skyhouse.projectrpg.net.listeners.UpdateListener;
import com.skyhouse.projectrpg.net.packets.DisconnectRequest;
import com.skyhouse.projectrpg.net.packets.LoginRequest;
import com.skyhouse.projectrpg.net.utils.NetworkUtils;
import com.skyhouse.projectrpg.scene.GameScene;
import com.skyhouse.projectrpg.scene.HomeScene;
import com.skyhouse.projectrpg.scene.LoadingScene;
import com.skyhouse.projectrpg.scene.MenuScene;
import com.skyhouse.projectrpg.scene.Scene;
import com.skyhouse.projectrpg.scene.StartScene;
import com.skyhouse.projectrpg.spriter.SpriterPlayer;
import com.skyhouse.projectrpg.utils.asset.loader.MapLoader;
import com.skyhouse.projectrpg.utils.font.ThaiLanguage;

/**
 * Client class of ProjectRPG.
 * @author Meranote
 */
public class ProjectRPGClient extends ApplicationAdapter {
	
	private AssetManager assetmanager;
	private InputManager inputmanager;
	private SceneManager scenemanager;
	private GameManager gamemanager;
	private Client net;
	
	@Override
	public void create()  {
		Gdx.app.setLogLevel(Logger.DEBUG);
		Log.set(Log.LEVEL_NONE);
		GLProfiler.enable();
		
		inputmanager = new InputManager();
		ProjectRPG.client.inputmanager = inputmanager;
		
		initialAssets();
		initialGraphics();
		initialNetwork();
		initialGame();
		startGame();
	}
	
	private void initialAssets() {
			assetmanager = new AssetManager();
			ProjectRPG.client.assetmanager = assetmanager;
			
			InternalFileHandleResolver resolver = new InternalFileHandleResolver();
			assetmanager.setLoader(Map.class, new MapLoader(resolver));
			
			// Build Distance field shader
			ShaderProgram fontshader = new ShaderProgram(Gdx.files.internal("shader/distancefieldfont.vert"), Gdx.files.internal("shader/distancefieldfont.frag"));
			if (!fontshader.isCompiled()) {
			    throw new GdxRuntimeException("compilation failed:\n" + fontshader.getLog());
			}
			ProjectRPG.client.graphic.font.shader = fontshader;
			
			assetmanager.load("texture/artwork/startbackground.png", Texture.class);
			
			assetmanager.finishLoading();
		}

	private void initialGraphics() {
		SpriteBatch batch = new SpriteBatch();
		ProjectRPG.client.graphic.batch = batch;
		
		ShapeRenderer renderer = new ShapeRenderer();
		ProjectRPG.client.graphic.renderer = renderer;
		
		Scene.initScene(batch, renderer);
		SpriterPlayer.init(batch, renderer);
	}

	private void initialNetwork() {
		net = new Client();
		ProjectRPG.client.network.net = net;
		Kryo kryo = net.getKryo();
		NetworkUtils.registerKryoClass(kryo);
	}
	
	private void initialGame() {
		scenemanager = new SceneManager();
		ProjectRPG.client.scenemanager = scenemanager;
		scenemanager.addScene(StartScene.class);
		
		scenemanager.setUseScene(StartScene.class, true);
		
		gamemanager = new GameManager();
		ProjectRPG.client.gamemanager = gamemanager;		
		ProjectRPG.client.mapmanager = gamemanager.getMapManager();
    	ProjectRPG.client.entitymanager = gamemanager.getEntityManager();
	}

	private void startGame() {
		net.start();
		try {
			net.connect(5000, "server.projectrpg.dev", 54555, 54556);
			net.addListener(new LoginListener.Client());
			net.addListener(new GameDataListener.Client());
			net.addListener(new UpdateListener.Client());
			net.addListener(new DisconnectListener.Client());
		} catch (IOException e) {
			Gdx.app.error(ProjectRPG.TITLE, "Can't connect to server");
			Gdx.app.exit();
		}
		
	}
	
	@Override
	public void resize (int width, int height) {
		scenemanager.resize(width, height);
	}

	@Override
	public void render () {
		// Clear buffer
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// Update Fixed time
		ProjectRPG.updateFixedTime(deltaTime);
		for(int i = 0; i < ProjectRPG.getUpdateFixedCount(); i++) {
			float step = ProjectRPG.getStep();
			gamemanager.updateFixed(step);
			scenemanager.updateFixed(step);
		}
		// Update
		assetmanager.update();
		gamemanager.update(deltaTime);
		scenemanager.update(deltaTime);
		inputmanager.update(deltaTime);
		
		// Render
		scenemanager.draw(deltaTime);
		
		GLProfiler.reset();
	}
	
	@Override
	public void dispose () {
		net.close();
		net.stop();
		
		scenemanager.dispose();
		VisUI.dispose();
		
		try {
			net.dispose();
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
}
