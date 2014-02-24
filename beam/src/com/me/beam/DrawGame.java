package com.me.beam;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class DrawGame {
	private SpriteBatch batch;
	private Texture pieceTexture;
	private Sprite pieceSprite;
	private ShapeRenderer shapes;
	
	public DrawGame(){
		batch = new SpriteBatch();
		
		pieceTexture = new Texture(Gdx.files.internal("data/piece.png"));
		pieceTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		TextureRegion region = new TextureRegion(pieceTexture, 0, 0, 256, 256);
		
		pieceSprite = new Sprite(region);
		shapes = new ShapeRenderer();
	}
	
	public void draw(Board b, GameEngine.GameState state){
		int bx = b.getBotLeftX();
		int by = b.getBotLeftY();
		int tilesize = b.getTileSize();
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		List<Piece> pieces = b.getAllPieces();
		List<Tile> tiles = b.getAllTiles();
		
		//Draw the basic grid
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.WHITE);
		for(int i = 0; i <= b.getNumHorizontalTiles(); i++){
			shapes.line(bx + (i * tilesize), by, bx + (i * tilesize), by + (b.getNumVerticalTiles() * tilesize));
		}
		for(int i = 0; i <= b.getNumVerticalTiles(); i++){
			shapes.line(bx, by + (i * tilesize), bx + (b.getNumHorizontalTiles() * tilesize), by + (i * tilesize));
		}
		shapes.end();
		
		//Draw the tiles
		shapes.begin(ShapeType.Line);
		for(Tile t: tiles){
			if(t.hasGlass()){
				int glassX = bx + (t.getXCoord() * tilesize);
				int glassY = by + (t.getYCoord() * tilesize);
				shapes.line(glassX, glassY + (0.25f * tilesize), glassX + (0.25f * tilesize), glassY);
				shapes.line(glassX, glassY + (0.5f * tilesize), glassX + (0.5f * tilesize), glassY);
				shapes.line(glassX, glassY + (0.75f * tilesize), glassX + (0.75f * tilesize), glassY);
				shapes.line(glassX, glassY + tilesize, glassX + tilesize, glassY);
				shapes.line(glassX + (0.25f * tilesize), glassY + tilesize, glassX + tilesize, glassY + (0.25f * tilesize));
				shapes.line(glassX + (0.5f * tilesize), glassY + tilesize, glassX + tilesize, glassY + (0.5f * tilesize));
				shapes.line(glassX + (0.75f * tilesize), glassY + tilesize, glassX + tilesize, glassY + (0.75f * tilesize));
			}
			if(t.hasGoal()){
				int goalX = bx + (t.getXCoord() * tilesize);
				int goalY = by + (t.getYCoord() * tilesize);
				switch(t.getGoalColor()){
				case RED: shapes.setColor(Color.RED); break;
				case BLUE: shapes.setColor(Color.BLUE); break;
				case GREEN: shapes.setColor(Color.GREEN); break;
				default: shapes.setColor(new Color(0,0,0,0)); break;
				}
				shapes.rect(goalX + (0.05f * tilesize), goalY + (0.05f * tilesize), 0.9f * tilesize,  0.9f * tilesize);
			}
		}
		shapes.end();
		shapes.begin(ShapeType.Filled);
		for(Tile t: tiles){
			if(t.hasPainter()){
				int paintX = bx + (t.getXCoord() * tilesize);
				int paintY = by + (t.getYCoord() * tilesize);
				switch(t.getPainterColor()){
				case RED: shapes.setColor(new Color(.3f, 0, 0, 1)); break;
				case BLUE: shapes.setColor(new Color(0, 0, .3f, 1)); break;
				case GREEN: shapes.setColor(new Color(0, .3f, 0, 1)); break;
				default: shapes.setColor(new Color(0,0,0,0)); break;
				}
				shapes.rect(paintX + (0.05f * tilesize), paintY + (0.05f * tilesize), 0.9f * tilesize,  0.9f * tilesize);
			}
		}
		shapes.end();
		
		//Draw the pieces
		batch.begin();
		pieceSprite.setSize(tilesize, tilesize);
		for(Piece p : pieces){
			if(p.getColor() == GameEngine.Color.RED){
				pieceSprite.setColor(Color.RED);
			} else if (p.getColor() == GameEngine.Color.BLUE){
				pieceSprite.setColor(Color.BLUE);
			} else if (p.getColor() == GameEngine.Color.GREEN){
				pieceSprite.setColor(Color.GREEN);
			} else {
				pieceSprite.setColor(new Color(0,0,0,0));
			}
			pieceSprite.setPosition(bx + (p.getXCoord() * tilesize), by + (p.getYCoord() * tilesize));
			pieceSprite.draw(batch);
		}
		batch.end();
	}
	
	public void dispose(){
		batch.dispose();
		pieceTexture.dispose();
	}

}
