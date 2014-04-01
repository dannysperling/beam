package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.*;

import model.Board;
import model.Piece;
import model.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import controller.GameEngine.Color;

public class LevelLoader {
	
	/**
	 * Stores the name of the level ID file
	 */
	private String file;
	
	/**
	 * Handle regex parsing of levels. Integers are used for groups because
	 * named capture isn't supported on android.
	 */
	private String FULL_LEVEL_REGEX = "(<level id=(\\d+) par=(\\d+) perfect=(\\d+)>)[\\s]+"
			+ "((.*\\n)+?)(</level>)";
	private static final int IDgroup = 2;
	private static final int PARgroup = 3;
	private static final int PERFECTgroup = 4;

	/**
	 * Has a reference to the level orderer to allow the loader to find the correct IDs
	 */
	private LevelOrderer levelOrderer;
	
	/**
	 * Indicates whether to use GDX for file reading or standard IO
	 */
	private boolean useGDX;

	/**
	 * Create a LeveLoader for the given file. Any FileNotFound or IO exceptions
	 * are returned to the caller.
	 * 
	 * @param fn
	 *            The path to the level file
	 * @param fon
	 *            The path to the fileOrder file
	 * @param useGDX
	 *		      Whether to use GDX for file loading or java readers
	 * 				
	 */
	public LevelLoader(String fn, LevelOrderer levelOrderer, boolean useGDX) {
		file = fn;
		this.levelOrderer = levelOrderer;
		this.useGDX = useGDX;
	}

	/**
	 * Load the level from file with the given world-ordinalInWorld
	 * i.e., world = 2, ordinalInWorld = 3 would load level 2-3
	 * 
	 * @param world
	 * 				World of the level, 1 <= world <= numWorlds
	 * @param ordinalInWorld
	 * 				Ordinal of level in world, 1 <= ordinalInWorld <= numLevelsInWorld
	 * @return null if level was not found or was malformed.
	 */
	public Board getLevel(int world, int ordinalInWorld) {

		//Get the unique ID
		int id = levelOrderer.getUniqueId(world, ordinalInWorld);
		
		//Access from board. Will correctly be null if malformed or out of bounds
		return loadLevelByID(id);
	}
	
	/**
	 * Loads a level from file based on the id. 
	 * 
	 * @param id
	 * 				The unique id of the level
	 * @return	
	 * 				The loaded level, or null if malformed
	 */
	private Board loadLevelByID(int id){
		GameEngine.debug("Looking for level " + id);

		String spec = findLevelByID(id);
		GameEngine.debug("Level spec: \n" + spec);
		if (spec == null)
			return null;
		Board b = buildBoard(spec);
		if (b == null) {
			GameEngine.debug("\n\nBoard is NULL\n");
		}
		return b;
	}

	/**
	 * Builds a board based on a string specification. Uses regex.
	 * @param spec
	 * 				Specification of the board
	 * @return
	 * 				The built board, or null if malformed
	 */
	private Board buildBoard(String spec) {
		
		//Match the full level to the specification to trim off excess characters,
		//ensure the spec matches and group out the various parts
		Pattern pat = Pattern.compile(FULL_LEVEL_REGEX, Pattern.UNIX_LINES);
		Matcher match = pat.matcher(spec.trim());
		if (!match.matches()) {
			GameEngine.debug("Regex doesn't match");
			return null;
		}
		
		//Extract out the specification for the tiles
		String tileSpec = extractBoard(match.group());
		
		//Separate out the rows and collumns
		String[] rows = tileSpec.split("\\n");
		int height = rows.length;
		int width = rows[0].split(",").length;
		GameEngine.debug("Level is " + width + " x " + height);
		
		//Build the tiles and pieces for the board
		Tile[][] tiles = new Tile[width][height];
		Piece[][] pieces = new Piece[width][height];
		
		//Go row by row
		for (int y = 0; y < height; y++) {
			//Build the row and associated columns
			String row = rows[y];
			String[] cols = row.split(",");
			
			//Ensure each row has the same number of columns
			if (cols.length != width)
				return null;
			
			//Go column by column
			for (int x = 0; x < width; x++) {
				
				//Work on a single tile
				String cell = cols[x];
				
				//Flip the row to get the correct y position
				Tile t = new Tile(x, height - 1 - y);
				Piece p = null;
				
				//Tiles can have multiple elements - go through and add each
				String[] contents = cell.split(":");
				for (String s : contents) {
					s = s.trim();
					if (s.equals("e"))
						break;
					if (s.equals("glass")) {
						t.isGlass = true;
						break;
					}
					//For goals, painters, and pieces, need to look up the
					//color based on the given index.
					if (s.startsWith("goal_")) {
						t.setGoal(Color.lookup(Integer.parseInt(s
								.substring("goal_".length()))));
					} else if (s.startsWith("painter_")) {
						t.setPainter(Color.lookup(Integer.parseInt(s
								.substring("painter_".length()))));
					} else {
						p = new Piece(x, height - 1 - y, Color.lookup(Integer
								.parseInt(s)));
					}
				}
				
				//Toss the tile and (possible) piece into the arrays
				tiles[x][height - 1 - y] = t;
				pieces[x][height - 1 - y] = p;
			}
		}
		
		//Fill in other assoceiated information based on matches
		int id = Integer.parseInt(match.group(IDgroup));
		int perfect = Integer.parseInt(match.group(PERFECTgroup));
		int par = Integer.parseInt(match.group(PARgroup));
		
		//Created the board
		Board b = new Board(tiles, pieces, id, perfect, par);
		
		//Add beam goals separately
		processBeamGoals(b, match.group());
		return b;
	}

	/**
	 * Searches the level for beam goals and adds them explicitly
	 * @param b
	 * 				The board to add the beam goals to
	 * @param group
	 * 				The level specification regex
	 */
	private void processBeamGoals(Board b, String group) {
		
		//Add any found beam goals
		Pattern pat = Pattern
				.compile("<beamGoal\\s*color=(\\d+)\\s*count=(\\d+)/>");
		Matcher mat = pat.matcher(group);
		while (mat.find()) {
			Color color = Color.lookup(Integer.parseInt(mat.group(1)));
			int count = Integer.parseInt(mat.group(2));
			b.addBeamObjective(color, count);
		}

	}

	/**
	 * Parses out the portions of the level specifying the tiles on the board
	 * 
	 * @param group
	 * 				The entire regex of the level
	 * @return
	 * 				The specific regex for the tiles
	 */
	private String extractBoard(String group) {
		Pattern pat = Pattern.compile(">\\s*\\n([^><]*?)</l",
				Pattern.UNIX_LINES);
		Matcher mat = pat.matcher(group);
		mat.find();
		GameEngine.debug("Extracted board:\n" + mat.group(1));
		return mat.group(1);
	}

	/**
	 * Searches for a level in the levels file based on its unique ID
	 * @param id
	 * 				The unique id of the level
	 * @return
	 * 				The string representation of the level
	 */
	private String findLevelByID(int id) {
		String text = "";
		
		//Either use simple gdx or the buffered reader depending on the application
		if (useGDX) {
			FileHandle fh = Gdx.files.internal(file);
			text = fh.readString();
		} else {
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
		}
		
		//Search for the generic pattern of the level
		Pattern pat = Pattern.compile("<level(.|\n)*?/level>",
				Pattern.UNIX_LINES);
		Matcher match = pat.matcher(text);
		while (match.find()) {
			if (match.group().contains("id=" + id)) {
				return match.group();
			}
		}
		return null;
	}

}
