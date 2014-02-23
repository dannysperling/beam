package com.me.beam;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

public class Board {
	
	private Tile[][] tiles;
	
	private Piece[][] pieces;
	
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
		
		pieces[2][2] = new Piece(2, 2, GameEngine.Color.RED);
		pieces[1][4] = new Piece(1, 4, GameEngine.Color.GREEN);
		pieces[8][6] = new Piece(8, 6, GameEngine.Color.BLUE);
		
		for (int i = 0; i < width; i++){
			for (int j = 0; j < height; j++){
				tiles[i][j] = new Tile(i, j);
			}
		}
		
		int screenWidth = (int) (Gdx.graphics.getWidth() * (1 - GameEngine.sideEmptySize*2));
		int screenHeight = (int) (Gdx.graphics.getHeight() * (1 - GameEngine.topBarSize - GameEngine.botBarSize));
		
		int maxWidth = (int) (screenWidth / width);
		int maxHeight = (int) (screenHeight / height);
		
		if (maxWidth < maxHeight){
			tileSize = maxWidth;
			botLeftX = (int) (Gdx.graphics.getWidth() * GameEngine.sideEmptySize);
			botLeftY = (int) (Gdx.graphics.getHeight() * GameEngine.botBarSize + (screenHeight - (tileSize * height))/2);
		} else {
			tileSize = maxHeight;
			botLeftX = (int) (Gdx.graphics.getWidth() * GameEngine.sideEmptySize + (screenWidth - (tileSize * width))/2);
			botLeftY = (int) (Gdx.graphics.getHeight() * GameEngine.botBarSize);
		}
		
	}
	
	public int getBotLeftX(){
		return botLeftX;
	}
	
	public int getBotLeftY(){
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
	
	public Tile getTileAtPosition(int x, int y){
		
		if (x < botLeftX || x >= botLeftX + tileSize * width ||
			y < botLeftY || y >= botLeftY + tileSize * height){
			return null;
		}
		
		return tiles[(x - botLeftX) / tileSize][(y - botLeftY) / tileSize];
	}
	
	public Piece getPieceOnTile(Tile t){
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

}
