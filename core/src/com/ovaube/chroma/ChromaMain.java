package com.ovaube.chroma;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.ovaube.chroma.screens.MenuScreen;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaSkin;

public class ChromaMain extends Game
{
	@Override
	public void create() 
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		
		Assets.instance.init(new AssetManager());
		ChromaSkin.instance.init();
		
		setScreen(new MenuScreen(this, false));
	}
}
