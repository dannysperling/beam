package view;

import model.Menu;
import utilities.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;


public class DrawTitlescreen {

	private SpriteBatch batch;
	private Texture iconTexture;
	private Sprite iconSprite;
	
	private int timePastLoading = 0;
	
	private BitmapFont symbolFont;
	private BitmapFont moeFont;

	public DrawTitlescreen(){
		batch = new SpriteBatch();

		iconTexture = new Texture(Gdx.files.internal("data/icon.png"));
		iconTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion loadingRegion = new TextureRegion(iconTexture, 0, 0, 1024,
				1024);
		iconSprite = new Sprite(loadingRegion);
	}

	public void draw(boolean loading, int framesLoading, Color titleColor){
		
		//Get the various dimensions
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		
		//Still haven't finished loading
		if (loading){
			
			//Drawing the primary loading screen
			if (framesLoading < Constants.LOAD_SCREEN_TIME){
				drawLoadingScreen(framesLoading, width, height);
			} 
			//Draw the title screen, with loading at the bottom
			else {
				drawTitlescreen(titleColor, width, height, true, framesLoading - Constants.LOAD_SCREEN_TIME);
			}
		} 
		//Draw the title screen, without the loading animation
		else {
			drawTitlescreen(titleColor, width, height, false, timePastLoading);
			
			timePastLoading = Math.min(timePastLoading+1, Constants.LOAD_TEXT_FADE_TIME * 2);
		}
	}
	
	public void offLoadingScreen(){
		timePastLoading = Constants.LOAD_TEXT_FADE_TIME * 2;
	}
	
	private void drawLoadingScreen(int framesLoading, int width, int height){
		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		float percentDark = 1;
		if (framesLoading < Constants.LOAD_FADE_TIME || framesLoading > Constants.LOAD_SCREEN_TIME - Constants.LOAD_FADE_TIME){
			percentDark = Math.min(framesLoading, Constants.LOAD_SCREEN_TIME - framesLoading) / (float)Constants.LOAD_FADE_TIME;
		}
	
		batch.begin();
		
		//Get an accurate font color
		Color fontColor = Constants.BOARD_COLOR.cpy();
		fontColor.a = percentDark;
		
		//Draw symbollic logo
		String symbols = "#@!?&";
		symbolFont.setColor(fontColor);
		TextBounds tb = symbolFont.getBounds(symbols);
		float yPos = height / 2 + tb.height;
		float xPos = width / 2 - tb.width / 2;
		symbolFont.draw(batch, symbols, xPos, yPos);
		yPos -= tb.height;
		
		//Draw team name
		String moe = "Mildly Offensive Entertainment";
		moeFont.setColor(fontColor);
		tb = moeFont.getBounds(moe);
		yPos -= tb.height;
		xPos = width / 2 - tb.width / 2;
		moeFont.draw(batch, moe, xPos, yPos);
		batch.end();
	}
	
	private void drawTitlescreen(Color titleColor, int width, int height, boolean loading, int framesIn){
		
		float percentDark = 1;
		if (loading && framesIn < Constants.LOAD_FADE_TIME){
			percentDark = (float) framesIn / Constants.LOAD_FADE_TIME;
		}
		
		titleColor.mul(percentDark);
		
		Gdx.gl.glClearColor(titleColor.r, titleColor.g, titleColor.b, titleColor.a);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		//Icon
		iconSprite.setSize(width*3/4, width*3/4);
		iconSprite.setPosition(width/8, (height - width) * 3/4);
		iconSprite.setColor(new Color(1, 1, 1, 1).mul(percentDark));
		iconSprite.draw(batch);
		
		Color fontColor = Constants.BOARD_COLOR.cpy();
		fontColor.mul(percentDark);
		
		//Beam
		String beam = "Beam";
		symbolFont.setColor(fontColor);
		TextBounds tb = symbolFont.getBounds(beam);
		float yPos = height - tb.height / 2;
		float xPos = width / 2 - tb.width / 2;
		symbolFont.draw(batch, beam, xPos, yPos);
		
		if (loading){
			//Loading
			String load = "Loading...";
			moeFont.setColor(fontColor);
			tb = moeFont.getBounds(load);
			yPos = (Menu.B_LOADING_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT) * height;
			xPos = width / 2 - tb.width / 2;
			moeFont.draw(batch, load, xPos, yPos);
		}
		
		else if (framesIn < Constants.LOAD_TEXT_FADE_TIME){
			float fadeAmount = (Constants.LOAD_TEXT_FADE_TIME - framesIn) / (float)Constants.LOAD_TEXT_FADE_TIME;
			fontColor.mul(fadeAmount);
			
			String load = "Loading...";
			moeFont.setColor(fontColor);
			tb = moeFont.getBounds(load);
			yPos = (Menu.B_LOADING_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT) * height;
			xPos = width / 2 - tb.width / 2;
			moeFont.draw(batch, load, xPos, yPos);
		} else {
			float fadeAmount = (framesIn - Constants.LOAD_TEXT_FADE_TIME) / (float)Constants.LOAD_TEXT_FADE_TIME;
			fontColor.mul(fadeAmount);
			
			String play = "Play";
			moeFont.setColor(fontColor);
			tb = moeFont.getBounds(play);
			yPos = (Menu.B_PLAY_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT) * height;
			xPos = width / 2 - tb.width / 2;
			moeFont.draw(batch, play, xPos, yPos);
			
			String settings = "Settings";
			tb = moeFont.getBounds(settings);
			yPos = (Menu.B_SETTINGS_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT) * height;
			xPos = width / 2 - tb.width / 2;
			moeFont.draw(batch, settings, xPos, yPos);
		}
		
		batch.end();
	}
	
	public void initLoadFonts(){
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		int firstFontSize = (Gdx.graphics.getHeight() / 6) - (Gdx.graphics.getHeight() / 6) % 2;
		symbolFont = generator.generateFont(firstFontSize);
		int secondFontSize = (Gdx.graphics.getHeight() / 24) - (Gdx.graphics.getHeight() / 24) % 2;
		moeFont = generator.generateFont(secondFontSize);
		generator.dispose();
	}
}
