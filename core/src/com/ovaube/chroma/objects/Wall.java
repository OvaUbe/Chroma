package com.ovaube.chroma.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;

public class Wall extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 8);

	public Wall(Vector2 defaultPosition, World world) 
	{
		super(defaultPosition, world);
		
		init();
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		batch.disableBlending();
		batch.draw(textureRegion, 0f, 0f,
				defaultPosition.x * Constants.PPM,
				defaultPosition.y * Constants.PPM);
		batch.enableBlending();
	}

	@Override
	protected void setTexturesAndEffects() 
	{
		textureRegion = Assets.instance.assetTextures.bg;
	}

	@Override
	protected void createBody() 
	{
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0f, 0f);
		
		// Default position here means the position of top right corner of the wall
		ChainShape shape = new ChainShape();
		shape.createLoop(new Vector2[] {
			new Vector2(0f, 0f),
			new Vector2(0f, defaultPosition.y),
			new Vector2(defaultPosition.x, defaultPosition.y),
			new Vector2(defaultPosition.x, 0f)} );
		
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.filter.categoryBits = BIT;
		fixtureDef.filter.maskBits = (short)(Player.BIT | Bullet.BIT);
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);
		
		shape.dispose();
	}
}
