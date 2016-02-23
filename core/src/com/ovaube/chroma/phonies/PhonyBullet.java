package com.ovaube.chroma.phonies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PlayerColor;

public class PhonyBullet extends AbstractPhony
{	
	private ParticleEffect effectBulletDeath;
	
	private Sound soundHit;
	private Sound soundSalvo;
	
	private boolean isDead;
	private boolean justFired;
	private int lifeCycle;
	
	public PhonyBullet(Vector2 position, PlayerColor color)
	{
		this.position = position;
		isDead = false;
		justFired = true;
		lifeCycle = Constants.BULLET_LIFECYCLE;
		
		switch(color)
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
	
	public void setFlagIsDead(boolean isDead)
	{
		this.isDead = isDead;
	}
	
	public void setFlagJustFired(boolean justFired)
	{
		this.justFired = justFired;
	}
	
	public void setLifeCycle(int lifeCycle)
	{
		this.lifeCycle = lifeCycle;
	}
	
	public void setPosition(float positionX, float positionY)
	{
		this.position.set(positionX, positionY);
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		if(justFired)
		{
			soundSalvo.play();
			justFired = false;
		}
		
		if(lifeCycle > 0)
		{
			batch.draw(textureRegion,
					position.x * Constants.PPM - textureRegion.getRegionWidth() / 2, 
					position.y * Constants.PPM - textureRegion.getRegionHeight() / 2);
		}
		else
		{
			if(!isDead)
			{
				effectBulletDeath.setPosition(
						position.x * Constants.PPM, 
						position.y * Constants.PPM);
				effectBulletDeath.start();
				// DEAD!
				isDead = true;
				
				soundHit.play();
			}
			// Render particles after death
			else
			{
				effectBulletDeath.update(deltaTime);		
				effectBulletDeath.draw(batch);
			}
		}
	}
}
