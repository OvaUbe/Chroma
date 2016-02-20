package com.ovaube.chroma.util;

public class Constants 
{
	private Constants(){}
	
	// Pixels per Meter (for B2D world)
	public static final float PPM = 100f;	
	
	public static final float STEP = 1 / 60f;
	public static final float MAX_PLAYER_VELOCITY = 6f;
	public static final float MAX_BULLET_VELOCITY = 23f;
	public static final float MAX_PLAYER_ANGULAR_VELOCITY = 1.5f;
	public static final float MAX_COOLDOWN = 0.2f;
	public static final float MAX_ROUND_TIME = 900f;
	public static final float MAX_SPAWN_TIMEOUT = 6f;
	public static final float MAX_PICKUP_TIMEOUT = 20f;
	public static final float MAX_BONUS_FAST_COOLDOWN_TIMEOUT = 15f;
	public static final float MAX_BONUS_SHIELD_TIMEOUT = 10f;
	// Radius in B2D Units
	public static final float PLAYER_RADIUS = 0.7f;
	
	public static final float VIEWPORT_WIDTH = 1.4f * 1960f;
	public static final float VIEWPORT_HEIGHT = 1.4f * 1080f;
	
	public static final int BULLET_LIFECYCLE = 3;
	public static final int BULLET_DAMAGE = 13;
	public static final int DAMAGER_DAMAGE = 4;
	public static final int HEALER_HEAL = 2;
	
	public static final String TEXTURE_ATLAS_MAIN = "texturepacks/gamePack.pack";
	public static final String TEXTURE_ATLAS_GUI = "texturepacks/guiPack.pack";	
	public static final String MAP = "tiledmap/testmap.tmx";
}
