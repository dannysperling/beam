package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.me.beam.Board;
import com.me.beam.GameEngine;
import com.me.beam.GameEngine.Color;
import com.me.beam.Piece;
import com.me.beam.Tile;

/**
 * This is the Level Editor board load/save tool, and is not compatible with the
 * game's level loader. Interestingly is loads games upside down to avoid having
 * to change draw code. This means boards are also saved upside down.
 * 
 * @author RGlidden
 * 
 */
public class LevelIO {
	private String file;
	private String FULL_LEVEL_REGEX = "(<level id=(\\d+) par=(\\d+) perfect=(\\d+)>)[\\s]+"
			// + "(<beamGoal color=(\\d+) count=(\\d+)/>)*[\\s]+"
			+ "((.*\\n)+?)(</level>)";
	// Regex groups because named capture isn't supported on Android
	private static final int IDgroup = 2;
	private static final int PARgroup = 3;
	private static final int PERFECTgroup = 4;
	private EditorModel model;

	/**
	 * Create a LeveLoader for the given file. Any FileNotFound or IO exceptions
	 * are returned to the caller.
	 * 
	 * @param fn
	 *            The path to the level file
	 * @param editorModel 
	 * @param fon
	 *            The path to the fileOrder file
	 */
	public LevelIO(String fn, EditorModel editorModel) {
		file = fn;
		model = editorModel;
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
		String spec = findLevelByID(id);
		if (spec == null)
			return null;
		Board b = buildBoard(spec);
		if (b == null) {
			System.out.println("Board is NULL");
			System.out.println(spec);
		}
		Pattern pat = Pattern.compile("<attribution name=\\\"(.*)\\\" author=\\\"(.*)\\\"/>");
		Matcher mat = pat.matcher(spec);
		if (mat.find()){
			model.workingTitle = mat.group(1);
			model.workingAuthor = mat.group(2);
		}
		return b;
	}

	// Abandon hope, all ye who try and read this
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
		Tile[][] tiles = new Tile[width][height];
		Piece[][] pieces = new Piece[width][height];
		for (int y = 0; y < height; y++) {
			String row = rows[y];
			String[] cols = row.split(",");
			if (cols.length != width)
				return null;
			for (int x = 0; x < width; x++) {
				String cell = cols[x];
				Tile t = new Tile(x, height - 1 - y);
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
						p = new Piece(x, height - 1 - y, Color.lookup(Integer
								.parseInt(s)));
					}
				}
				tiles[x][height - 1 - y] = t;
				pieces[x][height - 1 - y] = p;
			}
		}
		int id = Integer.parseInt(match.group(IDgroup));
		int perfect = Integer.parseInt(match.group(PERFECTgroup));
		int par = Integer.parseInt(match.group(PARgroup));
		Board b = new Board(tiles, pieces, id, perfect, par);
		processBeamGoals(b, match.group());
		return b;
	}

	private void processBeamGoals(Board b, String group) {
		Pattern pat = Pattern
				.compile("<beamGoal\\s*color=(\\d+)\\s*count=(\\d+)/>");
		Matcher mat = pat.matcher(group);
		while (mat.find()) {
			Color color = Color.lookup(Integer.parseInt(mat.group(1)));
			int count = Integer.parseInt(mat.group(2));
			b.addBeamObjective(color, count);
		}

	}

	private String extractBoard(String group) {
		Pattern pat = Pattern.compile(">\\s*\\n([^><]*?)</l",
				Pattern.UNIX_LINES);
		Matcher mat = pat.matcher(group);
		mat.find();
		// System.out.println("Group 1: \n"+reverseLines(mat.group(1)));
		return reverseLines(mat.group(1));
	}

	private static String reverseLines(String str) {
		StringBuffer sb = new StringBuffer();
		String[] lines = str.split("\n");
		for (String line : lines) {
			sb.insert(0, line + "\n");
		}
		return sb.toString();
	}

	private String findLevelByID(int id) throws IOException {
		String text = fileContent(file);
		Pattern pat = Pattern.compile("<level(.|\n)*?/level>",
				Pattern.UNIX_LINES);
		Matcher match = pat.matcher(text);
		while (match.find()) {
			if (match.group().contains("id=" + id +" ")) {
				return match.group();
			}
		}
		return null;
	}

	private String fileContent(String file) {
		FileReader fis;
		try {
			fis = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("File " + file + " not found!");
			System.exit(-1);
			return "";// Dead code to trick compiler
		}
		BufferedReader br = new BufferedReader(fis);
		StringBuffer buf = new StringBuffer();
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				buf.append(line + "\n");
			}
			br.close();
		} catch (IOException e) {
			return "Error";
		}
		String text = buf.toString();
		
		return text;
	}

	/**
	 * Appends the given board to the file this class was constructed with, with
	 * the provided name and author attributions. If a level with that ID
	 * already exists, it is replaced. Otherwise, the lowest available ID is
	 * chosen and the level is saved as new level. Currently the ordering is
	 * unaffected. Note that level file ordering is not preserved.
	 * 
	 * @param b
	 *            - board with accurate par/perfect fields
	 * @param title
	 *            - "" for no title
	 * @param author
	 *            - your name here, "" allowable
	 * @throws IOException
	 */
	public void saveBoard(Board b, String title, String author)
			throws IOException {
		String existingFile = fileContent(file);
		if (getLevel(b.id) != null) {// If id exists, remove it from file so we can re-insert
			Pattern pat = Pattern.compile("<level id="+b.id+"\\s.*?/level>", Pattern.DOTALL);
			Matcher mat = pat.matcher(existingFile);
			existingFile = mat.replaceAll("");
			mat.reset();
			System.out.println("And matches? - "+mat.find());
		} else { // otherwise get lowest positive id available
			b.id = generateNewId();
		}
		String header = "<level id=" + b.id + " par=" + b.par + " perfect="
				+ b.perfect + ">" + "\n" + "<attribution name=\"" + title
				+ "\" author=\"" + author + "\"/>" + "\n";
		String beamGoals = generateBeamGoalSpec();
		String boardSpec = generateSpec(b);
		String footer = "</level>\n";
		String newFile = existingFile +"\n"+ header+beamGoals+boardSpec+footer;
		File f = new File(file);
		System.out.println("Old file deleted? - "+f.delete());
		f.createNewFile();
		FileWriter fw = new FileWriter(f);
		fw.write(newFile.trim());
		fw.flush();
		fw.close();
		System.out.println("\n"+file);
		System.out.println("\n\n"+newFile);
	}

	private String generateBeamGoalSpec() {
		StringBuffer sb = new StringBuffer();
		for (GameEngine.Color c : GameEngine.Color.values()){
			if (c == GameEngine.Color.NONE) continue;
			int beamsReq = model.b.getBeamObjectiveCount(c);
			if (beamsReq>=0){
				sb.append("<beamGoal color="+c.toIndex()+" count="+beamsReq+"/>\n");
			}
		}
		return sb.toString();
	}

	private int generateNewId() {
		int id = 0;
		Pattern pat = Pattern.compile("level id=(\\d+)");
		Matcher mat = pat.matcher(fileContent(file));
		while (mat.find()) {
			int cur = Integer.parseInt(mat.group(1));
			if (cur > id) {
				id = cur;
			}
		}
		return id + 1;
	}

	private String generateSpec(Board b) {
		StringBuffer  sb = new StringBuffer();
		for (int y = 0; y < b.getNumVerticalTiles(); y++) {
			for (int x = 0; x < b.getNumHorizontalTiles(); x++) {
				if (x>0) sb.append(",");
				Tile t = b.getTileAtBoardPosition(x, y);
				Piece p = b.getPieceOnTile(t);
				String tileText = "";
				if (t.isGlass)
					tileText = "glass";
				else {
					if (t.hasGoal()) {
						tileText += "goal_" + t.getGoalColor().toIndex();
					}
					if (t.hasPainter()){
						if (!tileText.isEmpty()) tileText += ":";
						tileText += "painter_"+t.getPainterColor().toIndex();
					}
					if (p != null){
						tileText = p.getColor().toIndex()+(tileText.isEmpty()?"":":"+tileText);
					}
				}
				if (tileText.isEmpty()) tileText = "e";
				sb.append(tileText);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
