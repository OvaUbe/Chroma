package com.ovaube.chroma.phonies;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;

public class PhonyDamager extends AbstractPhony
{
	private ParticleEffect effectDamager;
	private final float radius = 0.92f;
	
	public PhonyDamager(Vector2 defaultPosition)
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
		effectDamager.draw(batch);
		
		if(effectDamager.isComplete())
			effectDamager.reset();		
	}

}
