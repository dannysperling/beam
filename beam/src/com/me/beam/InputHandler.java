package com.me.beam;

import com.badlogic.gdx.Gdx;

public class InputHandler {

	public GameEngine.GameState handleInput(Board b, GameEngine.GameState state) {

		/*Handles inputs starting from a level with no player input.*/
		if (state == GameEngine.GameState.IDLE) {
			if (Gdx.input.isTouched()
					&& b.getTileAtClickPosition(getX(), getY()) != null) {
				Tile t = b.getTileAtClickPosition(getX(), getY());
				Piece piece = b.getPieceOnTile(t);
				/*Given a touch on the board and on a piece, read piece as held.*/
				if (piece != null) {
					GameEngine.movingPiece = piece;
					GameEngine.movePath.add(t);
					return GameEngine.GameState.DECIDING;
				/*If the touch was anywhere else, don't change states*/
				} else {
					return GameEngine.GameState.IDLE;
				}
			}
		}
		
		/*Handles input if the player is already touching a piece.*/
		if (state == GameEngine.GameState.DECIDING) {
			// System.out.println("move path size = " +
			// GameEngine.movePath.size());
			if (Gdx.input.isTouched()) {
				if (b.getTileAtClickPosition(getX(), getY()) != null) {
					Tile t = b.getTileAtClickPosition(getX(), getY());
					if (isValidMove(b, t)) {
						int i = GameEngine.movePath.indexOf(t);
						if (i == -1) {
							GameEngine.movePath.add(t);
						} else {
							GameEngine.movePath = GameEngine.movePath.subList(
									0, i + 1);
						}
						/*If the proposed move isn't legal, change nothing*/
						return GameEngine.GameState.DECIDING;
					}
				}
				/*If the proposed move is off the board, change nothing*/
				else {
					return GameEngine.GameState.DECIDING;
				}
			} else {
				/*If they let go with moves queued, make them*/
				if (GameEngine.movePath.size() > 1) {
					return GameEngine.GameState.MOVING;
				/*If they let go without moves queued, idle*/
				} else {
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
		if (t.isGlass == false) {
			if ((b.getPieceOnTile(t) == null)
					|| b.getPieceOnTile(t) == GameEngine.movingPiece) {
				if (adjacentMove(t)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean adjacentMove(Tile t) {
		Tile s = GameEngine.movePath.get(GameEngine.movePath.size() - 1);
		if (s.getYCoord() == t.getYCoord()) {
			if ((s.getXCoord() == t.getXCoord() + 1)
					|| (s.getXCoord() == t.getXCoord() - 1)) {
				return true;
			}
		}
		if (s.getXCoord() == t.getXCoord()) {
			if ((s.getYCoord() == t.getYCoord() + 1)
					|| (s.getYCoord() == t.getYCoord() - 1)) {
				return true;
			}
		}
		return false;
	}
}
