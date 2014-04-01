package controller;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import controller.Logger.LogType;

public class GameProgress {

	private String fileString = "saveFile.xml";
	private FileHandle saveFile;

	private int[] scores;
	private int[] stars;
	private int[] uniqueIds;
	private String[] xmlTags;

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
		int numLevels = levelOrderer.getNumLevels();
		scores = new int[numLevels];
		stars = new int[numLevels];
		xmlTags = new String[numLevels];
		uniqueIds = new int[numLevels];
		for (int i = 0; i < numLevels; i++){
			scores[i] = 0;
			stars[i] = 0;
			xmlTags[i] = "";
			uniqueIds[i] = -1;
		}

		load();
	}

	//Returns if the new score was better
	public boolean setLevelScore(int index, int moves, int levelStars){
		if (GameEngine.LOGGING){
			Logger.log(LogType.BEAT_LEVEL_MOVES, moves);
		}
		if (scores[index] == 0 || moves < scores[index]){
			//Only store stars if better moves
			if (GameEngine.LOGGING){
				Logger.log(LogType.BEAT_LEVEL_STARS, levelStars);
			}
			uniqueIds[index] = levelOrderer.getUniqueId(index);
			scores[index] = moves;
			stars[index] = levelStars;
			setXmlTag(index);
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
		
		List<Integer> worldSizes = levelOrderer.getWorldSizes();
		if (world >= 0 && world < worldSizes.size()){
			List<Integer> worldStartIndices = levelOrderer.getWorldStartIndices();
			int numStars = 0;
			int startIndex = worldStartIndices.get(world);
			for (int i = 0; i < worldSizes.get(world) - 1; i++){
				numStars += getLevelStars(startIndex + i);
			}
			return numStars;
 		}
		return -1;
	}
	
	//Check to see if a world is unlocked. Returns false if out of bounds
	public boolean isWorldUnlocked(int world){
		//World zero is always unlocked
		if (world == 0){
			return true;
		} else {
			List<Integer> worldSizes = levelOrderer.getWorldSizes();
			if (world > 0 && world < worldSizes.size()){
				
				//Future world needs to average WORLD_UNLOCK_STARS
				int previousWorldProgress = getBaseWorldStars(world - 1);
				boolean averageTwo = (previousWorldProgress >= (worldSizes.get(world - 1) - 1) * WORLD_UNLOCK_STARS);
				if (averageTwo){
					
					//And have completed every level
					int startIndex = levelOrderer.getWorldStartIndices().get(world - 1);
					for (int i = 0; i < worldSizes.get(world - 1) - 1; i++){
						if (getLevelMoves(startIndex + i) == 0){
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
		List<Integer> worldSizes = levelOrderer.getWorldSizes();
		if (world >= 0 && world < worldSizes.size()){
			int currentWorldProgress = getBaseWorldStars(world);
			return (currentWorldProgress >= (worldSizes.get(world) - 1) * BONUS_UNLOCK_STARS);
		} else {
			return false;
		}
	}

	//Returns the number of moves done on this level, or 0 if not completed
	public int getLevelMoves(int index){
		return scores[index];
	}

	//Returns the number of stars earned on this level, or 0 if not completed
	public int getLevelStars(int index){
		return stars[index];
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
		for (String s : xmlTags){
			toSave += s;
		}
		toSave += "<sound music=" + playMusic + " fx=" + playFX + "/>";
		saveFile.writeString(toSave, false);
	}

	private void load(){
		String toMatch = saveFile.readString();
		Pattern pat = Pattern.compile("<level\\s*id=(\\d+)\\s*score=(\\d+)\\s*stars=(\\d+)\\s*/>");
		Matcher mat = pat.matcher(toMatch.trim());

		Map<Integer,Integer> inverseIndexMap = levelOrderer.getInverseMapping();
		while (mat.find()){
			int uniqueId = Integer.parseInt(mat.group(1));
			Integer indexObj = inverseIndexMap.get(uniqueId);
			if (indexObj != null){
				int index = indexObj;
				scores[index] = Integer.parseInt(mat.group(2));
				stars[index] = Integer.parseInt(mat.group(3));
				uniqueIds[index] = uniqueId;
				setXmlTag(index);
			}
		}

		pat = Pattern.compile("<sound\\s*music=(\\d+)\\s*fx=(\\d+)/>");
		mat = pat.matcher(toMatch);
		mat.find();
		playMusic = Integer.parseInt(mat.group(1));
		playFX = Integer.parseInt(mat.group(2));
	}

	private void setXmlTag(int index){
		xmlTags[index] = "<level id=" + uniqueIds[index] + 
				" score=" + scores[index] + 
				" stars=" + stars[index] +" />\n";
	}
}
