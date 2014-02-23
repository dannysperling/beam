package com.me.beam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DrawGame {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture pieceTexture;
	private Sprite pieceSprite;
	
	public DrawGame(){
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		batch = new SpriteBatch();
		
		pieceTexture = new Texture(Gdx.files.internal("data/piece.png"));
		pieceTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		TextureRegion region = new TextureRegion(pieceTexture, 0, 0, 256, 256);
		
		pieceSprite = new Sprite(region);
		pieceSprite.setPosition(0, 50);
		pieceSprite.setColor(Color.RED);
	}
	
	public void draw(Board b, GameEngine.GameState state){
		Gdx.gl.glClearColor(0, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		pieceSprite.draw(batch);
		batch.end();
	}
	
	public void dispose(){
		batch.dispose();
		pieceTexture.dispose();
	}

}
