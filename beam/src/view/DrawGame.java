package view;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import model.Board;
import model.GameProgress;
import model.Laser;
import model.Menu;
import model.Piece;
import model.Tile;
import utilities.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import controller.GameEngine;
import controller.GameEngine.AnimationState;
import controller.GameEngine.GameState;

public class DrawGame {
	private static final float beamThickness = 0.1f; // This is measured in
														// units of square size

	private SpriteBatch batch;
	private Texture pieceTexture;
	private Sprite pieceSprite;
	private Texture bangTexture;
	private Sprite bangSprite;
	private Texture starTexture;
	private Sprite starSprite;
	private Texture threeStarTexture;
	private Sprite threeStarSprite;
	private Texture oneStarTexture;
	private Sprite oneStarSprite;
	public ShapeRenderer shapes;

	BitmapFont titleFont;
	BitmapFont titleFontNoBest;
	BitmapFont menuButtonFont;
	BitmapFont introFont;
	BitmapFont levelNameFont;
	BitmapFont movesFont;
	BitmapFont moveWordFont;
	BitmapFont beamGoalFont;
	BitmapFont gameButtonFont;
	BitmapFont nonGameMButtonFont;
	BitmapFont nonGameNLButtonFont;

	public static Color translateColor(GameEngine.Color c) {
		switch (c) {
		case RED:
			return new Color(.808f, .098f, .149f, 1);
		case BLUE:
			return new Color(.098f, .396f, .808f, 1);
		case GREEN:
			return new Color(.067f, .510f, .067f, 1);
		case ORANGE:
			return new Color(255 / 255.0f, 150 / 255.0f, 20 / 255.0f, 1);
		case PURPLE:
			return new Color(.6f, 0, .6f, 1);
		default:
			return new Color(0, 0, 0, 0);
		}
	}

	public DrawGame(GameProgress gp) {
		batch = new SpriteBatch();

		pieceTexture = new Texture(Gdx.files.internal("data/piece.png"));
		pieceTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		bangTexture = new Texture(Gdx.files.internal("data/bangbang.png"));
		bangTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		starTexture = new Texture(Gdx.files.internal("data/star.png"));
		starTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		// gameProgress = gp;

		threeStarTexture = new Texture(Gdx.files.internal("data/3Star.png"));
		threeStarTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		oneStarTexture = new Texture(Gdx.files.internal("data/1Star.png"));
		oneStarTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion pieceregion = new TextureRegion(pieceTexture, 0, 0, 256,
				256);
		TextureRegion bangregion = new TextureRegion(bangTexture, 0, 0, 256,
				256);
		TextureRegion starregion = new TextureRegion(starTexture, 0, 0, 64, 64);
		TextureRegion threestarregion = new TextureRegion(threeStarTexture, 0,
				0, 128, 128);
		TextureRegion onestarregion = new TextureRegion(oneStarTexture, 0,
				0, 128, 128);

		pieceSprite = new Sprite(pieceregion);

		bangSprite = new Sprite(bangregion);
		starSprite = new Sprite(starregion);
		threeStarSprite = new Sprite(threestarregion);
		oneStarSprite = new Sprite(onestarregion);

		shapes = new ShapeRenderer();
	}

	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.internal("data/fonts/swanse.ttf"));
		titleFont = generator.generateFont(Gdx.graphics.getHeight() / 28);
		titleFontNoBest = generator.generateFont(Gdx.graphics.getHeight() / 25);
		menuButtonFont = generator.generateFont(Gdx.graphics.getHeight() / 45);
		introFont = generator.generateFont(Gdx.graphics.getHeight() / 20);
		levelNameFont = generator.generateFont((int) (Gdx.graphics.getHeight()
				* Constants.TOP_BAR_SIZE * 0.8f));
		moveWordFont = generator.generateFont((int) (Gdx.graphics.getHeight()
				* Constants.TOP_BAR_SIZE * 0.2f));
		movesFont = generator.generateFont((int) (Gdx.graphics.getHeight()
				* Constants.TOP_BAR_SIZE * 0.45f));
		beamGoalFont = generator.generateFont((int) (Gdx.graphics.getHeight()
				* Constants.BEAM_GOAL_HEIGHT * 0.5f));
		gameButtonFont = generator.generateFont((int) (Gdx.graphics.getHeight()
				* Constants.GAME_BUTTON_HEIGHT * 0.7f));
		nonGameMButtonFont = generator.generateFont((int) (Gdx.graphics
				.getHeight() * Constants.NON_GAME_BUTTON_HEIGHT * 0.7f));
		nonGameNLButtonFont = generator.generateFont((int) (Gdx.graphics
				.getHeight() * Constants.NON_GAME_BUTTON_HEIGHT * 0.5f));

		generator.dispose();
	}

	/**
	 * Draws the grid on which the game is played Scrolls with animation
	 */
	private void drawGrid(int bx, int by, int tilesize, Board b) {
		shapes.begin(ShapeType.Line);
		shapes.setColor(Constants.LINE_COLOR);
		Gdx.gl.glLineWidth(2);
		for (int i = 0; i <= b.getNumHorizontalTiles(); i++) {
			shapes.line(bx + (i * tilesize), by, bx + (i * tilesize),
					by + (b.getNumVerticalTiles() * tilesize));
		}
		for (int i = 0; i <= b.getNumVerticalTiles(); i++) {
			shapes.line(bx, by + (i * tilesize),
					bx + (b.getNumHorizontalTiles() * tilesize), by
							+ (i * tilesize));
		}
		shapes.end();
	}

	/**
	 * Draws anything that appears in a tile which never moves. This includes
	 * glass, painters, and goals Scrolls with animation
	 */
	private void drawTiles(int bx, int by, int tilesize, List<Tile> tiles) {
		shapes.begin(ShapeType.Line);
		for (Tile t : tiles) {
			if (t.hasGlass()) {
				shapes.setColor(Constants.LINE_COLOR);
				int glassX = bx + (t.getXCoord() * tilesize);
				int glassY = by + (t.getYCoord() * tilesize);
				shapes.line(glassX, glassY + (0.25f * tilesize), glassX
						+ (0.25f * tilesize), glassY);
				shapes.line(glassX, glassY + (0.5f * tilesize), glassX
						+ (0.5f * tilesize), glassY);
				shapes.line(glassX, glassY + (0.75f * tilesize), glassX
						+ (0.75f * tilesize), glassY);
				shapes.line(glassX, glassY + tilesize, glassX + tilesize,
						glassY);
				shapes.line(glassX + (0.25f * tilesize), glassY + tilesize,
						glassX + tilesize, glassY + (0.25f * tilesize));
				shapes.line(glassX + (0.5f * tilesize), glassY + tilesize,
						glassX + tilesize, glassY + (0.5f * tilesize));
				shapes.line(glassX + (0.75f * tilesize), glassY + tilesize,
						glassX + tilesize, glassY + (0.75f * tilesize));
			}
		}
		shapes.end();
		shapes.begin(ShapeType.Filled);
		for (Tile t : tiles) {
			if (t.hasGoal()) {
				int goalX = bx + (t.getXCoord() * tilesize);
				int goalY = by + (t.getYCoord() * tilesize);
				shapes.setColor(translateColor(t.getGoalColor()));
				shapes.rect(goalX + (0.05f * tilesize), goalY
						+ (0.05f * tilesize), 0.9f * tilesize, 0.9f * tilesize);
				shapes.setColor(Constants.BOARD_COLOR);
				shapes.rect(goalX + (0.12f * tilesize), goalY
						+ (0.12f * tilesize), 0.76f * tilesize,
						0.76f * tilesize);
			}
		}
		shapes.end();
		shapes.begin(ShapeType.Filled);
		for (Tile t : tiles) {
			if (t.hasPainter()) {
				int paintX = bx + (t.getXCoord() * tilesize);
				int paintY = by + (t.getYCoord() * tilesize);
				switch (t.getPainterColor()) {
				case RED:
					shapes.setColor(new Color(.3f, 0, 0, 1));
					break;
				case BLUE:
					shapes.setColor(new Color(0, 0, .3f, 1));
					break;
				case GREEN:
					shapes.setColor(new Color(0, .3f, 0, 1));
					break;
				default:
					shapes.setColor(new Color(0, 0, 0, 0));
					break;
				}
				shapes.rect(paintX + (0.05f * tilesize), paintY
						+ (0.05f * tilesize), 0.9f * tilesize, 0.9f * tilesize);
			}
		}
		shapes.end();
	}

	/**
	 * Draws the path indicating user input
	 */
	private void drawPaths(int bx, int by, int tilesize, List<Tile> path,
			AnimationState aState, GameState state, float moveAnimateTime) {
		if (aState != AnimationState.DESTRUCTION) {
			shapes.begin(ShapeType.Filled);
			shapes.setColor(new Color(.808f, .674f, 0, 1f));
			for (int i = 0; i < path.size(); i++) {
				int pointX = path.get(i).getXCoord();
				int pointY = path.get(i).getYCoord();
				if (state != GameState.MOVING || i > 0) {
					shapes.rect(bx + ((pointX + .4f) * tilesize), by
							+ ((pointY + .4f) * tilesize), .2f * tilesize,
							.2f * tilesize);
				}
				if (i != path.size() - 1) {
					float shiftX = 0;
					float shiftY = 0;
					if (i == 0 && path.size() > 1 && state == GameState.MOVING) {
						shiftX = (path.get(1).getXCoord() - GameEngine.movingPiece
								.getXCoord()) * moveAnimateTime;
						shiftY = (path.get(1).getYCoord() - GameEngine.movingPiece
								.getYCoord()) * moveAnimateTime;
					}
					int nextX = path.get(i + 1).getXCoord();
					int nextY = path.get(i + 1).getYCoord();
					if (pointX == nextX) {
						float originY = Math.min(pointY + shiftY, nextY);
						float endY = Math.max(pointY + shiftY, nextY);
						shapes.rect(bx + ((pointX + .4f) * tilesize), by
								+ ((originY + .4f) * tilesize), .2f * tilesize,
								(endY - originY) * tilesize);
					} else {
						float originX = Math.min(pointX + shiftX, nextX);
						float endX = Math.max(pointX + shiftX, nextX);
						shapes.rect(bx + ((originX + .4f) * tilesize), by
								+ ((pointY + .4f) * tilesize), (endX - originX)
								* tilesize, .2f * tilesize);
					}
				}
			}
			if (path.size() > 1) {
				int finalX = path.get(path.size() - 1).getXCoord();
				int finalY = path.get(path.size() - 1).getYCoord();
				int prevX = path.get(path.size() - 2).getXCoord();
				int prevY = path.get(path.size() - 2).getYCoord();
				int baseX = bx + (finalX * tilesize);
				int baseY = by + (finalY * tilesize);
				if (finalX > prevX) {
					shapes.triangle(baseX + (.5f * tilesize), baseY
							+ (0.3f * tilesize), baseX + (.5f * tilesize),
							baseY + (0.7f * tilesize), baseX
									+ (0.75f * tilesize), baseY
									+ (0.5f * tilesize));
				} else if (finalX < prevX) {
					shapes.triangle(baseX + (.5f * tilesize), baseY
							+ (0.3f * tilesize), baseX + (.5f * tilesize),
							baseY + (0.7f * tilesize), baseX
									+ (0.25f * tilesize), baseY
									+ (0.5f * tilesize));
				} else if (finalY > prevY) {
					shapes.triangle(baseX + (.3f * tilesize), baseY
							+ (0.5f * tilesize), baseX + (.7f * tilesize),
							baseY + (0.5f * tilesize), baseX
									+ (0.5f * tilesize), baseY
									+ (0.75f * tilesize));
				} else if (finalY < prevY) {
					shapes.triangle(baseX + (.3f * tilesize), baseY
							+ (0.5f * tilesize), baseX + (.7f * tilesize),
							baseY + (0.5f * tilesize), baseX
									+ (0.5f * tilesize), baseY
									+ (0.25f * tilesize));
				}
			}
			shapes.end();
		}
	}

	/**
	 * Draws all of the pieces. Draws them between spaces if they're being
	 * animated Scrolls with animation
	 */
	private void drawPieces(int bx, int by, int tilesize, List<Tile> path,
			Color paintColor, List<Piece> pieces, float paintAnimateTime,
			float moveAnimateTime) {

		batch.begin();
		pieceSprite.setSize(tilesize, tilesize);
		for (Piece p : pieces) {
			pieceSprite.setColor(translateColor(p.getColor()));
			if (p.equals(GameEngine.movingPiece)) {
				float rshift = (paintColor.r - translateColor(p.getColor()).r)
						* paintAnimateTime;
				float gshift = (paintColor.g - translateColor(p.getColor()).g)
						* paintAnimateTime;
				float bshift = (paintColor.b - translateColor(p.getColor()).b)
						* paintAnimateTime;
				pieceSprite.setColor(new Color(translateColor(p.getColor()).r
						+ rshift, translateColor(p.getColor()).g + gshift,
						translateColor(p.getColor()).b + bshift, 1));
			}
			pieceSprite.setPosition(bx + (p.getXCoord() * tilesize),
					by + (p.getYCoord() * tilesize));
			if (path.size() > 1
					&& p.getXCoord() == GameEngine.movingPiece.getXCoord()
					&& p.getYCoord() == GameEngine.movingPiece.getYCoord()) {
				float animateX = (path.get(1).getXCoord() - GameEngine.movingPiece
						.getXCoord()) * tilesize * moveAnimateTime;
				float animateY = (path.get(1).getYCoord() - GameEngine.movingPiece
						.getYCoord()) * tilesize * moveAnimateTime;
				pieceSprite.translate(animateX, animateY);
			}
			pieceSprite.draw(batch);
		}
		batch.end();

	}

	/**
	 * Draws all of the beams between pieces. Also draws intermediate states
	 * caused by animations scrolls with animation
	 */
	private void drawBeams(int bx, int by, int tilesize, Set<Laser> lasers,
			Laser disbandedLaser, Laser movedAlongLaser, AnimationState aState,
			List<Tile> path, float moveAnimateTime, float paintAnimateTime,
			float formAnimateTime, float breakAnimateTime) {
		shapes.begin(ShapeType.Filled);
		float laserWidth = beamThickness;
		for (Laser l : lasers) {
			if (disbandedLaser != null && l.equals(disbandedLaser)) {
				laserWidth = (1 - breakAnimateTime) * beamThickness;
			} else {
				laserWidth = beamThickness;
			}
			shapes.setColor(translateColor(l.getColor()));
			if (!l.equals(movedAlongLaser)) {
				if (l.getXStart() == l.getXFinish()) {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2))
							* tilesize, by
							+ (l.getYStart() + 0.5f - (laserWidth / 2))
							* tilesize, laserWidth * tilesize,
							(l.getYFinish() - l.getYStart()) * tilesize);
				} else {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2))
							* tilesize, by
							+ (l.getYStart() + 0.5f - (laserWidth / 2))
							* tilesize, (l.getXFinish() - l.getXStart())
							* tilesize, laserWidth * tilesize);
				}
			}
		}

		List<Laser> allFormedLasers = GameEngine.getFormedLaser();
		if (!allFormedLasers.isEmpty()) {
			for (Laser l : allFormedLasers)
				if (l != null) {
					laserWidth = formAnimateTime * beamThickness;
					shapes.setColor(translateColor(l.getColor()));
					if (l.getXStart() == l.getXFinish()) {
						shapes.rect(bx
								+ (l.getXStart() + 0.5f - (laserWidth / 2))
								* tilesize, by
								+ (l.getYStart() + 0.5f - (laserWidth / 2))
								* tilesize, laserWidth * tilesize,
								(l.getYFinish() - l.getYStart()) * tilesize);
					} else {
						shapes.rect(bx
								+ (l.getXStart() + 0.5f - (laserWidth / 2))
								* tilesize, by
								+ (l.getYStart() + 0.5f - (laserWidth / 2))
								* tilesize, (l.getXFinish() - l.getXStart())
								* tilesize, laserWidth * tilesize);
					}
				}
		}
		if (movedAlongLaser != null && aState != AnimationState.DESTRUCTION) {
			laserWidth = beamThickness * (1 - paintAnimateTime);
			shapes.setColor(translateColor(movedAlongLaser.getColor()));
			float moveAnimX = (path.get(1).getXCoord() - GameEngine.movingPiece
					.getXCoord()) * tilesize * moveAnimateTime;
			float moveAnimY = (path.get(1).getYCoord() - GameEngine.movingPiece
					.getYCoord()) * tilesize * moveAnimateTime;
			if (movedAlongLaser.getXStart() == movedAlongLaser.getXFinish()) {
				if (movedAlongLaser.getXStart() == GameEngine.movingPiece
						.getXCoord()
						&& movedAlongLaser.getYStart() == GameEngine.movingPiece
								.getYCoord()) {
					shapes.rect(
							bx
									+ (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2))
									* tilesize,
							(by + (movedAlongLaser.getYStart() + 0.5f - (laserWidth / 2))
									* tilesize)
									+ moveAnimY,
							laserWidth * tilesize,
							((movedAlongLaser.getYFinish() - movedAlongLaser
									.getYStart()) * tilesize) - moveAnimY);
				} else {
					shapes.rect(
							bx
									+ (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2))
									* tilesize,
							(by + (movedAlongLaser.getYStart() + 0.5f - (laserWidth / 2))
									* tilesize),
							laserWidth * tilesize,
							((movedAlongLaser.getYFinish() - movedAlongLaser
									.getYStart()) * tilesize) + moveAnimY);
				}
			} else {
				if (movedAlongLaser.getXStart() == GameEngine.movingPiece
						.getXCoord()
						&& movedAlongLaser.getYStart() == GameEngine.movingPiece
								.getYCoord()) {
					shapes.rect(
							(bx + (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2))
									* tilesize)
									+ moveAnimX,
							by
									+ (movedAlongLaser.getYStart() + 0.5f - (laserWidth / 2))
									* tilesize,
							((movedAlongLaser.getXFinish() - movedAlongLaser
									.getXStart()) * tilesize) - moveAnimX,
							laserWidth * tilesize);
				} else {
					shapes.rect(
							(bx + (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2))
									* tilesize),
							by
									+ (movedAlongLaser.getYStart() + 0.5f - (laserWidth / 2))
									* tilesize,
							((movedAlongLaser.getXFinish() - movedAlongLaser
									.getXStart()) * tilesize) + moveAnimX,
							laserWidth * tilesize);
				}
			}
		}
		shapes.end();
	}

	/**
	 * Draws the BANG effect where a piece has been destroyed
	 */
	private void drawBangs(int bx, int by, int tilesize) {
		batch.begin();
		List<Piece> destroyedPieces = GameEngine.getDestroyedPieces();
		if (destroyedPieces.size() > 0) {
			bangSprite.setSize(tilesize, tilesize);
			for (Piece dp : destroyedPieces) {
				bangSprite.setPosition(bx + (dp.getXCoord() * tilesize), by
						+ (dp.getYCoord() * tilesize));
				bangSprite.setColor(translateColor(dp.getColor()));
				bangSprite.draw(batch);
			}
		}
		batch.end();
	}

	/**
	 * Draws the control buttons on the HUD
	 */
	private void drawNongameButtons(int width, int height, TextBounds tb) {

		batch.begin();
		nonGameMButtonFont.setColor(Constants.BOARD_COLOR);
		tb = nonGameMButtonFont.getBounds("Menu");

		float textHeight = (height * Constants.NON_GAME_BUTTON_HEIGHT + tb.height) / 2;
		nonGameMButtonFont.draw(batch, "Menu", Menu.B_MENU_LEFT_X * width
				+ (Menu.B_MENU_WIDTH * width - tb.width) / 2, textHeight);

		// TODO: Draw the info button here

		nonGameNLButtonFont.setColor(Constants.BOARD_COLOR);
		tb = nonGameNLButtonFont.getBounds("Next Level");

		textHeight = (height * Constants.NON_GAME_BUTTON_HEIGHT + tb.height) / 2;
		nonGameNLButtonFont.draw(batch, "Next Level", Menu.B_NEXT_LEVEL_LEFT_X
				* width + (Menu.B_NEXT_LEVEL_WIDTH * width - tb.width) / 2,
				textHeight);

		batch.end();
	}

	/**
	 * Draws the game buttons above the level
	 */
	private void drawGameButtons(int shiftX, int by, int tilesize, Board b,
			TextBounds tb) {
		int baseY = b.getTopYCoord();

		String undo = "Undo";
		gameButtonFont.setColor(Constants.BOARD_COLOR);

		int screenHeight = Gdx.graphics.getHeight();
		int screenWidth = Gdx.graphics.getWidth();

		tb = gameButtonFont.getBounds(undo);
		batch.begin();
		float height = baseY
				+ (Constants.GAME_BUTTON_HEIGHT * screenHeight + tb.height) / 2;
		float xPos = Menu.B_UNDO_LEFT_X * screenWidth
				+ (Menu.B_UNDO_WIDTH * screenWidth - tb.width) / 2;
		xPos += shiftX;
		gameButtonFont.draw(batch, undo, xPos, height);

		String reset = "Reset";
		tb = gameButtonFont.getBounds(reset);
		xPos = Menu.B_RESET_LEFT_X * screenWidth
				+ (Menu.B_RESET_WIDTH * screenWidth - tb.width) / 2;
		gameButtonFont.draw(batch, reset, shiftX
				+ (Menu.B_RESET_LEFT_X * screenWidth), height);
		batch.end();
	}

	/**
	 * Draws the screen top graphics indicating level, moves, and perfect
	 */
	private void drawHeader(int width, int height, TextBounds tb,
			int currentWorld, int currentOrdinalInWorld, Board b) {
		float boxesWidth = width * 0.25f;
		float boxesHeight = height * Constants.TOP_BAR_SIZE * 0.65f;
		Color boxesColor = new Color(0.3f, 0.3f, 0.3f, 1);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Color.BLACK);
		shapes.rect(width * 0.40f, (1 - (Constants.TOP_BAR_SIZE * 0.825f))
				* height, boxesWidth, boxesHeight);
		shapes.rect(width * 0.70f, (1 - (Constants.TOP_BAR_SIZE * 0.825f))
				* height, boxesWidth, boxesHeight);
		shapes.setColor(boxesColor);
		shapes.rect((width * 0.40f) + 2,
				((1 - (Constants.TOP_BAR_SIZE * 0.825f)) * height) + 2,
				boxesWidth - 4, boxesHeight - 4);
		shapes.rect((width * 0.70f) + 2,
				((1 - (Constants.TOP_BAR_SIZE * 0.825f)) * height) + 2,
				boxesWidth - 4, boxesHeight - 4);
		shapes.end();

		int moves = GameEngine.getMoveCount();
		String movesString = moves + "";
		batch.begin();
		levelNameFont.setColor(Constants.BOARD_COLOR);
		String levelName = currentWorld + "-" + currentOrdinalInWorld;
		String moveWord = "Moves";
		levelNameFont.draw(batch, levelName, width * 0.05f,
				(1 - (Constants.TOP_BAR_SIZE * 0.2f)) * height);
		moveWordFont.setColor(Constants.BOARD_COLOR);
		tb = moveWordFont.getBounds(moveWord);
		moveWordFont.draw(batch, moveWord, (width * 0.4f)
				+ ((boxesWidth - tb.width) / 2.0f),
				(1 - (Constants.TOP_BAR_SIZE * 0.25f)) * height);
		tb = movesFont.getBounds(movesString);
		movesFont.setColor(Constants.BOARD_COLOR);
		movesFont.draw(batch, movesString, (width * 0.4f)
				+ ((boxesWidth - tb.width) / 2.0f),
				(1 - (Constants.TOP_BAR_SIZE * 0.45f)) * height);

		String perfectString = b.perfect + "";
		tb = movesFont.getBounds(perfectString);
		float starSize = 0.7f * (boxesWidth - tb.width);
		threeStarSprite.setSize(starSize, starSize);
		movesFont.draw(batch, perfectString, (width * 0.7f)
				+ (boxesWidth - (tb.width + (0.05f * boxesWidth))),
				(1 - (Constants.TOP_BAR_SIZE * 0.35f)) * height);
		threeStarSprite.setPosition((width * 0.7f)
				+ ((boxesWidth - tb.width) * .15f),
				(1 - (Constants.TOP_BAR_SIZE * 0.825f)) * height
						+ ((boxesHeight - starSize) / 2.0f));
		threeStarSprite.draw(batch);
		batch.end();

	}

	private void drawGoalProgress(int width, int height, TextBounds tb,
			Board b, float transitionPart) {
		if (b.getBeamObjectiveSet().isEmpty()) {
			// This is a piece placement level
			int remGoals = b.getNumGoalTiles() - b.getNumGoalsFilled();
			String ftg = "Fill the goals:";
			String remains = remGoals + " remain" + (remGoals == 1 ? "s" : "");
			batch.begin();
			tb = introFont.getBounds(ftg);
			introFont.setColor(Constants.BOARD_COLOR);
			introFont
					.draw(batch,
							ftg,
							((width - tb.width) / 2.0f) + transitionPart,
							(height * (Constants.BOT_BAR_SIZE + (0.8f * Constants.TEXT_GOAL_HEIGHT))));
			tb = introFont.getBounds(remains);
			introFont
					.draw(batch,
							remains,
							((width - tb.width) / 2.0f) + transitionPart,
							(height * (Constants.BOT_BAR_SIZE + (0.8f * Constants.TEXT_GOAL_HEIGHT)))
									- (tb.height * 1.5f));
			batch.end();
		} else {
			int totalBeamGoals = 0;
			for (GameEngine.Color c : b.getBeamObjectiveSet()) {
				totalBeamGoals += b.getBeamObjectiveCount(c);
			}
			if (totalBeamGoals == 0) {
				// This is a break all beams level
				// This is a piece placement level
				int remBeams = b.lasers.size();
				String bab = "Break all beams:";
				String remains = remBeams + " remain"
						+ (remBeams == 1 ? "s" : "");
				batch.begin();
				tb = introFont.getBounds(bab);
				introFont.setColor(Constants.BOARD_COLOR);
				introFont
						.draw(batch,
								bab,
								((width - tb.width) / 2.0f) + transitionPart,
								(height * (Constants.BOT_BAR_SIZE + (0.8f * Constants.TEXT_GOAL_HEIGHT))));
				tb = introFont.getBounds(remains);
				introFont
						.draw(batch,
								remains,
								((width - tb.width) / 2.0f) + transitionPart,
								(height * (Constants.BOT_BAR_SIZE + (0.8f * Constants.TEXT_GOAL_HEIGHT)))
										- (tb.height * 1.5f));
				batch.end();
			} else {
				EnumMap<GameEngine.Color, Integer> beamObjective = new EnumMap<GameEngine.Color, Integer>(
						GameEngine.Color.class);
				EnumMap<GameEngine.Color, Integer> curLaserCount = new EnumMap<GameEngine.Color, Integer>(
						GameEngine.Color.class);

				int objCount = 0;
				int existCount = 0;
				for (GameEngine.Color c : b.getBeamObjectiveSet()) {
					objCount = b.getBeamObjectiveCount(c);
					beamObjective.put(c, objCount);
					existCount = b.getLaserCount(c);
					curLaserCount.put(c, existCount);
				}

				int i = 0;
				for (GameEngine.Color c : GameEngine.Color.values()) {
					if (beamObjective.get(c) != null
							&& beamObjective.get(c) != 0) {
						shapes.begin(ShapeType.Filled);
						shapes.setColor(Color.BLACK);
						shapes.rect(
								(((width - (Constants.BEAM_GOAL_WIDTH * width)) / 2) - 2)
										+ transitionPart,
								(height * (Constants.BOT_BAR_SIZE + (Constants.BEAM_GOAL_HEIGHT * (i + .125f)))) - 2,
								(Constants.BEAM_GOAL_WIDTH * width) + 4,
								(height * 0.75f * Constants.BEAM_GOAL_HEIGHT) + 4);
						shapes.setColor(Constants.BOARD_COLOR);
						shapes.rect(
								((width - (Constants.BEAM_GOAL_WIDTH * width)) / 2)
										+ transitionPart,
								height
										* (Constants.BOT_BAR_SIZE + (Constants.BEAM_GOAL_HEIGHT * (i + .125f))),
								Constants.BEAM_GOAL_WIDTH * width, height
										* 0.75f * Constants.BEAM_GOAL_HEIGHT);
						shapes.setColor(translateColor(c));
						float progress = (float) (curLaserCount.get(c))
								/ beamObjective.get(c);
						shapes.rect(
								((width - (Constants.BEAM_GOAL_WIDTH * width)) / 2)
										+ transitionPart,
								height
										* (Constants.BOT_BAR_SIZE + (Constants.BEAM_GOAL_HEIGHT * (i + .125f))),
								(Constants.BEAM_GOAL_WIDTH * width) * progress,
								height * 0.75f * Constants.BEAM_GOAL_HEIGHT);
						shapes.end();
						String text = "Form " + beamObjective.get(c) + " "
								+ c.name() + " beam"
								+ (beamObjective.get(c) == 1 ? "" : "s");
						tb = beamGoalFont.getBounds(text);
						beamGoalFont.setColor(Color.BLACK);
						batch.begin();
						beamGoalFont
								.draw(batch,
										text,
										((width - tb.width) / 2)
												+ transitionPart,
										((Constants.BOT_BAR_SIZE + ((i + 1) * Constants.BEAM_GOAL_HEIGHT)) * height)
												- (((height * Constants.BEAM_GOAL_HEIGHT) - tb.height) / 2));
						batch.end();
						i++;
					}
				}

			}
		}
	}

	/**
	 * Draws the level introduction beam that appears at level start
	 */
	private void drawIntro(int bx, int by, int tilesize, int width, int height,
			Board b, float introProgress, TextBounds tb) {
		GameEngine.Color baseColor = GameEngine.Color.RED;
		float progress = introProgress;
		float alpha;
		if (progress < 0.15) {
			alpha = (progress / 0.15f) * 0.9f;
		} else if (progress > 0.85) {
			alpha = (1 - ((progress - 0.85f) / 0.15f)) * 0.9f;
		} else {
			alpha = 0.9f;
		}
		float boardHeight = tilesize * b.getNumVerticalTiles();
		float boardWidth = tilesize * b.getNumHorizontalTiles();

		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(new Color(1, 1, 1, alpha));
		shapes.rect(bx, by, boardWidth, boardHeight);
		shapes.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);

		if (b.getBeamObjectiveSet().isEmpty()) {
			String message = "Fill " + b.getNumGoalTiles() + " "
					+ (b.getNumGoalTiles() == 1 ? "goal" : "goals");
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			batch.begin();
			introFont.setColor(new Color(0, 0, 0, alpha * 1.111f));
			tb = introFont.getBounds(message);
			introFont.draw(batch, message, bx
					+ ((boardWidth - tb.width) / 2.0f), by
					+ ((boardHeight + tb.height) / 2.0f));
			batch.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);
		} else {
			int totalBeams = 0;
			for (GameEngine.Color c : b.getBeamObjectiveSet()) {
				totalBeams += b.getBeamObjectiveCount(c);
			}
			if (totalBeams == 0) {
				String message = "Break all beams";
				Gdx.gl.glEnable(GL10.GL_BLEND);
				Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				batch.begin();
				introFont.setColor(new Color(0, 0, 0, alpha * 1.111f));
				tb = introFont.getBounds(message);
				introFont.draw(batch, message, bx
						+ ((boardWidth - tb.width) / 2.0f), by
						+ ((boardHeight + tb.height) / 2.0f));
				batch.end();
				Gdx.gl.glDisable(GL10.GL_BLEND);

			} else {
				String message = "Form";
				tb = introFont.getBounds(message);
				float individualHeight = tb.height * 1.8f;
				int beamsToDraw = b.getBeamObjectiveSet().size() + 1;
				float totBeamHeight = ((beamsToDraw) + ((beamsToDraw - 1) / 2.0f))
						* individualHeight;
				float baseheight = ((height - totBeamHeight) / 2.0f)
						+ totBeamHeight;
				Gdx.gl.glEnable(GL10.GL_BLEND);
				Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				batch.begin();
				introFont.setColor(new Color(0, 0, 0, alpha * 1.111f));
				tb = introFont.getBounds(message);
				introFont.draw(batch, message, bx
						+ ((boardWidth - tb.width) / 2.0f), baseheight);
				batch.end();
				Gdx.gl.glDisable(GL10.GL_BLEND);
				int i = 1;
				for (GameEngine.Color c : b.getBeamObjectiveSet()) {
					String bmessage = b.getBeamObjectiveCount(c) + " "
							+ c.toString() + " Beam"
							+ (b.getBeamObjectiveCount(c) == 1 ? "" : "s");
					Gdx.gl.glEnable(GL10.GL_BLEND);
					Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA,
							GL10.GL_ONE_MINUS_SRC_ALPHA);
					batch.begin();
					introFont.setColor(new Color(0, 0, 0, alpha * 1.111f));
					tb = introFont.getBounds(bmessage);
					introFont.draw(batch, bmessage, bx
							+ ((boardWidth - tb.width) / 2.0f), baseheight
							- ((1.5f) * i * individualHeight));
					batch.end();
					Gdx.gl.glDisable(GL10.GL_BLEND);
					i++;
				}
			}
		}
	}

	/**
	 * Draws the sequence that appears after completing a level
	 */
	private void drawOutro(int bx, int by, int width, int height, Board b, TextBounds tb){
		
		
		float au = GameEngine.getWonAnimationUnit();
		float timeWon = GameEngine.getTimeWon();
		float boxAlpha = 0;
		float boardWidth = b.getTileSize() * b.getNumHorizontalTiles();
		float boardHeight = b.getTileSize() * b.getNumVerticalTiles();
		int numStars = 1;
		if (GameEngine.getMoveCount() <= b.perfect){
			numStars = 3;
		} else if (GameEngine.getMoveCount() <= b.par){
			numStars = 2;
		}
		if(timeWon < au){
			boxAlpha = (timeWon / au) * 0.9f;
		} else {
			boxAlpha = 0.9f;
		}
		
		float star1size = 0;
		float star2size = 0;
		float star3size = 0;
		
		float textAlpha = 0;
		
		if(timeWon < (1 + numStars) * au){
			if(timeWon >= au && timeWon < (2 * au)){
				star1size = starFunc(((timeWon - au) / au) * 2.05814f);
			} else if (timeWon >= (2 * au) && timeWon < (3 * au)) {
				star1size = 1;
				star2size = starFunc(((timeWon - (2 * au)) / au) * 2.05814f);
			} else if (timeWon >= (3 * au) && timeWon < (4 * au)) {
				star1size = 1;
				star2size = 1;
				star3size = starFunc(((timeWon - (3 * au)) / au) * 2.05814f);
			}
		} else {
			star1size = 1;
			star2size = 1;
			star3size = 1;
			if(timeWon < (3 + numStars) * au){
				textAlpha = ((timeWon - ((1 + numStars) * au)) / (2 * au));
			} else {
				textAlpha = 1.0f;
			}
		
		}
		
		float starWidth = boardWidth * 4.0f / 10.0f;
		star1size *= starWidth;
		star2size *= starWidth;
		star3size *= starWidth;
		
		String levelEndMessage = "Good!";
		if(numStars == 2){
			levelEndMessage = "Excellent!";
		}
		if(numStars == 3){
			levelEndMessage = "Perfect!";
		}	
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(new Color(1, 1, 1, boxAlpha));
		shapes.rect(bx, by, boardWidth, boardHeight);
		shapes.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);

		float star1X = bx + (0.1f * boardWidth) + ((starWidth - star1size) / 2.0f);
		float star1Y = by + (0.2f * boardHeight) + (((0.8f *boardHeight) - (1.666f * starWidth)) / 2.0f) + ((starWidth - star1size) / 2.0f);
		float star2X = bx + (0.1f * boardWidth) + starWidth + ((starWidth - star2size) / 2.0f);
		float star2Y = by + (0.2f * boardHeight) + (((0.8f *boardHeight) - (1.666f * starWidth)) / 2.0f) + ((starWidth - star2size) / 2.0f);
		float star3X = bx + ((boardWidth - star3size) / 2.0f);
		float star3Y = by + (0.2f * boardHeight) + (0.666f * starWidth) + (((0.8f *boardHeight) - (1.666f * starWidth)) / 2.0f) + ((starWidth - star3size) / 2.0f);
		/*
		float star1X = starWidth + ((starWidth - star1size) / 2.0f);
		float star1Y = by + (0.5f * boardHeight) + ((starWidth - star1size) / 2.0f);
		float star2X = (starWidth)  + starWidth + ((starWidth - star2size) / 2.0f);
		float star2Y = by + (0.5f * boardHeight) + ((starWidth - star2size) / 2.0f);
		float star3X = (starWidth) + (2 * starWidth) + ((starWidth - star3size) / 2.0f);
		float star3Y = by + (0.5f * boardHeight) + ((starWidth - star3size) / 2.0f);
		*/
		float textY = by + (((0.2f * boardHeight) + (((0.8f *boardHeight) - (1.666f * starWidth)) / 2.0f))/ 2.0f);
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		batch.begin();
		oneStarSprite.setColor(Color.WHITE);
		oneStarSprite.setPosition(star1X, star1Y);
		oneStarSprite.setSize(star1size, star1size);
		oneStarSprite.draw(batch);
		
		if(numStars > 1){
			oneStarSprite.setColor(Color.WHITE);
			oneStarSprite.setPosition(star2X, star2Y);
			oneStarSprite.setSize(star2size, star2size);
			oneStarSprite.draw(batch);
		} else {
			oneStarSprite.setColor(new Color(0,0,0,textAlpha/3.0f));
			oneStarSprite.setPosition(star2X, star2Y);
			oneStarSprite.setSize(starWidth, starWidth);
			oneStarSprite.draw(batch);

		}

		if(numStars > 2){
			oneStarSprite.setColor(Color.WHITE);
			oneStarSprite.setPosition(star3X, star3Y);
			oneStarSprite.setSize(star3size, star3size);
			oneStarSprite.draw(batch);
		} else {
			oneStarSprite.setColor(new Color(0,0,0,textAlpha/3.0f));
			oneStarSprite.setPosition(star3X, star3Y);
			oneStarSprite.setSize(starWidth, starWidth);
			oneStarSprite.draw(batch);
		}
		
		tb = introFont.getBounds(levelEndMessage);
		introFont.setColor(new Color(0, 0, 0, textAlpha));
		introFont.draw(batch, levelEndMessage, bx + ((boardWidth - tb.width) / 2.0f), textY + (tb.height / 2.0f));
		
		
		
		batch.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);


	}
	
	private float starFunc(float x){
		return 1.618f - ((1.27201f - x) * (1.27201f - x));

	}

	/**
	 * Draws a horizontal beam like the level intro, or the level loss reminder
	 * banner.
	 */
	private void drawOverlayBeam(float progress, float ibeamheight,
			float beamY, Color baseColor, String message, BitmapFont font) {
		Color c1 = baseColor;
		Color c2 = new Color(1.4f * c1.r, 1.4f * c1.g, 1.4f * c1.b, 1);
		Color c3 = new Color(1.8f * c1.r, 1.8f * c1.g, 1.8f * c1.b, 1);
		float curheight = 0;

		if (progress <= 0.1) {
			curheight = (progress / 0.1f) * ibeamheight;
		} else if (progress > 0.9) {
			curheight = ((1 - progress) / 0.1f) * ibeamheight;
		} else {
			curheight = ibeamheight;
		}

		int width = Gdx.graphics.getWidth();
		shapes.begin(ShapeType.Filled);
		GameEngine.debug("Curheigt" + curheight);
		shapes.setColor(c1);
		shapes.rect(0, beamY - (curheight / 2.0f), width, curheight);
		curheight = curheight * .6666f;
		shapes.setColor(c2);
		shapes.rect(0, beamY - (curheight / 2.0f), width, curheight);
		curheight = curheight * .5f;
		shapes.setColor(c3);
		shapes.rect(0, beamY - (curheight / 2.0f), width, curheight);
		shapes.end();

		TextBounds tb = font.getMultiLineBounds(message);
		float textwidth = tb.width;
		float textheight = tb.height;
		float textX = 0;
		float textY = beamY + (textheight / 2);
		if (progress >= 0.1 && progress < 0.15) {
			textX = ((((width - textwidth) / 2) + textwidth) * ((progress - 0.1f) / 0.05f))
					- textwidth;
		} else if (progress > 0.85 && progress <= 0.9) {
			textX = ((((width - textwidth) / 2) + textwidth) * (1 + ((progress - 0.85f) / 0.05f)))
					- textwidth;
		} else if (progress <= 0.85 && progress >= 0.15) {
			textX = (width - textwidth) / 2;
		} else {
			textX = -2 * textwidth;
		}

		batch.begin();
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, message, textX, textY);
		batch.end();

	}

	/**
	 * Draws the board statically at the given position and size
	 */
	public void drawBoard(Board b, int bx, int by, int tilesize, boolean faded) {

		List<Piece> pieces = b.getAllPieces();
		List<Tile> tiles = b.getAllTiles();

		// Draw Board Background
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Constants.BOARD_COLOR);
		shapes.rect(bx, by, b.getNumHorizontalTiles() * tilesize,
				b.getNumVerticalTiles() * tilesize);
		shapes.end();

		// Draw the basic grid
		shapes.begin(ShapeType.Line);
		shapes.setColor(Constants.LINE_COLOR);
		Gdx.gl.glLineWidth(1);
		for (int i = 0; i <= b.getNumHorizontalTiles(); i++) {
			shapes.line(bx + (i * tilesize), by, bx + (i * tilesize),
					by + (b.getNumVerticalTiles() * tilesize));
		}
		for (int i = 0; i <= b.getNumVerticalTiles(); i++) {
			shapes.line(bx, by + (i * tilesize),
					bx + (b.getNumHorizontalTiles() * tilesize), by
							+ (i * tilesize));
		}
		shapes.end();

		// Draw the tiles
		shapes.begin(ShapeType.Line);
		for (Tile t : tiles) {
			if (t.hasGlass()) {
				shapes.setColor(Constants.LINE_COLOR);
				int glassX = bx + (t.getXCoord() * tilesize);
				int glassY = by + (t.getYCoord() * tilesize);
				shapes.line(glassX, glassY + (0.25f * tilesize), glassX
						+ (0.25f * tilesize), glassY);
				shapes.line(glassX, glassY + (0.5f * tilesize), glassX
						+ (0.5f * tilesize), glassY);
				shapes.line(glassX, glassY + (0.75f * tilesize), glassX
						+ (0.75f * tilesize), glassY);
				shapes.line(glassX, glassY + tilesize, glassX + tilesize,
						glassY);
				shapes.line(glassX + (0.25f * tilesize), glassY + tilesize,
						glassX + tilesize, glassY + (0.25f * tilesize));
				shapes.line(glassX + (0.5f * tilesize), glassY + tilesize,
						glassX + tilesize, glassY + (0.5f * tilesize));
				shapes.line(glassX + (0.75f * tilesize), glassY + tilesize,
						glassX + tilesize, glassY + (0.75f * tilesize));
			}
		}
		shapes.end();
		shapes.begin(ShapeType.Filled);
		for (Tile t : tiles) {
			if (t.hasGoal()) {
				int goalX = bx + (t.getXCoord() * tilesize);
				int goalY = by + (t.getYCoord() * tilesize);
				shapes.setColor(translateColor(t.getGoalColor()));
				shapes.rect(goalX + (0.05f * tilesize), goalY
						+ (0.05f * tilesize), 0.9f * tilesize, 0.9f * tilesize);
				shapes.setColor(Constants.BOARD_COLOR);
				shapes.rect(goalX + (0.12f * tilesize), goalY
						+ (0.12f * tilesize), 0.76f * tilesize,
						0.76f * tilesize);
			}
		}
		shapes.end();
		shapes.begin(ShapeType.Filled);
		for (Tile t : tiles) {
			if (t.hasPainter()) {
				int paintX = bx + (t.getXCoord() * tilesize);
				int paintY = by + (t.getYCoord() * tilesize);
				switch (t.getPainterColor()) {
				case RED:
					shapes.setColor(new Color(.3f, 0, 0, 1));
					break;
				case BLUE:
					shapes.setColor(new Color(0, 0, .3f, 1));
					break;
				case GREEN:
					shapes.setColor(new Color(0, .3f, 0, 1));
					break;
				default:
					shapes.setColor(new Color(0, 0, 0, 0));
					break;
				}
				shapes.rect(paintX + (0.05f * tilesize), paintY
						+ (0.05f * tilesize), 0.9f * tilesize, 0.9f * tilesize);
			}
		}
		shapes.end();
		float moveAnimateTime = 0;

		// Draw the pieces
		List<Tile> path = GameEngine.movePath;
		Color paintColor = new Color(0, 0, 0, 0);
		if (path.size() > 1) {
			paintColor = translateColor(b.getTileAtBoardPosition(
					path.get(1).getXCoord(), path.get(1).getYCoord())
					.getPainterColor());
		}
		Laser disbandedLaser = null;
		Laser movedAlongLaser = null;
		float breakAnimateTime = 0;
		float formAnimateTime = 0;
		float paintAnimateTime = 0;

		batch.begin();
		pieceSprite.setSize(tilesize, tilesize);
		for (Piece p : pieces) {
			pieceSprite.setColor(translateColor(p.getColor()));
			if (p.equals(GameEngine.movingPiece)) {
				float rshift = (paintColor.r - translateColor(p.getColor()).r)
						* paintAnimateTime;
				float gshift = (paintColor.g - translateColor(p.getColor()).g)
						* paintAnimateTime;
				float bshift = (paintColor.b - translateColor(p.getColor()).b)
						* paintAnimateTime;
				pieceSprite.setColor(new Color(translateColor(p.getColor()).r
						+ rshift, translateColor(p.getColor()).g + gshift,
						translateColor(p.getColor()).b + bshift, 1));
			}
			pieceSprite.setPosition(bx + (p.getXCoord() * tilesize),
					by + (p.getYCoord() * tilesize));
			if (path.size() > 1
					&& p.getXCoord() == GameEngine.movingPiece.getXCoord()
					&& p.getYCoord() == GameEngine.movingPiece.getYCoord()) {
				float animateX = (path.get(1).getXCoord() - GameEngine.movingPiece
						.getXCoord()) * tilesize * moveAnimateTime;
				float animateY = (path.get(1).getYCoord() - GameEngine.movingPiece
						.getYCoord()) * tilesize * moveAnimateTime;
				pieceSprite.translate(animateX, animateY);
			}
			pieceSprite.draw(batch);
		}
		batch.end();

		// Draw Lasers
		Set<Laser> lasers = b.lasers;

		shapes.begin(ShapeType.Filled);
		float laserWidth = beamThickness;
		for (Laser l : lasers) {
			if (disbandedLaser != null && l.equals(disbandedLaser)) {
				laserWidth = (1 - breakAnimateTime) * beamThickness;
			} else {
				laserWidth = beamThickness;
			}
			shapes.setColor(translateColor(l.getColor()));
			if (!l.equals(movedAlongLaser)) {
				if (l.getXStart() == l.getXFinish()) {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2))
							* tilesize, by
							+ (l.getYStart() + 0.5f - (laserWidth / 2))
							* tilesize, laserWidth * tilesize,
							(l.getYFinish() - l.getYStart()) * tilesize);
				} else {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2))
							* tilesize, by
							+ (l.getYStart() + 0.5f - (laserWidth / 2))
							* tilesize, (l.getXFinish() - l.getXStart())
							* tilesize, laserWidth * tilesize);
				}
			}
		}

		List<Laser> allFormedLasers = GameEngine.getFormedLaser();
		if (!allFormedLasers.isEmpty()) {
			for (Laser l : allFormedLasers)
				if (l != null) {
					laserWidth = formAnimateTime * beamThickness;
					shapes.setColor(translateColor(l.getColor()));
					if (l.getXStart() == l.getXFinish()) {
						shapes.rect(bx
								+ (l.getXStart() + 0.5f - (laserWidth / 2))
								* tilesize, by
								+ (l.getYStart() + 0.5f - (laserWidth / 2))
								* tilesize, laserWidth * tilesize,
								(l.getYFinish() - l.getYStart()) * tilesize);
					} else {
						shapes.rect(bx
								+ (l.getXStart() + 0.5f - (laserWidth / 2))
								* tilesize, by
								+ (l.getYStart() + 0.5f - (laserWidth / 2))
								* tilesize, (l.getXFinish() - l.getXStart())
								* tilesize, laserWidth * tilesize);
					}
				}
		}
		shapes.end();
		if (faded){
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			shapes.begin(ShapeType.Filled);
			Color fade = Constants.BOARD_COLOR.cpy();
			fade.mul(.2f);
			fade.a = 0.75f;
			shapes.setColor(fade);
			shapes.rect(bx, by, b.getNumHorizontalTiles() * tilesize,
					b.getNumVerticalTiles() * tilesize);
			shapes.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);

		}

		
	}

	public void drawBoardless(Color bg, int currentWorld,
			int currentOrdinalInWorld, Board b) {
		Color curBG = bg;
		Gdx.gl.glClearColor(curBG.r, curBG.g, curBG.b, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		TextBounds tb = null;

		// Draw the buttons
		drawNongameButtons(width, height, tb);
		// Draw the level header
		drawHeader(width, height, tb, currentWorld, currentOrdinalInWorld, b);
	}

	/**
	 * This is the primary game drawing method
	 * 
	 * Draw method called every step during play. Draws the current state of the
	 * game and animations
	 */
	
	public void draw(Board b, GameEngine.GameState state,
			GameEngine.AnimationState aState, int currentWorld,
			int currentOrdinalInWorld, Color bg, float transitionPart,
			boolean partial) {

		// Define drawing variables including sizes and positions as well as
		// objects to be drawn
		int bx = b.getBotLeftX();
		int by = b.getBotLeftY();
		int tilesize = b.getTileSize();
		Color curBG = /* new Color(.1f, .1f, .1f, 1) */bg;
		/*
		 * //Random color fun times! curBG.r = (float) (((2577 + Math.pow(13,
		 * currentLevel))%255)/255.0) curBG.b = (float) (((5648 + Math.pow(7,
		 * currentLevel))%255)/255.0); curBG.g = (float) (((1124 + Math.pow(17,
		 * currentLevel))%255)/255.0);
		 */

		if (!partial) {
			Gdx.gl.glClearColor(curBG.r, curBG.g, curBG.b, 1);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		}
		List<Piece> pieces = b.getAllPieces();
		List<Tile> tiles = b.getAllTiles();
		List<Tile> path = GameEngine.movePath;
		Set<Laser> lasers = b.lasers;

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		TextBounds tb = null;
		float ibeamheight = height / 10.0f;
		Color paintColor = new Color(0, 0, 0, 0);
		if (path.size() > 1) {
			paintColor = translateColor(b.getTileAtBoardPosition(
					path.get(1).getXCoord(), path.get(1).getYCoord())
					.getPainterColor());
		}
		// Define some animation timing
		float moveAnimateTime = 0;
		if (aState == AnimationState.MOVING) {
			moveAnimateTime = ((float) (GameEngine.getTicksSpentOnAnimation()))
					/ (GameEngine.getTotalTicksForAnimation());
		} else if (aState == AnimationState.PAINTING
				|| aState == AnimationState.FORMING) {
			moveAnimateTime = 1;
		}
		Laser disbandedLaser = null;
		Laser movedAlongLaser = null;
		float breakAnimateTime = 0;
		float formAnimateTime = 0;
		float paintAnimateTime = 0;

		if (state == GameState.MOVING) {
			GameEngine.debug(aState);
			disbandedLaser = GameEngine.getBrokenLaser();

			movedAlongLaser = GameEngine.getLaserMovedAlong();
			if (aState == AnimationState.BREAKING) {
				breakAnimateTime = ((float) (GameEngine
						.getTicksSpentOnAnimation()))
						/ GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.MOVING) {
				breakAnimateTime = 1;
			} else if (aState == AnimationState.PAINTING) {
				breakAnimateTime = 1;
				paintAnimateTime = ((float) (GameEngine
						.getTicksSpentOnAnimation()))
						/ GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.FORMING) {
				breakAnimateTime = 1;
				if (!paintColor.equals(new Color(0, 0, 0, 0))
						&& !paintColor
								.equals(translateColor(GameEngine.movingPiece
										.getColor()))) {
					paintAnimateTime = 1;
				}
				formAnimateTime = ((float) (GameEngine
						.getTicksSpentOnAnimation()))
						/ GameEngine.getTotalTicksForAnimation();
			}
		}

		// Draw Board Background
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Constants.BOARD_COLOR);
		shapes.rect(bx + transitionPart, by, b.getNumHorizontalTiles()
				* tilesize, b.getNumVerticalTiles() * tilesize);
		shapes.end();

		// Draw the basic grid
		drawGrid((int) (bx + transitionPart), by, tilesize, b);

		// Draw the tiles
		drawTiles((int) (bx + transitionPart), by, tilesize, tiles);

		// Draw Paths
		drawPaths((int) (bx + transitionPart), by, tilesize, path, aState,
				state, moveAnimateTime);

		// Draw the pieces
		drawPieces((int) (bx + transitionPart), by, tilesize, path, paintColor,
				pieces, paintAnimateTime, moveAnimateTime);

		// Draw Lasers
		drawBeams((int) (bx + transitionPart), by, tilesize, lasers,
				disbandedLaser, movedAlongLaser, aState, path, moveAnimateTime,
				paintAnimateTime, formAnimateTime, breakAnimateTime);

		// Draw the bangs!
		if (state == GameState.DESTROYED
				|| aState == AnimationState.DESTRUCTION) {
			drawBangs(bx, by, tilesize);
		}

		// Drawing progress towards level objectives
		drawGoalProgress(width, height, tb, b, transitionPart);

		drawGameButtons((int) (transitionPart), by, tilesize, b, tb);

		if (!partial) {
			// Draw the buttons
			drawNongameButtons(width, height, tb);
			// Draw the level header
			drawHeader(width, height, tb, currentWorld, currentOrdinalInWorld,
					b);
		}

		// Draw intro
		if (state == GameState.INTRO) {
			drawIntro(bx, by, tilesize, width, height, b,
					GameEngine.getIntroProgress(), tb);
		}

		// Draw level loss reminder
		if (state == GameState.DESTROYED) {
			if (GameEngine.getTimeDead() >= GameEngine.getTimeBeforeDeathBeam()) {
				float progress = (GameEngine.getTimeDead() - GameEngine
						.getTimeBeforeDeathBeam()) / 300.0f;
				if (progress > 0.5) {
					progress = 0.5f;
				}
				drawOverlayBeam(progress, ibeamheight, height / 2.0f,
						translateColor(GameEngine.Color.BLUE),
						"PRESS UNDO OR RESET", titleFont);
			}
		}		
		//Draw outro
		if(state == GameState.WON || (state == GameState.LEVEL_TRANSITION && !partial)){
			drawOutro((int) (bx + transitionPart), by, width, height, b, tb);

		}

	}

	
	/**
	 * Disposes any batches, textures, and fonts being used
	 */
	public void dispose() {
		batch.dispose();
		pieceTexture.dispose();
		titleFont.dispose();
		titleFontNoBest.dispose();
		nonGameMButtonFont.dispose();
		nonGameNLButtonFont.dispose();
		menuButtonFont.dispose();
	}

}
