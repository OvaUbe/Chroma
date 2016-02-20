package com.ovaube.chroma.flyweights;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;

public class FlyweightHealer extends AbstractFlyweight
{
	private ParticleEffect effectHealer;
	private final float radius = 0.6f;
	
	public FlyweightHealer(Vector2 defaultPosition)
	{
		this.position = defaultPosition;
		
		effectHealer = new ParticleEffect(Assets.instance.assetPFX.healer);	
		effectHealer.setPosition((position.x + radius) * Constants.PPM, (position.y + radius) * Constants.PPM);
		effectHealer.start();
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		effectHealer.update(deltaTime);
		batch.begin();
		
		effectHealer.draw(batch);
		
		batch.end();
		
		if(effectHealer.isComplete())
			effectHealer.reset();		
	}

}
