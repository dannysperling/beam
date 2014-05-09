package com.me.beamsolver;

import controller.GameEngine.Color;
import model.Board;
import model.Piece;

public class Arrangement {
	private Color[][] colors;
	private static Piece[][] pieces;

	public Arrangement(Piece[][] ps) {
		this.colors = new Color[ps.length][ps[0].length];
		for (int i = 0; i < ps.length; i++) {
			for (int j = 0; j < ps[0].length; j++) {
				Piece temp = ps[i][j];
				if (temp != null) {
					this.colors[i][j] = ps[i][j].getColor();
				}
			}
		}
		if (pieces == null) {
			pieces = new Piece[ps.length][ps[0].length];
		}
	}
	
	public Arrangement(Color[][] colors) {
		this.colors = new Color[colors.length][colors[0].length];
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[0].length; j++) {
				this.colors[i][j] = colors[i][j];
			}
		}
		if (pieces == null) {
			pieces = new Piece[colors.length][colors[0].length];
		}
	}
	
	private Arrangement(Arrangement copy, int maskedX, int maskedY) {
		this.colors = new Color[copy.getXSize()][copy.getYSize()];
		for (int i = 0; i < copy.getXSize(); i++) {
			for (int j = 0; j < copy.getYSize(); j++) {
				if (i != maskedX && j != maskedY) {
					this.colors[i][j] = copy.colors[i][j];
				}
			}
		}
		if (pieces == null) {
			pieces = new Piece[copy.getXSize()][copy.getYSize()];
		}
	}
	
	public int getXSize() {
		return colors.length;
	}
	
	public int getYSize() {
		return colors[0].length;
	}
	
	public Color getColorAt(int x, int y) {
		if (colors[x][y] == null) {
			return null;
		}
		return colors[x][y];
	}
	
	public Piece getPieceAt(int x, int y) {
		if (colors[x][y] == null) {
			return null;
		}
		return new Piece(x, y, colors[x][y]);
	}
	
	public Arrangement mask(int x, int y) {
		if (colors[x][y] == null) {
			System.err.println("Error in Arrangement mask");
		}
		return new Arrangement(this, x, y);
	}
	
	Piece[][] getPieces() {
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[0].length; j++) {
				if (colors[i][j] != null) {
					pieces[i][j] = new Piece(i, j, colors[i][j]);
				} else {
					pieces[i][j] = null;
				}
			}
		}
		return pieces;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Arrangement)) {
			return false;
		}
		Arrangement a2 = (Arrangement) other;
		if (this.getXSize() != a2.getXSize()) {
			return false;
		}
		if (this.getYSize() != a2.getYSize()) {
			return false;
		}
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[0].length; j++) {
				if (colors[i][j] == null) {
					return a2.getColorAt(i, j) == null;
				}
				if (colors[i][j] != a2.getColorAt(i, j)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private String getColorString(Color c) {
		if(c == Color.ORANGE) {
			return "O";
		} else if(c == Color.PURPLE) {
			return "P";
		} else if(c == Color.BLUE) {
			return "B";
		} else if(c == Color.GREEN){
			return "G";
		} else if(c == Color.BLACK){
			return "L";
		} else {
			return "-";
		}
	}
	
	@Override
	public String toString() {
		String temp = "";
		for (int i = colors[0].length - 1; i >= 0; i--) {
			for (int j = 0; j < colors.length; j++) {
				if (colors[j][i] == null) {
					temp += "_";
				} else {
					temp += getColorString(colors[j][i]);
				}
			}
			temp += "\n";
		}
		return temp;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public String toStringDense(Board board) {
		StringBuffer temp = new StringBuffer();
		int count = 0;
		for (int x = 0; x < colors.length; x++) {
			for (int y = 0; y < colors[0].length; y++) {
				if (board.getTileAtBoardPosition(x, y).hasGlass()) {
					continue;
				}
				if (colors[x][y] == null) {
					count++;
				} else {
					if (count > 0) {
						temp.append(count);
						count = 0;
					}
					temp.append(getColorString(colors[x][y]));
				}
			}
		}
		return temp.toString();
	}
}
