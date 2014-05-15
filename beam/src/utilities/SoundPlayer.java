package utilities;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import model.GameProgress;

public class SoundPlayer {
	
	private static GameProgress progress;
	
	private static Music backgroundMusic;
	
	private static Sound goalFilled;
	
	public static void initSounds(GameProgress progress){
		SoundPlayer.progress = progress;
		backgroundMusic = AssetInitializer.getMusic(AssetInitializer.background_music);
		
		//TODO: All the sounds here
	}
	
	public enum SoundType{
		GOAL_FILLED
	}
	
	public static void playMusic(){
		backgroundMusic.setLooping(true);
		backgroundMusic.setVolume(0.2f);
		backgroundMusic.play();
	}
	
	public static void stopMusic(){
		backgroundMusic.stop();
	}
	
	public static void playSound(SoundType type){
		if (progress.isSoundPlaying()){
			switch (type){
			case GOAL_FILLED:
				goalFilled.play();
				break;
			default:
				break;
			}
		}
	}
}
