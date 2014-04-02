package model;

import controller.GameEngine;

public class Laser {
	
	/**
	 * Store the start and finish locations of a laser
	 */
	private int xStart;
	private int yStart;
	private int xFinish;
	private int yFinish;
	
	/**
	 * The color of laser - the only other important piece of data on it
	 */
	private GameEngine.Color color = GameEngine.Color.NONE;
	
	/**
	 * Creates a laser from (xStart, yStart) to (xFinish, yFinish), of color color.
	 * Requirement: xStart <= xFinish, yStart <= yFinish.
	 */
	public Laser(int xStart, int yStart, int xFinish, int yFinish, GameEngine.Color color){
		this.xStart = xStart;
		this.yStart = yStart;
		this.xFinish = xFinish;
		this.yFinish = yFinish;
				
		this.color = color;
	}
	
	/**
	 * Check if two lasers are equal by positions and color
	 */
	@Override
	public boolean equals(Object other){
		
		//Standard method of determining it's a laser
		if (!(other instanceof Laser)){
			return false;
		} else {
			Laser otherLaser = (Laser) other;
			
			//Just check if they have the same start and end positions
			return (xStart == otherLaser.getXStart() &&
					yStart == otherLaser.getYStart() &&
					xFinish == otherLaser.getXFinish() &&
					yFinish == otherLaser.getYFinish());
		}
	}
	
	/**
	 * Arbitrary hash code function for laser use in hash maps
	 */
	@Override
	public int hashCode(){
		return 7*xStart + 13*yStart + 11*xFinish + 29*yFinish;
	}
	
	/**
	 * Getters for the various laser fields
	 */
	public int getXStart(){return xStart;}
	public int getXFinish(){return xFinish;}
	public int getYStart(){return yStart;}
	public int getYFinish(){return yFinish;}
	public GameEngine.Color getColor(){return color;}

}
