package dang.AIrobot;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingWorker;


public class MainWindow implements ActionListener{

	JFrame frame;
	Canvas canvas;
	BufferStrategy strat;
	MainController control = null;
	Model model;
	ArrayList<PanelObject> objects;
	SpeedDialog sd;
	int speed = 50;
	JTextField numBlocks, numRobots;
	JCheckBoxMenuItem randomBlocks;
	int mapcounter = 0;
	JMenu submenu[];
	enum AlgoType {AStar, MaxN, Paran};
	AlgoType robotAlgos[] = new AlgoType[20];
	
	public MainWindow(){
		control = new MainController(this);
		setUpUI();
		initializeGame();
	}
	
	public void setUpUI(){
		frame = new JFrame("Robot Games");
		frame.setJMenuBar(setUpMenuBar());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 800);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);  
		frame.getContentPane().setLayout(new BorderLayout());
		canvas = new Canvas();
		//canvas.setSize(Env.mapx, Env.mapy);
		frame.getContentPane().add(canvas);
		frame.getContentPane().add(bottomPanel(), BorderLayout.SOUTH);
		//frame.pack();
		updateMenu();
		frame.setVisible(true);
		canvas.createBufferStrategy(2);
		strat = canvas.getBufferStrategy();
	}
	
	public JMenuBar setUpMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Settings");
		menuBar.add(menu);
		menu.add(randomBlocks = new JCheckBoxMenuItem("random blocks"));
		randomBlocks.setSelected(true);
		menu = new JMenu("Algorithms");
		submenu = new JMenu[20];
		for (int i = 0; i < 20; i++){
			menu.add(submenu[i] = new JMenu("Robot "+i));
			JRadioButtonMenuItem h1 = new JRadioButtonMenuItem("A*");
			JRadioButtonMenuItem h2 = new JRadioButtonMenuItem("Max-N");
			JRadioButtonMenuItem h3 = new JRadioButtonMenuItem("Paranoid");
			h1.setSelected(true);
			ButtonGroup g = new ButtonGroup();
			g.add(h1);
			g.add(h2);
			g.add(h3);
			submenu[i].add(h1);
			submenu[i].add(h2);
			submenu[i].add(h3);
			h1.addActionListener(this);
			h1.setActionCommand("AStar."+i);
			h2.addActionListener(this);
			h2.setActionCommand("Max-N."+i);
			h3.addActionListener(this);
			h3.setActionCommand("Paran."+i);
			robotAlgos[i] = AlgoType.AStar;
		}
		menuBar.add(menu);
		return menuBar;
	}
	
	public void update(){
		Graphics2D g = (Graphics2D)strat.getDrawGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setColor(Color.BLACK);
		for(PanelObject po : objects){
			po.draw(g);
		}
		strat.show();
	}
	
	/*
	 * Also need random block placement, based off of the UI.
	 * Additionally the ability to set the number of robots,
	 * and new menu items for each one and the ability to 
	 * set their algorithms. Eventually.
	 */
	public JPanel bottomPanel(){
		JPanel bottompanel = new JPanel(new BorderLayout());
		bottompanel.add(buttonPanel(),BorderLayout.NORTH);
		bottompanel.add(inputForm(), BorderLayout.CENTER);
		return bottompanel;
	}
	
	public JPanel inputForm(){
		JPanel input = new JPanel(new GridLayout(2,4));
		input.add(new JLabel("Number of blocks"));
		input.add(numBlocks = new JTextField("10"));
		input.add(new JLabel("Number of robots"));
		input.add(numRobots = new JTextField("2"));
		return input;
	}
	
	public JPanel buttonPanel(){
		JPanel buttonpanel =  new JPanel(new GridLayout(3,0));
		JButton start = new JButton("start");
		start.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				startGame();
			}
		});
		
		JButton restart = new JButton("new");
		restart.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				initializeGame();
			}
		});
		
		JButton printMap = new JButton("print map");
		printMap.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				model.printMap();
			}
		});
		
		JButton printGoals = new JButton("print goals");
		printGoals.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				model.printGoals();
			}
		});

		buttonpanel.add(restart);
		buttonpanel.add(start);
		buttonpanel.add(printMap);
		buttonpanel.add(printGoals);
		buttonpanel.add(sd = new SpeedDialog(this));
		return buttonpanel;
	}
	
	
	public int getNumBlocks(){
		return Integer.parseInt(numBlocks.getText());
	}
	
	public int getNumRobots(){
		return Integer.parseInt(numRobots.getText());
	}
	
	public void initializeGame(){
		objects = new ArrayList<PanelObject>();
		if (control!=null){
			control.running = false;
		}
		control = new MainController(this);
		int newRobots = Integer.parseInt(numRobots.getText());
		int newBlocks = Integer.parseInt(numBlocks.getText());
		model = new Model(this, control, objects, newRobots, newBlocks);
		objects.addAll(model.getRobots());
		control.setModel(model);
		updateMenu();
		update();
	}
	
	public void updateMenu(){
		JMenu menu = frame.getJMenuBar().getMenu(1);
		menu.removeAll();
		int total = Integer.parseInt(numRobots.getText());
		if (total > 20)total = 20;
		for (int i = 0; i < total; i++){
			menu.add(submenu[i]);
		}
	}
	
	public void startGame(){
		Thread worker = new Thread() {
			@Override
			public void run() {
				control.start();
			}

		};
		worker.start();
	}
	
	public static void main(String[] args){
		new MainWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		int index = command.charAt(6) - '0';
		setRobotAlgorithm(command.substring(0, 5), index);
		
	}
	
	public void setRobotAlgorithm(String str, int index) {
		if (str.equals("AStar")){
			robotAlgos[index] = AlgoType.AStar; 
		}else if(str.equals("Max-N")){
			robotAlgos[index] = AlgoType.MaxN; 
		}else if(str.equals("Paran")){
			robotAlgos[index] = AlgoType.Paran; 
		}else{
			new Exception("unknown algorithm").printStackTrace();
		}
		
	}
	
}
