package dang.AIrobot;

import java.awt.Point;
import java.awt.geom.Point2D;

public class Goal {
	
	Point mapPos;
	Point2D.Double pos;
	Block block;

	public Goal(Point2D.Double pos){
		this.pos = pos;
		mapPos = getMapPosition(pos);
	}
	
	public Goal(Block block){
		this.mapPos = block.mapPos;
		this.pos =    block.pos;
		this.block =  block;
	}
	
	
	public double mapx(){
		return mapPos.x;
	}
	
	public double mapy(){
		return mapPos.y;
	}
	
	public Point getMapPosition(Point2D.Double pos){
		return new Point((int)pos.x/Env.cellsize,
				(int)pos.y/Env.cellsize);
 
	}
	
	public boolean equals(Object goal){
		return ((Goal)(goal)).mapPos.equals(mapPos);
	}
}
