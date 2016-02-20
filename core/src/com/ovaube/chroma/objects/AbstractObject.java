package com.ovaube.chroma.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public abstract class AbstractObject 
{
	protected World world;
	protected Vector2 defaultPosition;
	protected Body body;
	protected FixtureDef fixtureDef;
	protected BodyDef bodyDef;
	
	protected TextureRegion textureRegion;
	
	public AbstractObject(Vector2 defaultPosition, World world)
	{
		this.world = world;
		this.defaultPosition = defaultPosition;
	}
	
	protected abstract void setTexturesAndEffects();
	
	protected abstract void createBody();
	
	public abstract void renderAndUpdate(SpriteBatch batch, float deltaTime);
	
	public Body getBody()
	{
		return body;
	}
}
