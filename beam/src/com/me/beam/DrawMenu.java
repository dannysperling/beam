package com.me.beam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class DrawMenu {
	
	
	private Menu menu;
	private BitmapFont menuFont;
	private BitmapFont numberFont;
	private SpriteBatch batch;
	
	public DrawMenu(Menu menu){
		this.menu = menu;
		batch = new SpriteBatch();
		initFonts();
	}
	
	public void draw(){
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		int scrolled = menu.getScrollAmount();
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		int itemTopY = scrolled % (int)((Menu.menuItemHeight * height)) + height;
		int itemOrdinal = menu.getLevelAtPosition(height);
		
		TextBounds tb;
		
		//Loop until the item wouldn't show at all
		batch.begin();
		String levelInfo;
		String stringOrdinal;
		while(itemTopY >= 0){
			//Draw the level number off to the left
			if (!menu.isUnlocked(itemOrdinal)){
				levelInfo = "LOCKED";
				menuFont.setColor(Color.RED);
				numberFont.setColor(Color.RED);
			} else {
				int bestMoves = menu.getLevelMoves(itemOrdinal);
				if (bestMoves != -1){
					int stars = menu.getLevelStars(itemOrdinal);
					levelInfo = "Your Best: " + bestMoves + " moves. " + stars + " star";
					levelInfo += (stars == 1)? "." : "s.";
					menuFont.setColor(Color.GREEN);
					numberFont.setColor(Color.GREEN);
				} else {
					levelInfo = "Incomplete";
					menuFont.setColor(Color.BLUE);
					numberFont.setColor(Color.BLUE);
				}
			}
			tb = menuFont.getBounds(levelInfo);
			menuFont.drawMultiLine(batch, levelInfo, (width * 0.30f), 
					itemTopY - (height * Menu.menuItemHeight - tb.height)/2);
			
			stringOrdinal = (itemOrdinal + 1) + "";
			tb = numberFont.getBounds(stringOrdinal);
			numberFont.draw(batch, stringOrdinal, (width * 0.2f - tb.width)/2 + width*0.05f, 
					itemTopY - ((height * Menu.menuItemHeight) - tb.height)/2);
			
			//Increment the botY and the ordinal
			itemTopY -= Menu.menuItemHeight * height;
			itemOrdinal++;
		}
		batch.end();
	}
	
	public void dispose(){
		
	}

	//Initializes menu fonts
	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		menuFont = generator.generateFont((int) (Gdx.graphics.getHeight() * Menu.menuItemHeight / 5));
		numberFont = generator.generateFont((int) (Gdx.graphics.getHeight() * Menu.menuItemHeight / 1.5f));
		generator.dispose();
	}
}
