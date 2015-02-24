package dang.AIrobot;

import java.awt.geom.Point2D;

public class MainController {

	MainWindow window;
	Model model;
	int speed = 50;
	boolean running = true;
	
	public MainController(MainWindow window){
		this.window = window;
	}
	
	public void setModel(Model model){
		this.model = model;
	}

	public void start() {
		// TODO Auto-generated method stub
		running = true;
		model.initialize();
		while(model.step()&&running){
			window.update();
			delay(window.speed);
		}
		//}
	}
	
	public void delay(int delay){
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
