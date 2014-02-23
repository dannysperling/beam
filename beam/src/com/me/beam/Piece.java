package com.me.beam;

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
}
