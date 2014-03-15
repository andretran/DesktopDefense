// Desktop Tower Defence Main

// NOTE: THERE ARE HOT KEYS: S = sell, U = upgrade, ESC = deselect
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DTDMain extends JFrame{	
	// Initializes the Game
	public DTDMain() throws Exception{
		super("Desktop Tower Defence");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		Container content = getContentPane();   // default BorderLayout used
		GamePanel gp = new GamePanel();			// custom gamePanel is intialized
		content.add(gp, "Center");   			
		
		pack();
		setResizable(false);
		setVisible(true);
	}
	

    public static void main(String []args) throws Exception{ 
		new DTDMain();
    }
}