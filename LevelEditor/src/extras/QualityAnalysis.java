package extras;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import main.LevelIO;
import model.Board;

public class QualityAnalysis {

	public static void main(String[] args) throws IOException {
		File dir = new File("src/").getAbsoluteFile();
		dir = dir.getParentFile().getParentFile();
		dir = new File(dir.getAbsolutePath()+"/beam-android/assets/data/levels/");
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(dir);
		fc.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "xml files";
			}
			
			@Override
			public boolean accept(File arg0) {
				if (arg0.isDirectory()) return true;
				return arg0.getName().endsWith("xml");
			}
		});
		int fcVal = fc.showOpenDialog(null);
		if (fcVal != JFileChooser.APPROVE_OPTION) return;
		LevelIO loader = new LevelIO(fc.getSelectedFile().getCanonicalPath(),null);
		
	}
	
	public static double getSLQF(Board b){
		double numPieces = b.getAllPieces().size();
		return (double)b.perfect/numPieces;
	}
	
	

}
