package main;

import java.io.IOException;

import javax.swing.JFrame;

import view.BoardPanel;
import view.MainWindow;

public class Main {
	
	public static void main(String[] args) throws IOException{
		EditorModel model = new EditorModel();
		model.loadBoard(1);
		MainWindow mw = new MainWindow(model);
	}

}
