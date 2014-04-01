package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private static int currentWorld = -1;
	private static int currentOrdinalInWorld = -1;
	private static long timeEnteredLevel = -1;
	private static long lastBeatLevelTime = -1;

	private static int numLevels;
	private static int[] scoresOnLevels;
	
	/**
	 * Maps strings representing worlds to the correct index
	 */
	private static Map<String, Integer> worldToIndex = new HashMap<String, Integer>();

	public static void initialize(List<List<Integer>> mapping){
		
		//First put together the string mapping
		List<Integer> uniqueIds = new ArrayList<Integer>();
		List<String> stringOrdinals = new ArrayList<String>();
		int index = 0;
		for (int world = 1; world <= mapping.size(); world++){
			List<Integer> curWorld = mapping.get(world - 1);
			
			//Add both the unique id and the world mapping
			for (int ordinalInWorld = 1; ordinalInWorld <= curWorld.size(); ordinalInWorld++){
				uniqueIds.add(curWorld.get(ordinalInWorld - 1));
				worldToIndex.put(world + "-" + ordinalInWorld, index);
				stringOrdinals.add(world + "-" + ordinalInWorld);
				index++;
			}
		}
		
		numLevels = uniqueIds.size();
		
		//Get the current user id if there is one
		if (userIds.exists()){
			String[] ids = userIds.readString().split("\\s+");
			currentUserId = Long.parseLong(ids[ids.length - 1]);
		}
		//Otherwise initialize all the files
		else {
			currentUserId = time();
			write(userIds, currentUserId + "", false);
			String uniques = "unique,";
			String ordinals = "levelOrdinals,";
			for (int i = 0; i < uniqueIds.size() - 1; i++){
				uniques += uniqueIds.get(i) + ",";
				ordinals += stringOrdinals.get(i) + ",";
			}
			uniques += uniqueIds.get(uniqueIds.size() - 1);
			ordinals += stringOrdinals.get(uniqueIds.size() - 1);
			String toWrite = uniques + "\r\n" + ordinals;

			//Init all csv files
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
		//Compile the data from the current user, if there is one
		if (currentUserFile.exists()){
			String userData = currentUserFile.readString();
			Pattern pat;
			Matcher mat;
			int[] data;

			//Level times
			data = new int[numLevels];
			pat = Pattern.compile(LEVEL_TIME + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int time = Integer.parseInt(mat.group(2));
				data[index] += time;
			}
			writeToCSV(data, levelTimes, false);

			//Time to solve
			data = new int[numLevels];
			pat = Pattern.compile(SOLVED + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int time = Integer.parseInt(mat.group(2));
				if (data[index] == 0){
					data[index] = time;
				}
			}
			writeToCSV(data, timeToSolve, false);

			//Level Moves
			data = new int[numLevels];
			int[] data2 = new int[numLevels];
			pat = Pattern.compile(MOVES + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int numMoves = Integer.parseInt(mat.group(2));
				//First
				if (data2[index] == 0){
					data2[index] = numMoves;
					data[index] = numMoves;
				}
				//Best
				if (numMoves < data[index])
					data[index] = numMoves;
			}
			scoresOnLevels = data;
			writeToCSV(data, levelMoves, false);
			writeToCSV(data2, firstSolveMoves, false);

			//Stars
			data = new int[numLevels];
			pat = Pattern.compile(STARS + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int stars = Integer.parseInt(mat.group(2));
				data[index] = stars;
			}
			writeToCSV(data, starsOnLevel, false);

			//Clicks
			//Undo
			data = new int[numLevels];
			pat = Pattern.compile(UNDO + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int undos = Integer.parseInt(mat.group(2));
				data[index] += undos;
			}
			writeToCSV(data, undoClicks, true);

			//Reset
			data = new int[numLevels];
			pat = Pattern.compile(RESET + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int resets = Integer.parseInt(mat.group(2));
				data[index] += resets;
			}
			writeToCSV(data, resetClicks, true);

			//Redo
			data = new int[numLevels];
			pat = Pattern.compile(REDO + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int redos = Integer.parseInt(mat.group(2));
				data[index] += redos;
			}
			writeToCSV(data, redoClicks, true);

			//And destructions
			data = new int[numLevels];
			pat = Pattern.compile(DEATH + "(\\d+-\\d+)" + SEP + "(\\d+)");
			mat = pat.matcher(userData);
			while (mat.find()){
				int index = worldToIndex.get(mat.group(1));
				int deaths = Integer.parseInt(mat.group(2));
				data[index] += deaths;
			}
			writeToCSV(data, destructions, true);
		}

		//Switch to the next user and create their file
		currentUserId = time();
		write(userIds, currentUserId + "");
		currentUserFile = Gdx.files.external(userFiles + currentUserId + ".txt");
	}

	public enum LogType {
		BEAT_LEVEL_MOVES, BEAT_LEVEL_STARS, UNDO, RESET, REDO, DEATH
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
	
	public static void enteredLevel(int world, int ordinalInWorld){
		timeEnteredLevel = time();
		currentWorld = world;
		currentOrdinalInWorld = ordinalInWorld;
		write(currentUserFile, "Entered level " + currentLevel());
	}
	
	public static void exitedLevel(){
		int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
		write(currentUserFile, LEVEL_TIME + currentLevel() + SEP + timeInSecs + " seconds\r\n\r\n");
		lastBeatLevelTime = -1;
	}

	public static void log(LogType type, int appropriateState){
		//Log based on what was given
		switch(type){
		case BEAT_LEVEL_MOVES:
			if (lastBeatLevelTime == -1){
				int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
				write(currentUserFile, SOLVED + currentLevel() + SEP + timeInSecs + " seconds");
			}
			write(currentUserFile, MOVES + currentLevel() + SEP + appropriateState);
			lastBeatLevelTime = time();
			break;
		case BEAT_LEVEL_STARS:
			write(currentUserFile, STARS + currentLevel() + SEP + appropriateState);
			break;
		case REDO:
			write(currentUserFile, REDO + currentLevel() + SEP + appropriateState);
			break;
		case RESET:
			write(currentUserFile, RESET + currentLevel() + SEP + appropriateState);
			break;
		case UNDO:
			write(currentUserFile, UNDO + currentLevel() + SEP + appropriateState);
			break;
		case DEATH:
			write(currentUserFile, DEATH + currentLevel() + SEP + appropriateState);
			break;
		}
	}
	
	private static String currentLevel(){
		return currentWorld + "-" + currentOrdinalInWorld;
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
