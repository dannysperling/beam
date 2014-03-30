package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.EnumMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import com.me.beam.Piece;

public class MainWindow extends JFrame implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel mainPanel = new JPanel();
	JPanel toolBar = new JPanel();
	JPanel sideBar = new JPanel();
	BoardPanel boardPanel;
	JPanel panelSideBarButtons = new JPanel();
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
	EnumMap<GameEngine.Color,SpinnerModel> goalSpinnerModels = new EnumMap<GameEngine.Color,SpinnerModel>(GameEngine.Color.class);
	JLabel beamGoalsLabel = new JLabel("Beam Goals");
	JButton buttonNew = new JButton("New");
	JButton buttonLoad = new JButton("Load");
	JButton buttonFin = new JButton("Save");
	EditorModel model;
	
	
	
	public MainWindow(final EditorModel m){
		model = m;
		boardPanel = new BoardPanel();
		boardPanel.setModel(model);
		boardPanel.addMouseListener(this);
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
		jrbPiece.setSelected(true);
		///
		sideBar.setLayout(new BoxLayout(sideBar,BoxLayout.Y_AXIS));
		beamGoalsLabel.setAlignmentX(CENTER_ALIGNMENT);
		sideBar.add(beamGoalsLabel);
		JLabel tip = new JLabel("(-1 = No requirment)");
		tip.setAlignmentX(CENTER_ALIGNMENT);
		sideBar.add(tip);
		for (final GameEngine.Color c : GameEngine.Color.values()){
			if (c == GameEngine.Color.NONE) continue;
			final SpinnerNumberModel snm = new SpinnerNumberModel(m.b.getBeamObjectiveCount(c),-1,100,1);
			goalSpinnerModels.put(c,snm);
			sideBar.add(new JLabel(c.toString()));
			sideBar.add(new JSpinner(snm));
			snm.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent arg0) {
					model.b.addBeamObjective(c, (int)snm.getValue());
					
				}
			});
		}
		
		///Panel these together TODO
		panelSideBarButtons.add(buttonNew);
		panelSideBarButtons.add(buttonLoad);
		panelSideBarButtons.add(buttonFin);
		sideBar.add(Box.createVerticalGlue());
		sideBar.add(panelSideBarButtons);
		sideBar.add(Box.createVerticalStrut(0));
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
		buttonNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new NewWindow(model, thisWindow());
			}
		});
		///
		this.setSize(800, 600);
		this.add(mainPanel);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.repaint();
		this.setVisible(true);
	}
	
	protected MainWindow thisWindow() {
		return this;
	}

	public void update(){
		boardPanel.setModel(model);
		//System.out.println("Par: "+model.b.par+"Perfect: "+model.b.perfect);
		spinParModel.setValue(model.b.par);
		spinPerfModel.setValue(model.b.perfect);
		for (GameEngine.Color c : GameEngine.Color.values()){
			if (c == GameEngine.Color.NONE) continue;
			SpinnerModel sm = goalSpinnerModels.get(c);
			sm.setValue(model.b.getBeamObjectiveCount(c));
		}
		this.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
		int xPos = e.getX();
		int yPos = e.getY();
				
		int tileSize = boardPanel.getTileSize();
		
		System.out.println("Why is the tile size " + tileSize);
		
		if (tileSize == 0)
			return;
		
		int tileX = xPos / tileSize;
		int tileY = yPos / tileSize;
		
		if (tileX < model.b.getNumHorizontalTiles() && tileY < model.b.getNumVerticalTiles()){
			
			GameEngine.Color curColor = (GameEngine.Color) colorDropdown.getSelectedItem();
			int button = e.getButton();
			
			//Determine what to do based on selected button and press
			if (jrbPiece.isSelected()){
				//Always remove, add of current color if first button press
				//Basically, left click to add, right click to remove
				Piece p = new Piece(tileX, tileY, curColor);
				model.b.removePiece(p);
				
				if (button == MouseEvent.BUTTON1 && curColor != GameEngine.Color.NONE){
					model.b.put(p);
				}
				
			} else if (jrbGlass.isSelected()){
				model.b.setGlass(tileX, tileY, button == MouseEvent.BUTTON1);
				
			} else if (jrbGoal.isSelected()){
				//Allows for right click to remove
				if (button != MouseEvent.BUTTON1){
					curColor = GameEngine.Color.NONE;
				}
				model.b.setGoal(tileX, tileY, curColor);
				
			} else if (jrbPainter.isSelected()){
				//Allows for right click to remove
				if (button != MouseEvent.BUTTON1){
					curColor = GameEngine.Color.NONE;
				}
				model.b.setPainter(tileX, tileY, curColor);
			}
			boardPanel.repaint();
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
	

}
