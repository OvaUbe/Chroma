package com.ovaube.chroma.GUI;

import java.util.Comparator;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ovaube.chroma.flyweights.FlyweightPlayer;
import com.ovaube.chroma.handlers.ClientUpdater;
import com.ovaube.chroma.handlers.ServerWorldController;
import com.ovaube.chroma.objects.Player;
import com.ovaube.chroma.util.Assets;
import com.ovaube.chroma.util.PlayerColor;

public class HUD 
{
	private BitmapFont fontWhite = Assets.instance.assetFonts.white;
	private BitmapFont fontRed = Assets.instance.assetFonts.red;
	private BitmapFont fontGreen = Assets.instance.assetFonts.green;
	private BitmapFont fontBlue = Assets.instance.assetFonts.blue;
	
	private TextureRegion textureJoystickLeft = Assets.instance.assetTextures.joystickLeft;
	private TextureRegion textureJoystickRight = Assets.instance.assetTextures.joystickRight;
	private TextureRegion textureStick = Assets.instance.assetTextures.stick;
	private TextureRegion statsBlue = Assets.instance.assetTextures.statsBlue;
	private TextureRegion statsGreen = Assets.instance.assetTextures.statsGreen;
	private TextureRegion statsViolet = Assets.instance.assetTextures.statsViolet;
	
	private Player player;
	private FlyweightPlayer flyweightPlayer;
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	private ServerWorldController serverController;
	private ClientUpdater clientUpdater;
	
	private boolean isFlyweight;
	
	private Array<Player> stats = new Array<Player>();
	private Array<FlyweightPlayer> flyweightStats = new Array<FlyweightPlayer>();
	
	private Vector2 leftJoystickCenter = new Vector2();
	private Vector2 stickCenter = new Vector2();
	
	public HUD(Player player, ServerWorldController serverContriller, OrthographicCamera camera)
	{
		this.player = player;
		this.serverController = serverContriller;
		this.camera = camera;
		isFlyweight = false;
		
		leftJoystickCenter.set(camera.viewportHeight / 4, camera.viewportHeight / 4);
	}
	
	public HUD(FlyweightPlayer flyweightPlayer, ClientUpdater clientUpdater, OrthographicCamera camera)
	{
		this.flyweightPlayer = flyweightPlayer;
		this.clientUpdater = clientUpdater;
		this.camera = camera;
		isFlyweight = true;
		
		leftJoystickCenter.set(camera.viewportHeight / 4, camera.viewportHeight / 4);
	}
	
	public void render(SpriteBatch batch, Float roundTime)
	{
		this.batch = batch;
		
		batch.begin();
		
		/*
		fontWhite.draw(batch,
				Gdx.graphics.getFramesPerSecond() + "", 
				camera.viewportWidth * 5 / 10 , 
				camera.viewportHeight * 8 / 10);
		*/
		
		renderHealth();
		
		renderTime(roundTime);
		
		renderJoysticks();
		
		renderStats();
		
		renderMessages();
		
		batch.end();
	}
	
	private void renderHealth()
	{
		if(isFlyweight)
		{
			if(flyweightPlayer.getHealth() > 15)
				fontWhite.draw(batch, flyweightPlayer.getHealth().toString(), 
					camera.viewportWidth * 1 / 15, 
					camera.viewportHeight * 14 / 15);
			else
				fontRed.draw(batch, flyweightPlayer.getHealth().toString(), 
					camera.viewportWidth * 1 / 15, 
					camera.viewportHeight * 14 / 15);
		}
		else
		{
			if(player.getHealth() > 15)
				fontWhite.draw(batch, player.getHealth().toString(), 
					camera.viewportWidth * 1 / 15, 
					camera.viewportHeight * 14 / 15);
			else
				fontRed.draw(batch, player.getHealth().toString(), 
					camera.viewportWidth * 1 / 15, 
					camera.viewportHeight * 14 / 15);
		}
	}
	
	private void renderTime(float time)
	{
		// Format time
		int minutes = (int)(time / 60);
		int seconds = (int)time - ((int)(time / 60))*60;
		
		if(minutes > 0)
		{
			fontWhite.draw(batch, minutes + ":" + seconds, 
				camera.viewportWidth * 1 / 2 - 170, 
				camera.viewportHeight * 1 / 15);
		}
		else
		{
			if(seconds > 15)
				fontBlue.draw(batch, minutes + ":" + seconds, 
						camera.viewportWidth * 1 / 2 - 170, 
						camera.viewportHeight * 1 / 15);
			else
				fontRed.draw(batch, minutes + ":" + seconds, 
						camera.viewportWidth * 1 / 2 - 170, 
						camera.viewportHeight * 1 / 15);
				
		}
	}
	
	private void renderJoysticks()
	{
		batch.draw(textureJoystickLeft, 
				leftJoystickCenter.x - textureJoystickLeft.getRegionHeight(), 
				leftJoystickCenter.y - textureJoystickLeft.getRegionHeight(),
				textureJoystickLeft.getRegionWidth() * 2,
				textureJoystickLeft.getRegionHeight() * 2);
		
		batch.draw(textureJoystickRight, 
				camera.viewportWidth - (camera.viewportHeight / 4) - textureJoystickRight.getRegionWidth(), 
				camera.viewportHeight / 4 - textureJoystickRight.getRegionHeight(),
				textureJoystickRight.getRegionWidth() * 2,
				textureJoystickRight.getRegionHeight() * 2);
		
		for(int i = 0; i != 3; i++)
		{
			if(Gdx.input.isTouched(i))
			{
				// There is a difference between our viewport and Gdx.graphics viewport (Meh.)
				// And inverted y-axis! (Damn it..)
				float fixedX = (camera.viewportWidth / Gdx.graphics.getWidth()) * Gdx.input.getX(i);
				float fixedY = (camera.viewportHeight / Gdx.graphics.getHeight()) * (Gdx.graphics.getHeight() - Gdx.input.getY(i));
				
				if(fixedX < camera.viewportWidth / 2)
				{
					stickCenter.set(fixedX, fixedY);
					// Clamp draw position
					stickCenter = stickCenter.sub(leftJoystickCenter).
							clamp(0f, textureStick.getRegionWidth() + textureJoystickLeft.getRegionWidth()).
								add(leftJoystickCenter);
					
					batch.draw(textureStick, 
							stickCenter.x - textureStick.getRegionWidth(), 
							stickCenter.y - textureStick.getRegionHeight(),
							textureStick.getRegionWidth() * 2,
							textureStick.getRegionHeight() * 2);
					break;
				}
				else if(Gdx.input.isTouched(i + 1))
					continue;
				else
				{
					batch.draw(textureStick, 
							leftJoystickCenter.x - textureStick.getRegionWidth(), 
							leftJoystickCenter.y - textureStick.getRegionHeight(),
							textureStick.getRegionWidth() * 2,
							textureStick.getRegionHeight() * 2);
					break;
				}
			}
			else
			{
				batch.draw(textureStick, 
						leftJoystickCenter.x - textureStick.getRegionWidth(), 
						leftJoystickCenter.y - textureStick.getRegionHeight(),
						textureStick.getRegionWidth() * 2,
						textureStick.getRegionHeight() * 2);
				break;
			}
		}
	}
	
	private void renderStats()
	{
		// Sorted output
		if(isFlyweight)
		{
			for(Map.Entry<PlayerColor, FlyweightPlayer> entry : clientUpdater.getPlayers().entrySet())
				flyweightStats.add(entry.getValue());
			
			flyweightStats.sort(new Comparator<FlyweightPlayer>() {
                                    @Override
                                    public int compare(FlyweightPlayer arg0, FlyweightPlayer arg1) {
                                        return (arg1.getKills() / arg1.getDeaths() - arg0.getKills() / arg0.getDeaths());
                                    }
                                }
			);
			
			fontWhite.setScale(1.3f, 1.3f);
			fontWhite.draw(batch, 
					"K:", 
					camera.viewportWidth * 8f / 10 , 
					camera.viewportHeight * 9.5f / 9.8f);
			fontWhite.draw(batch, 
					"D:", 
					camera.viewportWidth * 9f / 10 , 
					camera.viewportHeight * 9.5f / 9.8f);
			
			float i = 9f;
			for(FlyweightPlayer player : flyweightStats)
			{
				switch(player.getColor())
				{
				case BLUE:
					batch.draw(statsBlue, 
							camera.viewportWidth * 7.5f / 10 , 
							camera.viewportHeight * i / 10);
					break;
				case GREEN:
					batch.draw(statsGreen, 
							camera.viewportWidth * 7.5f / 10 , 
							camera.viewportHeight * i / 10);
					break;
				case VIOLET:
					batch.draw(statsViolet, 
							camera.viewportWidth * 7.5f / 10 , 
							camera.viewportHeight * i / 10);
					break;
				}
				
				fontWhite.draw(batch, 
						String.valueOf(player.getKills()), 
						camera.viewportWidth * 8f / 10 , 
						camera.viewportHeight * i / 9.8f);
				fontWhite.draw(batch, 
						String.valueOf(player.getUnsafeDeaths()), 
						camera.viewportWidth * 9f / 10 , 
						camera.viewportHeight * i / 9.8f);
				
				 i = i - 0.5f;
			}
			
			fontWhite.setScale(2f, 2f);
			flyweightStats.clear();
		}
		else
		{
			for(Map.Entry<PlayerColor, Player> entry : serverController.getPlayers().entrySet())
				stats.add(entry.getValue());

            stats.sort(new Comparator<Player>() {
                                    @Override
                                    public int compare(Player arg0, Player arg1) {
                                        return (arg1.getKills() / arg1.getDeaths() - arg0.getKills() / arg0.getDeaths());
                                    }
                                }
            );

			fontWhite.setScale(1.3f, 1.3f);
			fontWhite.draw(batch, 
					"K:", 
					camera.viewportWidth * 8f / 10 , 
					camera.viewportHeight * 9.5f / 9.8f);
			fontWhite.draw(batch, 
					"D:", 
					camera.viewportWidth * 9f / 10 , 
					camera.viewportHeight * 9.5f / 9.8f);
			
			float i = 9f;
			for(Player player : stats)
			{
				switch(player.getColor())
				{
				case BLUE:
					batch.draw(statsBlue, 
							camera.viewportWidth * 7.5f / 10 , 
							camera.viewportHeight * i / 10);
					break;
				case GREEN:
					batch.draw(statsGreen, 
							camera.viewportWidth * 7.5f / 10 , 
							camera.viewportHeight * i / 10);
					break;
				case VIOLET:
					batch.draw(statsViolet, 
							camera.viewportWidth * 7.5f / 10 , 
							camera.viewportHeight * i / 10);
					break;
				}
				
				fontWhite.draw(batch, 
						String.valueOf(player.getKills()), 
						camera.viewportWidth * 8f / 10 , 
						camera.viewportHeight * i / 9.8f);
				fontWhite.draw(batch, 
						String.valueOf(player.getUnsafeDeaths()), 
						camera.viewportWidth * 9f / 10 , 
						camera.viewportHeight * i / 9.8f);
				
				 i = i - 0.5f;
			}
			
			fontWhite.setScale(2f, 2f);
			stats.clear();
		}
	}
	
	private void renderMessages()
	{
		if(isFlyweight)
		{
			if(flyweightPlayer.isTurretEnabled())
			{
				if(!flyweightPlayer.isTurret())
					fontGreen.draw(batch, "SET TURRET", 
						camera.viewportWidth * 4 / 7, 
						camera.viewportHeight * 2 / 3);
				else
					fontBlue.draw(batch, "SET MOBILE", 
							camera.viewportWidth * 4 / 7, 
							camera.viewportHeight * 2 / 3);
			}
		}
		else
		{
			if(player.isTurretEnabled())
			{
				if(!player.isTurret())
					fontGreen.draw(batch, "SET TURRET", 
						camera.viewportWidth * 4 / 7, 
						camera.viewportHeight * 2 / 3);
				else
					fontBlue.draw(batch, "SET MOBILE", 
							camera.viewportWidth * 4 / 7, 
							camera.viewportHeight * 2 / 3);
			}
		}
	}	
}
