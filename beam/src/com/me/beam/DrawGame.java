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
import com.badlogic.gdx.math.MathUtils;

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
		
		shapes.begin(ShapeType.Line);
		for(int i = 0; i <= b.getNumHorizontalTiles(); i++){
			shapes.line(bx + (i * tilesize), by, bx + (i * tilesize), by + (b.getNumVerticalTiles() * tilesize));
		}
		for(int i = 0; i <= b.getNumVerticalTiles(); i++){
			shapes.line(bx, by + (i * tilesize), bx + (b.getNumHorizontalTiles() * tilesize), by + (i * tilesize));
		}
		List<Piece> pieces = b.getAllPieces();
		
		shapes.end();
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
