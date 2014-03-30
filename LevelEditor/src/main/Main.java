package main;

import java.io.IOException;

import view.MainWindow;

public class Main {
	
	public static void main(String[] args) throws IOException{
		EditorModel model = new EditorModel();
		model.loadBoard(1);
		
		@SuppressWarnings("unused")
		MainWindow mw = new MainWindow(model);
	}

}
