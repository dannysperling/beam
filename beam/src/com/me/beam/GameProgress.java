package com.me.beam;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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

	public GameProgress(LevelOrderer levelOrderer){
		saveFile = Gdx.files.local(fileString);
		this.levelOrderer = levelOrderer;
		
		//Check if the file exists and create it if it doesn't
		if (!saveFile.exists()){
			saveFile.writeString("<sound music=1 fx=1/>\n", false);
		}
		
		//Keep track of progress on levels
		int numLevels = levelOrderer.getNumLevels();
		scores = new int[numLevels];
		stars = new int[numLevels];
		xmlTags = new String[numLevels];
		uniqueIds = new int[numLevels];
		for (int i = 0; i < numLevels; i++){
			scores[i] = -1;
			stars[i] = -1;
			xmlTags[i] = "";
			uniqueIds[i] = -1;
		}
		
		load();
	}
	
	//Returns if the new score was better
	public boolean setLevelScore(int ordinal, int moves, int levelStars){
		if (scores[ordinal] == -1 || moves < scores[ordinal]){
			uniqueIds[ordinal] = levelOrderer.getUniqueId(ordinal);
			scores[ordinal] = moves;
			stars[ordinal] = levelStars;
			setXmlTag(ordinal);
			save();
			return true;
		}
		return false;
	}
	
	//Returns the number of moves done on this level, or -1 if not completed
	public int getLevelMoves(int ordinal){
		return scores[ordinal];
	}
	
	//Returns the number of stars earned on this level, or -1 if not completed
	public int getLevelStars(int ordinal){
		return stars[ordinal];
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
		
		Map<Integer,Integer> inverseOrdinalMap = levelOrderer.getInverseMapping();
		while (mat.find()){
			int uniqueId = Integer.parseInt(mat.group(1));
			int ordinal = inverseOrdinalMap.get(uniqueId);
			scores[ordinal] = Integer.parseInt(mat.group(2));
			stars[ordinal] = Integer.parseInt(mat.group(3));
			uniqueIds[ordinal] = uniqueId;
			setXmlTag(ordinal);
		}
		
		pat = Pattern.compile("<sound\\s*music=(\\d+)\\s*fx=(\\d+)/>");
		mat = pat.matcher(toMatch);
		mat.find();
		playMusic = Integer.parseInt(mat.group(1));
		playFX = Integer.parseInt(mat.group(2));
	}
	
	private void setXmlTag(int ordinal){
		xmlTags[ordinal] = "<level id=" + uniqueIds[ordinal] + 
							" score=" + scores[ordinal] + 
							" stars=" + stars[ordinal] +" />\n";
	}
}
