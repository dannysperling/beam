package com.me.beam;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.me.beam.GameEngine.Color;

public class Board {

	private Tile[][] tiles;

	private Piece[][] pieces;
	
	public List<Laser> lasers = new ArrayList<Laser>();

	private int tileSize;

	private int botLeftX;
	private int botLeftY;

	private int width;
	private int height;

	public Board(int numHorTiles, int numVertTiles) {
		this.width = numHorTiles;
		this.height = numVertTiles;
		
		tiles = new Tile[width][height];
		pieces = new Piece[width][height];
		
		for (int i = 0; i < width; i++){
			for (int j = 0; j < height; j++){
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

	/**
	 * Sets tile (X,Y) to have a goal of colour c. Returns the previous goal
	 * colour. Suppying valid coordinates is YOUR PROBLEM NOT MINE.
	 * 
	 */
	public Color setGoal(int x, int y, Color c) {
		Color ret = tiles[x][y].getGoalColor();
		tiles[x][y].setGoal(c);
		return ret;
	}

	/**
	 * Sets tile (X,Y) to have a goal of colour c. Returns the previous goal
	 * colour. Suppying valid coordinates is YOUR PROBLEM NOT MINE.
	 * 
	 */
	public Color setPainter(int x, int y, Color c) {
		Color ret = tiles[x][y].getPainterColor();
		tiles[x][y].setPainter(c);
		return ret;
	}
	
	/**
	 * Sets the tile at (X,Y) to either be or not be glass, according to the
	 * input argument is Glass. Suppying valid coordinates is YOUR PROBLEM NOT MINE.
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
	 * Addes piece p to the board, returning the piece that was previously there or null.
	 * @param p
	 * @return
	 */
	public Piece put(Piece p){
		int x = p.getXCoord();
		int y = p.getYCoord();
		Piece ret = pieces[x][y];
		pieces[x][y] = p;
		return ret;
	}
	
	public void removePiece(Piece p){
		pieces[p.getXCoord()][p.getYCoord()] = null;
	}

	/**
	 * This the thing that actually physically moves the piece, assuming you try
	 *  and move it to an adjacent empty non-glass tile.
	 * @return Sucess or failure
	 */
	public boolean move(Piece p, Tile t){
		if (canMove(p.getXCoord(), p.getYCoord(),t.getXCoord(),t.getYCoord())){
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
	 * Returns true iff (x1,y1) and (x2,y2) are adjacent and the second is not full (piece or glass)
	 */
	public boolean canMove(int x1, int y1, int x2, int y2){
		boolean ret = true;
		ret &= (Math.abs(x1-x2)==1 & y1==y2) | (Math.abs(y2-y1)==1 & x1==x2);
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

	public int getNumHorizontalTiles(){
		return width;
	}

	public int getNumVerticalTiles(){
		return height;
	}
	
	public int getTileSize(){
		return tileSize;
	}
	
	//rename to getTileAtClickPosition
	public Tile getTileAtPosition(int x, int y){
		
		if (x < botLeftX || x >= botLeftX + tileSize * width ||
			y < botLeftY || y >= botLeftY + tileSize * height){
			return null;
		}

		return tiles[(x - botLeftX) / tileSize][(y - botLeftY) / tileSize];
	}
	
	public Tile getTileAtBoardPosition(int x, int y){
		return tiles[x][y];
	}

	public Piece getPieceOnTile(Tile t) {
		return pieces[t.getXCoord()][t.getYCoord()];
	}
	
	public List<Piece> getAllPieces(){
		List<Piece> result = new ArrayList<Piece>();
		for (Piece[] subarray: pieces) {
		    for (Piece p: subarray) {
		        if(p != null){
		        	result.add(p);
		        }
		    }
		}
		return result;
	}
	
	public List<Tile> getAllTiles(){
		List<Tile> result = new ArrayList<Tile>();
		for (Tile[] subarray: tiles) {
		    for (Tile t: subarray) {
		        if(t != null){
		        	result.add(t);
		        }
		    }
		}
		return result;
	}

}
