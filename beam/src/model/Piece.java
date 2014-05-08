package model;

import java.awt.Point;

import controller.GameEngine;

public class Piece {

	/**
	 * Piece x and y coordinates, in terms of the TILE they are on
	 */
	private int xCoord;
	private int yCoord;
	
	/**
	 * Piece color - the only other data about it
	 */
	private GameEngine.Color color;
	
	/**
	 * Construct a piece from its basic data
	 */
	public Piece(int xCoord, int yCoord, GameEngine.Color color){
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		if (color == null) {
			System.err.println("error from passing null as color to Piece constructor.");
		}
		this.color = color;
	}
	
	/**
	 * Construct a piece on a point, for the convenience of the solver
	 */
	public Piece(Point point, GameEngine.Color color) {
		this.xCoord = point.x;
		this.yCoord = point.y;
		this.color = color;
	}
	
	/**
	 * Standard check for equality - same position and color.
	 */
	@Override
	public boolean equals(Object other){
		if (!(other instanceof Piece))
			return false;
		Piece p2 = (Piece) other;
		return (this.xCoord == p2.xCoord) &&  (this.yCoord == p2.yCoord) && (this.color == p2.color);
	}
	
	@Override
	public int hashCode() {
		return 2 * xCoord + 3 * yCoord + 5 * this.color.ordinal();
	}

	/**
	 * Getters and setters for the piece.
	 */
	public int getXCoord(){return xCoord;}
	public void setXCoord(int xCoord){this.xCoord = xCoord;}
	
	public int getYCoord(){return yCoord;}
	public void setYCoord(int yCoord){this.yCoord = yCoord;}
	
	public GameEngine.Color getColor(){return color;}
	public void setColor(GameEngine.Color color){this.color = color;}
}
