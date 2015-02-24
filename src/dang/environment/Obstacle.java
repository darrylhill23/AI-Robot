package dang.environment;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import dang.robot.Robot;


public interface Obstacle {
	
	public static final int WALL = -1;
	public static final int FLOWERPOT = -2;
	public static final int BLOCK = -3;

	
	public void draw(Graphics2D g);
	public boolean intersects(Robot robot);
	public boolean intersects(Line2D line);
	public Point2D.Double center();
	public int getRadius();
	public int getType();
	public double getX();
	public double getY();
	public void setY(double y);
	public void setX(double x);
	
	/**
	 * Sets a red square or circle or otherwise highlights the obstacle on the next
	 * draw iteration only (ie, one frame then it shuts itself off).
	 */
	public void setHalo(boolean value);
	/**
	 * returns RBG value, of the format RRGGBB, where each is a value between
	 * 0-255. So blue is mod 16776960, green is mod 16711680 right shifted
	 * 8 times, and red is the number right shifted 16 times. Formulas are to get
	 * them are:
	 * 
	 *  blue = value % 16776960;
	 *  green = value % 16711680 >> 8;
	 *  red = value >> 16;
	 *  
	 *  To make the number:
	 *  value = (red << 16) + (green << 8) + blue;
	 *  
	 *  Maybe I have been doing too much C/C++
	 *  
	 * @return
	 */
	public int getRGB();	
	
	/**
	 * 
	 * @return the approximate diameter from the camera perspective (ie horizontal), which will
	 * be used with the RGB values to determine what the camera sees. In cm
	 */
	public int getSize();
	
	/**
	 * For a line with multiple intersections, it should return the intersection
	 * closest to line.P1 
	 * @param line
	 * @return the intersection point
	 */
	public Point2D.Double getIntersect(Line2D.Double line);
}
