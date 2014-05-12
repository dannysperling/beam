package view;

import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import model.Board;

import com.me.beamsolver.Solver;

public class SolutionWindow extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	JLabel labelNumMoves = new JLabel();
	JScrollPane tracePane;
	JTextArea textArea = new JTextArea(50, 20);
	
	
	/**
	 * 
	 * @param b - Board to solve
	 */
	public SolutionWindow(Board b) {
		this.setTitle("Solution");
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		textArea.setEditable(false);
		///
		Solver sol = new Solver(b,false);
		labelNumMoves.setText("Perfect in "+sol.getMovesNeeded());
		textArea.setText(sol.getSolutionTrace());
		textArea.setCaretPosition(0);
		///
		this.setSize(300, 500);
		this.add(labelNumMoves);
		tracePane = new JScrollPane(textArea);
		tracePane.setToolTipText("Move trace");
		this.add(tracePane);
		this.setVisible(true);
		
	}
	
}
