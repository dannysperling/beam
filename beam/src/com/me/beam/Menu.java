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

		// Not in one of the buttons
		return GameEngine.ButtonPress.NONE;
	}
}
