package dang.AIrobot;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SpeedDialog extends JPanel{

	private static final long serialVersionUID = 7132858914782235474L;
	
	JSlider sensorSpeed;
	JTextField sSpeed;
	int speed;
	int lower = 0;
	int upper = 100;
	MainWindow game;
	JFrame frame;
	
	/**
	 * Constructor
	 * 
	 * Initializes the speed control GUI element
	 * @param game
	 */
	public SpeedDialog(final MainWindow game){
		//super(game.frame);
		this.frame = game.frame;
		this.game = game;
		//setSize (x,y);
		//JButton OK, cancel;
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(layout);
		setLocation(frame.getX()+frame.getWidth(),frame.getY());
		
		speed = game.speed;
		
		//x label
		constraints.gridx = 0; constraints.gridy = 0;
		constraints.gridwidth = 3; constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1; constraints.weighty = 1;
		constraints.insets = new Insets(5, 10, 0, 0);
		JLabel label = new JLabel("Sensor speed in ms ("+lower+" - "+ upper+  ")  :");
		layout.setConstraints(label, constraints);
		add(label);

		//x position
		sensorSpeed = new JSlider(JSlider.HORIZONTAL, lower, upper,speed);
		
		constraints.gridx = 1; constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(5, 5, 0, 10);
		layout.setConstraints(sensorSpeed, constraints);
		add(sensorSpeed);

		//x text
		sSpeed = new JTextField(5);
		sSpeed.setText(String.valueOf(sensorSpeed.getValue()));
		sSpeed.setEditable(false);
		constraints.gridx=0; constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 10, 0, 0);
		layout.setConstraints(sSpeed, constraints);
		add(sSpeed);

	
		
		sensorSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				sSpeed.setText(String.valueOf(source.getValue()));
				if (!source.getValueIsAdjusting()){
					speed = source.getValue();
					game.speed = speed;
				}
			}
		});
		
		setVisible(true);
	}

	public void OKButtonClicked(){
		//Debug.debug("Setting speed at: " + speed, "speed");
		//dispose();
	}
	
	public void cancelButtonClicked(){
		//dispose();
	}
}