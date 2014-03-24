package com.me.beam;

import java.util.ArrayList;
import java.util.List;

import com.me.beam.GameEngine.AnimationState;
import com.me.beam.GameEngine.Color;

public class GameEngineForSolver {
	
	private Color originalColor = Color.NONE; 
	private static List<Laser> lasersCreated = new ArrayList<Laser>();
	
	public GameEngineForSolver() {}

	public List<Piece> formLasersFromPieceAndDestroy(Piece[][] pieces, Piece p) {

		boolean horizontalMove = false;
		
		List<Piece> destroyed = new ArrayList<Piece>();

		// For each destruction
		List<Piece> possibleDestroy = new ArrayList<Piece>();

		// Check for left pieces
		Piece leftSameColor = null;

		int xPos = p.getXCoord() - 1;
		int yPos = p.getYCoord();

		Laser possibleFormed = null;

		// Slide to the left
		for (; leftSameColor == null && xPos >= 0; xPos--) {
			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					leftSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(),
							p.getYCoord(), p.getColor());
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Piece rightSameColor = null;
		xPos = p.getXCoord() + 1;

		// Slide to the right
		for (; rightSameColor == null && xPos < pieces.length; xPos++) {

			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					rightSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(),
							xPos, yPos, p.getColor());
					
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (leftSameColor != null && rightSameColor != null) {
			possibleFormed = null;
			
		}

		// If it's still possible, it was formed
		if (possibleFormed != null
				&& (originalColor != p.getColor() || !horizontalMove)) {
			lasersCreated.add(possibleFormed);
		}
		possibleFormed = null;

		possibleDestroy.clear();

		// Now do vertical!

		// Check for bot pieces
		Piece botSameColor = null;

		xPos = p.getXCoord();
		yPos = p.getYCoord() - 1;

		// Slide down
		for (; botSameColor == null && yPos >= 0; yPos--) {
			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					botSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(xPos, yPos, p.getXCoord(),
							p.getYCoord(), p.getColor());
				
				} else {
					possibleDestroy.add(possible);
				}
			}
		}

		possibleDestroy.clear();

		// Check for right colored pieces
		Piece topSameColor = null;

		yPos = p.getYCoord() + 1;

		// Slide up
		for (; topSameColor == null && yPos < pieces[0].length; yPos++) {

			Piece possible = pieces[xPos][yPos];

			// There's a piece there
			if (possible != null) {
				if (possible.getColor() == p.getColor()) {
					topSameColor = pieces[xPos][yPos];
					destroyed.addAll(possibleDestroy);
					possibleFormed = new Laser(p.getXCoord(), p.getYCoord(),
							xPos, yPos, p.getColor());
				
				} else {
					possibleDestroy.add(possible);
				}
			}
		}
		// Lasers on both sides. Remove!
		if (botSameColor != null && topSameColor != null) {
			possibleFormed = null;
			
		}
		// If it's still possible, it was formed
		if (possibleFormed != null
				&& (originalColor != p.getColor() || horizontalMove)) {
			lasersCreated.add(possibleFormed);
		}
		possibleFormed = null;

		return destroyed;
	}
}
