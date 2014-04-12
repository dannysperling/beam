package model;

import java.util.List;

import utilities.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import controller.GameEngine;

public class Menu {

	/**
	 * First, menu contains static variables and a method for use buttons on the level screen
	 */

	//Button positions
	//Undo 
	public static final float B_UNDO_WIDTH = 0.27f;
	public static final float B_UNDO_LEFT_X = 0.08f;
	//Reset 
	public static final float B_RESET_WIDTH = 0.27f;
	public static final float B_RESET_LEFT_X = 1 - B_RESET_WIDTH - 0.08f;
	
	//Menu 
	public static final float B_MENU_WIDTH = 0.27f;
	public static final float B_MENU_LEFT_X = 0.02f;
	//Next Level
	public static final float B_NEXT_LEVEL_WIDTH = 0.35f;
	public static final float B_NEXT_LEVEL_LEFT_X = 1 - B_NEXT_LEVEL_WIDTH - 0.02f;
	//Info
	public static final float B_INFO_WIDTH = 0.08f;
	public static final float B_INFO_LEFT_X = 0.5f - B_INFO_WIDTH / 2;


	/**
	 * Determines which button, if any, the (x, y) coordinate falls within, assuming
	 * the game is currently showing a standard level
	 * 
	 * @param x
	 * 				x coordinate of the press
	 * @param y
	 * 				y coordinate of the press
	 * @param botYCoord
	 * 				y coordinate of the bottom of the top buttons for this board
	 * @return
	 * 				Which button was clicked. Options are MENU, UNDO, RESET, NEXT_LEVEL, INFO
	 */
	public static GameEngine.ButtonPress containingButtonOfPixelLevelScreen(int x, int y, int botYCoord){

		//Get width and height of screen
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		
		// Bottom button presses
		if (y < Constants.GAME_BUTTON_HEIGHT * height ){
			//Menu
			if (x > B_MENU_LEFT_X * width && x < (B_MENU_LEFT_X + B_MENU_WIDTH) * width){
				return GameEngine.ButtonPress.MENU;
			}
			//Next Level
			else if (x > B_NEXT_LEVEL_LEFT_X * width && x < (B_NEXT_LEVEL_LEFT_X + B_NEXT_LEVEL_WIDTH) * width){
				return GameEngine.ButtonPress.NEXT_LEVEL;
			}
			//Info
			else if (x > B_INFO_LEFT_X * width && x < (B_INFO_LEFT_X + B_INFO_WIDTH) * width){
				return GameEngine.ButtonPress.INFO;
			}
		}
		
		// Above board button presses
		else if (y > botYCoord && y < botYCoord + Constants.GAME_BUTTON_HEIGHT * height){
			//Undo
			if (x > B_UNDO_LEFT_X * width && x < (B_UNDO_LEFT_X + B_UNDO_WIDTH) * width){
				return GameEngine.ButtonPress.UNDO;
			}
			//Reset
			else if (x > B_RESET_LEFT_X * width && x < (B_RESET_LEFT_X + B_RESET_WIDTH) * width){
				return GameEngine.ButtonPress.RESET;
			}
		}

		// Not in one of the buttons
		return GameEngine.ButtonPress.NONE;
	}
	
	
	
	
	/*******************************************************************************/

	
	
	
	/**
	 * This indicates how far down the menu has been scrolled. 0 indicates none at
	 * all; the first few levels should be showing. The max value of scrollDownAmount,
	 * not including the bounce back, is menuItemHeight*numLevels*screenHeight - screenHeight;
	 */
	private int downScrollAmount = 0;
	
	/**
	 * This indicates the scroll of each world. 0 indicates the first level is at the left.
	 * Positive scrolling means the world is scrolled further to the right.
	 */
	private int[] worldScrollAmounts;
	
	/**
	 * Store values regarding the worlds for convenience
	 */
	private int numWorlds;
	private List<Integer> worldSizes;
	
	/**
	 * Keeps a reference to the game progress to be able to get whether
	 * levels are locked and their star counts
	 */
	private GameProgress progress;
	
	/**
	 * Percentages of how much the world is of the screen, and how much the
	 * board is of a given world
	 */
	public final float worldItemPercent = 1/(3.0f);
	public final float boardHeightPercent = 0.6f;
	
	/**
	 * Constructs a menu item, with a reference to the sizes of each of the
	 * worlds and the player's progress for locking and stars.
	 * 
	 * @param worldSizes
	 * 					Indicates how many levels are in each world, for
	 * 					scrolling purposes
	 * @param progress	
	 * 					Indicates the player's progress
	 */
	public Menu(List<Integer> worldSizes, GameProgress progress){
		this.numWorlds = worldSizes.size();
		this.worldSizes = worldSizes;
		worldScrollAmounts = new int[numWorlds];
		this.progress = progress;
	}
	
	/**
	 * Limits the scroll amount of a given scroll that would be going off the
	 * screen. This slows down scrolling as you go off screen, and prevents
	 * scrolling going off of the PERCENT_OFF_SCROLL of the screen.
	 * 
	 * @param border
	 * 				The border value. This would be 0 for the left or top,
	 * 				or the max height or width for up-down or left-right
	 * @param attemptScroll
	 * 				The amount that is trying to be scrolled. This is where
	 * 				the scroll would be if there weren't a limitation
	 * @param referenceDim
	 * 				The dimension referenced. This is the screen height if 
	 * 				the scrolling is up-down, or the screen width if the
	 * 				scrolling is left-right
	 * @return
	 * 				The new scroll amount
	 */
	private float determineScrollAmount(int border, int attemptScroll, int referenceDim){
		//Determine how far past the border it was
		float pastBorder = Math.abs(attemptScroll - border);
		
		//The percentage past the border
		float percentPast = pastBorder / referenceDim;
		
		//Can't be less than zero past, in case its a very far scroll
		return Math.max((1 - percentPast / Constants.PERCENT_OFF_SCROLL) / 2, 0); 
	}
	
	
	/**
	 * Scrolls in the up-down direction on the screen. Allows the game
	 * to be scrolled partially past the end. If the user isn't holding the
	 * screen, the level will bounce back over time. Otherwise, it stays where
	 * the user is holding it.
	 * 
	 * @param scrollDownAmount
	 * 			Positive if scrolling up, negative for down.
	 * @param held
	 * 			Whether the player is holding the screen. If so, can't
	 * 			bounce back (would be unnatural to the player).
	 * @return
	 * 			True if the level moved the opposite direction of the scrolling
	 * 			This indicates that it had bounced back, and momentum should stop
	 * 			being applied in that direction.
	 */
	public boolean scrollUpDown(int scrollDownAmount, boolean held){
		
		int height = Gdx.graphics.getHeight();
		int itemHeight = getWorldHeight();
		
		int maxHeight =  itemHeight * numWorlds - Gdx.graphics.getHeight() - 1;
		
		//If going off bottom
		if (downScrollAmount + scrollDownAmount > maxHeight){
			//Take the scroll down amount past the bottom
			scrollDownAmount -= Math.max(maxHeight - downScrollAmount, 0);
			
			//Scroll down to bottom if not there
			boolean wasAbove = downScrollAmount < maxHeight;
			if (wasAbove)
				downScrollAmount = maxHeight;
			
			//Get reduced amount based on how close to end currently
			scrollDownAmount *= determineScrollAmount(maxHeight, downScrollAmount, height);
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasAbove){
				scrollDownAmount -= Constants.RESCROLL_BOUNCE * height;
				
				//Managed to reverse
				turned = (scrollDownAmount <= 0);
			}
			//And scroll.
			downScrollAmount += scrollDownAmount;
			return turned;
			
		} else if (downScrollAmount + scrollDownAmount < 0){
			//Take the scroll down amount up past the top
			scrollDownAmount += Math.min(-downScrollAmount, 0);
			
			//Scroll up to top if not there
			boolean wasBelow = downScrollAmount > 0;
			if (wasBelow)
				downScrollAmount = 0;
			
			//Get reduced amount based on how close to end currently
			scrollDownAmount *= determineScrollAmount(0, downScrollAmount, height);
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasBelow){
				scrollDownAmount += Constants.RESCROLL_BOUNCE * height;
				
				//Managed to reverse
				turned = (scrollDownAmount >= 0);
			}
			//And scroll.
			downScrollAmount += scrollDownAmount;
			return turned;
		} else {
			//Regular case: just scroll
			downScrollAmount += scrollDownAmount;
			return false;
		}
		
	}
	
	/**
	 * Scrolls in the left-right direction on a specific level. Allows the game
	 * to be scrolled partially past the end. If the user isn't holding the
	 * screen, the world will bounce back over time. Otherwise, it stays where
	 * the user is holding it.
	 * 
	 * @param world
	 * 			The world being scrolled
	 * @param scrollRightAmount
	 * 			Positive if scrolling right, negative for left.
	 * @param held
	 * 			Whether the player is holding the screen. If so, can't
	 * 			bounce back (would be unnatural to the player).
	 * @return
	 * 			True if the level moved the opposite direction of the scrolling
	 * 			This indicates that it had bounced back, and momentum should stop
	 * 			being applied in that direction.
	 */
	public boolean scrollLeftRight(int world, int scrollRightAmount, boolean held){
		
		//If it's not in bounds, don't do anything!
		if (!worldInBounds(world))
			return false;
		
		int worldIndex = world - 1;
		
		int width = Gdx.graphics.getWidth();
		int itemWidth = getLevelItemWidth();
		int maxWidth =  Math.max(itemWidth * worldSizes.get(worldIndex) - width - 1, 0);
		
		//Account for the fact that world is an ordinal
		if (worldScrollAmounts[worldIndex] > maxWidth){
			//Take the scroll right amount past the right side
			scrollRightAmount -= Math.max(maxWidth - worldScrollAmounts[worldIndex], 0);
			
			//Scroll to right if not there
			boolean wasRight = worldScrollAmounts[worldIndex] < maxWidth;
			if (wasRight)
				worldScrollAmounts[worldIndex] = maxWidth;
			
			//Get reduced amount based on how close to end currently
			scrollRightAmount *= determineScrollAmount(maxWidth, worldScrollAmounts[worldIndex], width);
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasRight){
				scrollRightAmount -= Constants.RESCROLL_BOUNCE * width;
				
				//Managed to reverse
				turned = (scrollRightAmount <= 0);
			}
			//And scroll.
			worldScrollAmounts[worldIndex] += scrollRightAmount;
			return turned;
		} else if (worldScrollAmounts[worldIndex] < 0){
			//Take the scroll down amount up past the top
			scrollRightAmount += Math.min(-worldScrollAmounts[worldIndex], 0);
			
			//Scroll up to top if not there
			boolean wasLeft = worldScrollAmounts[worldIndex] > 0;
			if (wasLeft)
				worldScrollAmounts[worldIndex] = 0;
			
			//Get reduced amount based on how close to end currently
			scrollRightAmount *= determineScrollAmount(0, worldScrollAmounts[worldIndex], width);
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasLeft){
				scrollRightAmount += Constants.RESCROLL_BOUNCE * width;
				
				//Managed to reverse
				turned = (scrollRightAmount >= 0);
			}
			//And scroll.
			worldScrollAmounts[worldIndex] += scrollRightAmount;
			return turned;
		} else {
			//Regular scrolling
			worldScrollAmounts[worldIndex] += scrollRightAmount;
		}
		return false;
	}
	
	/**
	 * Scrolls the screen to a specific level. This is useful when returning
	 * back to the main menu after playing on a level.
	 * 
	 * @param world
	 * 				The world of the level to scroll to
	 * @param ordinalInWorld
	 * 				The ordinal of the level in the world to scroll to
	 */
	public void scrollToLevel(int world, int ordinalInWorld){
		
		int worldIndex = world - 1;
		int width = Gdx.graphics.getWidth();
		
		//Scroll all the things
		for (int i = 0; i < worldSizes.size(); i++){
			if (worldIndex == i){
				//Scroll down
				int scrollToY = Math.max(worldIndex - 1, 0);
				int itemHeight = getWorldHeight();
				int maxHeight =  itemHeight * numWorlds - Gdx.graphics.getHeight();
				downScrollAmount = Math.min(itemHeight * scrollToY, maxHeight - 1);
				
				//And over
				int over = ordinalInWorld - 1;
				int itemWidth = getLevelItemWidth();
				int desiredScreenPos = (width - itemWidth)/2;
				worldScrollAmounts[worldIndex] = Math.max(over * itemWidth - desiredScreenPos, 0);
				
				//Don't go past maximum width either
				int maxWidth =  Math.max(itemWidth * worldSizes.get(worldIndex) - width - 1, 0);
				worldScrollAmounts[worldIndex] = Math.min(maxWidth, worldScrollAmounts[worldIndex]);
				
			} else {
				//Set all other worlds' scroll to 0
				worldScrollAmounts[i] = 0;
			}
		}
	}
	
	/**
	 * Get the current amount of vertical scrolling
	 * 
	 * @return
	 * 			How far down the screen has been scrolled
	 */
	public int getVerticalScrollAmount(){
		return downScrollAmount;
	}
	
	/**
	 * Gets the current horizontal scroll for a given world
	 * 
	 * @param world
	 * 			The world to get the scroll for
	 * @return
	 * 			How far to the right that world is scrolled
	 */
	public int getHorizontalScrollAmount(int world){
		if (world < 1 || world>worldScrollAmounts.length) 
			return 0;
		return worldScrollAmounts[world - 1];
	}
	
	/**
	 * Checks to see if a world ordinal is valid
	 * 
	 * @param world
	 * 			The world to check
	 * @return
	 * 			True if in bounds, false otherwise
	 */
	public boolean worldInBounds(int world){
		return world >= 1 && world <= numWorlds;
	}
	
	/**
	 * Allow read-through to the progress - best moves on a given level
	 */
	public int getLevelMoves(int world, int ordinalInWorld){
		return progress.getLevelMoves(world, ordinalInWorld);
	}
	
	/**
	 * Allow read-through to the progress - stars on a given level
	 */
	public int getLevelStars(int world, int ordinalInWorld){
		return progress.getLevelStars(world, ordinalInWorld);
	}
	
	/**
	 * Allow read-through to the progress - is a world unlocked
	 */
	public boolean isWorldUnlocked(int world){
		return progress.isWorldUnlocked(world);
	}
	
	/**
	 * Allow read-through to the progress - is the bonus level of
	 * a world unlocked
	 */
	public boolean isBonusLevelUnlocked(int world){
		return progress.isBonusLevelUnlocked(world);
	}

	/**
	 * Checks to see if a given level is a bonus level in that world
	 */
	public boolean isBonus(int world, int ordinalInWorld){
		return worldSizes.get(world - 1) == ordinalInWorld;
	}
	
	/**
	 * Checks if a given level is unlocked, based on whether the world
	 * is unlocked and, if so, if its not the bonus level or the bonus
	 * level for that world is unlocked.
	 */
	public boolean isLevelUnlocked(int world, int ordinalInWorld){
		return isWorldUnlocked(world) && (!isBonus(world, ordinalInWorld) || isBonusLevelUnlocked(world));
	}
	
	/**
	 * Returns the world currently at a given screen height
	 * 
	 * @param screenYPos
	 * 			The height on the screen
	 * @return
	 * 			The world at that height. Can be out of bounds
	 */
	public int getWorldAtPosition(int screenYPos){
		int selectedY = downScrollAmount - screenYPos + Gdx.graphics.getHeight() + 1;
		int itemHeight = getWorldHeight();
		return (selectedY / itemHeight) + 1;
	}
	
	/**
	 * Gets the ordinal of the level in a given world at a given
	 * screen position in terms of X. Will return -1 if no level
	 * at that position.
	 * @param world
	 * 			The world to choose the ordinal from
	 * @param screenXPos
	 * 			The x position from 0 to width - 1 on the screen
	 * @return
	 * 			The ordinal of the level at that position, or -1
	 * 			if no level is at that position.
	 */
	public int getLevelAtPositionInWorld(int world, int screenXPos){
		
		//Determine position on the screen
		int worldIndex = world - 1;
		int selectedX = worldScrollAmounts[worldIndex] + screenXPos;
		int itemWidth = getLevelItemWidth();
		
		//Calculate the given ordinal
		int ordinalInWorld = (selectedX / itemWidth) + 1;
		
		//Make sure it's in bounds
		if (ordinalInWorld < 1 || ordinalInWorld > worldSizes.get(worldIndex)){
			return -1;
		}
		return ordinalInWorld;
	}
	
	/**
	 * Gets the height of each of the worlds on the menu screen
	 */
	public int getWorldHeight(){
		return (int)(Gdx.graphics.getHeight() * worldItemPercent);
	}
	
	/**
	 * Gets the width of each of the level items on the menu screen
	 */
	public int getLevelItemWidth(){
		return (int)(getWorldHeight() * boardHeightPercent * 1.1f);
	}
	
	/**
	 * Gets the number of levels in a given world
	 * 
	 * @param world
	 * 			The ordinal of the world
	 * @return
	 * 			The size of that world, or 0 if out of bounds
	 */
	public int sizeOfWorld(int world) {
		if (world < 1 || world > worldSizes.size()) 
			return 0;
		return worldSizes.get(world - 1);
	}
	
	
	
	
	/*******************************************************************************/
	
	
	
	
	/**
	 * The remainder below here is used for determining colors of worlds and
	 * levels in the main menu. These are static variables and methods.
	 */

	/**
	 * Get the color of a level based on its world and position in that world
	 * 
	 * @param world
	 * 					Which world the level is in
	 * @param ordinalInWorld
	 * 					The ordinal of the level in that world
	 * @return
	 * 					The background color of the level
	 */
	public Color colorOfLevel(int world, int ordinalInWorld) {
		
		//Convert the world and ordinal into indices (zero - indexed)
		int worldIndex = world - 1;
		int levelIndex = ordinalInWorld - 1;
		
		//Copy out the color of the world
		Color ret = Constants.WORLD_COLORS[worldIndex].cpy();
		
		//Multiply the color based on its position
		float factor = 1.0f - 0.75f*((float)(levelIndex)/(float)worldSizes.get(worldIndex));
		ret.mul(factor);
		
		//Set alpha channel to opaque
		ret.a = 1;
		
		return ret;
	}

	/**
	 * Get the color of a given world. Black if its less than the first,
	 * white if its more than the second.
	 * 
	 * @param world
	 * 				Which world to get the color of
	 * @return
	 * 				The color of that world
	 */
	public static Color colorOfWorld(int world) {
		// Too low, do black
		if (world < 1)
			return Color.BLACK.cpy();
		
		// Too high, do white
		if (world > Constants.WORLD_COLORS.length)
			return Color.WHITE.cpy();
		
		//In between, pull from the world colors
		int worldIndex = world - 1;
		return Constants.WORLD_COLORS[worldIndex].cpy();
	}
}
