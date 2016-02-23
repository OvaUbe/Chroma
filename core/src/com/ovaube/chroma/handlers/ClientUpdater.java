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
import com.ovaube.chroma.phonies.PhonyBlock;
import com.ovaube.chroma.phonies.PhonyBullet;
import com.ovaube.chroma.phonies.PhonyDamager;
import com.ovaube.chroma.phonies.PhonyHealer;
import com.ovaube.chroma.phonies.PhonyPickup;
import com.ovaube.chroma.phonies.PhonyPlayer;
import com.ovaube.chroma.phonies.PhonyWall;
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
	
	private HashMap<PlayerColor, PhonyPlayer> players = new HashMap<PlayerColor, PhonyPlayer>();
	private Array<PhonyBlock> blocks = new Array<PhonyBlock>();
	private HashMap<Integer, PhonyBullet> bullets = new HashMap<Integer, PhonyBullet>();
	private Array<PhonyDamager> damagers = new Array<PhonyDamager>();
	private Array<PhonyHealer> healers = new Array<PhonyHealer>();
	
	private Array<Integer> bulletsToRemove = new Array<Integer>();
	Array<PlayerColor> playersToRemove = new Array<PlayerColor>();
	
	private PhonyPickup pickupInvictus;
	private PhonyPickup pickupWeapon;
	
	private PhonyWall wall;
	
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
		
		createPhonyPlayers();
		createPhonyBlocks();
		createPhonyDamagers();
		createPhonyHealers();
		createPhonyWall();
		
		initCameras();
	}

	private void createPhonyPlayers() 
	{
		for(Map.Entry<PlayerColor, String> entry : client.getPacketStart().startInfos.entrySet())
		{			
			PhonyPlayer player = new PhonyPlayer(entry.getKey(), entry.getValue());
			players.put(entry.getKey(), player);
		}
	}

	private void createPhonyBlocks() 
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Blocks");
		
		for(MapObject object : layer.getObjects())
		{
			PhonyBlock block;

			if(object.getProperties().get("type").toString().equals("Circle"))
			{
				block = new PhonyBlock(new Vector2(
						(Float)object.getProperties().get("x") / Constants.PPM, 
						(Float)object.getProperties().get("y") / Constants.PPM),
						new Vector2(
						(Float)object.getProperties().get("width") / Constants.PPM,
						(Float)object.getProperties().get("height") / Constants.PPM),
						BlockType.CIRCLE);
			}
			else if(object.getProperties().get("type").toString().equals("Square"))
			{
				block = new PhonyBlock(new Vector2(
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

	private void createPhonyDamagers() 
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Damagers");
		// Create damagers from map properties
		for(MapObject object : layer.getObjects())
		{
			PhonyDamager damager = new PhonyDamager(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM));
			damagers.add(damager);
		}		
	}

	private void createPhonyHealers() 
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Healers");
		// Create healers from map properties
		for(MapObject object : layer.getObjects())
		{
			PhonyHealer healer = new PhonyHealer(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM));
			healers.add(healer);
		}		
	}
	
	private void createPhonyWall() 
	{
		wall = new PhonyWall(new Vector2(
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
		batch.begin();

		wall.renderAndUpdate(batch, deltaTime);
		for(PhonyBlock block : blocks)
			block.renderAndUpdate(batch, deltaTime);
		for(Map.Entry<PlayerColor, PhonyPlayer> entry : players.entrySet())
			entry.getValue().renderAndUpdate(batch, deltaTime);
		for(Map.Entry<Integer, PhonyBullet> entry : bullets.entrySet())
			entry.getValue().renderAndUpdate(batch, deltaTime);
		for(PhonyDamager damager : damagers)
			damager.renderAndUpdate(batch, deltaTime);
		for(PhonyHealer healer : healers)
			healer.renderAndUpdate(batch, deltaTime);
		if(pickupInvictus != null)
			pickupInvictus.renderAndUpdate(batch, deltaTime);
		if(pickupWeapon != null)
			pickupWeapon.renderAndUpdate(batch, deltaTime);
		
		// Render gui
		hudCamera.update();
		batch.setProjectionMatrix(hudCamera.combined);
		hud.render(batch, currentTime);

		batch.end();
		
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
			PhonyBullet currentPhonyBullet;
			
			// If bullet is present both on server and on client
			if(bullets.containsKey(key))
			{
				currentPhonyBullet = bullets.get(key);
				
				currentPhonyBullet.setFlagIsDead(bulletInfo.isDead);
				currentPhonyBullet.setFlagJustFired(bulletInfo.justFired);
				currentPhonyBullet.setLifeCycle(bulletInfo.lifeCycle);
				currentPhonyBullet.setPosition(bulletInfo.positionX, bulletInfo.positionY);
			}
			// If bullet is present only on server - we have to add it to client world
			else
			{
				currentPhonyBullet = new PhonyBullet(new Vector2(bulletInfo.positionX, bulletInfo.positionY), bulletInfo.color);
				bullets.put(key, currentPhonyBullet);
			}
		}
		// If bullet is dead on server, but present on client - we have to delete it on client
		// Cannot remove from hashmap being iterated
		for(Map.Entry<Integer, PhonyBullet> entry : bullets.entrySet())
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
		for(Map.Entry<PlayerColor, PhonyPlayer> entry : players.entrySet())
			if(!packet.playersInfo.containsKey(entry.getKey()))
				playersToRemove.add(entry.getKey());
		for(PlayerColor color : playersToRemove)
			players.remove(color);
		
		playersToRemove.clear();				
		
		for(Map.Entry<PlayerColor, PlayerInfo> entry : packet.playersInfo.entrySet())
		{
			PlayerColor key = entry.getKey();
			PlayerInfo playerInfo = entry.getValue();
			
			PhonyPlayer currentPlayer = players.get(key);
			
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
			pickupInvictus = new PhonyPickup(new Vector2(
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
			pickupWeapon = new PhonyPickup(new Vector2(
					pickupWeaponInfo.positionX, 
					pickupWeaponInfo.positionY),
					PickupType.WEAPON);
		}
		// Present here, dead on server - delete on client world
		else if(pickupWeapon != null && pickupWeaponInfo.isNull)
			pickupWeapon = null;
		
		currentTime = packet.currentTime;
	}
	
	public HashMap<PlayerColor, PhonyPlayer> getPlayers()
	{
		return players;
	}
}
