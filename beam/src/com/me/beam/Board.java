package com.me.beam;

public class Board {
	
	private Tile[][] staticObjects;
	
	private Piece[][] pieces;
	
	public Board(int width, int height) {
		staticObjects = new Tile[width][height];
		pieces = new Piece[width][height];
		
		for (int i = 0; i < width; i++){
			for (int j = 0; j < height; j++){
				staticObjects[i][j] = new Tile(i, j);
			}
		}
	}

}
