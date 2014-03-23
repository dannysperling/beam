package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.me.beam.GameEngine;
import com.me.beam.GameEngine.GameState;

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
	///
	ArrayList<JLabel> goalTextFields = new ArrayList<JLabel>();
	JLabel beamGoalsLabel = new JLabel("Beam Goals");
	JButton buttonClear = new JButton("Clear");
	JButton buttonNew = new JButton("New");
	JButton buttonFin = new JButton("Finalize");
	
	
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
		sideBar.setLayout(new GridLayout(0, 3, 25, 50));
		sideBar.setAlignmentX(CENTER_ALIGNMENT);
		///
		for (GameEngine.Color c : GameEngine.Color.values()){
			if (c == GameEngine.Color.NONE) continue;
			JLabel num = new JLabel(c+" : X");
			num.setAlignmentX(CENTER_ALIGNMENT);
			num.setAlignmentY(CENTER_ALIGNMENT);
			goalTextFields.add(num);
		}
		sideBar.add(new JLabel(""));
		sideBar.add(beamGoalsLabel);
		sideBar.add(new JLabel(""));
		for (JLabel txt : goalTextFields){
			sideBar.add(new JButton(new ImageIcon("src/LeftArrow.png")));
			sideBar.add(txt);
			sideBar.add(new JButton(new ImageIcon("src/RightArrow.png")));
		}
		sideBar.add(buttonClear);
		sideBar.add(buttonNew);
		sideBar.add(buttonFin);
		///
		buttonFin.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new FinalizationWindow();
			}
			
		});
		///
		this.setSize(800, 600);
		this.add(mainPanel);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	

}
