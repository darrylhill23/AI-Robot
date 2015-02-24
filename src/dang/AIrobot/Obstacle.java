package dang.AIrobot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class Obstacle implements PanelObject {

	Point mapPos;
	
	public Obstacle(Point mapPos){
		this.mapPos = mapPos;
	}
	
	@Override
	public void draw(Graphics2D gg) {
		Graphics2D g = (Graphics2D) gg.create();
		g.setColor(Color.BLACK);
		g.fillRect(mapPos.x*Env.cellsize, mapPos.y*Env.cellsize,
				Env.cellsize, Env.cellsize);

	}

}
