package com.me.beam;

import com.badlogic.gdx.Gdx;

public class InputHandler {

	public GameEngine.GameState handleInput(Board b, GameEngine.GameState state){
		
		if(state == GameEngine.GameState.IDLE) {
			if (Gdx.input.isTouched()){
				Tile t = b.getTileAtPosition(getX(), getY());
				Piece piece = b.getPieceOnTile(t);
				if (piece != null) {
					GameEngine.movingPiece = piece;
					GameEngine.movePath.add(t);
					return GameEngine.GameState.DECIDING;
				}
				else {
					return GameEngine.GameState.IDLE;
				}
			}
		}	
		
		if(state == GameEngine.GameState.DECIDING) {
			if (Gdx.input.isTouched()) {
				Tile t = b.getTileAtPosition(getX(), getY());
				if(isValidMove(b, t)) {
					GameEngine.movePath.add(t);
					return GameEngine.GameState.DECIDING;
				}
			}
			else {
				if (GameEngine.movePath.size() > 1) {
					return GameEngine.GameState.MOVING;
				}
				else {
					GameEngine.movePath.clear();
					return GameEngine.GameState.IDLE;
				}
			}
		}
		return state;
	}
	
	public int getY() {
		return Gdx.graphics.getHeight() - Gdx.input.getY() - 1;
	}
	
	public int getX() {
		return Gdx.input.getX();
	}
	
	public boolean isValidMove(Board b, Tile t) {
		if((t.isGlass == false) && (b.getPieceOnTile(t) != null)) {
			if(adjacentMove(t)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean adjacentMove(Tile t) {
		Tile s = GameEngine.movePath.get(GameEngine.movePath.size());
		if(s.getYCoord() == t.getXCoord()) {
			if((s.getXCoord() == t.getXCoord() + 1) || (s.getXCoord() == t.getXCoord() - 1)) {
				return true;
			}
		}
		if(s.getXCoord() == t.getYCoord()) {
			if((s.getYCoord() == t.getXCoord() + 1) || (s.getYCoord() == t.getYCoord() - 1)) {
				return true;
			}
		}
		return false;
	}
}
