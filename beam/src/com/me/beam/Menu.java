package com.me.beam;

import java.util.List;

import com.badlogic.gdx.Gdx;

public class Menu {

	//Measured in terms of percentage of screen
	static final float buttonBotY = GameEngine.botBarSize*0.2f;
	static final float buttonHeight = GameEngine.botBarSize*0.7f;

	//Undo first
	static final float undoButtonLeftX = 0.02f;
	static final float undoButtonWidth = 0.31f;

	//Reset second
	static final float resetButtonLeftX = 0.39f;
	static final float resetButtonWidth = 0.20f;

	//Then redo
	static final float redoButtonLeftX = 0.67f;
	static final float redoButtonWidth = 0.31f;
	
	//Menu button: Top Left corner
	static final float menuButtonBotY = 1 - GameEngine.topBarSize*0.25f;
	static final float menuButtonLeftX = 0.02f;
	static final float menuButtonWidth = 0.15f;
	static final float menuButtonHeight = GameEngine.topBarSize*0.2f;

	//Get which button a pixel is inside of
	public static GameEngine.ButtonPress containingButtonOfPixel(int x, int y){

		//Get width and height of screen
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		
		// y's in the correct range
		if (y > buttonBotY * height && y < (buttonBotY + buttonHeight) * height ){
			//Undo
			if (x > undoButtonLeftX * width && x < (undoButtonLeftX + undoButtonWidth) * width){
				return GameEngine.ButtonPress.UNDO;
			}
			//Reset
			else if (x > resetButtonLeftX * width && x < (resetButtonLeftX + resetButtonWidth) * width){
				return GameEngine.ButtonPress.RESET;
			}
			//Redo
			else if (x > redoButtonLeftX * width && x < (redoButtonLeftX + redoButtonWidth) * width){
				return GameEngine.ButtonPress.REDO;
			}
		}
		
		else if (y > menuButtonBotY * height && y < (menuButtonBotY + menuButtonHeight) * height){
			//Go to menu
			if (x > menuButtonLeftX * width && x < (menuButtonLeftX + menuButtonWidth) * width){
				return GameEngine.ButtonPress.MENU;
			}
		}

		// Not in one of the buttons
		return GameEngine.ButtonPress.NONE;
	}
	
	public static GameEngine.ButtonPress containingButtonOfPixelWon(int x, int y){
		int screenHeight = Gdx.graphics.getHeight();
		int screenWidth = Gdx.graphics.getWidth();
		if(y > (screenHeight - screenWidth) / 2.0f && y < ((screenHeight - screenWidth) / 2.0f) + (screenWidth / 3.0f)){
			if(x < (screenWidth / 3.0f)){
				return GameEngine.ButtonPress.RESET;
			} else if (x > 2 * (screenWidth / 3.0f)){
				return GameEngine.ButtonPress.WON;
			} else {
				return GameEngine.ButtonPress.MENU;
			}
		}
		return GameEngine.ButtonPress.SKIPWIN;
	}
	
	static final float worldItemPercent = 1/(3.0f);
	
	//This indicates how far down the menu has been scrolled. 0 indicates none at
	//all; the first few levels should be showing. The max value of scrollDownAmount
	//is menuItemHeight*numLevels*screenHeight - screenHeight;
	private int downScrollAmount = 0;
	private int[] worldScrollAmounts;
	private int numWorlds;
	private List<Integer> worldSizes;
	private GameProgress progress;
	
	public Menu(List<Integer> worldSizes, GameProgress progress){
		this.numWorlds = worldSizes.size();
		this.worldSizes = worldSizes;
		worldScrollAmounts = new int[numWorlds];
		this.progress = progress;
	}
	
	//Returns true if able to scroll entirely; false otherwise (hit end)
	//scrollDownAmount is positive if scrolling down (to higher levels), 
	//negative for up
	public boolean scrollUpDown(int scrollDownAmount){
		
		downScrollAmount += scrollDownAmount;
		
		int itemHeight = (int)(Gdx.graphics.getHeight() * worldItemPercent);
		
		int maxHeight =  itemHeight * numWorlds - Gdx.graphics.getHeight();
		if (downScrollAmount >= maxHeight){
			downScrollAmount = maxHeight - 1;
			return false;
		} else if (downScrollAmount < 0){
			downScrollAmount = 0;
			return false;
		}
		
		return true;
	}
	
	//This should be between 1, 2, and 3. Otherwise, you'd never be able to select the final level...
	private final int NUM_TILES_SHOWING_RIGHT = 1;
	
	public boolean scrollLeftRight(int world, int scrollRightAmount){
		
		worldScrollAmounts[world] += scrollRightAmount;
		int itemWidth = (int)(Gdx.graphics.getWidth() * worldItemPercent);
		
		int maxWidth =  Math.max(itemWidth * (worldSizes.get(world) - NUM_TILES_SHOWING_RIGHT), 0);
		if (worldScrollAmounts[world] >= maxWidth){
			worldScrollAmounts[world] = maxWidth - 1;
			return false;
		} else if (worldScrollAmounts[world] < 0){
			worldScrollAmounts[world] = 0;
			return false;
		}
		
		return true;
	}
	
	//Scrolls to a specific level
	public void scrollToLevel(int ordinal){
		
		int prevLevels = 0;
		boolean foundAlready = false;
		//Scroll all the things
		for (int i = 0; i < worldSizes.size(); i++){
			if (!foundAlready && worldSizes.get(i) + prevLevels > ordinal){
				foundAlready = true;
				
				//Scroll down
				int scrollToY = Math.max(i - 1, 0);
				int itemHeight = (int)(Gdx.graphics.getHeight() * worldItemPercent);
				int maxHeight =  itemHeight * numWorlds - Gdx.graphics.getHeight();
				downScrollAmount = Math.min(itemHeight * scrollToY, maxHeight - 1);
				
				//And over
				int over = ordinal - prevLevels;
				int itemWidth = (int)(Gdx.graphics.getWidth() * worldItemPercent);
				worldScrollAmounts[i] = Math.max((over - 1) * itemWidth, 0);
			} else {
				worldScrollAmounts[i] = 0;
			}
			prevLevels += worldSizes.get(i);
		}
	}
	
	//Allows draw code to know which levels are on screen
	public int getVerticalScrollAmount(){
		return downScrollAmount;
	}
	
	public int getHorizontalScrollAmount(int world){
		return worldScrollAmounts[world];
	}
	
	//Allow some read-through to the game progress
	public int getLevelMoves(int ordinal){
		return progress.getLevelMoves(ordinal);
	}
	
	public int getLevelStars(int ordinal){
		return progress.getLevelStars(ordinal);
	}
	
	public boolean isUnlocked(int ordinal){
		return progress.isUnlocked(ordinal);
	}
	
	
	//Returns the ordinal of the selected level, or -1 if selected level is locked.
	public int getSelectedLevel(int screenXPos, int screenYPos){
		int levelOrdinal = getLevelAtPosition(screenXPos, screenYPos);
		if (progress.isUnlocked(levelOrdinal)){
			return levelOrdinal;
		} else {
			return -1;
		}
	}
	
	//Returns the world of the level at the given position
	public int getWorldAtPosition(int screenYPos){
		int selectedY = downScrollAmount - screenYPos + Gdx.graphics.getHeight() + 1;
		int itemHeight = (int)(Gdx.graphics.getHeight() * worldItemPercent);
		return (selectedY / itemHeight);
	}
	
	//Returns the ordinal of the level at the given position, INCLUDING LOCKED levels
	public int getLevelAtPosition(int screenXPos, int screenYPos){
		
		int world = getWorldAtPosition(screenYPos);
		int selectedX = worldScrollAmounts[world] + screenXPos;
		int itemWidth = (int)(Gdx.graphics.getWidth() * worldItemPercent);
		
		int withinWorld = (selectedX / itemWidth);
		if (withinWorld >= worldSizes.get(world)){
			return -1;
		}
		
		int prevLevels = 0;
		for (int i = 0; i < world; i++){
			prevLevels += worldSizes.get(i);
		}
		return prevLevels + withinWorld;
	}
}
