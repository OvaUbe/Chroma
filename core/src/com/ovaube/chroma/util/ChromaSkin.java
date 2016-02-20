package com.ovaube.chroma.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class ChromaSkin 
{
	public static ChromaSkin instance = new ChromaSkin(); 
	
	public Skin skin;
	
	public TextButtonStyle redButtonStyle;
	public TextButtonStyle whiteButtonStyle;
	public TextButtonStyle blueButtonStyle;
	
	public SliderStyle sliderStyle;
	
	public TextFieldStyle textFieldStyle;
	
	public LabelStyle greenLabelStyle;
	public LabelStyle redLabelStyle;
	public LabelStyle grayLabelStyle;
	
	private BitmapFont fontRed = Assets.instance.assetFonts.red;
	private BitmapFont fontGreen = Assets.instance.assetFonts.green;
	private BitmapFont fontWhite = Assets.instance.assetFonts.white;
	private BitmapFont fontBlue = Assets.instance.assetFonts.blue;
	private BitmapFont fontGray = Assets.instance.assetFonts.gray;
	
	private ChromaSkin() { }
	
	public void init()
	{
		skin = new Skin();
		
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		
		skin.add("white", new Texture(pixmap));	
		
		skin.add("red", fontRed);
		skin.add("white", fontWhite);
		skin.add("blue", fontBlue);
		skin.add("green", fontGreen);
		skin.add("gray", fontGray);
		
		redButtonStyle = new TextButtonStyle();
		redButtonStyle.down = skin.newDrawable("white", Color.MAROON);
		redButtonStyle.over = skin.newDrawable("white", Color.MAROON);
		redButtonStyle.font = skin.getFont("red");
		
		whiteButtonStyle = new TextButtonStyle();
		whiteButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		whiteButtonStyle.over = skin.newDrawable("white", Color.DARK_GRAY);
		whiteButtonStyle.font = skin.getFont("white");
		
		blueButtonStyle = new TextButtonStyle();
		blueButtonStyle.down = skin.newDrawable("white", Color.NAVY);
		blueButtonStyle.over = skin.newDrawable("white", Color.NAVY);
		blueButtonStyle.font = skin.getFont("blue");
		
		sliderStyle = new SliderStyle();
		skin.add("sliderKnob", Assets.instance.assetTextures.sliderKnob);
		sliderStyle.knob = skin.newDrawable("sliderKnob");
		sliderStyle.knob.setMinWidth(30f);
		skin.add("sliderBG", Assets.instance.assetTextures.sliderBG);
		sliderStyle.background = skin.newDrawable("sliderBG");
		
		textFieldStyle = new TextFieldStyle();
		textFieldStyle.font = fontGreen;
		textFieldStyle.fontColor = Color.GREEN;
		skin.add("cursor", Assets.instance.assetTextures.cursor);
		textFieldStyle.cursor = skin.newDrawable("cursor");
		textFieldStyle.cursor.setMinHeight(250);
		textFieldStyle.cursor.setMinWidth(3);
		
		grayLabelStyle = new LabelStyle();
		grayLabelStyle.font =  skin.getFont("gray");
		
		greenLabelStyle = new LabelStyle();
		greenLabelStyle.font =  skin.getFont("green");
		
		redLabelStyle = new LabelStyle();
		redLabelStyle.font =  skin.getFont("red");
	}
}
