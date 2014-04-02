package model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import controller.GameEngine;
import controller.LevelOrderer;
import controller.Logger;
import controller.Logger.LogType;

public class GameProgress {

	/**
	 * References to the name of the file and the handle of the file
	 */
	private String fileString = "saveFile.xml";
	private FileHandle saveFile;

	/**
	 * Map from unique IDs to the appropriate field
	 */
	private Map<Integer, Integer> scores;
	private Map<Integer, Integer> stars;
	private Map<Integer, String> xmlTags;

	/**
	 * Whether the user has set to have music and sound effects playing
	 */
	private int playMusic;
	private int playFX;

	/**
	 * Needs a reference to the level orderer to be able to convert from
	 * a given world and level ordinal to the level's unique id
	 */
	private LevelOrderer levelOrderer;
	
	/**
	 * Number of stars, per level, needed to unlock either the next world or the
	 * bonus level for a given world
	 */
	private final int WORLD_UNLOCK_STARS = 2;
	private final int BONUS_UNLOCK_STARS = 3;

	/**
	 * Constructs the game progress model object. Keeps a reference to the
	 * level orderer to be able to convert to unique ids.
	 * 
	 * @param levelOrderer
	 * 			Reference to the orderer
	 */
	public GameProgress(LevelOrderer levelOrderer){
		saveFile = Gdx.files.local(fileString);
		this.levelOrderer = levelOrderer;

		//Check if the file exists and create it if it doesn't
		if (!saveFile.exists()){
			saveFile.writeString("<sound music=1 fx=1/>\n", false);
		}

		//Initialize all the various fields
		init();
	}

	/**
	 * Initializes the various fields, and loads the current user data
	 */
	private void init(){
		//Keep track of progress on levels
		scores = new HashMap<Integer, Integer>();
		stars = new HashMap<Integer, Integer>();
		xmlTags = new HashMap<Integer, String>();

		load();
	}

	/**
	 * Sets the score on a given level. This includes both the moves and the stars earned.
	 * 
	 * @param world
	 * 			World of the level to set
	 * @param ordinalInWorld
	 * 			Ordinal of the level in that world
	 * @param moves
	 * 			How many moves it took the user
	 * @param levelStars
	 * 			The number of stars earned
	 * @return
	 * 			True if the new score was better; false otherwise
	 */
	public boolean setLevelScore(int world, int ordinalInWorld, int moves, int levelStars){
		//If logging, note that the level was beaten
		if (GameEngine.LOGGING){
			Logger.log(LogType.BEAT_LEVEL_MOVES, moves);
		}
		
		//Get the past moves on this level
		int uniqueId = levelOrderer.getUniqueId(world, ordinalInWorld);
		Integer pastMoves = scores.get(uniqueId);
		
		//See if we've beat for the first time or improved
		if (pastMoves == null|| moves < pastMoves){
			
			//Only store stars if better moves
			if (GameEngine.LOGGING){
				Logger.log(LogType.BEAT_LEVEL_STARS, levelStars);
			}
			
			//Update because better
			scores.put(uniqueId, moves);
			stars.put(uniqueId, levelStars);
			setXmlTag(uniqueId);
			
			//Save to file
			save();

			return true;
		}
		return false;
	}


	/**
	 * Clears all user data. Should ONLY be called for reseting logging.
	 */
	public void clearAllData() {
		saveFile.delete();
		saveFile.writeString("<sound music=1 fx=1/>\n", false);
		init();
	}
	
	/**
	 * Returns the stars earned within a world, ignoring the bonus level.
	 * Used to determine when the next world or bonus level has been unlocked.
	 * 
	 * @param world
	 * 			The world to get the stars for
	 * @return 
	 * 			The number of stars earned in world, ignoring the bonus
	 * 			level, or -1 if world is out of bounds
	 */
	public int getBaseWorldStars(int world){
		
		//Check to make sure world is in bounds
		int numWorlds = levelOrderer.getNumWorlds();
		if (world >= 1 && world <= numWorlds){
			
			//Count stars not including bonus
			int numStars = 0;
			for (int ordinalInWorld = 1; ordinalInWorld < levelOrderer.getWorldSize(world); ordinalInWorld++){
				numStars += getLevelStars(world, ordinalInWorld);
			}
			return numStars;
 		}
		return -1;
	}
	
	/**
	 * Check to see if a world is unlocked
	 * 
	 * @param world
	 * 			The world to do the check on
	 * @return
	 * 			True if world in bounds and unlocked, false otherwise
	 */
	public boolean isWorldUnlocked(int world){
		//World one is always unlocked
		if (world == 1){
			return true;
		} else {
			int numWorlds = levelOrderer.getNumWorlds();
			if (world >= 2 && world <= numWorlds){
				
				//Future world needs to average WORLD_UNLOCK_STARS
				int previousWorldProgress = getBaseWorldStars(world - 1);
				
				int numConsideredLevels = levelOrderer.getWorldSize(world - 1) - 1;
				int required = numConsideredLevels * WORLD_UNLOCK_STARS;
				boolean averageRequired = (previousWorldProgress >= required);
				
				//At least the average
				if (averageRequired){
					
					//And have completed every level
					for (int ordinalInWorld = 1; ordinalInWorld <= numConsideredLevels; ordinalInWorld++){
						if (getLevelMoves(world - 1, ordinalInWorld) == 0){
							return false;
						}	
					}
					return true;
				}
			} 
		}
		return false;
	}
	
	/**
	 * Check to see whether the bonus level of a given world is unlocked
	 * 
	 * @param world
	 * 			The world to check
	 * @return
	 * 			True if world in bounds and bonus level unlocked for that world
	 */
	public boolean isBonusLevelUnlocked(int world){
		
		//Ensure world is in bounds
		int numWorlds = levelOrderer.getNumWorlds();
		if (world >= 1 && world <= numWorlds){
			
			//Check the number of stars achieved for that world
			int currentWorldProgress = getBaseWorldStars(world);
			int numConsideredLevels = levelOrderer.getWorldSize(world) - 1;
			return (currentWorldProgress >= numConsideredLevels * BONUS_UNLOCK_STARS);
		} else {
			return false;
		}
	}

	/**
	 * Returns the number of moves done on this level, or 0 if not completed
	 */
	public int getLevelMoves(int world, int ordinalInWorld){
		int uniqueId = levelOrderer.getUniqueId(world, ordinalInWorld);
		Integer moves = scores.get(uniqueId);
		return (moves == null) ? 0 : moves;
	}

	/**
	 * Returns the number of stars earned on this level, or 0 if not completed
	 */
	public int getLevelStars(int world, int ordinalInWorld){
		int uniqueId = levelOrderer.getUniqueId(world, ordinalInWorld);
		Integer starCount = stars.get(uniqueId);
		return (starCount == null) ? 0 : starCount;
	}

	/**
	 * Set whether the user wants music playing or not
	 */
	public void setMusic(boolean isPlaying){
		playMusic = isPlaying? 1:0;
	}

	/**
	 * Get if music is playing or not
	 */
	public boolean isMusicPlaying(){
		return playMusic == 1;
	}

	/**
	 * Set whether the user wants sound effects or not
	 */
	public void setFX(boolean isPlaying){
		playFX = isPlaying? 1:0;
	}

	/**
	 * Get whether sound effects should be playing
	 */
	public boolean isSoundPlaying(){
		return playFX == 1;
	}

	/**
	 * Save all the current data to file. Overwrites the past save file with all
	 * the current save data.
	 */
	private void save(){
		String toSave = "";
		for (String s : xmlTags.values()){
			toSave += s;
		}
		toSave += "<sound music=" + playMusic + " fx=" + playFX + "/>";
		saveFile.writeString(toSave, false);
	}

	/**
	 * Loads all the data from the current save file into the progress object
	 */
	private void load(){
		
		//Parse the file using regex to find data on each of the levels
		String toMatch = saveFile.readString();
		Pattern pat = Pattern.compile("<level\\s*id=(\\d+)\\s*score=(\\d+)\\s*stars=(\\d+)\\s*/>");
		Matcher mat = pat.matcher(toMatch.trim());

		while (mat.find()){
			int uniqueId = Integer.parseInt(mat.group(1));
			scores.put(uniqueId, Integer.parseInt(mat.group(2)));
			stars.put(uniqueId, Integer.parseInt(mat.group(3)));
			setXmlTag(uniqueId);
		}
		
		//Also parse for whether the user has sound effects and music playing
		pat = Pattern.compile("<sound\\s*music=(\\d+)\\s*fx=(\\d+)/>");
		mat = pat.matcher(toMatch);
		mat.find();
		playMusic = Integer.parseInt(mat.group(1));
		playFX = Integer.parseInt(mat.group(2));
	}

	/**
	 * Sets the xml tag for a given unique id. This allows for a simple method of
	 * saving the file, by simply going through all set xml tags.
	 * 
	 * @param uniqueId
	 * 			The unique id of the level to save to.
	 */
	private void setXmlTag(int uniqueId){
		xmlTags.put(uniqueId, "<level id=" + uniqueId + 
				" score=" + scores.get(uniqueId) + 
				" stars=" + stars.get(uniqueId) +" />\n");
	}
}
