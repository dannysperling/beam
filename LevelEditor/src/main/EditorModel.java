package main;

import java.io.IOException;

import com.me.beam.Board;

public class EditorModel {
	public Board b;
	public String outputFile = "";
	LevelLoader ll;
	
	
	public void loadBoard(int n) throws IOException{
		if (ll == null){
			ll = new LevelLoader("src/levels.xml");
		}
		b = ll.getLevel(n);
	}
}
