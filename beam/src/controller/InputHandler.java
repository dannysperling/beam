package controller;

import java.util.ArrayList;
import java.util.List;

import model.Board;
import model.Menu;
import model.Piece;
import model.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import controller.GameEngine.GameState;

public class InputHandler {

	private GameEngine.ButtonPress buttonDown = GameEngine.ButtonPress.NONE;
	private int lastX = -1;
	private int lastY = -1;

	private boolean gameWonPressed = false;

	public GameEngine.GameState handleGameInput(Board b, GameEngine.GameState state) {

		/* Handles inputs starting from a level with no player input. */
		if (state == GameEngine.GameState.IDLE
				&& buttonDown == GameEngine.ButtonPress.NONE) {
			return selectPiece(b);
		}
		
		if(state == GameState.INTRO && Gdx.input.isTouched()){
			return GameState.IDLE;
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
	
	//Sets a flag if the back button has been pressed. Or just doesn't.
	private boolean backClicked = false;
	private boolean backDown = false;
	public void checkBackPressed(){
		if (Gdx.input.isKeyPressed(Keys.BACK)){
			backDown = true;
		} else {
			if (backDown){
				backClicked = true;
			}
			backDown = false;
		}
	}

	private int firstTouchHeight = -1;
	private int lastTouchHeight = -1;
	private int firstTouchX = -1;
	private int lastTouchX = -1;
	private int world = -1;
	private final int maxDiffClick = 15;
	private boolean movedTooFar = false;
	
	private float momentumY = 0;
	private float momentumX = 0;
	private final float momentumDropOff = 0.10f;
	
	private final int vertMoveBounds = 20;
	private final int horizMoveBounds = 10;
	private boolean movingVertically = false;
	private boolean movingHorizontally = false;
	
	//For logging purposes
	private int timeHeld = 0;
	private final int timeForLoggingReset = 240;
	
	private int mostRecentlySelectedWorld = -1;
	private int mostRecentlySelectedOrdinalInWorld = -1;

	//Returns 0 if level selected, -1 if nothing selected, -2 if exiting game, -3 if logging reset
	public int handleMainMenuInput(Menu menu){
		
		if (backClicked){
			return -2;
		}
		
		if (GameEngine.LOGGING){
			if (Gdx.input.isTouched(3)){
				timeHeld++;
			} else {
				timeHeld = 0;
			}
			if (timeHeld == timeForLoggingReset){
				return -3;
			}
		}

		if (Gdx.input.isTouched()){
			int y = getY();
			int x = getX();

			//Check for the first press
			if (firstTouchHeight == -1){
				firstTouchHeight = y;
				firstTouchX = x;
				world = menu.getWorldAtPosition(y);
				momentumX = 0;
				momentumY = 0;
			} 
			//Otherwise update momentum
			else {
				//Only allow one direction of motion
				if (!movingVertically && !movingHorizontally){
					if (Math.abs(firstTouchHeight - y) > vertMoveBounds){
						movingVertically = true;
						momentumY = (y - lastTouchHeight);
						menu.scrollUpDown(y - firstTouchHeight, true);
					} else if (Math.abs(firstTouchX - x) > horizMoveBounds){
						movingHorizontally = true;
						momentumX = (x - lastTouchX);
						menu.scrollLeftRight(world, firstTouchX - x, true);
					} 
				}
				else if (movingVertically){
					momentumY = (y - lastTouchHeight);
					menu.scrollUpDown((int)momentumY, true);
				} else {
					momentumX = -(x - lastTouchX);
					menu.scrollLeftRight(world, (int)momentumX, true);
				}
			}

			//Update where we are now
			lastTouchHeight = y;
			lastTouchX = x;
			if ((firstTouchHeight - lastTouchHeight)*(firstTouchHeight - lastTouchHeight) + 
					(firstTouchX - lastTouchX)*(firstTouchX - lastTouchX) > maxDiffClick * maxDiffClick)
				movedTooFar = true;

		} else {
			//Check if clicked a place
			movingVertically = false;
			movingHorizontally = false;
			if (firstTouchHeight != -1){
				firstTouchHeight = -1;
				if (!movedTooFar){
					int selected = menu.getLevelAtPositionInWorld(world, lastTouchX);
					
					lastTouchX = -1;
					lastTouchHeight = -1;
					movedTooFar = false;
					if (selected != -1){
						mostRecentlySelectedOrdinalInWorld = selected;
						mostRecentlySelectedWorld = world;
						return 0;
					} else {
						return -1;
					}
				} else {
					lastTouchX = -1;
					lastTouchHeight = -1;
					movedTooFar = false;
				}
			}
			if (momentumY != 0){
				boolean turned = menu.scrollUpDown((int)momentumY, false);
				momentumY = momentumY * (1 - momentumDropOff);
				
				//If we passed zero or hit the wall
				if (Math.abs(momentumY) < 2 || turned){
					momentumY = 0;
				}
			} else {
				menu.scrollUpDown(0, false);
			}
			if (momentumX != 0){
				boolean turned = menu.scrollLeftRight(world, (int)momentumX, false);
				momentumX = momentumX * (1 - momentumDropOff);
				
				//If we passed zero or hit the wall
				if (Math.abs(momentumX) < 2 || turned){
					momentumX = 0;
				}
			} else {
				menu.scrollLeftRight(world, 0, false);
			}
		}

		return -1;
	}
	
	public int getMostRecentlySelectedWorld(){
		return mostRecentlySelectedWorld;
	}
	
	public int getMostRecentlySelectedOrdinalInWorld(){
		return mostRecentlySelectedOrdinalInWorld;
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
				
				//REPLACING SUBLIST
				List<Tile> newPath = new ArrayList<Tile>();
				for (int j = 0; j < i + 1; j++){
					newPath.add(GameEngine.movePath.get(j));
				}
				GameEngine.movePath = newPath;
			}

			Tile intervening = findValidDiagonal(b, source, destination);
			if (isValidMove(b, source, destination)) {
				GameEngine.movePath.add(destination);
			} else if (intervening != null) {
				i = GameEngine.movePath.indexOf(intervening);
				if (i != -1) {
					//REPLACING SUBLIST
					List<Tile> newPath = new ArrayList<Tile>();
					for (int j = 0; j < i + 1; j++){
						newPath.add(GameEngine.movePath.get(j));
					}
					GameEngine.movePath = newPath;
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
	public GameEngine.ButtonPress checkForButtonPress(GameState state) {

		GameEngine.ButtonPress returnedButton;
		
		//Check to see if back was pressed
		if (backClicked){
			backClicked = false;
			return GameEngine.ButtonPress.MENU;
		}

		// Button pushed
		if (Gdx.input.isTouched()) {
			// Get inputs
			int xPress = getX();
			int yPress = getY();

			// Look for new button press
			if (buttonDown == GameEngine.ButtonPress.NONE && lastX == -1) {
				if(state != GameState.WON){
					buttonDown = Menu.containingButtonOfPixel(xPress, yPress);
				} else {
					buttonDown = Menu.containingButtonOfPixelWon(xPress, yPress);
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
				if(state != GameState.WON){
					returnedButton = Menu.containingButtonOfPixel(lastX, lastY);
				} else {
					returnedButton = Menu.containingButtonOfPixelWon(lastX, lastY);
				}
				if (returnedButton != buttonDown) {
					returnedButton = GameEngine.ButtonPress.NONE;
				}
			}
			// HACK HERE TO GET GAME WINNING HAPPENING ON TOUCH
			else if (lastX != -1 && gameWonPressed) {
				returnedButton = GameEngine.ButtonPress.NEXT_LEVEL;
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
		int y = Gdx.graphics.getHeight() - Gdx.input.getY() - 1;
		return Math.min(Math.max(y, 0), Gdx.graphics.getHeight() - 1);
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
