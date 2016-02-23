package com.ovaube.chroma.phonies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PlayerColor;

public class PhonyPlayer extends AbstractPhony
{
	private PlayerColor color;
	
	private ParticleEffect effectBecomeTurret;
	private ParticleEffect effectExplosion;
	private ParticleEffect effectShield;
	private ParticleEffect effectFastCooldown;
	private ParticleEffect effectBurning;
	private ParticleEffect effectDamage;
	
	private Sound soundExplosion;
	private Sound soundSpawn;
	private Sound soundBecomeTurret;
	private Sound soundDamage;
	
	private TextureRegion textureTurret;
	
	private boolean isInvicible;
	private boolean isTurret;
	private boolean isDead;
	private boolean isFastCooldown;
	private boolean isBurning;	
	private boolean justSpawned;
	private boolean justBecameTurret;
	private boolean isTurretEnabled;
	private boolean isDamaging;
	
	private int health;
	private int kills;
	private int deaths;
	
	private String nickname;
	
	private float currentAngle;

	public PhonyPlayer(PlayerColor color, String nickname)
	{
		position = new Vector2();
		
		this.nickname = nickname;
		this.color = color;
		
		isTurret = false;
		isInvicible = true;
		isDead = false;
		isFastCooldown = false;
		isBurning = false;
		isDamaging = false;
		
		health = 100;
		kills = 0;
		deaths = 0;
		
		currentAngle = 0f;
		
		// Set textures		
		textureTurret = Assets.instance.assetTextures.turret;

		switch(color)
		{
		case BLUE: 
			textureRegion = Assets.instance.assetTextures.playerBlue;
			break;
		case GREEN: 
			textureRegion = Assets.instance.assetTextures.playerGreen;
			break;
		case VIOLET: 
			textureRegion = Assets.instance.assetTextures.playerViolet;
			break;
		}
		
		// Set effects			
		effectBecomeTurret = new ParticleEffect(Assets.instance.assetPFX.becomeTurret);				
		effectExplosion = new ParticleEffect(Assets.instance.assetPFX.explosion);
		effectShield = new ParticleEffect(Assets.instance.assetPFX.shield);
		effectFastCooldown = new ParticleEffect(Assets.instance.assetPFX.fastCooldown);
		effectBurning = new ParticleEffect(Assets.instance.assetPFX.burning);
		effectDamage = new ParticleEffect(Assets.instance.assetPFX.damage);
		
		soundExplosion = Assets.instance.assetSFX.explosion;
		soundSpawn = Assets.instance.assetSFX.spawn;
		soundBecomeTurret = Assets.instance.assetSFX.becomeTurret;
		soundDamage = Assets.instance.assetSFX.damage;
	}
	
	public void setPosition(float positionX, float positionY)
	{
		this.position.set(positionX, positionY);
	}
	
	public void setHealth(int health)
	{
		this.health = health;
	}
	
	public void setAngle(float angle)
	{
		this.currentAngle = angle;
	}
	
	public void setKills(int kills)
	{
		this.kills = kills;
	}
	
	public void setDeaths(int deaths)
	{
		this.deaths = deaths;
	}
	
	public void setFlagInvictible(boolean isInvictible)
	{
		this.isInvicible = isInvictible;
	}
	
	public void setFlagDamaging(boolean isDamaging)
	{
		this.isDamaging = isDamaging;
	}
	
	public void setFlagTurret(boolean isTurret)
	{
		this.isTurret = isTurret;
	}
	
	public void setFlagDead(boolean isDead)
	{
		this.isDead = isDead;
	}
	
	public void setFlagFastCooldown(boolean isFastCooldown)
	{
		this.isFastCooldown = isFastCooldown;
	}
	
	public void setFlagBurning(boolean isBurning)
	{
		this.isBurning = isBurning;
	}
	
	public void setFlagTurretEnabled(boolean isTurretEnabled)
	{
		this.isTurretEnabled = isTurretEnabled;
	}
	
	public void setFlagJustSpawned(boolean justSpawned)
	{
		this.justSpawned = justSpawned;
	}
	
	public void setFlagJustBecameTurret(boolean justBecameTurret)
	{
		this.justBecameTurret = justBecameTurret;
	}
	
	public Vector2 getPosition()
	{
		return position;
	}	
	
	public Integer getKills()
	{
		return kills;
	}	
	
	public Integer getDeaths()
	{
		if(deaths == 0)
			return 1;
		else
			return deaths;
	}
	
	public Integer getUnsafeDeaths()
	{
		return deaths;
	}
	
	public boolean isDead()
	{
		return isDead;
	}
	
	public boolean isTurretEnabled()
	{
		return isTurretEnabled;
	}
	
	public boolean isTurret()
	{
		return isTurret;
	}
	
	public Integer getHealth()
	{
		return health;
	}
	
	public String getNickname()
	{
		return nickname;
	}
	
	public PlayerColor getColor()
	{
		return color;
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		if(justSpawned)
		{
			soundSpawn.play();
			justSpawned = false;
		}
		
		if(justBecameTurret)
		{
			effectBecomeTurret.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
			effectBecomeTurret.start();
			
			soundBecomeTurret.play();
			justBecameTurret = false;
		}
		
		if(isDamaging)
		{
			soundDamage.play(ChromaPreferences.instance.volumeSound);
		}

		if(health != 0)
		{
			batch.draw(textureRegion, 
					position.x * Constants.PPM - textureRegion.getRegionWidth() / 2, 
					position.y * Constants.PPM - textureRegion.getRegionHeight() / 2,
					textureRegion.getRegionWidth() / 2,
					textureRegion.getRegionHeight() / 2,
					textureRegion.getRegionWidth(),
					textureRegion.getRegionHeight(),
					1f, 1f,
					currentAngle * MathUtils.radiansToDegrees);
			
			if(isInvicible)
			{
				effectShield.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
				effectShield.update(deltaTime);
				effectShield.draw(batch);
				
				if(effectShield.isComplete())
					effectShield.reset();
			}
			
			if(isFastCooldown)
			{
				effectFastCooldown.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
				effectFastCooldown.update(deltaTime);
				effectFastCooldown.draw(batch);
				
				if(effectFastCooldown.isComplete())
					effectFastCooldown.reset();
			}
			
			if(isDamaging)
			{
				effectDamage.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
				effectDamage.update(deltaTime);
				effectDamage.draw(batch);
				
				if(effectDamage.isComplete())
					effectDamage.reset();
			}
			
			if(isTurret)
			{
				effectBecomeTurret.update(deltaTime);				
				effectBecomeTurret.draw(batch);
				
				batch.draw(textureTurret, 
						position.x * Constants.PPM - textureTurret.getRegionWidth() / 2, 
						position.y * Constants.PPM - textureTurret.getRegionHeight() / 2,
						textureTurret.getRegionWidth() / 2,
						textureTurret.getRegionHeight() / 2,
						textureTurret.getRegionWidth(), 
						textureTurret.getRegionHeight(), 
						1f, 1f, 
						currentAngle * MathUtils.radiansToDegrees);	
			}
			
			if(isBurning && !isInvicible)
			{
				effectBurning.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
				effectBurning.update(deltaTime);
				effectBurning.draw(batch);
				
				if(effectBurning.isComplete())
					effectBurning.reset();
			}
		}
		else
		{
			if(!isDead)
			{
				effectExplosion.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
				effectExplosion.start();
				
				soundExplosion.play();
			}
			// Render particles after death
			else
			{
				effectExplosion.update(deltaTime);			
				effectExplosion.draw(batch);
			}
		}
	}
}
