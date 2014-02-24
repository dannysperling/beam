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
		state = inputHandler.handleInput(b, state);
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
