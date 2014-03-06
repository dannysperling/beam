package com.me.beam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class GameProgress {
	
	private String fileString = "data/saveFile.xml";
	private FileHandle saveFile;

	public GameProgress(int numLevels){
		saveFile = Gdx.files.internal(fileString);
		
	}
	
	public void setLevelScore(int moves, int stars){
		
	}
	
	public int getLevelScore(){
		return 0;
	}
	
	public void save(){
		
	}
	
	public void load(){
		
	}
}
