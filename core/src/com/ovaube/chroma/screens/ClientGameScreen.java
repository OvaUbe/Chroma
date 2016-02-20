package com.ovaube.chroma.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.ovaube.chroma.handlers.ClientUpdater;
import com.ovaube.chroma.networking.ChromaClient;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.PlayerColor;

public class ClientGameScreen extends AbstractGameScreen
{
	private ChromaClient client;
	
	private ClientUpdater updater;
	private PlayerColor color;
	
	private Array<Music> music = Assets.instance.assetMusic.musicList;
	private Music currentMusic;
	
	private TiledMap map;
	
	public ClientGameScreen(Game game, PlayerColor color, ChromaClient client, TiledMap map)
	{
		super(game);
		this.client = client;
		this.color = color;
		this.map = map;
		
		playMusic();
	}
	
	@Override
	public void render (float deltaTime)
	{				
		if(client.isDisconnected())
		{
			currentMusic.stop();
			game.setScreen(new MenuScreen(game, true));
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.BACK))
		{
			client.getKryoClient().stop();
			client.getKryoClient().close();
			currentMusic.stop();
			game.setScreen(new MenuScreen(game, false));
		}
		
		// Set color and clear screen
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		playMusic();

		updater.update(deltaTime);
	}

	@Override
	public void resize(int width, int height) 
	{
		updater.resize(width, height);		
	}

	@Override
	public void show() 
	{
		updater = new ClientUpdater(game, color, client, map);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void hide() 
	{
		currentMusic.pause();
		updater.dispose();
		Gdx.input.setCatchBackKey(false);
	}
	
	@Override
	public void resume()
	{
		super.resume();
		currentMusic.play();
	}
	
	private void playMusic()
	{
		if(currentMusic == null || !currentMusic.isPlaying())
		{
			currentMusic = music.get(MathUtils.random(music.size - 1));
			currentMusic.play();
		}
	}
	
	public void dispose()
	{
		super.dispose();
		client.getKryoClient().stop();
		client.getKryoClient().close();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
}
