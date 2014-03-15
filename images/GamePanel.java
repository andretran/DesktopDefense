// GamePanel
    
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

// GamePanel - Draws, updates, runs everything
public class GamePanel extends JPanel implements Runnable, MouseMotionListener, MouseListener, ActionListener, KeyListener{	
	private int width, height;					// width and height of the gameScreen
	private int mx,my;							// mouseX, mouseY
	private boolean mb;							// keeps track if the mouse button state
	private Graphics dbg;						// init dbg
	private Image dbImage = null;				// Image
	private Thread runner;						// runner
	private Image bg, blockImage;				// towerBG: 28x28
	private int towerBselect=-1;				// the towerButton that is selected.
	private boolean buildTower = true;			// keeps track whether the player is building a tower or not
	private Rectangle gameBoard = new Rectangle(54,65,402,406);	// rectangel that incases the gameScren
	private ArrayList<Tower> towers = new ArrayList<Tower>();	// arrayList of tower
	private JButton[] towerButtons = new JButton[6];	// array of tower buttons
	private boolean displayStat = false;			// keeps track whether to display Stats or not
	private JButton pause, start, upgrade, sell;	// init the Jbuttons
	private JLabel stat1, stat2, stat3, statBg;		// init the JLabel for the tower stats
	private ImageIcon[] icons = new ImageIcon[6];	// init ImageIcons for the JButton
	private JLabel bgSide = new JLabel(new ImageIcon("images/Background2.png"));
	private int[] towerDmg = {10,5,10,8,60,20};		// array of towerDamages
	private int[] towerRange = {60,80,50,50,40,60}; // array of tower Ranges
	private int[] towerCost = {5,15,50,20,100,50};	// array of tower Costs
	private Tower tmp = null, tmp2 = null; 			// tmp tower keeps track of the tower selected by the player
	private boolean resetTmp = false;				// checks if the tmp tower needs to be reset or not
	private double time;							// tracks time in milisecond
	private Tile[][] gameGrid = new Tile[26][36];	// gameGrid is started
	private ArrayList<Monster> monsters = new ArrayList<Monster>(); // arraylist of monsters
	private Monster pathCheck;						// special monster: a pathChecker
	private boolean noPath = false, monsterPathUpdate = false;	// keeps track if the the pathChecker has path and whether the monsters needs to update their path
	private double pathTimer = Double.MAX_VALUE;	// 	Timer is used to display No Block for 1 second on the screen
	private double pathTimer2 = pathTimer;
	private boolean monsterMove = false;			// check if the monster can move or not
	private boolean paused = false;					// check if the game is paused or not
	private double spawnTimer = Double.MIN_VALUE;	// TImer that keeps track if the monster can spawn or not
	private boolean gameStart = true;					// keeps track that the game just started
	private boolean waiting = false;					// keeps track if the player is waiting or not
	// Player's variables:
	private int level, money, score, life;			// level, monster, score, life
	private ImageIcon label1, label2, label3, label4, label5, label6;
	private boolean []keys = new boolean[256];
	
	private String screen ="startScreen";			// inits all the screens: startScreen, instructionScreen, gameOVerScreen
	Image startScreen, instructionScreen, gameOverScreen;
	private Rectangle play, instruct, back, restart;	// rectangle of the buttons
	
	// First all the variables are initialized, Icons are initialized, etc.
	public GamePanel() throws IOException{			// the GamePanel 
		startScreen = new ImageIcon("images/startscreen.png").getImage();
		instructionScreen = new ImageIcon("images/instruction.png").getImage();
		gameOverScreen = new ImageIcon("images/gameover.png").getImage();
		play = new Rectangle(177,248,153,54);
		instruct = new Rectangle(143,318,226,57);
		back = new Rectangle(184,391,128,48);
		restart = new Rectangle(194,391,137,50);
		
		gameInit();				// inits game data
		
		setLayout(null);
		Tower.initData();
		Monster.initData();
		// pics
		bg = new ImageIcon("images/Background.png").getImage();	
		blockImage = new ImageIcon("images/NoBlocking.png").getImage();
		// mouse
		mb = false;
		
		width = bg.getWidth(this);
		height = bg.getHeight(this);
		
		bgSide.setSize(width-504,height);
		bgSide.setLocation(504,0);
			
		// Initialize button labels
		label1 = new ImageIcon("images/Label1.png");		
		label2 = new ImageIcon("images/Label2.png");		
		label3 = new ImageIcon("images/Label3.png");
		label4 = new ImageIcon("images/Label4.png");
		label5 = new ImageIcon("images/Label5.png");
		label6 = new ImageIcon("images/Label6.png");
		
		// Initialize towerbuttons
		for (int i=0; i<6; i++){
			icons[i] = new ImageIcon("images/tower"+(i+1)+"Button.png");			
			towerButtons[i] = new JButton(icons[i]);			
			towerButtons[i].addActionListener(this);
			towerButtons[i].addKeyListener(this);
			towerButtons[i].setSize(28,28);			
		}
			
		towerButtons[0].setLocation(533,110);			
		towerButtons[0].addMouseListener(new Listener1 ());
		add(towerButtons[0]);
		towerButtons[1].setLocation(533+(1*32),110);			
		towerButtons[1].addMouseListener(new Listener2 ());
		add(towerButtons[1]);	
		towerButtons[2].setLocation(533+(2*32),110);			
		towerButtons[2].addMouseListener(new Listener3 ());
		add(towerButtons[2]);	
		towerButtons[3].setLocation(533+(3*32),110);			
		towerButtons[3].addMouseListener(new Listener4 ());
		add(towerButtons[3]);	
		towerButtons[4].setLocation(533+(4*32),110);			
		towerButtons[4].addMouseListener(new Listener5 ());				
		add(towerButtons[4]);			
		towerButtons[5].setLocation(533+((5-5)*32),146);			
		towerButtons[5].addMouseListener(new Listener6 ());
		add(towerButtons[5]);				
		
		// initialize side buttons
		start = new JButton("Start");
		start.addActionListener(this);
		start.addKeyListener(this);	
		start.setSize(156,30);
		start.setLocation(533,40);
		add(start);
		
		pause = new JButton("Pause");
		pause.addActionListener(this);
		pause.addKeyListener(this);
		pause.setSize(156,30);
		pause.setLocation(533,75);
		add(pause);
		
		upgrade = new JButton("Upgrade");
		upgrade.addActionListener(this);
		upgrade.addKeyListener(this);
		upgrade.setSize(156,30);
		upgrade.setLocation(533,420);		
		add(upgrade);
		
		sell = new JButton("Sell");
		sell.addActionListener(this);
		sell.addKeyListener(this);
		sell.setSize(156,30);
		sell.setLocation(533,460);		
		add(sell);
				
		// stat labels:			
		stat1 = new JLabel();
		stat1.setSize(100,20);
		stat1.setLocation(606,249);		
		add(stat1);
		
		stat2 = new JLabel();
		stat2.setSize(100,20);
		stat2.setLocation(606,276);
		add(stat2);
		
		stat3 = new JLabel();
		stat3.setSize(100,20);
		stat3.setLocation(606,302);
		add(stat3);	
		
		statBg = new JLabel();
		statBg.setSize(169,231);
		statBg.setLocation(533,180);	
		add(statBg);
		
		add(bgSide);	
				
		// setBackground (Color.black);
		setPreferredSize (new Dimension (width, height));		
		setFocusable (true);
		requestFocus ();
			
		this.addMouseListener (this);
		this.addMouseMotionListener(this);	
		this.addKeyListener(this);			
			
	}
	// inits/resets and then inits all game stats (used when the game starts or restarts)
	public void gameInit(){
		level = 1;
		money = 80;
		score = 0;
		life = 20;
		monsters.clear();
		towers.clear();
		// the Grid is initialized
		for (int x=0; x<gameGrid.length; x++){
			for (int y=0; y<gameGrid[0].length; y++){
				gameGrid[x][y] = new Tile(x,y);
			}
		}
		// for the tiles that are not on the active game screen, set their empty as false. Monsters cannot go there.	
		for (int x=0; x<gameGrid.length; x++){
			for (int y = 0; y<gameGrid[x].length; y++){
				if (y < 5 || y > 30){
					if (x > 15 || x < 10){												
						gameGrid[x][y].setEmpty(false);
					}
				}				
			}
		}
		// init the variables
		towerBselect=-1;
		displayStat = false;
		tmp = null;
		tmp2 = null;
		resetTmp = false;
		noPath = false;			
		monsterPathUpdate = false;
		pathTimer = Double.MAX_VALUE;
		pathTimer2 = pathTimer;
		monsterMove = false;
		paused = false;
		spawnTimer = Double.MIN_VALUE;
		gameStart = true;					// keeps track that the game just started
		waiting = false;
		pathCheck = new Monster(0,0,gameGrid, this);		
	}
	// finds the top left x coordinate of the nearest Tower Slot, assuming the given position is the centre of a tower
	public int gridX(int x){		// assumes x is the center of box
		x = x-8;					// forces x to become the x co ordinate of top right corner
		return x-((x-3)%14);
	}
	// same as above except for left y coordinate
	public int gridY(int y){		// assumes y is the center of box
		y = y-8;					// forces y to become the y co ordinate top right corner
		return y-((y-85)%14);
	}
	// checks for JButton actions
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		
		if (source == pause){			// pause/resumes the game		
			if (!paused){
				paused = true;
				pause.setText("Resume");				
			}	
			else if (paused){				
				paused = false;				
				pause.setText("Pause");				
			}			
		}
		if (source == start){			// start the game
			if (!monsterMove){
				monsterMove = true;
			}
		}	
	
		
		for (int i=0; i<towerButtons.length; i++){	// checks if the player pressed the TowerButtons
			if (source == towerButtons[i]){
				if (money - towerCost[i]>=0){
					towerBselect = i+1;				// the selected tower is set
					resetTmp = true;					
				}		
				break;
			}
		}
		
		if (source == upgrade){		// upgrades the selected tower
			upgrade();
		}
		if (source == sell){		// sell the selected tower
			sell();			
		}		
	}
	// upgrades tower, player loses money for upgrade cost
	public void upgrade(){	
		if (tmp != null && money - tmp.getUpCost()>= 0){
			money -= tmp.getUpCost();
			tmp.upgrade();			
		}
	}
	// sells the tower, play gains 30% of the tower's worth. The tiles the tower occupy are reset to Empty=true
	public void sell(){
		if (tmp != null){
			towers.remove(tmp);
			int x = tmp.getTBX();
			int y = tmp.getTBY();
			money += (int)(0.3*tmp.getCost());
			gameGrid[(y-85)/14][(x-3)/14].setEmpty(true);
			gameGrid[(y-85)/14][(x-3)/14+1].setEmpty(true);
			gameGrid[(y-85)/14+1][(x-3)/14].setEmpty(true);
			gameGrid[(y-85)/14+1][(x-3)/14+1].setEmpty(true);	
			resetTmp = true;
			displayStat = false;
			monsterPathUpdate = true;				
		}
	}
	public void addNotify () {
		super.addNotify ();    // creates the peer
		startGame ();   // start the thread
    }
    
	public void startGame()	{ 				// starts the runner
		runner = new Thread(this);
		runner.start();
	}
	// KEY EVENTS
	public void keyPressed(KeyEvent e){
		keys[e.getKeyCode()] = true;		// the key is pressed, its true		
		pressKey();							// active the keyPRess method 
	}
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;;		// the key is released, set as false
	}
	public void keyTyped(KeyEvent e){
	}
	//MOUSE EVENT	
	public void mouseMoved(MouseEvent e){	// mouse position	
		mx = e.getX();
		my = e.getY();
	}		
	public void mouseReleased(MouseEvent e) {	// mouse is released
	    mb = false;
    }    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    // controls moouse activity. Functions:
    // Changes between screens. 
    // Select towers in game.
    public void mouseClicked(MouseEvent e){		// if mouse is clicked
    	int px = gridX(mx);						// get the modified grid coordiante
		int py = gridY(my);
		
    	if (e.getButton()==2){		// not used    		  		    	
    	}
    	if (e.getButton()==3){		// not used     		   		    	
    	}
    	
    	if (e.getButton()==1){		// if left clicked:
    		if (screen.equalsIgnoreCase("startScreen")){	// switch between screens
    			if (play.contains(mx,my)){
    				screen = "play";
    				buttonVisible(true);
    			}    			
    			if (instruct.contains(mx,my)){
    				screen = "instructScreen";
    			}
    		}
    		if (screen.equalsIgnoreCase("instructScreen")){
    			if (back.contains(mx,my)){
    				screen = "startScreen";
    			}
    		}
    		if (screen.equalsIgnoreCase("play") && life <= 0){
    			if (restart.contains(mx,my)){
    				screen = "startScreen";
    				gameInit();
    				
    			}
    		}
    		mb = true;		   		    
		    if (towerBselect < 0){				// if a tower button is not pressed
		    	for (int i=0; i<towers.size(); i++){	// then the player has selected a tower in the gameField
		    		if (towers.get(i).collide(mx,my)){
		    			tmp = towers.get(i);		    			
		    			break;		    				    	
		    		}
		  	  	}
		    }   	
		   	else{								// if a tower button is pressed
		   		if (gameBoard.contains(mx,my)){
		   			pathCheck.gridUpdate(gameGrid);		// pathCheck's grid is updated				   		
			   		pathCheck.reset();					// pathCheck's pathChecking requirements are reset
			   		pathCheck.gridUpdate(px,py);		// updates pathCheck's grid with the potential tower's position
			  	  	pathCheck.pathFind();		  	  	// finds path		  	  	
			  	  	pathCheck.gridUpdateReset(px,py);
			  	  	
			  	  	if (pathCheck.hasPath()){ 			// if has path
			  	  		if (gameBoard.contains(mx,my) && towerBselect > 0){			   			
				   			if (buildTower){			   			
				    			tmp2 = (new Tower(px,py,towerBselect,this));	// build the tower
				    			money -= tmp2.getCost();		// loses money		    		
				    			monsterPathUpdate = true;		// because the player has built a tower, update teh monster's path			    						    			
				  	  		}				  	  	
				  	 	}				  	  
			  	  	}
			  	  	else{								// if does not have path
			  	  		noPath = true;					// display the: "Blocking" icon
			  	  		pathTimer2 = System.currentTimeMillis();
			  	  	}
		   		}
		   		else{									// deselects	   				
				    towerBselect = -1;
				   	resetTmp = true;			   					  
		   		}				  	  			 
		   	}
		   	if (tmp != null){							// if the player selected a tower
		   		if (tmp.collide(mx,my)==false){			// deselect if it the player clicked elsewhere
		   			resetTmp = true;
		   			displayStat = false;
		   		}
		   	}		   			    
    	}    	
	}  
		
    public void mouseDragged(MouseEvent e){}
    	 
    public void mousePressed(MouseEvent e) {
    	mb = true;
    }
    
    // draws everything   
    public void gameRender (){	    	
		if (dbImage == null){
		    dbImage = createImage (504, height);		   
		    if (dbImage == null) {		
				//System.out.println ("dbImage is null");
				return;
		    }
		    else{
		    	dbg = dbImage.getGraphics ();
		    }	    				
		}			
		dbg.drawImage(bg,0,0,this);				
						
		for (Iterator i=towers.iterator(); i.hasNext();){			// draws the towers
			((Tower)i.next()).draw(dbg);		
		}
		
		int px = gridX(mx);
		int py = gridY(my);
		// 
		if (towerBselect>0 && gameBoard.contains(px,py) && gameBoard.contains(px+28,py+28)){			
			// draws range of tower if a tower is selected
			int r = towerRange[towerBselect-1];					// range of tower			
			dbg.setColor(new Color(220,220,220,120));			// draws tower's perimeter
			dbg.fillOval(px+14-r, py+14-r,2*r,2*r);
			dbg.setColor(new Color(220,220,220));
			dbg.drawOval(px+14-r, py+14-r,2*r,2*r);
			
			// the following decides whether the player can build a tower at the specified place after he/she
			// had selected a tower to build
			if (px>=73 && py >= 85 && px+28<= 437 && py+28 <= 449){		
				if (towers.size()>0){							// if the player is building a tower:
					for (Iterator i=towers.iterator(); i.hasNext();){	 
						if (((Tower)i.next()).collide2(px,py)){			// if an already made tower collides with a future tower, player cannot build a tower
							dbg.setColor(new Color(255,0,0,190));		// red
							buildTower = false;							// player cannot build tower
							break;
						}
					else{											// else the tower can be made
						dbg.setColor(new Color(0,255,0,190));		// green	
						buildTower = true;						
						}
					}	
				}
				else{											// else the tower can be made
					dbg.setColor(new Color(0,255,0,190));		// green
					buildTower = true;
				}
				for (int i=0; i<monsters.size(); i++){				// if a monster colllides with a future tower, the tower cannot be made		
					if (monsters.get(i).collide(new Rectangle(px, py, 28, 28))){						
						dbg.setColor(new Color(255,0,0,190));	// red
						buildTower = false;
						break;
					}
				}							
			}		
			
			if (px<73 || py<85 || px+28>437 || py+28>449){			// if the future tower is not entirely on the active game screen, you cant build it	
				dbg.setColor(new Color(255,0,0,190));		// red
				buildTower = false;				
			}
			if (towerBselect > -1){								// if the tower is selected but the player cannot make any more because he/she does not have enough money
				if (money - towerCost[towerBselect-1]< 0){		// tower cannot be build
					dbg.setColor(new Color(255,0,0,190));		// red
					buildTower = false;
				}
			}
						
			dbg.fillRect(px,py,28,28);							// draws the area where the future tower can be
			
		}
		if (tmp != null){										// if the player selected a tower, highlight that tower
			dbg.setColor(new Color(255,140,0,190));
			dbg.fillRect(tmp.getTBX(), tmp.getTBY(), 28,28);
			tmp.drawRange(dbg);
		}		
	
		if (noPath || (pathTimer - pathTimer2) > 0){			// if there is no path to the final point because of the future tower
			noPath = false;										// display "Blocking" for a set amount of time
			pathTimer = System.currentTimeMillis();
			dbg.drawImage(blockImage,220,230,this);	
			if (pathTimer - pathTimer2 >= 1000){				// display the pic for 1 second
				pathTimer = pathTimer2;
			}		
		}
		if (monsterMove){										// if the monsters are moving, draw the monsters
			for (int i=0; i<monsters.size(); i++){
				monsters.get(i).drawMonster(dbg);
			}
		}
		
		for (int i=0; i<towers.size();i++){						// draws each tower's shot(s)
			if (towers.get(i).getShot()!= null){
				towers.get(i).drawShot(dbg);
			}
		}	
		dbg.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));	// draws the current game stats such as: Level, Life, Money, score
		dbg.setColor(Color.white);
		dbg.drawString("Level: "+level,10,30);
		
		int timeDiff;
		if (spawnTimer > 0 && waiting){							// A countdown initiates when the player is waiting for the next wave of enemies
			timeDiff = 10-(int)(System.currentTimeMillis()-spawnTimer)/1000;	
		}
		else{													// if the monsters are moving, the timeDIff is 0
			timeDiff = 0;
		}		
		dbg.drawString("Timer: "+timeDiff,110,30);				// draws the stats:
		
		dbg.drawString("Life: "+life,200,30);
		dbg.drawString("Money: "+money,280,30);
		dbg.drawString("Score: "+score,430,30);
    } 
    
	public void delay(int n){ 					// responsible for delaying the game
		try{
			Thread.sleep(n);
		}
		catch(InterruptedException ex){
			System.out.println(ex);
		}
	}
	public void pressKey(){						// if the specified keys are pressed, do the following actions:
		if (keys[27]){							// ESC key
			resetTmp = true;					// rest everything
			towerBselect = -1;
			displayStat = false;
			//System.out.println(1);
		}
		if (keys[83]){							// S key
			sell();								// sell selected tower
		}
		if (keys[85]){							// U key
			upgrade();							// upgrade selected tower
		}				
	}
	public void gameUpdate(){					// updates everything in the game
		
		if (tmp2!=null){						// add the tower when the player chose to add a tower
			towers.add(tmp2);
			tmp2 = null;
		}
		
		if (resetTmp){							// if player chooses to deselected a tower, deselect the tower
			tmp = null;
			resetTmp = false;
		}
		
	
		for (Iterator i=towers.iterator(); i.hasNext();){	// the tower Rotates		
			((Tower)i.next()).rotate();	
		}
		if (monsterMove){						// The tower shoots
			towerShoot(time);	
		}
		for (Iterator i=towers.iterator(); i.hasNext();){	// move the tower's shot(s)			
			((Tower)i.next()).moveShot();			
		}	
		if (monsterPathUpdate){					// if the player has build/sold a tower, update the grid and the monster's grid
			gridUpdate();
			//print();
			for (int i=0; i<monsters.size(); i++){
				monsters.get(i).gridUpdate(gameGrid);
				monsters.get(i).reset();
				monsters.get(i).pathFind();		// Finds new path
			}
			monsterPathUpdate = false;
		}
		if (monsterMove){						// if the monsters can move
			for (int i=0; i<monsters.size(); i++){
				monsters.get(i).move();			// move the monsters
				if (monsters.get(i).survive()){	// if the monster survived the entire maze, player loses life
					life --;					
				}		
			}
		}			
	}
	// paints the dbImage
	public void paintScreen (){
		Graphics g;
		try	{
		    g = this.getGraphics ();
		    if ((g != null) && (dbImage != null))
			g.drawImage (dbImage, 0, 0, null);
		    Toolkit.getDefaultToolkit ().sync (); // sync the display on some systems
		    g.dispose ();
		}
		catch (Exception e)	{
		    System.out.println ("Graphics error: " + e);
		}	
    } 
	public void towerShoot(double time){	// THis method controls the tower's fire rate. The tower cannot fire multiple shots and has a delay between shots.
		boolean killed = false;				// checks if the tower killed its target
		if (monsterMove){					// if the monster is moving, the tower shoots if applicable
			for (int i=0; i<towers.size(); i++){
				if (towers.get(i).getTarget()!=null){					
					if (towers.get(i).getTarget().isAlive()==false){						
						towers.get(i).setTarget(null);	// if tower killed target, reset its target
						killed = true;									
					}
					else if (towers.get(i).inRange(towers.get(i).getTarget())==false){
						towers.get(i).setTarget(null);
					}
				}			
				towers.get(i).setTime(time);		// sets the tower's timer
				towers.get(i).findTarget(monsters);	// tower finds target if allowed
				towers.get(i).fire();				// fires a shot at the target												
			}
		}
		if (killed){								// if tower killed monster, score and money increase
			score ++;
			money += 2*((level/8)+1);			
		}	
	}
	
	public void gridUpdate(){						// update the gameGrid 
		for (int i=0; i<towers.size(); i++){		// the grid is updated. Whereever a tower exist, the Tiles there are no longer empty
			int tmpX = towers.get(i).getTBX();
			int tmpY = towers.get(i).getTBY();
			
			gameGrid[(tmpY-85)/14][(tmpX-3)/14].setEmpty(false);
			gameGrid[(tmpY-85)/14][(tmpX-3)/14+1].setEmpty(false);
			gameGrid[(tmpY-85)/14+1][(tmpX-3)/14].setEmpty(false);
			gameGrid[(tmpY-85)/14+1][(tmpX-3)/14+1].setEmpty(false);
		}
	}
	
	
	public void print(){						// prints 2D array list nicely
		int count = 0;
		String message = "[";
		String mess = "";
		for (int x=0; x<gameGrid.length; x++){
			for (int y=0; y<gameGrid[0].length; y++){
				if (gameGrid[x][y].getEmpty()==true){
					mess = "0";
				}
				else{
					mess = "1";
				}
				if (x == gameGrid.length-1 && y == gameGrid[0].length-1){
					message += mess+ "]";
				}
				else{
					if (y== gameGrid[0].length-1){
						message += mess+ "]\n[";
					}
					else{
						message = message + mess + ",";
					}
				}				
			}
		}
		System.out.println(message);
	}
	
	public void monsterCheck(){							// checks if there are any monsters that are still active
		int count = 0;
		int total = monsters.size();					// total number of monsters in the wave
		for (int i=0; i<monsters.size(); i++){			
			if (monsters.get(i).isAlive()==false){		// if the mosnter is not alive, counter goes up
				count ++;					
			}
			else if (monsters.get(i).getSurvive()==2){	// if the mosnter has survived the entire maze, counter goes up
				count ++;
			}
		}
		
		if (count==total && total != 0){				// if the counter = to the total and total is not 0
			monsters.clear();							// clears the monster arrayList
			level ++;									// the player advances to next level
			if (!gameStart){							// spawnTimer is set
				spawnTimer = System.currentTimeMillis();				
				waiting = true;							// the player is now waiting for next wave
			}			
		}
	}
	
	public int random(int y1, int y2){					// randomly returns an integer within the given range
		int range = y2-y1;
		return (int)(Math.random()*range+y1);
	}
	
	public void initMonster(){							// inits monster list
		if (gameStart){									// if the game started, set it to false
			gameStart = false;
		}
		if (monsters.size()==0 && System.currentTimeMillis()-spawnTimer >= 10000){	// if the there are no monsters and the Timer is longer than 10 sec
			waiting = false;							// the player is no longer waiting		
			for (int i=0; i<10; i++){					// monster is initialized
				if (level%8 != 7){
					monsters.add(new Monster(level%7,level,gameGrid,this));	// each level has a type of monster
				}
				else if (level%8 == 7){					// but every 7th level, the wave has random monster types
					int type = random(0,6);
					monsters.add(new Monster(type,level,gameGrid,this));					
				}
				monsters.get(i).reset();				// rests the monster
				monsters.get(i).pathFind();				// monsters find path
				monsters.get(i).upgrade(level);			// monster upgrades according the game level
			}
		}
	}
	
	public void statDisplay(){							// decides whether to display the Tower stats or not
		if (!displayStat){
			stat1.setVisible(false);
			stat2.setVisible(false);
			stat3.setVisible(false);
			statBg.setVisible(false);
		}
		else{			
			stat1.setVisible(true);
			stat2.setVisible(true);
			stat3.setVisible(true);
			statBg.setVisible(true);
		}
	}
	
	public void displayUpgrade(int x){					// decides whether to display the upgradeStats or not
		displayStat = true;
		stat1.setText("Cost: +"+tmp.getUpCost());		
	    stat2.setText("Damage: +"+tmp.getUpDmg());
	    stat3.setText("Range: +"+tmp.getUpRange());
		if (x==0){statBg.setIcon(label1);}
		if (x==1){statBg.setIcon(label2);}
		if (x==2){statBg.setIcon(label3);}
		if (x==3){statBg.setIcon(label4);}
		if (x==4){statBg.setIcon(label5);}
		if (x==5){statBg.setIcon(label6);}
	}
	
	public void buttonVisible(boolean b){				// for the starting and instruction screen only
		for (int i=0 ;i<towerButtons.length; i++){		// sets all the buttons either true or false
			towerButtons[i].setVisible(b);
		}
		pause.setVisible(b);
		start.setVisible(b);
		upgrade.setVisible(b);
		sell.setVisible(b);
	}
	
	// main game loop
	public void run(){		
		while (true){
			buttonVisible(false);							// sets all the buttons false
			if (screen.equalsIgnoreCase("startScreen")){	// if the screen is startScreen, draw startscreen							
				this.getGraphics().drawImage(startScreen,0,0,this);							
			}
			if (screen.equalsIgnoreCase("instructScreen")){	// if the screen is instructScreen, draw instructionScreen
				this.getGraphics().drawImage(instructionScreen,0,0,this);
			}		
			if (screen.equalsIgnoreCase("play")){			// if the screen is play, start the game
				while (life>0){
					if (paused){						
						while (paused){
							delay (100);
						}
					}						
						
					statDisplay();							// display stat is able
					monsterCheck();							// checks the monsters
					initMonster();							// init the monster
					time = System.currentTimeMillis();		// time is taken
					gameUpdate();							// update game
					if (tmp != null){						// if a tower is selected,		
						int tower = tmp.getType()-1;		// the tower selected is recorded
						displayUpgrade(tower);				// display the updateStat of the selected tower
					}									
																	
					gameRender();							// Draw everything
					paintScreen();							// paint the graphics	
					delay(30);
				}									
			}
			if (life <= 0){									// if player died, game over and player can choose to restart game
				this.getGraphics().drawImage(gameOverScreen,0,0,this);
			}
								 			
		}
	}
	// below are 6 private mouse listener class used for each of the tower Buttons
	// they detect if the mouse has entered the Button's container and displays the tower's stats
	class Listener1 implements MouseListener {				
			    
	    public void mouseEntered(MouseEvent e) {
	    	displayStat = true;
	    	stat1.setText("Cost: "+towerCost[0]);
	    	stat2.setText("Damage: "+towerDmg[0]);
	    	stat3.setText("Range: "+towerRange[0]);
	    	statBg.setIcon(label1);
	    	
	    }
	    public void mouseExited(MouseEvent e) {
	    	displayStat = false; 	
	    }	    
	    public void mouseClicked(MouseEvent e){}	    	 
	    public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}	
	}
	class Listener2 implements MouseListener {				
			    
	    public void mouseEntered(MouseEvent e) {
	    	displayStat = true;
	    	//System.out.println(1);
	    	stat1.setText("Cost: "+towerCost[1]);
	    	stat2.setText("Damage: "+towerDmg[1]);
	    	stat3.setText("Range: "+towerRange[1]);
	    	statBg.setIcon(label2);
	    }
	    public void mouseExited(MouseEvent e) {
	    	displayStat = false; 	
	    }	    
	    public void mouseClicked(MouseEvent e){}	    	 
	    public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}	
	}
	class Listener3 implements MouseListener {
			    
	    public void mouseEntered(MouseEvent e) {
	    	displayStat = true;	    	
	    	stat1.setText("Cost: "+towerCost[2]);
	    	stat2.setText("Damage: "+towerDmg[2]);
	    	stat3.setText("Range: "+towerRange[2]);
	    	statBg.setIcon(label3);
	    }
	    public void mouseExited(MouseEvent e) {
	    	displayStat = false; 	
	    }	    
	    public void mouseClicked(MouseEvent e){}	    	 
	    public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}	
	}
	class Listener4 implements MouseListener {				
			    
	    public void mouseEntered(MouseEvent e) {
	    	displayStat = true;	    
	    	stat1.setText("Cost: "+towerCost[3]);
	    	stat2.setText("Damage: "+towerDmg[3]);
	    	stat3.setText("Range: "+towerRange[3]);
	    	statBg.setIcon(label4);
	    }
	    public void mouseExited(MouseEvent e) {
	    	displayStat = false; 	
	    }	    
	    public void mouseClicked(MouseEvent e){}	    	 
	    public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}	
	}
	class Listener5 implements MouseListener {				
			    
	    public void mouseEntered(MouseEvent e) {
	    	displayStat = true;	    
	    	stat1.setText("Cost: "+towerCost[4]);
	    	stat2.setText("Damage: "+towerDmg[4]);
	    	stat3.setText("Range: "+towerRange[4]);
	    	statBg.setIcon(label5);
	    }
	    public void mouseExited(MouseEvent e) {
	    	displayStat = false; 	
	    }	    
	    public void mouseClicked(MouseEvent e){}	    	 
	    public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}	
	}
	class Listener6 implements MouseListener {				
			    
	    public void mouseEntered(MouseEvent e) {
	    	displayStat = true;	    	
	    	stat1.setText("Cost: "+towerCost[5]);
	    	stat2.setText("Damage: "+towerDmg[5]);
	    	stat3.setText("Range: "+towerRange[5]);
	    	statBg.setIcon(label6);
	    }
	    public void mouseExited(MouseEvent e) {
	    	displayStat = false; 	
	    }	    
	    public void mouseClicked(MouseEvent e){}	    	 
	    public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}	
	}
}