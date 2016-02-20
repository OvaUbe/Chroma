package com.ovaube.chroma.networking;

import java.util.HashMap;

import com.ovaube.chroma.util.PlayerColor;

public class PacketWorldInfo 
{
	public HashMap<Integer, BulletInfo> bulletsInfo;
	public HashMap<PlayerColor, PlayerInfo> playersInfo;
	public PickupInfo pickupInvictusInfo;
	public PickupInfo pickupWeaponInfo;
	public float currentTime;
	
	public static class BulletInfo
	{
		public float positionX;
		public float positionY;
		public boolean isDead;
		public boolean justFired;
		public int lifeCycle;
		public PlayerColor color;
	}
	
	public static class PlayerInfo
	{
		public float positionX;
		public float positionY;
		public boolean isInvictible;
		public boolean isTurret;
		public boolean isDead;
		public boolean isFastCooldown;
		public boolean isBurning;	
		public boolean isTurretEnabled;
		public boolean isDamaging;
		public boolean justSpawned;
		public boolean justBecameTurret;	
		public int health;	
		public int kills;
		public int deaths;
		public float currentAngle;
	}
	
	public static class PickupInfo
	{
		public boolean isNull;	
		public float positionX;
		public float positionY;
		public boolean isPickedUp;
		public boolean isDead;
	}
}
