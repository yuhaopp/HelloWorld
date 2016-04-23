package ElevatorDispatch;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class Main {
	public static void main(String[] args) {
		JFrame frame = new MainFrame();
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {   
            public void windowClosing(WindowEvent e) {   
                System.exit(0);   
            }   
        });   
        frame.setTitle("Elevator Dispatch");   
        frame.setBounds(0,0,1500,800);
        frame.setResizable(false);   
	}
}
