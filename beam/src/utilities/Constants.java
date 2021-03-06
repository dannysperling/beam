package utilities;

import com.badlogic.gdx.graphics.Color;

import controller.GameEngine;



public class Constants {
	/**
	 * Whether the game is in debug mode (all printing should happen only
	 * in debug mode), and if logging is enabled. Both should be false in
	 * the final game version.
	 */
	public static final boolean DEBUG_MODE = false;
	public static boolean UNLOCK_MODE = false;
	public static final boolean LOGGING = false;
	
	
	/**
	 * LOADING CONSTANTS
	 */
	public static final int LOAD_FADE_TIME = 60;
	public static final int LOAD_SCREEN_TIME = 240;
	public static final int LOAD_TEXT_FADE_TIME = 30;
	public static final int TRANS_TO_MENU_TIME = 30;
	
	
	/**
	 * ANIMATION CONSTANTS
	 */
	public static final int TIME_TO_MOVE_PIECE = 8;
	public static final int TIME_TO_FORM_BEAM = 10;
	public static final int TIME_TO_BREAK_BEAM = 8;
	public static final int TIME_TO_DESTROY_PIECE = 60;
	public static final int TIME_TO_PAINT_PIECE = 20;
	
	public static final int TIME_FOR_INTRO = 150;
	public static final int TIME_BEFORE_DEATH_MESSAGE = 56;
	public static final int WON_ANIMATION_UNIT = 20;
	
	public static final int TIME_FOR_LEVEL_TRANSITION = 50;
	public static final int TRANSITION_DELAY = 20; 
	public static final int TIME_FOR_MENU_TRANSITION = 25;
	
	public static final int WON_DELAY = 15; 
	
	public static final int DESTROY_COLS = 7;
	public static final int DESTROY_ROWS = 8;
	
	
	/**
	 * TUTORIAL CONSTANTS
	 */
	public static final float TUTORIAL_V_BREAK = 0.05f;
	public static final int TUTORIAL_IN_TIME = 30;
	public static final float TUTORIAL_TICKS_PER_FRAME = 45;

	
	/**
	 * SCREEN SIZE CONSTANTS
	 */
	public static final float TOP_BAR_SIZE = 0.12f;
	public static final float BOT_BAR_SIZE = 0.03f;//0.09f;
	public static final float SIDE_EMPTY_SIZE = 0.03f;
	public static final float GAME_BUTTON_HEIGHT = 0.08f;
	public static final float NON_GAME_BUTTON_HEIGHT = 0.08f;
	public static final float POPUP_WIDTH_BOUNDARY = 0.05f;
	
	/**
	 * GOAL PROGRESS CONSTANTS
	 */
	public static final float BEAM_GOAL_HEIGHT = 0.08f;
	public static final float BEAM_GOAL_WIDTH = 0.8f;
	public static final float TEXT_GOAL_HEIGHT = 0.12f;
	
	/**
	 * UNLOCK CONSTANTS
	 */
	/**
	 * Number of stars, per level, needed to unlock either the next world or the
	 * bonus level for a given world
	 */
	public static final int WORLD_UNLOCK_STARS = 2;
	public static final int BONUS_UNLOCK_STARS = 3;
	
	/**
	 * FONT CONSTANTS
	 */
	public static final String FONT_PATH = "data/fonts/weblysleekuil.ttf";
	
	
	/**
	 * SCREEN SCROLLING CONSTANTS
	 */
	public static final int MAX_DIFF_CLICK = 15;				//How far finger move before considered click
	public static final float MOMENTUM_DROP_OFF = 0.10f;		//How quickly momentum drops off
	public static final int MIN_MOMENTUM = 2;					//Min momentum before set to zero
	
	public static final int VERT_MOVE_BOUNDS = 20;				//How far to move vertically before scrolling
	public static final int HORIZ_MOVE_BOUNDS = 15;				//How far to move horizontally before scrolling

	public static final float PERCENT_OFF_SCROLL = 0.2f;		//How far off screen you can scroll by percentage
	public static final float RESCROLL_BOUNCE = 0.01f;			//How quickly the screen rebounds in scrolling
	
	
	/**
	 * COLOR CONSTANTS
	 */
	public static Color colorFromRGB(int r, int g, int b) {
		return new Color(r/255.0f, g/255.0f, b/255.0f,1);
	}
	public static Color getWorldColorCopy(int worldIndex) {
		// out of bounds? do black
		if (worldIndex < 0 || worldIndex >= WORLD_COLORS.length) {
			return Color.BLACK.cpy();
		}
		return WORLD_COLORS[worldIndex].cpy();
	}
	private static final Color[] WORLD_COLORS = {
		colorFromRGB(59,64,139),
	    colorFromRGB(79, 44, 99),
		colorFromRGB(91,145,182),
		colorFromRGB(103,31,68),
		colorFromRGB(66,123,62),
		colorFromRGB(50,138,144),
		colorFromRGB(141,42,42),
		colorFromRGB(169,94,54),
		colorFromRGB(150,51,26),
		colorFromRGB(51,51,6),
	};
	
	public static Color translateColor(GameEngine.Color c) {
		switch (c) {
		case ORANGE:
			return new Color(240 / 255f, 150 /255f, 0 / 255f, 1);
		case BLUE:
			return new Color(13 / 255f, 176 / 255f, 230 / 255f, 1);
		case PURPLE:
			return new Color(147 / 255f, 29 / 255f, 186 / 255f, 1);
		case GREEN:
			return new Color(10 / 255.0f, 220 / 255.0f, 10 / 255.0f, 1);
		case BLACK:
			return new Color(0, 0, 0, 1);
		default:
			return new Color(0, 0, 0, 0);
		}
	}
	
	public static Color translateColorDark(GameEngine.Color c) {
		switch (c) {
		case ORANGE:
			return new Color(163 / 255f, 100 /255f, 0 / 255f, 1);
		case BLUE:
			return new Color(9 / 255f, 119 / 255f, 153 / 255f, 1);
		case PURPLE:
			return new Color(84 / 255f, 17 / 255f, 107 / 255f, 1);
		case GREEN:
			return new Color(255 / 255.0f, 150 / 255.0f, 20 / 255.0f, 1);
		case BLACK:
			return new Color(.6f, 0, .6f, 1);
		default:
			return new Color(0, 0, 0, 0);
		}
	}
	
	public static Color translateColorLight(GameEngine.Color c) {
		switch (c) {
		case ORANGE:
			return new Color(255 / 255f, 195 /255f, 99 / 255f, 1);
		case BLUE:
			return new Color(122 / 255f, 224 / 255f, 255 / 255f, 1);
		case PURPLE:
			return new Color(205 / 255f, 96 / 255f, 242 / 255f, 1);
		case GREEN:
			return new Color(255 / 255.0f, 150 / 255.0f, 20 / 255.0f, 1);
		case BLACK:
			return new Color(.6f, 0, .6f, 1);
		default:
			return new Color(0, 0, 0, 0);
		}
	}
	
	public final static Color BOARD_COLOR = new Color(.95f, .95f, .9f, 1f);
	public final static Color LINE_COLOR = new Color(.1f, .1f, .1f, 1);
	public static final float START_COLOR_MUL = 1.1f;
	public static final float END_COLOR_MUL = .75f;
	public static final Color LOCK_COLOR = colorFromRGB(202,202,192);// = gilded grey
	public static final Color LOCKED_WORLD_OVERLAY = new Color (0,0,0,0.85f);
	public static final Color LOCKED_LEVEL_NUMBER_COLOR = new Color(.95f, .95f, .9f, .25f);
	public static final Color GLASS_COLOR = new Color(.18f,.18f,.18f,.7f);
	public static final int PAINTER_MODE = 0;
	public static final Color[] painterColor = {null,new Color(.6f, .1f, .1f, 1),new Color(.1f, .1f, .6f, 1),new Color(.1f, .6f, .1f, 1)};
	public static final int SHADOW_DISTANCE = 8;
	public static final Color SHADOW_COLOR = new Color(.1f,.1f,.1f,0.25f);
	public static int GLASS_STYLE = 2;
}
