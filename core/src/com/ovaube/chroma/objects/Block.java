package com.ovaube.chroma.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.BlockType;
import com.ovaube.chroma.util.Constants;

public class Block extends AbstractObject
{
	public static final short BIT = (short)Math.pow(2, 2);

	private Vector2 size;
	private BlockType type;
	
	public Block(Vector2 defaultPosition, World world, Vector2 size, BlockType type)
	{
		super(defaultPosition, world);
		this.size = new Vector2(size);
		this.type = type;
		
		setTexturesAndEffects();
		createBody();
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		batch.begin();
		
		batch.draw(textureRegion, 
				(body.getPosition().x - (size.x / 2)) * Constants.PPM, 
				(body.getPosition().y - (size.y / 2)) * Constants.PPM, 
				size.x * Constants.PPM, 
				size.y * Constants.PPM);
		
		batch.end();
	}

	@Override
	protected void setTexturesAndEffects() 
	{
		if(type == BlockType.CIRCLE)
		{
			if(size.x < 1.5f)
				textureRegion = Assets.instance.assetTextures.blockCircleSmall;	
			else if(size.x > 10f)
				textureRegion = Assets.instance.assetTextures.blockCircleBig;	
			else
				textureRegion = Assets.instance.assetTextures.blockCircleMedium;	
		}
		if(type == BlockType.SQUARE)
			textureRegion = Assets.instance.assetTextures.blockSquare;	
	}

	@Override
	protected void createBody() 
	{
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(new Vector2(defaultPosition.x + (size.x / 2), defaultPosition.y + (size.y / 2)));	
		
		fixtureDef = new FixtureDef();
		
		if(type == BlockType.CIRCLE)
		{
			CircleShape shape = new CircleShape();
			shape.setRadius(size.x / 2);
			fixtureDef.shape = shape;
		}
		if(type == BlockType.SQUARE)
		{
			PolygonShape shape = new PolygonShape();	
			shape.setAsBox(size.x / 2, size.y / 2);
			fixtureDef.shape = shape;
		}
		
		fixtureDef.density = 1;
		fixtureDef.friction = 0;
		fixtureDef.filter.categoryBits = BIT;
		fixtureDef.filter.maskBits = 
				(short) (Bullet.BIT |
				Player.BIT);
		
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);	
	}
}
