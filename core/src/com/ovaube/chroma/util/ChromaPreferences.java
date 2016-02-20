package com.ovaube.chroma.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

public class ChromaPreferences 
{
	public static final ChromaPreferences instance = new ChromaPreferences();
	
	public float volumeSound;
	public float volumeMusic;
	public String nickname;
	
	private Preferences preferences;
	
	private ChromaPreferences() 
	{
		preferences = Gdx.app.getPreferences("chroma.prefs");
	}
	
	public void load() 
	{
		volumeSound = MathUtils.clamp(
				preferences.getFloat("volumeSound", 0.5f),
				0f, 1.0f);
		volumeMusic = MathUtils.clamp(
				preferences.getFloat("volumeMusic", 0.5f),
				0f, 1.0f);
		nickname = preferences.getString("nickname", "Player");
	}
	
	public void save()
	{
		preferences.putFloat("volumeSound", volumeSound);
		preferences.putFloat("volumeMusic", volumeMusic);
		preferences.putString("nickname", nickname);
		preferences.flush();
	}
}
