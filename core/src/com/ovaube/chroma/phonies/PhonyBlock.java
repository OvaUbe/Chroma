package com.ovaube.chroma.phonies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.BlockType;
import com.ovaube.chroma.util.Constants;

public class PhonyBlock extends AbstractPhony
{
	private Vector2 size;
	
	public PhonyBlock(Vector2 defaultPosition, Vector2 size, BlockType type)
	{
		this.position = defaultPosition;
		this.size = size;
		
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
	public void renderAndUpdate(SpriteBatch batch, float deltaTime) 
	{
		batch.draw(textureRegion, 
				position.x * Constants.PPM, 
				position.y * Constants.PPM, 
				size.x * Constants.PPM, 
				size.y * Constants.PPM);
	}
}
