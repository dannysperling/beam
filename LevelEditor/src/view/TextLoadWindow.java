package view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import main.EditorModel;

import com.me.beam.Board;

public class TextLoadWindow extends JDialog {
	JTextArea inputArea;
	JPanel panelButtons = new JPanel();
	JPanel panelSpinners = new JPanel();
	JButton buttonLoad = new JButton("Load");
	JButton buttonCancel = new JButton("Cancel");

	public TextLoadWindow(final EditorModel m) {
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		String clip;
		try {
			clip = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			clip = null;
		}
		if (clip != null && clip.contains("<level")) {
			inputArea = new JTextArea(clip, 20, 50);
		} else {
			inputArea = new JTextArea(20, 50);
		}
		// /
		buttonCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}

		});

		buttonLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m.b = m.levelIO.buildBoard(inputArea.getText().trim());
				dispose();
			}
		});
		///
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.add(inputArea);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(buttonCancel);
		buttons.add(buttonLoad);
		inputArea.setBackground(Color.white);
		buttons.setBorder(BorderFactory.createRaisedBevelBorder());
		this.add(buttons);
		this.pack();
		this.setVisible(true);
	}

}
