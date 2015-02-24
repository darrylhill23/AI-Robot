package dang.robot;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import dang.environment.Block;
import dang.environment.Environment;
import dang.environment.Obstacle;
import dang.environment.Wall;

import dang.AIrobot.Debug;

public class Robot implements Runnable{
	
	Color GREEN = new Color(0,1.0f,0,0.50f);
	Color RED = new Color(1,0,0,0.50f);
	Color BLUE = new Color(0,0,1,0.5f);
	
	Color RobotColor = Color.BLACK;
	
	long startTime;
	
	int round = 0;
	int leftSpeed;
	int rightSpeed;
	double angle;
	double headAngle;			//head absolute angle, with respect to the world
	double headRelativeAngle;	//angle relative to the robot, set by the servos
	private int headPitch = 128;//the robot is hardwired. I will have to allow adjustment
								//of these values by the user
	public int headPitchCenter = 128;
	public int headPitchUpMax = 155;
	public int headPitchDownMax = 90;
	public int headPitchUpMaxDegrees = 40;
	public int headPitchDownMaxDegrees = 42;


	private int headYaw = 142;
	
	public int headYawCenter = 142;
	public int headYawLeftMax = 60;
	public int headYawRightMax = 231;
	
	/*
	 * LEFT_GRIPPER_MIN = 215
  	LEFT_GRIPPER_MID = 170
  	LEFT_GRIPPER_MAX = 140
  	RIGHT_GRIPPER_MIN = 104
  	RIGHT_GRIPPER_MID = 150
  	RIGHT_GRIPPER_MAX = 181
	 */
	
	public int leftGripOpen = 140;
	public int leftGripMid = 170;
	public int leftGripClosed = 215;
	
	private int leftGripValue = leftGripMid;
	
	public int rightGripOpen = 181;
	public int rightGripMid = 150;
	public int rightGripClosed = 104;
	private int rightGripValue = rightGripMid;
	
	/*
	 * The margin within which we still consider the grippers fully closed
	 * or fully open
	 */
	private int gripTolerance = 15;
	Block grippedBlock;

	public double GRIPPERLENGTH = 10;			//in cm
	public double GRIPPERMAXANGLE = 65;			//in degrees
	private Line2D.Double leftGripBaseline = new Line2D.Double();
	private Line2D.Double rightGripBaseline = new Line2D.Double();
	
	//specialized subclass of Line2D to draw the little hook at the end
	private Gripper leftGrip = new Gripper(Gripper.LEFT);
	private Gripper rightGrip = new Gripper(Gripper.RIGHT);
	
	private Gripper oldLeftGrip = new Gripper(Gripper.LEFT);
	private Gripper oldRightGrip = new Gripper(Gripper.RIGHT);
	
	public Color gripperColor = Color.BLACK;
	
	private Line2D.Double blockDetectBaseline = new Line2D.Double();
	private Line2D.Double blockDetect = new Line2D.Double();
	private double blockDetectDistance = 3;		//pixels
	private int blockBuffer = 0;
	private boolean bDetect;


	
	double x, y;				//location
	double length = 8.9;		//in cm. For simplicity we assume the robot is square
	//double width = 8.9;
	double wheelbase = 8.9;		//in cm
	int robotHeight = 20;		//in cm. Not to be confused with height below, though it is confusing
	int width = (int) (wheelbase *3);		//for drawing purposes, w and h in pixels
	int height = (int) (wheelbase *3);
	int id = 0;								//robot id (for tracking)
	boolean running = false;
	//Program program;						//copy of the program loaded onto the robot
	
	long time, delta;						//keep track of the time so we know the distance travelled each update
	double maxX, maxY, minX, minY;
	int collisions;							//I was going to track collisions, but I don't
	public int startX = 10, startY = 10;	//starting location, read in from config file
	public double startAngle = 0.0;			//starting angle, read in from config file
	//sensor base array is where the sensors are in relation to the robot
	//use this as a base value and the robot position to calculate where the sensors are
	public static final int SensorAngle = 20;
	Line2D.Double[] sensorBaseArray = new Line2D.Double[8];
	//sensor array is the adjusted position of the sensors
	Line2D.Double[] sensorArray = new Line2D.Double[8];
	public boolean IRSensors = true;
	public boolean showIRSensors = true;
	byte sensor = 0;
	
	
	/*
	 * Some camera stuff
	 */
	private Camera cam;
	public boolean camera = true;								//for the camera dipswitch
	public boolean showCamera = true;							//are we showing it on the tracker?
	private Color cameraColor = GREEN;							//color to draw in, if we see a block
																//turn red
	private Line2D.Double cameraBaseline = new Line2D.Double();
	private Line2D.Double cameraLeftSweep  = new Line2D.Double();
	private Line2D.Double cameraRightSweep = new Line2D.Double();
	
	/*
	 * For this sensor (and sonar), depending on the sensitivity setting, I will use
	 * a single line that I can sweep through the cone of the sensor model,
	 * (ie, for Dirrs I would sweep at different angles across 6 degrees and
	 * check for collisions). It isn't perfect, but it is good for the robot
	 * since its sensors are never 100% accurate anyway. None of these are
	 * constants or final, because I may allow the user to modify them.
	 */
	
	//dirrs stats
	private Line2D.Double dirrsBaseSensor = new Line2D.Double();
	private Line2D.Double dirrsSensor	  = new Line2D.Double();
	private Line2D.Double dirrsLeftSweep  = new Line2D.Double();
	private Line2D.Double dirrsRightSweep = new Line2D.Double();
	private int dirrsSpread = 6;			//in degrees
	private double dirrsDistance = 80;		//in cm
	private double dirrsOffset = -3.12;		//in cm, -ve means behing the center line
	private int dirrsError = 5; 			//as a percent
	public boolean dirrs = true;			//is the dirrs active (ie, the dipswitch is on)?
	public boolean showDirrs = true;		//do we draw the dirrs (ie, is it selected to draw
											//on the tracker menu)?
	public boolean drawDirrs = false;		//every time the user uses the dirrs, we draw it
											//it on the screen
	public Color dirrsColor = BLUE;			//if it detects something, it turns red
	
	//sonar stats
	private Line2D.Double sonarBaseSensor = new Line2D.Double();
	private Line2D.Double sonarSensor	  = new Line2D.Double();
	private Line2D.Double sonarLeftSweep  = new Line2D.Double();
	private Line2D.Double sonarRightSweep = new Line2D.Double();
	/*
	 * Not a cone, but a series of lines that sweeps through the area. Its coarse, but
	 * 100% accuracy is discouraged anyway. Granularity is the number of lines we sweep
	 * through the fan of the sonar
	 */
	private int sonarGranularity = 8;		
	private int sonarSpread = 38;			//in degrees
	private double sonarDistance = 300;		//in cm
	private double sonarOffset = 4.4;		//in cm 
	private int sonarError = 10;			//as a percent
	public boolean sonar = true;			//is the sonar active?
	public boolean showSonar = true;		//do we draw the dirrs (ie, is it selected to draw
	//on the tracker menu)?
	public boolean drawSonar = false;		//every time the user uses the dirrs, we draw it
	//it on the screen
	public Color sonarColor = BLUE;			//if it detects something, it turns red
	
	/**
	 * for these bad boys I may forgo all the integer to byte conversions. Seem
	 * arbitrarily pointless for a simulator. They are mainly deprecated anyway,
	 * but left around in case someone wants the pain of dealing with a more
	 * realistic scenario (that doesn't work very well)
	 */
	InputStream in;
	OutputStream out;
	/**
	 * Speed being a number between 0 and 1
	 */
	public double SPEED = 0.04;

	//may need another value here. I will be tracking pulses, but its 
	//unlikely I will use them in the formula
	double encode = 0.1684; //cm per pulse
	double leftEncoder = 0;
	double rightEncoder = 0;
	public boolean encoders = false;
	
	/****************************************
	 * kinematics formulas:
	 * 
	 * ricc = 8.9(wheelbase) * (pl/pr-pl) + 4.45(midpoint of wheelbase)
	 * ricc = 8.9 * (pl/pr-pl) + 4.45
	 * deltaangle = (0.01892 * (pr - pl)) radians
	 * deltay = y + ricc(cos (angle) - cos (angle + deltaangle))
	 * deltax = x + ricc(sin (angle + deltaangle) - sin (angle))
	 * 
	 * straight motion (pr = pl):
	 * deltax = x + encode * pr * cos(angle)
	 * deltay = y + encode * pr * sin(angle)
	 * 
	 * spinning (pr = -pl):
	 * angle = angle + deltaangle
	 * 
	 * @return
	 ******************************************/
	
	/*
	 * some drawing stuff here
	 */
	
	Rectangle rect;
	
	public Robot(){
		cam = new Camera(this);
		x = startX;
		y = startY;
		angle = startAngle;
		headAngle = startAngle;
		headRelativeAngle = 0;
		collisions = 0;
		rect = new Rectangle((int)getX()-width/2,(int)getY()-height/2,width, height);
		maxX= Environment.LENGTH*3 - Environment.WALLWIDTH-height/2;
		minX = Environment.WALLWIDTH + (height/2);
		maxY = Environment.WIDTH*3 - Environment.WALLWIDTH/2-height/2;
		minY = Environment.WALLWIDTH+height/2;
		Debug.debug("maxX: "+maxX+" minX: "+ minX);
		environment = new Environment();
		//double length = this.length *3;
		//double width = this.width *3;
		for (int i = 0; i<8; i++){
			sensorArray[i] = new Line2D.Double();
		}
		/*
		 * set up the base array, which is the positon of the sensors relative to the robot
		 */
		//rear sensor
		sensorBaseArray[0] = new Line2D.Double(-width/2, 0, -width/2-15, 0); 
		//front right using an angle of approx 15 degrees
		sensorBaseArray[1] = new Line2D.Double(width/2, height/4, width/2 + (Math.cos(Math.toRadians(SensorAngle))*30),
				height/4 +(Math.sin(Math.toRadians(SensorAngle))*30)); 
		 //middle front
		sensorBaseArray[2] = new Line2D.Double(width/2, 0, width/2+30,0);
		//front left at 15 degrees
		sensorBaseArray[3] = new Line2D.Double(width/2, -height/4, width/2 + (Math.cos(Math.toRadians(SensorAngle))*30),
				-height/4 -(Math.sin(Math.toRadians(SensorAngle))*30)); 
		//front left straight left
		sensorBaseArray[4] = new Line2D.Double(width/2 -6 , -height/2, width/2 -6, -height/2 - 15);
		//rear left straight left
		sensorBaseArray[5] = new Line2D.Double(-width/2 +6, -height/2, -width/2+6, -height/2 - 30);
		//rear right straight right
		sensorBaseArray[6] = new Line2D.Double(-width/2+6, height/2, -width/2+6, height/2 +30);
		//front right straight right
		sensorBaseArray[7] = new Line2D.Double(width/2-6 ,height/2, width/2-6 , height/2 +15);
		
		/*
		 * set up the dirrs base. *3 is to convert from cm to pixels,
		 * the offset value is the distance behind the robot center the
		 * sensor is
		 */
		dirrsBaseSensor.setLine(+dirrsOffset*3, 0, dirrsDistance*3, 0);
		sonarBaseSensor.setLine(+sonarOffset*3, 0, sonarDistance*3, 0);
		cameraBaseline.setLine(0,0,cam.getCameraDistance()*3,0);
		blockDetectBaseline.setLine(0, 0, wheelbase/2*3, 0);
		
		//set up a few values for the gripper baselines
		double x1 = width/2;
		double y1 = -Block.DIAMETER/2;
		double x2 = x1 + GRIPPERLENGTH;
		double y2 = -Block.DIAMETER/2;
		
		leftGripBaseline.setLine(x1, y1, x2, y2);
		
		x1 = width/2;
		y1 = Block.DIAMETER/2;
		x2 = x1 + GRIPPERLENGTH;
		y2 = Block.DIAMETER/2;
		
		rightGripBaseline.setLine(x1, y1, x2, y2);

		//update the sensors
		updateSensors();
		
	}
	
	public Robot(int x, int y, double angle){
		this();
		this.x = x;
		this.y = y;
		this.angle = angle;	
		
	}
	
	
	public void setSpeed(int speed){
		//Debug.debug("setting speed","speed");
		SPEED = ((double)speed)/1000;
		//Debug.debug("Speed: "+SPEED, "speed");
	}
	
	public int getSpeed(){
		return (int)(SPEED*1000);
	}
	
	public double getHeadRelativeAngle() {
		return headRelativeAngle;
	}

	public void setHeadRelativeAngle(double headRelativeAngle) {
		this.headRelativeAngle = headRelativeAngle;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}
	
	public double getRadius(){
		return wheelbase/2*3;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Point2D.Double center(){
		return new Point2D.Double(x,y);
	}

	public byte getSensor() {
		return sensor;
	}
	
	public void setSensor(byte sensorNumber){
		sensor = (byte) (sensor|(1<<sensorNumber));
	}
	
	public void resetSensor(){
		sensor =0;
	}
	
	public byte getSensor(byte sensorNumber) {
		//returning values based on the bit map. 1 byte, 8 bits, one for each sensor
		//checks whether each bit is set to 1 or 0
		Debug.debug("Sensor: "+sensorNumber+" value: "+((sensor&(1<<sensorNumber))>>sensorNumber));
		return (byte) ((sensor&(1<<sensorNumber))>>sensorNumber);
	}
	
	
	private Environment environment;
	
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDelta() {
		return delta;
	}

	public void setDelta(long delta) {
		this.delta = delta;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
		Debug.debug("************in set Running");

		if (!running){
			Debug.debug("************ending");
			setSpeeds(0,0);
		}
	}

	public double getHeadAngle(){
		return headAngle;
	}
	
	
	public void setHeadAngle(double headAngle){
		this.headAngle = headAngle;
	}
	
	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		
		//this makes sure that it is a positive
		//value between 0 and 2PI
		this.angle = (angle+ (2*Math.PI))%(2*Math.PI);
	}
	
	public void changeAngle(double angle){
		this.angle += angle;
		//this makes sure that it is a positive
		//value between 0 and 2PI
		this.angle = (this.angle+ (2*Math.PI))%(2*Math.PI);
		
	}

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
	
	public boolean blockDetect(){
		return blockDetect(blockDetectDistance);
	}
	
	/*
	public ArrayList<Block> blockDetect(){
		return blockDetect(blockDetectDistance);
	}*/
	
	/*
	 * This function is called by the SPIN program, but also used
	 * internally. Hence the parameter that is also a member variable,
	 * so that if needed we can redefine the detection distance
	 */
	public boolean blockDetect(double blockDetectDistance){
		//very similar to getCamera reading, we have two vectors (grippers)
		//and we want to test if there is a block between them
		Debug.debug("updating sensors","blockdetect");
		updateSensors();
		ArrayList<Block> blocks = getEnvironment().getBlocks();
		ArrayList<Block> detectedBlocks = new ArrayList<Block>();
		if (blocks.size() > 0){
			Debug.debug("blocks exist","blockdetect");
			Point2D.Double front = getRobotFront();
			for(Block block : blocks){
				if (block.center().distance(front)<block.getRadius()+blockDetectDistance){
					return true;
				}
			}
			

		}

		return false;
	}
	
	private Point2D.Double getRobotFront() {
		Point2D.Double robotFront = new Point2D.Double(width/2,0);
		rotatePoint(robotFront, getAngle());
		robotFront.setLocation(robotFront.x + getX(), robotFront.y + getY());
		return robotFront;
	}


	/**
	 * we guarantee headPitchCenter will be 0 degrees. Which
	 * means we fudge the numbers slightly if we have to.
	 * We call max head pitch up 40 degrees, and max head pitch
	 * down 42 degrees.
	 * @return For this return value, up is -ve degrees
	 */
	public double getHeadPitchAngle(){
		 
		double pitch = headPitchCenter - headPitch;
		double scale;
		if (pitch < 0){
			//scale of available values to degrees
			scale = (headPitchUpMax - headPitchCenter)/headPitchUpMaxDegrees;
			//change to degrees
			pitch *= scale;
		}else if (pitch > 0){
			scale = (headPitchCenter - headPitchDownMax)/headPitchDownMaxDegrees;
			pitch *=scale;
		}
		return pitch;
	}
	
	public double sonarDistanceCM(){
		if (!sonar) return 0;
		drawSonar = true;
		updateSensor(sonarSensor, sonarBaseSensor, getHeadAngle(), sonar);
		double distance;
		//going to make sonar a bit more fine grained by adding two more lines
		Line2D.Double right = new Line2D.Double();
		Line2D.Double left = new Line2D.Double();
		//double currentDistance;
		ArrayList<Line2D.Double> sensors = new ArrayList<Line2D.Double>();
		sonarRightSweep = sweepSensor(sonarBaseSensor,Math.toRadians(sonarSpread/2));
		right = sweepSensor(sonarBaseSensor,Math.toRadians(sonarSpread/4));
		sonarLeftSweep = sweepSensor(sonarBaseSensor,Math.toRadians(-sonarSpread/2));
		left = sweepSensor(sonarBaseSensor,Math.toRadians(-sonarSpread/4));
		sensors.add(sonarSensor);
		sensors.add(sonarRightSweep);
		sensors.add(sonarLeftSweep);
		sensors.add(left);
		sensors.add(right);
		
		distance = distanceCM(sensors);
		
		if (distance == 10000){
			sonarColor = GREEN;
			return 0;
		}
		sonarColor = RED;
		// if it is less than 10 cm (10 pixels) return 0;
		return distance/3;
	}
	
	/*
	 * Unlike the IRSensors which are always active, Dirrs only detects
	 * when it is called by the running program.
	 */
	public double dirrsDistanceCM(){
		if(!dirrs) return 0;
		drawDirrs = true;
		updateSensor(dirrsSensor,dirrsBaseSensor, getHeadAngle(), dirrs);
		double distance;
		//double currentDistance;
		ArrayList<Line2D.Double> sensors = new ArrayList<Line2D.Double>();
		sensors.add(dirrsSensor);
		dirrsRightSweep = sweepSensor(dirrsBaseSensor,Math.toRadians(dirrsSpread/2));
		sensors.add(dirrsRightSweep);
		dirrsLeftSweep = sweepSensor(dirrsBaseSensor,Math.toRadians(-dirrsSpread/2));
		sensors.add(dirrsLeftSweep);
		
		distance = distanceCM(sensors);
		if (distance == 10000){
			distance = 0;
			dirrsColor = BLUE;
			return 0;
		}
		dirrsColor = RED;
		// if it is less than 3 cm (10 pixels) return 0;
		//effective distance is listed at 10cm but in practice I found it was accurate
		//at a very close distance
		return distance > 3 ? distance/3 : 0;
		
	}
	
	private double distanceCM(ArrayList<Line2D.Double> sensors){
		double distance = 10000;
		double currentDistance;
		ArrayList<Obstacle> obstacles;
		for (Line2D.Double sensor: sensors){
			obstacles = environment.checkCollisions(sensor);
			if (obstacles.size()!=0){
				currentDistance = getMinDistance(obstacles, sensor);
				if (currentDistance < distance){
					distance = currentDistance;
				}
			}
		}
		return distance;
	}
	
	public double getMinDistance(ArrayList<Obstacle> obstacles, Line2D.Double line){
		Point2D.Double intersect; 
		double distance = 10000;
		double currentDistance;
		for (Obstacle obstacle : obstacles){
			intersect = obstacle.getIntersect(line);
			currentDistance = intersect.distance(line.getP1());
			if (currentDistance < distance){
				distance = currentDistance;
			}
		}
		return distance;
	}

	public int getLeftSpeed() {
		return leftSpeed;
	}
/**
 * we update the position every time we change wheel speed
 * to keep the position accurate
 * @param leftSpeed
 */
	public void setLeftSpeed(int leftSpeed) {
		updatePosition();
		if (leftSpeed > 40){
			this.leftSpeed = 40;
			return;
		}
		if (leftSpeed < -40){
			this.leftSpeed = -40;
			return;
		}
		this.leftSpeed = leftSpeed;
	}

	public int getRightSpeed() {
		return rightSpeed;
	}

	/**
	 * we update the position every time we change wheel speed
	 *
	 * to keep the position accurate
	 */
	public void setRightSpeed(int rightSpeed) {
		updatePosition();
		if (rightSpeed > 40){
			this.rightSpeed = 40;
			return;
		}
		if (rightSpeed < -40){
			this.rightSpeed = -40;
			return;
		}
		this.rightSpeed = rightSpeed;
	}
	
	
	public long time(){
		long temp = System.currentTimeMillis();
		delta = temp - time;
		time = temp;
		return delta;
	}
	
	
	
	public void run (){
		//TODO make the robot do something
	}
	

	
	
	public void setPosition(){
		x = startX;
		y = startY;
		angle = startAngle;
	}
	
	/**
	 * As in rotate the point around 0,0
	 * @param point
	 * @param angle
	 */
	public void rotatePoint(Point2D point, double angle){
		/*
		 * p'x = cos(theta) * (px-ox) - sin(theta) * (py-oy) + ox
		 * p'y = sin(theta) * (px-ox) + cos(theta) * (py-oy) + oy
		 */
	
		double x = Math.cos(angle)*point.getX() - Math.sin(angle)*point.getY();
		double y = Math.sin(angle)*point.getX() + Math.cos(angle) *point.getY();
		point.setLocation(x, y);
		//return new Point2D.Double(x, y);
	}
	
	private void updateSensors(){
		updateIrSensors();
		updateSensor(dirrsSensor,dirrsBaseSensor, getHeadAngle(), dirrs);
		updateSensor(sonarSensor, sonarBaseSensor, getHeadAngle(), sonar);
		updateSensor(blockDetectBaseline, blockDetect, getAngle(), true);
	}
	
	private void updateCamera(){
		cameraLeftSweep = sweepSensor(cameraBaseline, Math.toRadians(cam.getCameraSpreadAngle()/2));
		cameraRightSweep = sweepSensor(cameraBaseline, Math.toRadians(-cam.getCameraSpreadAngle()/2));
	}
	
	private void updateGrippers(){
		double leftAngle =(leftGripClosed - leftGripValue);
		leftAngle = (leftAngle/(leftGripClosed - leftGripOpen))*GRIPPERMAXANGLE;
		leftAngle = Math.toRadians(leftAngle);

		double rightAngle = (rightGripValue - rightGripClosed);
		rightAngle = (rightAngle/(rightGripOpen - rightGripClosed))*GRIPPERMAXANGLE;
		rightAngle = Math.toRadians(rightAngle);

		oldLeftGrip.setLine(leftGrip.x1,leftGrip.y1,leftGrip.x2,leftGrip.y2);	
		oldRightGrip.setLine(rightGrip.x1, rightGrip.y1, rightGrip.x2, rightGrip.y2);
		updateSensor(rightGrip, rightGripBaseline, getAngle(), true);
		updateSensor(leftGrip, leftGripBaseline, getAngle(), true);
		
		/* rotating a line around P1 */
		/* here is where it gets fun - watch the bouncing ball */
		
		Point2D lp2 = new Point2D.Double(leftGrip.x2, leftGrip.y2);
		Point2D lp1 = leftGrip.getP1();
		lp2.setLocation(lp2.getX()-lp1.getX(), lp2.getY() - lp1.getY());
		rotatePoint(lp2, -leftAngle);
		lp2.setLocation(lp2.getX()+lp1.getX(), lp2.getY() + lp1.getY());
		leftGrip.setLine(lp1, lp2);
		
		/* Lets see that again in super slo-mo */
		
		Point2D rp2 = new Point2D.Double(rightGrip.x2, rightGrip.y2);
		Point2D rp1 = rightGrip.getP1();
		rp2.setLocation(rp2.getX()-rp1.getX(), rp2.getY() - rp1.getY());
		rotatePoint(rp2, rightAngle);
		rp2.setLocation(rp2.getX()+rp1.getX(), rp2.getY() + rp1.getY());
		rightGrip.setLine(rp1, rp2);
		
	}
	
	/**
	 * for this to work properly I will likely have to rotate the base array
	 * (since it is with respect to the center of the robot), then add in the position 
	 * of the robot afterwards. 
	 * 
	 * This sets their position graphically. I needed to do it manually because they need
	 * to be in their rotated state for collision detection.
	 */
	private void updateIrSensors(){
		if (!IRSensors){
			return;
		}
		for (int i=0; i<8;i++){
			//rotate. We don't use the generic update sensor, because it is not dependant
			//on head angle.
			updateSensor(sensorArray[i],sensorBaseArray[i],getAngle(), true);
		}
	}
	
	/*
	 * This and sonar are done a little differently, since they sweep through their positions.
	 * Actually for now I will do the simplest implementation
	 * Everything drawn here is with respect to the robot position
	 */
	private void updateSensor(Line2D.Double sensor, Line2D.Double baseSensor,
			double angle, boolean checkSensor){
		//check if sensor is active
		if (!checkSensor) return;
		
		//rotate the line according to the angle
		Point2D.Double p1 = 
				new Point2D.Double(baseSensor.getP1().getX(), baseSensor.getP1().getY());
		rotatePoint(p1, angle);
		Point2D.Double p2 = 
				new Point2D.Double(baseSensor.getP2().getX(), baseSensor.getP2().getY());
		rotatePoint(p2, angle);
		sensor.setLine(p1,p2);
		sensor.setLine(sensor.getX1() + getX(), sensor.getY1() + getY(),
				sensor.getX2() + getX(), sensor.getY2() + getY());
		
	}
	
	//rotate a line through a specified angle relative to the baseSensor
	private Line2D.Double sweepSensor(Line2D.Double baseSensor,double angle){
		Line2D.Double sensor = new Line2D.Double();
		Point2D.Double p1 = 
				new Point2D.Double(baseSensor.getP1().getX(), baseSensor.getP1().getY());
		rotatePoint(p1, getHeadAngle()+angle);
		Point2D.Double p2 = 
				new Point2D.Double(baseSensor.getP2().getX(), baseSensor.getP2().getY());
		rotatePoint(p2, getHeadAngle()+angle);
		sensor.setLine(p1,p2);
		sensor.setLine(sensor.getX1() + getX(), sensor.getY1() + getY(),
				sensor.getX2() + getX(), sensor.getY2() + getY());
		
		return sensor;
	}
	
	
	public synchronized void updatePosition(){
		long time = time();
		
		//store the old values in case of collision
		Point2D.Double oldloc= center();
		
		double pl = getLeftSpeed() * time *encode*SPEED/3;
		double pr = getRightSpeed() * time *encode*SPEED/3;
		double deltaangle = (0.01892 * (pl-pr));
		
		if (encoders){
			if (pr<0){
				rightEncoder -= pr;
			}else{
				rightEncoder += pr;
			}
			if (pl<0){
				leftEncoder -= pl;
			}else{
				leftEncoder += pl;
			}
		}

		if (pr == pl){
			setX(getX() + encode * pr * Math.cos(angle)*3);
			setY(getY() + encode * pr * Math.sin(angle)*3);
		}else if (pr == -pl){
			setAngle(getAngle() + deltaangle);
		}else{
			double ricc = ((wheelbase * (pl/(pl-pr))) - ( wheelbase/2))*3;
			//Debug.debug("Ricc: "+ ricc, "ricc");
			double angle = getAngle()- Math.PI/2;
			setY(getY() + (ricc*((Math.cos (getAngle()) - Math.cos(getAngle() + deltaangle)))));
			setX(getX() + (ricc*((Math.sin(getAngle() + deltaangle) - Math.sin(getAngle())))));
			//setY(getY() + ricc*((Math.sin (angle + deltaangle) - Math.sin(angle))));
			//setX(getX() + ricc*((Math.cos (angle) - Math.cos(angle + deltaangle))));

			setAngle(getAngle() + deltaangle);
			Debug.debug("x: "+getX()+", y: "+getY()+", "+ angle);
		}
		//set the head angle to the new angle plus wherever the head is pointing
		setHeadAngle(getAngle() + getHeadRelativeAngle());
		
		//update grippers
		updateGrippers();
		
		//update sensors
		updateSensors();
		
		//update the camera
		updateCamera();
		
		//check for collisions
		checkCollisions(oldloc);
	}
	/**
	 * we update the position every time we change wheel speed
	 * to keep the position accurate
	 * @param leftSpeed
	 * @param rightSpeed
	 */
	public void setSpeeds(int leftSpeed, int rightSpeed){
		Debug.debug("ROBOT: setting wheel speeds: "+leftSpeed+", "+rightSpeed);
		//updatePosition();
		setLeftSpeed(leftSpeed);
		setRightSpeed(rightSpeed);
	}
	
	public void draw (Graphics g){
		//the x and y are the center points of the robot, but
		//the rect location refers to upper left corner
		rect.setLocation((int)getX()-width/2,(int)getY()-height/2);
		Graphics2D g2 = (Graphics2D)g.create();
		if(showSonar) Debug.debug("showsonar");
		if(drawSonar) Debug.debug("drawsonar");
		if (showCamera&&camera){
			//making sure if we don't take any readings that the color stays green
			//getCameraReading();
			Debug.debug("drawing camera");
			g2.setColor(cameraColor);
			Polygon cam = new Polygon();
			cam.addPoint((int)cameraLeftSweep.x1, (int)cameraLeftSweep.y1);
			cam.addPoint((int)cameraLeftSweep.x2, (int)cameraLeftSweep.y2);
			cam.addPoint((int)cameraRightSweep.x2, (int)cameraRightSweep.y2);
			g2.fill(cam);
		}
		if ((showSonar)&&(drawSonar)&&(sonar)){
			Debug.debug("drawing sonar");
			drawSonar = false;
			g2.setColor(sonarColor);
			Polygon sonarCone = new Polygon();
			sonarCone.addPoint((int)sonarSensor.getP1().getX(),(int)sonarSensor.getP1().getY());
			sonarCone.addPoint((int)sonarLeftSweep.getP2().getX(),(int)sonarLeftSweep.getP2().getY());
			sonarCone.addPoint((int)sonarRightSweep.getP2().getX(),(int)sonarRightSweep.getP2().getY());
			g2.fill(sonarCone);
		}
		if ((showDirrs)&&(drawDirrs)&&(dirrs)){
			drawDirrs = false;
			g2.setColor(dirrsColor);
			Polygon dirrsCone = new Polygon();
			dirrsCone.addPoint((int)dirrsSensor.getP1().getX(),(int)dirrsSensor.getP1().getY());
			dirrsCone.addPoint((int)dirrsLeftSweep.getP2().getX(),(int)dirrsLeftSweep.getP2().getY());
			dirrsCone.addPoint((int)dirrsRightSweep.getP2().getX(),(int)dirrsRightSweep.getP2().getY());
			g2.fill(dirrsCone);
		}
		/*
		 * Draw the irsensors first, then rotate and draw the robot.
		 * The sensors should already be rotated. fingers crossed.
		 * The colors are partially transparent, which is why they are new rather
		 * than premade.
		 */
		if((showIRSensors)&&(IRSensors)){
			
			for (int i = 0; i < 8; i ++){
				if (getSensor((byte) i) == 1){
					g2.setColor(RED);
				}else{
					g2.setColor(GREEN);
				}

				g2.draw(sensorArray[i]);
			}
		}
		
		/* **************draw grippers ******************************/
		Graphics2D g3 = (Graphics2D) g.create();
		g3.setColor(gripperColor);
		g3.setStroke(new BasicStroke(2.5f));
		leftGrip.draw(g3);
		rightGrip.draw(g3);
		
        /* **************set color, rotate, draw robot ***********************/
        g2.setColor(RobotColor);
        g2.rotate(getAngle(), getX(), getY());
        g2.fill(rect);
        //we are done, throw it out
        g2.dispose();
	}
	
	public void checkCollisions(Point2D.Double oldloc){
		
		/*
		 * Check the boundaries of the environment.
		 * This has become redundant since the addition of the
		 * wall collision system.
		 * TODO remove redundancy
		 */
		if (getX() > maxX){
			setX(maxX);
			collisions ++;
		}else if (getX()< minX){
			setX(minX);
		}
		if (getY() > maxY){
			setY(maxY);
		}else if (getY()< minY){
			setY(minY);
		}
		/*
		 * Check the robot collisions. 
		 */
		ArrayList<Obstacle> collisions = environment.checkCollisions(this);
		round ++;
		if (collisions.size()!=0){
			for (Obstacle obst : collisions){
				/*
				 * what i need is the point on the line between where the robot is and where it
				 * was that is at  of obstacle diameter + robot diameter.
				 * Who knows if this works. Essentially this should move it out of an
				 * obstacle back in the direction from whence it came.
				 */
				System.out.println("Obstacle "+obst+ " round: "+round);
				double distanceNeeded;
				if (obst.getType()==Obstacle.FLOWERPOT){
					distanceNeeded = getRadius() + obst.getRadius();
					Line2D.Double robotPath = new Line2D.Double(oldloc, new Point2D.Double(getX(),getY()));
					Point2D.Double collisionLocation = 
							Environment.lineCircleIntersect(robotPath, obst.center(), distanceNeeded);
					if (collisionLocation != null){
						setX(collisionLocation.getX());
						setY(collisionLocation.getY());
					}
					
					/* 
					 * Going to try this. Translate the block center using the 
					 * robot as (0,0). Switch to polar coordinates, set the proper
					 * distance, then switch back, and translate back. It might move 
					 * the block through a wall, but I will worry about that later.
					 */
				}else if (obst.getType() == Obstacle.BLOCK){
					distanceNeeded = getRadius() + obst.getRadius();
					Point2D.Double vec = new Point2D.Double(obst.getX() - center().x, 
							obst.getY() - center().y);
					
					//Because things don't always run perfectly.
					//This should never happen, but because of, I am
					//assuming, rounding errors and timing issues,
					//it does. ie, if a collision was detected, we should
					//always be within the distanceNeeded. But sometimes
					//we aren't. The result if this check isn't done is that
					//the robot will drag the blocks behind it
					
					if (vec.distance(0, 0)<distanceNeeded){
						double angle = Math.atan2(vec.x, vec.y);
						vec.x = distanceNeeded * Math.sin(angle);
						vec.y = distanceNeeded * Math.cos(angle);
						obst.setX((int)(vec.x + center().x));
						obst.setY((int)(vec.y + center().y));
					}
				}else if(obst.getType() == Obstacle.WALL){
					//the goal here is to find which side of the line the robot
					//is on, and assume, for simplicity, that that is the direction
					//we approached from. Find the normal in that direction and move the
					//robot back to the appropriate distance. Easy peasy
					Wall wall = (Wall)obst;
					Point2D.Double point = Environment.closestPoint(wall.getP1(), wall.getP2(), center());
					if (wall.pointIsOnLine(point)){
						//get the angle, which is the atan if the closest point is regarded as the origin
						Point2D.Double p2 = new Point2D.Double(center().x - point.x, center().y - point.y);
						distanceNeeded = getRadius();//+Environment.WALLWIDTH;
						double distanceToMove = distanceNeeded - center().distance(point);
						if (distanceToMove >0){
							double angle = Math.atan2(p2.y, p2.x);
							setX(getX()+Math.cos(angle)*distanceToMove);
							setY(getY()+Math.sin(angle)*distanceToMove);
						}
					}
				}

			}
			
		}
		updateSensors();
		updateGrippers();
		checkGripCollisions();
		
		/*
		 * Checking the IrSensors
		 */
		resetSensor();
		for (int i=0;i<8;i++){
			if (environment.checkCollisions(sensorArray[i]).size() != 0){
				setSensor((byte)i);
			}
		}
	}
	
	private boolean isRightTurn(Line2D.Double line, Point2D.Double point){
		/*
		 * 
		 * (line.p2.x-line.p1.x)(point.y-line.p1.y)-(line.p2.y-line.p1.y)(point.x-line.p1.x)
		t1 = (pix-sx)(pi+1y-sy)-(piy-sy)(pi+1x-sx)
		t2 = (pix-sx)(pi-1y-sy)-(piy-sy)(pi-1x-sx)
		IF ((t1 < 0) AND (t2 < 0)) THEN pL = pi
		IF ((t1 > 0) AND (t2 > 0)) THEN pR = pi

		 */
		
		Point2D.Double p1 = new Point2D.Double();
		p1.setLocation(line.getP1());
		Point2D.Double p2 = new Point2D.Double();
		p1.setLocation(line.getP2());
		
		double t1 = (p2.x - p1.x)*(point.y-p1.y)-(p2.y-p1.y)*(point.x-p1.x);
		//doesn't even matter if I got this backwards, as long as they both come up the same
		return (t1 < 0);
	}
	
	private void gripCollided(Block block, Gripper grip, Gripper oldGrip){
		//integrating the hook code. Its uglier and a harder read, but efficiency 
		//is becoming a bit of a factor. Hook refers to the the curved part
		//at the end of the gripper (simply a 90 degree angle in the simulator)
		
		/* ****************************************/
		/* ***Hook Code ***************************/
		Point2D.Double hook = grip.getHook();
		//if the block is colliding with the hook
		double distance = hook.distance(block.center());
		/* ***End Hook Code ***************************/
		/* ****************************************/

		
		int distanceNeeded;
		Point2D.Double point = Environment.closestPoint(grip.getP1(), grip.getP2(), block.center());
		boolean pointIsOnLine = grip.pointIsOnLine(point);
		
		
		/* ****************************************/
		/* ***Hook Code ***************************/
		if (distance<block.getRadius()){
			double angle;
			double minDistance;
			if (pointIsOnLine){
				angle = Math.atan2(grip.y1-grip.y2, grip.x1 - grip.x2);
				/*
				 * cheating a bit, but if the block is on the gripper, we know 
				 * that it will wind up lined up on the gripper. So we know that
				 * to put the block behind the hook will be 45 degrees, or PI/4
				 */
				minDistance = (block.getRadius()-4)*Math.cos(Math.PI/4);
			}else{
				angle = Math.atan2(block.center().x-hook.x, block.center().y-hook.y);
				minDistance = block.getRadius();
			}
			block.setX(block.getX()+Math.cos(angle)*minDistance);
			block.setY(block.getY()+Math.sin(angle)*minDistance);
		}
		/* ***End Hook Code ***************************/
		/* ****************************************/

		
		if (pointIsOnLine){
			//if the old grip location and the new grip location are both right turns to the block, 
			//then the block is on the correct side. If they are opposite (one left and one right)
			//that means the gripper has passed over it. To keep it on the correct side we reverse
			//the direction it has to travel. This isn't foolproof but gives a bit more
			//robustness
			Point2D.Double p2;
			double distanceToMove;
			//make sure the gripper hasn't overshot, due to lag or whatever
			if (isRightTurn(grip,block.center())==isRightTurn(oldGrip,block.center())){
				//p2 is strictly to find angle
				p2 = new Point2D.Double(block.center().x - point.x, block.center().y - point.y);
				distanceNeeded = block.getRadius()+blockBuffer;//+Environment.WALLWIDTH;
				distanceToMove = distanceNeeded - block.center().distance(point);
			}else{
				//turns are opposite, so the gripper has passed over the center of the block
				//Debug.debug("***block has moved through the gripper***","grip");
				p2 = new Point2D.Double(point.x- block.center().x, point.y-block.center().y);
				distanceToMove = block.getRadius()+blockBuffer + block.center().distance(point);
			}
			if (distanceToMove >0){
				//Debug.debug("moving block from grip","grip");
				double angle = Math.atan2(p2.y, p2.x);
				block.setX(block.getX()+(Math.cos(angle)*distanceToMove));
				block.setY(block.getY()+(Math.sin(angle)*distanceToMove));
			}
		}else{
			//Debug.debug("*******point is not on grip********","grip");
		}
	}
	
	public void checkHook(Block block, Gripper grip){
		Point2D.Double hook = grip.getHook();
		//if the block is colliding with the hook
		double distance = hook.distance(block.center());
		
		if (distance<block.getRadius()){
			double angle = Math.atan2(grip.y2-grip.y1, grip.x2 - grip.x1);
			double minDistance = block.getRadius();
			block.setX(block.getX()+Math.cos(angle)*minDistance);
			block.setY(block.getY()+Math.sin(angle)*minDistance);
		}
	}
	
	public void checkGripCollisions(){
		//have to play with these values
		//ArrayList<Block> blocks = blockDetect(7);
		ArrayList<Block> blocks = getEnvironment().getBlocks();
		if (blocks.size()>0){
			//Debug.debug("detectedblock","grip");

			/*
			 * Make sure they are riding the front of the grippers. Determine
			 * which gripper they are behind, if they are behind, then have it ride the
			 * normal until it is either at a proper distance, off the gripper,
			 * or locked in the pocket (or perhaps onto the other gripper if it
			 * is turning).
			 */
			for (Block block : blocks){
				gripCollided(block,leftGrip,oldLeftGrip);
				gripCollided(block,rightGrip, oldRightGrip);
			}
		}

	}
	
	
	
	@Override
	public String toString(){
		return "I, robot...";
	}

	public void setRightGripper(int value) {
		if (value > rightGripOpen) rightGripValue = rightGripOpen;
		else if (value < rightGripClosed) rightGripValue = rightGripClosed;
		else rightGripValue = value;		
	}
	
	public void setLeftGripper(int value){
		if (value < leftGripOpen) leftGripValue = leftGripOpen;
		else if (value > leftGripClosed) leftGripValue = leftGripClosed;
		else leftGripValue = value;
	}
	
	public int getHeadPitch() {
		return headPitch;
	}

	public int getHeadYaw() {
		return headYaw;
	}
	
	public void setHeadYaw(int value){
		
		if (value < headYawLeftMax) headYaw = headYawLeftMax;
		else if (value > headYawRightMax) headYaw = headYawRightMax;
		else headYaw = value;
		double val, val2;
		//convert it to the relative head angle
		if (headYaw == headYawCenter){
			setHeadRelativeAngle(0);
		}
		
		if (headYaw < headYawCenter){
			val = headYawCenter - headYawLeftMax;
			val2 = headYawCenter - value;
			//expressed as a percentage of 90 degrees in radians (PI/2)
			setHeadRelativeAngle(-(val2/val)*Math.PI/2);
		}
		if (headYaw > headYawCenter){
			val = headYawCenter - headYawRightMax;
			val2 = headYawCenter - value;
			setHeadRelativeAngle((val2/val)*Math.PI/2);
		}
	}
	
	public void setHeadPitch(int value){
		
		if (value > headPitchUpMax) headPitch = headPitchUpMax;
		else if (value < headPitchDownMax) headPitch = headPitchDownMax;
		else {
			headPitch = value;
		}
	}

	public void setTrackColor(int red, int green, int blue, int sensitivity) {
		//not used at the moment
	}
	
	private double phaseShift(double angle){
		return 360 - angle;
	}
	
	public void startEncoders(){
		encoders = true;
	}

	public int getLeftEncoderCount() {
		return (int)leftEncoder;
	}
	
	public int getRightEncoderCount() {
		return (int)rightEncoder;
	}

	public int resetCounters() {
		leftEncoder = 0;
		rightEncoder = 0;
		//value for 'true' in spin
		return -1;
	}
	
	public long getCnt(){
		
		//returns a 32 bit unsigned value equal to the current time the robot has
		//been running in milliseconds * 5000. A rough SPIN equivalent
		return ((System.currentTimeMillis() - startTime)%(long)Math.pow(2, 32));
	}

	

	

}
