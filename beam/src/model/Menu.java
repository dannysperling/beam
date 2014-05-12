package model;

import java.util.List;

import utilities.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import controller.GameEngine;

public class Menu {

	/**
	 * Menu contains static variables and a method for use buttons on the level screen
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
	public static final float B_INFO_WIDTH = 0.10f;
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
			else if (GameEngine.getTutorial() == null){
				if(x > B_INFO_LEFT_X * width && x < (B_INFO_LEFT_X + B_INFO_WIDTH) * width){
					return GameEngine.ButtonPress.INFO;
				}
			} else if (GameEngine.getTutorial() != null){
				if(x > ((width - (2 * Menu.B_INFO_WIDTH * width)) / 2.0f) && x < ((width - (2 * Menu.B_INFO_WIDTH * width)) / 2.0f) + (B_INFO_WIDTH * width)){
					return GameEngine.ButtonPress.INFO;
				} else if ( x > width/2.0f && x < ((width / 2.0f) + B_INFO_WIDTH * width)){
					return GameEngine.ButtonPress.TUTORIAL;
				}
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
	 * Static constants for positions on the title and settings screens
	 */
	//Button positions
	public static final float TITLE_SCREEN_BUTTON_HEIGHT = 0.04f;
	
	//Settings
	public static final float B_SETTINGS_WIDTH = 0.3f;
	public static final float B_SETTINGS_BOT_Y = 0.13f;
	
	//Play
	public static final float B_PLAY_BOT_Y = 0.20f;
	
	//Loading
	public static final float B_LOADING_BOT_Y = 0.17f;
	
	/**
	 * Determines which button, if any, the (x, y) coordinate falls within, assuming
	 * the game is currently showing a standard level
	 * 
	 * @param x
	 * 				x coordinate of the press
	 * @param y
	 * 				y coordinate of the press
	 * @return
	 * 				Which title option was clicked. Options are PLAY, SETTINGS
	 */
	public static GameEngine.TitleOption containingButtonOfPixelTitleScreen(int x, int y){

		//Get width and height of screen
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		
		// Bottom button presses
		if (y > B_SETTINGS_BOT_Y * height && y < (B_SETTINGS_BOT_Y + TITLE_SCREEN_BUTTON_HEIGHT) * height){
			//Menu
			if (x > (0.5 - B_SETTINGS_WIDTH / 2) * width && x < (0.5 + B_SETTINGS_WIDTH / 2) * width){
				return GameEngine.TitleOption.SETTINGS;
			}
		}
		// Not in the settings button - they clicked play!
		return GameEngine.TitleOption.PLAY;
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
	public int numWorlds;
	public List<Integer> worldSizes;
	
	
	/**
	 * Keeps a reference to the game progress to be able to get whether
	 * levels are locked and their star counts
	 */
	private GameProgress progress;
	
	/**
	 * Percentages of how much the world is of the screen, how much the
	 * board is of a given world, and how large the space between worlds
	 * is (as percent of screen). If worldItem = k*(1/n) and 
	 * interworldSpace = (1-k)/(n-1) then exactly n full worlds fit on screen.
	 */
	public final float worldItemPercent = 1/(3.0f)*0.90f;
	public final float boardHeightPercent = 0.52f;
	public final float starHeightPercent = 0.22f;
	public final float interWorldSpacePercent = 0.05f;
	
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
	 * 				The new scroll amount factor
	 */
	private float determineScrollAmount(int border, int attemptScroll, int referenceDim){
		
		//Figure out how far past it is
		float percentPast = determinePercentPast(border, attemptScroll, referenceDim);
		
		//Can't be less than zero past, in case its a very far scroll
		return Math.max((1 - percentPast) / 2, 0); 
	}
	
	/**
	 * Figure out how far past, by percentage of the allowable amount, scrolling
	 * has occurred
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
	 * 				The percentage of the allowable amount off
	 */
	private float determinePercentPast(int border, int attemptScroll, int referenceDim){
		//Determine how far past the border it was
		float pastBorder = Math.abs(attemptScroll - border);
				
		//The percentage past the border
		float percentPast = pastBorder / referenceDim;
		
		//Factor over the PERCENT_OFF allowance
		return percentPast/Constants.PERCENT_OFF_SCROLL;
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
		
		int maxHeight =  (itemHeight * numWorlds)+(getSpaceHeight() * (numWorlds - 1)) - Gdx.graphics.getHeight() - 1;
		
		//If going off bottom
		if (downScrollAmount + scrollDownAmount > maxHeight){
			//Take the scroll down amount past the bottom
			scrollDownAmount -= Math.max(maxHeight - downScrollAmount, 0);
			
			//Scroll down to bottom if not there
			boolean wasAbove = downScrollAmount < maxHeight;
			if (wasAbove)
				downScrollAmount = maxHeight;
			
			//Get reduced amount based on how close to end currently
			boolean goingBack = scrollDownAmount < 0;
			scrollDownAmount *= determineScrollAmount(maxHeight, downScrollAmount, height);
			//Always allow to scroll back down
			if (goingBack){
				scrollDownAmount = Math.min(-1, scrollDownAmount);
			}
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasAbove){
				scrollDownAmount -= Constants.RESCROLL_BOUNCE * height;
				
				//Managed to reverse
				turned = (scrollDownAmount <= 0);
			}
			//And scroll.
			downScrollAmount += scrollDownAmount;
			
			//Prevent going past bottom
			int farthestDown = (int)(maxHeight + height * Constants.PERCENT_OFF_SCROLL);
			downScrollAmount = Math.min(farthestDown, downScrollAmount);
			
			return turned;
			
		} else if (downScrollAmount + scrollDownAmount < 0){
			//Take the scroll down amount up past the top
			scrollDownAmount += Math.min(-downScrollAmount, 0);
			
			//Scroll up to top if not there
			boolean wasBelow = downScrollAmount > 0;
			if (wasBelow)
				downScrollAmount = 0;
			
			//Get reduced amount based on how close to end currently
			boolean goingBack = scrollDownAmount > 0;
			scrollDownAmount *= determineScrollAmount(0, downScrollAmount, height);
			if (goingBack){
				scrollDownAmount = Math.max(1, scrollDownAmount);
			}
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasBelow){
				scrollDownAmount += Constants.RESCROLL_BOUNCE * height;
				
				//Managed to reverse
				turned = (scrollDownAmount >= 0);
			}
			
			//And scroll.
			downScrollAmount += scrollDownAmount;
			
			//Prevent from going past bottom
			int farthestUp = (int)(-height * Constants.PERCENT_OFF_SCROLL);
			downScrollAmount = Math.max(farthestUp, downScrollAmount);
			
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
		
		
		int worldIndex = world - 1;
		int width = Gdx.graphics.getWidth();
		int itemWidth = getLevelItemWidth();
		
		//Scroll all other levels back to the middle if they need to be
		for (int i = 0; i < numWorlds; i++){
			if (i != worldIndex){
				int maxWidth =  Math.max(itemWidth * worldSizes.get(i) - width - 1, 0);
				int change = (int)(Constants.RESCROLL_BOUNCE * width);
				if (worldScrollAmounts[i] > maxWidth){
					worldScrollAmounts[i] = Math.max(worldScrollAmounts[i] - change, maxWidth);
				} else if (worldScrollAmounts[i] < 0){
					worldScrollAmounts[i] = Math.min(worldScrollAmounts[i] + change, 0);
				}
			}
		}
		
		//If it's not in bounds, don't do anything!
		if (!worldInBounds(world))
			return false;
		
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
			boolean goingLeft = scrollRightAmount < 0;
			scrollRightAmount *= determineScrollAmount(maxWidth, worldScrollAmounts[worldIndex], width);
			//Always allow to scroll back left
			if (goingLeft){
				scrollRightAmount = Math.min(-1, scrollRightAmount);
			}
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasRight){
				scrollRightAmount -= Constants.RESCROLL_BOUNCE * width;
				
				//Managed to reverse
				turned = (scrollRightAmount <= 0);
			}
			//And scroll.
			worldScrollAmounts[worldIndex] += scrollRightAmount;
			
			//Prevent going past right edge
			int farthestRight = (int)(maxWidth + width * Constants.PERCENT_OFF_SCROLL);
			worldScrollAmounts[worldIndex] = Math.min(farthestRight, worldScrollAmounts[worldIndex]);
			
			return turned;
		} else if (worldScrollAmounts[worldIndex] < 0){
			//Take the scroll down amount up past the top
			scrollRightAmount += Math.min(-worldScrollAmounts[worldIndex], 0);
			
			//Scroll up to top if not there
			boolean wasLeft = worldScrollAmounts[worldIndex] > 0;
			if (wasLeft)
				worldScrollAmounts[worldIndex] = 0;
			
			//Get reduced amount based on how close to end currently
			boolean goingRight = scrollRightAmount > 0;
			scrollRightAmount *= determineScrollAmount(0, worldScrollAmounts[worldIndex], width);
			if (goingRight){
				scrollRightAmount = Math.max(1,  scrollRightAmount);
			}
			
			//Figure out reverse effect if not held
			boolean turned = false;
			if (!held && !wasLeft){
				scrollRightAmount += Constants.RESCROLL_BOUNCE * width;
				
				//Managed to reverse
				turned = (scrollRightAmount >= 0);
			}
			//And scroll.
			worldScrollAmounts[worldIndex] += scrollRightAmount;
			
			//Prevent going past left edge
			int farthestLeft = (int)(-width * Constants.PERCENT_OFF_SCROLL);
			worldScrollAmounts[worldIndex] = Math.max(farthestLeft, worldScrollAmounts[worldIndex]);
			
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
				int maxHeight =  (itemHeight * numWorlds)+(getSpaceHeight() * (numWorlds - 1)) - Gdx.graphics.getHeight() - 1;
				downScrollAmount = Math.min((itemHeight + getSpaceHeight()) * scrollToY, maxHeight - 1);
				
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
	 * Returns number of non-bonus stars earned in the given world
	 * @param world
	 * @return
	 */
	public int getNumStarsEarned(int world){
		return progress.getBaseWorldStars(world);
	}
	
	/**
	 * Returns the number of stars needed in 'world' to unlock
	 * 'world+1'. That is to say world n+1 is unlocked iff
	 * genNumStarsEarned(n)>=getNumStarsNeeded(n). This may
	 * be slightly misleading.
	 * @param world
	 * @return
	 */
	public int getNumStarsNeeded(int world){
		return progress.numStarsNeeded(world);
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
	 * Checks if the next level is unlocked after this one
	 */
	public boolean isNextLevelUnlocked(int currentWorld, int currentOrdinalInWorld) {
		
		int nextLevelOrdinal = currentOrdinalInWorld + 1;
		int nextWorld = currentWorld;
		
		//Will never go to the bonus level
		if (nextLevelOrdinal >= worldSizes.get(nextWorld - 1)){
			nextLevelOrdinal = 1;
			nextWorld++;
		}
		
		return isLevelUnlocked(nextWorld, nextLevelOrdinal);
	}
	
	/**
	 * Check if this level is the last level in the world
	 */
	public boolean isLastLevelInWorld(int world, int ordinalInWorld) {

		return (ordinalInWorld >= worldSizes.get(world-1) - 1);
	}
	
	/**
	 * Returns the world currently at a given screen height
	 * 
	 * @param screenYPos
	 * 			The height on the screen
	 * @return
	 * 			The world at that height. Can be out of bounds.
	 * 			If screenYPos selected between world, will return 1000 + previous level
	 */
	public int getWorldAtPosition(int screenYPos){
		int selectedY = downScrollAmount - screenYPos + Gdx.graphics.getHeight() + 1;
		int itemHeight = getWorldHeight();
		int itemHeightPlus = itemHeight + getSpaceHeight();
		
		//Determine if we're between worlds
		int associatedWorld = (selectedY / itemHeightPlus) + 1;
		float fractionAbove = (selectedY % itemHeightPlus) / (float)itemHeightPlus;
		
		//If in the space between, add 1000
		if (fractionAbove > ((float)itemHeight) / itemHeightPlus)
			return associatedWorld + 1000;
		return associatedWorld;
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
		float ordinalInWorldFrac = (selectedX / (float) itemWidth);
		
		//Handle the fact that negative truncates to zero
		int ordinalInWorld = (int) ordinalInWorldFrac;
		if (ordinalInWorldFrac >= 0)
			ordinalInWorld++;
		
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
	 * Gets the height of the gap between worlds
	 */
	public int getSpaceHeight(){
		return (int)(Gdx.graphics.getHeight() * interWorldSpacePercent);
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
		float factor = Constants.START_COLOR_MUL - (1-Constants.END_COLOR_MUL)*((float)(levelIndex)/(float)worldSizes.get(worldIndex));
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
