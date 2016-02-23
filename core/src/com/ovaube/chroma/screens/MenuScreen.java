package com.ovaube.chroma.screens;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.ovaube.chroma.networking.ChromaClient;
import com.ovaube.chroma.networking.ChromaServer;
import com.ovaube.chroma.networking.PacketDisconnect;
import com.ovaube.chroma.networking.PacketStartGame;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.ChromaSkin;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PlayerColor;

public class MenuScreen extends AbstractGameScreen
{	
	private ChromaClient client;
	private ChromaServer server;
	
	private Stage stage;
	
	private Table tableMain;
	private Table tableSettings;
	private Table tableClientServer;
	private Table tableServer;
	private Table tableChooseMap;
	private Table tableClientConnection;
	private Table tableClient;
	private Table tableError;
	
	private HashMap<PlayerColor, Table> playerTables = new HashMap<PlayerColor, Table>();
	
	private TextButton buttonPlay;
	private TextButton buttonSettings;
	private TextButton buttonExit;
	private TextButton buttonSave;
	private TextButton buttonCancelSettings;
	private TextButton buttonCancelClientServer;
	private TextButton buttonHost;
	private TextButton buttonJoin;
	private TextButton buttonCancelClientConnection;
	private TextButton buttonError;
	private TextButton buttonTime;
	private TextButton buttonStart;
	private TextButton buttonDisconnectServer;
	private TextButton buttonDisconnectClient;
	
	private TextField textFieldNickname;
	
	private Slider sliderMusic;
	private Slider sliderSound;
	
	private Label labelError;
	private Label labelClientState;
	
	private SliderStyle sliderStyle = ChromaSkin.instance.sliderStyle;
	private TextButtonStyle redButtonStyle = ChromaSkin.instance.redButtonStyle;
	private TextButtonStyle whiteButtonStyle = ChromaSkin.instance.whiteButtonStyle;
	private TextButtonStyle blueButtonStyle = ChromaSkin.instance.blueButtonStyle;
	private LabelStyle grayLabelStyle = ChromaSkin.instance.grayLabelStyle;
	private LabelStyle redLabelStyle = ChromaSkin.instance.redLabelStyle;
	private LabelStyle greenLabelStyle = ChromaSkin.instance.greenLabelStyle;
	private TextFieldStyle textFieldStyle = ChromaSkin.instance.textFieldStyle;
	
	private Music music = Assets.instance.assetMusic.menuMusic;
	
	private LinkedHashMap<String, TiledMap> maps = Assets.instance.assetMaps.maps;
	private TiledMap chosenMap;
	
	private float buttonHeight = 150f;
	private float labelHeight = 150f;
	private float textFieldHeight = 150f;
	private float chosenTime = 900f;
	
	private boolean debug = false;
	private boolean isErrorOccured;
	
	private PlayerColor currentColor;
	private HashSet<PlayerColor> playersConnected = new HashSet<PlayerColor>();
	
	private SpriteBatch batch;
	
	private ParticleEffect effectMenu;
	
	public MenuScreen(Game game, boolean isErrorOccured)
	{
		super(game);
		this.isErrorOccured = isErrorOccured;
	}
	
	@Override
	public void show()
	{
		stage = new Stage(new StretchViewport(Constants.VIEWPORT_WIDTH,
											  Constants.VIEWPORT_HEIGHT));
		if(debug)
			stage.setDebugAll(true);
		
		Gdx.input.setInputProcessor(stage);

		changeVolumeMusic(ChromaPreferences.instance.volumeMusic);
		changeVolumeSound(ChromaPreferences.instance.volumeSound);
			
		buildStage();
		
		music.setLooping(true);
		music.play();

		// Set menu effect
		batch = new SpriteBatch();
		
		effectMenu = new ParticleEffect(Assets.instance.assetPFX.menu);
		effectMenu.setPosition(Constants.VIEWPORT_WIDTH / 6, Constants.VIEWPORT_HEIGHT / 8);
		effectMenu.start();
	}
	
	@Override
	public void render(float deltaTime)
	{
		// Set color and clear screen
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act(deltaTime);
		stage.draw();
		
		batch.begin();
		
		effectMenu.update(deltaTime);
		effectMenu.draw(batch);
		if(effectMenu.isComplete())
			effectMenu.reset();
		
		batch.end();
		
		if(Gdx.app.getType() == ApplicationType.Android)
			handleBackButton();

		handleServerNetworking();
		
		handleClientNetworking();
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
		music.pause();
		
		Gdx.input.setCatchBackKey(false);
	}
	
	@Override
	public void pause() {}
	
	@Override
	public void resume()
	{
		music.play();
	}
	
	private void buildStage()
	{
		stage.clear();
		
		Image bg = new Image(Assets.instance.assetTextures.bg);
		bg.setX(-20f);
		bg.setY(-20f);
		stage.addActor(bg);
		
		// Build tables
		buildMain();
		buildSettings();
		buildClientServer();
		buildChooseMap();
		
		buildErrorTable();
	}
	
	private void buildMain()
	{
		tableMain = new Table();
		tableMain.setFillParent(true);
		tableMain.setVisible(!isErrorOccured);
		
		stage.addActor(tableMain);
		
		buttonPlay = new TextButton("PLAY", whiteButtonStyle);
		buttonPlay.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onPlayClicked();
				}
			}		
		);
		
		buttonSettings = new TextButton("SETTINGS", whiteButtonStyle);
		buttonSettings.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onOptionsClicked();
				}
			}		
		);
		
		buttonExit = new TextButton("EXIT", redButtonStyle);
		buttonExit.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onExitClicked();
				}
			}		
		);
		
		tableMain.add(buttonPlay).height(buttonHeight);
		tableMain.row();
		tableMain.add(buttonSettings).height(buttonHeight);
		tableMain.row();
		tableMain.add(buttonExit).height(buttonHeight);
	}
	
	private void buildSettings()
	{
		tableSettings = new Table();
		tableSettings.setFillParent(true);
		tableSettings.setVisible(false);
		
		stage.addActor(tableSettings);
		
		sliderMusic = new Slider(0.0f, 1.0f, 0.1f, false, sliderStyle);
		sliderMusic.setValue(ChromaPreferences.instance.volumeMusic);
		sliderMusic.addListener(
			new ChangeListener() 
			{
		        @Override
		        public void changed(ChangeEvent event, Actor actor) 
		        {
		        	changeVolumeMusic(((Slider)actor).getValue());
		        }
		    }
		);
		sliderSound = new Slider(0.0f, 1.0f, 0.1f, false, sliderStyle);
		sliderSound.setValue(ChromaPreferences.instance.volumeSound);
		sliderSound.addListener(
				new ChangeListener() 
				{
			        @Override
			        public void changed(ChangeEvent event, Actor actor) 
			        {
			        	changeVolumeSound(((Slider)actor).getValue());
			        }
			    }
			);
		
		Label labelMusic = new Label("Music: ", grayLabelStyle);
		Label labelSound = new Label("Sound: ", grayLabelStyle);
		Label labelNickname = new Label("Nickname: ", grayLabelStyle);
				
		buttonSave = new TextButton("SAVE", whiteButtonStyle);
		buttonSave.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onSaveClicked();
				}
			}		
		);
		
		buttonCancelSettings = new TextButton("CANCEL", redButtonStyle);
		buttonCancelSettings.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onCancelSettingsClicked();
				}
			}		
		);
		
		textFieldNickname = new TextField(ChromaPreferences.instance.nickname, textFieldStyle);
		
		tableSettings.add(labelNickname).height(labelHeight);
		tableSettings.add(textFieldNickname).height(textFieldHeight).width(1000);
		tableSettings.row();
		tableSettings.add(labelMusic).height(labelHeight);
		tableSettings.add(sliderMusic).height(100).width(1000);
		tableSettings.row();
		tableSettings.add(labelSound).height(labelHeight);
		tableSettings.add(sliderSound).height(100).width(1000);
		tableSettings.row();
		tableSettings.add(buttonSave).height(buttonHeight);
		tableSettings.add(buttonCancelSettings).height(buttonHeight);
		tableSettings.row();
		tableSettings.add(new Label(" ", redLabelStyle));
		tableSettings.row();
		tableSettings.add(new Label(" ", redLabelStyle));
		tableSettings.row();
		tableSettings.add(new Label(" ", redLabelStyle));
		tableSettings.row();
		tableSettings.add(new Label(" ", redLabelStyle));
		tableSettings.row();
		tableSettings.add(new Label(" ", redLabelStyle));
		tableSettings.row();
	}
	
	private void buildClientServer()
	{
		tableClientServer = new Table();
		tableClientServer.setFillParent(true);
		tableClientServer.setVisible(false);
		
		stage.addActor(tableClientServer);
		
		buttonHost = new TextButton("HOST", whiteButtonStyle);
		buttonHost.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onHostClicked();
				}
			}		
		);
		
		buttonJoin = new TextButton("JOIN", whiteButtonStyle);
		buttonJoin.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onJoinClicked();
				}
			}		
		);
		
		buttonCancelClientServer = new TextButton("CANCEL", redButtonStyle);
		buttonCancelClientServer.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onCancelClientServerClicked();
				}
			}		
		);
		
		tableClientServer.add(buttonHost).height(buttonHeight);
		tableClientServer.row();
		tableClientServer.add(buttonJoin).height(buttonHeight);
		tableClientServer.row();
		tableClientServer.add(buttonCancelClientServer).height(buttonHeight);
	}
	
	private void buildChooseMap()
	{
		tableChooseMap = new Table();
		tableChooseMap.setFillParent(true);
		tableChooseMap.setVisible(false);
		
		stage.addActor(tableChooseMap);
		
		Label label = new Label("CHOOSE MAP:", grayLabelStyle);
		
		TextButton buttonCancelChooseMap = new TextButton("CANCEL", redButtonStyle);
		buttonCancelChooseMap.addListener(
			new ChangeListener()
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onCancelChooseMapClicked();
				}
			}
		);
		
		tableChooseMap.add(label).height(labelHeight);
		tableChooseMap.row();
		// Create list of maps		
		Table scrollTable = new Table();
		
		for(Map.Entry<String, TiledMap> entry : maps.entrySet())
		{
			String name = entry.getKey();
			TiledMap map = entry.getValue();
			
			TextButton buttonMap = new TextButton(name, whiteButtonStyle);
			buttonMap.setUserObject(map);
			buttonMap.addListener(
				new ChangeListener()
				{
					@Override
					public void changed(ChangeEvent event, Actor actor)
					{
						chosenMap = (TiledMap)((TextButton)actor).getUserObject();
						tableChooseMap.setVisible(false);
						
						try
						{
							server = new ChromaServer();
						}
						catch(IOException exc)
						{
							labelError.setText("Couldn't start \n server");
							System.out.println(exc.getMessage());
							tableError.setVisible(true);
							
							if(server != null)
								server.getKryoServer().stop();
							server = null;
						}
						if(!tableError.isVisible())
						{
							int randomNumber = MathUtils.random(2);
							currentColor = PlayerColor.values()[randomNumber];
							
							// Set free colors
							Array<PlayerColor> freeColors = new Array<PlayerColor>(PlayerColor.values());
							freeColors.removeValue(PlayerColor.values()[randomNumber], true);
							server.setFreeColors(freeColors);
							
							// Set map name from button text
							server.setMapName(((TextButton)actor).getText().toString());
							
							buildServer();							
							tableServer.setVisible(true);
						}
					}
				}
			);
			
			scrollTable.add(buttonMap).height(150).width(1000);
			scrollTable.row();
		}
		
		ScrollPane scrollPane = new ScrollPane(scrollTable);
		
		tableChooseMap.add(scrollPane).fillX().expand();
		tableChooseMap.row();
		tableChooseMap.add(buttonCancelChooseMap).height(buttonHeight);
	}
	
	private void buildClientConnection()
	{
		tableClientConnection = new Table();
		tableClientConnection.setFillParent(true);
		tableClientConnection.setVisible(false);
		
		stage.addActor(tableClientConnection);
		
		Label label = new Label("LocalServers:", grayLabelStyle);
		
		Table scrollTable = new Table();
		for(InetAddress address : client.getLocalServers())
		{
			TextButton buttonServer = new TextButton(address.getHostName(), whiteButtonStyle);
			buttonServer.setUserObject(address);
			buttonServer.addListener(
				new ChangeListener() 
				{
					@Override
					public void changed(ChangeEvent event, Actor actor)
					{
						onConnectClicked((InetAddress)((TextButton)actor).getUserObject());
					}
				}		
			);
			
			scrollTable.add(buttonServer);
			scrollTable.row();
		}
		
		buttonCancelClientConnection = new TextButton("CANCEL", redButtonStyle);
		buttonCancelClientConnection.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onCancelClientConnectionClicked();
				}
			}		
		);
		
		ScrollPane scrollPane = new ScrollPane(scrollTable);
		
		tableClientConnection.add(label).height(labelHeight);
		tableClientConnection.row();
		tableClientConnection.add(scrollPane).fillX().expand();
		tableClientConnection.row();
		tableClientConnection.add(buttonCancelClientConnection).height(buttonHeight);
	}	
	
	private void buildServer()
	{
		tableServer = new Table();
		tableServer.setFillParent(true);
		tableServer.setVisible(false);
		
		stage.addActor(tableServer);
		
		Label labelServer = new Label("SERVER", grayLabelStyle);
		
		buttonStart = new TextButton("START!", blueButtonStyle);
		buttonStart.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{	
					onStartClicked();
				}
			}		
		);
		
		buttonDisconnectServer = new TextButton("DISCONNECT", redButtonStyle);
		buttonDisconnectServer.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{	
					onDisconnectServerClicked();
				}
			}		
		);
		
		tableServer.add(labelServer).height(labelHeight);
		tableServer.row();
		
		Table tableHost = new Table();
		
		Label labelHostNickname = new Label(ChromaPreferences.instance.nickname, greenLabelStyle);
		Label labelHostColor = new Label(currentColor.name(), grayLabelStyle);
		Label labelHostState = new Label("HOST", greenLabelStyle);
		
		tableHost.add(labelHostNickname).height(labelHeight).width(900);
		tableHost.add(labelHostColor).height(labelHeight).width(700);
		tableHost.add(labelHostState).height(labelHeight).width(700);
		
		tableServer.add(tableHost);
		tableServer.row();
		
		for(PlayerColor color : server.getFreeColors())
		{
			Table table = new Table();
			table.setVisible(false);
			
			Label labelNickname = new Label("", greenLabelStyle);
			Label labelColor = new Label(color.name(), grayLabelStyle);
			TextButton buttonKick = new TextButton("KICK", redButtonStyle);
			buttonKick.setUserObject(color);
			buttonKick.addListener(
				new ChangeListener() 
				{
					@Override
					public void changed(ChangeEvent event, Actor actor)
					{
						// Send disconnect packet to connection mapped with this color
						PlayerColor color = (PlayerColor)((TextButton)actor).getUserObject();
						int id = server.getIdsConnected().get(color);
						server.getKryoServer().sendToTCP(id, new PacketDisconnect());
						
						playerTables.get(color).setVisible(false);
						playersConnected.remove(color);
					}
				}		
			);
			
			table.add(labelNickname).height(labelHeight).width(900);
			table.add(labelColor).height(labelHeight).width(700);
			table.add(buttonKick).height(buttonHeight).width(700);
			
			playerTables.put(color, table);
			
			tableServer.add(table);
			tableServer.row();
		}
		
		Label labelTime = new Label("ROUND TIME:", grayLabelStyle);
		buttonTime = new TextButton(String.format("%.0f minutes", (chosenTime / 60)), whiteButtonStyle);
		buttonTime.addListener(
				new ChangeListener() 
				{
					@Override
					public void changed(ChangeEvent event, Actor actor)
					{
						if(chosenTime < 2400f)
							chosenTime += 300f;
						else
							chosenTime = 300f;
						((TextButton)actor).setText(String.format("%.0f minutes", (chosenTime / 60)));
					}
				}		
			);

		tableServer.add(labelTime).height(labelHeight);
		tableServer.row();
		tableServer.add(buttonTime).height(buttonHeight);
		tableServer.row();
		tableServer.add(buttonStart).height(buttonHeight);
		tableServer.row();
		tableServer.add(buttonDisconnectServer).height(buttonHeight);
	}
	
	private void buildClient()
	{
		tableClient = new Table();
		tableClient.setFillParent(true);
		tableClient.setVisible(false);
		
		stage.addActor(tableClient);

		Label labelClient = new Label("CLIENT", grayLabelStyle);
		labelClientState = new Label("WAITING...", redLabelStyle);
		
		buttonDisconnectClient = new TextButton("DISCONNECT", redButtonStyle);
		buttonDisconnectClient.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{	
					onDisconnectClientClicked();
				}
			}		
		);
		
		tableClient.add(labelClient).height(labelHeight);
		tableClient.row();
		tableClient.add(labelClientState).height(labelHeight).width(700);
		tableClient.row();
		tableClient.add(buttonDisconnectClient).height(buttonHeight);
		
	}
	
	private void buildErrorTable()
	{
		tableError = new Table();
		tableError.setFillParent(true);
		tableError.setVisible(isErrorOccured);
		
		stage.addActor(tableError);
		
		labelError = new Label("You were disconnected",  redLabelStyle);
		
		buttonError = new TextButton("OK", whiteButtonStyle);
		buttonError.addListener(
			new ChangeListener() 
			{
				@Override
				public void changed(ChangeEvent event, Actor actor)
				{
					onOkErrorClicked();
				}
			}		
		);
		
		tableError.add(labelError).height(labelHeight);
		tableError.row();
		tableError.add(buttonError).height(buttonHeight);
	}
	
	private void handleBackButton()
	{
		if(Gdx.input.isKeyJustPressed(Keys.BACK))
		{
			if(tableMain.isVisible())
				onExitClicked();
			if(tableSettings.isVisible())
				onCancelSettingsClicked();
			if(tableClientServer.isVisible())
				onCancelClientServerClicked();
			if(tableChooseMap.isVisible())
				onCancelChooseMapClicked();
			if(tableError != null && tableError.isVisible())
				onOkErrorClicked();
			if(tableClientConnection != null)
				onCancelClientConnectionClicked();
			if(tableServer != null)
				onDisconnectServerClicked();
			if(tableClient != null)
				onDisconnectClientClicked();
		}
	}
	
	private void handleServerNetworking()
	{
		if(tableServer != null)
		{
			HashSet<PlayerColor> set = new HashSet<PlayerColor>(server.getPlayersConnected());			
			// Add players if new
			for(PlayerColor color : set)
			{
				if(!playersConnected.contains(color) && server.getPlayerInfos().containsKey(color))	
				{
					Table table = playerTables.get(color);
					// Nickname label
					((Label)table.getChildren().first()).setText(server.getPlayerInfos().get(color));
					table.setVisible(true);
					
					playersConnected.add(color);
				}
			}
			// Remove players if someone disconnected
			for(PlayerColor color : playersConnected)
			{
				if(!set.contains(color))	
				{
					Table table = playerTables.get(color);
					table.setVisible(false);
					
					playersConnected.remove(color);
				}
			}
		}
	}
	
	private void handleClientNetworking()
	{
		if(tableClient != null)
		{
			if(client.isServerInfoReceived())
			{
				if(!client.isDisconnected())
					labelClientState.setText("Connected!");
				else
					labelClientState.setText("You were disconnected.");
				// Play!
				if(client.isStartPacketReceived())
				{
					currentColor = PlayerColor.valueOf(client.getServerInfo().color);
					chosenMap = Assets.instance.assetMaps.maps.get(client.getServerInfo().mapName);
					game.setScreen(new ClientGameScreen(game, currentColor, client, chosenMap));
				}
					
			}
			else if(client.isServerFull())
				labelClientState.setText("Server is full!");
		}
	}
	
	private void onPlayClicked()
	{
		tableMain.setVisible(false);
		tableClientServer.setVisible(true);
	}
	
	private void onOptionsClicked()
	{
		tableMain.setVisible(false);
		tableSettings.setVisible(true);
	}
	
	private void onExitClicked()
	{
		Gdx.app.exit();
	}
	
	private void onSaveClicked()
	{
		tableMain.setVisible(true);
		tableSettings.setVisible(false);
		
		ChromaPreferences.instance.volumeMusic = sliderMusic.getValue();
		ChromaPreferences.instance.volumeSound = sliderSound.getValue();
		if(textFieldNickname.getText() != null && textFieldNickname.getText() != "")
			ChromaPreferences.instance.nickname = textFieldNickname.getText();
		ChromaPreferences.instance.save();
	}
	
	private void onCancelSettingsClicked()
	{
		tableMain.setVisible(true);
		tableSettings.setVisible(false);
	}
	
	private void onHostClicked()
	{
		tableClientServer.setVisible(false);
		tableChooseMap.setVisible(true);
	}
	
	private void onJoinClicked()
	{
		tableClientServer.setVisible(false);
		
		try
		{
			client = new ChromaClient();
		}
		catch(Exception exc)
		{
			labelError.setText("Unable to \n start client");
			System.out.println(exc.getMessage());
			tableError.setVisible(true);
			
			if(client != null)
				client.getKryoClient().stop();
			client = null;
		}
		if(!tableError.isVisible())
		{
			client.setNickname(ChromaPreferences.instance.nickname);
			
			buildClientConnection();
			tableClientConnection.setVisible(true);
		}
	}
	
	private void onCancelClientServerClicked()
	{
		tableClientServer.setVisible(false);
		tableMain.setVisible(true);
	}
	
	private void onCancelChooseMapClicked()
	{
		tableChooseMap.setVisible(false);
		tableClientServer.setVisible(true);
	}
	
	private void onConnectClicked(InetAddress ip)
	{
		tableClientConnection.setVisible(false);
		tableClientConnection = null;

		try
		{
			client.connect(ip);
		}
		catch(Exception exc)
		{
			labelError.setText("Unable to \n connect");
			System.out.println(exc.getMessage());
			tableError.setVisible(true);
			
			if(client != null)
				client.getKryoClient().stop();
			client = null;
		}
		if(!tableError.isVisible())
		{
			client.setNickname(ChromaPreferences.instance.nickname);
			
			buildClient();
			tableClient.setVisible(true);
		}
	}
	
	private void onCancelClientConnectionClicked()
	{
		tableClientConnection.setVisible(false);
		tableClientConnection = null;
		tableClientServer.setVisible(true);
		
		if(client != null)
			client.getKryoClient().stop();
		client = null;
	}
	
	private void onStartClicked()
	{
		game.setScreen(new ServerGameScreen(game, chosenMap, server, currentColor, chosenTime));
		
		// Notify all clients who is IN
		// Notify all clients: game starts
		PacketStartGame packet = new PacketStartGame();
		
		HashMap<PlayerColor, String> map = new HashMap<PlayerColor, String>();
		map.put(currentColor, ChromaPreferences.instance.nickname);
		for(Map.Entry<PlayerColor, String> entry : server.getPlayerInfos().entrySet())
			map.put(entry.getKey(), entry.getValue());
		
		packet.startInfos = map;
		
		server.getKryoServer().sendToAllTCP(packet);
	}
	
	private void onDisconnectServerClicked()
	{
		tableServer.setVisible(false);
		tableServer = null;
		tableMain.setVisible(true);
		
		if(server != null)
			server.getKryoServer().stop();
		server = null;
	}
	
	private void onDisconnectClientClicked()
	{
		tableClient.setVisible(false);
		tableClient = null;
		tableMain.setVisible(true);
		
		if(client != null)
			client.getKryoClient().stop();
		client = null;
	}
	
	private void onOkErrorClicked()
	{
		tableError.setVisible(false);
		tableMain.setVisible(true);
	}
	
	private void changeVolumeMusic(float volume)
	{
		Assets.instance.assetMusic.menuMusic.setVolume(volume);
		
		for(Music music : Assets.instance.assetMusic.musicList)
			music.setVolume(volume);
	}
	
	private void changeVolumeSound(float volume)
	{
		ChromaPreferences.instance.volumeSound = volume;
	}
	
	@Override
	public void dispose()
	{
		stage.dispose();
	}
}
