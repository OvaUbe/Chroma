package com.ovaube.chroma.flyweights;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PickupType;

public class FlyweightPickup extends AbstractFlyweight
{
	private ParticleEffect effectPickup;
	private Sound soundPickup;
	
	private boolean isPickedUp;
	private boolean isDead;
	
	public FlyweightPickup(Vector2 position, PickupType pickupType)
	{
		this.position = position;
		isPickedUp = false;
		isDead = false;
		
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
	
	public void setFlagIsPickedUp(boolean isPickedUp)
	{
		this.isPickedUp = isPickedUp;
	}
	
	public void setFlagIsDead(boolean isDead)
	{
		this.isDead = isDead;
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		batch.begin();
		
		if(!isPickedUp)
		{
			batch.draw(textureRegion, 
					position.x * Constants.PPM - textureRegion.getRegionWidth() / 2, 
					position.y * Constants.PPM - textureRegion.getRegionHeight() / 2);
		}
		else
		{
			if(!isDead)
			{
				soundPickup.play();
				
				effectPickup.setPosition(position.x * Constants.PPM, position.y * Constants.PPM);
				effectPickup.start();
				
				isDead = true;
			}
			// Render particles after pickup
			else
			{
				effectPickup.update(deltaTime);		
				effectPickup.draw(batch);
			}
		}
		batch.end();
		
	}

}
