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

	private String fileString = "saveFile.xml";
	private FileHandle saveFile;

	/**
	 * Map from unique IDs to the appropriate field
	 */
	private Map<Integer, Integer> scores;
	private Map<Integer, Integer> stars;
	private Map<Integer, String> xmlTags;

	private int playMusic;
	private int playFX;

	private LevelOrderer levelOrderer;
	
	//Number of stars, per level, needed to unlock either the next world or the
	//bonus level for a given world
	private final int WORLD_UNLOCK_STARS = 2;
	private final int BONUS_UNLOCK_STARS = 3;

	public GameProgress(LevelOrderer levelOrderer){
		saveFile = Gdx.files.local(fileString);
		this.levelOrderer = levelOrderer;

		//Check if the file exists and create it if it doesn't
		if (!saveFile.exists()){
			saveFile.writeString("<sound music=1 fx=1/>\n", false);
		}

		init();
	}

	public void init(){
		//Keep track of progress on levels
		scores = new HashMap<Integer, Integer>();
		stars = new HashMap<Integer, Integer>();
		xmlTags = new HashMap<Integer, String>();

		load();
	}

	//Returns if the new score was better
	public boolean setLevelScore(int world, int ordinalInWorld, int moves, int levelStars){
		if (GameEngine.LOGGING){
			Logger.log(LogType.BEAT_LEVEL_MOVES, moves);
		}
		
		int uniqueId = levelOrderer.getUniqueId(world, ordinalInWorld);
		Integer pastMoves = scores.get(uniqueId);
		
		//See if we've beat for the first time or improved
		if (pastMoves == null|| moves < pastMoves){
			//Only store stars if better moves
			if (GameEngine.LOGGING){
				Logger.log(LogType.BEAT_LEVEL_STARS, levelStars);
			}
			scores.put(uniqueId, moves);
			stars.put(uniqueId, levelStars);
			setXmlTag(uniqueId);
			save();

			return true;
		}
		return false;
	}


	public void clearAllData() {
		saveFile.delete();
		saveFile.writeString("<sound music=1 fx=1/>\n", false);
		init();
	}
	
	//Returns the number of stars in the progress for the current world
	//Returns -1 if world is out of bounds
	public int getBaseWorldStars(int world){
		
		int numWorlds = levelOrderer.getNumWorlds();
		if (world >= 1 && world <= numWorlds){
			int numStars = 0;
			for (int ordinalInWorld = 1; ordinalInWorld < levelOrderer.getWorldSize(world); ordinalInWorld++){
				numStars += getLevelStars(world, ordinalInWorld);
			}
			return numStars;
 		}
		return -1;
	}
	
	//Check to see if a world is unlocked. Returns false if out of bounds
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
	
	public boolean isBonusLevelUnlocked(int world){
		int numWorlds = levelOrderer.getNumWorlds();
		if (world >= 1 && world <= numWorlds){
			int currentWorldProgress = getBaseWorldStars(world);
			int numConsideredLevels = levelOrderer.getWorldSize(world) - 1;
			return (currentWorldProgress >= numConsideredLevels * BONUS_UNLOCK_STARS);
		} else {
			return false;
		}
	}

	//Returns the number of moves done on this level, or 0 if not completed
	public int getLevelMoves(int world, int ordinalInWorld){
		int uniqueId = levelOrderer.getUniqueId(world, ordinalInWorld);
		Integer moves = scores.get(uniqueId);
		return (moves == null) ? 0 : moves;
	}

	//Returns the number of stars earned on this level, or 0 if not completed
	public int getLevelStars(int world, int ordinalInWorld){
		int uniqueId = levelOrderer.getUniqueId(world, ordinalInWorld);
		Integer starCount = stars.get(uniqueId);
		return (starCount == null) ? 0 : starCount;
	}

	public void setMusic(boolean isPlaying){
		playMusic = isPlaying? 1:0;
	}

	public boolean isMusicPlaying(){
		return playMusic == 1;
	}

	public void setFX(boolean isPlaying){
		playFX = isPlaying? 1:0;
	}

	public boolean isSoundPlaying(){
		return playFX == 1;
	}

	private void save(){
		String toSave = "";
		for (String s : xmlTags.values()){
			toSave += s;
		}
		toSave += "<sound music=" + playMusic + " fx=" + playFX + "/>";
		saveFile.writeString(toSave, false);
	}

	private void load(){
		String toMatch = saveFile.readString();
		Pattern pat = Pattern.compile("<level\\s*id=(\\d+)\\s*score=(\\d+)\\s*stars=(\\d+)\\s*/>");
		Matcher mat = pat.matcher(toMatch.trim());

		while (mat.find()){
			int uniqueId = Integer.parseInt(mat.group(1));
			scores.put(uniqueId, Integer.parseInt(mat.group(2)));
			stars.put(uniqueId, Integer.parseInt(mat.group(3)));
			setXmlTag(uniqueId);
		}

		pat = Pattern.compile("<sound\\s*music=(\\d+)\\s*fx=(\\d+)/>");
		mat = pat.matcher(toMatch);
		mat.find();
		playMusic = Integer.parseInt(mat.group(1));
		playFX = Integer.parseInt(mat.group(2));
	}

	private void setXmlTag(int uniqueId){
		xmlTags.put(uniqueId, "<level id=" + uniqueId + 
				" score=" + scores.get(uniqueId) + 
				" stars=" + stars.get(uniqueId) +" />\n");
	}
}
