package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


import main.EditorModel;
import model.Board;

public class NewWindow extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	JLabel labelTitle = new JLabel("Title");
	JLabel labelAuthor = new JLabel("Author");
	JTextField textTitle = new JTextField("Level Title (internal use only)");
	JTextField textAuthor = new JTextField("Your name here");
	JPanel panelButtons = new JPanel();
	JPanel panelSpinners = new JPanel();
	JButton buttonGo = new JButton("Go!");
	JButton buttonCancel = new JButton("Cancel");
	SpinnerModel spinYModel;
	SpinnerModel spinXModel;
	JSpinner spinY;
	JSpinner spinX;
	
	
	public NewWindow(final EditorModel m, final MainWindow main){
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		spinYModel =new SpinnerNumberModel(3,0,100,1);
		spinXModel =new SpinnerNumberModel(3,0,100,1);
		spinY = new JSpinner(spinYModel);
		spinX = new JSpinner(spinXModel);
		///
		buttonCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				close();
			}

		});
		
		buttonGo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				m.b = new Board((int)spinXModel.getValue(), (int)spinYModel.getValue());
				m.workingTitle = textTitle.getText();
				m.workingAuthor = textAuthor.getText();
				m.b.id = -1;
				main.update();
				close();
			}
		});
		///
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		panelSpinners.setLayout(new BoxLayout(panelSpinners, BoxLayout.X_AXIS));
		panelSpinners.add(new JLabel("Height: "));
		panelSpinners.add(spinY);
		panelSpinners.add(Box.createHorizontalGlue());
		panelSpinners.add(new JLabel("Width: "));
		panelSpinners.add(spinX);
		this.add(panelSpinners);
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
		panelButtons.add(buttonGo);
		this.add(panelButtons);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	public void close(){
		this.dispose();
	}
}
