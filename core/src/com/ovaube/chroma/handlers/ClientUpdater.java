package com.ovaube.chroma.handlers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.ovaube.chroma.GUI.HUD;
import com.ovaube.chroma.flyweights.FlyweightBlock;
import com.ovaube.chroma.flyweights.FlyweightBullet;
import com.ovaube.chroma.flyweights.FlyweightDamager;
import com.ovaube.chroma.flyweights.FlyweightHealer;
import com.ovaube.chroma.flyweights.FlyweightPickup;
import com.ovaube.chroma.flyweights.FlyweightPlayer;
import com.ovaube.chroma.flyweights.FlyweightWall;
import com.ovaube.chroma.networking.ChromaClient;
import com.ovaube.chroma.networking.PacketInputInfo;
import com.ovaube.chroma.networking.PacketWorldInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.BulletInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PickupInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PlayerInfo;
import com.ovaube.chroma.screens.StatisticsScreen;
import com.ovaube.chroma.util.BlockType;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PickupType;
import com.ovaube.chroma.util.PlayerColor;

public class ClientUpdater implements Disposable
{
	private Game game;
	
	private ChromaClient client; 
	
	private PlayerColor color;
	
	private HashMap<PlayerColor, FlyweightPlayer> players = new HashMap<PlayerColor, FlyweightPlayer>();
	private Array<FlyweightBlock> blocks = new Array<FlyweightBlock>();
	private HashMap<Integer, FlyweightBullet> bullets = new HashMap<Integer, FlyweightBullet>();
	private Array<FlyweightDamager> damagers = new Array<FlyweightDamager>();
	private Array<FlyweightHealer> healers = new Array<FlyweightHealer>();
	
	private Array<Integer> bulletsToRemove = new Array<Integer>();
	Array<PlayerColor> playersToRemove = new Array<PlayerColor>();
	
	private FlyweightPickup pickupInvictus;
	private FlyweightPickup pickupWeapon;
	
	private FlyweightWall wall;
	
	private SpriteBatch batch;	
	private HUD hud;
	
	private OrthographicCamera camera;
	private OrthographicCamera hudCamera;
	
	private TiledMap map;
	
	private float currentTime;
	
	private Vector2 move = new Vector2();
	private Vector2 force = new Vector2();
	private final Vector2 stickCenter = new Vector2(Gdx.graphics.getHeight() / 4, Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 4));
	
	private PacketInputInfo packetInput = new PacketInputInfo();
	
	public ClientUpdater(Game game, PlayerColor color, ChromaClient client, TiledMap map)
	{
		this.color = color;
		this.client = client;
		this.map = map;
		this.game = game;
		
		// Wait until first info is received
		while(!client.isWorldInfoReceived())
		{
			try
			{
			Thread.sleep(100);
			}
			catch(Exception exc)
			{
				System.out.println(exc.getMessage());
			}
		}
		
		createFlyweightPlayers();
		createFlyweightBlocks();
		createFlyweightDamagers();
		createFlyweightHealers();
		createFlyweightWall();
		
		initCameras();
	}

	private void createFlyweightPlayers() 
	{
		for(Map.Entry<PlayerColor, String> entry : client.getPacketStart().startInfos.entrySet())
		{			
			FlyweightPlayer player = new FlyweightPlayer(entry.getKey(), entry.getValue());
			players.put(entry.getKey(), player);
		}
	}

	private void createFlyweightBlocks() 
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Blocks");
		
		for(MapObject object : layer.getObjects())
		{
			FlyweightBlock block;

			if(object.getProperties().get("type").toString().equals("Circle"))
			{
				block = new FlyweightBlock(new Vector2(
						(Float)object.getProperties().get("x") / Constants.PPM, 
						(Float)object.getProperties().get("y") / Constants.PPM),
						new Vector2(
						(Float)object.getProperties().get("width") / Constants.PPM,
						(Float)object.getProperties().get("height") / Constants.PPM),
						BlockType.CIRCLE);
			}
			else if(object.getProperties().get("type").toString().equals("Square"))
			{
				block = new FlyweightBlock(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM),
					new Vector2(
					(Float)object.getProperties().get("width") / Constants.PPM,
					(Float)object.getProperties().get("height") / Constants.PPM),
					BlockType.SQUARE);
			}
			else
				break;
			
			blocks.add(block);
		}
		
	}

	private void createFlyweightDamagers() 
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Damagers");
		// Create damagers from map properties
		for(MapObject object : layer.getObjects())
		{
			FlyweightDamager damager = new FlyweightDamager(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM));
			damagers.add(damager);
		}		
	}

	private void createFlyweightHealers() 
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Healers");
		// Create healers from map properties
		for(MapObject object : layer.getObjects())
		{
			FlyweightHealer healer = new FlyweightHealer(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM));
			healers.add(healer);
		}		
	}
	
	private void createFlyweightWall() 
	{
		wall = new FlyweightWall(new Vector2(
				(Integer)map.getProperties().get("width") * (Integer)map.getProperties().get("tilewidth") / Constants.PPM, 
				(Integer)map.getProperties().get("height") * (Integer)map.getProperties().get("tileheight") / Constants.PPM));		
	}

	private void initCameras()
	{		
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
		hudCamera.update();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
		camera.update();
		
		batch = new SpriteBatch();		
		hud = new HUD(players.get(color), this, hudCamera);	
	}
	
	public void update(float deltaTime)
	{
		openAndHandlePacket();
		
		// Render objects
		batch.setProjectionMatrix(camera.combined);

		wall.renderAndUpdate(batch, deltaTime);
		for(FlyweightBlock block : blocks)
			block.renderAndUpdate(batch, deltaTime);
		for(Map.Entry<PlayerColor, FlyweightPlayer> entry : players.entrySet())
			entry.getValue().renderAndUpdate(batch, deltaTime);
		for(Map.Entry<Integer, FlyweightBullet> entry : bullets.entrySet())
			entry.getValue().renderAndUpdate(batch, deltaTime);
		for(FlyweightDamager damager : damagers)
			damager.renderAndUpdate(batch, deltaTime);
		for(FlyweightHealer healer : healers)
			healer.renderAndUpdate(batch, deltaTime);
		if(pickupInvictus != null)
			pickupInvictus.renderAndUpdate(batch, deltaTime);
		if(pickupWeapon != null)
			pickupWeapon.renderAndUpdate(batch, deltaTime);
		
		// Render gui
		hudCamera.update();
		batch.setProjectionMatrix(hudCamera.combined);
		hud.render(batch, currentTime);
		
		// Follow player
		camera.position.set(
				players.get(color).getPosition().x * Constants.PPM, 
				players.get(color).getPosition().y * Constants.PPM, 
				0f);
		camera.update();
		
		if(client.isPacketEndGameReceived() || currentTime <= 0)
		{
			game.setScreen(new StatisticsScreen(game, players));
		}
		
		handleAndSendInput();
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
	
	private void handleAndSendInput()
	{
		move.set(Gdx.graphics.getHeight() / 4, Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 4));
		boolean isBulletShot = false;
		boolean isPlayerSetTurret = false;
		
		if(!players.get(color).isDead())
		{
			// Handle multitouch
			for(int i = 0; i != 3; i++)
			{
				if(Gdx.input.isTouched(i))
				{
					if(Gdx.input.getX(i) < Gdx.graphics.getWidth() / 2)
						move.set((float)Gdx.input.getX(i), (float)Gdx.input.getY(i));
					else
					{
						if(Gdx.input.getY(i) > Gdx.graphics.getHeight() / 2)
							isBulletShot = true;
						if(Gdx.input.getY(i) < Gdx.graphics.getHeight() / 2)
							// Only single touch!
							if(Gdx.input.justTouched())
								isPlayerSetTurret = true;
					}
				}
			}
		}
		
		// Don't you dare to forget the inverted Y-axis >:{
		force.set((move.x - stickCenter.x) * 6, -(move.y - stickCenter.y) * 6);
		
		packetInput.forceX = force.x;
		packetInput.forceY = force.y;
		packetInput.isBulletShot = isBulletShot;
		packetInput.isPlayerSetTurret = isPlayerSetTurret;
		packetInput.color = this.color;
		client.getKryoClient().sendTCP(packetInput);
	}
	
	private void openAndHandlePacket()
	{
		PacketWorldInfo packet = client.getPacketWorldInfo();
		
		// Handle bullets
		for(Map.Entry<Integer, BulletInfo> entry : packet.bulletsInfo.entrySet())
		{
			Integer key = entry.getKey();
			BulletInfo bulletInfo = entry.getValue();
			FlyweightBullet currentFlyweightBullet;
			
			// If bullet is present both on server and on client
			if(bullets.containsKey(key))
			{
				currentFlyweightBullet = bullets.get(key);
				
				currentFlyweightBullet.setFlagIsDead(bulletInfo.isDead);
				currentFlyweightBullet.setFlagJustFired(bulletInfo.justFired);
				currentFlyweightBullet.setLifeCycle(bulletInfo.lifeCycle);
				currentFlyweightBullet.setPosition(bulletInfo.positionX, bulletInfo.positionY);
			}
			// If bullet is present only on server - we have to add it to client world
			else
			{
				currentFlyweightBullet = new FlyweightBullet(new Vector2(bulletInfo.positionX, bulletInfo.positionY), bulletInfo.color);
				bullets.put(key, currentFlyweightBullet);
			}
		}
		// If bullet is dead on server, but present on client - we have to delete it on client
		// Cannot remove from hashmap being iterated
		for(Map.Entry<Integer, FlyweightBullet> entry : bullets.entrySet())
		{
			Integer key = entry.getKey();
			
			if(!packet.bulletsInfo.containsKey(key))
				bulletsToRemove.add(key);
		}
		for(Integer key : bulletsToRemove)
			bullets.remove(key);	
		
		bulletsToRemove.clear();
		
		// Handle players
		// Remove disconnected players
		for(Map.Entry<PlayerColor, FlyweightPlayer> entry : players.entrySet())
			if(!packet.playersInfo.containsKey(entry.getKey()))
				playersToRemove.add(entry.getKey());
		for(PlayerColor color : playersToRemove)
			players.remove(color);
		
		playersToRemove.clear();				
		
		for(Map.Entry<PlayerColor, PlayerInfo> entry : packet.playersInfo.entrySet())
		{
			PlayerColor key = entry.getKey();
			PlayerInfo playerInfo = entry.getValue();
			
			FlyweightPlayer currentPlayer = players.get(key);
			
			currentPlayer.setAngle(playerInfo.currentAngle);
			currentPlayer.setFlagBurning(playerInfo.isBurning);
			currentPlayer.setFlagDead(playerInfo.isDead);
			currentPlayer.setFlagFastCooldown(playerInfo.isFastCooldown);
			currentPlayer.setFlagInvictible(playerInfo.isInvictible);
			currentPlayer.setFlagDamaging(playerInfo.isDamaging);
			currentPlayer.setFlagJustBecameTurret(playerInfo.justBecameTurret);
			currentPlayer.setFlagJustSpawned(playerInfo.justSpawned);
			currentPlayer.setFlagTurret(playerInfo.isTurret);
			currentPlayer.setFlagTurretEnabled(playerInfo.isTurretEnabled);
			currentPlayer.setHealth(playerInfo.health);
			currentPlayer.setKills(playerInfo.kills);
			currentPlayer.setDeaths(playerInfo.deaths);
			currentPlayer.setPosition(playerInfo.positionX, playerInfo.positionY);
		}
		
		// Handle pickups
		PickupInfo pickupInvictusInfo = packet.pickupInvictusInfo;
		// Present both on server and on client
		if(pickupInvictus != null && !pickupInvictusInfo.isNull)
		{
			pickupInvictus.setFlagIsDead(pickupInvictusInfo.isDead);
			pickupInvictus.setFlagIsPickedUp(pickupInvictusInfo.isPickedUp);
		}
		// Dead here, present on server - add to client world
		else if(pickupInvictus == null && !pickupInvictusInfo.isNull)
		{
			pickupInvictus = new FlyweightPickup(new Vector2(
					pickupInvictusInfo.positionX, 
					pickupInvictusInfo.positionY),
					PickupType.INVICTUS);
		}
		// Present here, dead on server - delete on client world
		else if(pickupInvictus != null && pickupInvictusInfo.isNull)
			pickupInvictus = null;
		
		PickupInfo pickupWeaponInfo = packet.pickupWeaponInfo;
		// Present both on server and on client
		if(pickupWeapon != null && !pickupWeaponInfo.isNull)
		{
			pickupWeapon.setFlagIsDead(pickupWeaponInfo.isDead);
			pickupWeapon.setFlagIsPickedUp(pickupWeaponInfo.isPickedUp);
		}
		// Dead here, present on server - add to client world
		else if(pickupWeapon == null && !pickupWeaponInfo.isNull)
		{
			pickupWeapon = new FlyweightPickup(new Vector2(
					pickupWeaponInfo.positionX, 
					pickupWeaponInfo.positionY),
					PickupType.WEAPON);
		}
		// Present here, dead on server - delete on client world
		else if(pickupWeapon != null && pickupWeaponInfo.isNull)
			pickupWeapon = null;
		
		currentTime = packet.currentTime;
	}
	
	public HashMap<PlayerColor, FlyweightPlayer> getPlayers()
	{
		return players;
	}
}
