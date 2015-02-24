package dang.environment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import dang.robot.Robot;

public class Wall extends Line2D.Double implements Obstacle{
	
	int size = 170; 	//cm, approx 5 feet
	boolean halo;
	
	public Wall (double x1, double y1, double x2, double y2){
		super(x1,y1,x2,y2);
	}


	@Override
	public void draw(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(Environment.WALLWIDTH));
		g2.draw(this);
		if (halo){
			 g2.setStroke(new BasicStroke());
			 g2.setColor(Color.RED);
			 Rectangle rect = getBounds();
			 //expand the boundaries slightly
			 int x = 10, y = 10;
			 if(x1<x2){
				 x = -x;
			 }
			 if(y1<y2){
				 y = -y;
			 }
			 rect.add(x1+x, y1+y);
			 rect.add(x2-x, y2-y);
			 g2.draw(rect);
			
		}
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

	@Override
	public boolean intersects(Line2D line) {
		return Line2D.Double.linesIntersect(line.getX1(),line.getY1(), line.getX2(), line.getY2(),
				this.getX1(),this.getY1(), this.getX2(), this.getY2());
	}

	@Override
	public Point2D.Double center() {
		return new Point2D.Double((int)(x2 - (x2-x1)/2),(int)(y2 - (y2-y1)/2));
	}
	
	public int getRadius(){
		return 0;
	}
	
	public double getX(){
		return 0;
	}
	
	public double getY(){
		return 0;
	}

	public void setX(double x){
		
	}
	
	public void setY(double y){
		
	}
	@Override
	public boolean intersects(Robot robot) {
		return Environment.lineCircleIntersect(this, robot.center(),
				robot.getRadius())!= null;
	}

	@Override
	public Point2D.Double getIntersect(Line2D.Double line) {
		return intersection(this, line);
	}
	
	  /**
	   * Computes the intersection between two lines. The calculated point is approximate, 
	   * since integers are used. If you need a more precise result, use doubles
	   * everywhere. 
	   * (c) 2007 Alexander Hristov. Use Freely (LGPL license). http://www.ahristov.com
	   * modified from Point to Point2D.Double
	   *
	   * @param x1 Point 1 of Line 1
	   * @param y1 Point 1 of Line 1
	   * @param x2 Point 2 of Line 1
	   * @param y2 Point 2 of Line 1
	   * @param x3 Point 1 of Line 2
	   * @param y3 Point 1 of Line 2
	   * @param x4 Point 2 of Line 2
	   * @param y4 Point 2 of Line 2
	   * @return Point where the segments intersect, or null if they don't
	   */
	  public Point2D.Double intersection(Line2D l1, Line2D l2)
	    
	   {
		double x1 = l1.getX1();
		double y1 = l1.getY1();
		double x2 = l1.getX2();
		double y2 = l1.getY2(); 
		double x3 = l2.getX1();
		double y3 = l2.getY1();
		double x4 = l2.getX2();
		double y4 = l2.getY2();
	    double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
	    if (d == 0) return null;
	    
	    double xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
	    double yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
	    
	    return new Point2D.Double(xi,yi);
	  }

	@Override
	public int getRGB() {
		// again need to get a lab reading. For now, it is kind of white-ish
		return (220 << 16) + (220 << 8) + 220;
	}

	@Override
	public int getSize() {
		// ha ha, well pretty big. call it 5 feet average, but specific walls may need
		// to 
		return size;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return Obstacle.WALL;
	}

	@Override
	public void setHalo(boolean value) {
		halo = value;
		
	}
}
