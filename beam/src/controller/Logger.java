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

	/**
	 * Folder for the data storage
	 */
	private static String logFolder = "beamLogging/";

	/**
	 * File storing all user IDs that have been created on this device during the session.
	 * This allows for simple pick-up through a new session.
	 */
	private static FileHandle userIds = Gdx.files.external(logFolder + "userIds.txt");

	/**
	 * The names of all the .csv files created for convenient data comparisons
	 */
	private static FileHandle levelTimes = Gdx.files.external(logFolder + "totalLevelTimes.csv");
	private static FileHandle timeToSolve = Gdx.files.external(logFolder + "timesToFirstSolve.csv");
	private static FileHandle levelMoves = Gdx.files.external(logFolder + "bestMovesOnLevels.csv");
	private static FileHandle firstSolveMoves = Gdx.files.external(logFolder + "firstMovesOnLevels.csv");
	private static FileHandle starsOnLevel = Gdx.files.external(logFolder + "starsOnLevels.csv");
	private static FileHandle undoClicks = Gdx.files.external(logFolder + "timesUndoPressed.csv");
	private static FileHandle redoClicks = Gdx.files.external(logFolder + "timesRedoPressed.csv");
	private static FileHandle resetClicks = Gdx.files.external(logFolder + "timesResetPressed.csv");
	private static FileHandle destructions = Gdx.files.external(logFolder + "timesDestroyed.csv");

	/**
	 * Store the current user's data in its own file, so it can all be transfered to the log
	 * file at once.
	 */
	private static String userFiles = logFolder + "userLogs/";
	private static FileHandle currentUserFile;

	/**
	 * Information about the current user
	 */
	private static long currentUserId = -1;
	private static int currentWorld = -1;
	private static int currentOrdinalInWorld = -1;
	private static long timeEnteredLevel = -1;
	private static long lastBeatLevelTime = -1;
	private static int[] scoresOnLevels;

	/**
	 * General info about the game to make logging easier
	 */
	private static int numLevels;
	
	/**
	 * Maps strings representing worlds to the correct index
	 */
	private static Map<String, Integer> worldToIndex = new HashMap<String, Integer>();

	/**
	 * Sets up all the static variables for logging. Should be called once
	 * when the device turns on if logging is enabled.
	 * 
	 * @param mapping
	 * 			The mapping of level positions in worlds to their unique ids
	 * 			found in the level orderer.
	 */
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

	/**
	 * Called when changing from one user to the next. Compiles all the current
	 * user data into the csv files, if there is a current user. Then starts the
	 * next user's data.
	 */
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

	/**
	 * The various types of logging that can be done, at present
	 */
	public enum LogType {
		BEAT_LEVEL_MOVES, BEAT_LEVEL_STARS, UNDO, RESET, REDO, DEATH
	}

	/**
	 * First portions of the logging for each of the types of logging that
	 * can be done. This makes the file much more human readable, but still
	 * easy to parse using regex.
	 */
	private static String LEVEL_TIME = "TIME spent on level ";
	private static String SOLVED = "TIME to first solve level ";
	private static String MOVES = "MOVES to finish level ";
	private static String STARS = "STARS earned on level ";
	private static String UNDO = "UNDO pressed on level ";
	private static String REDO = "REDO pressed on level ";
	private static String RESET = "RESET pressed on level ";
	private static String DEATH = "DESTRUCTIONS on level ";

	private static String SEP = ": ";
	
	/**
	 * Explicit method for entry, as two arguments are necessary
	 * 
	 * @param world
	 * 			The world of the level entered
	 * @param ordinalInWorld
	 * 			The ordinal of the level entered in that world
	 */
	public static void enteredLevel(int world, int ordinalInWorld){
		timeEnteredLevel = time();
		currentWorld = world;
		currentOrdinalInWorld = ordinalInWorld;
		write(currentUserFile, "Entered level " + currentLevel());
	}
	
	/**
	 * Explict method for completing a level, as it needs no arguments
	 */
	public static void exitedLevel(){
		int timeInSecs = (int) ((time() - timeEnteredLevel) / 1000);
		write(currentUserFile, LEVEL_TIME + currentLevel() + SEP + timeInSecs + " seconds\r\n\r\n");
		lastBeatLevelTime = -1;
	}

	/**
	 * Writes the appropriate log data to the current user's file.
	 * 
	 * @param type
	 * 			Which type of logging is being done, i.e. BEAT_LEVEL_MOVES or RESET
	 * @param appropriateState
	 * 			The appropriate number for that logging, i.e., number of moves or
	 * 			number of times reset was pressed.
	 */
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
	
	/**
	 * Gets the string representation of the current level
	 * 
	 * @return
	 * 			The string representation of the current level, i.e. "3-7"
	 */
	private static String currentLevel(){
		return currentWorld + "-" + currentOrdinalInWorld;
	}

	/**
	 * Wrapper to get the current time more conveniently
	 * 
	 * @return
	 * 			Current time as a long in miliseconds
	 */
	private static long time(){
		return System.currentTimeMillis();
	}

	/**
	 * Appends a string to a given file
	 * 
	 * @param fh
	 * 				The file handle to write to
	 * @param toWrite
	 * 				What to append
	 */
	private static void write(FileHandle fh, String toWrite){
		write(fh, toWrite, true);
	}

	/**
	 * Writes a string to a given file. May append or overwrite. Writes
	 * a new line after it.
	 * 
	 * @param fh
	 * 				The file handle to write to
	 * @param toWrite
	 * 				The string to write
	 * @param append
	 * 				Whether to append (true) or overwrite (false)
	 */
	private static void write(FileHandle fh, String toWrite, boolean append){
		fh.writeString(toWrite + "\r\n", append);
	}

	/**
	 * Writes integer data to the appropriate csv. Depending on the data type,
	 * it may write zeros as well, when the score on the level is not equal to
	 * zero. This allows zero data to be written for button presses.
	 * 
	 * @param data
	 * 				The integer data to write to the csv
	 * @param fh
	 * 				The csv file to write to
	 * @param useZeros
	 * 				True to write zeros when the user has completed the level,
	 * 				false otherwise.
	 */
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
