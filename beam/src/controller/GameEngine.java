package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import view.DrawGame;
import view.DrawMenu;

import model.Board;
import model.GameProgress;
import model.Laser;
import model.Menu;
import model.Piece;
import model.Tile;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import controller.Logger.LogType;

public class GameEngine implements ApplicationListener {

	public static final boolean DEBUG_MODE = false;
	public static final boolean LOGGING = true;

	// Simple Objects for now
	private Board b;
	private DrawGame dg;
	private InputHandler inputHandler;
	private LevelLoader levelLoader;
	private GameProgress progress;
	private LevelOrderer levelOrderer;
		
	private int currentWorld = -1;
	private int currentOrdinalInWorld = -1;
	private static int moveCounter = 0;

	public static Piece movingPiece = null;
	public static List<Tile> movePath = new ArrayList<Tile>();
	private static List<Collection<Short>> boardStack = new ArrayList<Collection<Short>>();

	// Animation constants in ticks
	private static final int timeToMovePiece = 8;
	private static final int timeToFormBeam = 12;
	private static final int timeToBreakBeam = 12;
	private static final int timeToDestroyPiece = 60;
	private static final int timeToPaintPiece = 20;
	private static int timeSpentOnThisAnimation = 0;
	private static int totalTimeForThisAnimation = 0;
	
	private static final int timeForIntro = 300;
	private static int timeSpentOnIntro = 0;
	
	private static final int timeBeforeDeathMessage = 120;
	private static int timeDead = 0;
	
	private static int timeWon = 0;
	public static final int wonAnimationUnit = 14;
	
	
	private static AnimationState currentAnimationState = AnimationState.NOTANIMATING;
	private List<AnimationState> animationStack = new ArrayList<AnimationState>();

	//Keep track of what will happen
	private Color originalColor = Color.NONE; 
	private static List<Piece> piecesDestroyed = new ArrayList<Piece>();
	private static Laser laserRemoved = null;
	private static Laser laserMovedAlong = null;
	private static List<Laser> lasersCreated = new ArrayList<Laser>();
	private boolean wasPieceDestroyed = false;
	private Collection<Short> futureBoard;


	public enum GameState {
		PAUSED, IDLE, DECIDING, MOVING, DESTROYED, WON, INTRO
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
		UNDO, RESET, REDO, MENU, NEXT_LEVEL, SKIPWIN, NONE
	}

	public enum Color {
		RED, BLUE, GREEN, ORANGE, PURPLE, NONE;
		public static Color lookup(int i) {
			switch (i) {
			case 1:
				return Color.RED;
			case 2:
				return Color.BLUE;
			case 3:
				return Color.GREEN;
			case 4:
				return Color.ORANGE;
			case 5:
				return Color.PURPLE;
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
	
	//Logging
	private int undoTimes = 0;
	private int resetTimes = 0;
	private int redoTimes = 0;
	private int deaths = 0;
	
	@Override
	public void create() {

		//Use GDX for both the orderer and the loader
		levelOrderer = new LevelOrderer("data/levels/levelOrder.txt", true);
		levelLoader = new LevelLoader("data/levels/levels.xml", levelOrderer, true);
		progress = new GameProgress(levelOrderer);

		dg = new DrawGame(progress);
		menu = new Menu(levelOrderer.getWorldSizes(), progress);
		
		List<List<Board>> allBoards = initializeBoards();
		
		dm = new DrawMenu(menu, dg, allBoards);
		dg.initFonts();
		inputHandler = new InputHandler();

		Gdx.input.setCatchBackKey(true);

		tempFile = Gdx.files.local(tempData);
		
		if (LOGGING)
			Logger.initialize(levelOrderer.getMapping());
	}
	
	private List<List<Board>> initializeBoards(){
		
		List<List<Board>> allBoards = new ArrayList<List<Board>>();
		int numWorlds = levelOrderer.getNumWorlds();
		for (int world = 1; world <= numWorlds; world++){
			
			//Add the world of boards
			List<Board> curWorldBoards = new ArrayList<Board>();
			
			int worldSize = levelOrderer.getWorldSize(world);
			for (int ordinalInWorld = 1; ordinalInWorld <= worldSize; ordinalInWorld++){
				Board cur = levelLoader.getLevel(world, ordinalInWorld);
				initializeLasers(cur);
				curWorldBoards.add(cur);
			}
			allBoards.add(curWorldBoards);
		}
		
		return allBoards;
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
			
			if (LOGGING && selected == -3){
				logEnd();
				clearAllData();
				Logger.startNewSession();
			}

			//Exit to leave
			if (selected == -2){
				if (LOGGING){
					logEnd();
				}
				//TODO: Do things to check if the player wants to leave.
				System.exit(0);
			}

			//Picked a level
			if (selected == 0){
				
				int selectedWorld = inputHandler.getMostRecentlySelectedWorld();
				int selectedOrdinalInWorld = inputHandler.getMostRecentlySelectedOrdinalInWorld();
				
				//Check that it's unlocked
				boolean unlocked = menu.isLevelUnlocked(selectedWorld, selectedOrdinalInWorld);
				
				if (unlocked){
					//Enter the level if it's unlocked
					mainMenuShowing = false;
					
					//Only reset if different level
					if (selectedOrdinalInWorld != currentOrdinalInWorld || selectedWorld != currentWorld){
						if (LOGGING){
							if (currentWorld != -1){
								logEnd();
							}
							Logger.enteredLevel(selectedWorld, selectedOrdinalInWorld);
						}
						currentWorld = selectedWorld;
						currentOrdinalInWorld = selectedOrdinalInWorld;
						loadLevel(currentWorld, currentOrdinalInWorld);
					}
				}
			}

			dm.draw(b, currentWorld, currentOrdinalInWorld);
			return;
		}


		boolean pushedButton = false;

		if (state != GameState.DECIDING) {
			ButtonPress button = inputHandler.checkForButtonPress(state);
			if(button == ButtonPress.SKIPWIN){
				timeWon = wonAnimationUnit * 10;
			} else if (state == GameState.WON){
				if(button == ButtonPress.RESET || button == ButtonPress.MENU || button == ButtonPress.NEXT_LEVEL){
					int numStars = 1;
					if (GameEngine.getMoveCount() <= b.perfect){
						numStars = 3;
					} else if (GameEngine.getMoveCount() <= b.par){
						numStars = 2;
					}
					if(timeWon < (numStars + 2) * wonAnimationUnit){
						timeWon = wonAnimationUnit * 10;
						button = ButtonPress.SKIPWIN;
					} else if (button == ButtonPress.MENU){
						resetCurrentLevel();
						state = GameState.IDLE;
					} 
					// Go to the next level
					else if (button == ButtonPress.NEXT_LEVEL){
						state = GameState.IDLE;
						if (LOGGING){
							logEnd();
						}
						
						//TODO: Handle bonus levels and locked levels
						
						//Increment level and possibly world
						currentOrdinalInWorld++;
						if (currentOrdinalInWorld > levelOrderer.getWorldSize(currentWorld)){
							currentOrdinalInWorld = 1;
							currentWorld++;
						}
						
						//Check that there are remaining levels
						if (currentWorld < levelOrderer.getNumWorlds()){
							loadLevel(currentWorld, currentOrdinalInWorld);
							
							if (LOGGING){
								Logger.enteredLevel(currentWorld, currentOrdinalInWorld);
							}
						} 
						//No levels remaining
						else {
							currentWorld--;
							currentOrdinalInWorld = levelOrderer.getWorldSize(currentWorld);
							menu.scrollToLevel(currentWorld, currentOrdinalInWorld);
							mainMenuShowing=true;
						}
						pushedButton = true;
					} else {
						state = GameState.IDLE;
					}
				}
			}
			
			if (button == ButtonPress.REDO || button == ButtonPress.RESET || button == ButtonPress.UNDO) {
				debug(button);
				pushedButton = true;
				handleButtonPress(button);
			}

			else if (button == ButtonPress.MENU){
				mainMenuShowing = true;
				menu.scrollToLevel(currentWorld, currentOrdinalInWorld);
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
			if (state == GameState.DESTROYED){
				timeDead++;
			} else {
				timeDead = 0;
			}
			
			if (state == GameState.WON){
				timeWon++;
			} else {
				timeWon = 0;
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
						
						if (currentAnimationState == AnimationState.DESTRUCTION){
							deaths++;
							goBackToTheFuture();
						}
					} else {
						
						//Get the board where it should be now
						if (currentAnimationState != AnimationState.DESTRUCTION){
							goBackToTheFuture();
						}
						
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

								// Remove the old future, if it exists
								if (boardStack.size() >= moveCounter + 1){
									List<Collection<Short>> newStack = new ArrayList<Collection<Short>>();
									for (int i = 0; i < moveCounter+1; i++)
										newStack.add(boardStack.get(i));
									boardStack = newStack; 
								}

								if (b.isWon()) {
									state = GameState.WON;

									int numStars = 1;
									if (moveCounter <= b.perfect){
										numStars = 3;
									} else if (moveCounter <= b.par){
										numStars = 2;
									}
									progress.setLevelScore(currentWorld, currentOrdinalInWorld, moveCounter, numStars);
								}
							}
						} 
					}
				}
			} else if (state == GameState.INTRO){
				if(timeSpentOnIntro >= timeForIntro){
					state = GameState.IDLE;
				} else {
					timeSpentOnIntro++;
				}
			}
		}

		// Draw the game or menu
		if (!mainMenuShowing)
			dg.draw(b, state, currentAnimationState, currentWorld, currentOrdinalInWorld, menu.colorOfLevel(currentWorld, currentOrdinalInWorld));
		else
			dm.draw(b, currentWorld, currentOrdinalInWorld);
	}
	
	//Removes all user data. Be careful if you call this.
	private void clearAllData() {
		currentWorld = -1;
		currentOrdinalInWorld = -1;
		progress.clearAllData();
	}

	//Set up animations
	private void prepAnimationBeginning(){
		animationStack.clear();
		currentAnimationState = AnimationState.NOTANIMATING;
		timeSpentOnThisAnimation = 0;
		piecesDestroyed.clear();
		laserRemoved = null;
		laserMovedAlong = null;
		lasersCreated.clear();
		if (movingPiece != null){
			originalColor = movingPiece.getColor();
		} else {
			originalColor = Color.NONE;
		}
	}
	
	//Return to where we should be
	private void goBackToTheFuture(){
		movePath.remove(0);
		b.resetPieces(futureBoard);

		movingPiece = b.getPieceOnTile(movePath.get(0));
		
		//Remove destroyed pieces here
		List<Piece> newDestroyed = new ArrayList<Piece>();
		for (Piece p : piecesDestroyed){
			//Either piece removed, or was moving piece
			if (!b.removePiece(p)){
				b.removePiece(movingPiece);
				newDestroyed.add(movingPiece);
			} else {
				newDestroyed.add(p);
			}
		}
		piecesDestroyed = newDestroyed;
		
		initializeLasers(b);
	}

	private void handleButtonPress(ButtonPress button) {

		// Do things depending on which button was pressed
		switch (button) {
		case UNDO:
			moveCounter = Math.max(moveCounter - 1, 0);
			if (LOGGING){
				undoTimes++;
			}
			break;
		case RESET:
			resetCurrentLevel();
			state = GameState.IDLE;
			if (LOGGING){
				resetTimes++;
			}
			return;
		case REDO:
			// Make sure there's a move to go to
			if (boardStack.size() > moveCounter + 1) {
				moveCounter++;
				if (LOGGING){
					redoTimes++;
				}
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
		prepAnimationBeginning();
		initializeLasers(b);
		if (b.isWon())
			state = GameState.WON;
		else 
			state = GameState.IDLE;
	}
	
	//Resets the current level to its original state
	private void resetCurrentLevel(){
		moveCounter = 0;
		List<Collection<Short>> newStack = new ArrayList<Collection<Short>>();
		newStack.add(boardStack.get(0));
		b.resetPieces(boardStack.get(moveCounter));
		movingPiece = null;
		movePath.clear();
		prepAnimationBeginning();
		initializeLasers(b);
		boardStack = newStack;
	}

	// Loads a level, and handles initializations
	private void loadLevel(int world, int ordinalInWorld) {

		// Load the world
		b = levelLoader.getLevel(world, ordinalInWorld);
		if (b == null) {
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
		state = GameState.INTRO;
		timeSpentOnIntro = 0;
		moveCounter = 0;

		// Initialize the lasers
		initializeLasers(b);
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
				List<Piece> destroyed = formLasersFromPieceAndDestroy(b, movingPiece, movePath.get(0), false);

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
	public void initializeLasers(Board board) {
		board.lasers.clear();
		for (Piece p1 : board.getAllPieces()) {
			List<Piece> destroyed = formLasersFromPieceAndDestroy(board, p1, null, true);
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
		if (pieceTile.hasPainter() && pieceTile.getPainterColor() != p.getColor()) {
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
	public List<Piece> formLasersFromPieceAndDestroy(Board board, Piece p, Tile cameFrom, boolean addLasers) {
		
		boolean horizontalMove = false;
		if (cameFrom != null)
			horizontalMove = p.getYCoord() == cameFrom.getYCoord();
		
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
			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor());
					if (addLasers){
						board.lasers.add(possibleFormed);
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
		for (; rightSameColor == null && xPos < board.getNumHorizontalTiles(); xPos++) {

			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor());
					if (addLasers){
						board.lasers.add(possibleFormed);
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
				board.lasers.remove(new Laser(leftSameColor.getXCoord(), leftSameColor.getYCoord(),
						rightSameColor.getXCoord(), rightSameColor.getYCoord(), p.getColor()));
			}
		}

		// If it's still possible, it was formed
		if (!addLasers && possibleFormed != null && (originalColor != p.getColor() || !horizontalMove)){
			lasersCreated.add(possibleFormed);
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
			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor());
					if (addLasers){
						board.lasers.add(possibleFormed);
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
		for (; topSameColor == null && yPos < board.getNumVerticalTiles(); yPos++) {

			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor());
					if (addLasers){
						board.lasers.add(possibleFormed);
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
				board.lasers.remove(new Laser(botSameColor.getXCoord(), botSameColor.getYCoord(),
						topSameColor.getXCoord(), topSameColor.getYCoord(), p.getColor()));
			}
		}
		// If it's still possible, it was formed
		if (!addLasers && possibleFormed != null && (originalColor != p.getColor() || horizontalMove)){
			lasersCreated.add(possibleFormed);
		}
		possibleFormed = null;
		
		if (!lasersCreated.isEmpty() && !addLasers){
			animationStack.add(AnimationState.FORMING);
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

	public static int getTotalTicksForAnimation() {
		return totalTimeForThisAnimation;
	}

	public static int getTicksSpentOnAnimation() {
		return timeSpentOnThisAnimation;
	}

	public static int getMoveCount() {
		return moveCounter;
	}

	public static List<Piece> getDestroyedPieces(){
		return piecesDestroyed;
	}
	
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
		if (currentWorld != -1){
			String toSave = currentWorld + ";" + currentOrdinalInWorld + ";" + moveCounter + ";" + mainMenuShowing + ";";
			for (Collection<Short> curBoard : boardStack){
				for (Short s : curBoard){
					toSave += s + "--";
				}
				toSave += ";";
			}
			debug("Writing " + toSave);
			tempFile.writeString(toSave, false);
		} else {
			tempFile.delete();
		}
	}

	@Override
	public void resume() {
		dg.initFonts();
		dm.initFonts();
		
		//Check if there's data for to read
		if (!tempFile.exists())
			return;
		
		String fromTemp = tempFile.readString();
		debug("Read " + fromTemp);
		if (fromTemp != null){
			String[] parts = fromTemp.split(";");

			//Get the level
			currentWorld = Integer.parseInt(parts[0]);
			currentOrdinalInWorld = Integer.parseInt(parts[1]);
			loadLevel(currentWorld, currentOrdinalInWorld);

			//Set up moves and menu
			moveCounter = Integer.parseInt(parts[2]);
			mainMenuShowing = Boolean.parseBoolean(parts[3]);

			//Set up the stack
			boardStack.clear();
			for (int i = 4; i < parts.length; i++){
				List<Short> move = new ArrayList<Short>();
				String[] subParts = parts[i].split("--");
				for (String s : subParts){
					move.add(Short.parseShort(s));
				}
				boardStack.add(move);
			}

			b.resetPieces(boardStack.get(moveCounter));
			initializeLasers(b);
			
			//Handle having won on restarting level
			if (!mainMenuShowing && b.isWon()){
				state = GameState.WON;
				timeWon = wonAnimationUnit * 10;
			}
			menu.scrollToLevel(currentWorld, currentOrdinalInWorld);
		}
	}

	public static Laser getBrokenLaser(){
		return laserRemoved;
	}
	
	public static List<Laser> getFormedLaser(){
		return lasersCreated;
	}
	
	public static Laser getLaserMovedAlong(){
		return laserMovedAlong;
	}
	
	public static float getIntroProgress(){
		return ((float)(timeSpentOnIntro)) / timeForIntro;
	}
	
	public static int getTimeDead(){
		return timeDead;
	}
	
	public static int getTimeBeforeDeathBeam(){
		return timeBeforeDeathMessage;
	}
	
	public static int getTimeWon(){
		return timeWon;
	}
	
	
	public static <T> void debug(T s){
		if (!DEBUG_MODE){
			return;
		}
		System.out.println(s);
	}
	
	private void logEnd(){
		if (currentWorld != -1){
			Logger.log(LogType.UNDO, undoTimes);
			Logger.log(LogType.RESET, resetTimes);
			Logger.log(LogType.REDO, redoTimes);
			Logger.log(LogType.DEATH, deaths);
			Logger.exitedLevel();
			undoTimes = 0;
			resetTimes = 0;
			redoTimes = 0;
			deaths = 0;
		}
	}
}