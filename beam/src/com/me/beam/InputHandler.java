package com.me.beam;

import com.badlogic.gdx.Gdx;
import com.me.beam.GameEngine.GameState;

public class InputHandler {

	private GameEngine.ButtonPress buttonDown = GameEngine.ButtonPress.NONE;
	private int lastX = -1;
	private int lastY = -1;

	private boolean gameWonPressed = false;

	public GameEngine.GameState handleInput(Board b, GameEngine.GameState state) {

		/* Handles inputs starting from a level with no player input. */
		if (state == GameEngine.GameState.IDLE
				&& buttonDown == GameEngine.ButtonPress.NONE) {
			return selectPiece(b);
		}

		/* Handles input if the player is already touching a piece. */
		if (state == GameEngine.GameState.DECIDING) {
			if (Gdx.input.isTouched()) {
				return setPath(b);

			} else {
				return onRelease();

			}
		}
		return state;
	}

	private GameState onRelease() {
		if (GameEngine.movePath.size() > 1) {
			/* If they let go with moves queued, make them */
			return GameEngine.GameState.MOVING;
		} else {
			/* If they let go without moves queued, idle */
			GameEngine.movePath.clear();
			return GameEngine.GameState.IDLE;
		}
	}

	private GameState setPath(Board b) {
		if (b.getTileAtClickPosition(getX(), getY()) != null) {
			Tile source = GameEngine.movePath
					.get(GameEngine.movePath.size() - 1);
			Tile destination = b.getTileAtClickPosition(getX(), getY());

			// If the destination is on the path, short circuit the
			// path.
			int i = GameEngine.movePath.indexOf(destination);
			if (i != -1 && i != GameEngine.movePath.size() - 1) {
				GameEngine.movePath = GameEngine.movePath.subList(0, i + 1);
			}

			Tile intervening = findValidDiagonal(b, source, destination);
			if (isValidMove(b, source, destination)) {
				GameEngine.movePath.add(destination);
			} else if (intervening != null) {
				i = GameEngine.movePath.indexOf(intervening);
				if (i != -1) {
					GameEngine.movePath = GameEngine.movePath.subList(0, i + 1);
					GameEngine.movePath.add(destination);
				} else {
					GameEngine.movePath.add(intervening);
					GameEngine.movePath.add(destination);
				}
			}
		}
		return GameEngine.GameState.DECIDING;
	}

	private GameState selectPiece(Board b) {
		if (Gdx.input.isTouched()
				&& b.getTileAtClickPosition(getX(), getY()) != null) {
			Tile t = b.getTileAtClickPosition(getX(), getY());
			Piece piece = b.getPieceOnTile(t);
			/*
			 * Given a touch on the board and on a piece, read piece as held.
			 */
			if (piece != null) {
				GameEngine.movingPiece = piece;
				GameEngine.movePath.add(t);
				return GameEngine.GameState.DECIDING;
				/* If the touch was anywhere else, don't change states */
			} else {
				return GameEngine.GameState.IDLE;
			}
		}
		// If the player isn't touching the board, don't change states
		return GameEngine.GameState.IDLE;
	}

	// Returns which button was pressed, or none
	public GameEngine.ButtonPress checkForButtonPress() {

		GameEngine.ButtonPress returnedButton;

		// Button pushed
		if (Gdx.input.isTouched()) {
			// Get inputs
			int xPress = getX();
			int yPress = getY();

			// Look for new button press
			if (buttonDown == GameEngine.ButtonPress.NONE && lastX == -1) {
				buttonDown = Menu.containingButtonOfPixel(xPress, yPress);

				if (buttonDown == GameEngine.ButtonPress.NONE) {
					gameWonPressed = true;
				}
			}
			lastX = xPress;
			lastY = yPress;
			returnedButton = GameEngine.ButtonPress.NONE;
		}
		// Button not pushed
		else {
			// Look for removed input
			if (buttonDown != GameEngine.ButtonPress.NONE && lastX != -1) {
				returnedButton = Menu.containingButtonOfPixel(lastX, lastY);

				if (returnedButton != buttonDown) {
					returnedButton = GameEngine.ButtonPress.NONE;
				}
			}
			// HACK HERE TO GET GAME WINNING HAPPENING ON TOUCH
			else if (lastX != -1 && gameWonPressed) {
				returnedButton = GameEngine.ButtonPress.WON;
			} else {
				returnedButton = GameEngine.ButtonPress.NONE;
			}
			// Rest lastX and LastY
			lastX = -1;
			lastY = -1;
			buttonDown = GameEngine.ButtonPress.NONE;
		}
		return returnedButton;
	}

	private int getY() {
		return Gdx.graphics.getHeight() - Gdx.input.getY() - 1;
	}

	private int getX() {
		return Gdx.input.getX();
	}

	private boolean isEmptyTile(Board b, Tile t) {
		return (t.isGlass == false)
				&& ((b.getPieceOnTile(t) == null) || b.getPieceOnTile(t) == GameEngine.movingPiece);
	}

	private boolean isValidMove(Board b, Tile source, Tile destination) {
		return isEmptyTile(b, destination)
				&& isMoveAdjacent(source, destination);
	}

	// Returns null if illegal, or appropriate intervening tile if legal.
	private Tile findValidDiagonal(Board b, Tile source, Tile destination) {
		if (!(isEmptyTile(b, destination) && isMoveDiagonal(source, destination))) {
			return null;
		}

		// Ties go to vertical first path.
		Tile verticalIntervening = b.getTileAtBoardPosition(source.getXCoord(),
				destination.getYCoord());
		if (isEmptyTile(b, verticalIntervening)) {
			return verticalIntervening;
		}
		Tile horizontalIntervening = b.getTileAtBoardPosition(
				destination.getXCoord(), source.getYCoord());
		if (isEmptyTile(b, horizontalIntervening)) {
			return horizontalIntervening;
		}
		return null;
	}

	private boolean isMoveDiagonal(Tile t1, Tile t2) {
		return (Math.abs(t1.getXCoord() - t2.getXCoord()) == 1)
				&& (Math.abs(t1.getYCoord() - t2.getYCoord()) == 1);
	}

	private boolean isMoveAdjacent(Tile t1, Tile t2) {
		return (Math.abs(t1.getXCoord() - t2.getXCoord()) == 1 && t1
				.getYCoord() == t2.getYCoord())
				|| (Math.abs(t2.getYCoord() - t1.getYCoord()) == 1 && t1
						.getXCoord() == t2.getXCoord());
	}
}
