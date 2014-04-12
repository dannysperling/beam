package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import utilities.Constants;
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

	/**
	 * Game engine has references to each of the main game elements
	 */
	private Board b;
	private Board nextBoard;
	private DrawGame dg;
	private InputHandler inputHandler;
	private LevelLoader levelLoader;
	private GameProgress progress;
	private LevelOrderer levelOrderer;
	private Menu menu;
	private DrawMenu dm;

	/**
	 * Keep a reference to which level we're on
	 */
	private int currentWorld = -1;
	private int currentOrdinalInWorld = -1;

	private int nextLvWorld = -1;
	private int nextOrdinal = -1;

	/**
	 * Keep data on what's moving and where we've been
	 */
	private static int moveCounter = 0;
	public static Piece movingPiece = null;
	public static List<Tile> movePath = new ArrayList<Tile>();
	private static List<Collection<Short>> boardStack = new ArrayList<Collection<Short>>();

	/**
	 * All the animation constants and counters
	 */
	private static int timeSpentOnThisAnimation = 0;
	private static int totalTimeForThisAnimation = 0;
	private static int timeSpentOnIntro = 0;
	private static int timeSpentOnTransition = 0; 
	private static int timeSpentLeavingMenu = 0;
	private static int timeDead = 0;
	private static int timeWon = 0;

	/**
	 * Keep track of our current animation, and where it's going
	 */
	private static AnimationState currentAnimationState = AnimationState.NOTANIMATING;
	private List<AnimationState> animationStack = new ArrayList<AnimationState>();

	/**
	 * Keep track of what will happen, for animation purposes
	 */
	private Color originalColor = Color.NONE;
	private static List<Piece> piecesDestroyed = new ArrayList<Piece>();
	private static Laser laserRemoved = null;
	private static Laser laserMovedAlong = null;
	private static List<Laser> lasersCreated = new ArrayList<Laser>();
	private boolean wasPieceDestroyed = false;
	private Collection<Short> futureBoard;

	/**
	 * Enumeration of each of the states the game can be in
	 */
	public enum GameState {
		IDLE, DECIDING, MOVING, DESTROYED, WON, INTRO, LEVEL_TRANSITION, MENU_TO_LEVEL_TRANSITION
	}

	/**
	 * Enumeration of each of the animation states the game can currently be in
	 */
	public enum AnimationState {
		FORMING, MOVING, PAINTING, BREAKING, DESTRUCTION, NOTANIMATING;
		public static int getTime(AnimationState as) {
			switch (as) {
			case FORMING:
				return Constants.TIME_TO_FORM_BEAM;
			case BREAKING:
				return Constants.TIME_TO_BREAK_BEAM;
			case DESTRUCTION:
				return Constants.TIME_TO_DESTROY_PIECE;
			case MOVING:
				return Constants.TIME_TO_MOVE_PIECE;
			case NOTANIMATING:
				return 0;
			case PAINTING:
				return Constants.TIME_TO_PAINT_PIECE;
			default:
				return 0;
			}
		}
	}

	/**
	 * Enumeration of all button press types
	 */
	public enum ButtonPress {
		UNDO, RESET, MENU, INFO, NEXT_LEVEL, SKIPWIN, NONE
	}

	/**
	 * Enumeration of all colors
	 */
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

		// Easy conversion from colors to indices
		public int toIndex() {
			int i = 0;
			while (true) {
				if (Color.lookup(i) == this)
					return i;
				i++;
			}
		}
	}

	/**
	 * State of the game
	 */
	private GameState state = GameState.IDLE;
	private boolean mainMenuShowing = true;

	/**
	 * These variables allow our restoration protocol to work
	 */
	private String tempData = "level.temp";
	private FileHandle tempFile;

	/**
	 * These variables are for logging purposes
	 */
	private int undoTimes = 0;
	private int resetTimes = 0;
	private int redoTimes = 0;
	private int deaths = 0;

	/**
	 * Initialize the GameEngine. Called once at the very start of the game
	 */
	@Override
	public void create() {

		// Use GDX for both the orderer and the loader
		levelOrderer = new LevelOrderer("data/levels/levelOrder.txt", true);
		levelLoader = new LevelLoader("data/levels/levels.xml", levelOrderer,
				true);
		progress = new GameProgress(levelOrderer);

		// Create the drawing
		dg = new DrawGame(progress);
		dg.initFonts();

		// Create the menu
		menu = new Menu(levelOrderer.getWorldSizes(), progress);

		// Create the menu drawer
		List<List<Board>> allBoards = initializeBoards();
		dm = new DrawMenu(menu, dg, allBoards);
		dm.initFonts();

		// Set up input handling
		inputHandler = new InputHandler();
		Gdx.input.setCatchBackKey(true);

		// Set up restoration
		tempFile = Gdx.files.local(tempData);

		// Set up logging, if applicable
		if (Constants.LOGGING)
			Logger.initialize(levelOrderer.getMapping());
	}

	/**
	 * Called at the end of program execution to dispose of drawing objects
	 */
	@Override
	public void dispose() {
		dg.dispose();
		dm.dispose();
	}

	/**
	 * Initialize a mapping of all the boards. This is used in the menu drawing
	 * code to be able to quickly draw all boards.
	 * 
	 * @return A list of lists of boards. Each inner list represents a world of
	 *         levels.
	 */
	private List<List<Board>> initializeBoards() {

		// Initialize the boards
		List<List<Board>> allBoards = new ArrayList<List<Board>>();
		int numWorlds = levelOrderer.getNumWorlds();

		// Create one inner list per world
		for (int world = 1; world <= numWorlds; world++) {

			// Add the world of boards
			List<Board> curWorldBoards = new ArrayList<Board>();

			// Get each board based on its position in the mapping
			int worldSize = levelOrderer.getWorldSize(world);
			for (int ordinalInWorld = 1; ordinalInWorld <= worldSize; ordinalInWorld++) {
				Board cur = levelLoader.getLevel(world, ordinalInWorld);
				initializeLasers(cur);
				curWorldBoards.add(cur);
			}
			allBoards.add(curWorldBoards);
		}

		return allBoards;
	}

	/**
	 * This is the primary game loop. It is called once per cycle. All other
	 * program execution occurs linearly off from this method.
	 */
	@Override
	public void render() {

		// Check for back pressed first
		inputHandler.checkBackPressed();

		// Handle the menu separately
		boolean wasMenuShowing = mainMenuShowing;
		if (mainMenuShowing && state != GameState.MENU_TO_LEVEL_TRANSITION){
			state = handleMainMenu(state);
		}

		// Handle the level otherwise
		else {
			boolean pushedButton = handleButtonPress();

			// Only check for game in this case
			if (!pushedButton) {

				// Get input from the user on the level
				GameState pastState = state;
				state = inputHandler.handleGameInput(b, state);

				//Do various actions depending on the state
				//Most actions occur while moving
				//Other states do nothing or increment counters
				switch (state){
					case DESTROYED:
						timeDead++;
						break;
					case INTRO:
						if(timeSpentOnIntro >= Constants.TIME_FOR_INTRO){
							state = GameState.IDLE;
						} else {
							timeSpentOnIntro++;
						}
					case WON:
						timeWon++;
						break;
					case LEVEL_TRANSITION:
						if(timeSpentOnTransition >= Constants.TIME_FOR_LEVEL_TRANSITION){
							moveToNextLevel();
						} else {
							timeSpentOnTransition++;
						}
						break;
					case MENU_TO_LEVEL_TRANSITION:
						if(timeSpentLeavingMenu >= Constants.TIME_FOR_MENU_TRANSITION){
							mainMenuShowing = false;
							state = GameState.INTRO;
						} else {
							timeSpentLeavingMenu++;
						}
						break;
					case MOVING:						
						//Just started to move from deciding
						if (pastState == GameState.DECIDING){
							moveCounter++;
							debug(moveCounter);

							//Prep the animations
							prepAnimationBeginning();
						}
						
						//We're at the start of a move
						if (currentAnimationState == AnimationState.NOTANIMATING){
							
							//In which case, precompute the move
							precomputeMove();
						}
						
						//Then, handle all animations of the move
						handleAnimations();
						break;
					case IDLE:
					case DECIDING:
					default:
						break;
				}
			}
		}

		// Draw the game or menu
		if (mainMenuShowing || wasMenuShowing){
			if(state == GameState.MENU_TO_LEVEL_TRANSITION || state == GameState.INTRO){
				dm.draw(b, currentWorld, currentOrdinalInWorld, true, (float)(timeSpentLeavingMenu) / Constants.TIME_FOR_MENU_TRANSITION);
				if(wasMenuShowing && !mainMenuShowing){
					timeSpentLeavingMenu = 0;
				}
			} else {
				dm.draw(b, currentWorld, currentOrdinalInWorld, false, 0);
			}
		} else if (state == GameState.LEVEL_TRANSITION) {
			float transPart = ((float) (timeSpentOnTransition))
					/ Constants.TIME_FOR_LEVEL_TRANSITION;
			transPart = transPart * -1 * Gdx.graphics.getWidth();
			dg.draw(b, state, currentAnimationState, currentWorld,
					currentOrdinalInWorld,
					menu.colorOfLevel(currentWorld, currentOrdinalInWorld),
					transPart, false);
			dg.draw(nextBoard, state, currentAnimationState, nextLvWorld,
					nextOrdinal, menu.colorOfLevel(nextLvWorld, nextOrdinal),
					transPart + Gdx.graphics.getWidth(), true);

		} else {
			dg.draw(b, state, currentAnimationState, currentWorld,
					currentOrdinalInWorld,
					menu.colorOfLevel(currentWorld, currentOrdinalInWorld), 0,
					false);
		}
	}

	/**
	 * Handles the main update loop for when the main menu is showing
	 */
	private GameState handleMainMenu(GameState state){
		
		if (state == GameState.MENU_TO_LEVEL_TRANSITION){
			return GameState.MENU_TO_LEVEL_TRANSITION;
		}
		
		//Check to see what was pressed on the menu
		int selected = inputHandler.handleMainMenuInput(menu);

		// Possibly reset logging if the user says to do so
		if (Constants.LOGGING && selected == -3) {
			logEnd();
			clearAllData();
			Logger.startNewSession();
		}

		// Exit to leave
		if (selected == -2) {
			if (Constants.LOGGING) {
				logEnd();
			}
			// TODO: Do things to check if the player wants to leave.
			System.exit(0);
		}

		// Picked a level
		if (selected == 0) {

			// Figure out which level was selected
			int selectedWorld = inputHandler.getMostRecentlySelectedWorld();
			int selectedOrdinalInWorld = inputHandler.getMostRecentlySelectedOrdinalInWorld();

			//Check that it's unlocked
			boolean unlocked = menu.isLevelUnlocked(selectedWorld, selectedOrdinalInWorld);
			if (unlocked){
				//Enter the level if it's unlocked
				//mainMenuShowing = false;

				// Only reset if different level
				if (selectedOrdinalInWorld != currentOrdinalInWorld
						|| selectedWorld != currentWorld) {

					// Log the change
					if (Constants.LOGGING) {
						if (currentWorld != -1) {
							logEnd();
						}
						Logger.enteredLevel(selectedWorld,
								selectedOrdinalInWorld);
					}
					//Change current and load the new level
					currentWorld = selectedWorld;
					currentOrdinalInWorld = selectedOrdinalInWorld;
					loadLevel(currentWorld, currentOrdinalInWorld);
					return GameState.MENU_TO_LEVEL_TRANSITION;
				} else {
					return GameState.MENU_TO_LEVEL_TRANSITION;
				}
			}
		}
		
		return GameState.IDLE;
	}

	/**
	 * Handles button presses in the regular game execution.
	 * 
	 * @return True if a button was pressed, so other input should be ignored
	 */
	private boolean handleButtonPress() {

		boolean pushedButton = false;

		// Make sure to not check for input if we're in the middle of inputing a
		// move or transitioning between levels
		if (state != GameState.DECIDING && state != GameState.LEVEL_TRANSITION && state != GameState.MENU_TO_LEVEL_TRANSITION) {

			// Get the button that was pressed
			ButtonPress button = inputHandler.checkForButtonPress(state,
					b.getTopYCoord());

			//Check if it's skip win
			if (state == GameState.WON && button != ButtonPress.NONE) {

				// Check to see if not through the animation yet
				int numStars = 1;
				if (GameEngine.getMoveCount() <= b.perfect) {
					numStars = 3;
				} else if (GameEngine.getMoveCount() <= b.par) {
					numStars = 2;
				}
				if (timeWon < (numStars + 2) * Constants.WON_ANIMATION_UNIT) {
					button = ButtonPress.SKIPWIN;
				}
			}

			//They pushed a button!
			if (button != ButtonPress.NONE){

				//Indicate as such
				pushedButton = true;

				// Determine what to do on the press
				switch (button) {
				case SKIPWIN:
					timeWon = Constants.WON_ANIMATION_UNIT * 10;
					break;
				case UNDO:
					// Move back and reset the board
					state = GameState.IDLE;
					moveCounter = Math.max(moveCounter - 1, 0);
					if (Constants.LOGGING) {
						undoTimes++;
					}
					b.resetPieces(boardStack.get(moveCounter));
					movingPiece = null;
					movePath.clear();
					prepAnimationBeginning();
					initializeLasers(b);
					break;
				case RESET:
					resetCurrentLevel();
					state = GameState.IDLE;
					if (Constants.LOGGING) {
						resetTimes++;
					}
					break;
				case MENU:
					// Reset the level if going to menu when destroyed
					if (state == GameState.DESTROYED || state == GameState.WON) {
						resetCurrentLevel();
					}
					//Always clean up certain things if we're animating
					if (currentAnimationState != AnimationState.NOTANIMATING){
						moveCounter--;
						b.resetPieces(boardStack.get(moveCounter));
						movingPiece = null;
						movePath.clear();
						prepAnimationBeginning();
						initializeLasers(b);
					}

					state = GameState.IDLE;

					mainMenuShowing = true;
					menu.scrollToLevel(currentWorld, currentOrdinalInWorld);
					break;
				case NEXT_LEVEL:
					//Make sure the next level is unlocked
					int nextLevelOrdinal = currentOrdinalInWorld + 1;
					int nextWorld = currentWorld;
					
					//Will never go to the bonus level
					if (nextLevelOrdinal >= levelOrderer.getWorldSize(currentWorld)){
						nextLevelOrdinal = 1;
						nextWorld++;
					}
					if (!menu.isLevelUnlocked(nextWorld, nextLevelOrdinal))
						break;
					
					// Guess we're sick of this level already...
					state = GameState.LEVEL_TRANSITION;
					timeSpentOnTransition = 0;
					nextOrdinal = nextLevelOrdinal;
					nextLvWorld = nextWorld;
					nextBoard = levelLoader.getLevel(nextLvWorld, nextOrdinal);
					initializeLasers(nextBoard);
					break;
				case INFO:
					// TODO
				default:
					break;
				}
			}
		}

		return pushedButton;
	}

	/**
	 * Moves to the next level. Should handle bonus and locked levels but
	 * doesn't.
	 */
	private void moveToNextLevel() {

		if (Constants.LOGGING) {
			logEnd();
		}

		// TODO: Handle bonus levels and locked levels

		// Increment level and possibly world
		currentOrdinalInWorld++;
		if (currentOrdinalInWorld >= levelOrderer.getWorldSize(currentWorld)) {
			currentOrdinalInWorld = 1;
			currentWorld++;
		}

		// Check that there are remaining levels
		if (currentWorld <= levelOrderer.getNumWorlds()) {
			loadLevel(currentWorld, currentOrdinalInWorld);

			if (Constants.LOGGING) {
				Logger.enteredLevel(currentWorld, currentOrdinalInWorld);
			}
		}
		// No levels remaining - go back to the main menu
		else {
			currentWorld--;
			currentOrdinalInWorld = levelOrderer.getWorldSize(currentWorld);
			menu.scrollToLevel(currentWorld, currentOrdinalInWorld);
			mainMenuShowing = true;
		}

		// Reset animations
		prepAnimationBeginning();
	}

	/**
	 * Precomputes a move. This is done right before a piece moves, remainder of
	 * the move can be handled as animation.
	 */
	private void precomputeMove() {
		// Move the piece
		movePiece();

		// Update the board state
		wasPieceDestroyed = updateBoardState();

		// Debugging checks
		debug("Was piece destroyed? " + wasPieceDestroyed);
		for (AnimationState as : animationStack)
			debug(as);

		// Update the animation frame
		currentAnimationState = animationStack.remove(0);
		totalTimeForThisAnimation = AnimationState
				.getTime(currentAnimationState);

		// Record where we got to
		futureBoard = b.encodePieces();

		// Put the piece back
		b.move(movingPiece, movePath.get(0));
		movingPiece.setColor(originalColor);
	}

	/**
	 * Handles all the changes that happen thanks to animations
	 */
	private void handleAnimations() {
		// Increment animation time!
		timeSpentOnThisAnimation++;

		// Check if we've gone completed the current animation
		if (timeSpentOnThisAnimation > totalTimeForThisAnimation) {
			timeSpentOnThisAnimation = 1;

			// More animations to go!
			if (!animationStack.isEmpty()) {
				moveToNextAnimation();
			}
			// Done animating - move on
			else {
				// Get the board where it should be now
				if (currentAnimationState != AnimationState.DESTRUCTION) {
					goBackToTheFuture();
				}

				// Reset the animation data
				prepAnimationBeginning();

				// No more left to move or destroyed
				if (movePath.size() == 1 || wasPieceDestroyed) {

					// So, finish the current move
					handleMoveFinish();
				}
			}
		}
	}

	/**
	 * Called once one animation is finished, but there are more animations left
	 * to go. Moves to the next animation. If that's destruction, also set the
	 * board to where it will be after destruction so the destroyed piece isn't
	 * drawn.
	 */
	private void moveToNextAnimation() {

		// Get the next animation state
		currentAnimationState = animationStack.remove(0);
		totalTimeForThisAnimation = AnimationState
				.getTime(currentAnimationState);

		// Check to see if that will be destruction, and update accordingly
		if (currentAnimationState == AnimationState.DESTRUCTION) {
			deaths++;
			goBackToTheFuture();
		}
	}

	/**
	 * Perform all required checks at the end of a complete move
	 */
	private void handleMoveFinish() {

		// At this point, nothing is moving anymore
		movingPiece = null;
		movePath.clear();

		// See which state to transition to. If something was destroyed,
		// transition there.
		if (wasPieceDestroyed) {
			state = GameState.DESTROYED;
		}
		// Otherwise, we're still in a playable state
		else {
			// Made a move, so go back to idle
			state = GameState.IDLE;

			// Push the move onto the stack
			boardStack.add(moveCounter, (b.encodePieces()));

			// Remove the future REDO stack, if it exists
			if (boardStack.size() >= moveCounter + 1) {
				List<Collection<Short>> newStack = new ArrayList<Collection<Short>>();
				for (int i = 0; i < moveCounter + 1; i++)
					newStack.add(boardStack.get(i));
				boardStack = newStack;
			}

			// Check to see if we've beaten the level
			if (b.isWon()) {

				// If so, indicate this fact
				state = GameState.WON;
				timeWon = 0;

				// Update the game progress based on how well we did
				int numStars = 1;
				if (moveCounter <= b.perfect) {
					numStars = 3;
				} else if (moveCounter <= b.par) {
					numStars = 2;
				}
				progress.setLevelScore(currentWorld, currentOrdinalInWorld,
						moveCounter, numStars);
			}
		}
	}

	/**
	 * Removes all user data. Useful for logging purposes.
	 */
	private void clearAllData() {
		currentWorld = -1;
		currentOrdinalInWorld = -1;
		progress.clearAllData();
	}

	/**
	 * Sets up all information for the beginning of an animation, or a reseting.
	 * Clears all relevant fields.
	 */
	private void prepAnimationBeginning() {
		timeDead = 0;
		animationStack.clear();
		currentAnimationState = AnimationState.NOTANIMATING;
		timeSpentOnThisAnimation = 0;
		piecesDestroyed.clear();
		laserRemoved = null;
		laserMovedAlong = null;
		lasersCreated.clear();
		if (movingPiece != null) {
			originalColor = movingPiece.getColor();
		} else {
			originalColor = Color.NONE;
		}
	}

	/**
	 * Called after animation has completed for a given move. Puts the board
	 * into the state that it's supposed to be at.
	 */
	private void goBackToTheFuture() {

		// Cleans out the current move
		movePath.remove(0);

		// Set the board back to the future
		b.resetPieces(futureBoard);

		// Moving piece is now on the next tile
		movingPiece = b.getPieceOnTile(movePath.get(0));

		// Remove destroyed pieces here
		List<Piece> newDestroyed = new ArrayList<Piece>();
		for (Piece p : piecesDestroyed) {
			// Either piece removed, or was moving piece
			if (!b.removePiece(p)) {
				b.removePiece(movingPiece);
				newDestroyed.add(movingPiece);
			} else {
				newDestroyed.add(p);
			}
		}
		piecesDestroyed = newDestroyed;

		// And set the lasers to where they should be now
		initializeLasers(b);
	}

	/**
	 * Resets the current level to its original state
	 */
	private void resetCurrentLevel() {
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

	/**
	 * Loads a level and handles initializations
	 */
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

	/**
	 * Moves a piece, and handles all appropriate changes
	 */
	public void movePiece() {

		// Remove previous lasers
		boolean laserRemovedFromPiece = removeLasersFromPiece(movingPiece,
				movePath.get(1));
		if (laserRemovedFromPiece) {
			animationStack.add(AnimationState.BREAKING);
		}

		// Tell the board to move the piece
		animationStack.add(AnimationState.MOVING);
		b.move(movingPiece, movePath.get(1));

	}

	/**
	 * Update the board state after the piece has been moves
	 */
	public boolean updateBoardState() {

		// Check for piece destroyed
		boolean anyPiecesDestroyed = false;
		if (!checkIfPieceDestroyed(movingPiece)) {
			// Get painted
			boolean piecePainted = paintPiece(movingPiece);
			if (piecePainted) {
				animationStack.add(AnimationState.PAINTING);
			}

			// Check for piece destroyed
			if (!checkIfPieceDestroyed(movingPiece)) {

				// Form new lasers and cause destruction
				List<Piece> destroyed = formLasersFromPieceAndDestroy(b,
						movingPiece, movePath.get(0), false);

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

	/**
	 * Put all lasers onto the current board
	 */
	private void initializeLasers(Board board) {
		board.lasers.clear();
		for (Piece p1 : board.getAllPieces()) {
			List<Piece> destroyed = formLasersFromPieceAndDestroy(board, p1,
					null, true);
			if (!destroyed.isEmpty()) {
				debug("Pieces destroyed in current state.");
			}
		}
	}

	/**
	 * Paint the piece, if it's on a painter
	 */
	public boolean paintPiece(Piece p) {
		// Is there a painter?
		Tile pieceTile = b.getTileAtBoardPosition(p.getXCoord(), p.getYCoord());

		// Paint the piece!
		if (pieceTile.hasPainter()
				&& pieceTile.getPainterColor() != p.getColor()) {
			p.setColor(pieceTile.getPainterColor());
			return true;
		}
		return false;
	}

	/**
	 * Remove all lasers connected to the current piece. Depends on where the
	 * piece will be moving.
	 * 
	 * @param p
	 *            The piece to remove lasers from
	 * @param nextTile
	 *            Where the piece will be moving, or null if not moving
	 * @return True if any laser visually removed; false otherwise
	 */
	public boolean removeLasersFromPiece(Piece p, Tile nextTile) {
		int xPos = p.getXCoord();
		int yPos = p.getYCoord();

		Laser possibleHorizLaser = null;
		Laser possibleVertLaser = null;

		for (Laser l : b.lasers) {
			// Check if connected to this piece
			if ((l.getXStart() == xPos && l.getYStart() == yPos)
					|| (l.getXFinish() == xPos && l.getYFinish() == yPos)) {
				// Laser is horizontal
				if (l.getXStart() != l.getXFinish()) {
					// Checking horizontal
					possibleHorizLaser = possibleHorizLaser == null ? l : null;
				}
				// Laser goes up
				else {
					possibleVertLaser = possibleVertLaser == null ? l : null;
				}

			}
		}

		// Figure out what happend to the lasers
		boolean horizMove = p.getXCoord() != nextTile.getXCoord();
		if (possibleHorizLaser != null) {
			if (horizMove)
				laserMovedAlong = possibleHorizLaser;
			else
				laserRemoved = possibleHorizLaser;
		}
		if (possibleVertLaser != null) {
			if (!horizMove)
				laserMovedAlong = possibleVertLaser;
			else
				laserRemoved = possibleVertLaser;
		}
		return (laserRemoved != null);
	}

	/**
	 * Forms lasers from a piece that has just moved, and destroy pieces.
	 * 
	 * @param board
	 *            The board being moved on
	 * @param p
	 *            The piece that just moved
	 * @param cameFrom
	 *            The tile that the piece moved from
	 * @param addLasers
	 *            Whether or not to add created lasers to the board. This allows
	 *            initialize lasers to call this method to set up lasers on the
	 *            board, while the future calling method won't actually put new
	 *            lasers on the board.
	 * @return A list of the pieces destroyed
	 */
	public List<Piece> formLasersFromPieceAndDestroy(Board board, Piece p,
			Tile cameFrom, boolean addLasers) {

		// Check whether moving horizontally or vertically
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
			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(
					xPos, yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(),
							p.getYCoord(), p.getColor());
					if (addLasers) {
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

			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(
					xPos, yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(),
							xPos, yPos, p.getColor());
					if (addLasers) {
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
			if (addLasers) {
				board.lasers.remove(new Laser(leftSameColor.getXCoord(),
						leftSameColor.getYCoord(), rightSameColor.getXCoord(),
						rightSameColor.getYCoord(), p.getColor()));
			}
		}

		// If it's still possible, it was formed
		if (!addLasers && possibleFormed != null
				&& (originalColor != p.getColor() || !horizontalMove)) {
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
			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(
					xPos, yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(),
							p.getYCoord(), p.getColor());
					if (addLasers) {
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

			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(
					xPos, yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = board.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(),
							xPos, yPos, p.getColor());
					if (addLasers) {
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
			if (addLasers) {
				board.lasers.remove(new Laser(botSameColor.getXCoord(),
						botSameColor.getYCoord(), topSameColor.getXCoord(),
						topSameColor.getYCoord(), p.getColor()));
			}
		}
		// If it's still possible, it was formed
		if (!addLasers && possibleFormed != null
				&& (originalColor != p.getColor() || horizontalMove)) {
			lasersCreated.add(possibleFormed);
		}
		possibleFormed = null;

		if (!lasersCreated.isEmpty() && !addLasers) {
			animationStack.add(AnimationState.FORMING);
		}

		return destroyed;
	}

	/**
	 * Checks if a piece is destroyed in its current position (has a laser of a
	 * different color going through it). Assumes no other pieces are in a
	 * position where they would be destroyed at present.
	 */
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

		// Only need to look right if there's a piece of a different color
		// on the left.
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

		// Only need to check down if there was a piece of a different color
		// above the current piece
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

	/**
	 * Gets the current move count. Used for drawing.
	 */
	public static int getMoveCount() {
		return moveCounter;
	}

	/**
	 * What to do when the screen is resized. Currently unimplemented.
	 */
	@Override
	public void resize(int width, int height) {
	}

	/**
	 * What the game does when paused. This is the implementation of the
	 * restoration protocol. Needs to be careful if the game is in a lost state
	 */
	@Override
	public void pause() {
		// Only save if there's something to save
		if (currentWorld != -1) {

			List<Collection<Short>> saveStack;
			// If the game is currently in a destroyed state, undo that
			if (state == GameState.DESTROYED) {
				moveCounter--;

				// Only take up to a portion
				saveStack = boardStack.subList(0, moveCounter + 1);

			} else {
				saveStack = boardStack;
			}

			// Store the level number, move counter, whether the menu is
			// showing,
			// and information about the boards in the stack
			String toSave = currentWorld + ";" + currentOrdinalInWorld + ";"
					+ moveCounter + ";" + mainMenuShowing + ";";
			for (Collection<Short> curBoard : saveStack) {
				for (Short s : curBoard) {
					toSave += s + "--";
				}
				toSave += ";";
			}
			debug("Writing " + toSave);
			tempFile.writeString(toSave, false);
		}
		// Otherwise, make sure we won't pick up incorrect data
		else {
			tempFile.delete();
		}
	}

	/**
	 * What to do when the game resumes after being paused. Mostly reset the
	 * game exactly back to where it was, except handle a "WON" case explicitly.
	 */
	@Override
	public void resume() {
		// Reinitialize the fonts
		dg.initFonts();
		dm.initFonts();

		// Check if there's data for to read
		if (!tempFile.exists())
			return;

		String fromTemp = tempFile.readString();
		debug("Read " + fromTemp);
		if (fromTemp != null) {
			String[] parts = fromTemp.split(";");

			// Get the level
			currentWorld = Integer.parseInt(parts[0]);
			currentOrdinalInWorld = Integer.parseInt(parts[1]);
			loadLevel(currentWorld, currentOrdinalInWorld);

			// Set up moves and menu
			moveCounter = Integer.parseInt(parts[2]);
			mainMenuShowing = Boolean.parseBoolean(parts[3]);

			// Set up the stack
			boardStack.clear();
			for (int i = 4; i < parts.length; i++) {
				List<Short> move = new ArrayList<Short>();
				String[] subParts = parts[i].split("--");
				for (String s : subParts) {
					move.add(Short.parseShort(s));
				}
				boardStack.add(move);
			}

			// Indicates we were in the middle of a move
			if (moveCounter >= boardStack.size())
				moveCounter--;
			b.resetPieces(boardStack.get(moveCounter));
			initializeLasers(b);

			// Handle having won on restarting level
			if (!mainMenuShowing && b.isWon()) {
				state = GameState.WON;
				timeWon = Constants.WON_ANIMATION_UNIT * 10;
			}
			menu.scrollToLevel(currentWorld, currentOrdinalInWorld);
		}
	}

	/**
	 * Getters for animation values
	 */
	public static int getTotalTicksForAnimation() {
		return totalTimeForThisAnimation;
	}

	public static int getTicksSpentOnAnimation() {
		return timeSpentOnThisAnimation;
	}

	public static Laser getBrokenLaser() {
		return laserRemoved;
	}

	public static List<Laser> getFormedLaser() {
		return lasersCreated;
	}

	public static Laser getLaserMovedAlong() {
		return laserMovedAlong;
	}

	public static float getIntroProgress() {
		return ((float) (timeSpentOnIntro)) / Constants.TIME_FOR_INTRO;
	}

	public static int getTimeDead() {
		return timeDead;
	}

	public static int getTimeBeforeDeathBeam() {
		return Constants.TIME_BEFORE_DEATH_MESSAGE;
	}

	public static int getTimeWon() {
		return timeWon;
	}

	public static int getWonAnimationUnit() {
		return Constants.WON_ANIMATION_UNIT;
	}

	public static List<Piece> getDestroyedPieces() {
		return piecesDestroyed;
	}

	/**
	 * Debugging method. Prints anything given to it.
	 * 
	 * @param s
	 *            Some value to be printed as debugging output.
	 */
	public static <T> void debug(T s) {
		if (!Constants.DEBUG_MODE) {
			return;
		}
		System.out.println(s);
	}

	/**
	 * Explicit logging method for what to do when moving on to a new level.
	 */
	private void logEnd() {
		if (currentWorld != -1) {
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
