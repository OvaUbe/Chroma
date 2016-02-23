package com.ovaube.chroma.objects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PlayerColor;

public class Player extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 1);
	
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
	
	private boolean isInvictible;
	private boolean isTurret;
	private boolean isDead;
	private boolean isEnded;
	private boolean enableTurret;
	private boolean isHealing;
	private boolean isDamaging;
	private boolean isFastCooldown;
	private boolean isBurning;
	private boolean justSpawned;
	private boolean justBecameTurret;
	
	private int health;
	private int kills;
	private int deaths;
	
	private float coolDown;
	private float spawnTimeout;
	private float healTimeout;
	private float damageTimeout;
	private float fastCooldownTimeout;
	private float shieldTimeout;
	private float coolDownMax;
	
	private String nickname;
	
	private Healer healer;
	
	private Array<Vector2> spawnPoints = new Array<Vector2>();
	
	public Player(Vector2 defaultPosition, World world, PlayerColor color, String nickname, Array<Vector2> spawnPoints)
	{
		super(defaultPosition, world);

		this.color = color;
		this.nickname = nickname;
		this.spawnPoints.addAll(spawnPoints);

		init();
		
		spawnTimeout = Constants.MAX_SPAWN_TIMEOUT;
		healTimeout = Constants.MAX_COOLDOWN;
		damageTimeout = Constants.MAX_COOLDOWN;
		fastCooldownTimeout = Constants.MAX_BONUS_FAST_COOLDOWN_TIMEOUT;

		setTurretFixture();
		
		kills = 0;
		deaths = 0;
		
		// Respawn
		reinit();
	}
	
	public void reinit()
	{
		isTurret = false;
		isInvictible = true;
		isDead = false;
		isEnded = false;
		enableTurret = false;
		isHealing = false;
		isDamaging = false;
		isFastCooldown = false;
		isBurning = false;
		
		coolDownMax = Constants.MAX_COOLDOWN;
		// Invictible 1.5 sec after spawn
		shieldTimeout = 1.5f;
		
		health = 100;
		coolDown = 0f;
		
		// Random spawn point
		body.setTransform(spawnPoints.get(MathUtils.random(2)), 0f);
		// Turret fixture must be sensor on spawn
		body.getFixtureList().get(1).setSensor(true);
		body.setActive(true);
		body.setLinearVelocity(0f, 0f);
		body.setAngularVelocity(0f);;
		
		justSpawned = true;
	}
	
	public void move(Vector2 force)
	{
		// Apply force to front
		if(!isTurret)
			body.applyForce(force, body.getWorldPoint(getFrontCoordinates()), true);
		
		// Rotate while player's front isn't pointed to force direction
		// Angle between force vector and front vector is responsible for this
		float angle = getFrontCoordinates().angle(force) * 3;
		float torque = 150f;
		body.applyTorque(angle * torque, true);
		
		// Clamp some stuff
		body.setLinearVelocity(
				MathUtils.clamp(body.getLinearVelocity().x,
						-Constants.MAX_PLAYER_VELOCITY,
						Constants.MAX_PLAYER_VELOCITY),
				MathUtils.clamp(body.getLinearVelocity().y,
						-Constants.MAX_PLAYER_VELOCITY,
						Constants.MAX_PLAYER_VELOCITY));
	}
	
	public boolean salvoTimeout(float deltaTime)
	{
		if(coolDown <= 0)
		{
			coolDown = coolDownMax;
			return true;
		}
		else
		{
			coolDown -= deltaTime;
			return false;
		}
	}
	
	public boolean spawnTimeout(float deltaTime)
	{
		if(spawnTimeout <= 0)
		{
			spawnTimeout = Constants.MAX_SPAWN_TIMEOUT;
			return true;
		}
		else
		{
			spawnTimeout -= deltaTime;
			return false;
		}
	}
	
	public boolean healTimeout(float deltaTime)
	{
		if(healTimeout <= 0)
		{
			healTimeout = Constants.MAX_COOLDOWN;
			return true;
		}
		else
		{
			healTimeout -= deltaTime;
			return false;
		}
	}
	
	public boolean damageTimeout(float deltaTime)
	{
		if(damageTimeout <= 0)
		{
			damageTimeout = Constants.MAX_COOLDOWN;
			return true;
		}
		else
		{
			damageTimeout -= deltaTime;
			return false;
		}
	}

	public boolean fastCooldownTimeout(float deltaTime)
	{
		if(fastCooldownTimeout <= 0)
		{
			fastCooldownTimeout = Constants.MAX_BONUS_FAST_COOLDOWN_TIMEOUT;
			return true;
		}
		else
		{
			fastCooldownTimeout -= deltaTime;
			return false;
		}
	}
	
	public boolean shieldTimeout(float deltaTime)
	{
		if(shieldTimeout <= 0)
		{
			shieldTimeout = Constants.MAX_BONUS_SHIELD_TIMEOUT;
			return true;
		}
		else
		{
			shieldTimeout -= deltaTime;
			return false;
		}
	}
	
	public Vector2 getFrontCoordinates()
	{
		return new Vector2(Constants.PLAYER_RADIUS * (float)Math.cos(body.getAngle()),
				Constants.PLAYER_RADIUS * (float)Math.sin(body.getAngle()));
	}
	
	public Vector2 getBottomCornerCoordinates()
	{
		return new Vector2(Constants.PLAYER_RADIUS * (float)Math.cos(body.getAngle() - (Math.PI * 2 / 3)),
				Constants.PLAYER_RADIUS * (float)Math.sin(body.getAngle() - (Math.PI * 2 / 3)));
	}
	
	public Vector2 getUpperCornerCoordinates()
	{
		return new Vector2(Constants.PLAYER_RADIUS * (float)Math.cos(body.getAngle() + (Math.PI * 2 / 3)),
				Constants.PLAYER_RADIUS * (float)Math.sin(body.getAngle() + (Math.PI * 2 / 3)));
	}
	
	public void setHealer(Healer healer)
	{
		this.healer = healer;
	}
	
	public Healer getHealer()
	{
		return healer;
	}
	
	public void startHeal()
	{
		isHealing = true;
	}
	
	public void stopHeal()
	{
		isHealing = false;
	}
	
	public boolean isHealing()
	{
		return isHealing;
	}
	
	public void startDamage()
	{
		isDamaging = true;
	}
	
	public void stopDamage()
	{
		isDamaging = false;
	}
	
	public boolean isDamaging()
	{
		return isDamaging;
	}
	
	public void setInvictible()
	{
		isInvictible = true;
		
		effectShield.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
		effectShield.start();
	}
	
	public void unsetInvictible()
	{
		isInvictible = false;
	}
	
	public boolean isInvictible()
	{
		return isInvictible;
	}
	
	public void setFastCooldown()
	{
		coolDownMax = Constants.MAX_COOLDOWN / 2;
		isFastCooldown = true;
		
		effectFastCooldown.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
		effectFastCooldown.start();
	}
	
	public void unsetFastCooldown()
	{
		coolDownMax = Constants.MAX_COOLDOWN;
		isFastCooldown = false;
	}
	
	public boolean isFastCooldown()
	{
		return isFastCooldown;
	}
	
	public boolean isBurning()
	{
		return isBurning;
	}
	
	public void setTurret()
	{
		isTurret = true;
		body.getFixtureList().get(1).setSensor(false);
		
		body.setTransform(healer.getBody().getPosition(), body.getAngle());
		body.setLinearVelocity(0, 0);
		
		effectBecomeTurret.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
		effectBecomeTurret.start();
		
		healer.setOccupied();
		
		justBecameTurret = true;
	}
	
	public void unsetTurret()
	{
		isTurret = false;
		body.getFixtureList().get(1).setSensor(true);
		
		healer.setEmpty();
	}
	
	public void enableTurret()
	{
		enableTurret = true;
	}
	
	public void disableTurret()
	{
		enableTurret = false;
	}
	
	public boolean isTurretEnabled()
	{
		return enableTurret;
	}
	
	public boolean isTurret()
	{
		return isTurret;
	}
	
	public boolean getFlagJustBecameTurret()
	{
		return justBecameTurret;
	}
	
	public boolean getFlagJustSpawned()
	{
		return justSpawned;
	}

	public void decreaseHealth(int amount)
	{
		if(!isInvictible)
			health = MathUtils.clamp(health - amount, 0, 100);
		if(health < 15)
		{
			isBurning = true;
			effectBurning.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
			effectBurning.start();
		}
	}
	
	public void increaseHealth(int amount)
	{
		if(!isDead)
		{
			health = MathUtils.clamp(health + amount, 0, 100);
			if(health > 15)
				isBurning = false;
		}
	}
	
	
	public Integer getHealth()
	{
		return health;
	}
	
	public void increaseKills()
	{
		this.kills++;
	}
	
	public Integer getKills()
	{
		return kills;
	}
	
	public void increaseDeaths()
	{
		this.deaths++;
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
	
	public PlayerColor getColor()
	{
		return color;
	}
	
	public String getNickname()
	{
		return nickname;
	}
	
	public boolean isDead()
	{
		return isDead;
	}
	
	public boolean isEnded()
	{
		return isEnded;
	}
	
	private void setDead()
	{
		effectExplosion.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
		effectExplosion.start();

		// DEAD!
		isDead = true;
		increaseDeaths();
		// Ghost
		body.setActive(false);
		// Free healer
		if(isTurret)
		{
			healer.setEmpty();
			isHealing = false;
		}
		
		soundExplosion.play(ChromaPreferences.instance.volumeSound);
	}
	
	public String toString()
	{
		switch(color)
		{
		case BLUE: 
			return "blue";
		case GREEN: 
			return "green";
		case VIOLET: 
			return "violet";
		default:
			return "default";	
		}
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime)
	{		
		if(justSpawned)
		{
			soundSpawn.play(ChromaPreferences.instance.volumeSound);
			justSpawned = false;
		}
		
		if(justBecameTurret)
		{
			soundBecomeTurret.play(ChromaPreferences.instance.volumeSound);
			justBecameTurret = false;
		}
		
		if(isDamaging)
		{
			soundDamage.play(ChromaPreferences.instance.volumeSound);
		}

		if(health != 0)
		{
			batch.draw(textureRegion, 
					body.getPosition().x * Constants.PPM - textureRegion.getRegionWidth() / 2, 
					body.getPosition().y * Constants.PPM - textureRegion.getRegionHeight() / 2,
					textureRegion.getRegionWidth() / 2,
					textureRegion.getRegionHeight() / 2,
					textureRegion.getRegionWidth(),
					textureRegion.getRegionHeight(),
					1f, 1f,
					body.getAngle() * MathUtils.radiansToDegrees);
			
			if(isInvictible)
			{
				effectShield.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
				effectShield.update(deltaTime);
				effectShield.draw(batch);
				
				if(effectShield.isComplete())
					effectShield.reset();
			}
			
			if(isFastCooldown)
			{
				effectFastCooldown.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
				effectFastCooldown.update(deltaTime);
				effectFastCooldown.draw(batch);
				
				if(effectFastCooldown.isComplete())
					effectFastCooldown.reset();
			}
			
			if(isDamaging)
			{
				effectDamage.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
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
						body.getPosition().x * Constants.PPM - textureTurret.getRegionWidth() / 2, 
						body.getPosition().y * Constants.PPM - textureTurret.getRegionHeight() / 2,
						textureTurret.getRegionWidth() / 2,
						textureTurret.getRegionHeight() / 2,
						textureTurret.getRegionWidth(), 
						textureTurret.getRegionHeight(), 
						1f, 1f, 
						body.getAngle() * MathUtils.radiansToDegrees);	
			}
			
			if(isBurning && !isInvictible)
			{
				effectBurning.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
				effectBurning.update(deltaTime);
				effectBurning.draw(batch);
				
				if(effectBurning.isComplete())
					effectBurning.reset();
			}
		}
		else
		{
			if(!isDead)
				setDead();
			// Render particles after death
			else
			{
				effectExplosion.update(deltaTime);			
				effectExplosion.draw(batch);
				
				if(effectExplosion.isComplete())
					isEnded = true;
			}
		}
	}

	@Override
	protected void setTexturesAndEffects() 
	{
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

	@Override
	protected void createBody() 
	{
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(defaultPosition);	
		
		PolygonShape shape = new PolygonShape();
		shape.set(new Vector2[] { 
				new Vector2(-50f / Constants.PPM, -60f / Constants.PPM), 
				new Vector2(-50f / Constants.PPM, 60f / Constants.PPM), 
				new Vector2(70f / Constants.PPM, 0f) 
				} );
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 100;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 0;
		fixtureDef.filter.categoryBits = BIT;
		fixtureDef.filter.maskBits = 
				(short) (Block.BIT |
				Bullet.BIT |
				Damager.BIT |
				Healer.BIT |
				Player.BIT |
				Pickup.BIT |
				Wall.BIT);
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);
		
		shape.dispose();
	}
	
	private void setTurretFixture()
	{
		PolygonShape turretShape = new PolygonShape();
		
		Vector2[] verticles = new Vector2[3];
		verticles[0] = new Vector2(
				((float)textureTurret.getRegionWidth() / Constants.PPM) * 3 / 8, 
				((float)textureTurret.getRegionWidth() / Constants.PPM) / 2 + 0.2f);
		verticles[1] = new Vector2(
				((float)textureTurret.getRegionWidth() / Constants.PPM) * 3 / 8, 
				(-(float)textureTurret.getRegionWidth() / Constants.PPM) / 2 - 0.2f);
		verticles[2] = new Vector2(
				((float)textureTurret.getRegionWidth() / Constants.PPM) / 2, 
				0.2f);
		
		turretShape.set(verticles);
		
		FixtureDef turretFixtureDef = new FixtureDef();
		turretFixtureDef.shape = turretShape;
		turretFixtureDef.isSensor = true;
		turretFixtureDef.density = 1;
		turretFixtureDef.friction = 0;
		turretFixtureDef.filter.categoryBits = fixtureDef.filter.categoryBits;
		turretFixtureDef.filter.maskBits = fixtureDef.filter.maskBits;
		
		body.createFixture(turretFixtureDef).setUserData(this.toString() + "_turret");
		
		turretShape.dispose();
	}
}
