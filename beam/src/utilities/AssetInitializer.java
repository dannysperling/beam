package utilities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
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
		assetManager.load(bangbang, Texture.class, param);
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

		
		for(int i = 0; i < Constants.TIME_BEFORE_DEATH_MESSAGE; i++){
			assetManager.load("data/destruction/destruction"+ i +".png", Texture.class, param);
		}
		
	}
	
	public static boolean isFinished(){
		return assetManager.update();
	}
	
	public static Texture getTexture(String textName){
		
		return assetManager.get(textName, Texture.class);
	}
}
