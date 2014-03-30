package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.EnumMap;

import javax.swing.BorderFactory;
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
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.EditorModel;

import com.me.beam.Board;
import com.me.beam.GameEngine;
import com.me.beam.Piece;
import com.me.beam.Tile;

public class MainWindow extends JFrame implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean saved = false;
	JPanel mainPanel = new JPanel();
	JPanel toolBar = new JPanel();
	JPanel sideBar = new JPanel();
	JPanel boardHolder = new JPanel();
	JPanel resizeButtonPanel = new JPanel();
	BoardPanel boardPanel;
	JPanel panelSideBarButtons = new JPanel();
	ButtonGroup radioGroup = new ButtonGroup();
	///
	JRadioButton jrbPiece = new JRadioButton("Piece");
	JRadioButton jrbGlass = new JRadioButton("Glass");
	JRadioButton jrbPainter = new JRadioButton("Painter");
	JRadioButton jrbGoal = new JRadioButton("Goal");
	///
	JLabel labelCurLevel = new JLabel();
	JButton buttonPlusRow = new JButton("Add Row");
	JButton buttonPlusCol = new JButton("Add Column");
	JButton buttonMinusRow = new JButton("Remove Row");
	JButton buttonMinusCol= new JButton("Remove Column");
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
	///
	EditorModel model;
	
	
	
	public MainWindow(final EditorModel m){
		saved = false;
		model = m;
		///
		boardPanel = new BoardPanel();
		boardPanel.setModel(m);
		boardHolder.setLayout(new BorderLayout(20,20));
		boardHolder.add(boardPanel,BorderLayout.CENTER);
		labelCurLevel.setText(m.fileName()+" - "+m.idString());
		boardHolder.add(labelCurLevel,BorderLayout.NORTH);
		resizeButtonPanel.setLayout(new BorderLayout(5,5));
		resizeButtonPanel.add(buttonMinusRow,BorderLayout.NORTH);
		resizeButtonPanel.add(buttonPlusRow,BorderLayout.SOUTH);
		resizeButtonPanel.add(buttonMinusCol,BorderLayout.WEST);
		resizeButtonPanel.add(buttonPlusCol,BorderLayout.EAST);
		boardHolder.add(resizeButtonPanel,BorderLayout.SOUTH);
		///
		mainPanel.setLayout(new BorderLayout(10, 10));
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(sideBar, BorderLayout.EAST);
		mainPanel.add(boardHolder,BorderLayout.CENTER);
		////
		spinPerfModel =new SpinnerNumberModel(m.b.perfect,0,100,1);
		spinParModel =new SpinnerNumberModel(m.b.par,0,100,1);
		spinPerf = new JSpinner(spinPerfModel);
		spinPar = new JSpinner(spinParModel);
		spinPerf.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				if (model.b.perfect == (int) spinPerfModel.getValue()) return;
				model.b.perfect = (int) spinPerfModel.getValue();
				if (((int) spinParModel.getValue()) < model.b.perfect){
					model.b.par = model.b.perfect;
					spinParModel.setValue(model.b.par);
				}
				saved = false;
			}
			
		});
		spinPar.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				if (model.b.par == (int) spinParModel.getValue()) return;
				model.b.par = (int) spinParModel.getValue();
				if (((int) spinParModel.getValue()) < model.b.perfect){
					model.b.par = model.b.perfect;
					spinParModel.setValue(model.b.par);
				}
				saved = false;
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
					saved = false;
				}
			});
		}
	
		panelSideBarButtons.add(buttonNew);
		panelSideBarButtons.add(buttonLoad);
		panelSideBarButtons.add(buttonFin);
		sideBar.add(Box.createVerticalStrut(375));
		sideBar.add(panelSideBarButtons);
		//sideBar.add(Box.createVerticalStrut(0));
		///
		this.setSize(758, 750);
		boardPanel.setBackground(new Color(255,255,240));
		boardPanel.setForeground(Color.BLACK);
		boardPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
		mainPanel.setBackground(new Color(75,100,150));
		boardHolder.setBackground(mainPanel.getBackground());
		mainPanel.add(new JLabel(""),BorderLayout.WEST);
		toolBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		sideBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		resizeButtonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		labelCurLevel.setForeground(Color.white);
		///
		buttonFin.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new FinalizationWindow(model,DO_NOTHING_ON_CLOSE);
				saved = true;
			}
			
		});
		buttonLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Board old = m.b;
				int id;
				try{
				String retVal = JOptionPane.showInputDialog(null, "Enter the id of the level you wish to load", "Select level", JOptionPane.QUESTION_MESSAGE);
				if (retVal.isEmpty()){
					return;
				}
				id = Integer.parseInt(retVal);
				} catch (NumberFormatException ex){
					JOptionPane.showMessageDialog(null, "Bro that wasn't a number!", "Wat?", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					m.loadBoard(id);
					if (m.b == null){
						JOptionPane.showMessageDialog(null, "Umm, I don't think there is a level "+id, ":(", JOptionPane.ERROR_MESSAGE);
						m.b = old;
					}	
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				saved = true;
				System.out.println("Level loaded. Save? - "+saved+" = "+saved);
				update();
			}
		});
		buttonNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new NewWindow(model, thisWindow());
				saved = false;
			}
		});
		///
		buttonPlusCol.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saved = false;
				Board nb = new Board(m.b.getNumHorizontalTiles()+1,m.b.getNumVerticalTiles());
				nb.id = m.b.id;
				nb.par = m.b.par;
				nb.perfect = m.b.perfect;
				for (Tile t : m.b.getAllTiles()){
					int x = t.getXCoord();
					int y = t.getYCoord();
					nb.setGlass(x, y, t.isGlass);
					nb.setGoal(x, y, t.getGoalColor());
					nb.setPainter(x, y, t.getPainterColor());
					
				}
				for (int y = 0; y < nb.getNumVerticalTiles(); y++){
					nb.setGlass(m.b.getNumHorizontalTiles(), y, true);
				}
				for (GameEngine.Color c : GameEngine.Color.values()){
					if (c==GameEngine.Color.NONE) continue;
					nb.addBeamObjective(c, m.b.getBeamObjectiveCount(c));
				}
				for (Piece p : m.b.getAllPieces()){
					if (p.getXCoord() > nb.getNumHorizontalTiles()-1) continue;
					if (p.getYCoord() > nb.getNumVerticalTiles()-1) continue;
					nb.put(p);
				}
				m.b = nb;
				update();
			}
		});
		buttonPlusRow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saved = false;
				Board nb = new Board(m.b.getNumHorizontalTiles(),m.b.getNumVerticalTiles()+1);
				nb.id = m.b.id;
				nb.par = m.b.par;
				nb.perfect = m.b.perfect;
				for (Tile t : m.b.getAllTiles()){
					int x = t.getXCoord();
					int y = t.getYCoord();
					nb.setGlass(x, y, t.isGlass);
					nb.setGoal(x, y, t.getGoalColor());
					nb.setPainter(x, y, t.getPainterColor());
					
				}
				for (int x = 0; x < nb.getNumHorizontalTiles(); x++){
					nb.setGlass(x, m.b.getNumVerticalTiles(), true);
				}
				for (GameEngine.Color c : GameEngine.Color.values()){
					if (c==GameEngine.Color.NONE) continue;
					nb.addBeamObjective(c, m.b.getBeamObjectiveCount(c));
				}
				for (Piece p : m.b.getAllPieces()){
					if (p.getXCoord() > nb.getNumHorizontalTiles()-1) continue;
					if (p.getYCoord() > nb.getNumVerticalTiles()-1) continue;
					nb.put(p);
				}
				m.b = nb;
				update();
			}
		});
		buttonMinusCol.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saved = false;
				if (m.b.getNumHorizontalTiles() == 1)return;
				Board nb = new Board(m.b.getNumHorizontalTiles()-1,m.b.getNumVerticalTiles());
				nb.id = m.b.id;
				nb.par = m.b.par;
				nb.perfect = m.b.perfect;
				for (Tile t : m.b.getAllTiles()){
					int x = t.getXCoord();
					int y = t.getYCoord();
					if (x >= nb.getNumHorizontalTiles()) continue;
					if (y >= nb.getNumVerticalTiles()) continue;
					nb.setGlass(x, y, t.isGlass);
					nb.setGoal(x, y, t.getGoalColor());
					nb.setPainter(x, y, t.getPainterColor());
					
				}
				for (GameEngine.Color c : GameEngine.Color.values()){
					if (c==GameEngine.Color.NONE) continue;
					nb.addBeamObjective(c, m.b.getBeamObjectiveCount(c));
				}
				for (Piece p : m.b.getAllPieces()){
					if (p.getXCoord() > nb.getNumHorizontalTiles()-1) continue;
					if (p.getYCoord() > nb.getNumVerticalTiles()-1) continue;
					nb.put(p);
				}
				m.b = nb;
				update();
			}
		});
		buttonMinusRow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saved = false;
				if (m.b.getNumVerticalTiles() == 1)return;
				Board nb = new Board(m.b.getNumHorizontalTiles(),m.b.getNumVerticalTiles()-1);
				nb.id = m.b.id;
				nb.par = m.b.par;
				nb.perfect = m.b.perfect;
				for (Tile t : m.b.getAllTiles()){
					int x = t.getXCoord();
					int y = t.getYCoord();
					if (x >= nb.getNumHorizontalTiles()) continue;
					if (y >= nb.getNumVerticalTiles()) continue;
					nb.setGlass(x, y, t.isGlass);
					nb.setGoal(x, y, t.getGoalColor());
					nb.setPainter(x, y, t.getPainterColor());
					
				}
				for (GameEngine.Color c : GameEngine.Color.values()){
					if (c==GameEngine.Color.NONE) continue;
					nb.addBeamObjective(c, m.b.getBeamObjectiveCount(c));
				}
				for (Piece p : m.b.getAllPieces()){
					if (p.getXCoord() > nb.getNumHorizontalTiles()-1) continue;
					if (p.getYCoord() > nb.getNumVerticalTiles()-1) continue;
					nb.put(p);
				}
				m.b = nb;
				update();
			}
		});
		///
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				if (!saved){
					promptSave();
				}
				System.exit(0);
			}
			public void windowClosed(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {}
		});
		///
		this.add(mainPanel);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.repaint();
		boardPanel.addMouseListener(this);
		this.setVisible(true);
	}


	protected void promptSave() {
		int opt = JOptionPane.showConfirmDialog(thisWindow(), "You have unsaved changes, would you like to save first?","Save First?", JOptionPane.YES_NO_OPTION);
		if (opt == JOptionPane.YES_OPTION){
			new FinalizationWindow(model,FinalizationWindow.EXIT_ON_CLOSE);
		}
		
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
		System.out.println("Update. Saved? - "+saved+" = "+saved);
		labelCurLevel.setText(model.fileName()+" - "+model.idString()+(saved?"":"*"));
		this.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
		int xPos = e.getX();
		int yPos = e.getY();
				
		int tileSize = boardPanel.getTileSize();
		
		//System.out.println("Why is the tile size " + tileSize);
		
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
			update();
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
