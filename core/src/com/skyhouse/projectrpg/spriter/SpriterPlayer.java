package com.skyhouse.projectrpg.spriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.Player;
import com.brashmonkey.spriter.PlayerTweener;
import com.brashmonkey.spriter.SCMLReader;

/**
 * SpriterActor for Spriter.
 * @author Meranote
 */
public class SpriterPlayer extends PlayerTweener {

	private static SpriteBatch batch;
	private static ShapeRenderer renderer;
	
	private String newanimation = "idle";
	private SpriterDrawer drawer;
	
	/**
	 * Create a new SpriterActor for character, monster, etc. 
	 * @param pathToScml path to .scml file
	 */
	public SpriterPlayer(String pathToScml) {
		super();
		FileHandle handle = Gdx.files.internal(pathToScml);
		SCMLReader reader = new SCMLReader(handle.read());
		Data data = reader.getData();
		setPlayers(new Player(data.getEntity(0)), new Player(data.getEntity(0)));
		SpriterLoader loader = new SpriterLoader(data);
		loader.load(handle.file());
		drawer = new SpriterDrawer(loader, batch , renderer);
		getFirstPlayer().setAnimation("idle");
		getSecondPlayer().setAnimation("idle");
		setWeight(0.0f);
	}
	
	/**
	 * Set new animation to changed to.
	 * @param newanimation animation name
	 */
	public void setNewAnimation(String newanimation) {
		if(this.newanimation.equals(newanimation)) return;
		this.newanimation = newanimation;
		getSecondPlayer().setTime(0);
	}
	
	/**
	 * Update the tweener animation with default speed (3.5f).
	 * @param deltaTime deltatime
	 */
	public void update(float deltaTime) {
		update(deltaTime, 3.5f);
	}
	
	/**
	 * Update the tweener animation with given speed.
	 * @param deltaTime deltatime
	 * @param speed tween speed
	 */
	public void update(float deltaTime, float speed) {
		if(getFirstPlayer().getAnimation().name.equals(newanimation)) {
			if(getWeight() > 0f) setWeight(getWeight() - (speed * deltaTime));
			else if(getWeight() < 0f) setWeight(0f);
		} else {
			getSecondPlayer().setAnimation(newanimation);
			if(getWeight() < 1f) setWeight(getWeight() + (speed * deltaTime));
			else if(getWeight() > 1f) {
				setWeight(0f);
				getFirstPlayer().setAnimation(newanimation);
			}
		}
		update();
	}
	
	/**
	 * Draw spriter.
	 */
	public void draw() {
		drawer.draw(this);
	}
	
	/**
	 * Initial {@link SpriteBatch} and {@link ShapeRenderer} globally before call any {@link SpriterPlayer#draw()} objects.
	 */
	public static void init(SpriteBatch batch, ShapeRenderer renderer) {
		 SpriterPlayer.batch = batch;
		 SpriterPlayer.renderer = renderer;
	}
}
