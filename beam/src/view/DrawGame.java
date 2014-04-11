package view;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import utilities.Constants;

import model.Board;
import model.GameProgress;
import model.Laser;
import model.Menu;
import model.Piece;
import model.Tile;

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
	private static final float beamThickness = 0.1f; //This is measured in units of square size
	
	private SpriteBatch batch;
	private Texture pieceTexture;
	private Sprite pieceSprite;
	private Texture bangTexture;
	private Sprite bangSprite;
	private Texture starTexture;
	private Sprite starSprite;
	public ShapeRenderer shapes;

	private GameProgress gameProgress;

	BitmapFont buttonFont;
	BitmapFont titleFont;
	BitmapFont titleFontNoBest;
	BitmapFont menuButtonFont;
	BitmapFont introFont;

	public static Color translateColor(GameEngine.Color c) {
		switch (c) {
		case RED:
			return new Color(1, .133f, .133f, 1);
		case BLUE:
			return new Color(.133f, .337f, 1, 1);
		case GREEN:
			return new Color(.133f, 1, .177f, 1);
		case ORANGE:
			return new Color(255/255.0f,150/255.0f,20/255.0f, 1);
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
		
		gameProgress = gp;

		TextureRegion pieceregion = new TextureRegion(pieceTexture, 0, 0, 256, 256);
		TextureRegion bangregion = new TextureRegion(bangTexture, 0, 0, 256, 256);
		TextureRegion starregion = new TextureRegion(starTexture, 0, 0, 64, 64);

		pieceSprite = new Sprite(pieceregion);
		bangSprite = new Sprite(bangregion);
		starSprite = new Sprite(starregion);
		shapes = new ShapeRenderer();
	}

	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.internal("data/fonts/swanse.ttf"));
		buttonFont = generator.generateFont(Gdx.graphics.getHeight() / 35);
		titleFont = generator.generateFont(Gdx.graphics.getHeight() / 28);
		titleFontNoBest = generator.generateFont(Gdx.graphics.getHeight() / 25);
		menuButtonFont = generator.generateFont(Gdx.graphics.getHeight() / 45);
		introFont = generator.generateFont(Gdx.graphics.getHeight() / 20);
		generator.dispose();
	}

	/**
	 * Draws the grid on which the game is played
	 */
	private void drawGrid(int bx, int by, int tilesize, Board b){
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
	 * Draws anything that appears in a tile which never moves. This includes glass, painters, and goals
	 */
	private void drawTiles(int bx, int by, int tilesize, List<Tile> tiles){
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
						+ (0.12f * tilesize), 0.76f * tilesize, 0.76f * tilesize);
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
	private void drawPaths(int bx, int by, int tilesize, List<Tile> path, AnimationState aState, GameState state, float moveAnimateTime){
		if(aState != AnimationState.DESTRUCTION){
			shapes.begin(ShapeType.Filled);
			shapes.setColor(new Color(.9f, .9f, .2f, 1f));
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
							+ (0.3f * tilesize), baseX + (.5f * tilesize), baseY
							+ (0.7f * tilesize), baseX + (0.75f * tilesize), baseY
							+ (0.5f * tilesize));
				} else if (finalX < prevX) {
					shapes.triangle(baseX + (.5f * tilesize), baseY
							+ (0.3f * tilesize), baseX + (.5f * tilesize), baseY
							+ (0.7f * tilesize), baseX + (0.25f * tilesize), baseY
							+ (0.5f * tilesize));
				} else if (finalY > prevY) {
					shapes.triangle(baseX + (.3f * tilesize), baseY
							+ (0.5f * tilesize), baseX + (.7f * tilesize), baseY
							+ (0.5f * tilesize), baseX + (0.5f * tilesize), baseY
							+ (0.75f * tilesize));
				} else if (finalY < prevY) {
					shapes.triangle(baseX + (.3f * tilesize), baseY
							+ (0.5f * tilesize), baseX + (.7f * tilesize), baseY
							+ (0.5f * tilesize), baseX + (0.5f * tilesize), baseY
							+ (0.25f * tilesize));
				}
			}
			shapes.end();
		}
	}

	/**
	 * Draws all of the pieces. Draws them between spaces if they're being animated
	 */
	private void drawPieces(int bx, int by, int tilesize, List<Tile> path, Color paintColor, List<Piece> pieces, float paintAnimateTime, float moveAnimateTime){
		
		batch.begin();
		pieceSprite.setSize(tilesize, tilesize);
		for (Piece p : pieces) {
			pieceSprite.setColor(translateColor(p.getColor()));
			if(p.equals(GameEngine.movingPiece)){
				float rshift = (paintColor.r - translateColor(p.getColor()).r) * paintAnimateTime;
				float gshift = (paintColor.g - translateColor(p.getColor()).g) * paintAnimateTime;
				float bshift = (paintColor.b - translateColor(p.getColor()).b) * paintAnimateTime;
				pieceSprite.setColor(new Color(translateColor(p.getColor()).r + rshift, translateColor(p.getColor()).g + gshift, translateColor(p.getColor()).b + bshift, 1));
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
	 * Draws all of the beams between pieces. Also draws intermediate states caused by animations
	 */
	private void drawBeams(int bx, int by, int tilesize, Set<Laser> lasers, Laser disbandedLaser, Laser movedAlongLaser, AnimationState aState, List<Tile> path, float moveAnimateTime, float paintAnimateTime, float formAnimateTime, float breakAnimateTime){
		shapes.begin(ShapeType.Filled);
		float laserWidth = beamThickness;
		for (Laser l : lasers) {
			if (disbandedLaser != null && l.equals(disbandedLaser)){
				laserWidth = (1 - breakAnimateTime) * beamThickness;
			} else {
				laserWidth = beamThickness;
			}
			shapes.setColor(translateColor(l.getColor()));
			if(!l.equals(movedAlongLaser)){
				if (l.getXStart() == l.getXFinish()) {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
							by + (l.getYStart() + 0.5f - (laserWidth / 2)) * tilesize,
							laserWidth * tilesize, (l.getYFinish() - l.getYStart())
							* tilesize);
				} else {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
							by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize,
							(l.getXFinish() - l.getXStart()) * tilesize,
							laserWidth * tilesize);
				}
			}
		}
		
		List<Laser> allFormedLasers = GameEngine.getFormedLaser();
		if (!allFormedLasers.isEmpty()){
			for(Laser l : allFormedLasers)
			if (l != null){
				laserWidth = formAnimateTime * beamThickness; 
				shapes.setColor(translateColor(l.getColor()));
				if (l.getXStart() == l.getXFinish()) {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
							by + (l.getYStart() + 0.5f - (laserWidth / 2)) * tilesize,
							laserWidth * tilesize, (l.getYFinish() - l.getYStart())
							* tilesize);
				} else {
					shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
							by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize,
							(l.getXFinish() - l.getXStart()) * tilesize,
							laserWidth * tilesize);
				}
			}
		}
		if(movedAlongLaser != null && aState != AnimationState.DESTRUCTION){
			laserWidth = beamThickness * (1 - paintAnimateTime);
			shapes.setColor(translateColor(movedAlongLaser.getColor()));
			float moveAnimX = (path.get(1).getXCoord() - GameEngine.movingPiece
					.getXCoord()) * tilesize * moveAnimateTime;
			float moveAnimY = (path.get(1).getYCoord() - GameEngine.movingPiece
					.getYCoord()) * tilesize * moveAnimateTime;
			if(movedAlongLaser.getXStart() == movedAlongLaser.getXFinish()){
				if(movedAlongLaser.getXStart() == GameEngine.movingPiece.getXCoord() && movedAlongLaser.getYStart() == GameEngine.movingPiece.getYCoord()){
					shapes.rect(bx + (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
							(by + (movedAlongLaser.getYStart() + 0.5f - (laserWidth / 2)) * tilesize) + moveAnimY,
							laserWidth * tilesize, ((movedAlongLaser.getYFinish() - movedAlongLaser.getYStart())
							* tilesize) - moveAnimY);
				} else {
					shapes.rect(bx + (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
							(by + (movedAlongLaser.getYStart() + 0.5f - (laserWidth / 2)) * tilesize),
							laserWidth * tilesize, ((movedAlongLaser.getYFinish() - movedAlongLaser.getYStart())
							* tilesize) + moveAnimY);
				}
			} else {
				if(movedAlongLaser.getXStart() == GameEngine.movingPiece.getXCoord() && movedAlongLaser.getYStart() == GameEngine.movingPiece.getYCoord()){
					shapes.rect((bx + (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2)) * tilesize) + moveAnimX,
							by + (movedAlongLaser.getYStart() + 0.5f - (laserWidth/2)) * tilesize,
							((movedAlongLaser.getXFinish() - movedAlongLaser.getXStart()) * tilesize) - moveAnimX,
							laserWidth * tilesize);
				} else {
					shapes.rect((bx + (movedAlongLaser.getXStart() + 0.5f - (laserWidth / 2)) * tilesize),
							by + (movedAlongLaser.getYStart() + 0.5f - (laserWidth/2)) * tilesize,
							((movedAlongLaser.getXFinish() - movedAlongLaser.getXStart()) * tilesize) + moveAnimX,
							laserWidth * tilesize);
				}
			}
		}
		shapes.end();
	}

	/**
	 * Draws the BANG effect where a piece has been destroyed
	 */
	private void drawBangs(int bx, int by, int tilesize){
		batch.begin();
		List<Piece> destroyedPieces = GameEngine.getDestroyedPieces();
		if(destroyedPieces.size() > 0){
			bangSprite.setSize(tilesize, tilesize);
			for(Piece dp : destroyedPieces){
				bangSprite.setPosition(bx + (dp.getXCoord() * tilesize),
						by + (dp.getYCoord() * tilesize));
				bangSprite.setColor(translateColor(dp.getColor()));
				bangSprite.draw(batch);
			}
		}
		batch.end();
	}

	/**
	 * Draws the control buttons on the HUD
	 */
	private void drawButtons(int width, int height, TextBounds tb){
		batch.begin();
		buttonFont.setColor(Color.WHITE);

		tb = buttonFont.getBounds("UNDO");

		float textHeight = height * Menu.buttonBotY
				+ (Menu.buttonHeight * height + tb.height) / 2;
		buttonFont.draw(batch, "UNDO", Menu.undoButtonLeftX * width
				+ (Menu.undoButtonWidth * width - tb.width) / 2, textHeight);
		buttonFont.draw(batch, "REDO", Menu.redoButtonLeftX * width
				+ (Menu.redoButtonWidth * width - tb.width) / 2, textHeight);
		tb = buttonFont.getBounds("RESET");
		buttonFont.draw(batch, "RESET", Menu.resetButtonLeftX * width
				+ (Menu.resetButtonWidth * width - tb.width) / 2, textHeight);

		menuButtonFont.setColor(Color.WHITE);
		tb = menuButtonFont.getBounds("MENU");
		textHeight = height * Menu.menuButtonBotY
				+ (Menu.menuButtonHeight * height + tb.height) / 2;
		menuButtonFont.draw(batch, "MENU", Menu.menuButtonLeftX * width
				+ (Menu.menuButtonWidth * width - tb.width) / 2, textHeight);
		batch.end();
	}

	/**
	 * Draws the text at the top of the screen explaining the level goals and how near they are to being completed
	 */
	private void drawLevelProgress(int width, int height, TextBounds tb, int currentWorld, int currentOrdinalInWorld, Board b){
		batch.begin();
		titleFont.setColor(Color.WHITE);
		titleFontNoBest.setColor(Color.WHITE);
		String toPrint;
		int moves = gameProgress.getLevelMoves(currentWorld, currentOrdinalInWorld);
		float movesAdjust = 0.0f;
		if (moves != 0) {
			movesAdjust = 0.15f;
		}
		if (b.getBeamObjectiveSet().isEmpty()) {
			toPrint = b.getNumGoalsFilled() + " out of " + b.getNumGoalTiles()
					+ " goals filled.";
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - Constants.TOP_BAR_SIZE * 0.4f - Constants.TOP_BAR_SIZE
							* movesAdjust));
		} else {
			/*
			 * int beamObjective = 0; int curLaserCount = 0;
			 */
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

			Integer total;
			Integer existing;
			ArrayList<String> colorGoals = new ArrayList<String>();
			for (GameEngine.Color c : GameEngine.Color.values()) {
				total = beamObjective.get(c);
				existing = curLaserCount.get(c);
				if (total != null && total != 0) {
					toPrint = existing + " of " + total + " " + c
							+ " beams made.";
					colorGoals.add(toPrint);
				}
			}
			if (colorGoals.size() == 0) {
				toPrint = "Break all beams.";
				tb = titleFont.getBounds(toPrint);
				titleFont
						.draw(batch,
								toPrint,
								(width - tb.width) / 2,
								height
										* (1 - Constants.TOP_BAR_SIZE * 0.4f - Constants.TOP_BAR_SIZE
												* movesAdjust));
			} else {
				for (int i = 0; i < colorGoals.size(); i++) {
					toPrint = colorGoals.get(i);
					tb = titleFont.getBounds(toPrint);
					titleFont
							.draw(batch,
									toPrint,
									(width - tb.width) / 2,
									height
											* (1 - Constants.TOP_BAR_SIZE * 0.4f
													- Constants.TOP_BAR_SIZE
													* movesAdjust - Constants.TOP_BAR_SIZE
													* i * .15f));
				}
			}
		}

		toPrint = "Moves: " + GameEngine.getMoveCount() + " Perfect: "
				+ b.perfect;

		if (moves != 0) {
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - Constants.TOP_BAR_SIZE * 0.22f));
			// .3
		} else {
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - Constants.TOP_BAR_SIZE * 0.22f));
			// .36
		}

		if (moves != 0) {
			toPrint = "Your Best: " + moves;
			tb = titleFont.getBounds(toPrint);

			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - Constants.TOP_BAR_SIZE * 0.39f));
			// .525
		}
		batch.end();
	}

	/**
	 * Draws the level introduction beam that appears at level start
	 */
	private void drawIntro(int width, int height, float ibeamheight, Board b){
		GameEngine.Color baseColor = GameEngine.Color.RED;
		float progress = GameEngine.getIntroProgress();
		GameEngine.debug("intro: " + progress + " " + height + " ");
		if(b.getBeamObjectiveSet().isEmpty()){
			String message = "FILL " + b.getNumGoalTiles() + " " + (b.getNumGoalTiles()==1?"GOAL":"GOALS");
			drawOverlayBeam(progress, ibeamheight, height / 2.0f, translateColor(baseColor), message, introFont);
		} else {
			int totalBeams = 0;
			for(GameEngine.Color c : b.getBeamObjectiveSet()){
				totalBeams += b.getBeamObjectiveCount(c);
			}
			if(totalBeams == 0){
				String message = "BREAK ALL BEAMS";
				drawOverlayBeam(progress, ibeamheight, height / 2.0f, translateColor(GameEngine.Color.BLUE), message, introFont);
			} else {
				int beamsToDraw = b.getBeamObjectiveSet().size() + 1;
				float totBeamHeight = ((beamsToDraw) +  ((beamsToDraw - 1)/ 2.0f)) * ibeamheight;
				float baseheight = ((height - totBeamHeight) / 2.0f) + totBeamHeight - (ibeamheight / 2.0f);
				String message = "FORM";
				drawOverlayBeam(progress, ibeamheight, baseheight, translateColor(baseColor), message, introFont);
				int i = 1;
				for(GameEngine.Color c : b.getBeamObjectiveSet()){
					String bmessage = b.getBeamObjectiveCount(c) + " " + c.toString() + " BEAM" + (b.getBeamObjectiveCount(c) == 1?"":"S");
					drawOverlayBeam(progress, ibeamheight, baseheight - ((1.5f) * i * ibeamheight), translateColor(c), bmessage, introFont);
					i++;
				}
			}
		}
	}

	/**
	 * Draws the sequence that appears after completing a level
	 */
	private void drawOutro(int width, int height, Board b){
		float au = GameEngine.getWonAnimationUnit();
		float timeWon = GameEngine.getTimeWon();
		float starBeamWidth = width / 6.0f;
		float squareSize = 0;
		int numStars = 1;
		if (GameEngine.getMoveCount() <= b.perfect){
			numStars = 3;
		} else if (GameEngine.getMoveCount() <= b.par){
			numStars = 2;
		}
		if(timeWon < au){
			squareSize = (timeWon / au) * width;
		} else {
			squareSize = width;
		}
		float beam1X = (width - (4 * starBeamWidth)) / 2.0f;
		float beam1Progress = (timeWon - au) / (2 * au);
		float beam2Progress = 0, beam3Progress = 0;
		if(numStars >= 2){
			beam2Progress = (timeWon - (2 * au)) / (2 * au);
		}
		if(numStars == 3){
			beam3Progress = (timeWon - (3 * au)) / (2 * au);
		}
		float beam2X = beam1X + (1.5f * starBeamWidth);
		float beam3X = beam2X + (1.5f * starBeamWidth);
		float starheight = (height / 2.0f) + (0.2f * width);
		
		float horBeamProgress = 0;
		if(timeWon >= (numStars + 2) * au){
			horBeamProgress = (timeWon - (numStars + 2) * au) / (2 * au);
		}
		float horBeamY = 4.5f * height / 10f;
		float horBeamHeight = height / 10f;
		
		String levelEndMessage = "GOOD!";
		if(numStars == 2){
			levelEndMessage = "EXCELLENT!";
		}
		if(numStars == 3){
			levelEndMessage = "PERFECT!";
		}
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(new Color(0.4f,0.4f,0.4f,0.4f));
		shapes.rect(0,0,width, height);
		shapes.setColor(new Color(0,0,0,0.92f));
		shapes.rect((width - squareSize ) / 2.0f, (height - (squareSize)) / 2.0f, squareSize, squareSize);
		shapes.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
		
		batch.begin();
		starSprite.setSize(starBeamWidth, starBeamWidth);
		starSprite.setColor(Color.YELLOW);
		if(numStars >= 1 && timeWon >= 2 * au){
			starSprite.setPosition(beam1X, starheight);
			starSprite.draw(batch);
		}
		if(numStars >= 2 && timeWon >= 3 * au){
			starSprite.setPosition(beam2X, starheight);
			starSprite.draw(batch);
		}
		if(numStars == 3 && timeWon >= 4 * au){
			starSprite.setPosition(beam3X, starheight);
			starSprite.draw(batch);
		}
		if(timeWon > (numStars + 3) * au){
			TextBounds endTextBounds = introFont.getBounds(levelEndMessage);
			introFont.setColor(Color.WHITE);
			introFont.draw(batch, levelEndMessage, (width - endTextBounds.width) / 2.0f, (height + endTextBounds.height) / 2.0f);
		}
		batch.end();
		
		float buttonTextProgress;
		if(timeWon >= (numStars + 2) * au){
			buttonTextProgress = (timeWon - (numStars + 2) * au) / (2 * au);
		} else {
			buttonTextProgress = 0;
		}
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapes.begin(ShapeType.Line);
		shapes.setColor(new Color(1,1,1,buttonTextProgress));
		float space = width / 75.0f;
		float buttonWidth = Menu.wonButtonWidth * width - (2 * space);
		float buttonHeight = Menu.wonButtonHeight * height - (2 * space);
		float buttonBotPos = Menu.wonButtonBotY * height + space;
		shapes.rect(Menu.wonRetryButtonLeftX * width + space, buttonBotPos, buttonWidth, buttonHeight);
		shapes.rect(Menu.wonMenuButtonLeftX * width + space, buttonBotPos, buttonWidth, buttonHeight);
		shapes.rect(Menu.wonNextLevelButtonLeftX * width + space, buttonBotPos, buttonWidth, buttonHeight);
		shapes.end();
		
		batch.begin();
		titleFont.setColor(new Color(1,1,1,buttonTextProgress));
		float centerHeight = (Menu.wonButtonBotY + Menu.wonButtonHeight / 2) * height;
		float halfButtonWidth = Menu.wonButtonWidth / 2 * width;
		String buttonText = "RETRY";
		TextBounds buttonTB = titleFont.getBounds(buttonText);
		titleFont.draw(batch, buttonText, (width * Menu.wonRetryButtonLeftX) + halfButtonWidth - (buttonTB.width / 2.0f), centerHeight + (buttonTB.height / 2.0f));
		buttonText = "MENU";
		buttonTB = titleFont.getBounds(buttonText);
		titleFont.draw(batch, buttonText, (width * Menu.wonMenuButtonLeftX) + halfButtonWidth - (buttonTB.width / 2.0f), centerHeight + (buttonTB.height / 2.0f));
		buttonText = "NEXT";
		buttonTB = titleFont.getBounds(buttonText);
		titleFont.draw(batch, buttonText, (width * Menu.wonNextLevelButtonLeftX) + halfButtonWidth - (buttonTB.width / 2.0f), centerHeight + (buttonTB.height / 2.0f));
		batch.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);

		
		drawVerticalTempBeam(beam1Progress, beam1X, starBeamWidth, translateColor(GameEngine.Color.RED));
		drawVerticalTempBeam(beam2Progress, beam2X, starBeamWidth, translateColor(GameEngine.Color.BLUE));
		drawVerticalTempBeam(beam3Progress, beam3X, starBeamWidth, translateColor(GameEngine.Color.GREEN));
		drawHorizontalTempBeam(horBeamProgress, horBeamY, horBeamHeight, translateColor(GameEngine.Color.BLUE));
	}

	/**
	 * Draws a horizontal beam like the level intro, or the level loss reminder banner.
	 */
	private void drawOverlayBeam(float progress, float ibeamheight, float beamY, Color baseColor, String message, BitmapFont font){
		Color c1 = baseColor;
		Color c2 = new Color(1.4f * c1.r, 1.4f * c1.g, 1.4f * c1.b, 1);
		Color c3 = new Color(1.8f * c1.r, 1.8f * c1.g, 1.8f * c1.b, 1);
		float curheight = 0;
		
		if(progress <= 0.1){
			curheight = (progress / 0.1f) * ibeamheight;
		} else if (progress > 0.9){
			curheight = ((1-progress) / 0.1f) * ibeamheight;
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
		if(progress >= 0.1 && progress < 0.15){
			textX = ((((width - textwidth) / 2) + textwidth) * ((progress - 0.1f) / 0.05f)) - textwidth;
		} else if (progress > 0.85 && progress <= 0.9) {
			textX = ((((width - textwidth) / 2) + textwidth) * (1 + ((progress - 0.85f) / 0.05f))) - textwidth;
		} else if (progress <= 0.85 && progress >= 0.15){
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
	 * Draws the vertical beams presently used for level outro
	 */
	private void drawVerticalTempBeam(float progress, float posX, float width, Color baseColor){
		float trueWidth = 0;
		int height = Gdx.graphics.getHeight();
		if(progress < 0 || progress > 1){
			return;
		}
		if(progress <= 0.5){
			trueWidth = (progress / 0.5f) * width;
		} else {
			trueWidth = (2 - (progress /0.5f)) * width;
		}
		
		Color c1 = baseColor;
		Color c2 = new Color(1.4f * c1.r, 1.4f * c1.g, 1.4f * c1.b, 1);
		Color c3 = new Color(1.8f * c1.r, 1.8f * c1.g, 1.8f * c1.b, 1);
		
		shapes.begin(ShapeType.Filled);
		shapes.setColor(c1);
		shapes.rect(posX + ((width -trueWidth) / 2), 0, trueWidth, height);
		trueWidth = trueWidth * .6666f;
		shapes.setColor(c2);
		shapes.rect(posX + ((width -trueWidth) / 2), 0, trueWidth, height);
		trueWidth = trueWidth * .5f;
		shapes.setColor(c3);
		shapes.rect(posX + ((width -trueWidth) / 2), 0, trueWidth, height);
		shapes.end();
	}
	
	
	/**
	 * Draws the horizontal beams presently used for level outro
	 */
	private void drawHorizontalTempBeam(float progress, float posY, float height, Color baseColor){
		float trueHeight = 0;
		int width = Gdx.graphics.getWidth();
		if(progress < 0 || progress > 1){
			return;
		}
		if(progress <= 0.5){
			trueHeight = (progress / 0.5f) * height;
		} else {
			trueHeight = (2 - (progress /0.5f)) * height;
		}
		
		Color c1 = baseColor;
		Color c2 = new Color(1.4f * c1.r, 1.4f * c1.g, 1.4f * c1.b, 1);
		Color c3 = new Color(1.8f * c1.r, 1.8f * c1.g, 1.8f * c1.b, 1);
		
		shapes.begin(ShapeType.Filled);		
		shapes.setColor(c1);
		shapes.rect(0, posY + ((height -trueHeight) / 2), width, trueHeight);
		trueHeight = trueHeight * .6666f;
		shapes.setColor(c2);
		shapes.rect(0, posY + ((height -trueHeight) / 2), width, trueHeight);
		trueHeight = trueHeight * .5f;
		shapes.setColor(c3);
		shapes.rect(0, posY + ((height -trueHeight) / 2), width, trueHeight);
		shapes.end();
	}
	
	
	/**
	* Draws the board statically at the given position and size
	*/
	public void drawBoard(Board b, int bx, int by, int tilesize, Color curBG){
		
			List<Piece> pieces = b.getAllPieces();
			List<Tile> tiles = b.getAllTiles();
			
			//Draw Board Background
			shapes.begin(ShapeType.Filled);
			shapes.setColor(Constants.BOARD_COLOR);
			shapes.rect(bx, by, b.getNumHorizontalTiles() * tilesize, b.getNumVerticalTiles() * tilesize);
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
							+ (0.12f * tilesize), 0.76f * tilesize, 0.76f * tilesize);
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
			Color paintColor = new Color(0,0,0,0);
			if(path.size() > 1){
				paintColor = translateColor(b.getTileAtBoardPosition(path.get(1).getXCoord(), path.get(1).getYCoord()).getPainterColor());
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
				if(p.equals(GameEngine.movingPiece)){
					float rshift = (paintColor.r - translateColor(p.getColor()).r) * paintAnimateTime;
					float gshift = (paintColor.g - translateColor(p.getColor()).g) * paintAnimateTime;
					float bshift = (paintColor.b - translateColor(p.getColor()).b) * paintAnimateTime;
					pieceSprite.setColor(new Color(translateColor(p.getColor()).r + rshift, translateColor(p.getColor()).g + gshift, translateColor(p.getColor()).b + bshift, 1));
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
				if (disbandedLaser != null && l.equals(disbandedLaser)){
					laserWidth = (1 - breakAnimateTime) * beamThickness;
				} else {
					laserWidth = beamThickness;
				}
				shapes.setColor(translateColor(l.getColor()));
				if(!l.equals(movedAlongLaser)){
					if (l.getXStart() == l.getXFinish()) {
						shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
								by + (l.getYStart() + 0.5f - (laserWidth / 2)) * tilesize,
								laserWidth * tilesize, (l.getYFinish() - l.getYStart())
								* tilesize);
					} else {
						shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
								by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize,
								(l.getXFinish() - l.getXStart()) * tilesize,
								laserWidth * tilesize);
					}
				}
			}
			
			List<Laser> allFormedLasers = GameEngine.getFormedLaser();
			if (!allFormedLasers.isEmpty()){
				for(Laser l : allFormedLasers)
				if (l != null){
					laserWidth = formAnimateTime * beamThickness; 
					shapes.setColor(translateColor(l.getColor()));
					if (l.getXStart() == l.getXFinish()) {
						shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
								by + (l.getYStart() + 0.5f - (laserWidth / 2)) * tilesize,
								laserWidth * tilesize, (l.getYFinish() - l.getYStart())
								* tilesize);
					} else {
						shapes.rect(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize,
								by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize,
								(l.getXFinish() - l.getXStart()) * tilesize,
								laserWidth * tilesize);
					}
				}
			}
			
			shapes.end();			
	}
	
	/**
	 * This is the primary game drawing method
	 * 
	 * Draw method called every step during play. Draws the current state of the game and animations
	 */
	public void draw(Board b, GameEngine.GameState state,
		GameEngine.AnimationState aState, int currentWorld,
		int currentOrdinalInWorld, Color bg) {
		
		
		//Define drawing variables including sizes and positions as well as objects to be drawn
		int bx = b.getBotLeftX();
		int by = b.getBotLeftY();
		int tilesize = b.getTileSize();
		Color curBG = /*new Color(.1f, .1f, .1f, 1)*/bg;
		/*//Random color fun times!
		curBG.r = (float) (((2577 + Math.pow(13, currentLevel))%255)/255.0)
		curBG.b = (float) (((5648 + Math.pow(7, currentLevel))%255)/255.0);
		curBG.g = (float) (((1124 + Math.pow(17, currentLevel))%255)/255.0);
		*/
		if (state == GameState.DESTROYED) {
			curBG = new Color(.5f, 0, 0, 1);
		}
		Gdx.gl.glClearColor(curBG.r, curBG.g, curBG.b, 1);

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		List<Piece> pieces = b.getAllPieces();
		List<Tile> tiles = b.getAllTiles();
		List<Tile> path = GameEngine.movePath;
		Set<Laser> lasers = b.lasers;		
		
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		TextBounds tb = null;
		float ibeamheight = height / 10.0f;
		Color paintColor = new Color(0,0,0,0);
		if(path.size() > 1){
			paintColor = translateColor(b.getTileAtBoardPosition(path.get(1).getXCoord(), path.get(1).getYCoord()).getPainterColor());
		}
		//Define some animation timing
		float moveAnimateTime = 0;
		if(aState == AnimationState.MOVING){
			moveAnimateTime =((float)(GameEngine.getTicksSpentOnAnimation()))/(GameEngine.getTotalTicksForAnimation());
		} else if (aState == AnimationState.PAINTING || aState == AnimationState.FORMING){
			moveAnimateTime = 1;
		}
		Laser disbandedLaser = null;
		Laser movedAlongLaser = null;
		float breakAnimateTime = 0;
		float formAnimateTime = 0;
		float paintAnimateTime = 0;
				
		if(state == GameState.MOVING){
			GameEngine.debug(aState);
			disbandedLaser = GameEngine.getBrokenLaser();

			movedAlongLaser = GameEngine.getLaserMovedAlong();
			if(aState == AnimationState.BREAKING){
				breakAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.MOVING){
				breakAnimateTime = 1;
			} else if (aState == AnimationState.PAINTING){
				breakAnimateTime = 1;
				paintAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.FORMING){
				breakAnimateTime = 1;
				if(!paintColor.equals(new Color(0,0,0,0)) && !paintColor.equals(translateColor(GameEngine.movingPiece.getColor()))){
					paintAnimateTime = 1;
				}
				formAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			}
		}
		
		//Draw Board Background
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Constants.BOARD_COLOR);
		shapes.rect(bx, by, b.getNumHorizontalTiles() * tilesize, b.getNumVerticalTiles() * tilesize);
		shapes.end();

		// Draw the basic grid
		drawGrid(bx, by, tilesize, b);
		
		// Draw the tiles
		drawTiles(bx, by, tilesize, tiles);

		// Draw Paths
		drawPaths(bx, by, tilesize, path, aState, state, moveAnimateTime);

		// Draw the pieces
		drawPieces(bx, by, tilesize, path, paintColor, pieces, paintAnimateTime, moveAnimateTime);		

		// Draw Lasers
		drawBeams(bx, by, tilesize, lasers, disbandedLaser, movedAlongLaser, aState, path, moveAnimateTime, paintAnimateTime, formAnimateTime, breakAnimateTime);
				
		//Draw the bangs!
		if(state == GameState.DESTROYED || aState == AnimationState.DESTRUCTION){
			drawBangs(bx, by, tilesize);
		}		

		// Draw the buttons
		drawButtons(width, height, tb);

		// Drawing progress towards level objectives
		drawLevelProgress(width, height, tb, currentWorld, currentOrdinalInWorld, b);
		
		// Draw intro
		if (state == GameState.INTRO) {
			drawIntro(width, height, ibeamheight, b);
		}
		
		//Draw level loss reminder
		if(state == GameState.DESTROYED){
			if(GameEngine.getTimeDead() >= GameEngine.getTimeBeforeDeathBeam()){
				float progress = (GameEngine.getTimeDead() - GameEngine.getTimeBeforeDeathBeam()) / 300.0f;
				if(progress > 0.5) {
					progress = 0.5f;
				}
				drawOverlayBeam(progress, ibeamheight, height/ 2.0f, translateColor(GameEngine.Color.BLUE), "PRESS UNDO OR RESET", titleFont);
			}
		}
		
		//Draw outro
		if(state == GameState.WON){
			drawOutro(width, height, b);
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
		buttonFont.dispose();
		menuButtonFont.dispose();
	}

}
