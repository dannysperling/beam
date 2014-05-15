package view;

import model.Board;
import model.GameProgress;
import model.Menu;
import utilities.AssetInitializer;
import utilities.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class DrawTitlescreen {

	private SpriteBatch batch;
	private Texture iconTexture;
	private Sprite iconSprite;
	private Texture moeTexture;
	private Sprite moeSprite;
	
	private Texture musicOffTexture;
	private Sprite musicOffSprite;
	private Texture musicOnTexture;
	private Sprite musicOnSprite;
	private Texture soundOffTexture;
	private Sprite soundOffSprite;
	private Texture soundOnTexture;
	private Sprite soundOnSprite;
	private Texture creditsTexture;
	private Sprite creditsSprite;
	
	private GameProgress progress;

	private int timePastLoading = 0;

	private BitmapFont titleFont;
	private BitmapFont optionFont;
	private BitmapFont creditsFont;

	private FrameBuffer menuBuffer;
	private Sprite menuSprite;

	public DrawTitlescreen(GameProgress progress) {
		batch = new SpriteBatch();

		iconTexture = new Texture(Gdx.files.internal("data/icon.png"));
		iconTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		moeTexture = new Texture(Gdx.files.internal("data/moe.png"));
		moeTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		TextureRegion loadingRegion = new TextureRegion(iconTexture);
		iconSprite = new Sprite(loadingRegion);
	
		TextureRegion moeRegion = new TextureRegion(moeTexture);
		moeSprite = new Sprite(moeRegion);
		
		this.progress = progress;
	}

	public void draw(boolean loading, int framesLoading, Color titleColor,
			boolean transitioning, int transFrames, boolean creditsShowing) {

		// Get the various dimensions
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();

		// Still haven't finished loading
		if (loading) {

			// Drawing the primary loading screen
			if (framesLoading < Constants.LOAD_SCREEN_TIME) {
				drawLoadingScreen(framesLoading, width, height);
			}
			// Draw the title screen, with loading at the bottom
			else {
				drawTitlescreen(titleColor, width, height, true, framesLoading
						- Constants.LOAD_SCREEN_TIME, false, 0);
			}
		}
		// Draw the title screen, without the loading animation
		else {
			if (!creditsShowing){
				drawTitlescreen(titleColor, width, height, false, timePastLoading,
						transitioning, transFrames);
	
				timePastLoading = Math.min(timePastLoading + 1,
						Constants.LOAD_TEXT_FADE_TIME * 2);
			} else {
				drawCredits(titleColor, width, height);
			}
		}
	}

	public void offLoadingScreen() {
		timePastLoading = Constants.LOAD_TEXT_FADE_TIME * 2;
	}

	private void drawLoadingScreen(int framesLoading, int width, int height) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float percentDark = 1;
		if (framesLoading < Constants.LOAD_FADE_TIME || framesLoading > Constants.LOAD_SCREEN_TIME - Constants.LOAD_FADE_TIME) {
			percentDark = Math.min(framesLoading, Constants.LOAD_SCREEN_TIME - framesLoading) / (float) Constants.LOAD_FADE_TIME;
		}
		
		batch.begin();

		// Get an accurate font color
		Color fontColor = Color.WHITE.cpy();
		fontColor.a = percentDark;
		
		moeSprite.setColor(fontColor);
		
		float widthRatio = ((float)(width)) / moeSprite.getWidth();
		if(widthRatio * moeSprite.getHeight() <= height){
			moeSprite.setSize(width, moeSprite.getHeight() * widthRatio);
			moeSprite.setPosition(0, (height - (moeSprite.getHeight() * widthRatio)) / 2.0f);
		} else {
			float heightRatio = ((float)(height)) / moeSprite.getHeight();
			moeSprite.setSize(moeSprite.getWidth() * heightRatio, height);
			moeSprite.setPosition((width - (moeSprite.getWidth() * heightRatio)) / 2.0f, 0);
		}
		
		moeSprite.draw(batch);
		
		batch.end();
		
		

	}

	private void drawTitlescreen(Color titleColor, int width, int height,
			boolean loading, int framesIn, boolean transitioning,
			int transFrames) {

		float percentDark = 1;
		if (loading && framesIn < Constants.LOAD_FADE_TIME) {
			percentDark = (float) framesIn / Constants.LOAD_FADE_TIME;
		}

		titleColor.mul(percentDark);

		Gdx.gl.glClearColor(titleColor.r, titleColor.g, titleColor.b,
				titleColor.a);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.begin();

		// Icon
		iconSprite.setSize(width * 3 / 4, width * 3 / 4);
		iconSprite.setPosition(width / 8, (height - width) * 3 / 4);
		iconSprite.setColor(new Color(1, 1, 1, 1).mul(percentDark));
		iconSprite.draw(batch);

		Color fontColor = Constants.BOARD_COLOR.cpy();
		fontColor.mul(percentDark);

		// Beam
		String beam = "Beam";
		titleFont.setColor(fontColor);
		TextBounds tb = titleFont.getBounds(beam);
		float yPos = height - tb.height / 2;
		float xPos = width / 2 - tb.width / 2;
		titleFont.draw(batch, beam, (int)xPos, (int)yPos);

		if (loading) {
			// Loading
			String load = "Loading...";
			optionFont.setColor(fontColor);
			tb = optionFont.getBounds(load);
			yPos = (Menu.B_LOAD_PLAY_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT)
					* height;
			xPos = width / 2 - tb.width / 2;
			optionFont.draw(batch, load, (int)xPos, (int)yPos);
		}

		else if (framesIn < Constants.LOAD_TEXT_FADE_TIME) {
			float fadeAmount = (Constants.LOAD_TEXT_FADE_TIME - framesIn)
					/ (float) Constants.LOAD_TEXT_FADE_TIME;
			fontColor.a = fadeAmount;

			String load = "Loading...";
			optionFont.setColor(fontColor);
			tb = optionFont.getBounds(load);
			yPos = (Menu.B_LOAD_PLAY_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT)
					* height;
			xPos = width / 2 - tb.width / 2;
			optionFont.draw(batch, load, (int)xPos, (int)yPos);
		} else {
			float fadeAmount = (framesIn - Constants.LOAD_TEXT_FADE_TIME)
					/ (float) Constants.LOAD_TEXT_FADE_TIME;
			fontColor.a = fadeAmount;

			String play = "Play";
			optionFont.setColor(fontColor);
			tb = optionFont.getBounds(play);
			yPos = (Menu.B_LOAD_PLAY_BOT_Y + Menu.TITLE_SCREEN_BUTTON_HEIGHT)
					* height;
			xPos = width / 2 - tb.width / 2;
			optionFont.draw(batch, play, (int)xPos, (int)yPos);
			
			fontColor.a /= 2;
			
			//Draw the bottom icons
			float botIconSize = Menu.B_SMC_SIZE * width;
			Sprite soundSprite = progress.isSoundPlaying() ? soundOnSprite : soundOffSprite;
			soundSprite.setSize(botIconSize, botIconSize);
			soundSprite.setPosition(Menu.B_SOUND_LEFT_X * width, Menu.B_SMC_BOT_Y * height);
			soundSprite.setColor(fontColor);
			soundSprite.draw(batch);
			
			Sprite musicSprite = progress.isMusicPlaying() ? musicOnSprite : musicOffSprite;
			musicSprite.setSize(botIconSize, botIconSize);
			musicSprite.setPosition(Menu.B_MUSIC_LEFT_X * width, Menu.B_SMC_BOT_Y * height);
			musicSprite.setColor(fontColor);
			musicSprite.draw(batch);
			
			creditsSprite.setSize(botIconSize, botIconSize);
			creditsSprite.setPosition(Menu.B_CREDITS_LEFT_X * width, Menu.B_SMC_BOT_Y * height);
			creditsSprite.setColor(fontColor);
			creditsSprite.draw(batch);
		}

		batch.end();

		// Draw the menu if transitioning into it
		if (transitioning) {
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			batch.begin();
			menuSprite.setColor(new Color(1, 1, 1, (float) transFrames
					/ Constants.TRANS_TO_MENU_TIME));
			menuSprite.draw(batch);
			batch.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);
		}
	}
	
	public void drawCredits(Color titleColor, int width, int height){
		Gdx.gl.glClearColor(titleColor.r, titleColor.g, titleColor.b,
				titleColor.a);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		Color fontColor = Constants.BOARD_COLOR.cpy();		
		
		batch.begin();
		String[] credits = {
							"Developers", "", 
							"Mildly Offensive", "Entertainment", "", "",
							"Music", "", 
							"Dan-O at" ,"http://danosongs.com", "", "",
							"Sound", "",
							"deep-air-woosh by",
							"Cosmic Embers, derivative",
							"www.youtube.com/cosmicembers", "",
							"sizzle by jaide714", ""};
		
		creditsFont.setColor(fontColor);
		
		int topPosition = (int)(height * 0.80);
		int botPosition = (int)(height * 0.20);
		
		String ack = "Credits";
		TextBounds tb = creditsFont.getBounds(ack);
		int yPos = (int)(topPosition + tb.height * 4);
		int xPos = (int)(width / 2 - tb.width / 2);
		creditsFont.draw(batch, ack, xPos, yPos);
				
		for (int i = 0; i < credits.length; i++){
			tb = creditsFont.getBounds(credits[i]);
			yPos = (int)(topPosition + (botPosition - topPosition)/(credits.length-1)*i);
			xPos = (int)(width / 2 - tb.width / 2);
			creditsFont.draw(batch, credits[i], xPos, yPos);
		}
		
		batch.end();
	}
	
	public void initTitleScreenSymbols(){
		musicOffTexture = AssetInitializer.getTexture(AssetInitializer.music_off);
		musicOffTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion musicOffRegion = new TextureRegion(musicOffTexture);
		musicOffSprite = new Sprite(musicOffRegion);
		
		musicOnTexture = AssetInitializer.getTexture(AssetInitializer.music_on);
		musicOnTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion musicOnRegion = new TextureRegion(musicOnTexture);
		musicOnSprite = new Sprite(musicOnRegion);
		
		soundOffTexture = AssetInitializer.getTexture(AssetInitializer.sound_off);
		soundOffTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion soundOffRegion = new TextureRegion(soundOffTexture);
		soundOffSprite = new Sprite(soundOffRegion);
		
		soundOnTexture = AssetInitializer.getTexture(AssetInitializer.sound_on);
		soundOnTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion soundOnRegion = new TextureRegion(soundOnTexture);
		soundOnSprite = new Sprite(soundOnRegion);
		
		creditsTexture = AssetInitializer.getTexture(AssetInitializer.credits);
		creditsTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion creditsRegion = new TextureRegion(creditsTexture);
		creditsSprite = new Sprite(creditsRegion);
	}

	public void initMenuSprite(DrawMenu dm, Board curBoard, int curWorld,
			int curOrdinalInWorld) {
		menuBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight(), false);
		menuBuffer.begin();
		dm.draw(curBoard, curWorld, curOrdinalInWorld, false, 0, true);
		menuBuffer.end();

		Texture mtex = menuBuffer.getColorBufferTexture();
		mtex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegion menuRegion = new TextureRegion(mtex, 0, 0,
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		menuRegion.flip(false, true);
		menuSprite = new Sprite(menuRegion);
	}

	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.internal(Constants.FONT_PATH));
		int firstFontSize = (Gdx.graphics.getHeight() / 6);
		titleFont = generator.generateFont(firstFontSize);
		int secondFontSize = (Gdx.graphics.getHeight() / 12);
		optionFont = generator.generateFont(secondFontSize);
		int creditsFontSize = (int)(Gdx.graphics.getHeight() / 32);
		creditsFont = generator.generateFont(creditsFontSize);
		generator.dispose();
	}
}
