package com.me.beam;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LevelOrderer {

	private List<Integer> mapping = new ArrayList<Integer>();

	//Initializes the level orderer with the correct file
	public LevelOrderer(String fon){
		FileHandle fh = Gdx.files.internal(fon);
		String text = fh.readString();
		String[] parsed = text.split("\\s+");
		
		for (int i = 0; i < parsed.length; i++){
			try{
				int uniqueId = Integer.parseInt(parsed[i]);
				mapping.add(uniqueId);
			} catch (Exception e) {}
		}
	}

	//Get the unique id for an ordinal
	public int getUniqueId(int ordinal){
		if (ordinal >= 0 && ordinal < mapping.size())
			return mapping.get(ordinal);
		return 0;
	}
}
