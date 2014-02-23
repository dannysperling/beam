package com.me.beam;

import com.badlogic.gdx.Gdx;

public class InputHandler {
	
	public GameEngine.GameState handleInput(Board b, GameEngine.GameState state){
		
		if (Gdx.input.isTouched()){
			
			int yPos = Gdx.graphics.getHeight() - Gdx.input.getY() - 1;
			
			Tile t = b.getTileAtPosition(Gdx.input.getX(), yPos);
			if (t != null){
				System.out.println("In tile " + t.getXCoord() + ", " + t.getYCoord());
			}
		}	
		
		return state;
	}
}
