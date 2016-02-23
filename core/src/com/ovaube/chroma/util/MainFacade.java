package com.ovaube.chroma.util;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.ovaube.chroma.screens.MenuScreen;
import com.badlogic.gdx.Game;

public class MainFacade
{
    private Game game;

    public MainFacade(Game game)
    {
        this.game = game;
    }

    public void initializeGameSystems()
    {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        Assets.instance.init(new AssetManager());
        ChromaSkin.instance.init();
        ChromaPreferences.instance.load();

        Gdx.input.setCatchBackKey(true);
    }

    public void startGame()
    {
        game.setScreen(new MenuScreen(game, false));
    }
}
