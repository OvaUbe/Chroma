package com.ovaube.chroma.objects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.Constants;

public class Bullet extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 3);
	
	private Player player;
	private ParticleEffect effectBulletDeath;
	
	private Sound soundHit;
	private Sound soundSalvo;
	
	private boolean isDead;
	private boolean isDeleted;
	private boolean isEnded;
	private boolean justFired;
	private int lifeCycle;
	
	public Bullet(Vector2 defaultPosition, World world, Player player) 
	{
		super(defaultPosition, world);		
		this.player = player;
		
		isDead = false;
		isEnded = false;
		isDeleted = false;
		
		lifeCycle = Constants.BULLET_LIFECYCLE;
		
		setTexturesAndEffects();
		createBody();
		
		justFired = true;
	}
	
	public void setHit()
	{
		lifeCycle--;
	}
	
	public boolean isDead()
	{
		return isDead;
	}
	
	public void setDead()
	{
		lifeCycle = -1;
	}
	
	public boolean isEnded()
	{
		return isEnded;
	}
	
	public void setDeleted()
	{
		isDeleted = true;
	}
	
	public boolean isDeleted()
	{
		return isDeleted;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public boolean getFlagJustFired()
	{
		return justFired;
	}
	
	public int getLifeCycle()
	{
		return lifeCycle;
	}

	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		if(justFired)
		{
			soundSalvo.play(ChromaPreferences.instance.volumeSound);
			justFired = false;
		}
		
		batch.begin();
		
		if(lifeCycle > 0)
		{
			batch.draw(textureRegion,
					body.getPosition().x * Constants.PPM - textureRegion.getRegionWidth() / 2, 
					body.getPosition().y * Constants.PPM - textureRegion.getRegionHeight() / 2);
		}
		else
		{
			if(!isDead)
			{
				effectBulletDeath.setPosition(
						body.getPosition().x * Constants.PPM, 
						body.getPosition().y * Constants.PPM);
				effectBulletDeath.start();
				// DEAD!
				isDead = true;
				
				soundHit.play(ChromaPreferences.instance.volumeSound);
			}
			// Render particles after death
			else
			{
				effectBulletDeath.update(deltaTime);
				
				effectBulletDeath.draw(batch);
				
				if(effectBulletDeath.isComplete())
					isEnded = true;
			}
		}
			
		batch.end();
	}

	@Override
	protected void setTexturesAndEffects() 
	{
		switch(player.getColor())
		{
		case GREEN: 
			textureRegion = Assets.instance.assetTextures.bulletGreen;
			effectBulletDeath = new ParticleEffect(Assets.instance.assetPFX.bulletDeathGreen);
			break;
		case BLUE: 
			textureRegion = Assets.instance.assetTextures.bulletBlue;
			effectBulletDeath = new ParticleEffect(Assets.instance.assetPFX.bulletDeathBlue);
			break;
		case VIOLET: 
			textureRegion = Assets.instance.assetTextures.bulletViolet;
			effectBulletDeath = new ParticleEffect(Assets.instance.assetPFX.bulletDeathViolet);
			break;
		}
		
		soundHit = Assets.instance.assetSFX.bulletDeath;
		soundSalvo = Assets.instance.assetSFX.salvo;
	}

	@Override
	protected void createBody() 
	{
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		// Set linear velocity vector from player front coord
		// And add some random
		Vector2 random = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f));
		bodyDef.linearVelocity.set(
				(new Vector2((Constants.MAX_BULLET_VELOCITY / Constants.PLAYER_RADIUS) * player.getFrontCoordinates().x,
						(Constants.MAX_BULLET_VELOCITY / Constants.PLAYER_RADIUS) * player.getFrontCoordinates().y)).add(random));
		// Set position from player front coord
		bodyDef.position.set(player.getBody().getPosition().add(player.getFrontCoordinates()));
		
		CircleShape shape = new CircleShape();
		shape.setRadius((textureRegion.getRegionWidth() / 2) / Constants.PPM);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 1;
		fixtureDef.filter.categoryBits = BIT;
		fixtureDef.filter.maskBits = 
				(short) (Block.BIT |
				Wall.BIT |
				Player.BIT);
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);
		
		shape.dispose();
	}
}
