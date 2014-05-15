package utilities;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import model.GameProgress;

public class SoundPlayer {
	
	private static GameProgress progress;
	
	private static Music backgroundMusic;
	
	private static Sound click;
	private static Sound destruction;
	private static Sound transition;
	private static Sound starthud;
	
	public static void initSounds(GameProgress progress){
		SoundPlayer.progress = progress;
		backgroundMusic = AssetInitializer.getMusic(AssetInitializer.background_music);
		
		click = AssetInitializer.getSound(AssetInitializer.click_sound);
		destruction = AssetInitializer.getSound(AssetInitializer.destruction_sound);
		transition = AssetInitializer.getSound(AssetInitializer.transition_sound);
		starthud = AssetInitializer.getSound(AssetInitializer.starthud_sound);
	}
	
	public enum SoundType{
		CLICK, DESTRUCTION, TRANSITION, BEAM_FORM, STAR_THUD
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
			case CLICK:
				click.play(0.5f);
				break;
			case DESTRUCTION:
				destruction.play();
				break;
			case TRANSITION:
				transition.play(.05f);
				break;
			case STAR_THUD:
				starthud.play();
				break;
			default:
				break;
			}
		}
	}
}
