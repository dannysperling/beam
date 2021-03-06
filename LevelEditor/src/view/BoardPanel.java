package view;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import main.EditorModel;
import model.Laser;
import model.Piece;
import model.Tile;

import controller.GameEngine;


public class BoardPanel extends JPanel{
	private EditorModel m;
	private int tilesize;


	private static final long serialVersionUID = 1L;

	public BoardPanel(){
	}
	
	public void setModel(EditorModel model){
		m = model;
		if (m.b == null) return;
		int widthTiles = this.getSize().width /  m.b.getNumHorizontalTiles();
		int heightTiles = this.getSize().height / m.b.getNumVerticalTiles();
		tilesize = Math.min(widthTiles, heightTiles);
	}

	public static Color translateColor(GameEngine.Color c) {
		switch (c) {
		case ORANGE:
			return new Color(1, .533f, .133f, 1);
		case BLUE:
			return new Color(.333f, .437f, 1, 1);
		case PURPLE:
			return Color.magenta.darker();
		case GREEN:
			return new Color(10,150,10);
		case BLACK:
			return Color.black;
		default:
			return new Color(0, 0, 0, 0);	
		}
	}
	
	public int getTileSize(){
		return tilesize;
	}

	@Override
	public void paint(Graphics g){
		//super.paint(g);//Allows boarders,bevels,etc
		List<Piece> pieces = m.b.getAllPieces();
		List<Tile> tiles = m.b.getAllTiles();
		
		int widthTiles = this.getSize().width /  m.b.getNumHorizontalTiles();
		int heightTiles = this.getSize().height / m.b.getNumVerticalTiles();
		tilesize = Math.min(widthTiles, heightTiles);
		EditorModel.initializeLasers(m.b);
		//System.out.println(m.b.lasers.size());
		
		int bx = 0;
		int by = 0;
		
		g.setColor(this.getBackground());
		g.fillRect(0, 0, tilesize*m.b.getNumHorizontalTiles(), tilesize*m.b.getNumVerticalTiles());
		
		// Draw the basic grid
		g.setColor(this.getForeground());
		for (int i = 0; i <= m.b.getNumHorizontalTiles(); i++) {
			g.drawLine(bx + (i * tilesize), by, bx + (i * tilesize),
					by + (m.b.getNumVerticalTiles() * tilesize));
		}
		for (int i = 0; i <= m.b.getNumVerticalTiles(); i++) {
			g.drawLine(bx, by + (i * tilesize),
					bx + (m.b.getNumHorizontalTiles() * tilesize), by
					+ (i * tilesize));
		}

		// Draw the tiles
		for (Tile t : tiles) {
			if (t.hasGlass()) {
				g.setColor(Color.DARK_GRAY);
				int glassX = bx + (t.getXCoord() * tilesize);
				int glassY = by + (t.getYCoord() * tilesize);
				g.fillRect(glassX, glassY, tilesize, tilesize);
				/*g.drawLine(glassX,(int)( glassY + (0.25f * tilesize)),(int)( glassX
						+ (0.25f * tilesize)), glassY);
				g.drawLine(glassX, (int)(glassY + (0.5f * tilesize)), (int)(glassX
						+ (0.5f * tilesize)), glassY);
				g.drawLine(glassX, (int)(glassY + (0.75f * tilesize)), (int)(glassX
						+ (0.75f * tilesize)), glassY);
				g.drawLine(glassX, glassY + tilesize, glassX + tilesize,
						glassY);
				g.drawLine((int)(glassX + (0.25f * tilesize)), glassY + tilesize,
						glassX + tilesize, (int)(glassY + (0.25f * tilesize)));
				g.drawLine((int)(glassX + (0.5f * tilesize)), glassY + tilesize,
						glassX + tilesize, (int)(glassY + (0.5f * tilesize)));
				g.drawLine((int)(glassX + (0.75f * tilesize)), glassY + tilesize,
						glassX + tilesize, (int)(glassY + (0.75f * tilesize)));*/
			}
		}
		for (Tile t : tiles) {
			if (t.hasGoal()) {
				int goalX = bx + (t.getXCoord() * tilesize);
				int goalY = by + (t.getYCoord() * tilesize);
				g.setColor(translateColor(t.getGoalColor()));
				g.fillRect((int)(goalX + (0.05f * tilesize)), (int)(goalY
						+ (0.05f * tilesize)), (int)(0.9f * tilesize), (int)(0.9f * tilesize));
				g.setColor(getBackground());
				g.fillRect((int)(goalX + (0.12f * tilesize)), (int)(goalY
						+ (0.12f * tilesize)), (int)(0.76f * tilesize), (int)(0.76f * tilesize));
			}
		}
		for (Tile t : tiles) {
			if (t.hasPainter()) {
				int paintX = bx + (t.getXCoord() * tilesize);
				int paintY = by + (t.getYCoord() * tilesize);
				switch (t.getPainterColor()) {
				case ORANGE:
					g.setColor(new Color(.3f, 0, 0, 1));
					break;
				case BLUE:
					g.setColor(new Color(0, 0, .3f, 1));
					break;
				case PURPLE:
					g.setColor(new Color(0, .3f, 0, 1));
					break;
				default:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				}
				//EXPERIMENTAL (TODO decide)
				g.setColor(translateColor(t.getPainterColor()).darker().darker());
				//END EXPERIMENTAL
				g.fillRect((int)(paintX + (0.05f * tilesize)), (int)(paintY
						+ (0.05f * tilesize)), (int)(0.9f * tilesize), (int)(0.9f * tilesize));
			}
		}

		// Draw the pieces
		List<Tile> path = GameEngine.movePath;
		Color paintColor = new Color(0,0,0,0);
		if(path.size() > 1){
			paintColor = translateColor(m.b.getTileAtBoardPosition(path.get(1).getXCoord(), path.get(1).getYCoord()).getPainterColor());
		}

		for (Piece p : pieces) {
			g.setColor(translateColor(p.getColor()));
			if(p.equals(GameEngine.movingPiece)){
				float rshift = (paintColor.getRed() - translateColor(p.getColor()).getRed()) * 0;
				float gshift = (paintColor.getGreen() - translateColor(p.getColor()).getGreen()) * 0;
				float bshift = (paintColor.getBlue() - translateColor(p.getColor()).getBlue()) * 0;
				g.setColor(new Color(translateColor(p.getColor()).getRed() + rshift, translateColor(p.getColor()).getGreen() + gshift, translateColor(p.getColor()).getBlue() + bshift, 1));
			}
			int offset = (int) Math.round(tilesize*0.12);
			g.fillOval(bx + (p.getXCoord() * tilesize)+offset,
					by + (p.getYCoord() * tilesize)+offset, tilesize-2*offset, tilesize-2*offset);
		}


		// Draw Lasers
		Set<Laser> lasers = m.b.lasers;

		float laserWidth = 0.075f;
		for (Laser l : lasers) {
			g.setColor(translateColor(l.getColor()));
			//System.out.println(l.getXStart()+" , "+l.getYStart()+" to "+l.getXFinish()+" , "+l.getYFinish());
			//System.out.println("TileSize: "+ tilesize);
			if(true){
				if (l.getXStart() == l.getXFinish()) {
					g.fillRect((int)(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize),
							(int)(by + (l.getYStart() + 0.5f - (laserWidth / 2)) * tilesize),
							(int)(laserWidth * tilesize), (int)((l.getYFinish() - l.getYStart())
							* tilesize));
				} else {
					//System.out.println("Horizontal");
					g.fillRect((int)(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize),
							(int)(by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize),
							(int)((l.getXFinish() - l.getXStart()) * tilesize),
							(int)(laserWidth * tilesize));
					//System.out.println(((int)(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize)) + ", " + ((int)(by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize)));
					//System.out.println(((int)((l.getXFinish() - l.getXStart()) * tilesize)) + " , " + ((int)(laserWidth * tilesize)));
				}
			}
		}

	}
}
