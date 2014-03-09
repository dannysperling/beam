package com.me.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class GameEngine implements ApplicationListener {
	
	public static final boolean DEBUG_MODE = false;
	
	// Enter the levelID you want to play here:
	private int currentLevel = 0;

	// Simple Objects for now
	private Board b;
	private DrawGame dg;
	private InputHandler inputHandler;
	private LevelLoader levelLoader;
	private GameProgress progress;
	private LevelOrderer levelOrderer;

	private static int moveCounter = 0;

	public static Piece movingPiece = null;
	public static List<Tile> movePath = new ArrayList<Tile>();
	private static List<Collection<Short>> boardStack = new ArrayList<Collection<Short>>();

	// Animation constants in ticks
	private static final int timeOnTileBeforeMove = 7;

	private static int timeSpentOnTile = 0;

	public enum GameState {
		PAUSED, IDLE, DECIDING, MOVING, DESTROYED, WON
	}

	public enum ButtonPress {
		UNDO, RESET, REDO, MENU, NONE, WON // Note: WON should not exist in non-proto version
	}

	public enum Color {
		RED, BLUE, GREEN, NONE;
		public static Color lookup(int i) {
			switch (i) {
			case 1:
				return Color.RED;
			case 2:
				return Color.BLUE;
			case 3:
				return Color.GREEN;
			default:
				return Color.NONE;
			}
		}

		public int toIndex() {
			int i = 0;
			while (true) {
				if (Color.lookup(i) == this)
					return i;
				i++;
			}
		}
	}

	// Measured in terms of percentage of screen
	public static final float topBarSize = 0.22f;
	public static final float botBarSize = 0.13f;
	public static final float sideEmptySize = 0.02f;

	private GameState state = GameState.IDLE;

	//Menu time
	private boolean mainMenuShowing = true;
	private Menu menu;
	private DrawMenu dm;
	
	//Restoration
	private String tempData = "level.temp";
	private FileHandle tempFile;

	@Override
	public void create() {

		levelOrderer = new LevelOrderer("data/levels/levelOrder.txt");
		levelLoader = new LevelLoader("data/levels/levels.xml", levelOrderer);
		progress = new GameProgress(levelOrderer);

		loadLevel(currentLevel);

		dg = new DrawGame(progress);
		menu = new Menu(levelOrderer.getNumLevels(), progress);
		dm = new DrawMenu(menu);
		dg.initFonts();
		inputHandler = new InputHandler();
		
		Gdx.input.setCatchBackKey(true);
		
		tempFile = Gdx.files.local(tempData);
	}

	@Override
	public void dispose() {
		dg.dispose();
	}

	@Override
	public void render() {
		
		//Check for back pressed first
		inputHandler.checkBackPressed();

		//Handle the menu separately
		if (mainMenuShowing){

			int selected = inputHandler.handleMainMenuInput(menu);
			
			//Exit to leave
			if (selected == -2){
				
				//TODO: Do things to check if the player wants to leave.
				System.exit(0);
			}
			
			//Picked a level
			if (selected != -1){
				//Only reset if different level
				mainMenuShowing = false;
				if (selected != currentLevel){
					currentLevel = selected;
					loadLevel(currentLevel);
				}
			}

			dm.draw();
			return;
		}


		boolean pushedButton = false;

		if (state != GameState.DECIDING/* && state != GameState.WON */) {
			ButtonPress button = inputHandler.checkForButtonPress();

			if (button == ButtonPress.REDO || button == ButtonPress.RESET || button == ButtonPress.UNDO) {
				debug(button);
				pushedButton = true;
				handleButtonPress(button);
			}

			// Increase level. Should be done elsewhere in non-proto version
			else if (state == GameState.WON && button == ButtonPress.WON) {
				currentLevel++;
				if (currentLevel < levelOrderer.getNumLevels()){
					loadLevel(currentLevel);
				} else {
					currentLevel--;
					menu.scrollToLevel(currentLevel);
					mainMenuShowing=true;
				}
				pushedButton = true;
			}
			
			else if (button == ButtonPress.MENU){
				mainMenuShowing = true;
				menu.scrollToLevel(currentLevel);
				pushedButton = true;
			}
		}

		// Only check for game in this case
		if (!pushedButton) {
			// Get input from the user
			GameState pastState = state;
			state = inputHandler.handleGameInput(b, state);

			// Increment the moves when appropriate
			if (pastState == GameState.DECIDING && state == GameState.MOVING) {
				moveCounter++;
				debug(moveCounter);
			}

			// Do things if we're moving
			if (state == GameState.MOVING) {
				// Check to see if we actually move yet
				if (timeSpentOnTile < timeOnTileBeforeMove) {
					timeSpentOnTile++;
				} else {

					// Move the piece
					movePiece();

					// Update the board state
					boolean pieceDestroyed = updateBoardState();

					// No lockout after move
					if (movePath.size() == 1 || pieceDestroyed) {
						movingPiece = null;
						movePath.clear();

						// See which state to transition to
						if (pieceDestroyed) {
							state = GameState.DESTROYED;
						} else {
							// Made a move
							state = GameState.IDLE;

							// Push the move onto the stack
							boardStack.add(moveCounter, (b.encodePieces()));

							// Remove the old future
							List<Collection<Short>> newStack = new ArrayList<Collection<Short>>();
							for (int i = 0; i < moveCounter+1; i++)
								newStack.add(boardStack.get(i));
							boardStack = newStack; 

							if (b.isWon()) {
								state = GameState.WON;

								int numStars = 1;
								if (moveCounter <= b.perfect){
									numStars = 3;
								} else if (moveCounter <= b.par){
									numStars = 2;
								}
								boolean improved = progress.setLevelScore(currentLevel, moveCounter, numStars);

								//TODO: Currently temporary. Should pop-up win menu
								if (improved){
									debug("New record on level " + currentLevel + ": " + moveCounter + " moves!");
								}
							}
						}
					}
				}
			}
		}

		// Draw the game
		dg.draw(b, state, currentLevel);
	}

	private void handleButtonPress(ButtonPress button) {

		// Do things depending on which button was pressed
		switch (button) {
		case UNDO:
			moveCounter = Math.max(moveCounter - 1, 0);
			break;
		case RESET:
			moveCounter = 0;
			List<Collection<Short>> newStack = new ArrayList<Collection<Short>>();
			newStack.add(boardStack.get(0));
			boardStack = newStack;
			break;
		case REDO:
			// Make sure there's a move to go to
			if (boardStack.size() > moveCounter + 1) {
				moveCounter++;
			} else {
				// DON'T DO ANYTHING IF THERE'S NOTHING TO REDO
				return;
			}
			break;
		default: // Shouldn't occur
			break;
		}

		// Reset things
		b.resetPieces(boardStack.get(moveCounter));
		movingPiece = null;
		movePath.clear();
		timeSpentOnTile = 0;
		initializeLasers();
		if (b.isWon())
			state = GameState.WON;
		else 
			state = GameState.IDLE;
	}

	// Loads a level, and handles initializations
	public void loadLevel(int levelNumber) {

		// Load the world
		b = levelLoader.getLevel(levelNumber);
		if (b == null) {
			// TODO: fail correctly when the game is out of levels.
			debug("No further levels exist.");
			System.exit(1);
		}

		// Clean out all the inits
		movingPiece = null;
		movePath.clear();

		// Clear the board stack
		boardStack.clear();
		boardStack.add(b.encodePieces());

		// Set up the state and move counter
		state = GameState.IDLE;
		moveCounter = 0;

		// Initialize the lasers
		initializeLasers();
	}

	// Moves a piece, and handles changes
	public void movePiece() {

		// Reset time on tile
		timeSpentOnTile = 0;

		// Get rid of the place we were
		movePath.remove(0);

		// Remove previous lasers
		removeLasersFromPiece(movingPiece);

		// Tell the board to move the piece
		b.move(movingPiece, movePath.get(0));

	}

	// Updates the board after the piece has been moved
	public boolean updateBoardState() {
		// Check for piece destroyed

		boolean piecesDestroyed = false;

		if (!checkIfPieceDestroyed(movingPiece)) {

			// Get painted
			paintPiece(movingPiece);

			// Check for piece destroyed
			if (!checkIfPieceDestroyed(movingPiece)) {

				// Form new lasers and cause destruction
				List<Piece> destroyed = formLasersFromPieceAndDestroy(movingPiece);
				for (Piece p : destroyed) {
					b.removePiece(p);
					removeLasersFromPiece(p);
				}

				// Indicate that a piece was destroyed
				if (!destroyed.isEmpty()) {
					piecesDestroyed = true;
				}

			} else {
				b.removePiece(movingPiece);
				piecesDestroyed = true;
			}

		} else {
			b.removePiece(movingPiece);
			piecesDestroyed = true;
		}
		return piecesDestroyed;
	}

	// Add all lasers to the board
	public void initializeLasers() {
		b.lasers.clear();
		for (Piece p1 : b.getAllPieces()) {
			List<Piece> destroyed = formLasersFromPieceAndDestroy(p1);
			if (!destroyed.isEmpty()){
				debug("OH GOD WHY WHAT ARE YOU DOING YOU HEATHEN!" +
						"\nTHIS WOULD BE BEEPING IF I COULD MAKE IT.");
			}
		}
	}

	// Simple method to paint a piece
	public void paintPiece(Piece p) {

		// Is there a painter?
		Tile pieceTile = b.getTileAtBoardPosition(p.getXCoord(), p.getYCoord());

		// Paint the piece!
		if (pieceTile.hasPainter()) {
			p.setColor(pieceTile.getPainterColor());
		}
	}

	// Removes all lasers connected to the current piece
	public void removeLasersFromPiece(Piece p) {
		Set<Laser> survivingLasers = new HashSet<Laser>();

		Tile leftTile = null;
		Tile topTile = null;
		Tile botTile = null;
		Tile rightTile = null;

		int xPos = p.getXCoord();
		int yPos = p.getYCoord();

		for (Laser l : b.lasers) {
			// Check if started at same place
			if (l.getXStart() == xPos && l.getYStart() == yPos) {
				// Laser goes to the right
				if (l.getXStart() < l.getXFinish()) {
					rightTile = new Tile(l.getXFinish(), l.getYFinish());

					// New laser formed
					if (leftTile != null) {
						Laser newLaser = new Laser(leftTile.getXCoord(),
								leftTile.getYCoord(), rightTile.getXCoord(),
								rightTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}
				// Laser goes up
				else {
					topTile = new Tile(l.getXFinish(), l.getYFinish());

					if (botTile != null) {
						Laser newLaser = new Laser(botTile.getXCoord(),
								botTile.getYCoord(), topTile.getXCoord(),
								topTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}

			} else if (l.getXFinish() == xPos && l.getYFinish() == yPos) {
				// Laser goes to the left
				if (l.getXStart() < l.getXFinish()) {
					leftTile = new Tile(l.getXStart(), l.getYStart());

					// New laser formed
					if (rightTile != null) {
						Laser newLaser = new Laser(leftTile.getXCoord(),
								leftTile.getYCoord(), rightTile.getXCoord(),
								rightTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}
				// Laser goes down
				else {
					botTile = new Tile(l.getXStart(), l.getYStart());

					if (topTile != null) {
						Laser newLaser = new Laser(botTile.getXCoord(),
								botTile.getYCoord(), topTile.getXCoord(),
								topTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}
			} else {
				// Laser survives
				survivingLasers.add(l);
			}
		}
		// Modify the board's lasers
		b.lasers = survivingLasers;
	}

	// Form lasers from a piece that has just moved return destroyed pieces
	public List<Piece> formLasersFromPieceAndDestroy(Piece p) {
		List<Piece> destroyed = new ArrayList<Piece>();

		// For each destruction
		List<Piece> possibleDestroy = new ArrayList<Piece>();

		// Check for left pieces
		Tile leftSameColor = null;

		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		// Slide to the left
		for (; leftSameColor == null && xPos >= 0; xPos--) {
			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Tile rightSameColor = null;

		xPos = p.getXCoord() + 1;

		// Slide to the right
		for (; rightSameColor == null && xPos < b.getNumHorizontalTiles(); xPos++) {

			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (leftSameColor != null && rightSameColor != null) {
			b.lasers.remove(new Laser(leftSameColor.getXCoord(), leftSameColor
					.getYCoord(), rightSameColor.getXCoord(), rightSameColor
					.getYCoord(), p.getColor()));
		}

		possibleDestroy.clear();

		// Now do vertical!

		// Check for bot pieces
		Tile botSameColor = null;

		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Slide down
		for (; botSameColor == null && yPos >= 0; yPos--) {
			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Tile topSameColor = null;

		yPos = p.getYCoord() + 1;

		// Slide up
		for (; topSameColor == null && yPos < b.getNumVerticalTiles(); yPos++) {

			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (botSameColor != null && topSameColor != null) {
			b.lasers.remove(new Laser(botSameColor.getXCoord(), botSameColor
					.getYCoord(), topSameColor.getXCoord(), topSameColor
					.getYCoord(), p.getColor()));
		}

		return destroyed;
	}

	// Pieces in the list of pieces are destroyed
	public boolean checkIfPieceDestroyed(Piece p) {

		// Check if p is destroyed. First, horizontally
		Color leftColor = Color.NONE;
		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		// Slide to left
		for (; leftColor == Color.NONE && xPos >= 0; xPos--) {

			Piece atLeft = b.getPieceOnTile(b
					.getTileAtBoardPosition(xPos, yPos));

			if (atLeft != null) {
				leftColor = atLeft.getColor();
			}
		}

		if (leftColor != Color.NONE && leftColor != p.getColor()) {
			Color rightColor = Color.NONE;

			xPos = p.getXCoord() + 1;

			// Slide to the right
			for (; rightColor == Color.NONE && xPos < b.getNumHorizontalTiles(); xPos++) {

				Piece atRight = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
						yPos));

				if (atRight != null) {
					rightColor = atRight.getColor();
				}
			}
			// Criss cross
			if (leftColor == rightColor) {
				return true;
			}
		}

		// Now vertically
		Color topColor = Color.NONE;
		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Step up
		for (; topColor == Color.NONE && yPos >= 0; yPos--) {

			Piece atTop = b
					.getPieceOnTile(b.getTileAtBoardPosition(xPos, yPos));

			if (atTop != null) {
				topColor = atTop.getColor();
			}
		}

		if (topColor != Color.NONE && topColor != p.getColor()) {
			Color botColor = Color.NONE;

			yPos = p.getYCoord() + 1;

			// Step down
			for (; botColor == Color.NONE && yPos < b.getNumVerticalTiles(); yPos++) {

				Piece atBot = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
						yPos));

				if (atBot != null) {
					botColor = atBot.getColor();
				}
			}
			// Criss cross
			if (topColor == botColor) {
				return true;
			}
		}

		return false;
	}

	public static int getTicksPerTile() {
		return timeOnTileBeforeMove;
	}

	public static int getTimeOnThisTile() {
		return timeSpentOnTile;
	}

	public static int getMoveCount() {
		return moveCounter;
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		String toSave = currentLevel + ";" + moveCounter + ";" + mainMenuShowing + ";";
		for (Collection<Short> curBoard : boardStack){
			for (Short s : curBoard){
				toSave += s + "--";
			}
			toSave += ";";
		}
		debug("Writing " + toSave);
		tempFile.writeString(toSave, false);
	}

	@Override
	public void resume() {
		dg.initFonts();
		dm.initFonts();
		String fromTemp = tempFile.readString();
		debug("Read " + fromTemp);
		if (fromTemp != null){
			String[] parts = fromTemp.split(";");
			
			//Get the level
			currentLevel = Integer.parseInt(parts[0]);
			loadLevel(currentLevel);
			
			//Set up moves and menu
			moveCounter = Integer.parseInt(parts[1]);
			mainMenuShowing = Boolean.parseBoolean(parts[2]);
			
			//Set up the stack
			boardStack.clear();
			for (int i = 3; i < parts.length; i++){
				List<Short> move = new ArrayList<Short>();
				String[] subParts = parts[i].split("--");
				for (String s : subParts){
					move.add(Short.parseShort(s));
				}
				boardStack.add(move);
			}
			
			b.resetPieces(boardStack.get(boardStack.size() - 1));
			initializeLasers();
			
			menu.scrollToLevel(currentLevel);
		}
	}
	
	public static <T> void debug(T s){
		if (!DEBUG_MODE){
			return;
		}
		System.out.println(s);
	}
}
