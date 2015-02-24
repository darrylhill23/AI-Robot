package dang.AIrobot;

import java.awt.Point;
import java.util.ArrayList;

public class MaxNMove extends Move {

	int maxPlayer;		//the index of the player trying to maximize their score
	double[] scores;	//the scores themselves
	Map map;
	MaxNMove currentChild;
	static int numPlayers = 2;	
	ArrayList<Goal> goals;
	Point[] mapPoses;
	double MAXSCORE = Math.sqrt(Math.pow(Env.mapx,2) + Math.pow(Env.mapy,2));

	public static void setNumPlayers(int numPlayers){
		MaxNMove.numPlayers = numPlayers;
	}

	/*
	 * This constructor is only necessary at the root of the tree. The
	 * extra information is seeded to the children
	 */
	public MaxNMove(Point mapPos, double direction, 
			int maxPlayer, ArrayList<Goal> goals, Map map){
		this.mapPos = mapPos;
		this.direction = direction;
		scores = new double[MaxNMove.numPlayers];
		this.maxPlayer = maxPlayer;
		this.goals = goals;
		this.map = map;
		mapPoses = map.getMapPoses();
		initializeScores();
	}

	public MaxNMove(Point mapPos, MaxNMove parent, double direction,
			Map map){
		this.mapPos = mapPos;
		this.parent = parent;
		this.direction = direction;
		scores = new double[MaxNMove.numPlayers];
		maxPlayer = (parent.getMaxPlayer()+1)%MaxNMove.numPlayers;
		this.goals = parent.goals;
		this.map = map;
		mapPoses = map.getMapPoses();
		initializeScores();
	}

	public MaxNMove(int x, int y, MaxNMove parent, double direction,
			Map map){
		this.mapPos = new Point(x,y);
		this.parent = parent;
		goals = parent.goals;
		this.direction = direction;
		scores = new double[MaxNMove.numPlayers];
		maxPlayer = (parent.getMaxPlayer()+1)%MaxNMove.numPlayers;
		this.map = map;
		mapPoses = map.getMapPoses();
		initializeScores();
	}
	
	public void initializeScores(){
		for (int i = 0; i < numPlayers; i++){
			scores[i]=0;
		}
	}
	
	public Point getMaxPlayerMapPose(){
		if(mapPoses == null){
			Debug.debug("map poses are null");
		}
		Debug.debug("Maxplayer %d",maxPlayer);
		return mapPoses[maxPlayer];
	}

	public int getMaxPlayer() {
		return maxPlayer;
	}

	public void setMaxPlayer(int maxPlayer) {
		this.maxPlayer = maxPlayer;
	}

	public double[] getScores() {
		return scores;
	}

	public void setScore(double score, int index){
		scores[index] = score;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}
	
	public void propagatedValues(MaxNMove child){
		//maximize our own score
		if (child.scores[maxPlayer]>this.scores[maxPlayer]){
			this.scores = child.scores;
			currentChild = child;
		}
		if (parent != null){
			((MaxNMove)parent).propagatedValues(this);
		}
	}

	public void calculateValue(){
		/*
		 * Calculate the value at this node for every player, then
		 * propagate it up to the parent. Start simply, the lowest sum
		 * of all the distances to goals. I can use the cost algorithm 
		 * from A*
		 */
		for (int i = 0; i < numPlayers; i++){
			double score = 0;
			for (Goal goal: goals){
				double x = mapPos.x;
				double y = mapPos.y;
				/*
				 * Since I am already getting confused, this is simply
				 * so that the max score remains a max score rather than
				 * a min score. 
				 */
				double temp = ((Math.pow((x - goal.mapx()),2) + Math.pow((y - goal.mapy()),2)));
				if (temp == 0){
					score += 1000;
				}else{
					score += 1/temp;
				}
			}
			scores[i] = score;	
		}
		if (parent != null){
			((MaxNMove)parent).propagatedValues(this);
		}
	}


}
