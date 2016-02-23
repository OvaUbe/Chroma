package com.ovaube.chroma.objects;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;

public class Damager extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 4);
	
	private ParticleEffect effectDamager;
	private final float radius = 0.92f;

	public Damager(Vector2 defaultPosition, World world)
	{
		super(defaultPosition, world);

		init();
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{		
		effectDamager.update(deltaTime);
		effectDamager.draw(batch);
		
		if(effectDamager.isComplete())
			effectDamager.reset();
	}

	@Override
	protected void setTexturesAndEffects() 
	{
		effectDamager = new ParticleEffect(Assets.instance.assetPFX.damager);	
		effectDamager.setPosition((defaultPosition.x + radius) * Constants.PPM,
				  				  (defaultPosition.y + radius) * Constants.PPM);
		effectDamager.start();
	}

	@Override
	protected void createBody() 
	{
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(new Vector2(defaultPosition.x + radius, defaultPosition.y + radius));	
		
		CircleShape shape = new CircleShape();
		shape.setRadius(radius);
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		// Sensor!
		fixtureDef.isSensor = true;
		fixtureDef.density = 1;
		fixtureDef.friction = 0;
		fixtureDef.filter.categoryBits = BIT;
		fixtureDef.filter.maskBits = Player.BIT;
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);
		
		shape.dispose();
		
	}
}
