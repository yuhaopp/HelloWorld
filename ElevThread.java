package ElevatorDispatch;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ElevThread extends JPanel implements Runnable{
	
	public int onOff = -1;            //电梯是否在运营状态
	private static int UP = 1,DOWN = -1,ABORT = 0;   //电梯状态
	private static int FLOORNUM = 20;
	private int direction;
	private int currPosition;
	private boolean[] numState;
	private int targPosition;
	private Thread thread;
	
	private Color norNumColor = new Color(190,190,190);
	private Color resNumColor = new Color(18,183,245);
	private Color norFloorColor = new Color(200,200,200);
	private Color resFloorColor = new Color(29,48,124);
	
	JButton[] elevButton;
	JButton[] numButton;	
	JButton on;
	JButton off;
	JLabel stateLog,floorLog;
	
	public ElevThread(){
		direction = ABORT;
		currPosition = 0;
		targPosition = 0;
		numState = new boolean[FLOORNUM];
		for(int i = 0;i < numState.length;i++){
			numState[i] = false;
		}
		
		thread = new Thread(this);
		
		setLayout(new GridLayout(FLOORNUM+2,2));
		elevButton = new JButton[FLOORNUM];
		numButton = new JButton[FLOORNUM];
		
		floorLog = new JLabel("No.",SwingConstants.CENTER);
		stateLog = new JLabel("STOP",SwingConstants.CENTER);
		stateLog.setForeground(new Color(29,48,124));
		floorLog.setForeground(new Color(29,48,124));
		
		this.add(floorLog);
		this.add(stateLog);
		
		MouseListener numListener = new NumButtonAction();
		MouseListener onOffListener = new OnOffButtonAction();
		
		for(int i = elevButton.length-1;i >= 0;i--){
			numButton[i] = new JButton(String.valueOf(i + 1));   
            numButton[i].addMouseListener(numListener);   
            //numButton[i].setForeground(numColor0);   
            numButton[i].setBackground(norNumColor);   
            elevButton[i] = new JButton();   
            elevButton[i].setEnabled(false);   
            elevButton[i].setBackground(norFloorColor);   
            this.add(numButton[i]);   
            this.add(elevButton[i]);
		}
		elevButton[currPosition].setBackground(resFloorColor);
		on = new JButton("ON");
		off = new JButton("OFF");
		on.setBackground(new Color(230,230,230));
		off.setBackground(new Color(255,229,153));
		this.add(on);
		this.add(off);
		on.addMouseListener(onOffListener);
		off.addMouseListener(onOffListener);
	}

	class NumButtonAction extends MouseAdapter implements MouseListener{
		public void mousePressed(MouseEvent e){
			for(int i = 0;i < numButton.length;i++){
				if(e.getSource() == numButton[i]){
					numState[i] = true;
					numButton[i].setBackground(resNumColor);
					if(direction == ABORT){
						targPosition = i;
					}
					if(direction == UP){
						targPosition = getMaxTaskNum();
					}
					if(direction == DOWN){
						targPosition = getMinTaskNum();
					}
				}
			}
		}
	}
	
	class OnOffButtonAction extends MouseAdapter implements MouseListener{
		public void mousePressed(MouseEvent e){
			if(e.getSource() == on){
				on.setBackground(new Color(255,229,153));
				off.setBackground(new Color(230,230,230));
				onOff = 1;
			}
			if(e.getSource() == off){
				off.setBackground(new Color(255,229,153));
				on.setBackground(new Color(230,230,230));
				onOff = -1;
			}
		}
	}
	@Override
	public void run() {
		while(true){
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			if(onOff == 1){
				if(direction == UP || direction == DOWN){
					try{
						Thread.sleep(100);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					direction = ABORT;
				}
				
				if(targPosition > currPosition){
	                direction = UP;   
	                stateLog.setText("UP");   
	                moveUp();   
	                direction = ABORT;   
	                stateLog.setText("STOP"); 
				} else if(targPosition < currPosition){
	                direction = DOWN;   
	                stateLog.setText("DOWN");   
	                moveDown();   
	                direction = ABORT;   
	                stateLog.setText("STOP");  
				}
			}
		}
	}
	
	public void moveUp(){
		int lastPosition = currPosition;
		for(int i = currPosition + 1;i <= targPosition;i++){
			try{
                stateLog.setText("UP");   
                Thread.sleep(600);   
                elevButton[i].setBackground(resFloorColor);
                if(i > lastPosition){
                	elevButton[i-1].setBackground(norFloorColor);
                }
                
                if(numState[i]){
                    stateLog.setText("Open Door");   
                    Thread.sleep(1000);   
                       
                    stateLog.setText("Close Door");   
                    numButton[i].setBackground(norNumColor);   
                    Thread.sleep(1000);
                }
                currPosition = i;
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		clearState();
	}

	private void moveDown(){
		int lastPosition = currPosition;
		for(int i = currPosition-1;i >= targPosition;i--){
			try{
				Thread.sleep(600);
				elevButton[i].setBackground(resFloorColor);
				
				if(i < lastPosition){
					elevButton[i+1].setBackground(norFloorColor);
				}
				
				if(numState[i]){
                    stateLog.setText("Open Door");   
                    Thread.sleep(2000);            
                    stateLog.setText("关门");   
                    numButton[i].setBackground(norNumColor);   
                    Thread.sleep(800);  
				}
				currPosition = i;
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		clearState();
	}

	public Thread getThread() {
		return thread;
	}
	
	private int getMaxTaskNum(){
		int max = 0;
		for(int i = numState.length-1;i >= 0;i--){
			if(numState[i]){
				max = i;
				break;
			}
		}
		return max;
	}
	
	private int getMinTaskNum(){
		int min = 0;
		for(int i = 0; i < numState.length;i++){
			if(numState[i]){
				min = i;
				break;
			}
		}
		return min;
	}
	
	private void clearState(){
		for(int i = 0;i < numState.length;i++){
			if(numState[i]){
				numState[i] = false;
				numButton[i].setBackground(norNumColor);
			}
		}
	}
	
	public int getDirection(){
		return direction;
	}
	
	public int getTargPosition(){
		return targPosition;
	}
	
	public void setDirection(int i){
		direction = i;
	}
	
	public void setTargPosition(int i){
		if(direction == ABORT){
			targPosition = i;
			numState[i] = true;
			if(currPosition > targPosition){
				direction = DOWN;
			}
			if(currPosition < targPosition){
				direction = UP;
			}
		}
		if(direction == UP && i > targPosition){
			targPosition = i;
			numState[i] = true;
		}
		if(direction == DOWN && i < targPosition){
			targPosition = i;
			numState[i] = true;
		}
	}
	
	public boolean isUp(){
		return direction == UP;
	}
	
	public boolean isDown(){
		return direction == DOWN;
	}
	
	public boolean isAbort(){
		return direction == ABORT;
	}
	
	public int getCurrPosition(){
		return currPosition;
	}
	
	public void setDirectionUp(){
		direction = UP;
	}
	
	public void setDirectionDown(){
		direction = DOWN;
	}
	
	public void setDirectionAbort(){
		direction = ABORT;
	}
}
