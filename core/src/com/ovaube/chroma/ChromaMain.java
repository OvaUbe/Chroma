package com.ovaube.chroma;

import com.badlogic.gdx.Game;
import com.ovaube.chroma.util.MainFacade;

public class ChromaMain extends Game
{
	@Override
	public void create() 
	{
		MainFacade facade = new MainFacade(this);

		facade.initializeGameSystems();
		facade.startGame();
	}
}
