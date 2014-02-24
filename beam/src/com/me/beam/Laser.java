package com.me.beam;

public class Laser {
	
	private int xStart;
	private int yStart;
	
	private int xFinish;
	private int yFinish;
	
	private GameEngine.Color color = GameEngine.Color.NONE;
	
	//xStart <= xFinish, yStart <= yFinish
	public Laser(int xStart, int yStart, int xFinish, int yFinish, GameEngine.Color color){
		this.xStart = xStart;
		this.yStart = yStart;
		this.xFinish = xFinish;
		this.yFinish = yFinish;
		this.color = color;
	}
	
	@Override
	public boolean equals(Object other){
		
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
	
	public int getXStart(){
		return xStart;
	}
	
	public int getXFinish(){
		return xFinish;
	}
	
	public int getYStart(){
		return yStart;
	}
	
	public int getYFinish(){
		return yFinish;
	}
	
	public GameEngine.Color getColor(){
		return color;
	}

}
