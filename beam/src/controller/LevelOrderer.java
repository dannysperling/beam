package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LevelOrderer {

	/**
	 * Maps from positions in the world order to ID's in the level file
	 */
	private List<List<Integer>> mapping = new ArrayList<List<Integer>>();

	/**
	 * Create a LevelOrderer based on the ordering found in the level order file.
	 * 
	 * @param fon
	 * 			The path to the level order file
	 * @param useGDX
	 * 			Whether to use GDX to load the level or a buffered reader
	 */
	public LevelOrderer(String fon, boolean useGDX) {
		
		String text = "";
		
		//Simply use the gdx file handle if using Gdx
		if (useGDX){
			FileHandle fh = Gdx.files.internal(fon);
			text = fh.readString();
		} 
		//Otherwise use a buffered reader
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(fon));
				String line = null;
				while ((line = reader.readLine()) != null) {
					text += line + "\n";
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Match for each of the worlds
		Pattern pat = Pattern
				.compile("###\\s*WORLDSTART:.*###([^#]+)###\\s*WORLDEND\\s*###");
		Matcher mat = pat.matcher(text);
		while (mat.find()) {
			
			//Split on white space - search for numbers
			String[] parsed = mat.group(1).split("\\s+");

			//Levels IDs in the current world
			List<Integer> currentWorldLevels = new ArrayList<Integer>();

			for (int i = 0; i < parsed.length; i++) {
				try {
					int uniqueId = Integer.parseInt(parsed[i]);
					currentWorldLevels.add(uniqueId);
				} catch (Exception e) {
				}
			}
			//Add the world to the mapping
			mapping.add(currentWorldLevels);
		}
	}


	/**
	 * Get the loader ID of a level based on its world and ordinal.
	 * i.e., world = 2, ordinalInWorld = 3 would load level 2-3
	 * 
	 * @param world
	 * 				World of the level, 1 <= world <= numWorlds
	 * @param ordinalInWorld
	 * 				Ordinal of level in world, 1 <= ordinalInWorld <= numLevelsInWorld
	 * @return ID of the level, or -1 if out of bounds
	 */
	public int getUniqueId(int world, int ordinalInWorld) {
		
		//Check to make sure world is in bounds
		if (world >= 1 && world <= mapping.size()){
			
			//Subtract 1 from the world to get the index
			List<Integer> worldIDs = mapping.get(world - 1);
			
			//Check to make sure ordinal is in bounds
			if (ordinalInWorld >= 1 && ordinalInWorld <= worldIDs.size()){
				
				//Subtract 1 from the ordinal to get the index
				return worldIDs.get(ordinalInWorld - 1);
			}
		}
		
		//If out of bounds, return -1
		return -1;
	}
	
	/**
	 * Gets the number of worlds in the game
	 * 
	 * @return the number of worlds
	 */
	public int getNumWorlds(){
		return mapping.size();
	}
	
	/**
	 * Gets the size of a given world, or -1 if out of bounds
	 * 1 <= world <= numWorlds
	 * 
	 * @return the size of the world
	 */
	public int getWorldSize(int world){
		if (world >= 1 && world <= getNumWorlds()){
			return mapping.get(world - 1).size();
		}
		return -1;
	}

	/**
	 * Gets the size of every world in a list
	 * 
	 * @return List of the sizes of every world
	 */
	public List<Integer> getWorldSizes() {
		
		List<Integer> worldSizes = new ArrayList<Integer>();
		
		for (int i = 1; i <= getNumWorlds(); i++){
			worldSizes.add(getWorldSize(i));
		}
		
		return worldSizes;
	}

	/**
	 * Gets the full mapping for logging purposes
	 * @return the mapping to unique IDs
	 */
	public List<List<Integer>> getMapping() {
		return mapping;
	}
}
