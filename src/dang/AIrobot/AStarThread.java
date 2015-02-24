package dang.AIrobot;

import java.util.ArrayList;

public class AStarThread extends AlgoThread {

	int lookahead = 1;
	AStarMove root;
	AStarMove nextMove;
	//boolean done = false;
	ArrayList<AStarMove> moves;
	boolean look = true;
	Goal goal;
	Map map;
	Robot robot;

	public AStarThread(int lookahead, AStarMove root, Robot robot){
		this.lookahead = lookahead;
		this.root = root;
		this.robot = robot;
		this.goal = robot.goal;
		this.map = robot.map;
		//done = true;
	}

	public AStarMove getFinalMove(){
		return nextMove;
	}

	public void run(){
		//done = false;
		int lookCount = 0;
		moves = new ArrayList<AStarMove>();
		nextMove = root;
		while(true){
			if (look){
				lookCount++;
			}
			//Debug.debug("going to get moves robotid: %d",robot.id);
			getMoves(nextMove, moves);
			//Debug.debug("got moves");
			double nextMoveCost = 999999999;
			double cost;
			int index;
			for (int i=0; i < moves.size(); i++){
				if (moves.get(i).isExplored()){
					//Debug.debug("move is explored");
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
			//Debug.debug("move is not explored");
			nextMove.setExplored(true);
			//Debug.debug(String.format("next move cost: %.2f",nextMoveCost));
			//Debug.debug("move x %.2f y %.2f", nextMove.pos.x, nextMove.pos.y);
			if ((nextMove.getMapPos().equals(goal.mapPos))||(lookahead == lookCount)){
				Debug.debug("Found goal? lookahead %d, lookcount %d",
						lookahead, lookCount);
				//return  nextMove;
				//done = true;
				break;
			}
		}
		robot.calculating = false;
		robot.needCalculatePath = true;
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
				if (mapy < Env.mapy -1){
					if ((map.isClear(mapx-1,mapy+1))&&
							(map.isClear(mapx,mapy+1))&&
							(map.isClear(mapx-1,mapy))){
						move = new AStarMove(mapx-1, mapy+1, cost+Math.sqrt(2), thisMove, Math.atan2(1, -1));
						addMoves(move, moves);
					}
				}
			}
		}


		if (mapx < Env.mapx -1){
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


				if (mapy < Env.mapy -1){
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

		if (mapy < Env.mapy -1){
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
		//Debug.debug("adding moves");
		if (move.isExplored()){
			Debug.debug("Errror!! Move is explored!!");
		}
		double x = move.mapPos.x;
		double y = move.mapPos.y;
		double estimate = Math.sqrt(Math.pow((x - goal.mapx()),2) + Math.pow((y - goal.mapy()),2));
		//Debug.debug(String.format("Estimate: %.2f",estimate));
		/*
		 * Going to add in the cost of a turn. Watch as it falls apart
		 */
		double dir = Math.abs(robot.direction - move.getDirection());
		if (dir > Math.PI){
			dir = Math.PI*2 - dir;
		}
		//Debug.debug("turn cost %.2f", dir*2/Math.PI);

		estimate += dir*2/Math.PI;

		move.setEstimate(estimate);
		moves.add(move);
	}

}
