package com.me.beam;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public class GameEngine implements ApplicationListener {
	// Simple Objects for now
	private Board b;
	private DrawGame dg;
	private InputHandler inputHandler;
	
	public static Piece movingPiece = null;
	public static List<Tile> movePath = new ArrayList<Tile>();

	//Animation constants in ticks
	private static final int timeOnTileBeforeMove = 60;
	
	private int timeSpentOnTile = 0;
	
	public enum GameState {
		PAUSED, IDLE, DECIDING, MOVING
	}
	
	public enum Color {
		RED, BLUE, GREEN, NONE
	}
	
	public static final float topBarSize = 0.15f;
	public static final float botBarSize = 0.20f;
	public static final float sideEmptySize = 0.02f;
	
	private GameState state = GameState.PAUSED;
	
	@Override
	public void create() {		
		b = new Board(10,22);
		dg = new DrawGame();
		inputHandler = new InputHandler();
	}

	@Override
	public void dispose() {
		dg.dispose();
	}

	@Override
	public void render() {
		//Get input from the user
		state = inputHandler.handleInput(b, state);
		
		//Do things if we're moving
		if (state == GameState.MOVING){
			//Check to see if we actually move yet
			if (timeSpentOnTile < timeOnTileBeforeMove){
				timeSpentOnTile++;
			} else {
				//Reset time on tile
				timeSpentOnTile = 0;
				
				//Get rid of the place we were
				movePath.remove(0);
				
				//Remove previous lasers
				
				//Actually move!
				if (movePath.size() > 0){
					
					//Move to the next tile
					//board.move(movingPiece, movePath.get(0));
					
					//Check for destruction
					
					//Get painted
					
					//Check for destruction
					
					//Form new lasers
					
					//Check for destruction
					
				} else {
					state = GameState.IDLE;
				}
			}
		} //else if (state == GameState.DECIDING){
			//For now, do nothing if the state is deciding. There's nothing to do
		//} //else if (state == GameState.IDLE){
			//For now, do nothing if the state is idle. There's nothing to do
		//}
		
		//Draw the game
		dg.draw(b, state);
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
