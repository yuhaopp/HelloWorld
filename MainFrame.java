package ElevatorDispatch;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class MainFrame extends JFrame implements Runnable{
	private static int FLOORNUM = 20;   //楼层数
	private static int ELEVATORNUM = 5; //电梯数
	private ElevThread[] elevThread;    //每个电梯对应一个线程
	
	Container container;
	JLabel title = new JLabel();
	JPanel floorPanel = new JPanel();
	JButton[] floorButton;
	JButton[] upButton;
	JButton[] downButton;
	JButton upLog,downLog,floorLog;
	
	Color pressedUpDownButton = new Color(18,183,245);
	
	int[] upState;
	int[] downState;
	
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem chooses[] = {
			new JMenuItem("Help"),
			new JMenuItem("Exit")
	};
	
	public MainFrame(){
		container = this.getContentPane();
		container.setLayout(new GridLayout(1,FLOORNUM + 2));
		floorPanel.setLayout(new GridLayout(FLOORNUM + 2, 3));
		floorButton = new JButton[FLOORNUM];
		upButton = new JButton[FLOORNUM];
		downButton = new JButton[FLOORNUM];
		
		upLog = new JButton("UP");
		downLog = new JButton("DOWN");
		floorLog = new JButton("FLOOR");
		upLog.setEnabled(false);
		downLog.setEnabled(false);
		floorLog.setEnabled(false);
		floorPanel.add(upLog);
		floorPanel.add(downLog);
		floorPanel.add(floorLog);
		
		MouseListener upDownListener = new UpDownButtonAction();
		
		for(int i = floorButton.length-1;i >= 0;i--){
			floorButton[i] = new JButton(Integer.toString(i+1));
			floorButton[i].setForeground(new Color(18,183,245));
			floorButton[i].setFont(new Font("New Times",Font.BOLD,13));
			floorButton[i].setEnabled(false);
			upButton[i] = new JButton("⇧");
			upButton[i].addMouseListener(upDownListener);
			upButton[i].setBackground(new Color(237,237,237));
			downButton[i] = new JButton("⇩");
			downButton[i].addMouseListener(upDownListener);
			downButton[i].setBackground(new Color(237,237,237));
			floorPanel.add(upButton[i]);
			floorPanel.add(downButton[i]);
			floorPanel.add(floorButton[i]);
		}
		container.add(floorPanel);
		menuBar = new JMenuBar();
		menu = new JMenu("Options");
		menu.setFont(new Font("New Times", Font.BOLD, 14));
		menu.setForeground(new Color(18,183,245));
		
		for(int i = 0;i < chooses.length;i++){
			menu.add(chooses[i]);
			if(i < chooses.length - 1)
				menu.addSeparator();
            chooses[i].setForeground(new Color(18,183,245));   
            chooses[i].setFont(new Font("New Times", Font.BOLD, 14));  
		}
		
        chooses[0].addActionListener(new ActionListener()   
        {   
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame frame = new HelpFrame();
			}   
        });  
        chooses[1].addActionListener(new ActionListener()   
        {   
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}   
        });
        menuBar.add(menu);
        setJMenuBar(menuBar);
        
		elevThread = new ElevThread[ELEVATORNUM];
		
		for(int i = 0;i < ELEVATORNUM;i++){
			ElevThread elev = new ElevThread();   
            container.add(elev);   
            elev.getThread().start();
            elevThread[i] = elev;
		}
		
		upState = new int[FLOORNUM];
		downState = new int[FLOORNUM];
		
        for (int i = 0; i < upState.length; i++)   
        {   
            upState[i] = 0;   
            downState[i] = 0;   
        }
		
        Thread manageThread = new Thread(this);   
        manageThread.start(); //启动调度线程 
	}
	
	class UpDownButtonAction extends MouseAdapter implements MouseListener{
        public void mousePressed(MouseEvent e){   
            for (int i = 0; i < upButton.length; i++){   
                if (e.getSource() == upButton[i]){   
                    upButton[i].setBackground(new Color(18,183,245));   
                    upState[i] = 1;   
                }   
   
                if (e.getSource() == downButton[i]){   
                    downButton[i].setBackground(new Color(18,183,245));   
                    downState[i] = 1;   
                }   
            }   
        }
	}
	
	public static int getFLOORNUM(){
		return FLOORNUM;
	}
	
	@Override
	public void run() {
		while(true){
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			
			for(int i = 0;i < upState.length;i++){
				if(upState[i] == 1){
					searchUpElev(i);
				}
				if(upState[i] >= 5){
					if(i == elevThread[upState[i]-5].getCurrPosition()){
						upState[i] = 0;
						upButton[i].setBackground(new Color(237,237,237));
					}
				}
			}
			
			for(int i = 0;i< downState.length;i++){
				if(downState[i]==1){
					searchDownElev(i);
				}
				if(downState[i] >= 5){
					if(i == elevThread[downState[i]-5].getCurrPosition()){
						downState[i] = 0;
						downButton[i].setBackground(new Color(237,237,237));
					}
				}
			}
		}	
	}
	
	private boolean searchUpElev(int floor){
		int searchedElev = 0;   
        int distance = FLOORNUM;   
   
        for (int j = 0; j < elevThread.length; j++){   
            if ((elevThread[j].isAbort()   
                || (elevThread[j].isUp() && floor >= elevThread[j].getCurrPosition()))
            		&& elevThread[j].onOff == 1){   
                int temp = Math.abs(floor - elevThread[j].getCurrPosition());   
                if (temp < distance){   
                    searchedElev = j;   
                    distance = Math.abs(floor - elevThread[j].getCurrPosition());   
                }   
            }   
        }   
   
        if (distance != FLOORNUM){   
            upState[floor] = 5 + searchedElev;   
            elevThread[searchedElev].setTargPosition(floor);   
            return true;   
        } else{   
            return false;   
        } 
	}
	
	private boolean searchDownElev(int floor){
		int searchedElev = 0;   
	    int distance = FLOORNUM;
	    for (int j = 0; j < elevThread.length; j++){   
	    	if ((elevThread[j].isAbort()   
	    			|| (elevThread[j].isDown() && floor <= elevThread[j].getCurrPosition()))
	    			&& elevThread[j].onOff == 1){   
	    		int temp = Math.abs(floor - elevThread[j].getCurrPosition());   
	            if (temp < distance){
	            	searchedElev = j;   
	                distance = Math.abs(floor - elevThread[j].getCurrPosition());
	            }
	        }
	    }   
	    if (distance != FLOORNUM){
		    downState[floor] = 5 + searchedElev;   
	        elevThread[searchedElev].setTargPosition(floor);
	        return true;
	    } else{
		    return false;   
		}
	}   
}
