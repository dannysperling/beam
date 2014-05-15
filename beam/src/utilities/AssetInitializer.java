package utilities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class AssetInitializer {

	
	private static AssetManager assetManager;
	
	//Textures
	public static final String one_star = "data/1star.png";
	public static final String three_star = "data/3star.png";
	public static final String bangbang = "data/bangbang.png";
	public static final String tutorial = "data/help.png";
	public static final String info = "data/info.png";
	public static final String lock = "data/lock.png";
	public static final String piece = "data/piece.png";
	public static final String npiece = "data/piece_copy.png";
	public static final String painter = "data/painter.png";
	public static final String unlock = "data/unlock.png";
	public static final String two_star = "data/2star.png";
	public static final String innerburn = "data/destruction/innerburn.png";
	public static final String outerburn = "data/destruction/outerburn.png";
	public static final String destruction = "data/destruction/destruction1.png";
	public static final String arrow = "data/arrow.png";
	public static final String vert_star = "data/vert_stars.png";
	public static final String music_off = "data/music_off.png";
	public static final String music_on = "data/music_on.png";
	public static final String sound_off = "data/sound_off.png";
	public static final String sound_on = "data/sound_on.png";
	public static final String credits = "data/credits.png";
	
	//Music
	public static final String background_music = "data/sounds/background_music.mp3";
	
	//Sound
	public static final String click_sound = "data/sounds/click.mp3";
	public static final String destruction_sound = "data/sounds/destruction.mp3";
	public static final String transition_sound = "data/sounds/transition.mp3";
	public static final String starthud_sound = "data/sounds/starthud.mp3";
	public static final String painter_sound = "data/sounds/painter.mp3";
	
	public static void initialize(){
		
		//Request all assets
		assetManager = new AssetManager();
		
		TextureParameter param = new TextureParameter();
		param.minFilter = TextureFilter.Linear;
		param.magFilter = TextureFilter.Linear;
		
		TextureParameter nearestParam = new TextureParameter();
		nearestParam.minFilter = TextureFilter.Nearest;
		nearestParam.magFilter = TextureFilter.Nearest;
		
		//Textures
		assetManager.load(one_star, Texture.class, param);
		assetManager.load(three_star, Texture.class, param);
		assetManager.load(tutorial, Texture.class, param);
		assetManager.load(info, Texture.class, param);
		assetManager.load(lock, Texture.class, param);
		assetManager.load(piece, Texture.class, param);
		assetManager.load(npiece, Texture.class, nearestParam);
		assetManager.load(painter, Texture.class, param);
		assetManager.load(unlock, Texture.class, param);
		assetManager.load(two_star, Texture.class, param);
		assetManager.load(innerburn, Texture.class, param);
		assetManager.load(outerburn, Texture.class, param);
		assetManager.load(arrow, Texture.class, param);
		assetManager.load(vert_star, Texture.class, param);
		assetManager.load(destruction, Texture.class, param);
		assetManager.load(music_off, Texture.class, param);
		assetManager.load(music_on, Texture.class, param);
		assetManager.load(sound_off, Texture.class, param);
		assetManager.load(sound_on, Texture.class, param);
		assetManager.load(credits, Texture.class, param);
		
		//Sounds
		assetManager.load(click_sound, Sound.class);
		assetManager.load(destruction_sound, Sound.class);
		assetManager.load(transition_sound, Sound.class);
		assetManager.load(starthud_sound, Sound.class);
		assetManager.load(painter_sound, Sound.class);
		
		//Music
		assetManager.load(background_music, Music.class);

		String paintString = "data/painter/paint_000";
		for(int i = 0; i < 60; i++){
			paintString = paintString + (i < 10?"0":"");
			assetManager.load(paintString + i +".png", Texture.class, param);
			paintString = "data/painter/paint_000";
		}
		
	}
	
	public static boolean isFinished(){
		return assetManager.update();
	}
	
	public static Texture getTexture(String textName){
		
		return assetManager.get(textName, Texture.class);
	}
	
	public static Sound getSound(String soundName){
		return assetManager.get(soundName, Sound.class);
	}
	
	public static Music getMusic(String musicName){
		return assetManager.get(musicName, Music.class);
	}
}
