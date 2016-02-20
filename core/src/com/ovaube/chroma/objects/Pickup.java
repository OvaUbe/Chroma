package com.ovaube.chroma.objects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.ChromaPreferences;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PickupType;

public class Pickup extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 6);
	
	private PickupType pickupType;
	
	private ParticleEffect effectPickup;
	private Sound soundPickup;
	
	private boolean isPickedUp;
	private boolean isEnded;
	private boolean isDead;
	private boolean isDeleted;
	
	public Pickup(Vector2 defaultPosition, World world, PickupType pickupType) 
	{
		super(defaultPosition, world);
		
		this.pickupType = pickupType;
		
		isPickedUp = false;
		isEnded = false;
		isDead = false;
		isDeleted = false;
		
		setTexturesAndEffects();
		createBody();
	}
	
	public boolean isPickedUp()
	{
		return isPickedUp;
	}
	
	public void setPickedUp()
	{
		isPickedUp = true;
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
	
	public boolean isDead()
	{
		return isDead;
	}
	
	public PickupType getType()
	{
		return pickupType;
	}

	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		batch.begin();
		
		if(!isPickedUp)
		{
			batch.draw(textureRegion, 
					body.getPosition().x * Constants.PPM - textureRegion.getRegionWidth() / 2, 
					body.getPosition().y * Constants.PPM - textureRegion.getRegionHeight() / 2);
		}
		else
		{
			if(!isDead)
			{
				soundPickup.play(ChromaPreferences.instance.volumeSound);
				
				effectPickup.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
				effectPickup.start();
				
				isDead = true;
			}
			// Render particles after pickup
			else
			{
				effectPickup.update(deltaTime);		
				effectPickup.draw(batch);
				
				if(effectPickup.isComplete())
					isEnded = true;
			}
		}
		batch.end();
	}

	@Override
	protected void setTexturesAndEffects() 
	{
		switch(pickupType)
		{
		case INVICTUS:
			textureRegion = Assets.instance.assetTextures.powerupInvictus;
			soundPickup = Assets.instance.assetSFX.pickupInvictus;
			break;
		case WEAPON:
			textureRegion = Assets.instance.assetTextures.powerupWeapon;
			soundPickup = Assets.instance.assetSFX.pickupWeapon;
			break;
		}
		
		effectPickup = new ParticleEffect(Assets.instance.assetPFX.pickup);	
	}

	@Override
	protected void createBody() 
	{
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(defaultPosition);
		
		CircleShape shape = new CircleShape();
		shape.setRadius(textureRegion.getRegionWidth() / Constants.PPM);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0;
		fixtureDef.isSensor = true;
		fixtureDef.filter.categoryBits = BIT;
		fixtureDef.filter.maskBits = Player.BIT;
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);
		
		shape.dispose();
	}
}
