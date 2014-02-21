package com.me.beam;

import com.badlogic.gdx.ApplicationListener;

public class GameEngine implements ApplicationListener {
	// Simple Objects for now
	private Board b;
	private DrawGame dg;
	private InputHandler inputHandler;
	
	public enum GameState {
		PAUSED, WAITING, DECIDING, MOVING
	}
	
	private GameState state = GameState.PAUSED;
	
	@Override
	public void create() {		
		b = new Board();
		dg = new DrawGame();
		inputHandler = new InputHandler();
	}

	@Override
	public void dispose() {
		dg.dispose();
	}

	@Override
	public void render() {
		inputHandler.handleInput(b, state);
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
