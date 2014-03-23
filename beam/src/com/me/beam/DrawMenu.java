package com.me.beam;

import java.util.List;

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

	public void draw(DrawGame dg, List<Board> boardList, Board curBoard, int curLevel){
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		int verticalScrolled = menu.getVerticalScrollAmount();
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		int itemHeight = (int)(Gdx.graphics.getHeight() * Menu.worldItemPercent);
		int itemWidth = (int)(Gdx.graphics.getWidth() * Menu.worldItemPercent);
		int itemTopY = (verticalScrolled + 1) % itemHeight + height;
		int world = menu.getWorldAtPosition(height);

		TextBounds tb;

		//Loop until the item wouldn't show at all
		String stringOrdinal;
		while(itemTopY > 0){
		
			//Loop through horizontally as well
			int horizontalScrolled = menu.getHorizontalScrollAmount(world);
			int itemLeftX = -(horizontalScrolled % itemWidth);
			int itemBotY = itemTopY - itemHeight;
			int ordinal = menu.getLevelAtPosition(itemLeftX + itemWidth / 2, itemTopY - itemHeight / 2);
			while (itemLeftX < width - 1 && ordinal != -1){
				
				//Figure out how to draw the item
				if (!menu.isUnlocked(ordinal)){
					numberFont.setColor(Color.RED);
				} else {
					int bestMoves = menu.getLevelMoves(world);
					if (bestMoves != -1){
						numberFont.setColor(Color.GREEN);
					} else {
						menuFont.setColor(new Color(.133f, .337f, 1, 1));
						numberFont.setColor(new Color(.133f, .337f, 1, 1));
					}
				}
				
				//Either draw current state or entire board
				Board b = (ordinal == curLevel) ? curBoard : boardList.get(ordinal);
				
				// Get board dimensions
				Color curBG = new Color(.1f, .1f, .1f, 1);
				int bx = (int) ((((float) b.getBotLeftX()) / width) * itemWidth) + itemLeftX;
				int by = (int) ((((float) b.getBotLeftY()) / height) * itemHeight) + itemBotY;
				int tilesize = (int) ((((float) b.getTileSize()) / width) * itemWidth);
				dg.drawBoard(b, bx, by, tilesize, curBG);
				
				//Draw number on top
				batch.begin();
				stringOrdinal = (ordinal + 1) + "";
				tb = numberFont.getBounds(stringOrdinal);
				numberFont.draw(batch, stringOrdinal, itemLeftX + ((width * Menu.worldItemPercent) - tb.width)/2, 
						itemTopY - ((height * Menu.worldItemPercent) - tb.height)/2);
				batch.end();
				
				itemLeftX += itemWidth;
				ordinal = menu.getLevelAtPosition(itemLeftX + itemWidth / 2, itemTopY - itemHeight / 2);
			}

			itemTopY -= itemHeight;
			world++;
		}
	}

	public void dispose(){

	}

	//Initializes menu fonts
	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		menuFont = generator.generateFont((int) (Gdx.graphics.getHeight() * Menu.worldItemPercent / 5));
		numberFont = generator.generateFont((int) (Gdx.graphics.getHeight() * Menu.worldItemPercent / 5f));
		generator.dispose();
	}
}
