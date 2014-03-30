package com.me.beam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LevelOrderer {

	private List<Integer> mapping = new ArrayList<Integer>();
	private List<Integer> worldSizes = new ArrayList<Integer>();
	private List<Integer> worldStartIndices = new ArrayList<Integer>();

	// Initializes the level orderer with the correct file
	public LevelOrderer(String fon) {
		FileHandle fh = Gdx.files.internal(fon);
		String text = fh.readString();

		// Match for each of the worlds
		Pattern pat = Pattern
				.compile("###\\s*WORLDSTART:.*###([^#]+)###\\s*WORLDEND\\s*###");
		Matcher mat = pat.matcher(text);
		while (mat.find()) {
			String[] parsed = mat.group(1).split("\\s+");

			int numInWorld = 0;

			for (int i = 0; i < parsed.length; i++) {
				try {
					int uniqueId = Integer.parseInt(parsed[i]);
					mapping.add(uniqueId);
					numInWorld++;
				} catch (Exception e) {
				}
			}
			worldSizes.add(numInWorld);
		}
		worldStartIndices.add(0);
		for (int i = 0; i < worldSizes.size() - 1; i++){
			worldStartIndices.add(worldStartIndices.get(i) + worldSizes.get(i));
		}
	}

	public LevelOrderer(String file, boolean pointless) {
		String text = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				text += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Match for each of the worlds
		Pattern pat = Pattern
				.compile("###\\s*WORLDSTART:.*###([^#]+)###\\s*WORLDEND\\s*###");
		Matcher mat = pat.matcher(text);
		while (mat.find()) {
			String[] parsed = mat.group(1).split("\\s+");

			int numInWorld = 0;

			for (int i = 0; i < parsed.length; i++) {
				try {
					int uniqueId = Integer.parseInt(parsed[i]);
					mapping.add(uniqueId);
					numInWorld++;
				} catch (Exception e) {
				}
			}
			worldSizes.add(numInWorld);
		}
	}

	// Get the unique id for an index
	public int getUniqueId(int index) {
		if (index >= 0 && index < mapping.size())
			return mapping.get(index);
		return 0;
	}

	public int getNumLevels() {
		return mapping.size();
	}

	public List<Integer> getWorldSizes() {
		return worldSizes;
	}
	
	public List<Integer> getWorldStartIndices(){
		return worldStartIndices;
	}

	// Get the map in reverse for purposes of populating save game file
	public Map<Integer, Integer> getInverseMapping() {
		Map<Integer, Integer> inverseMap = new HashMap<Integer, Integer>();

		for (int i = 0; i < mapping.size(); i++) {
			inverseMap.put(mapping.get(i), i);
		}

		return inverseMap;
	}

	// Get an array of all unique ids
	public int[] getUniqueIds() {
		int[] uniqueIds = new int[mapping.size()];

		for (int i = 0; i < mapping.size(); i++) {
			uniqueIds[i] = mapping.get(i);
		}
		return uniqueIds;
	}
}
