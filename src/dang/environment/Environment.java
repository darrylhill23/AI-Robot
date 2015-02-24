package dang.environment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import dang.AIrobot.Debug;
import dang.robot.Robot;

/**
 * Units in cm. Some cases they are in pixels
 * @author darrylhill
 *
 */
public class Environment {
		
	public static final double WIDTH = 152.4; //5 feet in cm
	public static final double LENGTH = 213.36; //7 feet in cm
	public static final float WALLWIDTH = 10f;
	
	
	Wall north, south, east, west;
	ArrayList<Obstacle> allShapes = new ArrayList<Obstacle>();

	public Environment(){
		setDefaultObstacles();
	}
	
	public ArrayList<Block> getBlocks(){
		ArrayList<Block> blocks = new ArrayList<Block>();
		for (Obstacle ob : allShapes){
			if (ob.getType() == Obstacle.BLOCK){
				blocks.add((Block)ob);
			}
		}
		return blocks;
	}
	
	public ArrayList<Obstacle> getObstacles(){
		return allShapes;
	}
	
	public void addObstacle(Obstacle obstacle){
		allShapes.add(obstacle);
	}
	
	public boolean removeObstacle(Obstacle obstacle){
		return allShapes.remove(obstacle);
	}
	
	public void draw(Graphics g){
		Graphics2D g2 = (Graphics2D)g.create();
		for (Obstacle obstacle : allShapes){
			obstacle.draw(g2);
		}
		g2.dispose();
	}
	
	public ArrayList<Obstacle> checkCollisions(Robot robot){
		ArrayList<Obstacle> collisions = new ArrayList<Obstacle>();
		for (Obstacle obstacle: allShapes){
			if (obstacle.intersects(robot)){
				//if (obstacle.getType() != Obstacle.WALL){
					//we are temporarily not implementing the rest
					collisions.add(obstacle);
				//}
			}
		}
		return collisions;
	}
	
	public Block detectBlock(Point2D point2d, double radius){
		for (Obstacle obstacle: allShapes){
			if (obstacle.getType() == Obstacle.BLOCK){
				double distance = point2d.distance(obstacle.center());
				if (distance > (radius+obstacle.getRadius())){
					return (Block) obstacle;
				}
			}
		}
		return null;
	}

	public ArrayList<Obstacle> checkCollisions(Line2D.Double sensor) {
		ArrayList<Obstacle> collisions = new ArrayList<Obstacle>();
		for (Obstacle obstacle: allShapes){
			//we don't count collisions with blocks here. They are a special case
			if ((obstacle.intersects(sensor))&&(obstacle.getType() != Obstacle.BLOCK)){
				collisions.add(obstacle);
			}
		}
		return collisions;
	}
	
	public static Point2D.Double lineCircleIntersect(Line2D.Double line, Point2D.Double centerPoint,
			double radius){
		return lineCircleIntersect(line,centerPoint,radius,false);
	}
	
	/**
	 * Returns the intersect point closest to line.p1
	 * @param line
	 * @param centerPoint
	 * @param distanceNeeded
	 * @return
	 */
	public static Point2D.Double lineCircleIntersect(Line2D.Double line, Point2D.Double centerPoint,
			double radius, boolean debug){
		
		Point2D.Double closestPoint = closestPoint(line.getP1(), line.getP2(),
				centerPoint);
		if(debug){
			Debug.debug("closest point: "+closestPoint.distance(centerPoint),"grip");
			Debug.debug("radius: "+radius,"grip");
		}

		if (closestPoint.distance(centerPoint)>radius){
			//the closest point on the line is greater than the radius, so there
			//is no intersection
			return null;
		}
		
		
		double dn = radius*radius;
		double ld = Math.pow(centerPoint.distance(closestPoint),2);
		double distance;
		if (ld>dn){
			distance = -1.0;
		}else{
			//System.out.println("obstacle distance to line: "+Math.pow(centerPoint.distance(closestPoint),2));
			distance = Math.sqrt(radius*radius - 
					Math.pow(centerPoint.distance(closestPoint),2));
		}
		System.out.println("Distance: "+distance);
		double angle = Math.atan2(closestPoint.getY()-line.getP1().getY(), 
				closestPoint.getX()-line.getP2().getX());
		double x = Math.cos(angle)*distance;
		double y = Math.sin(angle)*distance;
		System.out.println("xdif: "+x+" ydif: "+y);
		//System.out.println("current x: "+getX()+" Y: "+getY());
		Point2D.Double intersect = new Point2D.Double(closestPoint.getX()-x,closestPoint.getY()-y);
		return intersect;
	}
	
	/**
	 * gives us the closest point on the line, if the line is assumed to go forever.
	 * @param a
	 * @param b
	 * @param p
	 * @return
	 */
	public static Point2D.Double closestPoint(Point2D a, Point2D b, Point2D p)
	{
		double x = (b.getX()-a.getX());
		double y = (b.getY()-a.getY());
		if ((x==0)&&(y==0))
			return (Double) a;
		double u = ((p.getX()-a.getX())*x+(p.getY()-a.getY())*y)/((x*x)+(y*y));
		double xq = a.getX()+ u*x;
		double yq = a.getY()+ u*y;

		return new Point2D.Double(xq, yq);
		
	}

	public void resetObstacles() {
		allShapes = new ArrayList<Obstacle>();
		
	}
	
	public void setDefaultObstacles(){
		allShapes = new ArrayList<Obstacle>();
		double length = LENGTH*3;
		double width = WIDTH *3;
		int wallwidth = (int)(WALLWIDTH/2);
		north = new Wall(0,wallwidth,length,wallwidth);
		west = new Wall(wallwidth,0,wallwidth,width);
		east = new Wall(length-wallwidth,0,length-wallwidth,width);
		south = new Wall(0,width,length,width);
		allShapes.add(north);
		allShapes.add(south);
		allShapes.add(west);
		allShapes.add(east);
		
	}
	
}
