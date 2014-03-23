package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FinalizationWindow extends JFrame {
	JLabel labelTitle = new JLabel("Title");
	JLabel labelAuthor = new JLabel("Author");
	JTextField textTitle = new JTextField("Level Title (internal use only)");
	JTextField textAuthor = new JTextField("Your name here");
	JPanel panelButtons = new JPanel();
	JButton buttonSave = new JButton("Save");
	JButton buttonCancel = new JButton("Cancel");

	public FinalizationWindow() {
		buttonCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				close();
			}

		});
		// /
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.add(labelTitle);
		this.add(Box.createVerticalGlue());
		this.add(textTitle);
		this.add(labelAuthor);
		this.add(Box.createVerticalGlue());
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
