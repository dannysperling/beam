package com.me.beam;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.me.beam.GameEngine.Color;

public class Solver {
	private Map<Piece[][], Integer> table;
	private List<Piece[][]> searchQueue;
	private Board board;
	private boolean horizontalSymmetry;
	private boolean verticalSymmetry;
	private boolean solved;
	private Piece[][] solution;
	private GameEngineForSolver solverEngine;

	public static void main(String[] args) {
		/*
		 * int numToSolve = 37; String level =
		 * "<level id=37 par=2 perfect=1>\n<attribution name=\"Moving and goals\" "
		 * + "author=\"John\"/>1,e,e\ne,e,e\ne,e,goal_1\n</level>\n";
		 * 
		 * 
		 * LevelLoader levelLoader = new LevelLoader(level, numToSolve);
		 */
		LevelOrderer levelOrderer = new LevelOrderer("C:\\Users/Douglas/workspace/Mildly-Offensive-Entertainment/beam/src/com/me/beam/levelOrder.txt", true);
		LevelLoader levelLoader = new LevelLoader("C:\\Users/Douglas/workspace/Mildly-Offensive-Entertainment/beam/src/com/me/beam/levels.xml", levelOrderer, true);

		Board toSolve = levelLoader.getLevel(1);
		GameEngineForSolver solverEngine = new GameEngineForSolver();
		Solver solver = new Solver(toSolve, solverEngine);
		System.out.println(solver.getMovesNeeded());
	}

	public Solver(Board board, GameEngineForSolver solverEngine) {
		table = new HashMap<Piece[][], Integer>();
		this.board = board;
		setSymmetry();
		searchQueue = new ArrayList<Piece[][]>();
		this.solved = false;
		this.solution = null;
		this.solverEngine = solverEngine;
	}

	public int getMovesNeeded() {
		if (!solved) {
			solve();
		}
		System.out.println(table.get(solution) == null);
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
		int moves = table.get(pieces);
		for (Piece[][] p : possibleMoves) {
			printBoard(p);
			addToQueue(p, moves + 1);
		}
	}
	
	private void printBoard(Piece[][] pieces) {
		for(int i = 0; i < pieces[0].length; i++) {
			for(int j = pieces.length - 1; j >= 0; j--) {
				System.out.print(pieces[i][j]);
			}
			System.out.println();
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

	private Set<Point> getSafePlaces(Piece[][] pieces, Color color) {
		// TODO: assumes that painters don't exist
		Set<Point> ret = new HashSet<Point>();
		for (int i = 0; i < this.board.getNumHorizontalTiles(); i++) {
			for (int j = 0; j < this.board.getNumVerticalTiles(); j++) {
				if (!this.board.isTilePassable(i, j, pieces)) {
					continue;
				}
				if (solverEngine.formLasersFromPieceAndDestroy(this.board,
						new Piece(i, j, color)).size() == 0) {
					ret.add(new Point(i, j));
				}
			}
		}
		return ret;
	}

	// Call with the current piece array and the piece to move,
	// this returns a set of new states (not including pieces) that
	// the board can now be in.
	private Set<Piece[][]> getMoves(Piece[][] pieces, Piece p) {
		// Temporarily remove p from the pieces.
		pieces[p.getXCoord()][p.getYCoord()] = null;
		Set<Point> moves = getContiguousPoints(
				getSafePlaces(pieces, p.getColor()),
				new Point(p.getXCoord(), p.getYCoord()));

		Set<Piece[][]> moveStates = new HashSet<Piece[][]>();

		for (Point movePoint : moves) {
			Piece[][] copy = pieces.clone();
			int x = movePoint.x;
			int y = movePoint.y;
			copy[x][y] = new Piece(x, y, p.getColor());
			moveStates.add(copy);
		}
		// Add p back to the pieces so there are no side effects.
		pieces[p.getXCoord()][p.getYCoord()] = p;
		return moveStates;
	}

	private Set<Point> getContiguousPoints(Set<Point> points, Point p) {
		Set<Point> contiguousPoints = new HashSet<Point>();
		List<Point> searchQueue = new ArrayList<Point>();
		searchQueue.add(p);

		while (searchQueue.size() > 0) {
			Point tempPoint = searchQueue.remove(0);
			Point up = new Point(tempPoint.x, tempPoint.y + 1);
			if (points.contains(up) && !contiguousPoints.contains(up)) {
				searchQueue.add(up);
				contiguousPoints.add(up);
			}
			Point down = new Point(tempPoint.x, tempPoint.y - 1);
			if (points.contains(down) && !contiguousPoints.contains(down)) {
				searchQueue.add(down);
				contiguousPoints.add(down);
			}
			Point right = new Point(tempPoint.x + 1, tempPoint.y);
			if (points.contains(right) && !contiguousPoints.contains(right)) {
				searchQueue.add(right);
				contiguousPoints.add(right);
			}
			Point left = new Point(tempPoint.x - 1, tempPoint.y);
			if (points.contains(left) && !contiguousPoints.contains(left)) {
				searchQueue.add(left);
				contiguousPoints.add(left);
			}
		}
		return contiguousPoints;
	}
}
