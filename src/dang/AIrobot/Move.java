package dang.AIrobot;

import java.awt.Point;

public abstract class Move {
	Point mapPos;
	double direction;
	Move parent;
	
	public Point getMapPos() {
		return mapPos;
	}
	public void setMapPos(Point mapPos) {
		this.mapPos = mapPos;
	}
	public double getDirection() {
		return direction;
	}
	public void setDirection(double direction) {
		this.direction = direction;
	}
	public Move getParent() {
		return parent;
	}
	public void setParent(Move parent) {
		this.parent = parent;
	}
	
	
}
