package com.ovaube.chroma.util;

import java.util.LinkedHashMap;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Assets implements Disposable, AssetErrorListener
{
	public static final String TAG = Assets.class.getName();
	
	public static final Assets instance = new Assets();
	
	private AssetManager assetManager;
	
	public AssetFonts assetFonts;
	public AssetTextures assetTextures;
	public AssetSFX assetSFX;
	public AssetPFX assetPFX;
	public AssetMusic assetMusic;
	public AssetMaps assetMaps;
	
	private Assets() {}
	
	public class AssetFonts 
	{
		public final BitmapFont white;
		public final BitmapFont red;
		public final BitmapFont blue;
		public final BitmapFont green;
		public final BitmapFont gray;
		
		public AssetFonts()
		{
			white = assetManager.get("fonts/WhiteFont.fnt", BitmapFont.class);
			red = assetManager.get("fonts/RedFont.fnt", BitmapFont.class);
			blue = assetManager.get("fonts/BlueFont.fnt", BitmapFont.class);
			green = assetManager.get("fonts/GreenFont.fnt", BitmapFont.class);
			gray = assetManager.get("fonts/GrayFont.fnt", BitmapFont.class);
			
			red.setScale(2f, 2f);
			white.setScale(2f, 2f);
			blue.setScale(2f, 2f);
			green.setScale(2f, 2f);
			gray.setScale(2f, 2f);
		}
	}
	
	public class AssetTextures 
	{
		// Game
		public final TextureRegion blockCircleSmall;
		public final TextureRegion blockCircleMedium;
		public final TextureRegion blockCircleBig;
		public final TextureRegion blockSquare;
		public final TextureRegion bg;
		public final TextureRegion bulletBlue;
		public final TextureRegion bulletGreen;
		public final TextureRegion bulletViolet;
		public final TextureRegion powerupInvictus;
		public final TextureRegion powerupWeapon;
		public final TextureRegion playerBlue;
		public final TextureRegion playerGreen;
		public final TextureRegion playerViolet;
		public final TextureRegion turret;
		// GUI
		public final TextureRegion statsBlue;
		public final TextureRegion statsGreen;
		public final TextureRegion statsViolet;
		public final TextureRegion joystickLeft;
		public final TextureRegion joystickRight;
		public final TextureRegion stick;
		public final TextureRegion sliderKnob;
		public final TextureRegion sliderBG;
		public final TextureRegion cursor;
		
		public AssetTextures(TextureAtlas atlasMain, TextureAtlas atlasGUI)
		{
			// Game textures
			blockCircleSmall = new TextureRegion(atlasMain.findRegion("BlockCircleSmall"));
			blockCircleMedium = new TextureRegion(atlasMain.findRegion("BlockCircleMedium"));
			blockCircleBig = new TextureRegion(atlasMain.findRegion("BlockCircleBig"));
			blockSquare = new TextureRegion(atlasMain.findRegion("BlockSquare"));
			bulletBlue = new TextureRegion(atlasMain.findRegion("BulletBlue"));
			bulletGreen = new TextureRegion(atlasMain.findRegion("BulletGreen"));
			bulletViolet = new TextureRegion(atlasMain.findRegion("BulletViolet"));
			powerupInvictus = new TextureRegion(atlasMain.findRegion("PowerupInvictus"));
			powerupWeapon = new TextureRegion(atlasMain.findRegion("PowerupWeapon"));
			playerBlue = new TextureRegion(atlasMain.findRegion("PlayerBlue"));
			playerGreen = new TextureRegion(atlasMain.findRegion("PlayerGreen"));
			playerViolet = new TextureRegion(atlasMain.findRegion("PlayerViolet"));
			turret = new TextureRegion(atlasMain.findRegion("Turret"));
			// BG
			bg = new TextureRegion(assetManager.get("textures/BG.png", Texture.class));
			// GUI Textures
			statsBlue = new TextureRegion(atlasGUI.findRegion("StatsBlue"));
			statsGreen = new TextureRegion(atlasGUI.findRegion("StatsGreen"));
			statsViolet = new TextureRegion(atlasGUI.findRegion("StatsViolet"));
			joystickLeft = new TextureRegion(atlasGUI.findRegion("LeftJoystick"));
			joystickRight = new TextureRegion(atlasGUI.findRegion("RightJoystick"));
			stick = new TextureRegion(atlasGUI.findRegion("Stick"));
			sliderKnob = new TextureRegion(atlasGUI.findRegion("SliderKnob"));
			sliderBG = new TextureRegion(atlasGUI.findRegion("SliderBG"));
			cursor = new TextureRegion(atlasGUI.findRegion("Cursor"));
		}
	}
	
	public class AssetSFX 
	{
		public final Sound bulletDeath;
		public final Sound damage;
		public final Sound explosion;
		public final Sound pickupInvictus;
		public final Sound pickupWeapon;
		public final Sound becomeTurret;
		public final Sound salvo;
		public final Sound spawn;
		public final Sound endGame;
		
		public AssetSFX()
		{
			bulletDeath = assetManager.get("sfx/BulletDeathSound.wav", Sound.class);
			damage = assetManager.get("sfx/DamageSound.wav", Sound.class);
			explosion = assetManager.get("sfx/ExplosionSound.wav", Sound.class);
			pickupInvictus = assetManager.get("sfx/InvictusPowerupSound.wav", Sound.class);
			salvo = assetManager.get("sfx/SalvoSound.wav", Sound.class);
			spawn = assetManager.get("sfx/SpawnSound.wav", Sound.class);
			pickupWeapon = assetManager.get("sfx/WeaponPowerupSound.wav", Sound.class);
			becomeTurret = assetManager.get("sfx/BecomeTurretSound.wav", Sound.class);
			endGame = assetManager.get("sfx/EndGameSound.wav", Sound.class);
		}
	}
	
	public class AssetPFX 
	{
		public final ParticleEffect becomeTurret;
		public final ParticleEffect damage;
		public final ParticleEffect damager;
		public final ParticleEffect explosion;
		public final ParticleEffect healer;
		public final ParticleEffect pickup;
		public final ParticleEffect bulletDeathGreen;
		public final ParticleEffect bulletDeathBlue;
		public final ParticleEffect bulletDeathViolet;
		public final ParticleEffect shield;
		public final ParticleEffect fastCooldown;
		public final ParticleEffect burning;
		public final ParticleEffect menu;
		
		public AssetPFX()
		{
			becomeTurret = assetManager.get("pfx/BecomeTurret", ParticleEffect.class);
			damage = assetManager.get("pfx/Damage", ParticleEffect.class);
			damager = assetManager.get("pfx/Damager", ParticleEffect.class);
			explosion = assetManager.get("pfx/Explosion", ParticleEffect.class);
			healer = assetManager.get("pfx/Healer", ParticleEffect.class);
			pickup = assetManager.get("pfx/Pickup", ParticleEffect.class);
			bulletDeathGreen = assetManager.get("pfx/bulletDeathGreen", ParticleEffect.class);
			bulletDeathBlue = assetManager.get("pfx/bulletDeathBlue", ParticleEffect.class);
			bulletDeathViolet = assetManager.get("pfx/bulletDeathViolet", ParticleEffect.class);
			shield = assetManager.get("pfx/Shield", ParticleEffect.class);
			fastCooldown = assetManager.get("pfx/FastBullets", ParticleEffect.class);
			burning = assetManager.get("pfx/Burning", ParticleEffect.class);
			menu = assetManager.get("pfx/Menu", ParticleEffect.class);
		}
	}
	
	public class AssetMusic 
	{
		public final Array<Music> musicList;	
		public final Music menuMusic;
		
		public AssetMusic(Array<Music> music)
		{
			musicList = new Array<Music>();
			musicList.addAll(music);
			
			menuMusic = assetManager.get("music/menu.mp3", Music.class);
		}
	}
	
	public class AssetMaps
	{
		public final LinkedHashMap<String, TiledMap> maps;
		
		public AssetMaps(LinkedHashMap<String, TiledMap> maps)
		{
			this.maps = maps;
		}
	}
	
	public void init(AssetManager assetManager)
	{
		this.assetManager = assetManager;
		
		assetManager.setErrorListener(this);
		// Load textures
		assetManager.load(Constants.TEXTURE_ATLAS_MAIN, TextureAtlas.class);
		assetManager.load(Constants.TEXTURE_ATLAS_GUI, TextureAtlas.class);
		assetManager.load("textures/BG.png", Texture.class);
		// Load fonts
		assetManager.load("fonts/RedFont.fnt", BitmapFont.class);
		assetManager.load("fonts/WhiteFont.fnt", BitmapFont.class);
		assetManager.load("fonts/BlueFont.fnt", BitmapFont.class);
		assetManager.load("fonts/GreenFont.fnt", BitmapFont.class);
		assetManager.load("fonts/GrayFont.fnt", BitmapFont.class);
		// Load pfx
		assetManager.load("pfx/BecomeTurret", ParticleEffect.class);
		assetManager.load("pfx/Damage", ParticleEffect.class);
		assetManager.load("pfx/Damager", ParticleEffect.class);
		assetManager.load("pfx/Explosion", ParticleEffect.class);
		assetManager.load("pfx/Healer", ParticleEffect.class);
		assetManager.load("pfx/Pickup", ParticleEffect.class);
		assetManager.load("pfx/bulletDeathGreen", ParticleEffect.class);
		assetManager.load("pfx/bulletDeathBlue", ParticleEffect.class);
		assetManager.load("pfx/bulletDeathViolet", ParticleEffect.class);
		assetManager.load("pfx/Shield", ParticleEffect.class);
		assetManager.load("pfx/FastBullets", ParticleEffect.class);
		assetManager.load("pfx/Burning", ParticleEffect.class);
		assetManager.load("pfx/Menu", ParticleEffect.class);
		// Load sfx
		assetManager.load("sfx/DamageSound.wav", Sound.class);
		assetManager.load("sfx/BulletDeathSound.wav", Sound.class);
		assetManager.load("sfx/ExplosionSound.wav", Sound.class);
		assetManager.load("sfx/InvictusPowerupSound.wav", Sound.class);
		assetManager.load("sfx/MenuSound.wav", Sound.class);
		assetManager.load("sfx/SalvoSound.wav", Sound.class);
		assetManager.load("sfx/SpawnSound.wav", Sound.class);
		assetManager.load("sfx/WeaponPowerupSound.wav", Sound.class);
		assetManager.load("sfx/BecomeTurretSound.wav", Sound.class);
		assetManager.load("sfx/EndGameSound.wav", Sound.class);
		// Load music
		assetManager.load("music/menu.mp3", Music.class);
		musicLoader();
		
		assetManager.finishLoading();
		
		// Check assets
		Gdx.app.debug(TAG, "# of assets loaded" + assetManager.getAssetNames().size);
		for(String asset : assetManager.getAssetNames())
			Gdx.app.debug(TAG, "asset: " + asset);
		
		TextureAtlas atlasMain = assetManager.get(Constants.TEXTURE_ATLAS_MAIN, TextureAtlas.class);
		TextureAtlas atlasGUI = assetManager.get(Constants.TEXTURE_ATLAS_GUI, TextureAtlas.class);
		// Set linear filtering
		for (Texture t : atlasMain.getTextures()) {
			t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		for (Texture t : atlasGUI.getTextures()) {
			t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		assetManager.get("textures/BG.png", Texture.class).setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		// Initialize assets
		assetFonts = new AssetFonts();
		assetTextures = new AssetTextures(atlasMain, atlasGUI);
		assetSFX = new AssetSFX();
		assetPFX = new AssetPFX();
		assetMusic = new AssetMusic(musicGetter());
		assetMaps = new AssetMaps(mapGetter());
	}
	
	private void musicLoader()
	{	
		FileHandle dir;
		if(Gdx.app.getType() == ApplicationType.Android)
			dir = Gdx.files.internal("music/gamemusic");
		else
			dir = Gdx.files.internal("./bin/music/gamemusic");
		
		for(FileHandle item : dir.list())
			if(!item.isDirectory() && item.name().matches(".*\\.mp3"))
				assetManager.load("music/gamemusic/" + item.name(), Music.class);
	}
	
	private Array<Music> musicGetter()
	{
		Array<Music> music = new Array<Music>();
		
		FileHandle dir;
		if(Gdx.app.getType() == ApplicationType.Android)
			dir = Gdx.files.internal("music/gamemusic");
		else
			dir = Gdx.files.internal("./bin/music/gamemusic");
		
		for(FileHandle item : dir.list())
			if(!item.isDirectory() && item.name().matches(".*\\.mp3"))
				music.add(assetManager.get("music/gamemusic/" + item.name(), Music.class));
		
		return music;
	}
	
	private LinkedHashMap<String, TiledMap> mapGetter()
	{
		LinkedHashMap<String, TiledMap> maps = new LinkedHashMap<String, TiledMap>();
		TmxMapLoader loader = new TmxMapLoader();
		
		FileHandle dir;
		if(Gdx.app.getType() == ApplicationType.Android)
			dir = Gdx.files.internal("maps");
		else
			dir = Gdx.files.internal("./bin/maps");
		
		for(FileHandle item : dir.list())
			if(!item.isDirectory() && item.name().matches(".*\\.tmx"))
				maps.put(item.name().replace(".tmx", ""), loader.load("maps/" + item.name()));
		
		return maps;
	}
	
	@Override
	public void dispose()
	{
		assetManager.dispose();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void error(AssetDescriptor asset, Throwable throwable)
	{
		Gdx.app.error(TAG, "Could not load asset: " + asset.fileName, (Exception)throwable);
	}
}
