package dang.environment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import dang.robot.Robot;

public class FlowerPot implements Obstacle {
	
	public static final int DIAMETER = 60;
	Ellipse2D.Double el;
	Point2D.Double center;
	boolean halo;

	public double getX() {
		return center.x;
	}

	public void setX(double x) {
		center.setLocation(x,center.y);
	}

	public double getY() {
		return center.y;
	}

	public void setY(double y) {
		center.setLocation(center.x, y);
	}
	
	public FlowerPot(){
		center = new Point2D.Double(0,0);
		el = new Ellipse2D.Double();
	}
	
	public FlowerPot(double x, double y){
		center = new Point2D.Double(x,y);
		el = new Ellipse2D.Double();
	}
	
	public Point2D.Double center(){
		return new Point2D.Double(center.x,center.y);
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setColor(Color.ORANGE);
		g2.fillOval((int)center.x -DIAMETER/2, (int)center.y-DIAMETER/2, DIAMETER, DIAMETER);
		//black border
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(0.1f));
		g2.drawOval((int)center.x-DIAMETER/2, (int)center.y-DIAMETER/2, DIAMETER, DIAMETER);
		if (halo){
			g2.setColor(Color.RED);
			g2.drawRect((int)center.x -DIAMETER/2-5, (int)center.y-DIAMETER/2-5, DIAMETER+10, DIAMETER+10);
		}
		g2.dispose();
	}

	@Override
	public boolean intersects(Robot robot) {
		el.setFrame(center.x -DIAMETER/2, center.y-DIAMETER/2, DIAMETER, DIAMETER);
		double distance = center().distance(robot.center());
		return distance < (getRadius()+robot.getRadius());
	}

	@Override
	public boolean intersects(Line2D line) {
		return pointToLineDistance(line.getP1(),line.getP2(), center())<getRadius();
	}
	

	public double pointToLineDistance(Point2D a, Point2D b, Point2D p)
	{
		double x = (b.getX()-a.getX());
		double y = (b.getY()-a.getY());
		double u = ((p.getX()-a.getX())*x+(p.getY()-a.getY())*y)/((x*x)+(y*y));
		double xq = a.getX()+ u*x;
		double yq = a.getY()+ u*y;
		if ((0<=u)&&(u<=1)){
			//the lines intersect, so distance is length from p to q
			return p.distance(xq, yq);
		}else{
			double a1 = p.distance(a);
			double a2 = p.distance(b);
			return a1 < a2 ? a1 : a2;
		}
	}
	
	public int getRadius(){
		return DIAMETER/2;
	}

	@Override
	public Point2D.Double getIntersect(Line2D.Double line) {
		// TODO Auto-generated method stub
		return Environment.lineCircleIntersect(line, center(), getRadius());
	}

	@Override
	public int getRGB() {
		// have to get RGB values for flower pots and walls and such
		return 0;
	}

	@Override
	public int getSize() {
		//I dunno, have to measuer. Guess for now
		return 20;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return Obstacle.FLOWERPOT;
	}

	@Override
	public void setHalo(boolean value) {
		halo = value;
		
	}

}
