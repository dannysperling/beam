package com.me.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.me.beam.GameEngine.Color;

public class Board {

	private Tile[][] tiles;

	private Piece[][] pieces;

	public Set<Laser> lasers = new HashSet<Laser>();

	public List<Tile> goalTiles = new ArrayList<Tile>();

	public int id; // ID in its levels file
	public int par; // Good solution
	public int perfect; //Optimal solution

	private EnumMap<Color, Integer> beamObjectives = new EnumMap<Color, Integer>(
			Color.class);

	private int tileSize;

	private int botLeftX;
	private int botLeftY;

	// In tiles:
	private int width;
	private int height;

	public Board(int numHorTiles, int numVertTiles) {
		this.width = numHorTiles;
		this.height = numVertTiles;

		tiles = new Tile[width][height];
		pieces = new Piece[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tiles[i][j] = new Tile(i, j);
			}
		}

		int screenWidth = (int) (Gdx.graphics.getWidth() * (1 - GameEngine.sideEmptySize * 2));
		int screenHeight = (int) (Gdx.graphics.getHeight() * (1 - GameEngine.topBarSize - GameEngine.botBarSize));

		int maxWidth = (int) (screenWidth / width);
		int maxHeight = (int) (screenHeight / height);

		if (maxWidth < maxHeight) {
			tileSize = maxWidth;
			botLeftX = (int) (Gdx.graphics.getWidth() * GameEngine.sideEmptySize);
			botLeftY = (int) (Gdx.graphics.getHeight() * GameEngine.botBarSize + (screenHeight - (tileSize * height)) / 2);
		} else {
			tileSize = maxHeight;
			botLeftX = (int) (Gdx.graphics.getWidth()
					* GameEngine.sideEmptySize + (screenWidth - (tileSize * width)) / 2);
			botLeftY = (int) (Gdx.graphics.getHeight() * GameEngine.botBarSize);
		}

	}

	public Board(Tile[][] t, Piece[][] p, int id, int perfect, int par) {
		this(t.length, t[0].length);
		this.tiles = t;
		this.pieces = p;
		this.id = id;
		this.perfect = perfect;
		this.par = par;

		for (int i = 0; i < t.length; i++) {
			for (int j = 0; j < t[i].length; j++) {
				if (this.tiles[i][j].hasGoal()) {
					goalTiles.add(this.tiles[i][j]);
				}
			}
		}
	}

	public void addBeamObjective(Color c, int n) {
		beamObjectives.put(c, n);
	}

	public Set<Color> getBeamObjectiveSet() {
		return beamObjectives.keySet();
	}

	public int getBeamObjectiveCount(Color c) {
		return beamObjectives.get(c);
	}
	
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
		boolean ret = tiles[x][y].isGlass;
		tiles[x][y].isGlass = isGlass;
		return ret;
	}

	/**
	 * Addes piece p to the board, returning the piece that was previously there
	 * or null.
	 * 
	 * @param p
	 * @return
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
	 * @param p The piece to remove
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
	 * @return Success or failure
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
		boolean ret = true;
		ret &= (Math.abs(x1 - x2) == 1 & y1 == y2)
				| (Math.abs(y2 - y1) == 1 & x1 == x2);
		ret &= !tiles[x2][y2].isGlass;
		ret &= pieces[x2][y2] == null;
		return ret;
	}

	public int getBotLeftX() {
		return botLeftX;
	}

	public int getBotLeftY() {
		return botLeftY;
	}

	public int getNumHorizontalTiles() {
		return width;
	}

	public int getNumVerticalTiles() {
		return height;
	}

	public int getTileSize() {
		return tileSize;
	}

	public Tile getTileAtClickPosition(int x, int y) {

		if (x < botLeftX || x >= botLeftX + tileSize * width || y < botLeftY
				|| y >= botLeftY + tileSize * height) {
			return null;
		}

		return tiles[(x - botLeftX) / tileSize][(y - botLeftY) / tileSize];
	}

	public Tile getTileAtBoardPosition(int x, int y) {
		return tiles[x][y];
	}

	public Piece getPieceOnTile(Tile t) {
		return getPieceOnTile(t, this.pieces);
	}
	
	public Piece getPieceOnTile(Tile t, Piece[][] pieces) {
		return pieces[t.getXCoord()][t.getYCoord()];
	}

	public boolean isGoalMet(Tile t) {
		return isGoalMet(t, this.pieces);
	}
	
	public boolean isGoalMet(Tile t, Piece[][] pieces) {
		return getPieceOnTile(t, pieces) != null
				&& getPieceOnTile(t, pieces).getColor() == t.getGoalColor();
	}

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

	public int getLaserCount(GameEngine.Color C) {
		int store = 0;
		for (Laser l : lasers) {
			if (l.getColor() == C) {
				store++;
			}
		}
		return store;
	}

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
	
	public boolean isWon() {
		return isWon(this.pieces);
	}
	
	public boolean isWon(Piece[][] pieces) {
		if (getNumGoalsFilled(pieces) != goalTiles.size()) {
			return false;
		}
		//TODO: add support to solver for laser objective levels
		for (Color c : getBeamObjectiveSet()) {
			if (getLaserCount(c) != getBeamObjectiveCount(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * For a board with n pieces, this method returns an n-element list of
	 * 2-byte shorts. This allows for up 15 colors and up to 63x63 boards.
	 * 
	 * @return
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
	 * Returns the representation of the board in a minimal encoding, intended
	 * for the undo/redo stack. Each board position is stored in 2 bytes,
	 * allowing for up to 31 colors and for painters on goals.
	 * 
	 * @return A 2-D array of shorts. A short is 16 bits.
	 */
	@Deprecated
	public short[][] toMinimalEncoding() {
		short[][] ret = new short[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ret[x][y] = 0;
				if (tiles[x][y].isGlass) {
					ret[x][y] = (short) 1;
					continue;
				}
				if (pieces[x][y] != null) {
					ret[x][y] |= ((pieces[x][y].getColor().toIndex() & 0x1f) << 1);
				}
				if (tiles[x][y].hasGoal()) {
					ret[x][y] |= ((tiles[x][y].getGoalColor().toIndex() & 0x1f) << 6);
				}
				if (tiles[x][y].hasPainter()) {
					ret[x][y] |= ((tiles[x][y].getPainterColor().toIndex() & 0x1f) << 11);
				}
			}
		}
		return ret;
	}

	// find out if theres a beam at a piece
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
	 * Takes an existing board, leaving metadata (par, id, beamGoal) untouched
	 * and resets the state of the pieces and tiles to the encoded state, which
	 * I just realized encodes way too much information.
	 * 
	 * @param ecodedStated
	 *            - The state of the pieces and tiles as generated by
	 *            toMinimalEncoding
	 */
	@Deprecated
	public void revertToState(short[][] ecodedStated) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Tile t = new Tile(x, y);
				short s = ecodedStated[x][y];
				if ((s & 1) == 1) {
					t.isGlass = true;
				} else {
					if ((s & 0x003e) != 0) {
						pieces[x][y] = new Piece(x, y,
								Color.lookup((s & 0xe) >> 1));
					} else {
						pieces[x][y] = null;
					}
					if ((s & 0x07c0) != 0) {
						t.setGoal(Color.lookup((s & 0x0070) >> 6));
					}
					if ((s & 0xf800) != 0) {
						t.setPainter(Color.lookup((s & 0xf800) >> 11));
					}
				}
				tiles[x][y] = t;
			}
		}
	}

}
