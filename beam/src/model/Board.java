package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utilities.Constants;

import com.badlogic.gdx.Gdx;

import controller.GameEngine;
import controller.GameEngine.Color;

public class Board {

	/**
	 * Private data for the pieces and tiles
	 */
	private Piece[][] pieces;
	private Tile[][] tiles;

	/**
	 * Explicitly store the goal tiles for convenience
	 */
	private List<Tile> goalTiles = new ArrayList<Tile>();

	/**
	 * Public data on the lasers, id, par, and perfect scores
	 */
	public Set<Laser> lasers = new HashSet<Laser>();
	public int id; // ID in its levels file
	public int par; // Good solution
	public int perfect; // Optimal solution
	
	/**
	 * Private data on what the objectives are for the goals on beam counts
	 */
	private EnumMap<Color, Integer> beamObjectives = new EnumMap<Color, Integer>(
			Color.class);

	/**
	 * Private data regarding drawing the board
	 */
	private int tileSize;
	private int botLeftX;
	private int botLeftY;

	/**
	 * Private data on the size of the board
	 */
	private int width;
	private int height;

	/**
	 * Constructor for a new board
	 * @param numHorTiles
	 * 			The width of the board in tiles
	 * @param numVertTiles
	 * 			The height of the board in tiles
	 */
	public Board(int numHorTiles, int numVertTiles) {

		//Establish private variables on the board dimensions
		this.width = numHorTiles;
		this.height = numVertTiles;
		tiles = new Tile[width][height];
		pieces = new Piece[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tiles[i][j] = new Tile(i, j);
			}
		}

	}

	/**
	 * Construct a board from its data
	 * 
	 * @param t
	 * 			The tile set of the board
	 * @param p
	 * 			The piece set of the board
	 * @param id
	 * 			The unique id of the board
	 * @param perfect
	 * 			The perfect score on the board
	 * @param par
	 * 			The par score for the board
	 */
	public Board(Tile[][] t, Piece[][] p, int id, int perfect, int par) {

		//Call the other constructor to set up basic constants
		this(t.length, t[0].length);

		//Overwrite the tiles and pieces
		this.tiles = t;
		this.pieces = p;

		//Set the id, perfect and par scores
		this.id = id;
		this.perfect = perfect;
		this.par = par;

		//Include the goal tiles
		for (int i = 0; i < t.length; i++) {
			for (int j = 0; j < t[i].length; j++) {
				if (this.tiles[i][j].hasGoal()) {
					goalTiles.add(this.tiles[i][j]);
				}
			}
		}
	}

	/**
	 * Determines the appropriate sizing for the board locations based on all
	 * of its characteristics
	 */
	public void recalculateSizing(){
		//Determine the applicable screen width and height from gdx
		int screenWidth;
		int screenHeight;
		float goalSpace = 0;
		int totalBeams = 0;

		if(beamObjectives.isEmpty()){
			goalSpace = Constants.TEXT_GOAL_HEIGHT;
		} else {
			for(Color c : beamObjectives.keySet()){
				totalBeams += beamObjectives.get(c);
			}
			if(totalBeams == 0){
				goalSpace = Constants.TEXT_GOAL_HEIGHT;
			} else {
				goalSpace = Constants.BEAM_GOAL_HEIGHT * beamObjectives.keySet().size();
			}
		}

		if (Gdx.graphics == null) {
			screenWidth = 0;
			screenHeight = 0;
		} else {
			screenWidth = (int) (Gdx.graphics.getWidth() * (1 - Constants.SIDE_EMPTY_SIZE * 2));
			screenHeight = (int) (Gdx.graphics.getHeight() * (1 - Constants.TOP_BAR_SIZE - Constants.BOT_BAR_SIZE - goalSpace - Constants.GAME_BUTTON_HEIGHT));
		}

		//Determine positions of the board for drawing purposes
		int maxWidth = (int) (screenWidth / width);
		int maxHeight = (int) (screenHeight / height);
		if (Gdx.graphics != null) {
			if (maxWidth < maxHeight) {
				tileSize = maxWidth;
				botLeftX = (int) (Gdx.graphics.getWidth() * Constants.SIDE_EMPTY_SIZE);
				botLeftY = (int) (Gdx.graphics.getHeight()
						* (Constants.BOT_BAR_SIZE + goalSpace) + (screenHeight - (tileSize * height)) / 2);
			} else {
				tileSize = maxHeight;
				botLeftX = (int) (Gdx.graphics.getWidth()
						* Constants.SIDE_EMPTY_SIZE + (screenWidth - (tileSize * width)) / 2);
				botLeftY = (int) (Gdx.graphics.getHeight() * (Constants.BOT_BAR_SIZE + goalSpace));
			}
		}
	}
	
	/**
	 * Gets the top Y coordinate of this board. Used to determine where the buttons
	 * directly above the board will be placed
	 */
	public int getTopYCoord(){
		return botLeftY + (tileSize * getNumVerticalTiles());
	}

	/**
	 * Determine if two tiles are "similar," i.e., they have the same 
	 * qualities in terms of glass, painters, and goals
	 * 
	 * @param x1, y1
	 * 				Coordinates of first tile
	 * @param x2, y2
	 * 				Coordinates of second tile
	 * @return
	 * 				True if the tiles are the same with regards to
	 * 				glass, painter, and goal; false otherwise
	 */
	public boolean areTilesSimilar(int x1, int y1, int x2, int y2) {
		Tile t1 = this.getTileAtBoardPosition(x1, y1);
		Tile t2 = this.getTileAtBoardPosition(x2, y2);
		return t1.hasGlass() == t2.hasGlass()
				&& t1.getGoalColor() == t2.getGoalColor()
				&& t1.getPainterColor() == t2.getPainterColor();
	}

	/**
	 * Adds a beam objective to the board, the number of a certain
	 * color of beam that must be made.
	 * 
	 * @param color
	 * 			The color of the beam objective
	 * @param number
	 * 			The number of beams of that color required
	 */
	public void addBeamObjective(Color color, int number) {
		beamObjectives.put(color, number);
	}

	/**
	 * Gets the colors that are beam objectives for this level
	 */
	public Set<Color> getBeamObjectiveSet() {
		return beamObjectives.keySet();
	}

	/**
	 * Get how many beams need to be made of a certain color on this level
	 * @param color
	 * 			Which color to get the count for
	 * @return
	 * 			How many beams are required of that color, or -1 if that
	 * 			color isn't a beam objective for this level
	 */
	public int getBeamObjectiveCount(Color color) {
		if (beamObjectives != null && beamObjectives.containsKey(color))
			return beamObjectives.get(color);
		return -1;
	}

	/**
	 * Accesses the pieces for this board for purposes of the solver.
	 * Should not be used for main game applications
	 */
	public Piece[][] getPieces() {
		return pieces;
	}

	/**
	 * Sets the tile at (X,Y) to either be or not be glass, according to the
	 * input argument is Glass. Suppying valid coordinates is YOUR PROBLEM NOT
	 * MINE.
	 * 
	 * @param isGlass
	 * @return whether or not the tile was glass before
	 */
	public boolean setGlass(int x, int y, boolean isGlass) {
		boolean ret = tiles[x][y].hasGlass();
		tiles[x][y].setGlass(isGlass);
		return ret;
	}

	/**
	 * Sets the tile at (X,Y) to be a type of painter.
	 * GameEngine.Color.NONE removes the painter.
	 * 
	 * @param painterColor
	 */
	public void setPainter(int x, int y, GameEngine.Color painterColor) {
		tiles[x][y].setPainter(painterColor);
	}

	/**
	 * Sets the tile at (X,Y) to be a type of goal.
	 * GameEngine.Color.NONE removes the goal.
	 * 
	 * @param goalColor
	 */
	public void setGoal(int x, int y, GameEngine.Color goalColor) {
		tiles[x][y].setGoal(goalColor);
	}

	/**
	 * Addes piece p to the board, returning the piece that was previously there
	 * or null.
	 * 
	 * @param p
	 * 			The piece being added
	 * @return
	 * 			The piece previously at the location p is being added to
	 */
	public Piece put(Piece p) {
		int x = p.getXCoord();
		int y = p.getYCoord();
		Piece ret = pieces[x][y];
		pieces[x][y] = p;
		return ret;
	}

	/**
	 * Attempts to remove a piece p, based on p's coordinates
	 * 
	 * @param p
	 *            The piece to remove
	 * @return Whether a piece was removed
	 */
	public boolean removePiece(Piece p) {
		Piece past = pieces[p.getXCoord()][p.getYCoord()];
		pieces[p.getXCoord()][p.getYCoord()] = null;
		return (past != null);
	}

	/**
	 * This the thing that actually physically moves the piece, assuming you try
	 * and move it to an adjacent empty non-glass tile.
	 * 
	 * @return true if piece moved; false otherwise
	 */
	public boolean move(Piece p, Tile t) {
		if (canMove(p.getXCoord(), p.getYCoord(), t.getXCoord(), t.getYCoord())) {
			pieces[p.getXCoord()][p.getYCoord()] = null;
			pieces[t.getXCoord()][t.getYCoord()] = p;
			p.setXCoord(t.getXCoord());
			p.setYCoord(t.getYCoord());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns true iff (x1,y1) and (x2,y2) are adjacent and the second is not
	 * full (piece or glass)
	 */
	public boolean canMove(int x1, int y1, int x2, int y2) {
		return arePlacesAdjacent(x1, y1, x2, y2)
				&& isTilePassable(x2, y2, this.pieces);
	}

	/**
	 * Returns true iff (x1,y1) is adjacent to (x2,y2)
	 */
	private boolean arePlacesAdjacent(int x1, int y1, int x2, int y2) {
		return (Math.abs(x1 - x2) == 1 & y1 == y2)
				| (Math.abs(y2 - y1) == 1 & x1 == x2);
	}

	/**
	 * Returns true iff tile at (x1, y1) doesn't have glass or a piece in it
	 */
	public boolean isTilePassable(int x, int y, Piece[][] pieces) {
		return !tiles[x][y].hasGlass() && pieces[x][y] == null;
	}

	/**
	 * Access the bottom left corner for drawing purposes
	 */
	public int getBotLeftX() {
		return botLeftX;
	}
	public int getBotLeftY() {
		return botLeftY;
	}

	/**
	 * Access the tile size for drawing purposes
	 */
	public int getTileSize() {
		return tileSize;
	}

	/**
	 * Access size of the board
	 */
	public int getNumHorizontalTiles() {
		return width;
	}
	public int getNumVerticalTiles() {
		return height;
	}

	/**
	 * Gets a tile at certain screen position, as from a click
	 * @param x, y
	 * 			The screen position
	 * @return
	 * 			The tile at that position, or null if off the board
	 */
	public Tile getTileAtClickPosition(int x, int y) {

		//Check if the tile is off the board
		if (x < botLeftX || x >= botLeftX + tileSize * width || y < botLeftY
				|| y >= botLeftY + tileSize * height) {
			return null;
		}

		//Otherwise compute and return the tile
		return tiles[(x - botLeftX) / tileSize][(y - botLeftY) / tileSize];
	}

	/**
	 * Get the tile at a certain board position. Does not check for out of bounds
	 */
	public Tile getTileAtBoardPosition(int x, int y) {
		return tiles[x][y];
	}

	/**
	 * Gets the piece on a given tile, possibly using a different piece set,
	 * for the convenience of the solver heuristic
	 */
	public Piece getPieceOnTile(Tile t) {
		return getPieceOnTile(t, this.pieces);
	}
	public Piece getPieceOnTile(Tile t, Piece[][] pieces) {
		return pieces[t.getXCoord()][t.getYCoord()];
	}
	
	public boolean isGlassAt(int x, int y){
		return tiles[x][y].hasGlass();
	}

	/**
	 * Gets whether the goal is met on a given tile, possibly
	 * using another piece set for convenience of the solver.
	 */
	public boolean isGoalMet(Tile t) {
		return isGoalMet(t, this.pieces);
	}
	public boolean isGoalMet(Tile t, Piece[][] pieces) {
		return getPieceOnTile(t, pieces) != null
				&& getPieceOnTile(t, pieces).getColor() == t.getGoalColor();
	}

	/**
	 * Gets the number of goals filled, possibly using another
	 * piece set for the convenience of the solver.
	 */
	public int getNumGoalsFilled() {
		return getNumGoalsFilled(this.pieces);
	}
	public int getNumGoalsFilled(Piece[][] pieces) {
		int goalsFilled = 0;
		for (Tile t : goalTiles) {
			if (isGoalMet(t, pieces)) {
				goalsFilled++;
			}
		}
		return goalsFilled;
	}

	/**
	 * Gets the number of goals UNfulfilled - the opposite of
	 * the number of goals filled.
	 */
	public int getNumGoalsUnfilled(Piece[][] pieces) {
		return goalTiles.size() - getNumGoalsFilled(pieces);
	}

	/**
	 * Determine the number of lasers of a certain color in the board
	 * 
	 * @param color
	 * 			The color to check for
	 * @return
	 * 			Number of lasers of that color
	 */
	public int getLaserCount(GameEngine.Color color) {
		int store = 0;
		for (Laser l : lasers) {
			if (l.getColor() == color || color == Color.NONE) {
				store++;
			}
		}
		return store;
	}

	/**
	 * Returns all pieces as a list for drawing purposes
	 */
	public List<Piece> getAllPieces() {
		List<Piece> result = new ArrayList<Piece>();
		for (Piece[] subarray : pieces) {
			for (Piece p : subarray) {
				if (p != null) {
					result.add(p);
				}
			}
		}
		return result;
	}

	/**
	 * Returns all tiles as a list for drawing purposes
	 */
	public List<Tile> getAllTiles() {
		List<Tile> result = new ArrayList<Tile>();
		for (Tile[] subarray : tiles) {
			for (Tile t : subarray) {
				if (t != null) {
					result.add(t);
				}
			}
		}
		return result;
	}

	/**
	 * Gets the number of goal tiles on this board
	 */
	public int getNumGoalTiles(){
		return goalTiles.size();
	}

	/**
	 * Returns true iff all goal squares are full of the correct color and all
	 * beam goals are met. Can be called with a separate piece set to check against
	 * a different set of pieces. Default call uses the board's pieces.
	 */
	public boolean isWon() {
		return isWon(this.pieces);
	}
	public boolean isWon(Piece[][] pieces) {
		if (getNumGoalsFilled(pieces) != goalTiles.size()) {
			return false;
		}
		for (Color c : getBeamObjectiveSet()) {
			if (getLaserCount(pieces, c) != getBeamObjectiveCount(c)) {
				return false;
			}
		}
		return true;
	}

	public int getLaserCount(Piece[][] pieces, Color c) {
		int sum = 0;
		int[] columnCounts = new int[pieces[0].length];
		for (int j = 0; j < columnCounts.length; j++) {
			columnCounts[j] = 0;
		}
		for (int i = 0; i < pieces.length; i++) {
			int rowCount = 0;
			for (int j = 0; j < pieces[0].length; j++) {
				Piece p = pieces[i][j];
				if (p != null && p.getColor() == c) {
					rowCount++;
					columnCounts[j]++;
				}
			}
			if (rowCount >= 2) {
				sum += rowCount - 1;
			}
		}
		for (int j = 0; j < columnCounts.length; j++) {
			if (columnCounts[j] >= 2) {
				sum += columnCounts[j] - 1;
			}
		}
		return sum;
	}

	/**
	 * For a board with n pieces, this method returns an n-element list of
	 * 2-byte shorts. This allows for up 15 colors and up to 63x63 boards.
	 */
	public List<Short> encodePieces() {
		ArrayList<Short> ret = new ArrayList<Short>();
		for (Piece[] arr : pieces) {
			for (Piece p : arr) {
				if (p == null)
					continue;
				short s = (short) (p.getColor().toIndex() & 0xf);
				s |= (p.getXCoord() & 0x3f) << 4;
				s |= (p.getYCoord() & 0x3f) << 4 + 6;
				ret.add(s);
			}
		}
		return ret;
	}

	/**
	 * Reverts the piece array to match a different state specified by the
	 * shorts in the provided list, presumably and output
	 * ofBoard.encodePieces(). Returns false if the encoding is not well formed
	 */
	public boolean resetPieces(Collection<Short> pieceList) {
		try {
			pieces = new Piece[width][height];
			for (short p : pieceList) {
				int x = (p & 0x03f0) >> 4;
				int y = (p & 0xfc00) >> 10;
				pieces[x][y] = new Piece(x, y, Color.lookup(p & 0xf));
			}
			return true;
		} catch (ArrayIndexOutOfBoundsException e) {
			GameEngine.debug("restPieces failed with exception:");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Determine whether the piece is the endpoint of a laser currently
	 * on the board.
	 * 
	 * @param p
	 * 			The piece to check
	 * @return
	 * 			True if the piece forms a laser, false otherwise
	 */
	public boolean isPiecePartOfBeam(Piece p) {
		for (Laser l : lasers) {
			if ((l.getXStart() == p.getXCoord() && l.getYStart() == p
					.getYCoord())
					|| (l.getXFinish() == p.getXCoord() && l.getYFinish() == p
					.getYCoord())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets whether the board is currently in a valid state.
	 * UNIMPLEMENTED
	 * 
	 * @return
	 * 			True if no piece is in a destroyed state, false otherwise
	 */
	public boolean validate() {

		//TODO IMPLEMENT

		return true;
	}

}
