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

public class Healer extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 5);

	private ParticleEffect effectHealer;
	private final float radius = 0.85f;
	
	private boolean isEmpty;

	public Healer(Vector2 defaultPosition, World world)
	{
		super(defaultPosition, world);
		
		isEmpty = true;
		
		createBody();
		setTexturesAndEffects();
	}
	
	public boolean isEmpty()
	{
		return isEmpty;
	}
	
	public void setEmpty()
	{
		isEmpty = true;
	}
	
	public void setOccupied()
	{
		isEmpty = false;
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

	@Override
	protected void setTexturesAndEffects() 
	{
		effectHealer = new ParticleEffect(Assets.instance.assetPFX.healer);	
		effectHealer.setPosition(body.getPosition().x * Constants.PPM, body.getPosition().y * Constants.PPM);
		effectHealer.start();
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
