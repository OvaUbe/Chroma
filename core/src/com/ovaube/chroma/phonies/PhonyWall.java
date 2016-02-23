package com.ovaube.chroma.phonies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.Constants;

public class PhonyWall extends AbstractPhony
{
	// Position of right top corner
	public PhonyWall(Vector2 position)
	{
		this.position = position;
		
		textureRegion = Assets.instance.assetTextures.bg;
	}
	
	@Override
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		batch.disableBlending();
		batch.draw(textureRegion, 0f, 0f, 
				position.x * Constants.PPM, 
				position.y * Constants.PPM);
		batch.enableBlending();
	}

}
