package com.me.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	private static final int timeToMovePiece = 8;
	private static final int timeToFormBeam = 4;
	private static final int timeToBreakBeam = 4;
	private static final int timeToDestroyPiece = 0;
	private static final int timeToPaintPiece = 20;
	private static int timeSpentOnThisAnimation = 0;
	private static int totalTimeForThisAnimation = 0;

	private static AnimationState currentAnimationState = AnimationState.NOTANIMATING;
	private List<AnimationState> animationStack = new ArrayList<AnimationState>();

	//Keep track of what will happen
	private Color originalColor = Color.NONE; 
	private static List<Piece> piecesDestroyed = new ArrayList<Piece>();
	private static Laser laserRemoved = null;
	private static Laser laserMovedAlong = null;
	private static Laser laserCreated = null;
	private boolean wasPieceDestroyed = false;
	private Collection<Short> futureBoard;


	public enum GameState {
		PAUSED, IDLE, DECIDING, MOVING, DESTROYED, WON
	}

	public enum AnimationState {
		FORMING, MOVING, PAINTING, BREAKING, DESTRUCTION, NOTANIMATING;
		public static int getTime(AnimationState as){
			switch(as) {
			case FORMING: return timeToFormBeam;
			case BREAKING: return timeToBreakBeam;
			case DESTRUCTION: return timeToDestroyPiece;
			case MOVING: return timeToMovePiece;
			case NOTANIMATING: return 0;
			case PAINTING: return timeToPaintPiece;
			default: return 0;
			}
		}
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

				//Precompute the move
				moveCounter++;
				debug(moveCounter);

				//Prep the animations
				prepAnimationBeginning();
				
			}
			if (state == GameState.MOVING){
				
				//In between steps
				if (currentAnimationState == AnimationState.NOTANIMATING){
					// Move the piece
					movePiece();

					// Update the board state
					wasPieceDestroyed = updateBoardState();
					
					debug("Was piece destroyed? " + wasPieceDestroyed);
					for (AnimationState as : animationStack)
						debug(as);
					
					currentAnimationState = animationStack.remove(0);
					totalTimeForThisAnimation = AnimationState.getTime(currentAnimationState);
					
					//Record where we got to
					futureBoard = b.encodePieces();
					
					//Put the piece back
					b.move(movingPiece, movePath.get(0));
					movingPiece.setColor(originalColor);
				}
				
				//Increment animation time!
				timeSpentOnThisAnimation++;
				if (timeSpentOnThisAnimation > totalTimeForThisAnimation){
					timeSpentOnThisAnimation = 1;
					
					//More animations to go!
					if (!animationStack.isEmpty()){
						currentAnimationState = animationStack.remove(0);
						totalTimeForThisAnimation = AnimationState.getTime(currentAnimationState);
					} else {
						
						//Get the board where it should be now
						movePath.remove(0);
						b.resetPieces(futureBoard);
						movingPiece = b.getPieceOnTile(movePath.get(0));
						
						//Remove destroyed pieces here
						for (Piece p : piecesDestroyed){
							b.removePiece(p);
						}
						
						initializeLasers();
						prepAnimationBeginning();
						
						// No lockout after move
						if (movePath.size() == 1 || wasPieceDestroyed) {
							movingPiece = null;
							movePath.clear();

							// See which state to transition to
							if (wasPieceDestroyed) {
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
		}

		// Draw the game
		dg.draw(b, state, currentAnimationState, currentLevel);
	}
	
	//Set up animations
	private void prepAnimationBeginning(){
		animationStack.clear();
		currentAnimationState = AnimationState.NOTANIMATING;
		timeSpentOnThisAnimation = 0;
		piecesDestroyed.clear();
		laserRemoved = null;
		laserMovedAlong = null;
		laserCreated = null;
		originalColor = movingPiece.getColor();
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

		// Remove previous lasers
		boolean laserRemovedFromPiece = removeLasersFromPiece(movingPiece, movePath.get(1));
		if (laserRemovedFromPiece){
			animationStack.add(AnimationState.BREAKING);
		}

		// Tell the board to move the piece
		animationStack.add(AnimationState.MOVING);
		b.move(movingPiece, movePath.get(1));

	}

	// Updates the board after the piece has been moved
	public boolean updateBoardState() {
		// Check for piece destroyed

		boolean anyPiecesDestroyed = false;

		if (!checkIfPieceDestroyed(movingPiece)) {

			// Get painted
			boolean piecePainted = paintPiece(movingPiece);
			if (piecePainted){
				animationStack.add(AnimationState.PAINTING);
			}

			// Check for piece destroyed
			if (!checkIfPieceDestroyed(movingPiece)) {

				// Form new lasers and cause destruction
				List<Piece> destroyed = formLasersFromPieceAndDestroy(movingPiece, movePath.get(0), false);

				// Indicate that a piece was destroyed
				if (!destroyed.isEmpty()) {
					animationStack.add(AnimationState.DESTRUCTION);
					piecesDestroyed.addAll(destroyed);
					anyPiecesDestroyed = true;
				}

			} else {
				animationStack.add(AnimationState.DESTRUCTION);
				piecesDestroyed.add(movingPiece);
				anyPiecesDestroyed = true;
			}

		} else {
			animationStack.add(AnimationState.DESTRUCTION);
			piecesDestroyed.add(movingPiece);
			anyPiecesDestroyed = true;
		}
		return anyPiecesDestroyed;
	}

	// Add all lasers to the board
	public void initializeLasers() {
		b.lasers.clear();
		for (Piece p1 : b.getAllPieces()) {
			List<Piece> destroyed = formLasersFromPieceAndDestroy(p1, null, true);
			if (!destroyed.isEmpty()){
				debug("OH GOD WHY WHAT ARE YOU DOING YOU HEATHEN!" +
						"\nTHIS WOULD BE BEEPING IF I COULD MAKE IT.");
			}
		}
	}

	// Simple method to paint a piece
	public boolean paintPiece(Piece p) {

		// Is there a painter?
		Tile pieceTile = b.getTileAtBoardPosition(p.getXCoord(), p.getYCoord());

		// Paint the piece!
		if (pieceTile.hasPainter()) {
			p.setColor(pieceTile.getPainterColor());
			return true;
		}
		return false;
	}

	// Removes all lasers connected to the current piece
	public boolean removeLasersFromPiece(Piece p, Tile nextTile) {
		int xPos = p.getXCoord();
		int yPos = p.getYCoord();

		Laser possibleHorizLaser = null;
		Laser possibleVertLaser = null;

		for (Laser l : b.lasers) {
			// Check if connected to this piece
			if ((l.getXStart() == xPos && l.getYStart() == yPos) || 
					(l.getXFinish() == xPos && l.getYFinish() == yPos)) {
				// Laser is horizontal
				if (l.getXStart() != l.getXFinish()) {
					// Checking horizontal
					possibleHorizLaser = possibleHorizLaser == null? l : null;
				}
				// Laser goes up
				else {
					possibleVertLaser = possibleVertLaser == null? l : null;
				}

			}
		}

		//Figure out what happend to the lasers
		boolean horizMove = p.getXCoord() != nextTile.getXCoord();
		if (possibleHorizLaser != null){
			if (horizMove)
				laserMovedAlong = possibleHorizLaser;
			else
				laserRemoved = possibleHorizLaser;
		}
		if (possibleVertLaser != null){
			if (!horizMove)
				laserMovedAlong = possibleVertLaser;
			else
				laserRemoved = possibleVertLaser;
		}
		return (laserRemoved != null);
	}

	// Form lasers from a piece that has just moved return destroyed pieces
	public List<Piece> formLasersFromPieceAndDestroy(Piece p, Tile cameFrom, boolean addLasers) {
		
		//TODO: Use camefrom to not form lasers when moving along
		
		List<Piece> destroyed = new ArrayList<Piece>();

		// For each destruction
		List<Piece> possibleDestroy = new ArrayList<Piece>();

		// Check for left pieces
		Tile leftSameColor = null;

		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		Laser possibleFormed = null;

		// Slide to the left
		for (; leftSameColor == null && xPos >= 0; xPos--) {
			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor());
					if (addLasers){
						b.lasers.add(possibleFormed);
					}
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
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor());
					if (addLasers){
						b.lasers.add(possibleFormed);
					}
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (leftSameColor != null && rightSameColor != null) {
			possibleFormed = null;
			if (addLasers){
				b.lasers.remove(new Laser(leftSameColor.getXCoord(), leftSameColor.getYCoord(),
						rightSameColor.getXCoord(), rightSameColor.getYCoord(), p.getColor()));
			}
		}

		// If it's still possible, it was formed
		if (possibleFormed != null){
			laserCreated = possibleFormed;
			animationStack.add(AnimationState.FORMING);
		}
		possibleFormed = null;

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
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor());
					if (addLasers){
						b.lasers.add(possibleFormed);
					}
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
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor());
					if (addLasers){
						b.lasers.add(possibleFormed);
					}
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (botSameColor != null && topSameColor != null) {
			possibleFormed = null;
			if (addLasers){
				b.lasers.remove(new Laser(botSameColor.getXCoord(), botSameColor.getYCoord(),
						topSameColor.getXCoord(), topSameColor.getYCoord(), p.getColor()));
			}
		}
		// If it's still possible, it was formed
		if (possibleFormed != null){
			laserCreated = possibleFormed;
			animationStack.add(AnimationState.FORMING);
		}
		possibleFormed = null;

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

	public static int getTotalTicksForAnimation() {
		return totalTimeForThisAnimation;
	}

	public static int getTicksSpentOnAnimation() {
		return timeSpentOnThisAnimation;
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

	public static Laser getBrokenLaser(){
		return laserRemoved;
	}
	
	public static Laser getFormedLaser(){
		return laserCreated;
	}
	
	public static Laser getLaserMovedAlong(){
		return laserMovedAlong;
	}
	
	public static <T> void debug(T s){
		if (!DEBUG_MODE){
			return;
		}
		System.out.println(s);
	}
}
