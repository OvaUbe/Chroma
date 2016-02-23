package com.ovaube.chroma.phonies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractPhony
{
	protected TextureRegion textureRegion;
	protected Vector2 position;
	
	public abstract void renderAndUpdate(SpriteBatch batch, float deltaTime);
}
