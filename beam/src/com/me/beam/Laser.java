package com.me.beam;

public class Laser {
	
	private int xStart;
	private int yStart;
	
	private int xFinish;
	private int yFinish;
	
	private GameEngine.Color color = GameEngine.Color.NONE;
	
	public Laser(int xStart, int yStart, int xFinish, int yFinish, GameEngine.Color color){
		this.xStart = xStart;
		this.yStart = yStart;
		this.xFinish = xFinish;
		this.yFinish = yFinish;
		this.color = color;
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
