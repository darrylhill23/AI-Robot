package dang.AIrobot;

import java.awt.Point;
import java.util.ArrayList;

public class MaxNThread extends AlgoThread {

	MaxNMove nextMove;
	Robot robot;
	int depth;
	MaxNMove root;
	ArrayList<MaxNMove> moves;
	//boolean look = true;
	ArrayList<Goal> goals;
	//Map map;
	//Point[] mapPoses;
	
	
	public MaxNThread(int depth, MaxNMove firstNMove, Robot robot) {
		this.depth = depth;
		this.robot = robot;
		root = firstNMove;
		goals = robot.goals;
		
		//map = robot.getMap();
		
	}
	
	public void run(){
		Debug.debug("running MaxNThread");
		moves = new ArrayList<MaxNMove>();
		nextMove = root;
		/*
		 * So we need a list for every layer, and a layer
		 * for every player. That means up to 10.
		 * This tree is going to be bushy. 2 levels might
		 * be too many. Going with one. When the scores are
		 * calculated they will have to propagate up to the
		 * parent.
		 */
		ArrayList<ArrayList<MaxNMove>> allmoves = new ArrayList<ArrayList<MaxNMove>>();
		for (int i = 0; i < MaxNMove.numPlayers + 1; i ++){
			allmoves.add(new ArrayList<MaxNMove>());
		}
		allmoves.get(0).add(root);
		for(int i = 0; i < depth; i ++){
			for (int j =0; j < MaxNMove.numPlayers; j ++){
				for (MaxNMove move : allmoves.get(j)){
					getMoves(j, move, allmoves.get(j+1));		
				}
			}
		}
		
		/*
		 * So we have gotten all the moves, now we need to calculate the leaves
		 * There is a problem with the logic, right now it won't do more than
		 * one round deep, as it will mix the leaf nodes with regular nodes
		 */
		for(MaxNMove move: allmoves.get(MaxNMove.numPlayers)){
			move.calculateValue();
		}
		robot.calculating = false;
		robot.needCalculatePath = true;
	}
	
	public void getMoves(int playerNum, MaxNMove thisMove, ArrayList<MaxNMove> moves){
		MaxNMove move = null;
		Debug.debug("getting moves");

		int mapx = thisMove.getMaxPlayerMapPose().x;
		int mapy = thisMove.getMaxPlayerMapPose().y;
		
		Map map = thisMove.map;
		Map clone = map.clone();
		if (mapx != 0){
			/*
			 * Have to update the map
			 */
			if (map.isClear(mapx-1,mapy)){
				clone.set(mapx, mapy, Map.EMPTY);
				clone.set(mapx-1, mapy, playerNum);
				move = new MaxNMove(mapx-1, mapy, thisMove, Math.atan2(0,-1),clone);
				addMoves(move, moves);

				if (mapy != 0){
					//check the space but also the adjoining spaces, since
					//we are travelling on the diagonal
					if ((map.isClear(mapx-1,mapy-1))&&
						(map.isClear(mapx,mapy-1))&&
						(map.isClear(mapx-1,mapy))){
						clone.set(mapx, mapy, Map.EMPTY);
						clone.set(mapx-1, mapy-1, playerNum);
						move = new MaxNMove(mapx-1, mapy-1,thisMove, Math.atan2(-1, -1),clone);
						addMoves(move, moves);
					}
				}
				if (mapy < Env.mapy -1){
					if ((map.isClear(mapx-1,mapy+1))&&
							(map.isClear(mapx,mapy+1))&&
							(map.isClear(mapx-1,mapy))){
						clone.set(mapx, mapy, Map.EMPTY);
						clone.set(mapx-1, mapy+1, playerNum);
						move = new MaxNMove(mapx-1, mapy+1, thisMove, Math.atan2(1, -1),clone);
						addMoves(move, moves);
					}
				}
			}
		}


		if (mapx < Env.mapx -1){
			if (map.isClear(mapx+1,mapy)){
				clone.set(mapx, mapy, Map.EMPTY);
				clone.set(mapx+1, mapy, playerNum);
				move = new MaxNMove(mapx+1, mapy, thisMove, Math.atan2(0, 1),clone);
				addMoves(move, moves);

				if (mapy != 0){
					if ((map.isClear(mapx+1,mapy-1))&&
							(map.isClear(mapx,mapy-1))&&
							(map.isClear(mapx+1,mapy))){
						clone.set(mapx, mapy, Map.EMPTY);
						clone.set(mapx+1, mapy-1, playerNum);
						move = new MaxNMove(mapx+1, mapy-1, thisMove, Math.atan2(-1,1),clone);
						addMoves(move, moves);
					}
				}


				if (mapy < Env.mapy -1){
					if ((map.isClear(mapx+1,mapy+1))&&
							(map.isClear(mapx,mapy+1))&&
							(map.isClear(mapx+1,mapy))){
						clone.set(mapx, mapy, Map.EMPTY);
						clone.set(mapx+1, mapy+1, playerNum);
						move = new MaxNMove(mapx+1, mapy+1, thisMove, Math.atan2(1, 1),clone);
						addMoves(move, moves);
					}
				}
			}
		}


		if (mapy != 0){
			if (map.isClear(mapx,mapy-1)){
				clone.set(mapx, mapy, Map.EMPTY);
				clone.set(mapx, mapy-1, playerNum);
				move = new MaxNMove(mapx, mapy-1, thisMove, Math.atan2(-1,0),clone);
				addMoves(move, moves);
			}
		}

		if (mapy < Env.mapy -1){
			if (map.isClear(mapx,mapy+1)){
				clone.set(mapx, mapy, Map.EMPTY);
				clone.set(mapx, mapy+1, playerNum);
				move = new MaxNMove(mapx, mapy+1, thisMove, Math.atan2(1, 0),clone);
				addMoves(move, moves);
			}
		}


	}
	
	public void addMoves(MaxNMove move, ArrayList<MaxNMove> moves){
		moves.add(move);
	}


	@Override
	Move getFinalMove() {
		return root;
	}

}
