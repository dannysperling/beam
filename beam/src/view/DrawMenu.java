package view;

import java.util.List;

import utilities.AssetInitializer;
import utilities.Constants;
import model.Board;
import model.Menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class DrawMenu {

	private Menu menu;
	private BitmapFont numberFont;
	private SpriteBatch batch;
	private DrawGame dg;
	private List<List<Board>> allBoards;
	private Texture lockTexture;
	private Sprite lockSprite;
	private Texture starTexture;
	private Sprite starSprite;
	private FrameBuffer bgBuffer;
	private FrameBuffer shiftBoardBuffer;
	public boolean shiftBoardNew = true;
	
	private Sprite boardSprite;
	private Sprite bgSprite;
	
	private int fullHeight = 0;
	private int fullWidth = 0;
	
	
	

	/**
	 * Constructs a drawMenu, with a reference to the menu, the drawgame,
	 * and all of the boards in their unstarted states
	 * 
	 * @param menu
	 * 				Reference to the menu object
	 * @param dg
	 * 				Reference to the drawgame object
	 * @param allBoards
	 * 				List of lists of all the boards
	 */
	public DrawMenu(Menu menu, DrawGame dg, List<List<Board>> allBoards){
		this.menu = menu;
		batch = new SpriteBatch();
		this.dg = dg;
		this.allBoards = allBoards;
		
		lockTexture = AssetInitializer.getTexture(AssetInitializer.lock);
		lockTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion lockregion = new TextureRegion(lockTexture, 0, 0, 128, 128);
		lockSprite = new Sprite(lockregion);
		
		starTexture = AssetInitializer.getTexture(AssetInitializer.one_star);
		starTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion starRegion = new TextureRegion(starTexture, 0, 0, 128, 128);
		starSprite = new Sprite(starRegion);
	}

	/**
	 * This is the primary method to draw the menu. Based on the current board, world,
	 * and level ordinal it can determine which level to explicitly draw in a partially
	 * completed state. All other levels are drawn based on the stored boards.
	 * 
	 * Uses menu to determine how far each world has been scrolled, as well as the
	 * vertical scroll.
	 * 
	 * @param curBoard
	 * 					The current board of the player
	 * @param curWorld
	 * 					The current world of the player
	 * @param curOrdinalInWorld
	 * 					The current level ordinal within the world of the player
	 */
	public void draw(Board curBoard, int curWorld, int curOrdinalInWorld, boolean shifting, float shiftProg){
		//Clear colors
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		//Get the various dimensions
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();


		int worldHeight = menu.getWorldHeight();
		int levelItemWidth = menu.getLevelItemWidth();

		//Start at the top of the screen
		int world = menu.getWorldAtPosition(height);
		
		//Handle in-between world states
		if (world > 1000)
			world -= 1000;

		//Determine the top of the first world showing
		int verticalScrolled = menu.getVerticalScrollAmount();
		int itemTopY = (verticalScrolled + 1) % (worldHeight + menu.getSpaceHeight()) + height;

		Board shiftBoard = null;
		int shiftBotY = 0;
		int shiftLeftX = 0;
		
		boolean shiftIsLastLevel = false;
		boolean shiftNextLevelLocked = false;

		//Loop down until the current world wouldn't show at all - 
		//the top of the world is below the bottom of the scren
		while(itemTopY > 0){

			//Ensure world in bounds
			if (menu.worldInBounds(world)){

				//Loop through horizontally as well
				int horizontalScrolled = menu.getHorizontalScrollAmount(world);
				int itemLeftX = -(horizontalScrolled % levelItemWidth);
				int itemBotY = itemTopY - worldHeight;
				int ordinalInWorld = menu.getLevelAtPositionInWorld(world, itemLeftX + levelItemWidth / 2);
				boolean worldUnlocked = menu.isWorldUnlocked(world);

				//Draw the world background
				drawWorldBackground(world, itemBotY);

				//Loop until the left side of the level that would be drawn is off screen
				while (itemLeftX < width - 1){

					//Ensure index in bounds
					if (ordinalInWorld != -1){

						//Draw the numbers above the level
						drawLevelNumber(worldUnlocked, world, ordinalInWorld, itemTopY, itemLeftX);

						//Figure out which board we're drawing
						Board b;


						boolean shiftThisOne = false;
						//Check if we're drawing the current board 
						if (ordinalInWorld == curOrdinalInWorld && world == curWorld){
							b = curBoard;
							shiftThisOne = shifting;
						} else { 
							b = allBoards.get(world - 1).get(ordinalInWorld - 1);
						}
						//Draw the current level
						
						int stars = menu.getLevelStars(world, ordinalInWorld);

						if(!shiftThisOne){
							drawLevelBoard(true, world, ordinalInWorld, b, itemBotY, itemLeftX, stars);
						} else {
							shiftBoard = b;
							shiftBotY = itemBotY;
							shiftLeftX = itemLeftX;
							shiftIsLastLevel = menu.isLastLevelInWorld(world, ordinalInWorld);
							shiftNextLevelLocked = !menu.isNextLevelUnlocked(world, ordinalInWorld);
						}
					}

					//Increment to the next item in the world
					itemLeftX += levelItemWidth;

					//Grab the next ordinal. That way, we get -1 if we're past the bounds
					ordinalInWorld = menu.getLevelAtPositionInWorld(world, itemLeftX + levelItemWidth / 2);
				}
				drawWorldOverlay(world, itemBotY);
			}

			//Drawing from top to bottom, so decrement the y coordinate
			itemTopY -= worldHeight+menu.getSpaceHeight();

			//Worlds increase as you go down the screen
			world++;
		}


		if(shiftBoard != null){

			if(shiftBoardNew){
				shiftBoardNew = false;
				fullHeight = shiftBoard.getTileSize() * shiftBoard.getNumVerticalTiles();
				fullWidth = shiftBoard.getTileSize() * shiftBoard.getNumHorizontalTiles();

				bgBuffer = new FrameBuffer(Format.RGBA8888, width, height, false);
				bgBuffer.begin();
				dg.drawBoardless(menu.colorOfLevel(curWorld, curOrdinalInWorld), curWorld, curOrdinalInWorld, shiftBoard, 
						shiftIsLastLevel, shiftNextLevelLocked);
				bgBuffer.end();

				TextureRegion background = new TextureRegion(bgBuffer.getColorBufferTexture());
				background.flip(false, true);

				shiftBoardBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
				shiftBoardBuffer.begin();
				dg.drawBoard(shiftBoard, 0, 0, shiftBoard.getTileSize(), false);
				shiftBoardBuffer.end();	
				bgSprite = new Sprite(background);

				Texture btex = shiftBoardBuffer.getColorBufferTexture();
				btex.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
				TextureRegion boardTex = new TextureRegion(btex, 0, 0, fullWidth, fullHeight);
				boardTex.flip(false, true);
				boardSprite = new Sprite(boardTex);
			}


			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			batch.begin();
			bgSprite.setColor(new Color(1,1,1,shiftProg));
			bgSprite.draw(batch);
			batch.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);

			//Figure out the board information
			int by = (int)(menu.starHeightPercent * worldHeight + shiftBotY);
			int tilesize = (int)(menu.boardHeightPercent * worldHeight / shiftBoard.getNumVerticalTiles());
			int bx = (levelItemWidth - tilesize * shiftBoard.getNumHorizontalTiles()) / 2 + shiftLeftX;


			int nowheight = tilesize * shiftBoard.getNumVerticalTiles();
			int nowwidth = tilesize * shiftBoard.getNumHorizontalTiles();

			bx += (shiftBoard.getBotLeftX() - bx) * shiftProg;
			by += (shiftBoard.getBotLeftY() - by) * shiftProg;

			boardSprite.setSize(nowwidth + ((fullWidth - nowwidth) * shiftProg), nowheight + ((fullHeight - nowheight) * shiftProg));
			boardSprite.setPosition(bx, by);
			batch.begin();
			boardSprite.draw(batch);
			batch.end();


		} else {
			if(bgBuffer != null)
				bgBuffer.dispose();
			if(shiftBoardBuffer != null)
				shiftBoardBuffer.dispose();
			shiftBoardNew = true;
		}

	}

	private void drawWorldOverlay(int world, int itemBotY) {
		
		//There is not overlay if the world is unlocked
		if (menu.isWorldUnlocked(world)){
			return;
		}
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ShapeRenderer shape = dg.shapes;
		shape.begin(ShapeType.Filled);
		//Grey out world
		shape.setColor(Constants.LOCKED_WORLD_OVERLAY);
		shape.rect(0, itemBotY, Gdx.graphics.getWidth(),  menu.getWorldHeight());
		shape.end();
		
		//Several set values
		float vertOverlaySpace = (menu.getWorldHeight()*menu.boardHeightPercent);
		float lockYPos;
		
		if (menu.isWorldUnlocked(world-1)) {
			//Draw text telling user how many stars they need
			String textEarned = "" + menu.getNumStarsEarned(world-1);
			String textSlash = "/";
			String textNeeded = "" + menu.getNumStarsNeeded(world-1);
			batch.begin();
			numberFont.setColor(Constants.BOARD_COLOR);
			
			//Find the bounds based on the size of the drawn text
			float earnedWidth = numberFont.getBounds(textEarned).width;
			float slashWidth = numberFont.getBounds(textSlash).width;
			float neededWidth = numberFont.getBounds(textNeeded).width;
		
			float stringHeight = numberFont.getBounds(textEarned).height;
			
			//Add one star as well
			float spriteSize = stringHeight * 1.60f;
			int spaceSize = (int)(slashWidth * 0.25);
			float lineLength = earnedWidth + slashWidth + neededWidth + spriteSize * 2/3 + 2.5f*spaceSize;
			
			//Draw iteratively
			float startX = Gdx.graphics.getWidth()/2-lineLength/2;
			float yPos = itemBotY+vertOverlaySpace/2;
			
			//Each text piece
			numberFont.draw(batch, textEarned, startX, yPos);
			startX += earnedWidth + spaceSize;
			numberFont.draw(batch, textSlash, startX, yPos);
			startX += slashWidth + spaceSize;
			numberFont.draw(batch, textNeeded, startX, yPos);
			startX += neededWidth + spaceSize / 2;
			
			//And the star
			float sizeFix = 0.46f; //Fixing Reese's less-than-size-of-edge star
			starSprite.setSize(spriteSize, spriteSize);
			starSprite.setY(yPos - stringHeight/2 - spriteSize * sizeFix);
			starSprite.setX(startX);
			starSprite.draw(batch);
			
			batch.end();
			
			//Set the lock position to be above the numbers
			lockYPos = itemBotY + vertOverlaySpace * 7 / 10;
		} else {
			//Set the lock position to the center of the vertical overlay
			lockYPos = itemBotY + vertOverlaySpace * 5 / 10;
		}
		
		//Always draw the lock
		batch.begin();
		lockSprite.setColor(Constants.LOCK_COLOR);
				
		float spriteSize = vertOverlaySpace * 4 / 5;
		lockSprite.setSize(spriteSize, spriteSize);
		lockSprite.setX(Gdx.graphics.getWidth()/2-spriteSize/2);
		lockSprite.setY(lockYPos);
		lockSprite.draw(batch);
		
		batch.end();
		
		Gdx.gl.glDisable(GL10.GL_BLEND);
		
	}

	/**
	 * Draws the colored background of a given world
	 * 
	 * @param world
	 * 				The world being drawn
	 * @param itemBotY
	 * 				The y position of the bottom of the current rectangle
	 */
	private void drawWorldBackground(int world, int itemBotY){

		//Get the world dimensions
		int worldHeight = menu.getWorldHeight();
		int levelItemWidth = menu.getLevelItemWidth();
		int maxWidth = levelItemWidth*menu.sizeOfWorld(world);
		int leftStartingPoint = -menu.getHorizontalScrollAmount(world);

		//Drawing rectangles
		ShapeRenderer shape = dg.shapes;
		shape.begin(ShapeType.Filled);

		//Go from light to dark accross the given world
		Color startColor = Menu.colorOfWorld(world).mul(Constants.START_COLOR_MUL);
		Color endColor = Menu.colorOfWorld(world).mul(Constants.END_COLOR_MUL);
		//Color startColor = setSaturation(Menu.colorOfWorld(world),0.25f);
		//Color endColor = setSaturation(Menu.colorOfWorld(world),0.95f);
		
		//Draw main background:
		shape.rect(leftStartingPoint, itemBotY, maxWidth, worldHeight, startColor, endColor, endColor, startColor);


		//Draw left overflow box, if applicable
		if (leftStartingPoint > 0){
			//Draw from left side of the screen to the first level
			shape.setColor(startColor);
			shape.rect(0, itemBotY, leftStartingPoint, worldHeight);
		}

		//Draw right overflow box, if applicable
		int rightEndPoint = leftStartingPoint + maxWidth;
		int screenWidth = Gdx.graphics.getWidth();

		if (rightEndPoint < screenWidth){
			//Draw from right end point to the width of the screen
			shape.setColor(endColor);
			shape.rect(rightEndPoint, itemBotY, screenWidth - rightEndPoint, worldHeight);
		}
		shape.end();
		//Stroke world border
		shape.begin(ShapeType.Line);
		shape.setColor(Color.WHITE);
		int thickness = 2;
		for (int i = 0; i < thickness; i++){
			shape.line(0, itemBotY-i+thickness, Gdx.graphics.getWidth(), itemBotY-i+thickness);
			shape.line(0, itemBotY+worldHeight+i-thickness+1, Gdx.graphics.getWidth(), itemBotY+worldHeight+i-thickness+1);
		}
		shape.end();
	}
	
	/**
	 * Draws the number above the board inside the menu item. At present, the number
	 * is red if locked, green if completed, and blue otherwise.
	 * 
	 * @param worldUnlocked
	 * 						Whether the current world is unlocked
	 * @param world
	 * 						What the current world to draw for is
	 * @param ordinalInWorld
	 * 						What the current level ordinal within the world is
	 * @param itemTopY
	 * 						The top y position of the menu item being drawn
	 * @param itemLeftX
	 * 						The left x position of the menu item being drawn
	 */
	private void drawLevelNumber(boolean worldUnlocked, int world, int ordinalInWorld, int itemTopY, int itemLeftX){
		numberFont.setColor(worldUnlocked?Constants.BOARD_COLOR:Constants.LOCKED_LEVEL_NUMBER_COLOR);

		int levelItemWidth = menu.getLevelItemWidth();
		int worldHeight = menu.getWorldHeight();

		//Draw number on the top portion within its own batch
		batch.begin();
		String stringNumber =  world + "-" + ordinalInWorld;

		//Find the bounds based on the size of the drawn text
		TextBounds tb = numberFont.getBounds(stringNumber);
		numberFont.draw(batch, stringNumber, itemLeftX + (levelItemWidth - tb.width)/2, 
				itemTopY - ((worldHeight * (1-menu.boardHeightPercent-menu.starHeightPercent) - tb.height) / 2));
		batch.end();
	}

	/**
	 * Draws a board onto the menu given the bottom left coordinate of 
	 * the menu item it is being drawn onto.
	 * 
	 * @param b
	 * 				The board to be drawn
	 * @param itemBotY
	 * 				The y coordinate of the bottom of the menu item
	 * @param itemLeftX
	 * 				The x coordinate of the left side of the menu item
	 */
	private void drawLevelBoard(boolean worldUnlocked, int world, int ordinalInWorld, Board b, int itemBotY, int itemLeftX, int stars){
		// Get board dimensions
		int levelItemWidth = menu.getLevelItemWidth();
		int worldHeight = menu.getWorldHeight();

		//Figure out the board information
		int by = (int)(menu.starHeightPercent * worldHeight + itemBotY);
		int tilesize = (int)(menu.boardHeightPercent * worldHeight / b.getNumVerticalTiles());
		int bx = (levelItemWidth - tilesize * b.getNumHorizontalTiles()) / 2 + itemLeftX;
		boolean locked = !worldUnlocked || (menu.isBonus(world, ordinalInWorld) && !menu.isBonusLevelUnlocked(world));
		
		//Draw the board in the appropriate location
		dg.drawBoard(b, bx, by, tilesize, locked);
		
		//If locked, draw the lock
		drawLock(itemLeftX, by, locked);
		
		//If stars, draw them
		drawStars(itemLeftX, itemBotY, stars);
	}

	private void drawLock(int x, int y, boolean locked) {
		if (!locked) return;
		batch.begin();
		lockSprite.setColor(Constants.LOCK_COLOR);
		float spriteSize = menu.getLevelItemWidth()*0.6f;
		lockSprite.setSize(spriteSize, spriteSize);
		lockSprite.setX(x+(menu.getLevelItemWidth()-spriteSize)/2);
		lockSprite.setY(y+(menu.getWorldHeight()*menu.boardHeightPercent)/2 - (spriteSize)/2);
		lockSprite.draw(batch);
		batch.end();
	}
	
	private void drawStars(int x, int y, int stars){
		if (stars == 0) return;
		
		batch.begin();
		int itemWidth = menu.getLevelItemWidth();
		int worldHeight = menu.getWorldHeight();
		float starPercent = 0.75f;
		float spriteSize = worldHeight * menu.starHeightPercent * starPercent;
		starSprite.setSize(spriteSize, spriteSize);
		starSprite.setY(y + (1 - starPercent)/2 * menu.starHeightPercent * worldHeight);
		
		//Draw different sets of stars differently
		switch(stars){
			case 1:
				//One star in middle
				starSprite.setX(x + (itemWidth - spriteSize)/2);
				starSprite.draw(batch);
				break;
			case 2:
				//Two stars centered in middle
				starSprite.setX(x + itemWidth/2 - spriteSize);
				starSprite.draw(batch);
				starSprite.setX(x + itemWidth/2);
				starSprite.draw(batch);
				break;
			case 3:
				//Three stars!
				starSprite.setX(x + (itemWidth - 3*spriteSize)/2);
				starSprite.draw(batch);
				starSprite.setX(x + (itemWidth - spriteSize)/2);
				starSprite.draw(batch);
				starSprite.setX(x + (itemWidth + spriteSize)/2);
				starSprite.draw(batch);
				break;
			default: //How we get here man?
				break;
		}
		batch.end();
	}

	/**
	 * Disposes any batches, textures, and fonts being used
	 */
	public void dispose(){
		if(bgBuffer != null)
			bgBuffer.dispose();
		if(shiftBoardBuffer != null)
			shiftBoardBuffer.dispose();
		numberFont.dispose();
		batch.dispose();
	}

	/**
	 * Initializes fonts for the menu. This needs to be called any time the device wakes up.
	 * Otherwise, the fonts get out of sync
	 */
	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		numberFont = generator.generateFont((int) (menu.getWorldHeight() * (1 - menu.boardHeightPercent - menu.starHeightPercent) * 3 / 4));
		generator.dispose();
	}
}
