package dang.AIrobot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class DrawPanel extends JPanel {
	ArrayList<PanelObject> objects;
	
	public DrawPanel(){
		objects = new ArrayList<PanelObject>();
	}
	
	public void addObject(PanelObject o){
		objects.add(o);
	}
	
	public void paintComponent(Graphics gg){
		Graphics2D g = (Graphics2D)gg;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.BLACK);
		for(PanelObject po : objects){
			po.draw(g);
		}
	}
}
