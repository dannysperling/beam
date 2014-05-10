package com.me.beamsolver;

import model.Board;
import model.Piece;

public class Arrangement {
	private Piece[][] pieces;
	private Piece maskedPiece;

	public Arrangement(Piece[][] ps) {
		this.pieces = new Piece[ps.length][ps[0].length];
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[0].length; j++) {
				Piece temp = ps[i][j];
				if (temp != null) {
					this.pieces[i][j] = new Piece(temp.getXCoord(),
						temp.getYCoord(), temp.getColor());
				}
			}
		}
	}
	
	public int getXSize() {
		return pieces.length;
	}
	
	public int getYSize() {
		return pieces[0].length;
	}
	
	public Piece getPiece(int x, int y) {
		return pieces[x][y];
	}
	
	public void mask(int x, int y) {
		if (pieces[x][y] == null || maskedPiece != null) {
			System.err.println("Error in Arrangement mask");
		}
		maskedPiece = pieces[x][y];
		pieces[x][y] = null;
	}
	
	public void unmask(int x, int y) {
		if (pieces[x][y] != null || maskedPiece == null) {
			System.err.println("Error in Arrangement unmask");
		}
		pieces[x][y] = maskedPiece;
		maskedPiece = null;
	}
	
	public Piece[][] getPieces() {
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
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[0].length; j++) {
				if (pieces[i][j] == null) {
					return a2.getPiece(i, j) == null;
				}
				if (!pieces[i][j].equals(a2.getPiece(i, j))) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer temp = new StringBuffer();
		for (int i = pieces[0].length - 1; i >= 0; i--) {
			for (int j = 0; j < pieces.length; j++) {
				if (pieces[j][i] == null) {
					temp.append("_");
				} else {
					temp.append(pieces[j][i].toString());
				}
			}
			temp.append("\n");
		}
		return temp.toString();
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public String toStringDense(Board board) {
		StringBuffer temp = new StringBuffer();
		int count = 0;
		for (int x = 0; x < pieces.length; x++) {
			for (int y = 0; y < pieces[0].length; y++) {
				if (board.getTileAtBoardPosition(x, y).hasGlass()) {
					continue;
				}
				if (pieces[x][y] == null) {
					count++;
				} else {
					if (count > 0) {
						temp.append(count);
						count = 0;
					}
					temp.append(pieces[x][y].toString());
				}
			}
		}
		return temp.toString();
	}
}
