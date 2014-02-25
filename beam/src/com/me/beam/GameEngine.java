package com.me.beam;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;

public class GameEngine implements ApplicationListener {
	// Simple Objects for now
	private Board b;
	private DrawGame dg;
	private InputHandler inputHandler;
	private LevelLoader levelLoader;
	
	public static final int LEVEL_IN = 3;

	public static Piece movingPiece = null;
	public static List<Tile> movePath = new ArrayList<Tile>();

	// Animation constants in ticks
	private static final int timeOnTileBeforeMove = 7;

	private int timeSpentOnTile = 0;

	public enum GameState {
		PAUSED, IDLE, DECIDING, MOVING
	}

	public enum Color {
		RED, BLUE, GREEN, NONE;
		public static Color lookup(int i) {
			switch (i) {
			case 1:
				return Color.RED;
			case 2:
				return Color.BLUE;
			case 3:
				return Color.GREEN;
			default:
				return Color.NONE;
			}
		}

		public int toIndex() {
			int i = 0;
			while (true) {
				if (Color.lookup(i) == this)
					return i;
				i++;
			}
		}
	}

	public static final float topBarSize = 0.15f;
	public static final float botBarSize = 0.20f;
	public static final float sideEmptySize = 0.02f;

	private GameState state = GameState.IDLE;

	@Override
	public void create() {

		// setHardCodedLevel();
		levelLoader = new LevelLoader("data/levels/levels.xml");
		b = levelLoader.getLevel(LEVEL_IN);

		dg = new DrawGame();
		inputHandler = new InputHandler();

		initializeLasers();
	}

	private void setHardCodedLevel() {
		// 12 Move Game
		b = new Board(7, 7);

		b.put(new Piece(0, 0, Color.GREEN));
		b.put(new Piece(0, 6, Color.GREEN));
		b.put(new Piece(6, 0, Color.GREEN));
		b.put(new Piece(6, 6, Color.GREEN));

		b.put(new Piece(2, 2, Color.RED));
		b.put(new Piece(2, 4, Color.RED));
		b.put(new Piece(4, 2, Color.RED));
		b.put(new Piece(4, 4, Color.RED));

		b.put(new Piece(1, 3, Color.BLUE));
		b.put(new Piece(3, 1, Color.BLUE));
		b.put(new Piece(3, 3, Color.BLUE));
		b.put(new Piece(3, 5, Color.BLUE));
		b.put(new Piece(5, 3, Color.BLUE));

		b.setGlass(1, 0, true);
		b.setGlass(1, 2, true);
		b.setGlass(1, 4, true);
		b.setGlass(1, 6, true);

		b.setGlass(2, 1, true);
		b.setGlass(2, 5, true);

		b.setGlass(3, 0, true);
		b.setGlass(3, 6, true);

		b.setGlass(4, 1, true);
		b.setGlass(4, 5, true);

		b.setGlass(5, 0, true);
		b.setGlass(5, 2, true);
		b.setGlass(5, 4, true);
		b.setGlass(5, 6, true);

		b.setGoal(1, 1, Color.RED);
		b.setGoal(1, 5, Color.RED);
		b.setGoal(5, 5, Color.RED);
		b.setGoal(5, 1, Color.RED);
	}

	@Override
	public void dispose() {
		dg.dispose();
	}

	@Override
	public void render() {
		// Get input from the user
		state = inputHandler.handleInput(b, state);

		// Do things if we're moving
		if (state == GameState.MOVING) {
			// Check to see if we actually move yet
			if (timeSpentOnTile < timeOnTileBeforeMove) {
				timeSpentOnTile++;
			} else {
				// Reset time on tile
				timeSpentOnTile = 0;

				// Get rid of the place we were
				movePath.remove(0);

				// Remove previous lasers
				removeLasersFromPiece(movingPiece);

				b.move(movingPiece, movePath.get(0));

				// Check for piece destroyed
				if (!checkIfPieceDestroyed(movingPiece)) {

					// Get painted
					paintPiece(movingPiece);

					// Check for piece destroyed
					if (!checkIfPieceDestroyed(movingPiece)) {

						// Form new lasers and cause destruction
						List<Piece> destroyed = formLasersFromPieceAndDestroy(movingPiece);
						for (Piece p : destroyed) {
							b.removePiece(p);
							removeLasersFromPiece(p);
						}

					} else {
						b.removePiece(movingPiece);
						movingPiece = null;
						movePath.clear();
						state = GameState.IDLE;
					}

				} else {
					b.removePiece(movingPiece);
					movingPiece = null;
					movePath.clear();
					state = GameState.IDLE;
				}

				// No lockout after move
				if (movePath.size() == 1) {
					movePath.clear();
					state = GameState.IDLE;
				}
			}

		} // else if (state == GameState.DECIDING){
			// For now, do nothing if the state is deciding. There's nothing to
			// do
			// } //else if (state == GameState.IDLE){
			// For now, do nothing if the state is idle. There's nothing to do
			// }

		// Draw the game
		dg.draw(b, state);
	}

	public void initializeLasers() {
		for (Piece p1 : b.getAllPieces()) {
			for (Piece p2 : b.getAllPieces()) {
				if (!p1.equals(p2)
						&& p1.getColor() == p2.getColor()
						&& (p1.getXCoord() == p2.getXCoord() || p1.getYCoord() == p2
								.getYCoord())) {
					int xStart = Math.min(p1.getXCoord(), p2.getXCoord());
					int xFinish = Math.max(p1.getXCoord(), p2.getXCoord());
					int yStart = Math.min(p1.getYCoord(), p2.getYCoord());
					int yFinish = Math.max(p1.getYCoord(), p2.getYCoord());
					Laser l = new Laser(xStart, yStart, xFinish, yFinish,
							p1.getColor());

					if (!b.lasers.contains(l))
						b.lasers.add(l);
				}
			}
		}
	}

	public void paintPiece(Piece p) {

		// Is there a painter?
		Tile pieceTile = b.getTileAtBoardPosition(p.getXCoord(), p.getYCoord());

		// Paint the piece!
		if (pieceTile.hasPainter()) {
			p.setColor(pieceTile.getPainterColor());
		}
	}

	// Removes all lasers connected to the current piece
	public void removeLasersFromPiece(Piece p) {
		List<Laser> survivingLasers = new ArrayList<Laser>();

		Tile leftTile = null;
		Tile topTile = null;
		Tile botTile = null;
		Tile rightTile = null;

		int xPos = p.getXCoord();
		int yPos = p.getYCoord();

		for (Laser l : b.lasers) {
			// Check if started at same place
			if (l.getXStart() == xPos && l.getYStart() == yPos) {
				// Laser goes to the right
				if (l.getXStart() < l.getXFinish()) {
					rightTile = new Tile(l.getXFinish(), l.getYFinish());

					// New laser formed
					if (leftTile != null) {
						Laser newLaser = new Laser(leftTile.getXCoord(),
								leftTile.getYCoord(), rightTile.getXCoord(),
								rightTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}
				// Laser goes up
				else {
					topTile = new Tile(l.getXFinish(), l.getYFinish());

					if (botTile != null) {
						Laser newLaser = new Laser(botTile.getXCoord(),
								botTile.getYCoord(), topTile.getXCoord(),
								topTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}

			} else if (l.getXFinish() == xPos && l.getYFinish() == yPos) {
				// Laser goes to the left
				if (l.getXStart() < l.getXFinish()) {
					leftTile = new Tile(l.getXStart(), l.getYStart());

					// New laser formed
					if (rightTile != null) {
						Laser newLaser = new Laser(leftTile.getXCoord(),
								leftTile.getYCoord(), rightTile.getXCoord(),
								rightTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}
				// Laser goes down
				else {
					botTile = new Tile(l.getXStart(), l.getYStart());

					if (topTile != null) {
						Laser newLaser = new Laser(botTile.getXCoord(),
								botTile.getYCoord(), topTile.getXCoord(),
								topTile.getYCoord(), l.getColor());
						survivingLasers.add(newLaser);
					}
				}
			} else {
				// Laser survives
				survivingLasers.add(l);
			}
		}
		// Modify the board's lasers
		b.lasers = survivingLasers;
	}

	// Form lasers from a piece that has just moved return destroyed pieces
	public List<Piece> formLasersFromPieceAndDestroy(Piece p) {
		List<Piece> destroyed = new ArrayList<Piece>();

		// For each destruction
		List<Piece> possibleDestroy = new ArrayList<Piece>();

		// Check for left pieces
		Tile leftSameColor = null;

		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		// Slide to the left
		for (; leftSameColor == null && xPos >= 0; xPos--) {
			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Tile rightSameColor = null;

		xPos = p.getXCoord() + 1;

		// Slide to the right
		for (; rightSameColor == null && xPos < b.getNumHorizontalTiles(); xPos++) {

			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (leftSameColor != null && rightSameColor != null) {
			b.lasers.remove(new Laser(leftSameColor.getXCoord(), leftSameColor
					.getYCoord(), rightSameColor.getXCoord(), rightSameColor
					.getYCoord(), p.getColor()));
		}

		possibleDestroy.clear();

		// Now do vertical!

		// Check for bot pieces
		Tile botSameColor = null;

		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Slide down
		for (; botSameColor == null && yPos >= 0; yPos--) {
			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Tile topSameColor = null;

		yPos = p.getYCoord() + 1;

		// Slide up
		for (; topSameColor == null && yPos < b.getNumVerticalTiles(); yPos++) {

			Piece possible = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = b.getTileAtBoardPosition(xPos, yPos);
					destroyed.addAll(possibleDestroy);
					b.lasers.add(new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor()));
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (botSameColor != null && topSameColor != null) {
			b.lasers.remove(new Laser(botSameColor.getXCoord(), botSameColor
					.getYCoord(), topSameColor.getXCoord(), topSameColor
					.getYCoord(), p.getColor()));
		}

		return destroyed;
	}

	// Pieces in the list of pieces are destroyed
	public boolean checkIfPieceDestroyed(Piece p) {

		// Check if p is destroyed. First, horizontally
		Color leftColor = Color.NONE;
		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		// Slide to left
		for (; leftColor == Color.NONE && xPos >= 0; xPos--) {

			Piece atLeft = b.getPieceOnTile(b
					.getTileAtBoardPosition(xPos, yPos));

			if (atLeft != null) {
				leftColor = atLeft.getColor();
			}
		}

		if (leftColor != Color.NONE && leftColor != p.getColor()) {
			Color rightColor = Color.NONE;

			xPos = p.getXCoord() + 1;

			// Slide to the right
			for (; rightColor == Color.NONE && xPos < b.getNumHorizontalTiles(); xPos++) {

				Piece atRight = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
						yPos));

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

			Piece atTop = b
					.getPieceOnTile(b.getTileAtBoardPosition(xPos, yPos));

			if (atTop != null) {
				topColor = atTop.getColor();
			}
		}

		if (topColor != Color.NONE && topColor != p.getColor()) {
			Color botColor = Color.NONE;

			yPos = p.getYCoord() + 1;

			// Step down
			for (; botColor == Color.NONE && yPos < b.getNumVerticalTiles(); yPos++) {

				Piece atBot = b.getPieceOnTile(b.getTileAtBoardPosition(xPos,
						yPos));

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

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
