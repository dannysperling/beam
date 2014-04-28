package controller;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;

import model.Tutorial;

public class TutorialLoader {

	/**
	 * Stores the name of the tutorial file
	 */
	private String file;
	
	/**
	 * Has a reference to the level orderer to allow the loader to find the correct IDs
	 */
	private LevelOrderer levelOrderer;
	
	
	/**
	 * Create a TutorialLoader for the given file. Any FileNotFound or IO exceptions
	 * are returned to the caller.
	 * 
	 * @param fn
	 *            The path to the level file
	 * @param levelOrderer
	 * 			  The orderer to be used to find unique ids
	 */
	public TutorialLoader(String fn, LevelOrderer levelOrderer){
		file = fn;
		this.levelOrderer = levelOrderer;
	}
	
	/**
	 * Load the tutorial from file with the given world-ordinalInWorld
	 * i.e., world = 2, ordinalInWorld = 3 would load level 2-3
	 * 
	 * @param world
	 * 				World of the level, 1 <= world <= numWorlds
	 * @param ordinalInWorld
	 * 				Ordinal of level in world, 1 <= ordinalInWorld <= numLevelsInWorld
	 * @return null if there is no tutorial, otherwise the tutorial
	 */
	public Tutorial getTutorial(int world, int ordinalInWorld) {

		//Get the unique ID
		int id = levelOrderer.getUniqueId(world, ordinalInWorld);
		
		//Access from board. Will correctly be null if malformed or out of bounds
		return loadTutorialByID(id);
	}
	
	/**
	 * Loads a tutorial from file based on the id. 
	 * 
	 * @param id
	 * 				The unique id of the level
	 * @return	
	 * 				The loaded tutorial, or null if not present or malformed
	 */
	private Tutorial loadTutorialByID(int id){
		GameEngine.debug("Looking for tutorial " + id);

		List<String> elements = findTutorialByID(id);
		GameEngine.debug("Tutorial spec: \n" + elements);
		if (elements == null)
			return null;
		
		//Compile the tutorial object
		Tutorial tutorial = new Tutorial();
		for (String elem : elements){
			//Is it an image?
			if (elem.contains("IMAGE")){
				int numFrames = -1, sideLength = -1;
				Pattern pat = Pattern.compile("(\\d+) frames, (\\d+) side,",
						Pattern.UNIX_LINES);
				Matcher match = pat.matcher(elem);
				if (match.find()) {
					numFrames = Integer.parseInt(match.group(1));
					sideLength = Integer.parseInt(match.group(2));
				}
				//File name at the end
				String beforeName = "side, ";
				String fileName = elem.substring(elem.indexOf(beforeName) + beforeName.length());
				
				//Add to the tutorial
				tutorial.addImageElement(fileName, sideLength, numFrames);
			}
			//Nope - must be text
			else {
				//Add to the tutorial
				tutorial.addTextElement(elem);
			}
		}
		return tutorial;
	}
	
	/**
	 * Searches for a tutorial in the tutorials file based on its unique ID
	 * @param id
	 * 				The unique id of the level
	 * @return
	 * 				The string representation of the tutorial, as a list of line
	 */
	private List<String> findTutorialByID(int id) {
		
		//Search for the id
		List<String> foundElements = null;
		try {
			BufferedReader reader = Gdx.files.internal(file).reader(8);
			String line = null;
			boolean finding = false, found = false;
			while ((line = reader.readLine()) != null && !found) {
				//Found the id
				if (line.contains("TUTORIAL FOR LEVEL UNIQUE ID: " + id)){
					finding = true;
					foundElements = new ArrayList<String>();
				}
				//Compile its lines
				else if (finding){
					if (line.contains("END TUTORIAL")){
						found = true;
					} else {
						foundElements.add(line);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return foundElements;
	}
}
