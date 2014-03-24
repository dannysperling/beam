package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.me.beam.Board;
import com.me.beam.GameEngine;
import com.me.beam.GameEngine.Color;
import com.me.beam.Piece;
import com.me.beam.Tile;

public class LevelLoader {
	private String file;
	private String FULL_LEVEL_REGEX = "(<level id=(\\d+) par=(\\d+) perfect=(\\d+)>)[\\s]+"
			//+ "(<beamGoal color=(\\d+) count=(\\d+)/>)*[\\s]+"
			+ "((.*\\n)+?)(</level>)";
	//Regex groups because named capture isn't supported on Android
	private static final int IDgroup = 2;
	private static final int PARgroup = 3;
	private static final int PERFECTgroup = 4;

	/**
	 * Create a LeveLoader for the given file. Any FileNotFound or IO exceptions
	 * are returned to the caller.
	 * 
	 * @param fn
	 *            The path to the level file
	 * @param fon
	 * 			  The path to the fileOrder file
	 */
	public LevelLoader(String fn) {
		file = fn;
	}
	
	/**
	 * Load the level from file with the given id
	 * 
	 * @param ordinal
	 *            Must be a valid ordinal in the file
	 * @return null if id was not found or level was malformed.
	 * @throws IOException 
	 */
	public Board getLevel(int id) throws IOException {
		GameEngine.debug("Looking for level " + id);
		
		String spec = findLevelByID(id);
		GameEngine.debug("Level spec: \n" + spec);
		if (spec == null)
			return null;
		Board b = buildBoard(spec);
		if (b == null) {
			System.out.println("Board is NULL");
			System.out.println(spec);
		}
		return b;
	}

	//Abandon hope, all ye who try and read this
	private Board buildBoard(String spec) {
		Pattern pat = Pattern.compile(FULL_LEVEL_REGEX, Pattern.UNIX_LINES);
		Matcher match = pat.matcher(spec.trim());
		if (!match.matches()) {
			System.out.println("Regex doesn't match");
			return null;
		}
		// Board ret = new Board();
		String tileSpec = extractBoard(match.group());
		String[] rows = tileSpec.split("\\n");
		int height = rows.length;
		int width = rows[0].split(",").length;
		GameEngine.debug("Level is " + width + " x " + height);
		Tile[][] tiles = new Tile[width][height];
		Piece[][] pieces = new Piece[width][height];
		for (int y = 0; y < height; y++) {
			String row = rows[y];
			String[] cols = row.split(",");
			if (cols.length != width)
				return null;
			for (int x = 0; x < width; x++) {
				String cell = cols[x];
				Tile t = new Tile(x, height-1-y);
				Piece p = null;
				String[] contents = cell.split(":");
				for (String s : contents) {
					s = s.trim();
					if (s.equals("e"))
						break;
					if (s.equals("glass")) {
						t.isGlass = true;
						break;
					}
					if (s.startsWith("goal_")) {
						t.setGoal(Color.lookup(Integer.parseInt(s
								.substring("goal_".length()))));
					} else if (s.startsWith("painter_")) {
						t.setPainter(Color.lookup(Integer.parseInt(s
								.substring("painter_".length()))));
					} else {
						p = new Piece(x, height-1-y, Color.lookup(Integer.parseInt(s)));
					}
				}
				tiles[x][height-1-y] = t;
				pieces[x][height-1-y] = p;
			}
		}
		int id = Integer.parseInt(match.group(IDgroup));
		int perfect = Integer.parseInt(match.group(PERFECTgroup));
		int par = Integer.parseInt(match.group(PARgroup));
		Board b = new Board(tiles, pieces, id, perfect, par);
		processBeamGoals(b,match.group());
		return b;
	}

	private void processBeamGoals(Board b, String group) {
		Pattern pat = Pattern.compile("<beamGoal\\s*color=(\\d+)\\s*count=(\\d+)/>");
		Matcher mat = pat.matcher(group);
		while (mat.find()){
			Color color = Color.lookup(Integer.parseInt(mat.group(1)));
			int count = Integer.parseInt(mat.group(2));
			b.addBeamObjective(color, count);
		}
		
	}

	private String extractBoard(String group) {
		Pattern pat = Pattern.compile(">\\s*\\n([^><]*?)</l",Pattern.UNIX_LINES);
		Matcher mat = pat.matcher(group);
		mat.find();
		GameEngine.debug("Extracted board:\n"+mat.group(1));
		return mat.group(1);
	}

	private String findLevelByID(int id) throws IOException {
		FileReader fis = new FileReader(file);
		BufferedReader br = new BufferedReader(fis);
		StringBuffer buf = new StringBuffer();
		String line = "";
		while ((line = br.readLine()) != null){
			buf.append(line+"\n");
		}
		String text = buf.toString();
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