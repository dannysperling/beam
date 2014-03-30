package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.EditorModel;

import com.me.beam.GameEngine;

public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel mainPanel = new JPanel();
	JPanel toolBar = new JPanel();
	JPanel sideBar = new JPanel();
	JPanel boardPanel;
	ButtonGroup radioGroup = new ButtonGroup();
	///
	JRadioButton jrbPiece = new JRadioButton("Piece");
	JRadioButton jrbGlass = new JRadioButton("Glass");
	JRadioButton jrbPainter = new JRadioButton("Painter");
	JRadioButton jrbGoal = new JRadioButton("Goal");
	///
	JComboBox<GameEngine.Color> colorDropdown;
	SpinnerModel spinPerfModel;
	SpinnerModel spinParModel;
	JSpinner spinPar;
	JSpinner spinPerf;
	///
	ArrayList<JLabel> goalTextFields = new ArrayList<JLabel>();
	JLabel beamGoalsLabel = new JLabel("Beam Goals");
	JButton buttonNew = new JButton("New");
	JButton buttonLoad = new JButton("Load");
	JButton buttonFin = new JButton("Finalize");
	EditorModel model;
	
	
	
	public MainWindow(final EditorModel m){
		model = m;
		boardPanel = new BoardPanel(model);
		mainPanel.setLayout(new BorderLayout(0, 0));
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(sideBar, BorderLayout.EAST);
		mainPanel.add(boardPanel,BorderLayout.CENTER);
		//toolBar.setBackground(Color.GRAY);
		//sideBar.setBackground(Color.GRAY);
		boardPanel.setBackground(Color.DARK_GRAY);
		////
		spinPerfModel =new SpinnerNumberModel(m.b.perfect,0,100,1);
		spinParModel =new SpinnerNumberModel(m.b.par,0,100,1);
		spinPerf = new JSpinner(spinPerfModel);
		spinPar = new JSpinner(spinParModel);
		spinPerf.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				model.b.perfect = (int) spinPerfModel.getValue();
				if (((int) spinParModel.getValue()) < model.b.perfect){
					model.b.par = model.b.perfect;
					spinParModel.setValue(model.b.par);
				}
				
			}
			
		});
		spinPar.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				model.b.par = (int) spinParModel.getValue();
				if (((int) spinParModel.getValue()) < model.b.perfect){
					model.b.par = model.b.perfect;
					spinParModel.setValue(model.b.par);
				}
				
			}
			
		});
		///
		toolBar.add(new JLabel("Perfect: "));
		toolBar.add(spinPerf);
		toolBar.add(new JLabel("Par: "));
		toolBar.add(spinPar);
		///
		toolBar.add(new JLabel("Active Tool: "));
		colorDropdown = new JComboBox<GameEngine.Color>(GameEngine.Color.values());
		toolBar.add(colorDropdown);
		toolBar.add(jrbPiece);
		toolBar.add(jrbGlass);
		toolBar.add(jrbPainter);
		toolBar.add(jrbGoal);
		////
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
		sideBar.add(buttonNew);
		sideBar.add(buttonLoad);
		sideBar.add(buttonFin);
		///
		buttonFin.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new FinalizationWindow(model);
			}
			
		});
		buttonLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int oldID = m.b.id;
				int id;
				try{
				id = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter the id of the level you wish to load", "Select level", JOptionPane.QUESTION_MESSAGE));
				} catch (NumberFormatException ex){
					JOptionPane.showMessageDialog(null, "Bro that wasn't a number!", "Wat?", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					m.loadBoard(id);
					if (m.b == null){
						m.loadBoard(oldID);
						JOptionPane.showMessageDialog(null, "Umm, I don't think there is a level "+id, ":(", JOptionPane.ERROR_MESSAGE);
					}
					update();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		///
		this.setSize(800, 600);
		this.add(mainPanel);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.repaint();
		this.setVisible(true);
	}
	
	public void update(){
		boardPanel = new BoardPanel(model);
		spinPerfModel.setValue(model.b.perfect);
		spinParModel.setValue(model.b.par);
		this.repaint();
	}
	

}
