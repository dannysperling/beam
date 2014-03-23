package view;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.me.beam.Board;
import com.me.beam.GameEngine;
import com.me.beam.Laser;
import com.me.beam.Piece;
import com.me.beam.Tile;


public class BoardPanel extends JPanel{

	private Board b;
	private int tilesize;


	private static final long serialVersionUID = 1L;

	public BoardPanel(Board b){
		this.b = b;
		int widthTiles = this.getSize().width /  b.getNumHorizontalTiles();
		int heightTiles = this.getSize().height / b.getNumVerticalTiles();
		tilesize = Math.min(widthTiles, heightTiles);
	}

	public static Color translateColor(GameEngine.Color c) {
		switch (c) {
		case RED:
			return new Color(1, .133f, .133f, 1);
		case BLUE:
			return new Color(.133f, .337f, 1, 1);
		case GREEN:
			return new Color(.133f, 1, .177f, 1);
		default:
			return new Color(0, 0, 0, 0);	
		}
	}

	@Override
	public void paint(Graphics g){
		List<Piece> pieces = b.getAllPieces();
		List<Tile> tiles = b.getAllTiles();
		int bx = 0;
		int by = 0;
		
		// Draw the basic grid
		g.setColor(Color.white);
		for (int i = 0; i <= b.getNumHorizontalTiles(); i++) {
			g.drawLine(bx + (i * tilesize), by, bx + (i * tilesize),
					by + (b.getNumVerticalTiles() * tilesize));
		}
		for (int i = 0; i <= b.getNumVerticalTiles(); i++) {
			g.drawLine(bx, by + (i * tilesize),
					bx + (b.getNumHorizontalTiles() * tilesize), by
					+ (i * tilesize));
		}

		// Draw the tiles
		for (Tile t : tiles) {
			if (t.isGlass) {
				g.setColor(Color.WHITE);
				int glassX = bx + (t.getXCoord() * tilesize);
				int glassY = by + (t.getYCoord() * tilesize);
				g.drawLine(glassX,(int)( glassY + (0.25f * tilesize)),(int)( glassX
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
						glassX + tilesize, (int)(glassY + (0.75f * tilesize)));
			}
		}
		for (Tile t : tiles) {
			if (t.hasGoal()) {
				int goalX = bx + (t.getXCoord() * tilesize);
				int goalY = by + (t.getYCoord() * tilesize);
				g.setColor(translateColor(t.getGoalColor()));
				g.fillRect((int)(goalX + (0.05f * tilesize)), (int)(goalY
						+ (0.05f * tilesize)), (int)(0.9f * tilesize), (int)(0.9f * tilesize));
				g.setColor(Color.DARK_GRAY);
				g.fillRect((int)(goalX + (0.12f * tilesize)), (int)(goalY
						+ (0.12f * tilesize)), (int)(0.76f * tilesize), (int)(0.76f * tilesize));
			}
		}
		for (Tile t : tiles) {
			if (t.hasPainter()) {
				int paintX = bx + (t.getXCoord() * tilesize);
				int paintY = by + (t.getYCoord() * tilesize);
				switch (t.getPainterColor()) {
				case RED:
					g.setColor(new Color(.3f, 0, 0, 1));
					break;
				case BLUE:
					g.setColor(new Color(0, 0, .3f, 1));
					break;
				case GREEN:
					g.setColor(new Color(0, .3f, 0, 1));
					break;
				default:
					g.setColor(new Color(0, 0, 0, 0));
					break;
				}
				g.fillRect((int)(paintX + (0.05f * tilesize)), (int)(paintY
						+ (0.05f * tilesize)), (int)(0.9f * tilesize), (int)(0.9f * tilesize));
			}
		}
		float moveAnimateTime = 0;

		// Draw the pieces
		List<Tile> path = GameEngine.movePath;
		Color paintColor = new Color(0,0,0,0);
		if(path.size() > 1){
			paintColor = translateColor(b.getTileAtBoardPosition(path.get(1).getXCoord(), path.get(1).getYCoord()).getPainterColor());
		}
		Laser disbandedLaser = null;
		Laser movedAlongLaser = null;
		float breakAnimateTime = 0;
		float formAnimateTime = 0;
		float paintAnimateTime = 0;

		for (Piece p : pieces) {
			g.setColor(translateColor(p.getColor()));
			if(p.equals(GameEngine.movingPiece)){
				float rshift = (paintColor.getRed() - translateColor(p.getColor()).getRed()) * paintAnimateTime;
				float gshift = (paintColor.getGreen() - translateColor(p.getColor()).getGreen()) * paintAnimateTime;
				float bshift = (paintColor.getBlue() - translateColor(p.getColor()).getBlue()) * paintAnimateTime;
				g.setColor(new Color(translateColor(p.getColor()).getRed() + rshift, translateColor(p.getColor()).getGreen() + gshift, translateColor(p.getColor()).getBlue() + bshift, 1));
			}
			g.fillOval(bx + (p.getXCoord() * tilesize),
					by + (p.getYCoord() * tilesize), tilesize, tilesize);
		}


		// Draw Lasers
		Set<Laser> lasers = b.lasers;

		float laserWidth = 0.1f * tilesize;
		for (Laser l : lasers) {
			if (disbandedLaser != null && l.equals(disbandedLaser)){
				laserWidth = (1 - breakAnimateTime) * 0.1f * tilesize;
			} else {
				laserWidth = 0.1f * tilesize;
			}
			g.setColor(translateColor(l.getColor()));
			if(!l.equals(movedAlongLaser)){
				if (l.getXStart() == l.getXFinish()) {
					g.fillRect((int)(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize),
							(int)(by + (l.getYStart() + 0.5f - (laserWidth / 2)) * tilesize),
							(int)(laserWidth * tilesize), (int)((l.getYFinish() - l.getYStart())
							* tilesize));
				} else {
					g.fillRect((int)(bx + (l.getXStart() + 0.5f - (laserWidth / 2)) * tilesize),
							(int)(by + (l.getYStart() + 0.5f - (laserWidth/2)) * tilesize),
							(int)((l.getXFinish() - l.getXStart()) * tilesize),
							(int)(laserWidth * tilesize));
				}
			}
		}

	}
}
