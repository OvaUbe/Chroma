package com.ovaube.chroma.flyweights;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;

public class FlyweightDamager extends AbstractFlyweight
{
	private ParticleEffect effectDamager;
	private final float radius = 0.92f;
	
	public FlyweightDamager(Vector2 defaultPosition)
	{
		this.position = defaultPosition;
		
		effectDamager = new ParticleEffect(Assets.instance.assetPFX.damager);	
		effectDamager.setPosition((position.x + radius) * Constants.PPM, (position.y + radius) * Constants.PPM);
		effectDamager.start();
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		effectDamager.update(deltaTime);
		batch.begin();
		
		effectDamager.draw(batch);
		
		batch.end();
		
		if(effectDamager.isComplete())
			effectDamager.reset();		
	}

}
