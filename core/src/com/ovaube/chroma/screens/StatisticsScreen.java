package com.ovaube.chroma.screens;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.ovaube.chroma.phonies.PhonyPlayer;
import com.ovaube.chroma.objects.Player;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.ChromaSkin;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PlayerColor;

public class StatisticsScreen extends AbstractGameScreen
{
	private Stage stage;
	
	private LabelStyle grayLabelStyle = ChromaSkin.instance.grayLabelStyle;
	private LabelStyle redLabelStyle = ChromaSkin.instance.redLabelStyle;
	private LabelStyle greenLabelStyle = ChromaSkin.instance.greenLabelStyle;
	
	private HashMap<PlayerColor, Player> players = new HashMap<PlayerColor, Player>();
	private HashMap<PlayerColor, PhonyPlayer> flyweightPlayers = new HashMap<PlayerColor, PhonyPlayer>();
	
	private boolean isFlyweight;
	
	public StatisticsScreen(Game game, HashMap<PlayerColor, Player> players, boolean dummy)
	{
		super(game);
		this.players.putAll(players);
		isFlyweight = false;
	}
	
	public StatisticsScreen(Game game, HashMap<PlayerColor, PhonyPlayer> flyweightPlayers)
	{
		super(game);
		this.flyweightPlayers.putAll(flyweightPlayers);
		isFlyweight = true;
	}
	
	@Override
	public void show()
	{
		stage = new Stage(
				new StretchViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
		
		Gdx.input.setInputProcessor(stage);
			
		Image bg = new Image(Assets.instance.assetTextures.bg);
		bg.setX(-20f);
		bg.setY(-20f);
		stage.addActor(bg);
		
		buildStage();
		
		Gdx.input.setCatchBackKey(true);
		
		Assets.instance.assetSFX.endGame.play(ChromaPreferences.instance.volumeSound);
	}
	
	@Override
	public void render(float deltaTime)
	{
		// Set color and clear screen
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act(deltaTime);
		stage.draw();
		
		if(Gdx.app.getType() == ApplicationType.Android)
			handleBackButton();
	}
	
	@Override
	public void resize(int width, int height)
	{
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void hide()
	{
		stage.dispose();
		
		Gdx.input.setCatchBackKey(false);
	}
	
	@Override
	public void pause() {}
	
	@Override
	public void resume() {}
	
	private void buildStage()
	{
		Table table = new Table();
		table.setFillParent(true);
		table.setVisible(true);
		
		stage.addActor(table);
		
		Label labelTitleNickname = new Label("NICK", grayLabelStyle);
		Label labelTitleColor = new Label("COLOR", greenLabelStyle);
		Label labelTitleKills = new Label("K", grayLabelStyle);
		Label labelTitleDeaths = new Label("D", grayLabelStyle);
		Label labelTitleKD = new Label("K/D", redLabelStyle);
		
		table.add(labelTitleNickname).height(150).width(500);
		table.add(labelTitleColor).height(150).width(500);
		table.add(labelTitleKills).height(150).width(350);
		table.add(labelTitleDeaths).height(150).width(350);
		table.add(labelTitleKD).height(150).width(350);
		table.row();
		table.add(new Label("", grayLabelStyle)).height(150).width(500);
		table.add(new Label("", grayLabelStyle)).height(150).width(350);
		table.add(new Label("", grayLabelStyle)).height(150).width(350);
		table.add(new Label("", grayLabelStyle)).height(150).width(350);
		table.row();
		
		if(isFlyweight)
		{
			for(Map.Entry<PlayerColor, PhonyPlayer> entry : flyweightPlayers.entrySet())
			{
				PhonyPlayer player = entry.getValue();
				PlayerColor color = entry.getKey();
				
				Label labelNickname = new Label(player.getNickname(), grayLabelStyle);
				Label labelColor = new Label(color.toString(), greenLabelStyle);
				Label labelKills = new Label(player.getKills().toString(), grayLabelStyle);
				Label labelDeaths = new Label(player.getDeaths().toString(), grayLabelStyle);
				Float KD = (float)player.getKills() / (float)player.getDeaths();
				Label labelKD = new Label(KD.toString(), redLabelStyle);
				
				table.add(labelNickname).height(150).width(500);
				table.add(labelColor).height(150).width(500);
				table.add(labelKills).height(150).width(350);
				table.add(labelDeaths).height(150).width(350);
				table.add(labelKD).height(150).width(350);
				table.row();
			}
		}
		else
		{
			for(Map.Entry<PlayerColor, Player> entry : players.entrySet())
			{
				Player player = entry.getValue();
				PlayerColor color = entry.getKey();
				
				Label labelNickname = new Label(player.getNickname(), grayLabelStyle);
				Label labelColor = new Label(color.toString(), greenLabelStyle);
				Label labelKills = new Label(player.getKills().toString(), grayLabelStyle);
				Label labelDeaths = new Label(player.getUnsafeDeaths().toString(), grayLabelStyle);
				Float KD = (float)player.getKills() / (float)player.getDeaths();
				Label labelKD = new Label(String.format("%.2f", KD), redLabelStyle);
				
				table.add(labelNickname).height(150).width(500);
				table.add(labelColor).height(150).width(500);
				table.add(labelKills).height(150).width(350);
				table.add(labelDeaths).height(150).width(350);
				table.add(labelKD).height(150).width(350);
				table.row();
			}
		}
	}
	
	private void handleBackButton()
	{
		if(Gdx.input.isKeyJustPressed(Keys.BACK))
			game.setScreen(new MenuScreen(game, false));
	}
}
