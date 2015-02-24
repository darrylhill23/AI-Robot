package dang.AIrobot;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import dang.AIrobot.MainWindow.AlgoType;

public class Robot implements PanelObject{

	static int ID = 0;
	int id;
	Point2D.Double pos;
	Point mapPos;
	ArrayList<Goal> goals = new ArrayList<Goal>();
	Goal goal;
	double direction;
	Map map;
	final int MAPX;
	final int MAPY;
	boolean running = false;
	int lookahead = 100;
	int depth = 1;
	int roundsSpentThinking  = 0;
	boolean look = true;
	ArrayList<AStarMove> moves;
	ArrayList<Move> path;
	final int size = 20;
	Rectangle2D.Double rect;
	Line2D.Double line;
	AffineTransform transform;
	Iterator<Move> iterator;
	Move currentMove;
	boolean needPath = true;
	Model model;
	AlgoThread algoThread = null;
	boolean calculating = false;
	boolean needCalculatePath = false;	
	AlgoType algo;

	public Robot(Point2D.Double pos, Model model,Map map, AlgoType algo){
		ID++;
		id = ID;
		this.model = model;
		this.pos = pos;
		mapPos = getMapPosition(pos);
		//Debug.debug("Map x: %d y: %d", mapPos.x, mapPos.y);
		direction = -Math.PI/2;
		MAPX = Env.mapx;
		MAPY = Env.mapy;
		path = new ArrayList<Move>();
		this.map = map;
		rect = new Rectangle2D.Double(0- size/2, 0 - size/2, size, size);
		line = new Line2D.Double(0, 0, size, 0);
		transform = new AffineTransform();
		this.algo = algo;
		//goal = new Point2D.Double(20,10);
	}
	
	/* ********************************************/
	/* Planning section*/
	/* ********************************************/

	public void initialize(){
		Debug.debug("intialize...");
		//map.setPos(mapPos, Map.ROBOT);
		map.setPos(mapPos, id);
		getNextGoal();
	}
	
	public void calculate(){
		
		Debug.debug("calculating");
		switch(algo){
		case AStar:	
			AStarMove firstAMove = new AStarMove(mapPos,0,null,0);
			algoThread = new AStarThread(lookahead, firstAMove, this);
			calculating = true;
			algoThread.start();
			break;
		case MaxN:
			MaxNMove firstNMove = new MaxNMove(mapPos,direction, 0,goals,
					map.clone());
			algoThread = new MaxNThread(depth, firstNMove, this);
			calculating = true;
			algoThread.start();
			break;
		}
		//if it returns right away we get a path
		if (calculating){
			Debug.debug("waiting");
			roundsSpentThinking++;
			return;
		}
		calculatePath(lookahead);
		
	}
	
	
	public boolean calculatePath(int lookahead){
		
		Debug.debug("done waiting");
		Move finalMove = algoThread.getFinalMove();
		Debug.debug("mappos "+ finalMove.mapPos);
		path.clear();
		switch(algo){
		case AStar:
			while (finalMove != null){
				Debug.debug("adding move");
				path.add(0,finalMove);
				finalMove = finalMove.parent;
			}
			break;
		case MaxN:
			path.add(finalMove);
			path.add(((MaxNMove)finalMove).currentChild);
			break;
		}
		
		Debug.debug("done calculating");
		iterator = path.iterator();
		//the first move is the current locations
		currentMove = iterator.next();
		if(iterator.hasNext()){
			currentMove = iterator.next();
		}else{
			needPath = true;
		}
		return true;
	}
	
	/*
	 * What algorithm is this? I think Max-N.
	 * So each robot takes their turn on a separate level, 
	 * and tries to maximize their score. What's more, each
	 * node tracks the score of each robot. 
	 * 
	 * Might need a different Move, or a subclass. In effect, the
	 * score should perhaps be separate? Or I should make a Move
	 * interface
	 */
/*	public MaxNMove maxN(int lookahead){
		int lookCount = 0;
		moves = new ArrayList<AStarMove>();
		MaxNMove.numPlayers = model.getRobots().size();
		MaxNMove nextMove = new MaxNMove(mapPos,null,0,0);
		
		/*
		 * So what now? Plunge to a certain depth, generate leaves,
		 * come back. 
		 */
		
	//}
	
	
	
	public AStarMove aStar(int lookahead, Move root){

		int lookCount = 0;
		moves = new ArrayList<AStarMove>();
		AStarMove nextMove = (AStarMove)root;
		while(true){
			if (look){
			  lookCount++;
			}
			//Debug.debug("going to get moves");
			getMoves(nextMove, moves);
			//Debug.debug("got moves");
			double nextMoveCost = 999999999;
			double cost;
			int index;
			for (int i=0; i < moves.size(); i++){
				if (moves.get(i).isExplored()){
					continue;
				}
				cost = moves.get(i).getTotalCost();
				if (nextMoveCost > cost){
					nextMoveCost = cost;
					index = i;
					nextMove = moves.get(i);
					//break ties randomly
				}else if(nextMoveCost == cost){
					if (Math.random() > 0.5){
						index = i;
						nextMove = moves.get(i);
					}
				}
			}
			nextMove.setExplored(true);
			//Debug.debug(String.format("next move cost: %.2f",nextMoveCost));
			//Debug.debug("move x %.2f y %.2f", nextMove.pos.x, nextMove.pos.y);
			if ((nextMove.getMapPos().equals(goal.mapPos))||(lookahead == lookCount)){
				//Debug.debug("Found goal? lookahead %d, lookcount %d",
				//		lookahead, lookCount);
				return  nextMove;
			}
		}
	}
	
	public void getMoves(AStarMove thisMove, ArrayList<AStarMove> moves){
		 	AStarMove move = null;

		    double cost = thisMove.getCost();
		    
		    int mapx = (int)thisMove.getMapPos().x;
		    int mapy = (int)thisMove.getMapPos().y;
		    
		    /*
		      Four possible moves, maybe the diagonals later.
		     */
		    if (mapx != 0){
		        /*check for obstacles. Later when we use fuzzy
		        * logic we can implement a cost adjustment for
		        * partial obstacles
		        * 
		        * The robot can travel on the diagonal only if
		        * there are no blocks on either side of it
		        */
		        if (map.isClear(mapx-1,mapy)){
		            move = new AStarMove(mapx-1, mapy, cost+1, thisMove, Math.atan2(0,-1));
		            addMoves(move, moves);

		            if (mapy != 0){
		            	//check the space but also the adjoining spaces, since
		            	//we are travelling on the diagonal
		                if ((map.isClear(mapx-1,mapy-1))&&
		                	(map.isClear(mapx,mapy-1))&&
		                	(map.isClear(mapx-1,mapy))){
		                    move = new AStarMove(mapx-1, mapy-1, cost+Math.sqrt(2),thisMove, Math.atan2(-1, -1));
				            addMoves(move, moves);
		                }
		            }
		            if (mapy < MAPY -1){
		                if ((map.isClear(mapx-1,mapy+1))&&
		                	(map.isClear(mapx,mapy+1))&&
		                	(map.isClear(mapx-1,mapy))){
		                    move = new AStarMove(mapx-1, mapy+1, cost+Math.sqrt(2), thisMove, Math.atan2(1, -1));
				            addMoves(move, moves);
		                }
		            }
		        }
		    }


		    if (mapx < MAPX -1){
		        if (map.isClear(mapx+1,mapy)){
		            move = new AStarMove(mapx+1, mapy, cost+1, thisMove, Math.atan2(0, 1));
		            addMoves(move, moves);

		            if (mapy != 0){
		                if ((map.isClear(mapx+1,mapy-1))&&
		                	(map.isClear(mapx,mapy-1))&&
		                	(map.isClear(mapx+1,mapy))){
		                    move = new AStarMove(mapx+1, mapy-1, cost+Math.sqrt(2), thisMove, Math.atan2(-1,1));
				            addMoves(move, moves);
		                }
		            }


		            if (mapy < MAPY -1){
		            	 if ((map.isClear(mapx+1,mapy+1))&&
				             (map.isClear(mapx,mapy+1))&&
				             (map.isClear(mapx+1,mapy))){
		                    move = new AStarMove(mapx+1, mapy+1, cost+Math.sqrt(2), thisMove, Math.atan2(1, 1));
				            addMoves(move, moves);
		                }
		            }
		        }
		    }


		    if (mapy != 0){
		        if (map.isClear(mapx,mapy-1)){
		            move = new AStarMove(mapx, mapy-1, cost+1, thisMove, Math.atan2(-1,0));
		            addMoves(move, moves);
		        }
		    }

		    if (mapy < MAPY -1){
		        if (map.isClear(mapx,mapy+1)){
		            move = new AStarMove(mapx, mapy+1, cost+1, thisMove, Math.atan2(1, 0));
		            addMoves(move, moves);
		        }
		    }


	}

	/*
	 * As the name implies, it does add the moves, but it actually
	 * adds the moves while supplying the estimate. 
	 */
	public void addMoves(AStarMove move, ArrayList<AStarMove> moves){
		
		double x = move.mapPos.x;
		double y = move.mapPos.y;
        double estimate = Math.sqrt(Math.pow((x - goal.mapx()),2) + Math.pow((y - goal.mapy()),2));
        //Debug.debug(String.format("Estimate: %.2f",estimate));
        /*
         * Going to add in the cost of a turn. Watch as it falls apart
         */
        double dir = Math.abs(direction - move.getDirection());
        if (dir > Math.PI){
        	dir = Math.PI*2 - dir;
        }
        //Debug.debug("turn cost %.2f", dir*2/Math.PI);
        
        estimate += dir*2/Math.PI;
        
        move.setEstimate(estimate);
        moves.add(move);
	}
	
	public void removeGoal(Goal goal) {
		//Debug.debug("removing goal");
		goals.remove(goal);
		//Debug.debug("Goal size after removal: %d", goals.size());
		if (this.goal.equals(goal)){
			//Debug.debug("goals are equal");
			//Debug.debug("goalx: %d y: %d",goal.mapPos.x, goal.mapPos.y);
			getNextGoal();
			needPath = true;
		}
		
	}

	
	public boolean getNextGoal(){
		//Debug.debug("goals left %d", goals.size());
		if (goals.size()==0){
			return false;
		}else{
			//find the closest one and git it
			double closest = 999999999;
			double dist;
			for (Goal g: goals){
				dist = mapPos.distance(g.mapPos);
				if (closest>dist){
					goal = g;
					closest = dist;
				}
			}
		}
		return true;
	}
	
	
	
	/* ********************************************/
	/* End planning section*/
	/* ********************************************/
	
	/* ********************************************/
	/* Moving section*/
	/* ********************************************/

	public boolean step(){
		/*
		 * This section might be a little confusing. The checks are
		 * in an unintuitive order. Needpath and needCalculatePath
		 * are different as follows: needPath is when the current
		 * goal has been reached or eliminated and a new goal is
		 * needed. needCalculate path means the "thinking" portion
		 * is done, so we can get our new path from the result. 
		 * Terrible naming and a crappy explanation.
		 */
		if(calculating){
			roundsSpentThinking ++;
			return true;
		}
		if(needCalculatePath){
			calculatePath(lookahead);
			needCalculatePath = false;
		}
		
		if(goals.size()==0){
			return false;
		}
		if (needPath){
			calculate();
			needPath = false;
			return true;
		}
		
		//Debug.debug("Moves: "+path.size());

		/*
		 * A bit of logic concerning how to handle
		 * a move to the next position, when to update
		 * our search tree, etc
		 */
		if(currentMove == null){
			Debug.debug("currentMove =null");
		}
		while(currentMove.mapPos.equals(mapPos)){

			if (iterator.hasNext()){
				currentMove = iterator.next();
			}else{
				if (goal.mapPos.equals(mapPos)){
					model.goalAcheived(goal);
					needPath = true;
					return getNextGoal();
				}
				//not at goal, but no moves left
				//in iterator, so get another path
				//this is if we have limited lookahead
				needPath = true;
				return true;
			}
		}
		/*
		 * The situation where presumably another robot has moved
		 * into our way, although it is generalized and therefore
		 * extensible.
		 */
		if (!map.isClear(currentMove.mapPos)){
			Debug.debug("Path not empty: %.2f", map.pos(currentMove.mapPos));
			calculate();
			needPath = true;
			return true;
		}
		
		
		//Move move = path.get(1);
		//Debug.debug("Move direction: %.2f", currentMove.getDirection());
		makeAMove(currentMove);
		return true;
	}
	
	
	public void makeAMove(Move move){
		//moving one square is cost 1, turning 90 degrees cost 1
		//turning less than 45 is free
		
		//ok that works, lets make it fine grained
		//Debug.debug("Making move x: %d y:%d", move.mapPos.x, move.mapPos.y);
		//Debug.debug("Cost: %.2f", move.getTotalCost());
		double x = (move.mapPos.x * Env.cellsize)+
				Env.cellsize/2;
		double y = (move.mapPos.y * Env.cellsize)+
				Env.cellsize/2;
		
		x-=pos.x;
		y-=pos.y;
		
		//Debug.debug("dif x: %.2f y: %.2f",x,y);
		
		double dir = Math.atan2(y,x);
		
		//Debug.debug("Absolute direction: %.2f", dir);
		
		dir = dir - direction;
		
		//Debug.debug("Actual direction: %.2f", dir);

		if (dir > Math.PI){
			dir -= 2*Math.PI;
		}else if (dir < -Math.PI){
			dir += 2*Math.PI;
		}
		

		
		//greater than 5, so move 5 and return
		if (Math.abs(dir)>(Math.PI/36)){
			if (dir > 0){
				dir = Math.PI/36;
			}else{
				dir = -Math.PI/36;
			}
			direction += dir;
			return;
		}
		
		//less than 5 degrees so rotate the remainder then move
		direction += dir;
		moveForward(Env.cellsize/18);
		
	}
	
	public void moveForward(double distance){
		//divide this number into x and y components
		//based on the current direction
		pos.x += Math.cos(direction)*distance;
		pos.y += Math.sin(direction)*distance;
		//if we have moved from our current map position, update
		//the map.
		map.setPos(mapPos, Map.EMPTY);
		mapPos = getMapPosition(pos);
		//map.setPos(mapPos, Map.ROBOT);
		map.setPos(mapPos, id);
	}
	
	
	/* ********************************************/
	/* End moving section*/
	/* ********************************************/

	
	public void draw(Graphics2D g){
		transform.setToTranslation(pos.x, pos.y);
		transform.rotate(direction);
		//Debug.debug("Translate x: %.2f, y: %.2f", 
		//		transform.getTranslateX(), transform.getTranslateY());
		g.draw(transform.createTransformedShape(rect));
		g.draw(transform.createTransformedShape(line));

	}

	
	public Point getMapPosition(Point2D.Double pos){
		return new Point((int)pos.x/Env.cellsize,
				(int)pos.y/Env.cellsize);
 
	}









	
	public AlgoType getAlgo() {
		return algo;
	}

	public void setAlgo(AlgoType algo) {
		this.algo = algo;
	}

	public Point2D.Double getPos() {
		return pos;
	}


	public void setPos(Point2D.Double pos) {
		this.pos = pos;
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

	public int getLookahead() {
		return lookahead;
	}

	public void setLookahead(int lookahead) {
		this.lookahead = lookahead;
	}

	public ArrayList<AStarMove> getMoves() {
		return moves;
	}

	public void setMoves(ArrayList<AStarMove> moves) {
		this.moves = moves;
	}

	public ArrayList<Move> getPath() {
		return path;
	}

	public void setPath(ArrayList<Move> path) {
		this.path = path;
	}

	public Goal getGoal() {
		return goal;
	}


	public void addGoal(Goal goal) {
		goals.add(goal);
		//Debug.debug("goalx: %d y: %d",goal.mapPos.x, goal.mapPos.y);
	}


	public Map getMap() {
		return map;
	}


	public void setMap(Map map) {
		this.map = map;
	}


	public int getMapx() {
		return MAPX;
	}

	
	public int getMapy() {
		return MAPY;
	}


	public boolean isRunning() {
		return running;
	}


	public void setRunning(boolean running) {
		this.running = running;
	}

	
}
