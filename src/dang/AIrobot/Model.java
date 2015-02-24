package dang.AIrobot;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Model {

	ArrayList<Robot> robots;
	MainWindow window;
	MainController control;
	ArrayList<PanelObject> objects;
	ArrayList<Goal> goals;
	Map map;
	int numRobots, numBlocks;
	int mapcounter = 0;
	int goalCounter = 0;
	
	



	public Model(MainWindow window, MainController control, ArrayList<PanelObject> objects, int newRobots, int newBlocks){
		this.window = window;
		this.control = control;
		this.objects = objects;
		numRobots = newRobots;
		numBlocks = newBlocks;
		goals = new ArrayList<Goal>();
		createRobots();
		Point2D.Double p = new Point2D.Double(400,200);

		//load up the map
		Point point;
		Block block;

		for (int i = 0; i < Env.mapx; i++){
			for (int j = 0; j < Env.mapy; j++){
				if(!window.randomBlocks.isSelected()){
					if (Env.map[i].charAt(j) == Env.BLOCK){
						//TODO make a block
						point = new Point(j,i);
						block = new Block(point);
						objects.add(block);
						for (Robot robot: robots){
							robot.addGoal(new Goal(block));
						}
					}

				}
				if (Env.map[i].charAt(j) == Env.OBSTACLE){
					//TODO make an obstacle
					point = new Point(j,i);
					objects.add(new Obstacle(point));
					for (Robot robot: robots){
						map.set(j,i,Map.OBSTACLE);
					}
				}
			}
		}
		if (window.randomBlocks.isSelected()){
			for (int i = 0; i < numBlocks; i ++){
				while(true){
					int x = (int)(Math.random()*Env.mapx);
					int y = (int)(Math.random()*Env.mapy);
					if (map.get(x,y)==Map.EMPTY){
						point = new Point(x,y);
						block = new Block(point);
						objects.add(block);
						for (Robot robot: robots){
							robot.addGoal(new Goal(block));
						}
						map.set(x, y, Map.BLOCK);
						break;
					}
				}
			}
		}

	}

	public void printGoals(){
		goalCounter ++;
		String name = "currentgoals"+goalCounter;
		for (Robot r: robots){
			StringBuilder str = new StringBuilder();
			str.append(String.format("robot %d goalx: %d goaly: %d",r.id,r.goal.mapPos.x,r.goal.mapPos.y));
			Debug.debug(str.toString(), name);
		}
	}

	public void printMap(){
		mapcounter ++;
		String name = "map"+mapcounter;
		for (int i = 0; i< Env.mapy; i++){
			StringBuilder str = new StringBuilder();
			for (int j = 0; j < Env.mapx; j++){
				if((int)map.get(j, i)>=0){
					str.append(String.format("%d",(int)map.get(j,i)));
				}else if ((int)map.get(j, i)==Map.EMPTY){
					str.append("  ");
				}else if ((int)map.get(j, i)==Map.OBSTACLE){
					str.append("#");
				}else if ((int)map.get(j, i)==Map.BLOCK){
					str.append("+");
				}
			}
			Debug.debug(str.toString(), name);
		}
	}

	public void createRobots(){
		map = new Map();
		Robot.ID = -1;
		robots = new ArrayList<Robot>();
		numRobots = window.getNumRobots();
		//because inquiring minds like to know
		MaxNMove.setNumPlayers(numRobots);
		Robot robot;
		for (int i = 0; i < numRobots; i++){
			while(true){
				int x = (int)(Math.random()*Env.mapx);
				//int y = (int)(Math.random()*Env.mapy);
				int y = Env.mapy-1;
				if (map.get(x,y)==Map.EMPTY){
					robots.add(
							robot = new Robot(getPosFromMapPos(x,y), this, map,
									window.robotAlgos[i]));
					
					map.set(x, y, robot.id);
					break;
				}
			}
		}
	}

	public ArrayList<Robot> getRobots(){
		return robots;
	}



	public boolean step(){
		boolean moreGoals = false;
		for (Robot robot: robots){
			if(robot.step()){
				moreGoals = true;
			}
		}
		return moreGoals;
	}

	public Point getMapPosition(Point2D.Double pos){
		return new Point((int)pos.x/Env.cellsize,
				(int)pos.y/Env.cellsize);

	}

	public Point2D.Double getPosFromMapPos(int x, int y){
		return new Point2D.Double(x * Env.cellsize + Env.cellsize/2,
				y * Env.cellsize + Env.cellsize/2);
	}

	public void goalAcheived(Goal goal) {
		objects.remove(goal.block);

		for (Robot robot: robots){
			robot.removeGoal(goal);
		}

	}



	public void initialize() {
		for (Robot robot: robots){
			robot.initialize();
		}

	}
}
