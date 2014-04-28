package com.me.beamsolver;

import java.util.Map;

import model.Board;
import model.Piece;

public class Arrangement {
	private Piece[][] pieces;

	public Arrangement(Piece[][] ps) {
		this.pieces = new Piece[ps.length][ps[0].length];
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[0].length; j++) {
				Piece temp = ps[i][j];
				this.pieces[i][j] = new Piece(temp.getXCoord(),
						temp.getYCoord(), temp.getColor());
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
				if (!pieces[i][j].equals(a2.getPiece(i, j))) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		String temp = "";
		for (int i = pieces[0].length - 1; i >= 0; i--) {
			for (int j = 0; j < pieces.length; j++) {
				if (pieces[j][i] == null) {
					temp += "_";
				} else {
					temp += pieces[j][i].toString();
				}
			}
			temp += "\n";
		}
		return temp;
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
	
	private void checkSolution(Map<String, Integer> table) {
		System.out.println("0 : " + table.get("G5G1BB2BGR2RR1G5G"));
		System.out.println("1 : " + table.get("G7BBG1BGR2RR1G5G"));
		System.out.println("2 : " + table.get("G1B5B1G1BGR2RR1G5G"));
		System.out.println("3 : " + table.get("G1B5BG2BGR2RR1G5G"));
		System.out.println("4 : " + table.get("G1B1R3BG2BGR2R2G5G"));
		System.out.println("5 : " + table.get("G1B1R3BG2BGR2RG1G"));
		System.out.println("6 : " + table.get("G1B1R1R1BG2BGR3G1G"));
	}
}
