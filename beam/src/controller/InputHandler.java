package controller;

import java.util.ArrayList;
import java.util.List;

import utilities.Constants;

import model.Board;
import model.Menu;
import model.Piece;
import model.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import controller.GameEngine.GameState;
import controller.GameEngine.TitleOption;

public class InputHandler {

	/**
	 * Stores what button the user has pressed, so all other input can proceed
	 * accordingly.
	 */
	private GameEngine.ButtonPress buttonDown = GameEngine.ButtonPress.NONE;

	/**
	 * Stores whether we're in a secondary move queue, used for consequence free
	 * queuing
	 */
	public boolean secondMoveStarted = false;

	/**
	 * Stores the currently happening move in its entirety to aid second move
	 * checking
	 */
	public List<Tile> wholeMoves = new ArrayList<Tile>();

	/**
	 * Private helper methods to get the current relevant (x,y) touch position
	 * of the user.
	 */
	private int getY() {
		int y = Gdx.graphics.getHeight() - Gdx.input.getY() - 1;
		return Math.min(Math.max(y, 0), Gdx.graphics.getHeight() - 1);
	}

	private int getX() {
		return Gdx.input.getX();
	}

	/**
	 * Primary input handler for the game while its being played. Builds up
	 * moves by the user, and reflects these back to the game engine.
	 * 
	 * @param b
	 *            The current board being input onto
	 * @param state
	 *            The state of the game. Most game input only accepted while the
	 *            game is IDLE or DECIDING (user choosing where to go).
	 * @return The new state of the game, based on the input received.
	 */
	public GameEngine.GameState handleGameInput(Board b,
			GameEngine.GameState state) {

		/* Handles inputs starting from a level with no player input. */
		if (state == GameEngine.GameState.IDLE
				&& buttonDown == GameEngine.ButtonPress.NONE) {
			return selectPiece(b);
		}

		/* Handles input when a level is in its intro screen */
		if (state == GameState.INTRO && Gdx.input.isTouched()) {
			return GameState.IDLE;
		}

		/* Can remove tutorial once it's gotten to the bottom */
		if (state == GameState.TUTORIAL
				&& (Gdx.input.isTouched() || backClicked)) {
			if (GameEngine.timeToStopTutorial == Integer.MAX_VALUE
					&& GameEngine.timeSpentOnTutorial >= Constants.TUTORIAL_IN_TIME) {
				GameEngine.timeToStopTutorial = GameEngine.timeSpentOnTutorial
						+ Constants.TUTORIAL_IN_TIME;
			}
			backClicked = false;
			return GameState.TUTORIAL;
		}

		/* Can remove info once it's gotten to the bottom */
		if (state == GameState.INFO && (Gdx.input.isTouched() || backClicked)) {
			if (GameEngine.timeToStopInfo == Integer.MAX_VALUE
					&& GameEngine.timeSpentOnInfo >= Constants.TUTORIAL_IN_TIME) {
				GameEngine.timeToStopInfo = GameEngine.timeSpentOnInfo
						+ Constants.TUTORIAL_IN_TIME;
			}
			backClicked = false;
			return GameState.INFO;
		}

		/* Handles input if the player is already touching a piece. */
		if (state == GameEngine.GameState.DECIDING) {
			// Either still touching - set path
			if (Gdx.input.isTouched()) {
				return setPath(b);
			}
			// Or released - handle release
			else {
				return onRelease();
			}
		}

		/* Handles secondary queue during move */
		if (state == GameEngine.GameState.MOVING) {
			// Selects piece during move
			if (buttonDown == GameEngine.ButtonPress.NONE) {
				if (Gdx.input.isTouched()) {
					if (!secondMoveStarted) {
						GameEngine.movePath2.clear();
						selectSecondPiece(b);
					} else {
						setSecondPath(b);
					}
				} else if (secondMoveStarted) {
					onSecondRelease();
				}
			}
			return GameEngine.GameState.MOVING;
		}

		// If no input, return the same state
		return state;
	}

	/**
	 * Handles the user's first press down onto the board. If the user touches a
	 * piece, select it and enter deciding state.
	 * 
	 * @param b
	 *            The board to select a piece from
	 * @return The new state. IDLE if no piece was touched, or DECIDING if piece
	 *         touched
	 */
	private GameState selectPiece(Board b) {
		Tile destination = b.getTileAtClickPosition(getX(), getY());
		/* Check first if the screen is even being touched on a valid tile */
		if (Gdx.input.isTouched() && destination != null) {
			Piece piece = b.getPieceOnTile(destination);
			/*
			 * Given a touch on the board and on a piece, read piece as held.
			 */
			if (piece != null) {
				GameEngine.movingPiece = piece;
				GameEngine.movePath.add(destination);
				return GameEngine.GameState.DECIDING;
				/* If the touch was anywhere else, don't change states */
			} else {
				return GameEngine.GameState.IDLE;
			}
		}
		// If the player isn't touching the board, don't change states
		return GameEngine.GameState.IDLE;
	}

	private GameState selectSecondPiece(Board b) {
		Tile destination = b.getTileAtClickPosition(getX(), getY());
		/* Check first if the screen is even being touched on a valid tile */
		if (Gdx.input.isTouched() && destination != null) {
			Piece piece = b.getPieceOnTile(destination);
			/*
			 * Given a touch on the board and on a piece, read piece as held.
			 */
			if (piece != null && piece != GameEngine.movingPiece) {
				GameEngine.movingPiece2 = piece;
				GameEngine.movePath2.add(destination);
				secondMoveStarted = true;
				return GameEngine.GameState.MOVING;
				/* If the touch was anywhere else, don't change boolean */
			} else {
				return GameEngine.GameState.MOVING;
			}
		}
		// If the player isn't touching the board, don't change states
		return GameEngine.GameState.MOVING;
	}

	/**
	 * When the player releases, check to see if they'd moved places. If so,
	 * indicate such. Otherwise, nothing to do - clear past moves.
	 * 
	 * @return The new game state. MOVING if the player had places they were
	 *         going, or IDLE if still holding in original position.
	 */
	private GameState onRelease() {
		if (GameEngine.movePath.size() > 1) {
			for (Tile next : GameEngine.movePath) {
				wholeMoves.add(next);
			}
			/* If they let go with moves queued, make them */
			return GameEngine.GameState.MOVING;
		} else {
			/* If they let go without moves queued, idle */
			GameEngine.movePath.clear();
			return GameEngine.GameState.IDLE;
		}
	}

	/**
	 * When the player releases during another move, check to see if there's a
	 * second queue. If so, leave it be. If not, clear second move state.
	 * 
	 * @return The game state MOVING for consistency, while changing the second
	 *         move flag.
	 */
	private GameState onSecondRelease() {
		secondMoveStarted = false;
		if (GameEngine.movePath2.size() > 1) {
			/* If they let go with moves queued, keep them */
			return GameEngine.GameState.MOVING;
		} else {
			/* If they let go without moves queued, idle */
			GameEngine.movePath2.clear();
			return GameEngine.GameState.MOVING;
		}
	}

	/**
	 * Called when the user is still holding on the screen, having already
	 * started a valid move for a piece. Handles continuing the move.
	 * 
	 * @param b
	 *            The board being moved on, for reference
	 * @return GameState.DECIDING, as the state at present can't change in this
	 *         method.
	 */
	private GameState setPath(Board b) {
		Tile destination = b.getTileAtClickPosition(getX(), getY());
		// Ensure the user is still pressing on a valid tile.
		if (destination != null) {

			// Get where they were and where they're going
			Tile source = GameEngine.movePath
					.get(GameEngine.movePath.size() - 1);

			// If the destination is on the path, short circuit the
			// path.
			boolean onPath = false;
			int i = GameEngine.movePath.indexOf(destination);
			if (i != -1) {
				onPath = true;
				// Shorten the list down to the past occurrence of that
				// location, if need be
				if (i != GameEngine.movePath.size() - 1) {
					List<Tile> newPath = new ArrayList<Tile>();
					for (int j = 0; j < i + 1; j++) {
						newPath.add(GameEngine.movePath.get(j));
					}
					GameEngine.movePath = newPath;
				}
			}

			// Only do remaining checks if the tile wasn't on the path. Fixes
			// the
			// headless arrow bug.
			if (!onPath) {
				// Check for regular or diagonal moves
				Tile intervening = findValidDiagonal(b, source, destination);
				ArrayList<Tile> interveningAll = findValidLinear(b, source,
						destination);
				if (isValidMove(b, source, destination)) {
					GameEngine.movePath.add(destination);
				} else if (intervening != null) {

					// If intervening was already on the path, chop back to that
					// point
					i = GameEngine.movePath.indexOf(intervening);

					if (i != -1) {
						List<Tile> newPath = new ArrayList<Tile>();
						for (int j = 0; j < i + 1; j++) {
							newPath.add(GameEngine.movePath.get(j));
						}
						GameEngine.movePath = newPath;
						GameEngine.movePath.add(destination);
					}
					// Otherwise simply add the intervening and destination tile
					else {
						GameEngine.movePath.add(intervening);
						GameEngine.movePath.add(destination);
					}
				} else if (interveningAll != null) {
					for (Tile next : interveningAll) {

						// If intervening was already on the path, chop back to
						// that point
						i = GameEngine.movePath.indexOf(next);

						if (i != -1) {
							List<Tile> newPath = new ArrayList<Tile>();
							for (int j = 0; j < i + 1; j++) {
								newPath.add(GameEngine.movePath.get(j));
							}
							GameEngine.movePath = newPath;
						}
						// Then simply add the next tile
						GameEngine.movePath.add(next);
					}
				}
			}
		}
		// Always still in the deciding state, for now.
		return GameEngine.GameState.DECIDING;
	}

	/**
	 * Called when the user touches the screen during a move
	 * 
	 * @param b
	 *            The board being moved on, for reference
	 * @return GameState.DECIDING, as the state at present can't change in this
	 *         method.
	 */
	private GameState setSecondPath(Board b) {
		Tile destination = b.getTileAtClickPosition(getX(), getY());
		// Ensure the user is still pressing on a valid tile.
		if (destination != null) {

			// Get where they were and where they're going
			Tile source = GameEngine.movePath2
					.get(GameEngine.movePath2.size() - 1);

			// If the destination is on the path, short circuit the
			// path.
			boolean onPath = false;
			int i = GameEngine.movePath2.indexOf(destination);
			if (i != -1) {
				onPath = true;
				// Shorten the list down to the past occurrence of that
				// location, if need be
				if (i != GameEngine.movePath2.size() - 1) {
					List<Tile> newPath = new ArrayList<Tile>();
					for (int j = 0; j < i + 1; j++) {
						newPath.add(GameEngine.movePath2.get(j));
					}
					GameEngine.movePath2 = newPath;
				}
			}

			// Only do remaining checks if the tile wasn't on the path. Fixes
			// the
			// headless arrow bug.
			if (!onPath) {
				// Check for regular or diagonal moves
				Tile intervening = findSecondValidDiagonal(b, source,
						destination);
				ArrayList<Tile> interveningAll = findSecondValidLinear(b,
						source, destination);
				if (isSecondValidMove(b, source, destination)) {
					GameEngine.movePath2.add(destination);
				} else if (intervening != null) {

					// If intervening was already on the path, chop back to that
					// point
					i = GameEngine.movePath2.indexOf(intervening);

					if (i != -1) {
						List<Tile> newPath = new ArrayList<Tile>();
						for (int j = 0; j < i + 1; j++) {
							newPath.add(GameEngine.movePath2.get(j));
						}
						GameEngine.movePath2 = newPath;
						GameEngine.movePath2.add(destination);
					}
					// Otherwise simply add the intervening and destination tile
					else {
						GameEngine.movePath2.add(intervening);
						GameEngine.movePath2.add(destination);
					}
				} else if (interveningAll != null) {
					for (Tile next : interveningAll) {

						// If intervening was already on the path, chop back to
						// that point
						i = GameEngine.movePath2.indexOf(next);

						if (i != -1) {
							List<Tile> newPath = new ArrayList<Tile>();
							for (int j = 0; j < i + 1; j++) {
								newPath.add(GameEngine.movePath2.get(j));
							}
							GameEngine.movePath2 = newPath;
						}
						// Then simply add the next tile
						GameEngine.movePath2.add(next);
					}
				}
			}
		}
		// Always still in the moving state while doing secondaries.
		return GameEngine.GameState.MOVING;
	}
	
	private boolean isOnPathEnd(Tile t) {
		return t.equals(wholeMoves.get(wholeMoves.size() - 1));
	}

	private boolean isSecondValidMove(Board b, Tile source, Tile destination) {
		// If the destination is on the existing path it can be full
		return (wholeMoves.contains(destination) || isEmptyTile(b, destination))
				&& isMoveAdjacent(source, destination)
				&& !isOnPathEnd(destination);
	}

	private ArrayList<Tile> findSecondValidLinear(Board b, Tile source,
			Tile destination) {
		ArrayList<Tile> path = new ArrayList<Tile>();
		Tile toAdd;

		if (isMoveVerticalNonadjacent(source, destination)) {
			int x = source.getXCoord();
			int first = source.getYCoord();
			int second = destination.getYCoord();
			if (first - second > 0) {
				for (int i = first - 1; i >= second; i--) {
					toAdd = b.getTileAtBoardPosition(x, i);
					if (!(b.isTilePassable(toAdd) || wholeMoves.contains(toAdd))
							|| isOnPathEnd(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}

			else if (first - second < 0) {
				for (int i = first + 1; i <= second; i++) {
					toAdd = b.getTileAtBoardPosition(x, i);
					if ((!b.isTilePassable(toAdd) || wholeMoves.contains(toAdd))
							|| isOnPathEnd(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}
		} else if (isMoveHorizontalNonadjacent(source, destination)) {
			int y = source.getYCoord();
			int first = source.getXCoord();
			int second = destination.getXCoord();
			if (first - second > 0) {
				for (int i = first - 1; i >= second; i--) {
					toAdd = b.getTileAtBoardPosition(i, y);
					if (!(b.isTilePassable(toAdd) || wholeMoves.contains(toAdd))
							|| isOnPathEnd(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}

			else if (first - second < 0) {
				for (int i = first + 1; i <= second; i++) {
					toAdd = b.getTileAtBoardPosition(i, y);
					if (!(b.isTilePassable(toAdd) || wholeMoves.contains(toAdd))
							|| isOnPathEnd(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}
		}

		return path;
	}

	private Tile findSecondValidDiagonal(Board b, Tile source, Tile destination) {
		// Check to ensure the destination is empty or on current move and the
		// move from the source to
		// the destination is in fact diagonal
		if (!((isEmptyTile(b, destination) || wholeMoves.contains(destination)) && isMoveDiagonal(
				source, destination)) || isOnPathEnd(destination)) {
			return null;
		}

		// Ties go to vertical first path.
		Tile verticalIntervening = b.getTileAtBoardPosition(source.getXCoord(),
				destination.getYCoord());
		if ((isEmptyTile(b, verticalIntervening)
				|| wholeMoves.contains(verticalIntervening))
				&& !isOnPathEnd(verticalIntervening)) {
			return verticalIntervening;
		}

		// Then check horizontal path
		Tile horizontalIntervening = b.getTileAtBoardPosition(
				destination.getXCoord(), source.getYCoord());
		if ((isEmptyTile(b, horizontalIntervening) 
				|| wholeMoves.contains(horizontalIntervening))
				&& !isOnPathEnd(horizontalIntervening)) {
			return horizontalIntervening;
		}
		return null;
	}

	/**
	 * Checks if a tile is empty on the given board
	 */
	private boolean isEmptyTile(Board b, Tile t) {
		return (!t.hasGlass())
				&& ((b.getPieceOnTile(t) == null) || b.getPieceOnTile(t) == GameEngine.movingPiece);
	}

	/*
	 * private boolean isEmptySecondMoveTile(Board b, Tile t) { Piece[][]
	 * modifiedState = b.getPieces(); }
	 */

	/**
	 * Checks if a move from source to destination is valid. Must be adjacent
	 * tiles, and the tile moved to must be empty.
	 */
	private boolean isValidMove(Board b, Tile source, Tile destination) {
		return isEmptyTile(b, destination)
				&& isMoveAdjacent(source, destination);
	}

	/**
	 * Attempts to see if there exists a valid diagonal move from source to
	 * destination. If so, returns the valid, empty intervening tile. Otherwise,
	 * returns null.
	 */
	private Tile findValidDiagonal(Board b, Tile source, Tile destination) {

		// Check to ensure the destination is empty and the move from the source
		// to
		// the destination is in fact diagonal
		if (!(isEmptyTile(b, destination) && isMoveDiagonal(source, destination))) {
			return null;
		}

		// Ties go to vertical first path.
		Tile verticalIntervening = b.getTileAtBoardPosition(source.getXCoord(),
				destination.getYCoord());
		if (isEmptyTile(b, verticalIntervening)) {
			return verticalIntervening;
		}

		// Then check horizontal path
		Tile horizontalIntervening = b.getTileAtBoardPosition(
				destination.getXCoord(), source.getYCoord());
		if (isEmptyTile(b, horizontalIntervening)) {
			return horizontalIntervening;
		}
		return null;
	}

	/**
	 * Checks if tile t1 and t2 are a diagonal move away from each other
	 */
	private boolean isMoveDiagonal(Tile t1, Tile t2) {
		return (Math.abs(t1.getXCoord() - t2.getXCoord()) == 1)
				&& (Math.abs(t1.getYCoord() - t2.getYCoord()) == 1);
	}

	/**
	 * 
	 */
	private boolean isMoveVerticalNonadjacent(Tile t1, Tile t2) {
		if (t1.getXCoord() == t2.getXCoord()) {
			return (Math.abs(t1.getYCoord() - t2.getYCoord()) > 1);
		}
		return false;
	}

	private boolean isMoveHorizontalNonadjacent(Tile t1, Tile t2) {
		if (t1.getYCoord() == t2.getYCoord()) {
			return (Math.abs(t1.getXCoord() - t2.getXCoord()) > 1);
		}
		return false;
	}

	private ArrayList<Tile> findValidLinear(Board b, Tile source,
			Tile destination) {
		ArrayList<Tile> path = new ArrayList<Tile>();
		Tile toAdd;

		if (isMoveVerticalNonadjacent(source, destination)) {
			int x = source.getXCoord();
			int first = source.getYCoord();
			int second = destination.getYCoord();
			if (first - second > 0) {
				for (int i = first - 1; i >= second; i--) {
					toAdd = b.getTileAtBoardPosition(x, i);
					if (!b.isTilePassable(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}

			else if (first - second < 0) {
				for (int i = first + 1; i <= second; i++) {
					toAdd = b.getTileAtBoardPosition(x, i);
					if (!b.isTilePassable(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}
		} else if (isMoveHorizontalNonadjacent(source, destination)) {
			int y = source.getYCoord();
			int first = source.getXCoord();
			int second = destination.getXCoord();
			if (first - second > 0) {
				for (int i = first - 1; i >= second; i--) {
					toAdd = b.getTileAtBoardPosition(i, y);
					if (!b.isTilePassable(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}

			else if (first - second < 0) {
				for (int i = first + 1; i <= second; i++) {
					toAdd = b.getTileAtBoardPosition(i, y);
					if (!b.isTilePassable(toAdd)) {
						return null;
					}
					path.add(toAdd);
				}
			}
		}

		return path;
	}

	/**
	 * Checks if tile t1 and t2 are a horizontal or vertical move from each
	 * other
	 */
	private boolean isMoveAdjacent(Tile t1, Tile t2) {
		return (Math.abs(t1.getXCoord() - t2.getXCoord()) == 1 && t1
				.getYCoord() == t2.getYCoord())
				|| (Math.abs(t2.getYCoord() - t1.getYCoord()) == 1 && t1
						.getXCoord() == t2.getXCoord());
	}

	/*******************************************************************************/

	/**
	 * Code in this section handles button presses during the game or win menu
	 */
	private boolean backClicked = false;
	private boolean backDown = false;

	/**
	 * Handle pressing the back button. Consider a press-release to be a valid
	 * back button press, even if the user moved their finger off of the back
	 * button instead of actually releasing it.
	 */
	public void checkBackPressed() {
		if (Gdx.input.isKeyPressed(Keys.BACK)) {
			backDown = true;
		} else {
			if (backDown) {
				backClicked = true;
			}
			backDown = false;
		}
	}

	/**
	 * Keep indications of previous press information to know if the user
	 * pressed and released on the same button.
	 */
	private int lastX = -1;
	private int lastY = -1;

	/**
	 * Check to see if a button has been pressed, both during the standard level
	 * as well as when the menu is showing.
	 * 
	 * @param state
	 *            What state the game is in
	 * @param botYCoord
	 *            The bottom Y coordinate of the top buttons for this board
	 * @return Which button was pressed, if any
	 */
	public GameEngine.ButtonPress checkForButtonPress(GameState state,
			int botYCoord, boolean bonusUnlocked) {

		// Short circuit if back had been pressed
		if (backClicked) {
			backClicked = false;
			return GameEngine.ButtonPress.MENU;
		}

		GameEngine.ButtonPress returnedButton;

		// If screen just being touched, possibly starting a button push
		if (Gdx.input.isTouched()) {
			// Get inputs
			int xPress = getX();
			int yPress = getY();

			// Look for new button press
			if (lastX == -1) {

				if (state != GameState.WON) {
					buttonDown = Menu.containingButtonOfPixelLevelScreen(
							xPress, yPress, botYCoord);
				} else {
					buttonDown = Menu.containingButtonOfPixelWonScreen(xPress,
							yPress, bonusUnlocked);
				}

				if (buttonDown == GameEngine.ButtonPress.NONE
						&& state == GameState.DESTROYED) {
					buttonDown = GameEngine.ButtonPress.RESET;
				}
			}

			// Indicate the user is clicking on the screen, so can't slide onto
			// a different
			// button during a single press
			lastX = xPress;
			lastY = yPress;

			// While pushing down, nothing pressed yet.
			returnedButton = GameEngine.ButtonPress.NONE;
		}
		// Screen not touched - could be removing touch
		else {
			// Look for removed input
			if (buttonDown != GameEngine.ButtonPress.NONE && lastX != -1) {

				if (state != GameState.WON) {
					returnedButton = Menu.containingButtonOfPixelLevelScreen(
							lastX, lastY, botYCoord);
				} else {
					returnedButton = Menu.containingButtonOfPixelWonScreen(
							lastX, lastY, bonusUnlocked);
				}

				// Any other click if destroyed does a reset
				if (returnedButton == GameEngine.ButtonPress.NONE
						&& state == GameState.DESTROYED) {
					returnedButton = GameEngine.ButtonPress.RESET;
				}

				// Check to make sure they were still pressing the same button
				// as originally
				if (returnedButton != buttonDown) {
					returnedButton = GameEngine.ButtonPress.NONE;
				}

			} else {
				returnedButton = GameEngine.ButtonPress.NONE;
			}
			// Reset lastX and LastY
			lastX = -1;
			lastY = -1;
			buttonDown = GameEngine.ButtonPress.NONE;
		}
		return returnedButton;
	}

	private GameEngine.TitleOption optionPressed = TitleOption.NONE;

	/**
	 * Check to see which title option was pressed, if any
	 */
	public GameEngine.TitleOption checkForTitleOptionPress(
			boolean creditsShowing) {
		// Short circuit if back had been pressed
		if (backClicked) {
			backClicked = false;
			return GameEngine.TitleOption.EXIT;
		}

		GameEngine.TitleOption returnedOption;

		// If screen just being touched, possibly starting a button push
		if (Gdx.input.isTouched()) {
			// Get inputs
			int xPress = getX();
			int yPress = getY();

			// Look for new button press
			if (lastX == -1) {
				optionPressed = Menu.containingButtonOfPixelTitleScreen(xPress,
						yPress);
			}

			if (creditsShowing) {
				optionPressed = GameEngine.TitleOption.CREDITS;
			}
			// Indicate the user is clicking on the screen, so can't slide onto
			// a different
			// button during a single press
			lastX = xPress;
			lastY = yPress;

			// While pushing down, nothing pressed yet.
			returnedOption = GameEngine.TitleOption.NONE;
		}
		// Screen not touched - could be removing touch
		else {
			// Look for removed input
			if (optionPressed != GameEngine.TitleOption.NONE && lastX != -1) {

				returnedOption = Menu.containingButtonOfPixelTitleScreen(lastX,
						lastY);

				// Check to make sure they were still pressing the same button
				// as originally
				if (returnedOption != optionPressed) {
					returnedOption = GameEngine.TitleOption.NONE;
				}

				if (creditsShowing) {
					returnedOption = GameEngine.TitleOption.CREDITS;
				}
			} else {
				returnedOption = GameEngine.TitleOption.NONE;
			}
			// Reset lastX and LastY
			lastX = -1;
			lastY = -1;
			optionPressed = GameEngine.TitleOption.NONE;
		}
		return returnedOption;
	}

	/*******************************************************************************/

	/**
	 * Remainder below here is to handle input into the menu. Various fields to
	 * see where had been clicked.
	 */
	private int firstTouchX = -1;
	private int firstTouchY = -1;
	private int lastTouchX = -1;
	private int lastTouchY = -1;
	private int worldTouched = -1;

	/**
	 * Determines if what the user did counts as a click. MAX_DIFF_CLICK
	 * indicates how far the user can have moved their finger and still count as
	 * a "click"
	 */
	private boolean movedTooFar = false;

	/**
	 * Determines the amount the screen should be scrolling in terms of X and Y.
	 * Can only move one way at once for now. MOMENTUM_DROP_OFF indicates what
	 * percentage of momentum should be removed each cycle after the user has
	 * released the screen. MIN_MOMENTUM is the smallest momentum is allowed to
	 * get before it is set back to zero to stop movement.
	 */
	private float momentumY = 0;
	private float momentumX = 0;

	/**
	 * Determine if the screen should be scrolling vertically or horizontally.
	 * VERT_MOVE_BOUNDS and HORIZ_MOVE_BOUNDS indicate how far, in pixels, the
	 * user needs to move before they are considered to be scrolling in that
	 * direction. Can only scroll in one direction at once right now.
	 */
	private boolean movingVertically = false;
	private boolean movingHorizontally = false;

	/**
	 * Allows a four finger press for TIME_FOR_LOGGING_RESET cycles to reset
	 * logging
	 */
	private int timeHeld = 0;
	private final int TIME_FOR_LOGGING_RESET = 240;

	/**
	 * Allows a click on a level to be stored, so both the world and ordinal can
	 * be selected afterward.
	 */
	private int mostRecentlySelectedWorld = -1;
	private int mostRecentlySelectedOrdinalInWorld = -1;

	/**
	 * Primary method for handling input on the menu screen. Returns based on
	 * what was pressed.
	 * 
	 * @param menu
	 *            The menu object storing information about the locations and
	 *            information on all of the levels
	 * @return 0 if a level was selected, -1 if nothing was selected, -2 if
	 *         exiting the game, or -3 if logging is enabled and a logging reset
	 *         occurred.
	 */
	public int handleMainMenuInput(Menu menu) {

		// Check if back was pressed - exit the game
		if (backClicked) {
			backClicked = false;
			return -2;
		}

		// Check if logging is enabled and four fingers have been pressed for
		// long enough
		if (Constants.LOGGING) {
			if (Gdx.input.isTouched(3)) {
				timeHeld++;
			} else {
				timeHeld = 0;
			}
			if (timeHeld == TIME_FOR_LOGGING_RESET) {
				return -3;
			}
		}

		// Otherwise, check for standard input
		// Pressing on the screen
		if (Gdx.input.isTouched()) {
			int y = getY();
			int x = getX();

			// Check for the first press - store info about where it happened
			if (firstTouchY == -1) {
				firstTouchY = y;
				firstTouchX = x;
				worldTouched = menu.getWorldAtPosition(y);
				momentumX = 0;
				momentumY = 0;
			}
			// Otherwise update momentum
			else {
				// Only allow one direction of motion
				if (!movingVertically && !movingHorizontally) {

					// If we've moved far enough vertically, start movement that
					// way
					if (Math.abs(firstTouchY - y) > Constants.VERT_MOVE_BOUNDS) {
						movingVertically = true;
						momentumY = (y - lastTouchY);

						// Scroll up down, while saying we are still holding the
						// screen
						menu.scrollUpDown(y - firstTouchY, true);
						menu.scrollLeftRight(worldTouched, 0, true);
					}
					// Otherwise, same thing for moving horizontally
					else if (menu.worldInBounds(worldTouched)
							&& Math.abs(firstTouchX - x) > Constants.HORIZ_MOVE_BOUNDS) {
						movingHorizontally = true;
						momentumX = (lastTouchX - x);

						// Scroll the world, while saying we are still holding
						// the screen
						menu.scrollLeftRight(worldTouched, firstTouchX - x,
								true);
					} else {
						menu.scrollLeftRight(worldTouched, 0, true);
					}
				}
				// Otherwise, already moving vertically or horizontally.
				// Continue
				else if (movingVertically) {
					momentumY = (y - lastTouchY);

					// Scroll up down, while saying we are still holding the
					// screen
					menu.scrollUpDown((int) momentumY, true);
					menu.scrollLeftRight(worldTouched, 0, true);
				} else {
					momentumX = (lastTouchX - x);

					// Scroll the world, while saying we are still holding the
					// screen
					menu.scrollLeftRight(worldTouched, (int) momentumX, true);
				}
			}

			// Update where we are now
			lastTouchY = y;
			lastTouchX = x;

			// Check if we've moved too far to be considered a click
			int yMoveDistSq = (firstTouchY - lastTouchY)
					* (firstTouchY - lastTouchY);
			int xMoveDistSq = (firstTouchX - lastTouchX)
					* (firstTouchX - lastTouchX);
			int maxDistSq = Constants.MAX_DIFF_CLICK * Constants.MAX_DIFF_CLICK;
			if (xMoveDistSq + yMoveDistSq > maxDistSq) {
				movedTooFar = true;
			}

		}
		// Not pressing the screen anymore - may have clicked a place
		// May still have momentum
		else {
			// Check if clicked a place
			movingVertically = false;
			movingHorizontally = false;

			// Just released the screen
			if (firstTouchY != -1) {
				firstTouchY = -1;

				// Didn't move too far - clicked wherever we last were
				if (!movedTooFar && menu.worldInBounds(worldTouched)) {
					int selected = menu.getLevelAtPositionInWorld(worldTouched,
							lastTouchX);

					// Reset other variables for later
					lastTouchX = -1;
					lastTouchY = -1;
					movedTooFar = false;

					// If we did select a level, indicate such
					if (selected != -1) {
						mostRecentlySelectedOrdinalInWorld = selected;
						mostRecentlySelectedWorld = worldTouched;
						return 0;
					}
					// Otherwise, pressed nothing
					else {
						return -1;
					}
				} else {
					// Reset variables even if we didn't click
					lastTouchX = -1;
					lastTouchY = -1;
					movedTooFar = false;
				}
			}

			// Handle remaining vertical momentum
			if (momentumY != 0) {
				// Scroll and decrease momentum
				boolean turned = menu.scrollUpDown((int) momentumY, false);
				momentumY = momentumY * (1 - Constants.MOMENTUM_DROP_OFF);

				// If our momentum has dropped off or we've been turned by the
				// bounce back
				if (Math.abs(momentumY) < Constants.MIN_MOMENTUM || turned) {
					momentumY = 0;
				}
			} else {
				// Even without momentum, call screen scrolling, so it can
				// potentially bounce back
				menu.scrollUpDown(0, false);
			}

			// Handle remaing world momentum
			if (momentumX != 0) {
				// Scroll and decrease momentum
				boolean turned = menu.scrollLeftRight(worldTouched,
						(int) momentumX, false);
				momentumX = momentumX * (1 - Constants.MOMENTUM_DROP_OFF);

				// If our momentum has dropped off or we've been turned by the
				// bounce back
				if (Math.abs(momentumX) < 2 || turned) {
					momentumX = 0;
				}
			} else {
				// Even without momentum, call screen scrolling, so it can
				// potentially bounce back
				menu.scrollLeftRight(worldTouched, 0, false);
			}
		}
		// If we get to this point, nothing was pressed.
		return -1;
	}

	/**
	 * Allows the game engine to get which world and ordinal had been selected
	 * if a level was in fact selected.
	 */
	public int getMostRecentlySelectedWorld() {
		return mostRecentlySelectedWorld;
	}

	public int getMostRecentlySelectedOrdinalInWorld() {
		return mostRecentlySelectedOrdinalInWorld;
	}

	public void setMostRecentlySelectedWorld(int recent) {
		mostRecentlySelectedWorld = recent;
	}

	public void setMostRecentlySelectedOrdinalInWorld(int recent) {
		mostRecentlySelectedOrdinalInWorld = recent;
	}

}
