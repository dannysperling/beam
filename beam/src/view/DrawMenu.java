package view;

import java.util.List;

import model.Board;
import model.Menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class DrawMenu {

	private Menu menu;
	private BitmapFont numberFont;
	private SpriteBatch batch;
	private DrawGame dg;
	private List<List<Board>> allBoards;

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
		
		//Determine the top of the first world showing
		int verticalScrolled = menu.getVerticalScrollAmount();
		int itemTopY = (verticalScrolled + 1) % worldHeight + height;

		Board shiftBoard = null;
		int shiftBotY = 0;
		int shiftLeftX = 0;
		
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
						if(!shiftThisOne){
							drawLevelBoard(b, itemBotY, itemLeftX, false, 0);
						} else {
							shiftBoard = b;
							shiftBotY = itemBotY;
							shiftLeftX = itemLeftX;
						}
					}
					
					//Increment to the next item in the world
					itemLeftX += levelItemWidth;
					
					//Grab the next ordinal. That way, we get -1 if we're past the bounds
					ordinalInWorld = menu.getLevelAtPositionInWorld(world, itemLeftX + levelItemWidth / 2);
				}
			}

			//Drawing from top to bottom, so decrement the y coordinate
			itemTopY -= worldHeight;
			
			//Worlds increase as you go down the screen
			world++;
		}
		
		if(shiftBoard != null){
			drawLevelBoard(shiftBoard, shiftBotY, shiftLeftX, true, shiftProg);

		}
			
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
		Color light = Menu.colorOfWorld(world).mul(1.1f);//change mul factor to lighten startpoint
		Color dark = Menu.colorOfWorld(world ).mul(.25f);//change mul factor to lighten endpoint
		
		//Draw main background:
		shape.rect(leftStartingPoint, itemBotY, maxWidth, worldHeight, light, dark, dark, light);
		
		//Draw left overflow box, if applicable
		if (leftStartingPoint > 0){
			//Draw from left side of the screen to the first level
			shape.setColor(light);
			shape.rect(0, itemBotY, leftStartingPoint, worldHeight);
		}
		
		//Draw right overflow box, if applicable
		int rightEndPoint = leftStartingPoint + maxWidth;
		int screenWidth = Gdx.graphics.getWidth();
		
		if (rightEndPoint < screenWidth){
			//Draw from right end point to the width of the screen
			shape.setColor(dark);
			shape.rect(rightEndPoint, itemBotY, screenWidth - rightEndPoint, worldHeight);
		}
		
		//Alpha broken :(//shape.rect(-menu.getHorizontalScrollAmount(world), itemTopY-itemHeight, itemWidth*menu.sizeOfWorld(world), itemHeight/10.0f, Color.WHITE, Color.WHITE, new Color(1,1,1,0.0f), new Color(1,1,1,0.5f));
		//Not sure what was being done with alpha here
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
		//If the level is locked, make it red
		if (!worldUnlocked || (menu.isBonus(world, ordinalInWorld) && !menu.isBonusLevelUnlocked(world))){
			numberFont.setColor(Color.RED);
		} else {
			
			//Otherwise, if it's solved, make it green
			int bestMoves = menu.getLevelMoves(world, ordinalInWorld);
			if (bestMoves != 0){
				numberFont.setColor(Color.GREEN);
			} 
			//If not locked or solved, blue
			else {
				numberFont.setColor(new Color(.133f, .337f, 1, 1));
			}
		}
		
		int levelItemWidth = menu.getLevelItemWidth();
		int worldHeight = menu.getWorldHeight();
		
		//Draw number on the top portion within its own batch
		batch.begin();
		String stringNumber =  world + "-" + ordinalInWorld;

		//Find the bounds based on the size of the drawn text
		TextBounds tb = numberFont.getBounds(stringNumber);
		numberFont.draw(batch, stringNumber, itemLeftX + (levelItemWidth - tb.width)/2, 
				itemTopY - ((worldHeight * (1-menu.boardHeightPercent) * 3/4) - tb.height) / 2);
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
	private void drawLevelBoard(Board b, int itemBotY, int itemLeftX, boolean shift, float shiftProgress){
		// Get board dimensions
		int levelItemWidth = menu.getLevelItemWidth();
		int worldHeight = menu.getWorldHeight();
		
		//Figure out the board information
		Color curBG = new Color(.1f, .1f, .1f, 1);
		int by = (int)((1 - menu.boardHeightPercent) / 4 * worldHeight + itemBotY);
		int tilesize = (int)(menu.boardHeightPercent * worldHeight / b.getNumVerticalTiles());
		int bx = (levelItemWidth - tilesize * b.getNumHorizontalTiles()) / 2 + itemLeftX;
		
		if(shift){
			bx += (b.getBotLeftX() - bx) * shiftProgress;
			by += (b.getBotLeftY() - by) * shiftProgress;
			tilesize += (b.getTileSize() - tilesize) * shiftProgress;
		}
		
		//Draw the board in the appropriate location
		dg.drawBoard(b, bx, by, tilesize, curBG);
	}

	/**
	 * Disposes any batches, textures, and fonts being used
	 */
	public void dispose(){
		numberFont.dispose();
		batch.dispose();
	}

	/**
	 * Initializes fonts for the menu. This needs to be called any time the device wakes up.
	 * Otherwise, the fonts get out of sync
	 */
	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		numberFont = generator.generateFont((int) (menu.getWorldHeight() * (1 - menu.boardHeightPercent) / 2));
		generator.dispose();
	}
}
