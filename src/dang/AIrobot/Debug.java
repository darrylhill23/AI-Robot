package dang.AIrobot;



import java.util.HashMap;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;




public class Debug {
	static String[] disable = {"bc","blockdetect"};
	static HashMap<String, DebugFrame> debugFrames = new HashMap<String, DebugFrame>();
	static public boolean DEBUG = true;
	static JTextPane console;
	static boolean useConsole = false;
	
	public static void setDebug(boolean d){
		DEBUG = d;
	}
	
	public static void setConsole(JTextPane con){
		console = con;
		useConsole = true;
	}
	
	public static void debug(String message) {
		if (DEBUG){
			if (useConsole){
				StyledDocument doc = console.getStyledDocument();
				try {
					doc.insertString(doc.getLength(), message+"\n", null);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				System.out.println(message);
			}
		}
	}
	
	public static void debug(String message, Object ...args){
		System.out.printf(message+"\n", args);
	}
	
	public static void debug(char c){
		if (DEBUG){
			if (useConsole){
				StyledDocument doc = console.getStyledDocument();
				try {
					doc.insertString(doc.getLength(), Character.toString(c), null);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				System.out.println(c);
			}
		}
	}
	
	public static void debug(String message, String window){
		//disable windows we don't need to avoid clutter
		for (int i = 0; i < disable.length; i++){
			if (disable[i].equals(window)){
				return;
			}
		}
		if (!DEBUG)return;
		if (debugFrames.containsKey(window)){
			debugFrames.get(window).addMessage(message);
		}else{
			DebugFrame frame = new DebugFrame(window);
			debugFrames.put(window, frame);
			frame.addMessage(message);
		}
	}
	
	private static class DebugFrame extends JFrame{
		
		JTextArea output;
		public DebugFrame(String name){
			super(name);
			setLocation(400, 0);
			setSize(400,400);
			output = new JTextArea();
			JScrollPane pane = new JScrollPane(output,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			add(pane);
			setVisible(true);
			
		}
		
		public void addMessage(String message){
			output.append(message+"\n");
		}
	}
}
