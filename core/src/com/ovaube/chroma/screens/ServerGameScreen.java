package com.ovaube.chroma.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.ovaube.chroma.handlers.ServerWorldController;
import com.ovaube.chroma.handlers.ServerWorldRenderer;
import com.ovaube.chroma.networking.ChromaServer;
import com.ovaube.chroma.util.PlayerColor;

public class ServerGameScreen extends AbstractGameScreen
{	
	private ChromaServer server;
	
	private ServerWorldController worldController;
	private ServerWorldRenderer worldRenderer;
	
	private TiledMap map;
	private PlayerColor color;
	
	float time;
	
	public ServerGameScreen(Game game, TiledMap map, ChromaServer server, PlayerColor color, float time)
	{
		super(game);
		this.map = map;
		this.server = server;
		this.color = color;
		this.time = time;
	}
	
	@Override
	public void render (float deltaTime)
	{			
		worldController.update(deltaTime);
		// Set color and clear screen
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		worldRenderer.render(deltaTime);
	}

	@Override
	public void resize(int width, int height) 
	{
		worldRenderer.resize(width, height);		
	}

	@Override
	public void show() 
	{
		worldController = new ServerWorldController(game, map, server, color, time);
		worldRenderer = new ServerWorldRenderer(worldController);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void hide() 
	{
		worldController.getPlayingMusic().pause();
		worldRenderer.dispose();
		Gdx.input.setCatchBackKey(false);
	}
	
	@Override
	public void resume()
	{
		super.resume();
		worldController.getPlayingMusic().play();
	}
	
	public void dispose()
	{
		super.dispose();
		server.getKryoServer().stop();
		server.getKryoServer().close();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
}
