package com.me.beam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static FileHandle destructions = Gdx.files.external(logFolder + "timesDestroyed.csv");

	private static String userFiles = logFolder + "userLogs/";
	private static FileHandle currentUserFile;

	private static long currentUserId = -1;
	private static int currentLevel = -1;
	private static long timeEnteredLevel = -1;
	private static long lastBeatLevelTime = -1;

	private static int numLevels;
	private static int[] scoresOnLevels;

	public static void initialize(int[] uniqueIds){
		numLevels = uniqueIds.length;

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
			write(destructions, toWrite, false);
		}
		currentUserFile = Gdx.files.external(userFiles + currentUserId + ".txt");
	}

	public static void startNewSession(){
		//Compile the data from the current user
		if (currentUserFile.exists()){
			String userData = currentUserFile.readString();
			Pattern pat;
			Matcher mat;
			int[] data;

			//Level times
			data = new int[numLevels];
			pat = Pattern.compile(LEVEL_TIME + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int time = Integer.parseInt(mat.group(2));
				data[levelNumber - 1] += time;
			}
			writeToCSV(data, levelTimes, false);

			//Time to solve
			data = new int[numLevels];
			pat = Pattern.compile(SOLVED + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int time = Integer.parseInt(mat.group(2));
				if (data[levelNumber - 1] == 0){
					data[levelNumber - 1] = time;
				}
			}
			writeToCSV(data, timeToSolve, false);

			//Level Moves
			data = new int[numLevels];
			int[] data2 = new int[numLevels];
			pat = Pattern.compile(MOVES + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int numMoves = Integer.parseInt(mat.group(2));
				//First
				if (data2[levelNumber - 1] == 0){
					data2[levelNumber - 1] = numMoves;
					data[levelNumber - 1] = numMoves;
				}
				//Best
				if (numMoves < data[levelNumber - 1])
					data[levelNumber - 1] = numMoves;
			}
			scoresOnLevels = data;
			writeToCSV(data, levelMoves, false);
			writeToCSV(data2, firstSolveMoves, false);

			//Stars
			data = new int[numLevels];
			pat = Pattern.compile(STARS + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int stars = Integer.parseInt(mat.group(2));
				data[levelNumber - 1] = stars;
			}
			writeToCSV(data, starsOnLevel, false);

			//Clicks
			//Undo
			data = new int[numLevels];
			pat = Pattern.compile(UNDO + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int undos = Integer.parseInt(mat.group(2));
				data[levelNumber - 1] += undos;
			}
			writeToCSV(data, undoClicks, true);

			//Reset
			data = new int[numLevels];
			pat = Pattern.compile(RESET + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int resets = Integer.parseInt(mat.group(2));
				data[levelNumber - 1] += resets;
			}
			writeToCSV(data, resetClicks, true);

			//Redo
			data = new int[numLevels];
			pat = Pattern.compile(REDO + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int redos = Integer.parseInt(mat.group(2));
				data[levelNumber - 1] += redos;
			}
			writeToCSV(data, redoClicks, true);

			//And destructions
			data = new int[numLevels];
			pat = Pattern.compile(DEATH + "(\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int levelNumber = Integer.parseInt(mat.group(1));
				int deaths = Integer.parseInt(mat.group(2));
				data[levelNumber - 1] += deaths;
			}
			writeToCSV(data, destructions, true);
		}

		//Switch to the next user
		currentUserId = time();
		write(userIds, currentUserId + "");
		currentUserFile = Gdx.files.external(userFiles + currentUserId + ".txt");
	}

	public enum LogType {
		ENTERED_LEVEL, EXITED_LEVEL, BEAT_LEVEL_MOVES, BEAT_LEVEL_STARS, UNDO, RESET, REDO, DEATH
	}

	private static String LEVEL_TIME = "TIME spent on level ";
	private static String SOLVED = "TIME to first solve level ";
	private static String MOVES = "MOVES to finish level ";
	private static String STARS = "STARS earned on level ";
	private static String UNDO = "UNDO pressed on level ";
	private static String REDO = "REDO pressed on level ";
	private static String RESET = "RESET pressed on level ";
	private static String DEATH = "DESTRUCTIONS on level ";

	private static String SEP = ": ";

	public static void log(LogType type, int appropriateState){
		//Log based on what was given
		switch(type){
		case ENTERED_LEVEL:
			timeEnteredLevel = time();
			currentLevel = appropriateState + 1;
			write(currentUserFile, "Entered level " + currentLevel);
			break;
		case BEAT_LEVEL_MOVES:
			if (lastBeatLevelTime == -1){
				int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
				write(currentUserFile, SOLVED + currentLevel + SEP + timeInSecs + " seconds");
			}
			write(currentUserFile, MOVES + currentLevel + SEP + appropriateState);
			lastBeatLevelTime = time();
			break;
		case BEAT_LEVEL_STARS:
			write(currentUserFile, STARS + currentLevel + SEP + appropriateState);
			break;
		case EXITED_LEVEL:
			int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
			write(currentUserFile, LEVEL_TIME + currentLevel + SEP + timeInSecs + " seconds\r\n\r\n");
			lastBeatLevelTime = -1;
			break;
		case REDO:
			write(currentUserFile, REDO + currentLevel + SEP + appropriateState);
			break;
		case RESET:
			write(currentUserFile, RESET + currentLevel + SEP + appropriateState);
			break;
		case UNDO:
			write(currentUserFile, UNDO + currentLevel + SEP + appropriateState);
			break;
		case DEATH:
			write(currentUserFile, DEATH + currentLevel + SEP + appropriateState);
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

	private static void writeToCSV(int[] data, FileHandle fh, boolean useZeros) {
		String toWrite = currentUserId + "";
		for (int i = 0; i < data.length; i++){
			if (data[i] != 0 || (useZeros && scoresOnLevels[i] != 0)){
				toWrite += "," + data[i];
			} else {
				toWrite += ",";
			}
		}
		write(fh, toWrite);
	}
}
