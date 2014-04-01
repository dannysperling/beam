package model;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import controller.GameEngine;

public class Menu {

	//Measured in terms of percentage of screen
	public static final float buttonBotY = GameEngine.botBarSize*0.2f;
	public static final float buttonHeight = GameEngine.botBarSize*0.7f;

	//Undo first
	public static final float undoButtonLeftX = 0.02f;
	public static final float undoButtonWidth = 0.31f;

	//Reset second
	public static final float resetButtonLeftX = 0.39f;
	public static final float resetButtonWidth = 0.20f;

	//Then redo
	public static final float redoButtonLeftX = 0.67f;
	public static final float redoButtonWidth = 0.31f;
	
	//Menu button: Top Left corner
	public static final float menuButtonBotY = 1 - GameEngine.topBarSize*0.25f;
	public static final float menuButtonLeftX = 0.02f;
	public static final float menuButtonWidth = 0.15f;
	public static final float menuButtonHeight = GameEngine.topBarSize*0.2f;

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
				return GameEngine.ButtonPress.NEXT_LEVEL;
			} else {
				return GameEngine.ButtonPress.MENU;
			}
		}
		return GameEngine.ButtonPress.SKIPWIN;
	}
	
	static final float worldItemPercent = 1/(3.0f);
	
	public static final Color[] WORLD_COLORS = 
		{	colorFromRGB(75,125,204),
			colorFromRGB(121,224,224),
			colorFromRGB(34,233,38),
			colorFromRGB(240,128,10),
			colorFromRGB(240,22,22),
			colorFromRGB(212,60,204)
		 	};
	
	//This indicates how far down the menu has been scrolled. 0 indicates none at
	//all; the first few levels should be showing. The max value of scrollDownAmount
	//is menuItemHeight*numLevels*screenHeight - screenHeight;
	private int downScrollAmount = 0;
	private int[] worldScrollAmounts;
	private int numWorlds;
	private List<Integer> worldSizes;
	private GameProgress progress;
	
	public final float boardHeightPercent = 0.6f;
	
	public Menu(List<Integer> worldSizes, GameProgress progress){
		this.numWorlds = worldSizes.size();
		this.worldSizes = worldSizes;
		worldScrollAmounts = new int[numWorlds];
		this.progress = progress;
	}
	
	
	private static Color colorFromRGB(int i, int j, int k) {
		return new Color(i/255.0f, j/255.0f,k/255.0f,1);
	}

	private final float PERCENT_OFF_SCROLL = 0.2f;
	private final float RESCROLL_BOUNCE = 0.01f;
	
	
	private float determineScrollAmount(int max, int cur, int ref){
		return Math.max((1 - (Math.abs((float)cur - max) / ref) / PERCENT_OFF_SCROLL) / 2, 0); 
	}
	
	//Returns true if able to scroll entirely; false otherwise (hit end)
	//scrollDownAmount is positive if scrolling down (to higher levels), 
	//negative for up
	public boolean scrollUpDown(int scrollDownAmount, boolean held){
		
		int height = Gdx.graphics.getHeight();
		int itemHeight = getItemHeight();
		
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
				scrollDownAmount -= RESCROLL_BOUNCE * height;
				
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
				scrollDownAmount += RESCROLL_BOUNCE * height;
				
				turned = (scrollDownAmount >= 0);
			}
			//And scroll.
			downScrollAmount += scrollDownAmount;
			return turned;
		} else {
			downScrollAmount += scrollDownAmount;
			return false;
		}
		
	}
	
	public boolean scrollLeftRight(int world, int scrollRightAmount, boolean held){
		
		if (!worldInBounds(world))
			return false;
		
		int worldIndex = world - 1;
		
		int width = Gdx.graphics.getWidth();
		int itemWidth = getItemWidth();
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
				scrollRightAmount -= RESCROLL_BOUNCE * width;
				
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
				scrollRightAmount += RESCROLL_BOUNCE * width;
				
				turned = (scrollRightAmount >= 0);
			}
			//And scroll.
			worldScrollAmounts[worldIndex] += scrollRightAmount;
			return turned;
		} else {
			worldScrollAmounts[worldIndex] += scrollRightAmount;
		}
		return false;
	}
	
	//Scrolls to a specific level
	public void scrollToLevel(int world, int ordinalInWorld){
		
		//Scroll all the things
		for (int i = 0; i < worldSizes.size(); i++){
			if (world - 1 == i){
				//Scroll down
				int scrollToY = Math.max(i - 1, 0);
				int itemHeight = getItemHeight();
				int maxHeight =  itemHeight * numWorlds - Gdx.graphics.getHeight();
				downScrollAmount = Math.min(itemHeight * scrollToY, maxHeight - 1);
				
				//And over
				int over = ordinalInWorld - 1;
				int itemWidth = getItemWidth();
				worldScrollAmounts[i] = Math.max((over - 1) * itemWidth, 0);
			} else {
				worldScrollAmounts[i] = 0;
			}
		}
	}
	
	//Allows draw code to know which levels are on screen
	public int getVerticalScrollAmount(){
		return downScrollAmount;
	}
	
	public int getHorizontalScrollAmount(int world){
		if (world < 1 || world>worldScrollAmounts.length) 
			return 0;
		return worldScrollAmounts[world - 1];
	}
	
	public boolean worldInBounds(int world){
		return world >= 1 && world <= numWorlds;
	}
	
	//Allow some read-through to the game progress
	public int getLevelMoves(int world, int ordinalInWorld){
		return progress.getLevelMoves(world, ordinalInWorld);
	}
	
	public int getLevelStars(int world, int ordinalInWorld){
		return progress.getLevelStars(world, ordinalInWorld);
	}
	
	public boolean isWorldUnlocked(int world){
		return progress.isWorldUnlocked(world);
	}
	
	public boolean isBonusLevelUnlocked(int world){
		return progress.isBonusLevelUnlocked(world);
	}

	public boolean isBonus(int world, int ordinalInWorld){
		return worldSizes.get(world - 1) == ordinalInWorld;
	}
	
	public boolean isLevelUnlocked(int world, int ordinalInWorld){
		return isWorldUnlocked(world) && (!isBonus(world, ordinalInWorld) || isBonusLevelUnlocked(world));
	}
	
	//Returns the world of the level at the given position
	public int getWorldAtPosition(int screenYPos){
		int selectedY = downScrollAmount - screenYPos + Gdx.graphics.getHeight() + 1;
		int itemHeight = getItemHeight();
		return (selectedY / itemHeight) + 1;
	}
	
	//Returns the ordinalInWorld of the level at the given position, INCLUDING LOCKED levels
	public int getLevelAtPositionInWorld(int world, int screenXPos){
		
		int worldIndex = world - 1;
		int selectedX = worldScrollAmounts[worldIndex] + screenXPos;
		int itemWidth = getItemWidth();
		
		int ordinalInWorld = (selectedX / itemWidth) + 1;
		if (ordinalInWorld < 1 || ordinalInWorld > worldSizes.get(worldIndex)){
			return -1;
		}
		
		return ordinalInWorld;
	}
	
	public int getItemHeight(){
		return (int)(Gdx.graphics.getHeight() * worldItemPercent);
	}
	
	public int getItemWidth(){
		return (int)(getItemHeight() * boardHeightPercent * 1.1f);
	}

	public Color colorOfLevel(int world, int ordinalInWorld) {
		Color ret = WORLD_COLORS[world - 1].cpy();
		float factor = 1.0f - 0.75f*((float)(ordinalInWorld - 1)/(float)worldSizes.get(world));
		
		ret.mul(factor);
		ret.a = 1;
		return ret;
	}

	public int sizeOfWorld(int world) {
		if (world < 1 || world > worldSizes.size()) 
			return 0;
		return worldSizes.get(world - 1);
	}

	public static Color colorOfWorld(int world) {
		if (world < 1)
			return Color.BLACK.cpy();
		if (world > WORLD_COLORS.length)
			return Color.WHITE.cpy();
		return WORLD_COLORS[world - 1].cpy();
	}
}
