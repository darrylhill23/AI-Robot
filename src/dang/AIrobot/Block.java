package dang.AIrobot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

public class Block implements PanelObject {

	int size = 10;
	Point2D.Double pos;
	Point mapPos;
	
	public Block(Point2D.Double pos){
		this.pos = pos;
		mapPos = getMapPosition(pos);
	}
	
	public Block(Point mapPos){
		this.mapPos = mapPos;
		pos = new Point2D.Double(mapPos.x*Env.cellsize+Env.cellsize/2,
				mapPos.y*Env.cellsize+Env.cellsize/2);
	}
	@Override
	public void draw(Graphics2D gg) {
		Graphics2D g = (Graphics2D)gg.create();
		g.setColor(Color.red);
		g.fillOval((int)pos.x, (int)pos.y, size, size);
	}

	public Point getMapPosition(Point2D.Double pos){
		return new Point((int)pos.x/Env.cellsize,
				(int)pos.y/Env.cellsize);
 
	}
}
