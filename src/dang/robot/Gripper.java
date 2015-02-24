package dang.robot;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


public class Gripper extends Line2D.Double {
	public static final int LEFT = -1;
	public static final int RIGHT = -2;
	public static final double SCALE = 0.5;
	public static final double TOLERANCE = 5;
	/**
	 * Whether this is a left or right gripper
	 */
	private int direction;
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public Gripper(int direction){
		super();
		this.direction = direction;
	}
	
	public Point2D.Double getHook(){
		double x3,y3;
		if (direction == RIGHT){
			x3 =  (y2 - y1)*SCALE;
			y3 = -(x2 - x1)*SCALE;
		}else{
			x3 = -(y2 - y1)*SCALE;
			y3 =  (x2 - x1)*SCALE;
		}
		return new Point2D.Double(x2+x3, y2+y3);
	}
	
	public void draw(Graphics2D g2){
		g2.draw(this);
		Line2D.Double hook = new Line2D.Double();
		double x3,y3;
		if (direction == RIGHT){
			x3 =  (y2 - y1)*SCALE;
			y3 = -(x2 - x1)*SCALE;
			hook.setLine(x2, y2, x2+x3, y2+y3);
			g2.draw(hook);
		}else{
			x3 = -(y2 - y1)*SCALE;
			y3 =  (x2 - x1)*SCALE;
			hook.setLine(x2, y2, x2+x3, y2+y3);
			g2.draw(hook);
		}
	}
	
	/**
	 * 
	 * @return return the normal to the gripper, as an angle in radians.
	 */
	public double getNormal(){
		double x,y;
		x = getX2() - getX1();
		y = getY2() - getY1();
		double angle = Math.atan2(y, x);
		if (direction == RIGHT){
			angle += 3*Math.PI/2;
			angle = angle%(Math.PI*2);
		}else{
			angle += (Math.PI/2);
			angle = angle%(Math.PI*2);
		}
		return angle;
	}
	
	public boolean pointIsOnLine(Point2D.Double point){
		if (x2>x1){
			if (point.x>x2)return false;
			if (point.x<x1)return false;
		}else{
			if (point.x>x1)return false;
			if (point.x<x2)return false;
		}
		if (y2>y1){
			if (point.y>y2)return false;
			if (point.y<y1)return false;
		}else{
			if (point.y>y1)return false;
			if (point.y<y2)return false;
		}
		return true;
	}

}
