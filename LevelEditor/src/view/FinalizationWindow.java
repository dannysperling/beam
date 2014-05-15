package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.EditorModel;

public class FinalizationWindow extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	JLabel labelTitle = new JLabel("Title");
	JLabel labelAuthor = new JLabel("Author");
	JTextField textTitle = new JTextField("Level Title (internal use only)");
	JTextField textAuthor = new JTextField("Your name here");
	JPanel panelButtons = new JPanel();
	JButton buttonSave = new JButton("Save");
	JButton buttonCancel = new JButton("Cancel");
	int exitMode = DO_NOTHING_ON_CLOSE;
	EditorModel model;

	/**
	 * 
	 * @param m
	 * @param mode - One of FianlizationWindow.DO_NOTHING_ON_CLOSE or EXIT_ON_CLOSE. If the latter, the program terminates upon completion of the save operation.
	 */
	public FinalizationWindow(final EditorModel m, int mode) {
		model = m;
		exitMode = mode;
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		System.out.println("Working title: "+m.workingTitle);
		System.out.println("Working Author: "+m.workingAuthor);
		buttonCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				close();
			}

		});
		
		buttonSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String name = textTitle.getText();
				String author = textAuthor.getText();
				int id = -1;
				//TODO: Validate levels
				try {
					id = m.levelIO.saveBoard(m.b, name, author);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(FinalizationWindow.this, "A(n) "+e.getClass()+"  has occured!", "MUCH ERROR, VERY BROKEN, SUCH WOW", ERROR);
				}
				JOptionPane.showMessageDialog(FinalizationWindow.this, "Save sucessful! Your level has been assigned id #"+id, "Save Sucessful", JOptionPane.INFORMATION_MESSAGE);
				close();
				if (exitMode == EXIT_ON_CLOSE)
					System.exit(0);
			}
		});
		///
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.add(labelTitle);
		this.add(Box.createVerticalGlue());
		if (!m.workingTitle.isEmpty())
			textTitle.setText(m.workingTitle);
		this.add(textTitle);
		this.add(labelAuthor);
		this.add(Box.createVerticalGlue());
		if (!m.workingAuthor.isEmpty())
			textAuthor.setText(m.workingAuthor);
		this.add(textAuthor);
		this.setSize(400, 200);
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		panelButtons.add(Box.createHorizontalGlue());
		panelButtons.add(buttonCancel);
		panelButtons.add(buttonSave);
		this.add(panelButtons);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	public void close(){
		this.dispose();
	}
	
}
