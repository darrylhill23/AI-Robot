package dang.environment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import dang.AIrobot.Debug;
import dang.robot.Robot;


public class Block extends FlowerPot {
	public static final int DIAMETER = 10;
	double x,y;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Block(){}
	
	public Block(double x, double y){
		this.x=x;
		this.y=y;
	}
	@Override
	public void draw(Graphics2D g) {
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setColor(Color.RED);
		g2.fillOval((int)x-DIAMETER/2,(int) y-DIAMETER/2, DIAMETER, DIAMETER);
		//black border
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(0.1f));
		g2.drawOval((int)x-DIAMETER/2, (int)y-DIAMETER/2, DIAMETER, DIAMETER);
		if (halo){
			halo = false;
			g2.setColor(Color.RED);
			g2.drawRect((int)x -DIAMETER/2-2, (int)y-DIAMETER/2-2, DIAMETER+4, DIAMETER+4);
		}
		g2.dispose();
	}

	
	@Override
	public boolean intersects(Robot robot) {
		double distance = center().distance(robot.center());
		return distance < (getRadius()+robot.getRadius());
	}
/*
	@Override
	public boolean intersects(Line2D line) {
		// TODO Auto-generated method stub
		return false;
	}
*/
	@Override
	public Point2D.Double center() {
		return new Point2D.Double(x,y);
	}
	
	public int getRadius(){
		//Debug.debug("getting radius","bc");
		return DIAMETER/2;
	}

	/*
	@Override
	public Point2D.Double getIntersect(Line2D.Double line) {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public int getRGB() {
		return (216 << 16) + (16 << 8) + 16;
	}

	@Override
	public int getSize() {
		return 3;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return Obstacle.BLOCK;
	}

}
