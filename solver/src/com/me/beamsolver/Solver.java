package com.me.beamsolver;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.me.beam.Board;
import com.me.beam.GameEngine.Color;
import com.me.beam.Laser;
import com.me.beam.LevelLoader;
import com.me.beam.LevelOrderer;
import com.me.beam.Piece;

public class Solver {

	private class QueueEntry implements Comparable<QueueEntry>{
		private Piece[][] pieces;
		private int moves;

		private QueueEntry(Piece[][] pieces, int moves) {
			this.pieces = pieces;
			this.moves = moves;
		}

		@Override
		public int compareTo(QueueEntry other) {
			if (!(other instanceof QueueEntry)) {
				System.err.println("QueueEntry compare failed.");
				return 0;
			}
			QueueEntry qe = (QueueEntry) other;
			return this.moves - qe.moves;
		}
	}

	private Map<String, Integer> table;
	private PriorityQueue<QueueEntry> searchQueue;
	private Board board;
	private boolean horizontalSymmetry;
	private boolean verticalSymmetry;
	private boolean solved;
	private Piece[][] solution;
	private int highestDepthPrinted;
	private int cutoffs;
	private long startTime;

	public static void main(String[] args) {
		LevelOrderer levelOrderer = new LevelOrderer(
				"../beam-android/assets/data/levels/levelOrder.txt", true);
		LevelLoader levelLoader = new LevelLoader(
				"../beam-android/assets/data/levels/levels.xml", levelOrderer,
				true);

		int ordinal = 36;
		int index = ordinal - 1;
		Board toSolve = levelLoader.getLevel(index);
		printPieces(toSolve.getPieces());
		System.out.println("Solving level " + ordinal);
		Solver solver = new Solver(toSolve);
		System.out.println("Moves: " + solver.getMovesNeeded());
	}

	public Solver(Board board) {
		table = new HashMap<String, Integer>();
		this.board = board;
		setSymmetry();
		searchQueue = new PriorityQueue<QueueEntry>();
		this.solved = false;
		this.solution = null;
		this.highestDepthPrinted = 0;
		this.cutoffs = 0;
	}

	public int getMovesNeeded() {
		if (!solved) {
			solve();
		}
		return safeGet(solution);
	}

	public Piece[][] getSolution() {
		if (!solved) {
			solve();
		}
		return solution;
	}

	private void solve() {
		addToQueue(board.getPieces(), 0);
		this.startTime = System.currentTimeMillis();
		while (searchQueue.size() > 0 && !this.solved) {
			QueueEntry qe = searchQueue.poll();
			expand(qe.pieces, qe.moves);
		}
	}

	private void expand(Piece[][] pieces, int searchDepth) {
		if (board.isWon(pieces)) {
			this.solved = true;
			this.solution = pieces;
			return;
		}

		// printBoard(pieces);
		Set<Piece[][]> possibleMoves = getAllMoves(pieces);
		if (searchDepth > this.highestDepthPrinted) {
			double timeToSolveSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			System.out.println("At least " + searchDepth + " moves with "
					+ table.size() + " positions evaluated and with "
					+ this.cutoffs + " cutoffs and took " + timeToSolveSeconds
					+ " seconds.");
			this.highestDepthPrinted = searchDepth;
			this.cutoffs = 0;
			this.startTime = System.currentTimeMillis();
		}
		int moves = safeGet(pieces);
		for (Piece[][] p : possibleMoves) {
			addToQueue(p, moves + 1);
		}
	}

	private static String piecesString(Piece[][] pieces) {
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

	private static String piecesStringDense(Piece[][] pieces) {
		String temp = "";
		int count = 0;
		for (int i = pieces[0].length - 1; i >= 0; i--) {
			for (int j = 0; j < pieces.length; j++) {
				if (pieces[j][i] == null) {
					count++;
				} else {
					if (count > 0) {
						temp += count;
						count = 0;
					}
					temp += pieces[j][i].toString();
				}
			}
		}
		return temp;
	}

	private static void printPieces(Piece[][] pieces) {
		System.out.println(piecesString(pieces));
	}

	private void addToQueue(Piece[][] pieces, int moves) {
		if (safeGet(pieces) == null) {
			int heuristic = board.getNumGoalsUnfilled(pieces);
			searchQueue.add(new QueueEntry(pieces, moves + heuristic));
			setMovesToReach(pieces, moves);
		} else {
			this.cutoffs++;
		}
	}

	private void setSymmetry() {
		horizontalSymmetry = isHSym();
		verticalSymmetry = isVSym();
		// */
		/*
		 * horizontalSymmetry = false; verticalSymmetry = false; //
		 */
	}

	private boolean isHSym() {
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		for (int x = 0; x < numH / 2; x++) {
			for (int y = 0; y < numV; y++) {
				if (!board.areTilesSimilar(x, y, numH - x - 1, y)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isVSym() {
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		for (int x = 0; x < numH; x++) {
			for (int y = 0; y < numV / 2; y++) {
				if (!board.areTilesSimilar(x, y, x, numV - y - 1)) {
					return false;
				}
			}
		}
		return true;
	}

	private Piece[][] reflectHorizontally(Piece[][] pieces) {
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		Piece[][] ret = new Piece[pieces.length][pieces[0].length];
		for (int x = 0; x < numH; x++) {
			for (int y = 0; y < numV; y++) {
				ret[x][y] = pieces[numH - x - 1][y];
			}
		}
		return ret;
	}

	private Piece[][] reflectVertically(Piece[][] pieces) {
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		Piece[][] ret = new Piece[pieces.length][pieces[0].length];
		for (int x = 0; x < numH; x++) {
			for (int y = 0; y < numV; y++) {
				ret[x][y] = pieces[x][numV - y - 1];
			}
		}
		return ret;
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

	private Integer safeGet(Piece[][] pieces) {
		return table.get(piecesStringDense(pieces));
	}

	private void safePut(Piece[][] pieces, int moves) {
		table.put(piecesStringDense(pieces), moves);
	}

	private void setMovesToReach(Piece[][] pieces, int moves) {
		safePut(pieces, moves);
		if (horizontalSymmetry) {
			Piece[][] horizontallyReflectedPieces = reflectHorizontally(pieces);
			safePut(horizontallyReflectedPieces, moves);
			if (verticalSymmetry) {
				safePut(reflectVertically(horizontallyReflectedPieces), moves);
				safePut(reflectVertically(pieces), moves);
			}
		} else if (verticalSymmetry) {
			safePut(reflectVertically(pieces), moves);
		}
	}
	
	private boolean isPlaceSafe(Piece[][] pieces, Piece p) {
		if (!(p.getXCoord() >= 0 && p.getXCoord() < this.board.getNumHorizontalTiles())) {
			return false;
		}
		if (!(p.getYCoord() >= 0 && p.getYCoord() < this.board.getNumVerticalTiles())) {
			return false;
		}
		if (!this.board.isTilePassable(p.getXCoord(), p.getYCoord(), pieces)) {
			return false;
		}
		if ((formLasersFromPieceAndDestroy(pieces, p).size() == 0)
				&& !checkIfPieceDestroyed(pieces, p)) {
			return true;
		}
		return false;
	}

	// Call with the current piece array and the piece to move,
	// this returns a set of new states (not including pieces) that
	// the board can now be in.
	private Set<Piece[][]> getMoves(Piece[][] pieces, Piece p) {
		// Temporarily remove p from the pieces.
		pieces[p.getXCoord()][p.getYCoord()] = null;
		Set<Point> moves = getContiguousPoints(pieces, p);

		Set<Piece[][]> moveStates = new HashSet<Piece[][]>();

		for (Point movePoint : moves) {
			Piece[][] copy = new Piece[board.getNumHorizontalTiles()][board
					.getNumVerticalTiles()];
			for (int i = 0; i < copy.length; i++) {
				for (int j = 0; j < copy[0].length; j++) {
					copy[i][j] = pieces[i][j];
				}
			}
			int x = movePoint.x;
			int y = movePoint.y;
			copy[x][y] = new Piece(x, y, p.getColor());
			moveStates.add(copy);
		}
		// Add p back to the pieces so there are no side effects.
		pieces[p.getXCoord()][p.getYCoord()] = p;
		return moveStates;
	}

	private Set<Point> getContiguousPoints(Piece[][] pieces, Piece p) {
		Set<Point> contiguousPoints = new HashSet<Point>();
		List<Point> searchQueue = new ArrayList<Point>();
		searchQueue.add(new Point(p.getXCoord(), p.getYCoord()));
		
		while (searchQueue.size() > 0) {
			Point tempPoint = searchQueue.remove(0);
			Piece up = new Piece(tempPoint.x, tempPoint.y + 1, p.getColor());
			Point up_point = new Point(tempPoint.x, tempPoint.y + 1);
			if (isPlaceSafe(pieces, up) && !contiguousPoints.contains(up_point)) {
				searchQueue.add(up_point);
				contiguousPoints.add(up_point);
			}
			Piece down = new Piece(tempPoint.x, tempPoint.y - 1, p.getColor());
			Point down_point = new Point(tempPoint.x, tempPoint.y - 1);
			if (isPlaceSafe(pieces, down) && !contiguousPoints.contains(down_point)) {
				searchQueue.add(down_point);
				contiguousPoints.add(down_point);
			}
			Piece left = new Piece(tempPoint.x - 1, tempPoint.y, p.getColor());
			Point left_point = new Point(tempPoint.x - 1, tempPoint.y);
			if (isPlaceSafe(pieces, left) && !contiguousPoints.contains(left_point)) {
				searchQueue.add(left_point);
				contiguousPoints.add(left_point);
			}
			Piece right = new Piece(tempPoint.x + 1, tempPoint.y, p.getColor());
			Point right_point = new Point(tempPoint.x + 1, tempPoint.y);
			if (isPlaceSafe(pieces, right) && !contiguousPoints.contains(right_point)) {
				searchQueue.add(right_point);
				contiguousPoints.add(right_point);
			}
		}
		return contiguousPoints;
	}

	private Color originalColor = Color.NONE;
	private static List<Laser> lasersCreated = new ArrayList<Laser>();

	public List<Piece> formLasersFromPieceAndDestroy(Piece[][] pieces, Piece p) {

		boolean horizontalMove = false;

		List<Piece> destroyed = new ArrayList<Piece>();

		// For each destruction
		List<Piece> possibleDestroy = new ArrayList<Piece>();

		// Check for left pieces
		Piece leftSameColor = null;

		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		Laser possibleFormed = null;

		// Slide to the left
		for (; leftSameColor == null && xPos >= 0; xPos--) {
			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(),
							p.getYCoord(), p.getColor());
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Piece rightSameColor = null;
		xPos = p.getXCoord() + 1;

		// Slide to the right
		for (; rightSameColor == null && xPos < pieces.length; xPos++) {

			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(),
							xPos, yPos, p.getColor());

				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (leftSameColor != null && rightSameColor != null) {
			possibleFormed = null;

		}

		// If it's still possible, it was formed
		if (possibleFormed != null
				&& (originalColor != p.getColor() || !horizontalMove)) {
			lasersCreated.add(possibleFormed);
		}
		possibleFormed = null;

		possibleDestroy.clear();

		// Now do vertical!

		// Check for bot pieces
		Piece botSameColor = null;

		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Slide down
		for (; botSameColor == null && yPos >= 0; yPos--) {
			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(),
							p.getYCoord(), p.getColor());

				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Piece topSameColor = null;

		yPos = p.getYCoord() + 1;

		// Slide up
		for (; topSameColor == null && yPos < pieces[0].length; yPos++) {

			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(),
							xPos, yPos, p.getColor());

				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (botSameColor != null && topSameColor != null) {
			possibleFormed = null;

		}
		// If it's still possible, it was formed
		if (possibleFormed != null
				&& (originalColor != p.getColor() || horizontalMove)) {
			lasersCreated.add(possibleFormed);
		}
		possibleFormed = null;

		return destroyed;
	}

	public boolean checkIfPieceDestroyed(Piece[][] pieces, Piece p) {

		// Check if p is destroyed. First, horizontally
		Color leftColor = Color.NONE;
		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		// Slide to left
		for (; leftColor == Color.NONE && xPos >= 0; xPos--) {

			Piece atLeft = pieces[xPos][yPos];

			if (atLeft != null) {
				leftColor = atLeft.getColor();
			}
		}

		if (leftColor != Color.NONE && leftColor != p.getColor()) {
			Color rightColor = Color.NONE;

			xPos = p.getXCoord() + 1;

			// Slide to the right
			for (; rightColor == Color.NONE && xPos < pieces.length; xPos++) {

				Piece atRight = pieces[xPos][yPos];

				if (atRight != null) {
					rightColor = atRight.getColor();
				}
			}
			// Criss cross
			if (leftColor == rightColor) {
				return true;
			}
		}

		// Now vertically
		Color topColor = Color.NONE;
		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Step up
		for (; topColor == Color.NONE && yPos >= 0; yPos--) {

			Piece atTop = pieces[xPos][yPos];

			if (atTop != null) {
				topColor = atTop.getColor();
			}
		}

		if (topColor != Color.NONE && topColor != p.getColor()) {
			Color botColor = Color.NONE;

			yPos = p.getYCoord() + 1;

			// Step down
			for (; botColor == Color.NONE && yPos < pieces[0].length; yPos++) {

				Piece atBot = pieces[xPos][yPos];

				if (atBot != null) {
					botColor = atBot.getColor();
				}
			}
			// Criss cross
			if (topColor == botColor) {
				return true;
			}
		}

		return false;
	}
}
