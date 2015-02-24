package dang.AIrobot;

import java.awt.Point;

public class Map {
/*
	public static final double OBSTACLE = 1;
	public static final double EMPTY = 0;
	public static final double ROBOT = 2;
	public static final double BLOCK = 3;
	
	*/
	
	public static final double OBSTACLE = -2;
	public static final double EMPTY = -1;
	public static final double ROBOT = -4;
	public static final double BLOCK = -3;
	
	double [][] map;
	public Map(){
		map = new double[Env.mapx][Env.mapy];
		for (int i =0; i < Env.mapx; i ++){
			for (int j = 0; j < Env.mapy; j++){
				map[i][j] = Map.EMPTY;
			}
		}
	}
	
	public double get(int x, int y){
		return map[x][y];
	}
	
	public boolean isClear(int x, int y){
		//if ((map[x][y]==Map.OBSTACLE)||(map[x][y]==Map.ROBOT)){
		if ((map[x][y]==Map.OBSTACLE)||(map[x][y]>=0)){
			return false;
		}else{
			return true;
		}
	}
	
	public boolean isClear(Point p){
		//if ((map[p.x][p.y]==Map.OBSTACLE)||(map[p.x][p.y]==Map.ROBOT)){
		if ((map[p.x][p.y]==Map.OBSTACLE)||(map[p.x][p.y]>=0)){
			return false;
		}else{
			return true;
		}
	}
	
	public void set(int x, int y, double value){
		map[x][y] = value;
	}
	
	public double pos(Point point){
		return map[point.x][point.y];
	}
	
	public void setPos(Point mapPos, double thing){
		map[mapPos.x][mapPos.y] = thing;
	}
	
	/*
	 * If we want a list of where the agents are, rather than
	 * the matrix (ie for efficiency reasons)
	 */
	public Point[] getMapPoses(){
		Point[] mapPoses = new Point[MaxNMove.numPlayers];
		for (int i = 0; i < Env.mapx; i++){
			for (int j = 0; j < Env.mapy; j++){
				if (get(i, j)>=0){
					mapPoses[(int)get(i, j)] = new Point(i,j);
				}
			}
		}
		return mapPoses;
	}
	
	public Map clone(){
		Map map = new Map();
		for (int i = 0; i < Env.mapx; i ++){
			for (int j = 0; j < Env.mapy; j++){
				map.set(i, j, get(i,j));
			}
		}
		return map;
	}
}
