package com.me.beam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Solver {
	private Map<Piece[][], Integer> table;
	private List<Piece[][]> searchQueue;
	private Board board;
	private boolean horizontalSymmetry;
	private boolean verticalSymmetry;
	private boolean solved;
	private Piece[][] solution;

	public Solver(Board board) {
		table = new HashMap<Piece[][], Integer>();
		this.board = board;
		setSymmetry();
		searchQueue = new ArrayList<Piece[][]>();
		this.solved = false;
		this.solution = null;
	}

	public int getMovesNeeded() {
		if (!solved) {
			solve();
		}
		return table.get(solution);
	}

	public Piece[][] getSolution() {
		if (!solved) {
			solve();
		}
		return solution;
	}

	private void solve() {
		addToQueue(board.getPieces(), 0);
		while (searchQueue.size() > 0 && !this.solved) {
			expand(searchQueue.remove(0));
		}
	}

	private void expand(Piece[][] pieces) {
		if (board.isWon(pieces)) {
			this.solved = true;
			this.solution = pieces;
			return;
		}
		
		Set<Piece[][]> possibleMoves = getAllMoves(pieces);
		int moves = 0;
		for (Piece[][] p : possibleMoves) {
			moves = table.get(p);
			addToQueue(p, moves);
		}
	}

	private void addToQueue(Piece[][] pieces, int moves) {
		if (table.get(pieces) == null) {
			searchQueue.add(pieces);
			setMovesToReach(pieces, moves);
		}
	}

	private void setSymmetry() {
		// TODO: support symmetry
		horizontalSymmetry = false;
		verticalSymmetry = false;
	}

	private Piece[][] reflectHorizontally(Piece[][] pieces) {
		// TODO: support symmetry
		return pieces;
	}

	private Piece[][] reflectVertically(Piece[][] pieces) {
		// TODO: support symmetry
		return pieces;
	}

	private Set<Piece[][]> getAllMoves(Piece[][] pieces) {
		Set<Piece[][]> newStates = new HashSet<Piece[][]>();
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				if (pieces[i][j] != null) {
					newStates.addAll(getMoves(pieces, pieces[i][j]));
				}
			}
		}
		return newStates;
	}

	private void setMovesToReach(Piece[][] pieces, int moves) {
		table.put(pieces, moves);
		if (horizontalSymmetry) {
			Piece[][] horizontallyReflectedPieces = reflectHorizontally(pieces);
			table.put(horizontallyReflectedPieces, moves);
			if (verticalSymmetry) {
				table.put(reflectVertically(horizontallyReflectedPieces), moves);
				table.put(reflectVertically(pieces), moves);
			}
		} else if (verticalSymmetry) {
			table.put(reflectVertically(pieces), moves);
		}
	}
}
