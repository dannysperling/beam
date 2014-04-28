package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import utilities.AssetInitializer;
import utilities.Constants;
import view.DrawGame;
import view.DrawLoading;
import view.DrawMenu;

import model.Board;
import model.GameProgress;
import model.Laser;
import model.Menu;
import model.Piece;
import model.Tile;
import model.Tutorial;

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
	
	/**
	 * The current tutorial, if there is one, or null if there isn't
	 */
	private static Tutorial tutorial;
	
	private DrawGame dg;
	private InputHandler inputHandler;
	private LevelLoader levelLoader;
	private GameProgress progress;
	private LevelOrderer levelOrderer;
	private TutorialLoader tutorialLoader;
	private Menu menu;
	private DrawMenu dm;
	private DrawLoading dl;

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
	private static int timeSpentLeavingLevel = 0;
	private static int timeDead = 0;
	private static int timeWon = 0;
	
	public static int timeToStopTutorial = Integer.MAX_VALUE;
	public static int timeSpentOnTutorial = 0;

	public static int timeToStopInfo = Integer.MAX_VALUE;
	public static int timeSpentOnInfo = 0;

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
		IDLE, DECIDING, MOVING, DESTROYED, WON, INTRO, TUTORIAL, INFO, LEVEL_TRANSITION, MENU_TO_LEVEL_TRANSITION, LEVEL_TO_MENU_TRANSITION
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
		UNDO, RESET, MENU, INFO, TUTORIAL, NEXT_LEVEL, SKIPWIN, NONE
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
		
		AssetInitializer.initialize();

		// Use GDX for both the orderer and the loader
		levelOrderer = new LevelOrderer("data/levels/levelOrder.txt", true);
		levelLoader = new LevelLoader("data/levels/levels.xml", levelOrderer,
				true);
		progress = new GameProgress(levelOrderer);
		
		//Set up tutorials
		tutorialLoader = new TutorialLoader("data/tutorials/tutorialDescriptions.txt", levelOrderer);

		// Create the menu
		menu = new Menu(levelOrderer.getWorldSizes(), progress);

		// Set up input handling
		inputHandler = new InputHandler();
		Gdx.input.setCatchBackKey(true);

		// Set up restoration
		tempFile = Gdx.files.local(tempData);
		
		//Set up the loading drawer
		dl = new DrawLoading();

		// Set up logging, if applicable
		if (Constants.LOGGING)
			Logger.initialize(levelOrderer.getMapping());
	}

	/**
	 * Called at the end of program execution to dispose of drawing objects
	 */
	@Override
	public void dispose() {
		//For now, avoid disposing
		/*dg.dispose();
		dm.dispose();*/
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
	
	private boolean finishedLoading = false;
	private int timeLoading = 0;
	
	/**
	 * Initializes the various drawing screens. Needs to wait until after the assets are loaded
	 */
	private void initializeDrawing(){
		
		// Create the game drawing
		dg = new DrawGame(progress);
		dg.initFonts();
		
		// Create the menu drawer
		List<List<Board>> allBoards = initializeBoards();
		dm = new DrawMenu(menu, dg, allBoards);
		dm.initFonts();
	}

	/**
	 * This is the primary game loop. It is called once per cycle. All other
	 * program execution occurs linearly off from this method.
	 */
	@Override
	public void render() {
		
		//Start by loading
		if (!finishedLoading){
			
			//Check if we're finished
			if (AssetInitializer.isFinished() && timeLoading == Constants.LOAD_FADE_TIME){
				initializeDrawing();
				finishedLoading = true;
			}
			
			dl.draw(timeLoading / (float) Constants.LOAD_FADE_TIME);
			timeLoading = Math.min(timeLoading+1, Constants.LOAD_FADE_TIME);
			//Always return from here if not loaded previously
			return;
		} 
		//Fade out the loading
		else if (timeLoading > 0){
			timeLoading--;
			dl.draw(timeLoading / (float) Constants.LOAD_FADE_TIME);
			return;
		}

			
		//Otherwise, game loop!
			
		// Check for back pressed first
		inputHandler.checkBackPressed();

		// Handle the menu separately
		boolean wasMenuShowing = mainMenuShowing;
		if (mainMenuShowing && state != GameState.MENU_TO_LEVEL_TRANSITION && state != GameState.LEVEL_TO_MENU_TRANSITION){
			timeSpentLeavingLevel = 0;
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
						break;
					case TUTORIAL:
						if(timeSpentOnTutorial >= timeToStopTutorial){
							state = GameState.INTRO;
							timeSpentOnIntro = 0;
							timeSpentOnTutorial = 0;
							timeToStopTutorial = Integer.MAX_VALUE;
						} else {
							timeSpentOnTutorial++;
						}
						break;
					case INFO:
						if(timeSpentOnInfo >= timeToStopInfo){
							state = GameState.IDLE;
							timeSpentOnInfo = 0;
							timeToStopInfo = Integer.MAX_VALUE;
						} else {
							timeSpentOnInfo++;
						}
						break;	
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
							if(tutorial != null){
								state = GameState.TUTORIAL;
							} else {
								state = GameState.INTRO;
							}
						} else {
							timeSpentLeavingMenu++;
						}
						break;
					case LEVEL_TO_MENU_TRANSITION:
						if(timeSpentLeavingLevel >= Constants.TIME_FOR_MENU_TRANSITION){
							state = GameState.IDLE;
						} else {
							timeSpentLeavingLevel++;
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
			if(state == GameState.MENU_TO_LEVEL_TRANSITION || state == GameState.INTRO || state == GameState.TUTORIAL){
				dm.draw(b, currentWorld, currentOrdinalInWorld, true, (float)(timeSpentLeavingMenu) / Constants.TIME_FOR_MENU_TRANSITION);
				if(wasMenuShowing && !mainMenuShowing){
					timeSpentLeavingMenu = 0;
				}
			} else if(state == GameState.LEVEL_TO_MENU_TRANSITION){
				dm.draw(b, currentWorld, currentOrdinalInWorld, true, 1 - (((float)(timeSpentLeavingLevel)) / Constants.TIME_FOR_MENU_TRANSITION));
			}else {
				dm.draw(b, currentWorld, currentOrdinalInWorld, false, 0);
			}
		} else if (state == GameState.LEVEL_TRANSITION) {
			float transPart = ((float) (timeSpentOnTransition))
					/ Constants.TIME_FOR_LEVEL_TRANSITION;
			float totalTransPart = transPart * -1 * Gdx.graphics.getWidth();
			com.badlogic.gdx.graphics.Color a = menu.colorOfLevel(currentWorld, currentOrdinalInWorld);
			com.badlogic.gdx.graphics.Color cb = menu.colorOfLevel(nextLvWorld, nextOrdinal);
			com.badlogic.gdx.graphics.Color mixed = new com.badlogic.gdx.graphics.Color(a.r + ((cb.r - a.r) * transPart), 
					a.g + ((cb.g - a.g) * transPart), a.b + ((cb.b - a.b) * transPart), a.a + ((cb.a - a.a) * transPart));
			
			boolean isLast = menu.isLastLevelInWorld(currentWorld, currentOrdinalInWorld);
			boolean isNextLocked = !menu.isNextLevelUnlocked(currentWorld, currentOrdinalInWorld);
			dg.draw(b, state, currentAnimationState, currentWorld,
					currentOrdinalInWorld, mixed, totalTransPart, false, isLast, isNextLocked, progress);
			
			isLast = menu.isLastLevelInWorld(nextLvWorld, nextOrdinal);
			isNextLocked = !menu.isNextLevelUnlocked(nextLvWorld, nextOrdinal);
			dg.draw(nextBoard, state, currentAnimationState, nextLvWorld,
					nextOrdinal, menu.colorOfLevel(nextLvWorld, nextOrdinal),
					totalTransPart + Gdx.graphics.getWidth(), true, isLast, isNextLocked, progress);

		} else {
			boolean isLast = menu.isLastLevelInWorld(currentWorld, currentOrdinalInWorld);
			boolean isNextLocked = !menu.isNextLevelUnlocked(currentWorld, currentOrdinalInWorld);
			dg.draw(b, state, currentAnimationState, currentWorld,
					currentOrdinalInWorld,
					menu.colorOfLevel(currentWorld, currentOrdinalInWorld), 0,
					false, isLast, isNextLocked, progress);
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
		if (state != GameState.DECIDING && state != GameState.LEVEL_TRANSITION && state != GameState.MENU_TO_LEVEL_TRANSITION && state != GameState.LEVEL_TO_MENU_TRANSITION) {

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
				if (timeWon < (numStars + 3) * Constants.WON_ANIMATION_UNIT) {
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
					
					// Remove the move just backtracked from
					List<Collection<Short>> newStack = new ArrayList<Collection<Short>>();
					for (int i = 0; i <= moveCounter; i++)
						newStack.add(boardStack.get(i));
					boardStack = newStack;

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

					state = GameState.LEVEL_TO_MENU_TRANSITION;
					dm.shiftBoardNew = true;
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
					state = GameState.INFO;
					break;
				case TUTORIAL:
					state = GameState.TUTORIAL;
					break;
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

		// Increment level and possibly world - won't move to bonus
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
				if (currentAnimationState != AnimationState.DESTRUCTION)
					goBackToTheFuture();

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
			
			//Push move onto the stack
			boardStack.add(moveCounter, (b.encodePieces()));
		}
		// Otherwise, we're still in a playable state
		else {
			// Made a move, so go back to idle
			state = GameState.IDLE;

			// Push the move onto the stack
			boardStack.add(moveCounter, (b.encodePieces()));

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
		
		// Load the tutorial
		tutorial = tutorialLoader.getTutorial(world, ordinalInWorld);

		// Clean out all the inits
		movingPiece = null;
		movePath.clear();

		// Clear the board stack
		boardStack.clear();
		boardStack.add(b.encodePieces());

		// Set up the state and move counter
		
		if(tutorial != null){
			state = GameState.TUTORIAL;
			timeSpentOnTutorial = 0;
		} else {
			state = GameState.INTRO;
			timeSpentOnIntro = 0;
		}
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
			
			List<Collection<Short>> saveStack = boardStack;
			
			// Store the current state, level number, move counter, whether the menu is
			// showing, and information about the boards in the stack
			String toSave = state + ";" + currentWorld + ";" + currentOrdinalInWorld + ";"
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
			currentWorld = Integer.parseInt(parts[1]);
			currentOrdinalInWorld = Integer.parseInt(parts[2]);
			loadLevel(currentWorld, currentOrdinalInWorld);
			
			//Load the state
			state = GameState.valueOf(parts[0]);

			// Set up moves and menu
			moveCounter = Integer.parseInt(parts[3]);
			mainMenuShowing = Boolean.parseBoolean(parts[4]);

			// Set up the stack
			boardStack.clear();
			for (int i = 5; i < parts.length; i++) {
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
			
			//Figure out the state
			if (state != GameState.WON && state != GameState.DESTROYED){
				state = GameState.INTRO;
			}
			if (state == GameState.WON){
				timeWon = Constants.WON_ANIMATION_UNIT * 10;
			} else if (state == GameState.DESTROYED){
				timeDead = Constants.TIME_BEFORE_DEATH_MESSAGE;
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
	
	public static Tutorial getTutorial(){
		return tutorial;
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
