package com.me.beam;

import java.util.List;
import java.util.Set;

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
import com.me.beam.GameEngine.GameState;

public class DrawGame {
	private SpriteBatch batch;
	private Texture pieceTexture;
	private Sprite pieceSprite;
	private ShapeRenderer shapes;

	private GameProgress gameProgress;

	BitmapFont buttonFont;
	BitmapFont titleFont;
	BitmapFont titleFontNoBest;
	BitmapFont menuButtonFont;

	public static Color translateColor(GameEngine.Color c){
		switch (c) {
		case RED:
			return new Color(1, .133f, .133f, 1);
		case BLUE:
			return new Color(.133f, .337f, 1, 1);
		case GREEN:
			return new Color(.133f, 1, .177f, 1);
		default:
			return new Color(0, 0, 0, 0);
		}
	}
	
	public DrawGame(GameProgress gp) {
		batch = new SpriteBatch();

		pieceTexture = new Texture(Gdx.files.internal("data/piece.png"));
		pieceTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		gameProgress = gp;

		TextureRegion region = new TextureRegion(pieceTexture, 0, 0, 256, 256);

		pieceSprite = new Sprite(region);
		shapes = new ShapeRenderer();
	}

	public void initFonts(){
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		buttonFont = generator.generateFont(Gdx.graphics.getHeight() / 35);
		titleFont = generator.generateFont(Gdx.graphics.getHeight() / 28);
		titleFontNoBest = generator.generateFont(Gdx.graphics.getHeight() / 25);
		menuButtonFont = generator.generateFont(Gdx.graphics.getHeight() / 45);
		generator.dispose();
	}

	public void draw(Board b, GameEngine.GameState state, GameEngine.AnimationState aState, int currentLevel) {
		int bx = b.getBotLeftX();
		int by = b.getBotLeftY();
		int tilesize = b.getTileSize();
		Color curBG = new Color(.1f, .1f, .1f, 1);
		if (state == GameState.DESTROYED) {
			curBG = new Color(.5f, 0, 0, 1);
		}
		if (state == GameState.WON) {
			curBG = new Color(0, 0.3f, 0, 1);
		}
		Gdx.gl.glClearColor(curBG.r, curBG.g, curBG.b, 1);

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		List<Piece> pieces = b.getAllPieces();
		List<Tile> tiles = b.getAllTiles();

		// Draw the basic grid
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.WHITE);
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
			if (t.isGlass) {
				shapes.setColor(Color.WHITE);
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
				shapes.setColor(curBG);
				shapes.rect(goalX + (0.15f * tilesize), goalY
						+ (0.15f * tilesize), 0.7f * tilesize, 0.7f * tilesize);
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

		// Draw Paths
		List<Tile> path = GameEngine.movePath;
		float animateTime = ((float)(GameEngine.getTimeOnThisTile()))/(GameEngine.getTicksPerTile());
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
							.getXCoord()) * animateTime;
					shiftY = (path.get(1).getYCoord() - GameEngine.movingPiece
							.getYCoord()) * animateTime;
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

		// Draw the pieces
		path = GameEngine.movePath;
		batch.begin();
		pieceSprite.setSize(tilesize, tilesize);
		for (Piece p : pieces) {
			pieceSprite.setColor(translateColor(p.getColor()));
			pieceSprite.setPosition(bx + (p.getXCoord() * tilesize),
					by + (p.getYCoord() * tilesize));
			if (path.size() > 1
					&& p.getXCoord() == GameEngine.movingPiece.getXCoord()
					&& p.getYCoord() == GameEngine.movingPiece.getYCoord()) {
				float animateX = (path.get(1).getXCoord() - GameEngine.movingPiece
						.getXCoord()) * tilesize * animateTime;
				float animateY = (path.get(1).getYCoord() - GameEngine.movingPiece
						.getYCoord()) * tilesize * animateTime;
				pieceSprite.translate(animateX, animateY);
			}
			pieceSprite.draw(batch);
		}
		batch.end();

		// Draw Lasers
		Set<Laser> lasers = b.lasers;
		shapes.begin(ShapeType.Filled);
		for (Laser l : lasers) {
			shapes.setColor(translateColor(l.getColor()));
			if (l.getXStart() == l.getXFinish()) {
				shapes.rect(bx + (l.getXStart() + 0.45f) * tilesize,
						by + (l.getYStart() + 0.45f) * tilesize,
						0.1f * tilesize, (l.getYFinish() - l.getYStart())
						* tilesize);
			} else {
				shapes.rect(bx + (l.getXStart() + 0.45f) * tilesize,
						by + (l.getYStart() + 0.45f) * tilesize,
						(l.getXFinish() - l.getXStart()) * tilesize,
						0.1f * tilesize);
			}
		}
		shapes.end();
		
		// Draw the buttons
		batch.begin();
		buttonFont.setColor(Color.WHITE);

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		TextBounds tb = buttonFont.getBounds("UNDO");

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

		// Drawing progress towards level objectives
		batch.begin();
		String toPrint;
		int moves = gameProgress.getLevelMoves(currentLevel);

		if (b.getBeamObjectiveSet().isEmpty()) {
			toPrint = b.getNumGoalsFilled() + " out of " + b.goalTiles.size()
					+ " goals filled.";
		} else {
			int beamObjective = 0;
			int curLaserCount = 0;

			for (GameEngine.Color c : b.getBeamObjectiveSet()) {
				beamObjective += b.getBeamObjectiveCount(c);
				curLaserCount += b.getLaserCount(c);
			}
			toPrint = curLaserCount + " out of " + beamObjective + " lasers.";
		}

		//Draw differently if the level has been completed
		titleFont.setColor(Color.WHITE);
		titleFontNoBest.setColor(Color.WHITE);
		if (moves != -1){
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize*0.75f));
		} else {
			tb = titleFontNoBest.getBounds(toPrint);
			titleFontNoBest.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize*0.64f));
		}

		toPrint = "Moves: " + GameEngine.getMoveCount() + " Perfect: " + b.perfect;
		
		if (moves != -1){
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize*0.3f));
		} else {
			tb = titleFontNoBest.getBounds(toPrint);
			titleFontNoBest.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize*0.36f));
		}

		if (moves != -1){
			toPrint = "Your Best: " + moves;
			tb = titleFont.getBounds(toPrint);

			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize*0.525f));
		}
		batch.end();
	}

	public void dispose() {
		batch.dispose();
		pieceTexture.dispose();
		titleFont.dispose();
		titleFontNoBest.dispose();
		buttonFont.dispose();
		menuButtonFont.dispose();
	}

}
