package com.me.beam;

import java.util.ArrayList;
import java.util.EnumMap;
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
import com.me.beam.GameEngine.AnimationState;
import com.me.beam.GameEngine.GameState;

public class DrawGame {
	private static final float beamThickness = 0.1f; //This is measured in units of square size
	
	private SpriteBatch batch;
	private Texture pieceTexture;
	private Sprite pieceSprite;
	private ShapeRenderer shapes;

	private GameProgress gameProgress;

	BitmapFont buttonFont;
	BitmapFont titleFont;
	BitmapFont titleFontNoBest;
	BitmapFont menuButtonFont;

	public static Color translateColor(GameEngine.Color c) {
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

	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.internal("data/fonts/swanse.ttf"));
		buttonFont = generator.generateFont(Gdx.graphics.getHeight() / 35);
		titleFont = generator.generateFont(Gdx.graphics.getHeight() / 28);
		titleFontNoBest = generator.generateFont(Gdx.graphics.getHeight() / 25);
		menuButtonFont = generator.generateFont(Gdx.graphics.getHeight() / 45);
		generator.dispose();
	}

	public void draw(Board b, GameEngine.GameState state,
			GameEngine.AnimationState aState, int currentLevel) {
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
		float moveAnimateTime = 0;
		if(aState == AnimationState.MOVING){
			moveAnimateTime =((float)(GameEngine.getTicksSpentOnAnimation()))/(GameEngine.getTotalTicksForAnimation());
		} else if (aState == AnimationState.PAINTING || aState == AnimationState.FORMING){
			moveAnimateTime = 1;
		}
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

		// Draw the pieces
		path = GameEngine.movePath;
		Color paintColor = new Color(0,0,0,0);
		if(path.size() > 1){
			paintColor = translateColor(b.getTileAtBoardPosition(path.get(1).getXCoord(), path.get(1).getYCoord()).getPainterColor());
		}
		Laser disbandedLaser = null;
		Laser formedLaser = null;
		Laser movedAlongLaser = null;
		float breakAnimateTime = 0;
		float formAnimateTime = 0;
		float moveAlongAnimateTime = 0;
		float paintAnimateTime = 0;
		if(state == GameState.MOVING){
			System.out.println(aState);
			disbandedLaser = GameEngine.getBrokenLaser();
			
			//TODO: This is where I changed the code Ryan.
			List<Laser> allLasers = GameEngine.getFormedLaser();
			if (!allLasers.isEmpty())
				formedLaser = allLasers.get(0);
			
			movedAlongLaser = GameEngine.getLaserMovedAlong();
			if(aState == AnimationState.BREAKING){
				breakAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.MOVING){
				breakAnimateTime = 1;
				moveAlongAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.PAINTING){
				breakAnimateTime = 1;
				moveAlongAnimateTime = 1;
				paintAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			} else if (aState == AnimationState.FORMING){
				System.out.println("Is formed laser null? " + (formedLaser == null));
				breakAnimateTime = 1;
				moveAlongAnimateTime = 1;
				if(!paintColor.equals(new Color(0,0,0,0))){
					paintAnimateTime = 1;
				}
				formAnimateTime = ((float)(GameEngine.getTicksSpentOnAnimation())) / GameEngine.getTotalTicksForAnimation();
			}
		}
		
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
		if (formedLaser != null){
			laserWidth = formAnimateTime * beamThickness; 
			System.out.println("Changing width " + laserWidth);
			Laser l = formedLaser;
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
		if(movedAlongLaser != null){
			laserWidth = beamThickness;
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
		titleFont.setColor(Color.WHITE);
		titleFontNoBest.setColor(Color.WHITE);
		String toPrint;
		int moves = gameProgress.getLevelMoves(currentLevel);
		float movesAdjust = 0.0f;
		if (moves != -1) {
			System.out.println("Moves found!");
			movesAdjust = 0.15f;
		}
		if (b.getBeamObjectiveSet().isEmpty()) {
			toPrint = b.getNumGoalsFilled() + " out of " + b.goalTiles.size()
					+ " goals filled.";
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize * 0.4f - GameEngine.topBarSize
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
										* (1 - GameEngine.topBarSize * 0.4f - GameEngine.topBarSize
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
											* (1 - GameEngine.topBarSize * 0.4f
													- GameEngine.topBarSize
													* movesAdjust - GameEngine.topBarSize
													* i * .15f));
				}
			}
		}

		toPrint = "Moves: " + GameEngine.getMoveCount() + " Perfect: "
				+ b.perfect;

		if (moves != -1) {
			tb = titleFont.getBounds(toPrint);
			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize * 0.22f));
			// .3
		} else {
			tb = titleFontNoBest.getBounds(toPrint);
			titleFontNoBest.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize * 0.22f));
			// .36
		}

		if (moves != -1) {
			toPrint = "Your Best: " + moves;
			tb = titleFont.getBounds(toPrint);

			titleFont.draw(batch, toPrint, (width - tb.width) / 2, height
					* (1 - GameEngine.topBarSize * 0.39f));
			// .525
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
