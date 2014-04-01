package model;

import java.awt.Point;

import controller.GameEngine;

public class Piece {

	//These are in terms of grid squares!
	private int xCoord;
	private int yCoord;
	
	private GameEngine.Color color;
	
	public Piece(int xCoord, int yCoord, GameEngine.Color color){
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.color = color;
	}
	
	public Piece(Point point, GameEngine.Color color) {
		this.xCoord = point.x;
		this.yCoord = point.y;
		this.color = color;
	}
	
	@Override
	public boolean equals(Object other){
		if (!(other instanceof Piece))
			return false;
		Piece p2 = (Piece) other;
		return (this.xCoord == p2.xCoord) &&  (this.yCoord == p2.yCoord) && (this.color == p2.color);
	}
	
	public int getXCoord(){
		return xCoord;
	}
	
	public void setXCoord(int xCoord){
		this.xCoord = xCoord;
	}
	
	public int getYCoord(){
		return yCoord;
	}
	
	public void setYCoord(int yCoord){
		this.yCoord = yCoord;
	}
	
	public GameEngine.Color getColor(){
		return color;
	}
	
	public void setColor(GameEngine.Color color){
		this.color = color;
	}
	
	public String toString() {
		if(color == GameEngine.Color.RED) {
			return "R";
		} else if(color == GameEngine.Color.GREEN) {
			return "G";
		} else if(color == GameEngine.Color.BLUE) {
			return "B";
		} else {
			return "_";
		}
	}
}
