// Monster Class
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class Monster{					
	private int type, level, direction;						// the monster's type, level and direction
	private Tile [][] gameGrid = new Tile[26][36];			// the gamegrid (used for pathfinding)
	private Rectangle m_box;								// Rectangle that takes place of the monster to check if monster collides with a future tower
	private double x,y, finalX, finalY, vx, vy, ang, cx, cy;	// monster's cordoaitnes, velocity, angle
	private Tile currentPos, finalPos, activePos;			// Tiles that keeps track of the monster's current position, final position, and the active positon( for pathfinding)
	private int tileX, tileY, tileFinalX, tileFinalY;		// The cordinates of the tiles (top left corner)
	private int steps = 0;									// keeps tracks of the number of steps the mosnter took
	private Image monsterPic;								// pic of the monster
	private boolean monsterMove = false;					// keeps track whether the monster is moving or not
	private GamePanel gPane;								// gamePanel
	private int attribute; 									// tower's attributes
	// Pathfindg:
	private ArrayList<Tile> openList = new ArrayList<Tile>();		// open List
	private ArrayList<Tile> closeList = new ArrayList<Tile>();		// close List
 	private ArrayList<Tile> path = new ArrayList<Tile>();			// Path
 			
 	private static int [][] stats;							// array of ALL the monster's stats
 	private int hp, totalHP;								// monster's currentHP and totalHP
 	private double speed; 									// monster's moving speed
 		
 	private double deadTimer = 0;							// keeps track when the monster dies a timer starts (-1 life)
 	private boolean dead;									// tracks if the monster is dead or not								
 	private int survive = 0;								// keeps track if the monster survived the entire maze or not
 	private double surviveTimer = 0;						// keeps track when the monster survives the entire maze (+1 score)
 	
	public Monster(int type, int level, Tile[][] grid, GamePanel gPane){		// initiate the monster class	
		gridUpdate(grid);									// the monster's grid is updated
		this.type = type;									// variables are assigned
		this.direction = 2;									// not used 
		monsterPic = new ImageIcon("Monsters/Monster"+type+".png").getImage();			// monster image
		m_box = new Rectangle((int)x,(int)y,monsterPic.getWidth(gPane), monsterPic.getHeight(gPane));	// rectangle of the monster
		this.gPane = gPane;									// gamePane is assigned
		init();												// initiate the monster
		if (type != 0){										// as long as the monster type is not 0. Monster type 0 = pathChecker
			if (type <6){									// monster type 1- 5 is ground monster
				attribute = 1;	// 1 = ground
			}
			else{											// monster type 6 is air monster
				attribute = 2;	// 2 = air
			}
				
			hp = stats[type-1][0];							// hp, total hp, speed are assigned
			totalHP = hp;
			speed = (stats[type-1][1])/10.0;
		}		
	}
	public void upgrade(int level){							// upgrades the monster
		this.level = level;									// the game is used to increase monster's HP
		hp += 30*(int)(level/8);							// hp increases relative to level
		//System.out.println(hp);
		totalHP = hp;
	}
	
	public void loseHP(int x){								// monster loses hp
		hp -= x;
		if (hp<=0){											// monster dies if <= 0 hp
			dead = true;			
			hp = 0;
		}		
	}

	public void init(){					// inits the monster's positions	
		if (direction == 2){			// right >> way	
			tileX = random(0,4);		// random starting location is assigned within a given range
			tileY = random(10,15);
			//tileX = 2;
			//tileY = 14;
			
			tileFinalY = tileY;			// position of the finalPosition
			tileFinalX = (gameGrid[0].length) -1;
			
			currentPos = gameGrid[tileY][tileX];			// current position is assigned			
			finalPos = gameGrid[tileFinalY][tileFinalX];	// final position is assigned
			
			x = currentPos.getCentreX() - m_box.getWidth()/2;	// the monster's x and y co ordaitnes (top left) is assigned
			y = currentPos.getCentreY() - m_box.getHeight()/2;
			
			cx = x + m_box.getWidth()/2;		// centre of the monster
			cy = y + m_box.getHeight()/2;			
		}
	}	
	public static void initData() throws IOException{		// reads the monster data from file
		Scanner inFile = new Scanner(new BufferedReader (new FileReader ("Monsters.txt")));
		int n_monster = inFile.nextInt();		// reads the number of monsters
		stats = new int[n_monster][2];
		for (int i=0; i<n_monster; i++){
			for (int n=0; n<2; n++){			
				stats[i][n] = inFile.nextInt();				
			}
		}			
	}
	
	public void reset(){				// resets the monster -> open, close and path arrayLists are cleared
		openList.clear();
		closeList.clear();
		path.clear();			
	}
	
	public int random(int y1, int y2){			// returns random number given the range
		int range = y2-y1;
		return (int)(Math.random()*range+y1);
	}
	public void gridUpdate(Tile [][] grid){		// updates the Grid
		gameGrid = grid;				// note: any modication to the gameGrid will change all gameGrids in all classes		
	}
	
	public void gridUpdate(int x, int y){		// updates the grid (setting the tiles where the tower is to not empty)
		gameGrid[(y-85)/14][(x-3)/14].setEmpty(false);
		gameGrid[(y-85)/14][(x-3)/14+1].setEmpty(false);
		gameGrid[(y-85)/14+1][(x-3)/14].setEmpty(false);
		gameGrid[(y-85)/14+1][(x-3)/14+1].setEmpty(false);	
	}
	public void gridUpdateReset(int x, int y){	// resets the grid given a co ordiante (the tiles there are reset to empty)
		gameGrid[(y-85)/14][(x-3)/14].setEmpty(true);
		gameGrid[(y-85)/14][(x-3)/14+1].setEmpty(true);
		gameGrid[(y-85)/14+1][(x-3)/14].setEmpty(true);
		gameGrid[(y-85)/14+1][(x-3)/14+1].setEmpty(true);		
	}

	public boolean isAlive(){return hp > 0;}	// returns if the monster is alive
	
	public double distance(double x1, double y1, double x2, double y2){	// distance formula
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	public void print(){				// prints the gameGrid nicely
		int count = 0;
		String message = "[";
		String mess = "";
		for (int x=0; x<gameGrid.length; x++){
			for (int y=0; y<gameGrid[0].length; y++){
				if (gameGrid[x][y].getEmpty()==true){
					if (path.contains(gameGrid[x][y])){
						mess = "X";
					}
					else if(gameGrid[x][y]==currentPos){
						mess = "S";
					}
					else{
						mess = "0";
					}					
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
	
	public void pathFind(){				// starts pathFind		
		openList.clear();
		closeList.clear();			
		pathFind(currentPos);			// cals the recursive pathFind
	}
	// recusively uses A Star Pathfinding. FIrst you have a active position then check the squares around it
	// and add them to openList if not in it. Sort the open list. Finds the tile with the lowest Cost and 
	// that becomes the next path in the mosnter's path
	public void pathFind(Tile activePos){	// recursively pathfinds
		steps ++;						// number of steps the monster took - step increases
		boolean corner = true;			// checks if the monster can move through the corners
		if (activePos == finalPos){		// if the monster reaches its final positon, stop pathfinding				
			makePath();					// create the path
		}
		else{	
			Tile tmp = null;			// a temp Tile
			if (openList.contains(activePos)==false){	// if the openist does not contain the activePOsition, add it to the open list
				openList.add(activePos);
			}		
			int xValue = activePos.getSpotX();	// the top left corner of the activePosition TIle
			int yValue = activePos.getSpotY();			
			for (int x= xValue-1; x<= xValue+1; x++){		// goes through the 8 tiles around currentPos
				for (int y= yValue-1; y<= yValue+1; y++){
					corner = true;				// allows the monster to through corners
					if (x>-1 && x < gameGrid[0].length && y>-1 && y < gameGrid.length){
						// however if the two adjacent tiles of the corner that are within the 8 tiles around the current Positon is not empty, then you cant go diagonally to the corner						
						if (x == xValue-1 && y == yValue-1){	
							if (gameGrid[y+1][x].getEmpty()==false || gameGrid[y][x+1].getEmpty()==false){
								corner=false;
							}
						}
						if (x == xValue-1 && y == yValue+1){
							if (gameGrid[y][x+1].getEmpty()==false || gameGrid[y-1][x].getEmpty()==false){
								corner=false;
							}
						}
						if (x == xValue+1 && y == yValue-1){
							if (gameGrid[y][x-1].getEmpty()==false || gameGrid[y+1][x].getEmpty()==false){
								corner=false;
							}
						}
						if (x == xValue+1 && y == yValue+1){
							if (gameGrid[y][x-1].getEmpty()==false || gameGrid[y-1][x].getEmpty()==false){
								corner=false;
							}
						}
						if (attribute == 2){corner = true;};	// if the monster flies, it can go through corners regardless
						if (corner){	// if monster can go through the corner	
							// if the position is not the activePos, the monster flies, the closeList does not contain the Tile or the grid does not have the active postiion, the tile there is empty,	 and the closelist does not contain those tiles					
							/// theres two cases, air units ignore towers and ground units take account of towers (blocks monster)
							if (gameGrid[y][x]!=activePos && attribute == 2 && closeList.contains(gameGrid[y][x]) == false || gameGrid[y][x]!=activePos && gameGrid[y][x].getEmpty() && closeList.contains(gameGrid[y][x]) == false){
								if (openList.contains(gameGrid[y][x])==false){	// if the tile is not in the openList
									openList.add(gameGrid[y][x]);				// add it tot he openList
									gameGrid[y][x].setParent(activePos);		// the tile's parent, Gscore, Hscore, steps, are all assigned
									gameGrid[y][x].resetTotal();				// the total is reset each time
									gameGrid[y][x].setG(x,y,xValue,yValue);
									gameGrid[y][x].setH(finalPos);
									gameGrid[y][x].setSteps(steps);
									
								}
								else{											// if the tile is already in openList, check if it is easier to move to the tile from the current tile or from its assigned parent Tile	
									tmp = gameGrid[y][x];																
									if (tmp.getG()>tmp.returnG(x,y,xValue,yValue)){		// checks the Gscore to decide whether it is a better path to take
										tmp.resetTotal();						// variablse are assigned
										tmp.setG(x,y,xValue,yValue);
										tmp.setH(finalPos);
										tmp.setParent(activePos);
										tmp.setSteps(steps);
																		
									}
								}								
							}											
						}												
					}					
				}			
			}			
			openList.remove(activePos);			// remove the activePositon from openList		
			closeList.add(activePos);			// adds it to the closeList
			Collections.sort(openList);			// sorts the openList accorind to the tile's total value (in ascending order)
			
			if (openList.size() == 0){			// if the openlist is empty, there is no path;
				path.clear();
				//System.out.println("NO PATH");				
			}
			else{								// recursively runs the pathFind using the tile with the lowest total cost in the openList
				pathFind(openList.get(0));
			}			
		}			
	}
			
	public void makePath(){						// makes the monster's path after pathfinding
		path.clear();							// the path clears
		Tile activeTile = finalPos;				// the activeTile becomes the final tile
		while (true){
			path.add(activeTile);				// goes through the tiles by going to each of the tile's parents
			if (activeTile.getParent() == currentPos){		// stops the parent becomes currentPos	
				break;
			}
			else{								// reassigns the activeTile to the activeTile's parent TIle
				activeTile = activeTile.getParent();
			}		
		}
		
		Collections.reverse(path);				// reverses the path becomes the path is backwards at the moment
	}
	
	public boolean hasPath(){					// returns whether the monster has path or not
		return path.size()>0;
	}

	public void move(){							// moves the monster
		if (hasPath() && isAlive()){			// if the monster has path and is alive		
			findVelocity(path.get(0));			// monster's velocity is assigned
			x += vx;							// moves the monster
			y += vy;
			cx = x + m_box.getWidth()/2;		// the centre of the monster is altered
			cy = y + m_box.getHeight()/2;
		if (distance(x+m_box.getWidth()/2,y+m_box.getHeight()/2,path.get(0).getCentreX(),path.get(0).getCentreY())<1){ // if the distance between the monster and the next tile in its path is less than 3, move to the next tile
				currentPos = path.get(0);		// the current position changes			
				path.remove(0);					// removes that position from the path				
				x = currentPos.getCentreX() - m_box.getWidth()/2;		// the centre of the mosnter is altered
				y = currentPos.getCentreY() - m_box.getHeight()/2;
			 				
			}
			m_box = new Rectangle((int)x,(int)y,monsterPic.getWidth(gPane), monsterPic.getHeight(gPane));	// the reactangle of the monster is updated each time the monster moves
			if (x > gameGrid[0][gameGrid[0].length-4].getCentreX()+7 && survive ==0){
				survive = 1;					// if the monster moves path the entire maze, the monster has survived the game				
			}
		}		
	}
	public void findVelocity(Tile destination){		 // finds the velocity of the monster to its destination tile
		int dx = destination.getCentreX();			// finds the distance between the current Tile and the destination Tile
		int dy = destination.getCentreY();				
		double ddx = dx - (x + m_box.getWidth()/2);
		double ddy = dy - (y + m_box.getHeight()/2);
		ang = Math.atan2(ddy,ddx);					// the angle between the current position and the destination Tile is calcualted using atan2
		vx = speed*Math.cos(ang);					
		vy = speed*Math.sin(ang);			
	}

	public void drawMonster(Graphics g){			// draws the monster							
		if (dead || !dead && System.currentTimeMillis()-deadTimer<= 500){	// if the monster is not dead or if it is dead and the timer for death is less than 500 milliseconds					
			g.setColor(Color.black);				
			g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
			g.drawString("+1",(int)x+monsterPic.getWidth(gPane)/2-3, (int)(y-5));	// draws the +1 when a monster dies
			if (dead){	
				dead = false;
				deadTimer = System.currentTimeMillis();
			}
		}		
		// if monster is alive, within the gameScreen:
		if (isAlive() && x >= gameGrid[0][0].getCentreX()-7 && x <= gameGrid[0][gameGrid[0].length-3].getCentreX()+7){
			Graphics2D g2D = (Graphics2D)g;							// Using graphics 2D, the monster is rotated and draw
			AffineTransform saveXform = g2D.getTransform();
			AffineTransform at = new AffineTransform();
			at.rotate(ang,x+monsterPic.getWidth(gPane)/2,y+monsterPic.getHeight(gPane)/2);
			g2D.transform(at);
			g2D.drawImage(monsterPic, (int)x, (int)y, gPane);
			g2D.setTransform(saveXform);			
			
			g.setColor(Color.red);									// draws the monsters's hp bar
			int picWidth = monsterPic.getWidth(gPane);
			g.fillRect((int)x,(int)(y-5),picWidth,2);				
			g.setColor(Color.green);
			g.fillRect((int)x,(int)(y-5),(int)((hp/(totalHP*1.0))*picWidth),2);	
		}
		
		if (survive==1 || survive!= 1 && System.currentTimeMillis()-surviveTimer<= 500){	// if the monster did not surve and the monster has survived for less than 500 milliseconds:						
			g.setColor(Color.red);
			g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
			g.drawString("-1",(int)x+monsterPic.getWidth(gPane)/2-3, (int)(y-5));			// displays the score - 1		
			if (survive==1){
				survive = 2;
				surviveTimer = System.currentTimeMillis();									// surviveTImer tracks the time at this moment
			}
		}	
				
	}
	// retusn the rectangle around the monster
	public Rectangle getBox(){																
		return m_box;
	}
	
	public boolean collide(Rectangle r){		// checks if the monster collides with another rectangle
		double bx = m_box.getX();				// monster box
		double bx2 = bx + m_box.getWidth();		// the monster rectangle's x,y,width,height are assigned
		double by = m_box.getY();
		double by2 = by + m_box.getHeight();
		
		double rx = r.getX();					// potential tower box
		double rx2 = rx + r.getWidth();			// the tower's rectangle's x,y,width,height are assigned
		double ry = r.getY();
		double ry2 = ry + r.getHeight();
		
		if (bx <= rx2 && bx2 >= rx && by <= ry2 && by2 >= ry){	// if it the two rectangles overlap, they collide
			return true;
		}
		else{
			return false;
		}
	}
	// returns values
	public Tile[][] getGameGrid(){return gameGrid;}
	public int getCX(){return (int)cx;}
	public int getCY(){return (int)cy;}
	public int getAttribute(){return attribute;}
	public int getSurvive(){return survive;}
	public int getHP(){return hp;}
	
	// changes the  value of the survive variable (there are different states for survive)
	public boolean survive(){
		if (survive == 1){	
			return true;
		}
		if (survive == 2 || survive == 0){
			return false;
		}
		return true;	
	}
	// to string
	public String toString(){
		return cx+","+cy;
	}

}
