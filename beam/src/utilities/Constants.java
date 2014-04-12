package utilities;

import com.badlogic.gdx.graphics.Color;



public class Constants {
	/**
	 * Whether the game is in debug mode (all printing should happen only
	 * in debug mode), and if logging is enabled. Both should be false in
	 * the final game version.
	 */
	public static final boolean DEBUG_MODE = false;
	public static final boolean UNLOCK_MODE = true;
	public static final boolean LOGGING = true;
	
	
	/**
	 * ANIMATION CONSTANTS
	 */
	public static final int TIME_TO_MOVE_PIECE = 8;
	public static final int TIME_TO_FORM_BEAM = 10;
	public static final int TIME_TO_BREAK_BEAM = 8;
	public static final int TIME_TO_DESTROY_PIECE = 60;
	public static final int TIME_TO_PAINT_PIECE = 20;
	
	public static final int TIME_FOR_INTRO = 300;
	public static final int TIME_BEFORE_DEATH_MESSAGE = 120;
	public static final int WON_ANIMATION_UNIT = 14;
	
	public static final int TIME_FOR_LEVEL_TRANSITION = 60;

	
	/**
	 * SCREEN SIZE CONSTANTS
	 */
	public static final float TOP_BAR_SIZE = 0.15f;
	public static final float BOT_BAR_SIZE = 0.09f;
	public static final float SIDE_EMPTY_SIZE = 0.02f;
	public static final float GAME_BUTTON_HEIGHT = 0.08f;
	public static final float NON_GAME_BUTTON_HEIGHT = 0.08f;
	
	/**
	 * GOAL PROGRESS CONSTANTS
	 */
	public static final float BEAM_GOAL_HEIGHT = 0.08f;
	public static final float BEAM_GOAL_WIDTH = 0.73f;
	public static final float TEXT_GOAL_HEIGHT = 0.12f;
	
	
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
	 * TODO: remove extra colorFromRGB and make the game not crash if they're there
	 */
	private static Color colorFromRGB(int r, int g, int b) {
		return new Color(r/255.0f, g/255.0f, b/255.0f,1);
	}
	public static final Color[] WORLD_COLORS = 
		{	colorFromRGB(75,125,204),
			colorFromRGB(121,224,224),
			colorFromRGB(34,233,38),
			colorFromRGB(240,128,10),
			colorFromRGB(240,22,22),
			colorFromRGB(212,60,204),
			colorFromRGB(0,0,0),
			colorFromRGB(0,0,0),
			colorFromRGB(0,0,0),
			colorFromRGB(0,0,0),
			colorFromRGB(0,0,0),
			colorFromRGB(0,0,0),
		 	};
	public final static Color BOARD_COLOR = new Color(.95f, .95f, .9f, .85f);
	public final static Color LINE_COLOR = new Color(.1f, .1f, .1f, 1);
}
