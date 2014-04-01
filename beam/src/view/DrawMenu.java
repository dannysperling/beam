package view;

import java.util.List;

import model.Board;
import model.Menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class DrawMenu {

	private Menu menu;
	private BitmapFont numberFont;
	private SpriteBatch batch;

	public DrawMenu(Menu menu){
		this.menu = menu;
		batch = new SpriteBatch();
		initFonts();
	}

	public void draw(DrawGame dg, List<Board> boardList, Board curBoard, int curLevel){
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		int verticalScrolled = menu.getVerticalScrollAmount();
		int height = Gdx.graphics.getHeight();
		int width = Gdx.graphics.getWidth();
		int itemHeight = menu.getItemHeight();
		int itemWidth = menu.getItemWidth();
		int itemTopY = (verticalScrolled + 1) % itemHeight + height;
		int world = menu.getWorldAtPosition(height);

		TextBounds tb;

		//Loop until the item wouldn't show at all
		String stringNumber;
		while(itemTopY > 0){
			//Try to draw world colors? This loop is strange to me
			//Am I drawing upside down?
			ShapeRenderer shape = dg.shapes;
			shape.begin(ShapeType.Filled);
			Color start = Menu.colorOfWorld(world).mul(1.1f);//change mul factor to lighten startpoint
			Color dark = Menu.colorOfWorld(world).mul(.25f);//change mul factor to lighten endpoint
			//Draw main backgroun:
			shape.rect(-menu.getHorizontalScrollAmount(world), itemTopY-itemHeight, itemWidth*menu.sizeOfWorld(world), itemHeight, start, dark, dark, start);
			//Now draw overflow boxes:
			shape.setColor(start);
			shape.rect(-menu.getHorizontalScrollAmount(world) - 100, itemTopY-itemHeight, 100, itemHeight);
			shape.setColor(dark);
			shape.rect(-menu.getHorizontalScrollAmount(world)+itemWidth*menu.sizeOfWorld(world), itemTopY-itemHeight, 100, itemHeight);
			//Alpha broken :(//shape.rect(-menu.getHorizontalScrollAmount(world), itemTopY-itemHeight, itemWidth*menu.sizeOfWorld(world), itemHeight/10.0f, Color.WHITE, Color.WHITE, new Color(1,1,1,0.0f), new Color(1,1,1,0.5f));
			shape.end();
			//Ensure world in bounds
			if (menu.worldInBounds(world)){
				//Loop through horizontally as well
				int horizontalScrolled = menu.getHorizontalScrollAmount(world);
				int itemLeftX = -(horizontalScrolled % itemWidth);
				int itemBotY = itemTopY - itemHeight;
				int index = menu.getLevelAtPosition(itemLeftX + itemWidth / 2, itemTopY - itemHeight / 2);
				boolean worldUnlocked = menu.isWorldUnlocked(world);
				
				
				while (itemLeftX < width - 1){
					//Ensure index in bounds
					if (index != -1){
						//Figure out how to draw the item
						if (!worldUnlocked || (menu.isBonus(world, index) && !menu.isBonusLevelUnlocked(world))){
							numberFont.setColor(Color.RED);
						} else {
							int bestMoves = menu.getLevelMoves(index);
							if (bestMoves != 0){
								numberFont.setColor(Color.GREEN);
							} else {
								numberFont.setColor(new Color(.133f, .337f, 1, 1));
							}
						}
						
						//Either draw current state or entire board
						Board b = (index == curLevel) ? curBoard : boardList.get(index);
						
						// Get board dimensions
						Color curBG = new Color(.1f, .1f, .1f, 1);
						int by = (int)((1 - menu.boardHeightPercent) / 4 * itemHeight + itemBotY);
						int tilesize = (int)(menu.boardHeightPercent * itemHeight / b.getNumVerticalTiles());
						int bx = (itemWidth - tilesize * b.getNumHorizontalTiles()) / 2 + itemLeftX;
						
						dg.drawBoard(b, bx, by, tilesize, curBG);
						
						//Draw number on top
						batch.begin();
						stringNumber = (world + 1) + "-" + menu.getPositionInWorld(index);
						tb = numberFont.getBounds(stringNumber);
						numberFont.draw(batch, stringNumber, itemLeftX + (itemWidth - tb.width)/2, 
								itemTopY - ((itemHeight * (1-menu.boardHeightPercent) * 3/4) - tb.height) / 2);
						batch.end();
					}
					
					itemLeftX += itemWidth;
					index = menu.getLevelAtPosition(itemLeftX + itemWidth / 2, itemTopY - itemHeight / 2);
				}
			}

			itemTopY -= itemHeight;
			world++;
		}
	}

	public void dispose(){

	}

	//Initializes menu fonts
	public void initFonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/swanse.ttf"));
		numberFont = generator.generateFont((int) (menu.getItemHeight() * (1 - menu.boardHeightPercent) / 2));
		generator.dispose();
	}
}
