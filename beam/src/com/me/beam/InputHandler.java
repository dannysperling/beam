package com.me.beam;

import com.badlogic.gdx.Gdx;

public class InputHandler {
	
	public GameEngine.GameState handleInput(Board b, GameEngine.GameState state){
		
		if (Gdx.input.isTouched())
			System.out.println("Touch at " + Gdx.input.getX() + ", " + Gdx.input.getY());
		
		return state;
	}
}
