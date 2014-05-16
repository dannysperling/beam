package model;

import controller.GameEngine;

public class Tile {
	
	/**
	 * Coordinates relative to the board
	 */
	private int xCoord, yCoord;
	
	/**
	 * Can have a goal and/or painter, or glass
	 */
	private GameEngine.Color goal = GameEngine.Color.NONE;
	private GameEngine.Color painter = GameEngine.Color.NONE;
	private boolean isGlass = false;
	
	/**
	 * Very simple constructor
	 */
	public Tile(int x, int y){
		xCoord = x;
		yCoord = y;
	}
	
	
	/**
	 * Getters for coordinate
	 * @return
	 */
	public int getXCoord(){return xCoord;}
	public int getYCoord(){return yCoord;}

	/**
	 * Getters and setters for goal
	 */
	public boolean hasGoal(){return goal != GameEngine.Color.NONE;}
	public GameEngine.Color getGoalColor(){return goal;}
	public void setGoal(GameEngine.Color goal){this.goal = goal;}
	
	/**
	 * Getters and setters for painter
	 */
	public boolean hasPainter(){return painter != GameEngine.Color.NONE;}
	public GameEngine.Color getPainterColor(){return painter;}
	public void setPainter(GameEngine.Color painter){this.painter = painter;}
	
	/**
	 * Getters and setters for glass
	 */
	public boolean hasGlass(){return isGlass;}
	public void setGlass(boolean isGlass){this.isGlass = isGlass;}
	
	public boolean equals(Tile t) {
		boolean areSame = true;
		areSame &= this.xCoord == t.getXCoord();
		areSame &= this.yCoord == t.getYCoord();
		areSame &= this.isGlass == t.hasGlass();
		areSame &= this.goal == t.getGoalColor();
		areSame &= this.painter == t.getPainterColor();
		return areSame;
	}
}
