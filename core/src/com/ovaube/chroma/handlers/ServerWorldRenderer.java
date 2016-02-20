package com.ovaube.chroma.handlers;

import java.util.Map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.ovaube.chroma.GUI.HUD;
import com.ovaube.chroma.objects.Block;
import com.ovaube.chroma.objects.Bullet;
import com.ovaube.chroma.objects.Damager;
import com.ovaube.chroma.objects.Healer;
import com.ovaube.chroma.objects.Player;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PlayerColor;

public class ServerWorldRenderer implements Disposable
{
	private ServerWorldController worldController;
	private SpriteBatch batch;	
	private HUD hud;
	
	private OrthographicCamera camera;
	private OrthographicCamera hudCamera;
	
	private PlayerColor color;
	
	public ServerWorldRenderer(ServerWorldController worldController)
	{
		this.worldController = worldController;
		this.color = worldController.getColor();
		init();
	}
	
	private void init()
	{
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
		hudCamera.update();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
		camera.update();
		
		batch = new SpriteBatch();		
		hud = new HUD(worldController.getPlayers().get(color), worldController, hudCamera);		
	}
	
	public void render(float deltaTime)
	{	
		// Render objects
		batch.setProjectionMatrix(camera.combined);
		
		worldController.getWall().renderAndUpdate(batch, deltaTime);
		for(Map.Entry<PlayerColor, Player> entry : worldController.getPlayers().entrySet())
			entry.getValue().renderAndUpdate(batch, deltaTime);
		for(Block block : worldController.getBlocks())
			block.renderAndUpdate(batch, deltaTime);
		for(Map.Entry<Integer, Bullet> entry : worldController.getBullets().entrySet())
			entry.getValue().renderAndUpdate(batch, deltaTime);
		for(Damager damager : worldController.getDamagers())
			damager.renderAndUpdate(batch, deltaTime);
		for(Healer healer : worldController.getHealers())
			healer.renderAndUpdate(batch, deltaTime);
		if(worldController.getPickupInvictus() != null)
			worldController.getPickupInvictus().renderAndUpdate(batch, deltaTime);
		if(worldController.getPickupWeapon() != null)
			worldController.getPickupWeapon().renderAndUpdate(batch, deltaTime);
		
		// Render gui
		hudCamera.update();
		batch.setProjectionMatrix(hudCamera.combined);
		hud.render(batch, worldController.getTime());
		
		// Follow player
		camera.position.set(
				worldController.getPlayers().get(color).getBody().getPosition().x * Constants.PPM, 
				worldController.getPlayers().get(color).getBody().getPosition().y * Constants.PPM, 
				0f);
		camera.update();
	}
	
	public void resize(int width, int height)
	{
		camera.viewportWidth = (Constants.VIEWPORT_HEIGHT / (float) height) * (float) width;
		camera.update();

		hudCamera.viewportHeight = Constants.VIEWPORT_HEIGHT;
		hudCamera.viewportWidth = (Constants.VIEWPORT_HEIGHT / (float)height) * (float)width;
		hudCamera.position.set(hudCamera.viewportWidth / 2, hudCamera.viewportHeight / 2, 0);
		hudCamera.update();
	}

	@Override
	public void dispose() 
	{
		batch.dispose();
	}
}
