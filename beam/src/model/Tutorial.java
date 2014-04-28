package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tutorial {
	
	
	/**
	 * Indicates how many elements are in this tutorial
	 *  a.k.a. Lines in the original tutorial file
	 */
	private int numElements;
	
	/**
	 * Only two possible elements in a tutorial at present:
	 * Text, or an animation. None is to handle out of bounds access
	 */
	public enum ElementType{
		TEXT, ANIMATED_IMAGE, NONE
	}
	
	/**
	 * Store an in-order list of the element types
	 */
	private List<ElementType> elementsInTutorial;
	
	/**
	 * Store the text lines and the lists of image sprites
	 * in separate maps. Must be accessed separately
	 */
	private Map<Integer, String> textElements;
	private Map<Integer, List<Sprite>> imageElements;
	
	/**
	 * Default constructor is an empty tutorial object
	 */
	public Tutorial(){
		elementsInTutorial = new ArrayList<ElementType>();
		numElements = 0;
		textElements = new HashMap<Integer, String>();
		imageElements = new HashMap<Integer, List<Sprite>>();
	}
	
	/**
	 * Adds an image element to the end of the tutorial based on its elements.
	 */
	public void addImageElement(String fileName, int sideLength, int numFrames){
		
		//Indicate that we're adding an image element
		elementsInTutorial.add(ElementType.ANIMATED_IMAGE);
		
		//Add the element
		Texture stripTexture = new Texture(Gdx.files.internal("data/tutorials/" + fileName));
		stripTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion[][] tiles = TextureRegion.split(stripTexture, sideLength, sideLength);
		
		//Extract correct number of frames
		int tilesUsed = 0;
		int row = 0, col = 0;
		List<Sprite> spriteFrames = new ArrayList<Sprite>();
		
		//Go rows first, then columns
		while (row < tiles.length && tilesUsed < numFrames){
			while(col < tiles[0].length && tilesUsed < numFrames){
				
				//Insert the sprites
				spriteFrames.add(new Sprite(tiles[row][col]));
				tilesUsed++;
				col++;
			}
			col = 0;
			row++;
		}
		imageElements.put(numElements, spriteFrames);
		
		//Increment number of elements
		numElements++;
	}
	
	/**
	 * Adds a text element to the end of the tutorial
	 */
	public void addTextElement(String text){
		
		//Indicate that we're adding a text element
		elementsInTutorial.add(ElementType.TEXT);
		
		//Add the element
		textElements.put(numElements, text);
		
		//Increment number of elements
		numElements++;
	}
	
	/**
	 * Gets the number of elements in this tutorial
	 */
	public int getNumElements(){
		return numElements;
	}
	
	/**
	 * Returns the element type at a given index
	 * 
	 * @param index
	 * 			The index of the element type to get, 0 <= index < numElements 
	 * @return
	 * 			The element type there, or NONE if out of bounds
	 */
	public ElementType getElementTypeAt(int index){
		if (0 <= index  && index < numElements){
			return elementsInTutorial.get(index);
		}
		return ElementType.NONE;
	}
	
	/**
	 * Returns the text elements at a given index, or
	 * null if no element at that index
	 */
	public String getTextElementAt(int index){
		return textElements.get(index);
	}
	
	/**
	 * Returns the image sprites in order at a given index,
	 * or null if no image element at that index
	 * 
	 * Note: Could be modified to return a list of sprites.
	 */
	public List<Sprite> getImageElementAt(int index){
		return imageElements.get(index);
	}
}
