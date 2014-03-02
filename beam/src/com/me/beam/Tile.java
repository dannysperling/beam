package com.me.beam;

public class Tile {
	
	private int xCoord, yCoord;
	
	private GameEngine.Color goal = GameEngine.Color.NONE;
	private GameEngine.Color painter = GameEngine.Color.NONE;
	public boolean isGlass = false;
	
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
	
	public void setGoal(GameEngine.Color goal){
		this.goal = goal;
	}
	
	public boolean hasPainter(){
		return painter != GameEngine.Color.NONE;
	}
	
	public GameEngine.Color getPainterColor(){
		return painter;
	}
	
	public void setPainter(GameEngine.Color painter){
		this.painter = painter;
	}

}
