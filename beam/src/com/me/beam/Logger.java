package com.me.beam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Logger {

	private static String logFolder = "beamLogging/";
	
	private static FileHandle userIds = Gdx.files.external(logFolder + "userIds.txt");
	
	
	//All the csv files
	private static FileHandle levelTimes = Gdx.files.external(logFolder + "totalLevelTimes.csv");
	private static FileHandle timeToSolve = Gdx.files.external(logFolder + "timesToFirstSolve.csv");
	private static FileHandle levelMoves = Gdx.files.external(logFolder + "bestMovesOnLevels.csv");
	private static FileHandle firstSolveMoves = Gdx.files.external(logFolder + "firstMovesOnLevels.csv");
	private static FileHandle starsOnLevel = Gdx.files.external(logFolder + "starsOnLevels.csv");
	
	private static FileHandle undoClicks = Gdx.files.external(logFolder + "timesUndoPressed.csv");
	private static FileHandle redoClicks = Gdx.files.external(logFolder + "timesRedoPressed.csv");
	private static FileHandle resetClicks = Gdx.files.external(logFolder + "timesResetPressed.csv");
	
	private static String userFiles = logFolder + "userLogs/";
	private static FileHandle currentUserFile;
	
	private static long currentUserId = -1;
	private static int currentLevel = -1;
	private static long timeEnteredLevel = -1;
	private static long lastBeatLevelTime = -1;
	
	public static void initialize(int[] uniqueIds){
		if (userIds.exists()){
			String[] ids = userIds.readString().split("\\s+");
			currentUserId = Long.parseLong(ids[ids.length - 1]);
		}
		//Initialize all the files
		else {
			currentUserId = time();
			write(userIds, currentUserId + "", false);
			String uniques = "unique,";
			String ordinals = "ordinals,";
			for (int i = 0; i < uniqueIds.length - 1; i++){
				uniques += uniqueIds[i] + ",";
				ordinals += (i+1) + ",";
			}
			uniques += uniqueIds[uniqueIds.length - 1];
			ordinals += uniqueIds.length;
			String toWrite = uniques + "\r\n" + ordinals;
			
			//Init all
			write(levelTimes, toWrite, false);
			write(timeToSolve, toWrite, false);
			write(levelMoves, toWrite, false);
			write(firstSolveMoves, toWrite, false);
			write(starsOnLevel, toWrite, false);
			write(undoClicks, toWrite, false);
			write(redoClicks, toWrite, false);
			write(resetClicks, toWrite, false); 
		}
		currentUserFile = Gdx.files.external(userFiles + currentUserId + ".txt");
		if (!currentUserFile.exists()){
			write(currentUserFile, "INITIALIZING", false);
		}
	}
	
	public static void startNewSession(){
		
	}
	
	public enum LogType {
		ENTERED_LEVEL, EXITED_LEVEL, BEAT_LEVEL_MOVES, BEAT_LEVEL_STARS, UNDO, RESET, REDO
	}
	
	private static String LEVEL_TIME = "TIME spent on level ";
	private static String SOLVED = "TIME to first solve level ";
	private static String MOVES = "MOVES to finish level ";
	private static String STARS = "STARS earned on level ";
	private static String UNDO = "UNDO pressed on level ";
	private static String REDO = "REDO pressed on level ";
	private static String RESET = "RESET pressed on level ";
	
	private static String SEP = ": ";
	
	public static void log(LogType type, int appropriateStat){
		//Log based on what was given
		switch(type){
		case ENTERED_LEVEL:
			timeEnteredLevel = time();
			currentLevel = appropriateStat;
			break;
		case BEAT_LEVEL_MOVES:
			if (lastBeatLevelTime == -1){
				int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
				write(currentUserFile, SOLVED + currentLevel + SEP + timeInSecs + " seconds");
			}
			write(currentUserFile, MOVES + currentLevel + SEP + appropriateStat);
			lastBeatLevelTime = time();
			break;
		case BEAT_LEVEL_STARS:
			write(currentUserFile, STARS + currentLevel + SEP + appropriateStat);
			break;
		case EXITED_LEVEL:
			int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
			write(currentUserFile, LEVEL_TIME + currentLevel + SEP + timeInSecs + " seconds");
			lastBeatLevelTime = -1;
			break;
		case REDO:
			write(currentUserFile, REDO + currentLevel + SEP + appropriateStat);
			break;
		case RESET:
			write(currentUserFile, RESET + currentLevel + SEP + appropriateStat);
			break;
		case UNDO:
			write(currentUserFile, UNDO + currentLevel + SEP + appropriateStat);
			break;
		}
	}
	
	private static long time(){
		return System.currentTimeMillis();
	}
	
	private static void write(FileHandle fh, String toWrite){
		write(fh, toWrite, true);
	}
	
	private static void write(FileHandle fh, String toWrite, boolean append){
		fh.writeString(toWrite + "\r\n", append);
	}
}
