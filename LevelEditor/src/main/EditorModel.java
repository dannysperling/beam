package main;

import java.io.File;
import java.io.IOException;

import com.me.beam.Board;
import com.me.beam.Laser;
import com.me.beam.Piece;
import com.me.beam.Tile;

public class EditorModel {
	public Board b;
	public String outputFile = "";
	public LevelIO levelIO;
	public String workingTitle = "";
	public String workingAuthor = "";
	public String fn;
	
	public EditorModel(){
		b = new Board(3,3);
	}
	
	public EditorModel(String fileName){
		fn = fileName;
		levelIO = new LevelIO(fileName,this);
		b = new Board(3,3);
		b.id = -1;
	}
	
	
 	public void loadBoard(int n) throws IOException{
		if (levelIO == null){
			levelIO = new LevelIO("src/levels.xml",this);
		}
		b = levelIO.getLevel(n);
	}
	
	// Add all lasers to the board
	public static void initializeLasers(Board board) {
		board.lasers.clear();
		for (Piece p : board.getAllPieces()) {
			formLasers(board, p);
		}
		//System.out.println(board.lasers.size()+" lasers");
	}

	private static void formLasers(Board board, Piece p) {
		// Check for left pieces
		Tile leftSameColor = null;

		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		Laser possibleFormed = null;

		// Slide to the left
		for (; leftSameColor == null && xPos >= 0; xPos--) {
			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = board.getTileAtBoardPosition(xPos, yPos);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor());
					board.lasers.add(possibleFormed);
				}
			}
		}

		// Check for right colored pieces
		Tile rightSameColor = null;
		xPos = p.getXCoord() + 1;

		// Slide to the right
		for (; rightSameColor == null && xPos < board.getNumHorizontalTiles(); xPos++) {

			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = board.getTileAtBoardPosition(xPos, yPos);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor());
					board.lasers.add(possibleFormed);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (leftSameColor != null && rightSameColor != null) {
			possibleFormed = null;
			board.lasers.remove(new Laser(leftSameColor.getXCoord(), leftSameColor.getYCoord(),
						rightSameColor.getXCoord(), rightSameColor.getYCoord(), p.getColor()));
			
		}
		possibleFormed = null;

		// Now do vertical!

		// Check for bot pieces
		Tile botSameColor = null;

		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Slide down
		for (; botSameColor == null && yPos >= 0; yPos--) {
			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = board.getTileAtBoardPosition(xPos, yPos);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(), p
							.getYCoord(), p.getColor());
					board.lasers.add(possibleFormed);
				}
			}
		}

		// Check for right colored pieces
		Tile topSameColor = null;

		yPos = p.getYCoord() + 1;

		// Slide up
		for (; topSameColor == null && yPos < board.getNumVerticalTiles(); yPos++) {

			Piece possible = board.getPieceOnTile(board.getTileAtBoardPosition(xPos,
					yPos));

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = board.getTileAtBoardPosition(xPos, yPos);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(), xPos,
							yPos, p.getColor());
					board.lasers.add(possibleFormed);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (botSameColor != null && topSameColor != null) {
			possibleFormed = null;
			board.lasers.remove(new Laser(botSameColor.getXCoord(), botSameColor.getYCoord(),
						topSameColor.getXCoord(), topSameColor.getYCoord(), p.getColor()));
		}
		possibleFormed = null;
	}



	public String idString() {
		if (b.id < 1){
			return "new Level";
		}
		return "id#"+b.id;
	}

	public String fileName() {
		//System.out.println(fn);
		//System.out.println(File.separatorChar);
		String name = fn.substring(fn.lastIndexOf(File.separatorChar));
		String dir;
		if (fn.contains("beam-android")){
			dir = ". . . "+File.separator+"beam-android";
		} else if (fn.contains("LevelEditor")){
			dir = ". . . "+File.separator+"LevelEditor";
		} else {
			dir = fn.substring(0, fn.indexOf(File.separatorChar, 23));
		}
		return dir+File.separator+" . . . "+name;
	}
}
