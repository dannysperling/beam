package com.me.beam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.me.beam.GameEngine.Color;

public class LevelLoader implements Iterable<Board> {
	public static final boolean DEBUG_MODE = true;
	private String file;
	private ArrayList<Integer> ids = new ArrayList<Integer>();
	private String LEVEL_REGEX = "(<level id=(?<id>\\d+) par=(?<par>\\d+)>)[\\s]+"
			+ "(<beamGoal color=(?<goalColor>\\d+) count=(?<goalNumber>\\d+)/>)?[\\s]+"
			+ "(?<board>(.*\\n)+)" + "(</level>)";

	/**
	 * Create a LeveLoader for the given file. Any FileNotFound or IO exceptions
	 * are returned to the caller.
	 * 
	 * @param fn
	 *            The path to the file
	 */
	public LevelLoader(String fn) {
		file = fn;
		getIds();
	}

	private void getIds() {
		FileHandle fh = Gdx.files.internal(file);
		String text = fh.readString();
		Pattern pat = Pattern.compile("id=(\\d)");
		Matcher match = pat.matcher(text);
		while (match.find()) {
			ids.add(Integer.parseInt(match.group(1)));
		}
		debug(ids.size() + " levels found:");
		debug("\t" + ids.toString());
	}

	/**
	 * Load the level from file with the given id
	 * 
	 * @param id
	 *            Must be a valid id in the file
	 * @return null if id was not found or level was malformed.
	 */
	public Board getLevel(int id) {
		debug("Looking for level " + id);
		String spec = findLevelByID(id);
		debug("Level spec: \n" + spec);
		if (spec == null)
			return null;
		Board b = buildBoard(spec);
		if (DEBUG_MODE && b == null) {
			debug("\n\nBoard is NULL\n");
		}
		return b;
	}

	private Board buildBoard(String spec) {
		Pattern pat = Pattern.compile(LEVEL_REGEX, Pattern.UNIX_LINES);
		Matcher match = pat.matcher(spec);
		if (!match.matches()) {
			debug("Regex doesn't match");
			return null;
		}
		// Board ret = new Board();
		String tileSpec = match.group("board");
		String[] rows = tileSpec.split("\\n");
		int height = rows.length;
		int width = rows[0].split(",").length;
		debug("Level is " + width + " x " + height);
		Tile[][] tiles = new Tile[width][height];
		Piece[][] pieces = new Piece[width][height];
		for (int y = 0; y < height; y++) {
			String row = rows[y];
			String[] cols = row.split(",");
			if (cols.length != width)
				return null;
			for (int x = 0; x < width; x++) {
				String cell = cols[x];
				Tile t = new Tile(x, y);
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
						debug("("+x+","+y+") = "+s);
						p = new Piece(x, y, Color.lookup(Integer.parseInt(s)));
					}
				}
				tiles[x][y] = t;
				pieces[x][y] = p;
			}
		}
		int id = Integer.parseInt(match.group("id"));
		int par = Integer.parseInt(match.group("par"));
		Board b = new Board(tiles, pieces, id, par);
		boolean hasBeamGoal = match.group("goalColor") != null;
		if (hasBeamGoal) {
			debug("Has beam goals? - " + hasBeamGoal);
			Color gc = Color.lookup(Integer.parseInt(match.group("goalColor")));
			int gn = Integer.parseInt(match.group("goalNumber"));
			b.setBeamGoal(gc, gn);
		}
		return b;
	}

	private String findLevelByID(int id) {
		FileHandle fh = Gdx.files.internal(file);
		String text = fh.readString();
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

	private void debug(String s) {
		if (!DEBUG_MODE)
			return;
		System.out.println(s);
	}

	/**
	 * Snazzy! You can use: for(Board b : LevelLoader) to load all of the levels
	 * in a file. Pretty sweet, eh?
	 */
	public Iterator<Board> iterator() {
		return new LevelFileIterator(this);
	}

	public class LevelFileIterator implements Iterator<Board> {
		private int index = 0;
		private LevelLoader ll;

		public LevelFileIterator(LevelLoader l) {
			ll = l;
		}

		public boolean hasNext() {
			for (Integer i : ll.ids) {
				if (i >= index)
					return true;
			}
			return false;
		}

		public Board next() {
			if (!hasNext())
				throw new NoSuchElementException();
			while (!ll.ids.contains(index)) {
				++index;
			}
			Board ret = ll.getLevel(index);
			index++;
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"Why on earth do you want to delete a level?");

		}
	}

}
