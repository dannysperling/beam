package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.EditorModel;

public class FinalizationWindow extends JFrame {
	JLabel labelTitle = new JLabel("Title");
	JLabel labelAuthor = new JLabel("Author");
	JTextField textTitle = new JTextField("Level Title (internal use only)");
	JTextField textAuthor = new JTextField("Your name here");
	JPanel panelButtons = new JPanel();
	JButton buttonSave = new JButton("Save");
	JButton buttonCancel = new JButton("Cancel");

	public FinalizationWindow(final EditorModel m) {
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
				try {
					m.levelIO.saveBoard(m.b, name, author);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "A(n) "+e.getClass()+"  has occured!", "MUCH ERROR, VERY BROKEN, SUCH WOW", ERROR);
				}
				close();
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
