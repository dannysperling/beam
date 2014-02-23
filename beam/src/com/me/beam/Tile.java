package com.me.beam;

public class Tile {
	
	private int xCoord, yCoord;
	
	private GameEngine.Color goal = GameEngine.Color.NONE;
	private GameEngine.Color painter = GameEngine.Color.NONE;
	private boolean hasGlass = false;
	private boolean hasWall = false;
	
	public Tile(int x, int y){
		xCoord = x;
		yCoord = y;
	}
	
	public int getXCoord(){
		return xCoord;
	}
	
	public int getYCoord(){
		return yCoord;
	}

	public boolean hasGoal(){
		return goal != GameEngine.Color.NONE;
	}
	
	public GameEngine.Color getGoalColor(){
		return goal;
	}
	
	public boolean hasPainter(){
		return painter != GameEngine.Color.NONE;
	}
	
	public GameEngine.Color getPainterColor(){
		return painter;
	}
	
	public boolean hasGlass(){
		return hasGlass;
	}
	
	public boolean hasWall(){
		return hasWall;
	}
}
