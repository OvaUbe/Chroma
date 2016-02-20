package com.ovaube.chroma.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.ovaube.chroma.networking.ChromaServer;
import com.ovaube.chroma.networking.PacketEndGame;
import com.ovaube.chroma.networking.PacketInputInfo;
import com.ovaube.chroma.networking.PacketWorldInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.BulletInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PickupInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PlayerInfo;
import com.ovaube.chroma.objects.Block;
import com.ovaube.chroma.objects.Bullet;
import com.ovaube.chroma.objects.Damager;
import com.ovaube.chroma.objects.Healer;
import com.ovaube.chroma.objects.Pickup;
import com.ovaube.chroma.objects.Player;
import com.ovaube.chroma.objects.Wall;
import com.ovaube.chroma.screens.MenuScreen;
import com.ovaube.chroma.screens.StatisticsScreen;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.BlockType;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PickupType;
import com.ovaube.chroma.util.PlayerColor;

public class ServerWorldController
{	
	private Game game;
	
	private ChromaServer server;
	
	private TiledMap map;
	private World world;
	private ChromaContactListener contactListener;

	private Wall wall;
	
	private Array<Block> blocks = new Array<Block>();
	private Array<Healer> healers = new Array<Healer>();
	private Array<Damager> damagers = new Array<Damager>();
	private HashMap<Integer, Bullet> bullets = new HashMap<Integer, Bullet>();
	private HashMap<PlayerColor, Player> players = new HashMap<PlayerColor, Player>();
	
	private Array<PacketInputInfo> inputs = new Array<PacketInputInfo>();
	private Array<Integer> bulletsToRemove = new Array<Integer>();
	private Array<PlayerColor> playersToRemove = new Array<PlayerColor>();
	private HashMap<Integer, BulletInfo> bulletsInfo = new HashMap<Integer, BulletInfo>();
	private HashMap<PlayerColor, PlayerInfo> playersInfo = new HashMap<PlayerColor, PlayerInfo>();
	
	private Pickup pickupWeapon;
	private Pickup pickupInvictus;
	
	private Array<Vector2> spawnPointsInvictus = new Array<Vector2>();
	private Array<Vector2> spawnPointsWeapon = new Array<Vector2>();
	
	private Array<Music> music = Assets.instance.assetMusic.musicList;
	private Music currentMusic;
	
	private PlayerColor color;
	
	private float currentTime;
	private float pickupTimeoutInvictus; 
	private float pickupTimeoutWeapon; 
	
	private Vector2 force = new Vector2();
	private final Vector2 stickCenter = new Vector2(Gdx.graphics.getHeight() / 4, Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 4));
	
	private PacketWorldInfo packetWorldInfo = new PacketWorldInfo();
	
	public ServerWorldController(Game game, TiledMap map, ChromaServer server, PlayerColor color, float time)
	{
		this.game = game;
		this.map = map;
		this.server = server;
		this.color = color;
		init();
		// Start timer
		currentTime = time;
	}
	
	private void init()
	{		
		createWorld();
		
		createPlayers();
		
		createblocks();
		
		createHealers();
		
		createDamagers();
		
		createWall();
		
		MapLayer layer;
		
		// Define invictus spawn points
		layer = (MapLayer) map.getLayers().get("InvictusSpawnPoints");
		for(MapObject object : layer.getObjects())
			spawnPointsInvictus.add(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM,
					(Float)object.getProperties().get("y") / Constants.PPM));
			
			
		// Define weapon spawn points
		layer = (MapLayer) map.getLayers().get("WeaponSpawnPoints");
		for(MapObject object : layer.getObjects())
			spawnPointsWeapon.add(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM,
					(Float)object.getProperties().get("y") / Constants.PPM));
		
		playMusic();
	}
	
	private void createWorld()
	{
		world = new World(new Vector2(0, 0), true);
		contactListener = new ChromaContactListener();
		world.setContactListener(contactListener);		
	}
	
	private void createPlayers()
	{		
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("SpawnPoints");
		
		// Add host color
		HashSet<PlayerColor> set = new HashSet<PlayerColor>(server.getPlayersConnected());
		set.add(color);
		
		Array<Vector2> spawnPoints = new Array<Vector2>();
		spawnPoints.addAll(
				new Vector2((Float)layer.getObjects().get(0).getProperties().get("x") / Constants.PPM,
						(Float)layer.getObjects().get(0).getProperties().get("y") / Constants.PPM),
				new Vector2((Float)layer.getObjects().get(1).getProperties().get("x") / Constants.PPM,
						(Float)layer.getObjects().get(1).getProperties().get("y") / Constants.PPM),
				new Vector2((Float)layer.getObjects().get(2).getProperties().get("x") / Constants.PPM,
						(Float)layer.getObjects().get(2).getProperties().get("y") / Constants.PPM));
		
		for(PlayerColor color : set)
		{
			Player player = new Player(new Vector2(0f, 0f),
					world,
					color,
					color == this.color ? ChromaPreferences.instance.nickname : server.getPlayerInfos().get(color),
					spawnPoints);
			players.put(color, player);
		}		
	}
	
	private void createblocks()
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Blocks");
		// Create blocks from map properties
		for(MapObject object : layer.getObjects())
		{
			Block block;

			if(object.getProperties().get("type").toString().equals("Circle"))
			{
				block = new Block(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM),
					world, new Vector2(
							(Float)object.getProperties().get("width") / Constants.PPM,
							(Float)object.getProperties().get("height") / Constants.PPM),
							BlockType.CIRCLE);
			}
			else if(object.getProperties().get("type").toString().equals("Square"))
			{
				block = new Block(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM),
					world, new Vector2(
							(Float)object.getProperties().get("width") / Constants.PPM,
							(Float)object.getProperties().get("height") / Constants.PPM),
							BlockType.SQUARE);
			}
			else
				break;
			
			blocks.add(block);
		}
	}
	
	private void createHealers()
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Healers");
		// Create healers from map properties
		for(MapObject object : layer.getObjects())
		{
			Healer healer = new Healer(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM),
					world);
			healers.add(healer);
		}
	}
	
	private void createDamagers()
	{
		MapLayer layer;
		layer = (MapLayer) map.getLayers().get("Damagers");
		// Create damagers from map properties
		for(MapObject object : layer.getObjects())
		{
			Damager damager = new Damager(new Vector2(
					(Float)object.getProperties().get("x") / Constants.PPM, 
					(Float)object.getProperties().get("y") / Constants.PPM),
					world);
			damagers.add(damager);
		}
	}
	
	private void createWall()
	{
		// Create wall from map properties
		wall = new Wall(new Vector2(
				(Integer)map.getProperties().get("width") * (Integer)map.getProperties().get("tilewidth") / Constants.PPM, 
				(Integer)map.getProperties().get("height") * (Integer)map.getProperties().get("tileheight") / Constants.PPM),
				world);
	}
	
	private void handleMyInput(float deltaTime)
	{
		if(Gdx.input.isKeyJustPressed(Keys.BACK))
		{
			server.getKryoServer().stop();
			server.getKryoServer().close();
			currentMusic.stop();
			game.setScreen(new MenuScreen(game, false));
		}
		
		if(!players.get(color).isDead())
		{
			// Handle multitouch
			for(int i = 0; i != 3; i++)
			{
				if(Gdx.input.isTouched(i))
				{
					if(Gdx.input.getX(i) < Gdx.graphics.getWidth() / 2)
					{
						// Don't you dare to forget the inverted Y-axis >:{
						force.set(((float)Gdx.input.getX(i) - stickCenter.x) * 6,
								-((float)Gdx.input.getY(i) - stickCenter.y) * 6);
						
						players.get(color).move(force);
					}
					else
					{
						if(Gdx.input.getY(i) > Gdx.graphics.getHeight() / 2)
							fire(color, deltaTime);
						
						if(Gdx.input.getY(i) < Gdx.graphics.getHeight() / 2)
							// Only single touch!
							if(Gdx.input.justTouched())
								handleTurret(color);
					}
				}
			}
		}
	}	
	
	private synchronized void handleClientInput(float deltaTime)
	{
		inputs.addAll(server.getPackets());
		
		if(inputs.size != 0)
		{
			for(PacketInputInfo input : inputs)
			{
				// Sometimes it crashes here with a nullpointer
				// Don't really know why this is happening, just added a simple !null check
				if(input != null && !input.color.equals(color) && server.getPlayersConnected().contains(input.color))
				{
					players.get(input.color).move(force.set(input.forceX, input.forceY));
					
					if(input.isBulletShot)
						fire(input.color, deltaTime);
					
					if(input.isPlayerSetTurret)
						handleTurret(input.color);
				}
			}
		}
		
		inputs.clear();
	}
	
	private void fire(PlayerColor color, float deltaTime)
	{
		if(players.get(color).salvoTimeout(deltaTime))
		{
			Bullet bullet = new Bullet(new Vector2(), world, players.get(color));
			
			// Create bullet under random number
			// Due to hashmap it must be unique
			int randomInt = -1;
			boolean success = false;
			while(!success)
			{
				randomInt = MathUtils.random(10000);
				if(!bullets.containsKey(randomInt))
					success = true;
			}
			bullets.put(randomInt, bullet);
		}
	}
	
	private void handleTurret(PlayerColor color)
	{
		Player player = players.get(color);
		if(player.isTurretEnabled())
		{
			if(!player.isTurret())
				player.setTurret();
			else
				player.unsetTurret();
		}
	}
	
	private void playMusic()
	{
		if(currentMusic == null || !currentMusic.isPlaying())
		{
			currentMusic = music.get(MathUtils.random(music.size - 1));
			currentMusic.play();
		}
	}
	
	private boolean pickupTimeoutInvictus(float deltaTime)
	{
		if(pickupTimeoutInvictus <= 0)
		{
			pickupTimeoutInvictus = Constants.MAX_PICKUP_TIMEOUT;
			return true;
		}
		else
		{
			pickupTimeoutInvictus -= deltaTime;
			return false;
		}
	}
	
	private boolean pickupTimeoutWeapon(float deltaTime)
	{
		if(pickupTimeoutWeapon <= 0)
		{
			pickupTimeoutWeapon = Constants.MAX_PICKUP_TIMEOUT;
			return true;
		}
		else
		{
			pickupTimeoutWeapon -= deltaTime;
			return false;
		}
	}
	
	private void handlePickupWeapon(float deltaTime)
	{
		if(pickupWeapon == null && pickupTimeoutWeapon(deltaTime))
			pickupWeapon = new Pickup(
				spawnPointsWeapon.get(MathUtils.random(spawnPointsWeapon.size - 1)),
				world, PickupType.WEAPON);
		
		if(pickupWeapon != null && pickupWeapon.isPickedUp())
		{
			if(!pickupWeapon.isDeleted())
			{
				world.destroyBody(pickupWeapon.getBody());
				pickupWeapon.setDeleted();
			}
			if(pickupWeapon.isEnded())
				pickupWeapon = null;
		}
	}

	private void handlePickupInvictus(float deltaTime)
	{
		if(pickupInvictus == null && pickupTimeoutInvictus(deltaTime))
			pickupInvictus = new Pickup(
				spawnPointsInvictus.get(MathUtils.random(spawnPointsInvictus.size - 1)),
				world, PickupType.INVICTUS);
		
		if(pickupInvictus != null && pickupInvictus.isPickedUp())
		{
			if(!pickupInvictus.isDeleted())
			{
				world.destroyBody(pickupInvictus.getBody());
				pickupInvictus.setDeleted();
			}
			if(pickupInvictus.isEnded())
				pickupInvictus = null;
		}
	}
	
	private void handlePlayers(float deltaTime)
	{
		for(Map.Entry<PlayerColor, Player> entry : players.entrySet())
		{
			Player player = entry.getValue();
			// Clamp some stuff
			player.getBody().setAngularVelocity(MathUtils.clamp(
					player.getBody().getAngularVelocity(),
					-Constants.MAX_PLAYER_ANGULAR_VELOCITY, 
					Constants.MAX_PLAYER_ANGULAR_VELOCITY));
			
			if(player.isHealing())
				if(player.healTimeout(deltaTime))
					player.increaseHealth(Constants.HEALER_HEAL);
			
			if(player.isDamaging())
				if(player.damageTimeout(deltaTime))
					player.decreaseHealth(Constants.DAMAGER_DAMAGE);
			
			if(player.isDead())
				if(player.spawnTimeout(deltaTime))
					player.reinit();
				
			// Prevents moving from the turret center
			if(player.isTurret())
				player.getBody().setTransform(
						player.getHealer().getBody().getPosition(), 
						player.getBody().getAngle());
		
			if(player.isFastCooldown())
				if(player.fastCooldownTimeout(deltaTime))
					player.unsetFastCooldown();
			
			if(player.isInvictible())
				if(player.shieldTimeout(deltaTime))
					player.unsetInvictible();
			
			// Sometimes they do disconnect. (remove disconnected players)
			if(!entry.getKey().equals(color) && !server.getPlayersConnected().contains(entry.getKey()))
				playersToRemove.add(entry.getKey());
		}
		
		for(PlayerColor key : playersToRemove)
			players.remove(key);
		playersToRemove.clear();
	}
	
	private void handleBullets()
	{
		for(Map.Entry<Integer, Bullet> entry  : bullets.entrySet())
		{
			Bullet bullet = entry.getValue();
			Integer key = entry.getKey();
			// Sometimes they get out of the level border...
			if(bullet.getBody().getPosition().x < -0.3 ||
					bullet.getBody().getPosition().x > ((Integer)map.getProperties().get("width") * (Integer)map.getProperties().get("tilewidth") / Constants.PPM) + 0.3 ||
					bullet.getBody().getPosition().y < -0.3 ||
					bullet.getBody().getPosition().y > ((Integer)map.getProperties().get("height") * (Integer)map.getProperties().get("tileheight") / Constants.PPM) + 0.3)
			{
				bullet.setDead();
			}
			
			// If the speed is too low - destroy bullet
			if(bullet.getBody().getLinearVelocity().len() < 13f)
				bullet.setDead();
			
			if(bullet.isDead())
			{
				if(!bullet.isDeleted())
				{
					world.destroyBody(bullet.getBody());
					bullet.setDeleted();
				}
				if(bullet.isEnded())
					bulletsToRemove.add(key);
			}
		}
		
		for(Integer key : bulletsToRemove)
			bullets.remove(key);
		
		bulletsToRemove.clear();
	}
	
	private void sendInfoPackets()
	{
		// Put bullets data
		for(Map.Entry<Integer, Bullet> entry : bullets.entrySet())
		{
			Integer key = entry.getKey();
			Bullet bullet = entry.getValue();
			
			BulletInfo bulletInfo = new BulletInfo();
			
			bulletInfo.isDead = bullet.isDead();
			bulletInfo.justFired = bullet.getFlagJustFired();
			bulletInfo.lifeCycle = bullet.getLifeCycle();
			bulletInfo.color = bullet.getPlayer().getColor();
			bulletInfo.positionX = bullet.getBody().getPosition().x;
			bulletInfo.positionY = bullet.getBody().getPosition().y;
			
			bulletsInfo.put(key, bulletInfo);
		}
		// Critical, hash map in packet was not initialized
		packetWorldInfo.bulletsInfo = bulletsInfo;
		
		// Put players data
		for(Map.Entry<PlayerColor, Player> entry : players.entrySet())
		{
			Player player = entry.getValue();
			
			PlayerColor color = player.getColor();
			
			PlayerInfo playerInfo = new PlayerInfo();
			
			playerInfo.currentAngle = player.getBody().getAngle();
			playerInfo.health = player.getHealth();
			playerInfo.kills = player.getKills();
			playerInfo.deaths = player.getDeaths();
			playerInfo.isBurning = player.isBurning();
			playerInfo.isDead = player.isDead();
			playerInfo.isFastCooldown = player.isFastCooldown();
			playerInfo.isInvictible = player.isInvictible();
			playerInfo.isTurret = player.isTurret();
			playerInfo.isTurretEnabled = player.isTurretEnabled();
			playerInfo.isDamaging = player.isDamaging();
			playerInfo.justBecameTurret = player.getFlagJustBecameTurret();
			playerInfo.justSpawned = player.getFlagJustSpawned();
			playerInfo.positionX = player.getBody().getPosition().x;
			playerInfo.positionY = player.getBody().getPosition().y;
			
			playersInfo.put(color, playerInfo);
		}
		// Critical, hash map in packet was not initialized
		packetWorldInfo.playersInfo = playersInfo;
			
		PickupInfo pickupInvictusInfo = new PickupInfo();
		if(pickupInvictus != null)
		{
			pickupInvictusInfo.isNull = false;
			pickupInvictusInfo.isDead = pickupInvictus.isDead();
			pickupInvictusInfo.isPickedUp = pickupInvictus.isPickedUp();
			pickupInvictusInfo.positionX = pickupInvictus.getBody().getPosition().x;
			pickupInvictusInfo.positionY = pickupInvictus.getBody().getPosition().y;
		}
		else
			pickupInvictusInfo.isNull = true;
		packetWorldInfo.pickupInvictusInfo = pickupInvictusInfo;
		
		PickupInfo pickupWeaponInfo = new PickupInfo();
		if(pickupWeapon != null)
		{
			pickupWeaponInfo.isNull = false;
			pickupWeaponInfo.isDead = pickupWeapon.isDead();
			pickupWeaponInfo.isPickedUp = pickupWeapon.isPickedUp();
			pickupWeaponInfo.positionX = pickupWeapon.getBody().getPosition().x;
			pickupWeaponInfo.positionY = pickupWeapon.getBody().getPosition().y;
		}
		else
			pickupWeaponInfo.isNull = true;
		packetWorldInfo.pickupWeaponInfo = pickupWeaponInfo;
		
		packetWorldInfo.currentTime = currentTime;
		
		server.getKryoServer().sendToAllTCP(packetWorldInfo);	
		
		playersInfo.clear();
		bulletsInfo.clear();
	}
	
	public void update(float deltaTime)
	{
		playMusic();
		
		handleMyInput(deltaTime);
		handleClientInput(deltaTime);
		
		world.step(Constants.STEP, 8, 4);
		
		handleBullets();		
		handlePickupWeapon(deltaTime);
		handlePickupInvictus(deltaTime);				
		handlePlayers(deltaTime);
		
		currentTime -= deltaTime;
		
		if(currentTime <= 0)
		{
			server.getKryoServer().sendToAllTCP(new PacketEndGame());
			game.setScreen(new StatisticsScreen(game, players, true));
		}
		
		sendInfoPackets();
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public Array<Block> getBlocks()
	{
		return blocks;
	}
	
	public HashMap<Integer, Bullet> getBullets()
	{
		return bullets;
	}

	public HashMap<PlayerColor, Player> getPlayers()
	{

		return players;
	}
	
	public Array<Healer> getHealers()
	{
		return healers;
	}
	
	public Array<Damager> getDamagers()
	{
		return damagers;
	}
	
	public Pickup getPickupInvictus()
	{		
		return pickupInvictus;
	}
	
	public Pickup getPickupWeapon()
	{		
		return pickupWeapon;
	}
	
	public Wall getWall()
	{
		return wall;
	}
	
	public Float getTime()
	{
		return currentTime;
	}
	
	public Music getPlayingMusic()
	{
		return currentMusic;
	}
	
	public PlayerColor getColor()
	{
		return color;
	}
}
