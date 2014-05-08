package com.me.beamsolver;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import model.Board;
import model.Piece;
import model.Tile;
import controller.LevelLoader;
import controller.LevelOrderer;
import controller.GameEngine.Color;

public class Solver {

	private class QueueEntry implements Comparable<QueueEntry> {
		private Arrangement arrangement;
		private int moves;

		private QueueEntry(Arrangement arrangement, int moves) {
			this.arrangement = arrangement;
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
	private final boolean horizontalSymmetry;
	private final boolean verticalSymmetry;
	private boolean solved;
	private Arrangement originalArrangement;
	private Arrangement solution;
	private int highestDepthPrinted;
	private int cutoffs;
	private int positionsInQueue;
	private int positionsExpandedThisDepth;
	private long startTime;
	private static final int moveIntervalStart = 10000;
	private static final int moveInterval = 10000;
	private Set<Piece> searchesCompleted; // for painters
	private List<Piece> blockingPieces;
	private boolean showMonitor;

	public static void main(String[] args) {

		// Neither the level orderer nor loader should use GDX in this
		// application
		LevelOrderer levelOrderer = new LevelOrderer(
				"../beam-android/assets/data/levels/levelOrder.txt", false);
		LevelLoader levelLoader = new LevelLoader(
				"../beam-android/assets/data/levels/levels.xml", levelOrderer,
				false);
		
		int world = 11;
		int ordinalInWorld = 14;
		Board toSolve = levelLoader.getLevel(world, ordinalInWorld);
		Solver solver = new Solver(toSolve, true);
		System.out.println("Solving level " + world + "-" + ordinalInWorld);
		solver.solve();
		System.out.println("Moves: " + solver.getMovesNeeded());
		solver.printSolutionTrace();
	}

	public Solver(Board board, boolean showMonitor) {
		table = new HashMap<String, Integer>();
		this.showMonitor = showMonitor;
		this.board = board;
		horizontalSymmetry = isHSym();
		verticalSymmetry = isVSym();
		searchQueue = new PriorityQueue<QueueEntry>();
		this.solved = false;
		this.solution = null;
		this.highestDepthPrinted = 0;
		this.cutoffs = 0;
		this.positionsInQueue = 0;
		this.positionsExpandedThisDepth = 1;
		initializeBlockedPieces();
		if (this.showMonitor) {
			for (Piece p : blockingPieces) {
				System.out.println(p.getXCoord() + ", " + p.getYCoord() + ", " + p.getColor());
			}
		}
	}

	public int getMovesNeeded() {
		if (!solved) {
			solve();
		}
		if (solution == null) {
			return -1;
		}
		return getMovesToReach(solution);
	}

	public Arrangement getSolution() {
		if (!solved) {
			solve();
		}
		return solution;
	}

	public void printSolutionTrace() {
		if (!solved) {
			solve();
		}
		if (solution == null) {
			return;
		}
		List<Arrangement> solutionTrace = new ArrayList<Arrangement>();
		solutionTrace.add(solution);
		Arrangement pieces = solution;
		for (int moves = getMovesNeeded() - 1; moves > 0; moves--) {
			Set<Arrangement> possibleMoves = getAllMoves(pieces);
			for (Arrangement move : possibleMoves) {
				Integer temp = getMovesToReach(move);
				if (temp == null) {
					continue;
				}
				if (temp == moves) {
					solutionTrace.add(move);
					pieces = move;
					break;
				}
			}
		}
		solutionTrace.add(originalArrangement);
		for (int i = solutionTrace.size() - 1; i >= 0; i--) {
			System.out.println("Move: " + (solutionTrace.size() - 1 - i));
			System.out.println(solutionTrace.get(i));
		}
	}

	public void solve() {
		this.originalArrangement = new Arrangement(board.getPieces());
		addToQueue(originalArrangement, 0);
		this.startTime = System.currentTimeMillis();
		while (searchQueue.size() > 0 && !this.solved) {
			QueueEntry qe = searchQueue.poll();
			if (this.showMonitor) {
				monitor(qe.moves);
			}
			expand(qe.arrangement, qe.moves);
		}
	}

	private void expand(Arrangement arrangement, int searchDepth) {
		if (board.isWon(arrangement.getPieces())) {
			this.solved = true;
			this.solution = arrangement;
			return;
		}

		// printBoard(pieces);
		Set<Arrangement> possibleMoves = getAllMoves(arrangement);
		int moves = getMovesToReach(arrangement);
		for (Arrangement a : possibleMoves) {
			addToQueue(a, moves + 1);
		}
	}

	private void monitor(int searchDepth) {
		this.positionsExpandedThisDepth++;
		int countdown = this.positionsInQueue - this.positionsExpandedThisDepth;
		if (this.positionsInQueue > moveIntervalStart
				&& countdown % moveInterval == 0) {
			System.out.println(countdown / moveInterval + "\t" + moveInterval
					+ "s more to go.");
		}
		if (searchDepth > this.highestDepthPrinted) {
			double timeToSolveSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			this.positionsInQueue = searchQueue.size();
			System.out.println("At least " + searchDepth + " moves.");
			System.out.println("\t" + table.size() + " positions evaluated.");
			System.out.println("\t" + this.cutoffs + " cutoffs.");
			System.out
					.println("\t" + positionsInQueue + " positions in queue.");
			System.out.println("\t" + timeToSolveSeconds + " seconds.");
			this.highestDepthPrinted = searchDepth;
			this.cutoffs = 0;
			this.positionsExpandedThisDepth = 1;
			this.startTime = System.currentTimeMillis();
		}
	}

	private void addToQueue(Arrangement arrangement, int moves) {
		if (getMovesToReach(arrangement) == null) {
			searchQueue.add(new QueueEntry(arrangement, moves + heuristic(arrangement)));
			setMovesToReach(arrangement, moves);
		} else {
			this.cutoffs++;
		}
	}

	private int heuristic(Arrangement arrangement) {
		int heuristic = board.getNumGoalsUnfilled(arrangement.getPieces());

		for (Piece blockingPiece : blockingPieces) {
			//heuristic += encourageNotPiece(arrangement, blockingPiece);
		}

		if (heuristic > 0) {
			return heuristic;
		}

		for (Color c : board.getBeamObjectiveSet()) {
			int laserCount = board.getLaserCount(arrangement.getPieces(), c);
			int objective = board.getBeamObjectiveCount(c);
			int absDiff = Math.abs(laserCount - objective);
			heuristic += (int) Math.ceil(absDiff / 2.0);
		}

		return heuristic;
	}

	private void initializeBlockedPieces() {
		Set<Color> blockingColors = getBlockingColors(board.getPieces());
		Arrangement fantasyPieces = getFantasyPieces();
		this.blockingPieces = new ArrayList<Piece>();
		
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		for (int x = 0; x < numH; x++) {
			for (int y = 0; y < numV; y++) {
				for (Color c : blockingColors) {
					Piece p = new Piece(x, y, c);
					Tile t = board.getTileAtBoardPosition(x, y);
					if (t.hasGlass()) {
						continue;
					}
					if (!this.isPlaceSafe(fantasyPieces, p)) {
						blockingPieces.add(p);
					}
				}
			}
		}
	}

	private Set<Color> getInvolvedColors(Piece[][] pieces) {
		Set<Color> involvedColors = new HashSet<Color>();
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[0].length; j++) {
				if (pieces[i][j] != null) {
					involvedColors.add(pieces[i][j].getColor());
				}
				Tile t = board.getTileAtBoardPosition(i, j);
				if (t.hasPainter()) {
					involvedColors.add(t.getPainterColor());
				}
			}
		}
		return involvedColors;
	}

	private Set<Color> getBlockingColors(Piece[][] pieces) {
		Set<Color> ret = getInvolvedColors(pieces);
		ret.removeAll(getGoalColors());
		return ret;
	}

	private Set<Color> getGoalColors() {
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		Set<Color> objectiveColors = new HashSet<Color>();
		for (int i = 0; i < numH; i++) {
			for (int j = 0; j < numV; j++) {
				Tile t = board.getTileAtBoardPosition(i, j);
				if (t.hasGoal()) {
					objectiveColors.add(t.getGoalColor());
				}
			}
		}
		objectiveColors.addAll(board.getBeamObjectiveSet());
		return objectiveColors;
	}

	private Arrangement getFantasyPieces() {
		int numH = board.getNumHorizontalTiles();
		int numV = board.getNumVerticalTiles();
		Piece[][] pieces = new Piece[numH][numV];
		for (int i = 0; i < numH; i++) {
			for (int j = 0; j < numV; j++) {
				Tile t = board.getTileAtBoardPosition(i, j);
				if (t.hasGoal()) {
					pieces[i][j] = new Piece(i, j, t.getGoalColor());
				}
			}
		}
		return new Arrangement(pieces);
	}

	// returns 1 if the piece is the color (which it shouldn't be)
	private int encourageNotPiece(Arrangement arrangement, Piece p) {
		return (arrangement.getPiece(p.getXCoord(), p.getYCoord()) != null && 
				arrangement.getPiece(p.getXCoord(), p.getYCoord()).getColor() == p.getColor()) ? 1
				: 0;
	}

	@SuppressWarnings("unused")
	private int countPiecesOfColor(Arrangement arrangement, Color color) {
		int count = 0;
		for (int i = 0; i < arrangement.getXSize(); i++) {
			for (int j = 0; j < arrangement.getYSize(); j++) {
				if (arrangement.getPiece(i, j) != null && arrangement.getPiece(i, j).getColor() == color) {
					count++;
				}
			}
		}
		return count;
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

	private Arrangement reflectHorizontally(Arrangement arrangement) {
		Piece[][] ret = new Piece[arrangement.getXSize()][arrangement.getYSize()];
		for (int x = 0; x < arrangement.getXSize(); x++) {
			for (int y = 0; y < arrangement.getYSize(); y++) {
				ret[x][y] = arrangement.getPiece(arrangement.getXSize() - x - 1, y);
			}
		}
		return new Arrangement(ret);
	}

	private Arrangement reflectVertically(Arrangement arrangement) {		
		Piece[][] ret = new Piece[arrangement.getXSize()][arrangement.getYSize()];
		for (int x = 0; x < arrangement.getXSize(); x++) {
			for (int y = 0; y < arrangement.getYSize(); y++) {
				ret[x][y] = arrangement.getPiece(x, arrangement.getYSize() - y - 1);
			}
		}
		return new Arrangement(ret);
	}

	private Set<Arrangement> getAllMoves(Arrangement arrangement) {
		Set<Arrangement> newStates = new HashSet<Arrangement>();
		for (int i = 0; i < arrangement.getXSize(); i++) {
			for (int j = 0; j < arrangement.getYSize(); j++) {
				if (arrangement.getPiece(i, j) != null) {
					Piece p = arrangement.getPiece(i, j);

					// To avoid exponential explosion on painter levels,
					// we'll store what pieces have been searched on:
					this.searchesCompleted = new HashSet<Piece>();

					// Temporarily remove p from the pieces.
					Arrangement temp = arrangement.mask(p.getXCoord(), p.getYCoord());

					newStates.addAll(getMoves(temp, p, false));
				}
			}
		}
		return newStates;
	}

	private Integer safeGet(Arrangement arrangement) {
		return table.get(arrangement.toStringDense(board));
	}

	private void safePut(Arrangement arrangement, int moves) {
		table.put(arrangement.toStringDense(board), moves);
	}

	private Integer getMovesToReach(Arrangement arrangment) {
		Integer ret = safeGet(arrangment);
		if (ret != null)
			return ret;
		if (horizontalSymmetry) {
			Arrangement horizontallyReflectedPieces = reflectHorizontally(arrangment);
			ret = safeGet(horizontallyReflectedPieces);
			if (ret != null)
				return ret;
			if (verticalSymmetry) {
				ret = safeGet(reflectVertically(horizontallyReflectedPieces));
				if (ret != null)
					return ret;
				ret = safeGet(reflectVertically(arrangment));
				if (ret != null)
					return ret;
			}
		} else if (verticalSymmetry) {
			ret = safeGet(reflectVertically(arrangment));
			if (ret != null)
				return ret;
		}
		return null;
	}

	private void setMovesToReach(Arrangement arrangement, int moves) {
		safePut(arrangement, moves);
	}

	private boolean isPlaceSafe(Arrangement arrangement, Piece p) {
		if (!(p.getXCoord() >= 0 && p.getXCoord() < this.board
				.getNumHorizontalTiles())) {
			return false;
		}
		if (!(p.getYCoord() >= 0 && p.getYCoord() < this.board
				.getNumVerticalTiles())) {
			return false;
		}
		if (!this.board.isTilePassable(p.getXCoord(), p.getYCoord(), arrangement.getPieces())) {
			return false;
		}
		if (isPieceDestroyed(arrangement, p)) {
			return false;
		}
		Tile t = board.getTileAtBoardPosition(p.getXCoord(), p.getYCoord());
		if (t.hasPainter() && t.getPainterColor() != p.getColor()) {
			// The piece is moving onto a painter of a different color
			Piece recoloredPiece = new Piece(p.getXCoord(), p.getYCoord(),
					t.getPainterColor());
			return !doesPieceDestroy(arrangement, recoloredPiece)
					&& !isPieceDestroyed(arrangement, recoloredPiece);
		}
		return !doesPieceDestroy(arrangement, p);
	}

	// Call with the current piece array and the piece to move,
	// this returns a set of new states (not including pieces) that
	// the board can now be in.
	private Set<Arrangement> getMoves(Arrangement arrangement, Piece p, boolean recursing) {
		boolean success = this.searchesCompleted.add(p);
		if (!success) {
			return null;
		}

		List<Point> moves = new ArrayList<Point>();
		moves.addAll(getContiguousPoints(arrangement, p, recursing));
		Set<Arrangement> moveStates = new HashSet<Arrangement>();

		for (int i = 0; i < moves.size(); i++) {
			Point move = moves.get(i);
			// Handle painters:
			Tile t = board.getTileAtBoardPosition(move.x, move.y);
			if (t.hasPainter() && t.getPainterColor() != p.getColor()) {
				Piece coloredPiece = new Piece(move.x, move.y,
						t.getPainterColor());
				Set<Arrangement> temp = getMoves(arrangement, coloredPiece, true);
				if (temp != null) {
					moveStates.addAll(temp);
				}
				// Since the move ended on a painter
				// remove it from the original color moves
				moves.remove(i);
				i--;
			}
		}

		// moves shouldn't contain any
		fillMoveStates(moveStates, moves, arrangement, p.getColor());
		return moveStates;
	}

	private void fillMoveStates(Set<Arrangement> moveStates, List<Point> moves,
			Arrangement arrangement, Color color) {
		for (Point movePoint : moves) {
			Piece[][] copy = new Piece[board.getNumHorizontalTiles()][board
					.getNumVerticalTiles()];
			for (int i = 0; i < copy.length; i++) {
				for (int j = 0; j < copy[0].length; j++) {
					copy[i][j] = arrangement.getPiece(i, j);
				}
			}
			int x = movePoint.x;
			int y = movePoint.y;
			copy[x][y] = new Piece(x, y, color);
			moveStates.add(new Arrangement(copy));
		}
	}

	private Set<Point> getContiguousPoints(Arrangement arrangement, Piece p, boolean recursing) {
		Set<Point> contiguousPoints = new HashSet<Point>();
		List<Point> searchQueue = new ArrayList<Point>();
		Point originalPoint = new Point(p.getXCoord(), p.getYCoord());
		searchQueue.add(originalPoint);
		if (recursing) {
			contiguousPoints.add(originalPoint);
		}

		while (searchQueue.size() > 0) {
			Point tempPoint = searchQueue.remove(0);

			// Handle painters
			Tile t = board.getTileAtBoardPosition(tempPoint.x, tempPoint.y);
			if (t.hasPainter() && t.getPainterColor() != p.getColor()) {
				continue;
			}

			Point up_point = new Point(tempPoint.x, tempPoint.y + 1);
			Piece up = new Piece(up_point, p.getColor());
			if (isPlaceSafe(arrangement, up) && !contiguousPoints.contains(up_point)) {
				searchQueue.add(up_point);
				if (!up_point.equals(originalPoint)) {
					contiguousPoints.add(up_point);
				}
			}

			Point down_point = new Point(tempPoint.x, tempPoint.y - 1);
			Piece down = new Piece(down_point, p.getColor());
			if (isPlaceSafe(arrangement, down)
					&& !contiguousPoints.contains(down_point)) {
				searchQueue.add(down_point);
				if (!down_point.equals(originalPoint)) {
					contiguousPoints.add(down_point);
				}
			}

			Point left_point = new Point(tempPoint.x - 1, tempPoint.y);
			Piece left = new Piece(left_point, p.getColor());
			if (isPlaceSafe(arrangement, left)
					&& !contiguousPoints.contains(left_point)) {
				searchQueue.add(left_point);
				if (!left_point.equals(originalPoint)) {
					contiguousPoints.add(left_point);
				}
			}

			Point right_point = new Point(tempPoint.x + 1, tempPoint.y);
			Piece right = new Piece(right_point, p.getColor());
			if (isPlaceSafe(arrangement, right)
					&& !contiguousPoints.contains(right_point)) {
				searchQueue.add(right_point);
				if (!right_point.equals(originalPoint)) {
					contiguousPoints.add(right_point);
				}
			}
		}
		return contiguousPoints;
	}

	private boolean doesPieceDestroy(Arrangement arrangement, Piece p) {
		boolean destroyPossible = false;

		final int PX = p.getXCoord();
		final int PY = p.getYCoord();

		// Slide left
		for (int xPos = PX - 1; xPos >= 0; xPos--) {
			Piece possible = arrangement.getPiece(xPos, PY);
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					if (destroyPossible) {
						return true;
					}
					break;
				} else {
					destroyPossible = true;
				}
			}
		}
		destroyPossible = false;

		// Slide right
		for (int xPos = PX + 1; xPos < arrangement.getXSize(); xPos++) {
			Piece possible = arrangement.getPiece(xPos, PY);
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					if (destroyPossible) {
						return true;
					}
					break;
				} else {
					destroyPossible = true;
				}
			}
		}
		destroyPossible = false;

		// Slide down
		for (int yPos = PY - 1; yPos >= 0; yPos--) {
			Piece possible = arrangement.getPiece(PX, yPos);
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					if (destroyPossible) {
						return true;
					}
					break;
				} else {
					destroyPossible = true;
				}
			}
		}
		destroyPossible = false;

		// Slide up
		for (int yPos = PY + 1; yPos < arrangement.getYSize(); yPos++) {
			Piece possible = arrangement.getPiece(PX, yPos);
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					if (destroyPossible) {
						return true;
					}
					break;
				} else {
					destroyPossible = true;
				}
			}
		}
		return false;
	}

	private boolean isPieceDestroyed(Arrangement arrangement, Piece p) {

		// Check if p is destroyed. First, horizontally
		Color leftColor = Color.NONE;
		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		// Slide to left
		for (; leftColor == Color.NONE && xPos >= 0; xPos--) {

			Piece atLeft = arrangement.getPiece(xPos, yPos);

			if (atLeft != null) {
				leftColor = atLeft.getColor();
			}
		}

		if (leftColor != Color.NONE && leftColor != p.getColor()) {
			Color rightColor = Color.NONE;

			xPos = p.getXCoord() + 1;

			// Slide to the right
			for (; rightColor == Color.NONE && xPos < arrangement.getXSize(); xPos++) {

				Piece atRight = arrangement.getPiece(xPos, yPos);

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

			Piece atTop = arrangement.getPiece(xPos, yPos);

			if (atTop != null) {
				topColor = atTop.getColor();
			}
		}

		if (topColor != Color.NONE && topColor != p.getColor()) {
			Color botColor = Color.NONE;

			yPos = p.getYCoord() + 1;

			// Step down
			for (; botColor == Color.NONE && yPos < arrangement.getYSize(); yPos++) {

				Piece atBot = arrangement.getPiece(xPos, yPos);

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
