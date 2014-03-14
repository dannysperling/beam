package com.me.beam;

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
	
	static final float menuItemHeight = 1/(7.0f);
	
	//This indicates how far down the menu has been scrolled. 0 indicates none at
	//all; the first few levels should be showing. The max value of scrollDownAmount
	//is menuItemHeight*numLevels*screenHeight - screenHeight;
	private int scrollAmount = 0;
	private int numLevels;
	private GameProgress progress;
	
	public Menu(int numLevels, GameProgress progress){
		this.numLevels = numLevels;
		this.progress = progress;
	}
	
	//Returns true if able to scroll entirely; false otherwise (hit end)
	//scrollDownAmount is positive if scrolling down (to higher levels), 
	//negative for up
	public boolean scroll(int scrollDownAmount){
		
		scrollAmount += scrollDownAmount;
		
		int itemHeight = (int)(Gdx.graphics.getHeight() * menuItemHeight);
		
		int maxHeight =  itemHeight * numLevels - Gdx.graphics.getHeight();
		if (scrollAmount >= maxHeight){
			scrollAmount = maxHeight - 1;
			return false;
		} else if (scrollAmount < 0){
			scrollAmount = 0;
			return false;
		}
		
		return true;
	}
	
	//Scrolls to a specific level
	public void scrollToLevel(int ordinal){
		int scrollTo = Math.max(ordinal - 3, 0);
		int itemHeight = (int)(Gdx.graphics.getHeight() * menuItemHeight);
		int maxHeight =  itemHeight * numLevels - Gdx.graphics.getHeight();
		scrollAmount = Math.min(itemHeight * scrollTo, maxHeight - 1);
	}
	
	//Allows draw code to know which levels are on screen
	public int getScrollAmount(){
		return scrollAmount;
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
	public int getSelectedLevel(int screenYPos){
		int levelOrdinal = getLevelAtPosition(screenYPos);
		if (progress.isUnlocked(levelOrdinal)){
			return levelOrdinal;
		} else {
			return -1;
		}
	}
	
	//Returns the ordinal of the level at the given position, INCLUDING LOCKED levels
	public int getLevelAtPosition(int screenYPos){
		int selectedY = scrollAmount - screenYPos + Gdx.graphics.getHeight() + 1;
		int itemHeight = (int)(Gdx.graphics.getHeight() * menuItemHeight);
		return (selectedY / itemHeight);
	}
}
