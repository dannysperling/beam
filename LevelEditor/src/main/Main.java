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
		/*JFrame dummy = new JFrame();
		dummy.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dummy.add(new BoardPanel(model.b));
		dummy.setSize(600, 600);
		dummy.setVisible(true);*/
	}

}
