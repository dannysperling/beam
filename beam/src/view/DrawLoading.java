package view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DrawLoading {

	private SpriteBatch batch;
	private Texture loadingTexture;
	private Sprite loadingSprite;

	public DrawLoading(){
		batch = new SpriteBatch();

		loadingTexture = new Texture(Gdx.files.internal("data/moe.png"));
		loadingTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion loadingRegion = new TextureRegion(loadingTexture, 0, 0, 1024,
				1024);
		loadingSprite = new Sprite(loadingRegion);
	}

	public void draw(float percentDark){
		//Clear colors
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		//Get the various dimensions
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		
		batch.begin();
		loadingSprite.setSize(width, width);
		loadingSprite.setPosition(0, (height - width)/2);
		loadingSprite.setColor(new Color(1, 1, 1, percentDark));
		loadingSprite.draw(batch);
		batch.end();
	}
}
