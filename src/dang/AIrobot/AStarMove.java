package dang.AIrobot;

import java.awt.Point;
import java.awt.geom.Point2D;

public class AStarMove extends Move{
	//Point mapPos;
	//double direction;
	double estimate;
	double cost;
	
	boolean explored;
	//AStarMove parent;
	
	public AStarMove(Point mapPos, double cost, AStarMove parent, double direction){
		this.mapPos = mapPos;
		this.cost = cost;
		this.parent = parent;
		this.direction = direction;
		estimate = 0;
	}
	
	public AStarMove(int x, int y, double cost, AStarMove parent, double dir){
		this(new Point(x,y), cost, parent, dir);
	}
	
	public double getTotalCost(){
		return cost + estimate;
	}

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

	public double getEstimate() {
		return estimate;
	}

	public void setEstimate(double estimate) {
		this.estimate = estimate;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean isExplored() {
		return explored;
	}

	public void setExplored(boolean explored) {
		this.explored = explored;
	}

	public Move getParent() {
		return parent;
	}

	public void setParent(AStarMove parent) {
		this.parent = parent;
	}
	
	
}
