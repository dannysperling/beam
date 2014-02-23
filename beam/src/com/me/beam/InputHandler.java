package com.me.beam;

import com.badlogic.gdx.Gdx;

public class InputHandler {
	
	public GameEngine.GameState handleInput(Board b, GameEngine.GameState state){
		
		if (Gdx.input.isTouched()){
			Tile t = b.getTileAtPosition(Gdx.input.getX(), Gdx.input.getY());
			if (t != null){
				System.out.println("In tile " + t.getXCoord() + ", " + t.getYCoord());
			}
		}	
		
		return state;
	}
}
