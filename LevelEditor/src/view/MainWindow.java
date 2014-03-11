package view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.me.beam.GameEngine;

public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel mainPanel = new JPanel();
	JPanel toolBar = new JPanel();
	JPanel sideBar = new JPanel();
	ButtonGroup radioGroup = new ButtonGroup();
	///
	JRadioButton jrbPiece = new JRadioButton("Piece");
	JRadioButton jrbGlass = new JRadioButton("Glass");
	JRadioButton jrbPainter = new JRadioButton("Painter");
	JRadioButton jrbGoal = new JRadioButton("Goal");
	///
	JComboBox<GameEngine.Color> colorDropdown;
	
	
	public MainWindow(){
		mainPanel.setLayout(new BorderLayout(0, 0));
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(sideBar, BorderLayout.EAST);
		toolBar.setBackground(Color.RED);
		sideBar.setBackground(Color.BLUE);
		///
		colorDropdown = new JComboBox<GameEngine.Color>(GameEngine.Color.values());
		toolBar.add(colorDropdown);
		toolBar.add(jrbPiece);
		toolBar.add(jrbGlass);
		toolBar.add(jrbPainter);
		toolBar.add(jrbGoal);
		
		
		///
		radioGroup.add(jrbPiece);
		radioGroup.add(jrbGlass);
		radioGroup.add(jrbGoal);
		radioGroup.add(jrbPainter);
		
		///
		this.setSize(800, 600);
		this.add(mainPanel);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	

}
