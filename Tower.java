// Tower Class

import java.io.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class Tower{
	
	private int tx,ty, tbx, tby, cx, cy; 	// towerX, towerY, towerBackgroundX, toweBackgroundY, centre x, centre y
	private double ang;						// tower angle between tower's cannon and enemy
	private GamePanel gPane;				// game panel
	private Image towerBG, tower;			// tower images
	private int tcx, tcy, type, cannonLength;				// towerCenterX, towerCenterY, towerType
	private boolean spin;					// keeps track if the tower can spin or not
	private Rectangle area;				// used to detect if another tower is building ontop of this	
	private static int[][] stats;		// arrayList of stats of ALL the towers			
	private boolean shooting = false, type5Shoot = false;	// keeps track if the tower is shooting and if it is shoot Type5Shot
	private ArrayList<Shot> shots = new ArrayList<Shot>();	// Arraylist of Shots
	private Shot shot = null;				// tower's shot is set to null
	private double time, oldTime=0;			// timers to help with the tower's delay between firing
	private Monster target = null;			// tower's target
	private int attribute;					// tower's attribute: 1= ground, 2 = air, 3 = both
	private int cost, dmg, range, delay;	// tower's stats
	private int upCost, upDmg, upRange;		// tower's upgraded stats
	

	public Tower(int tbx, int tby, int type, GamePanel pane){		// all variables are intialized and assigned
		this.tbx = tbx;
		this.tby = tby;
		this.type = type;
		area = new Rectangle(tbx,tby,28,28);
		gPane = pane;
		loadPics();							// images are loaded	
		// variables are assigned from the stats array:		
		tx = tbx + stats[type-1][0];		
		ty = tby + stats[type-1][1];
		tcx = stats[type-1][2];
		tcy = stats[type-1][3];
		if (1 == stats[type-1][4]){
			spin = true;
		}
		if (0 == stats[type-1][4]){
			spin = false;
		}	
		range = stats[type-1][5];
		cannonLength = stats[type-1][6];		
		attribute = stats[type-1][7];
		delay = stats[type-1][8];
		dmg = stats[type-1][9];
		cost = stats[type-1][10];
		
		cx = tbx + 14;						// centre of the tower is assigned
		cy = tby + 14;	
		
		upCost = (int)(cost*1.7);			// the upgraded tower's values are calculated
		upDmg = (int)(dmg*1.5);
		if (type != 5){						// type 5 tower's range does not change
			upRange = (int)(range*1.1);
			if (upRange > 150){
				upRange = 150;
			}
		}
		else{
			upRange = range;
		}	
	}
	
	public void upgrade(){					// upgrades the tower
		cost = upCost;						// the tower's stats is upgraded
		dmg = upDmg;
		range = upRange;		
		upCost = (int)(cost*1.7);			// next upgrade is calcualted
		upDmg = (int)(dmg*1.15);
		if (type != 5){						// type 5 tower's range does not change
			upRange = (int)(range*1.1);
			if (upRange > 150){				// range cannot go pass 150
				upRange = 150;
			}
		}
		else{
			upRange = range;
			//System.out.println(1);
		}	
		
	}
	// Read Towers2.txt to identify the order of the stats
	public static void initData() throws IOException{		// Towers2.txt is read. Stats are assigned into stats array
		Scanner inFile = new Scanner(new BufferedReader (new FileReader ("Towers.txt")));
		int n_tower = inFile.nextInt();			// total number of towers
		stats = new int[n_tower][11];
		for (int i=0; i<n_tower; i++){
			for (int n=0; n<11; n++){			
				stats[i][n] = inFile.nextInt();
			}
		}			
	}

	
	public void setTime(double t){			// sets the time the tower shoots	
		time = t;
	}
	public Shot getShot(){					// returns shot
		return shot;
	}
	
	// controls the tower's fire rate. The tower cannot fire 2 shots at the same time and there is delay between shots
	public void fire(){						// method controls the firing of the tower
		if (target != null){			
			int tx = target.getCX();		// target is assigned
			int ty = target.getCY();
			if (!shooting && distance(tbx+12,tby+12,tx,ty)<= getRange()){	// if not shooting and in range
				int diff = (int)(time-oldTime);
				
				if (diff>=delay){			// if the tower has waited long enough between shots, fire!
					shooting = true;		// tower is shooting
					if (type != 5 && type != 6){	// shots 5 and 6 have different type of shot
						shots.clear();
						shot = new Shot((int)(cannonLength*Math.cos(ang)+cx),(int)(cannonLength*Math.sin(ang)+cy),tx,ty,type,gPane,this);	// Change 12
						shot.setTarget(target);		// the shot is assigned a target
						shots.add(shot);			// add the shot to the tower's arraylist of shots
					}
					if (type == 6){			// if its tower6: 4 shots are shot, each shot is assigned the target						
						shots.clear();
						shot = new Shot(cx-6,cy,tx,ty,type,gPane,this);	
						shot.setTarget(target);						
						shots.add(shot);
						shot = new Shot(cx+6,cy,tx,ty,type,gPane,this);	
						shot.setTarget(target);						
						shots.add(shot);
						shot = new Shot(cx,cy-8,tx,ty,type,gPane,this);	
						shot.setTarget(target);						
						shots.add(shot);
						shot = new Shot(cx,cy+8,tx,ty,type,gPane,this);	
						shot.setTarget(target);						
						shots.add(shot);
					}
					if (type == 5){			// if its tower 5: the shot does not move
						shots.clear();
						shot = new Shot((int)(cx),(int)(cy),tx,ty,type,gPane,this);	
						shot.setTarget(target);	
						shots.add(shot);															
					}					
				}
			}
		}
	
	}
	public void moveShot(){				// moves the shot			
		if (shooting && shot!= null){	// if firing:
			for (int i=0; i<shots.size(); i++){	// move each shot
				shots.get(i).move();
			}			
			if (shot.shotMove()==false){		// if the shot stopped moving (because the shot hit the target)							
				shooting = false;				// the tower is no longer shooting
				shot = null;
				oldTime = System.currentTimeMillis();	// time is remembered for delay						
			}
		}		
	}
	
	public void drawRange(Graphics g){			// draws the tower's range	
		g.setColor(new Color(220,220,220,120));			// draws tower perimeter
		g.fillOval(tbx+14-range, tby+14-range,2*range,2*range);
		g.setColor(new Color(220,220,220));
		g.drawOval(tbx+14-range, tby+14-range,2*range,2*range);
		
	}	
	
	public boolean inRange(Monster m){			// checks if the monster is in range with the tower			
		int x = m.getCX();
		int y = m.getCY();
		if (distance(x,y,cx,cy)<= range){		// if it is return true else false
			return true;
		}
		else{			
			return false;		
		}		
	}	
		
	public boolean collide(int x, int y){		// checks if the tower collides/overlaps with a point
		if (area.contains(x,y)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean collide2(int x, int y){		// another collision method, checks if the tower collides with another future tower
		if (area.contains(x,y) ||area.contains(x+27,y) || area.contains(x+27,y+27)|| area.contains(x,y+27)){
			return true;
		}
		else{
			return false;
		}
	}

	public void loadPics(){						// all pics are loaded
		tower = new ImageIcon("images/tower"+type+".png").getImage();
		towerBG = new ImageIcon("images/towerBG.png").getImage();
	}
	
	public void rotate(){						// rotates the cannon
		if (target!= null){					
			int x = target.getCX();
			int y = target.getCY();
			ang = Math.atan2(y-ty,x-tx);		// angle is calcualted
		}	
		
	}
	public void drawShot(Graphics g){			// each of the tower's shots are drawn
		for (int i=0; i<shots.size(); i++){
			shots.get(i).drawShot(g);
		}
	}
	public void draw(Graphics g){				// if the tower does not spin, its angle is always set to 0
		if (!spin){
			ang = 0;
		}		
		g.drawImage(towerBG, tbx, tby, gPane);		// Tower background		
	
		Graphics2D g2D = (Graphics2D)g;				// tower's cannon is rotated and drawn using graphics 2D
		AffineTransform saveXform = g2D.getTransform();	
		AffineTransform at = new AffineTransform();
		at.rotate(ang,tx+tcx,ty+tcy);
		g2D.transform(at);
		g2D.drawImage(tower, (int)tx, (int)ty, gPane);
		g2D.setTransform(saveXform);
			
	}	
	public double distance(double x1, double y1, double x2, double y2){	// distance formula
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	public int getTX(){return tx;}					// the follow methods returns the private variables
	public int getTY(){return ty;}
	public int getTBX(){return tbx;}
	public int getTBY(){return tby;}
	public int getDmg(){return dmg;}
	public int getCost(){return cost;}
	public int getType(){return type;}
	public int getUpRange(){return upRange-range;}
	public int getUpCost(){return upCost-cost;}
	public int getUpDmg(){return upDmg-dmg;}
	public int getRange(){return range;} 				// returns range
	public Monster getTarget(){return target;}			// returns target
		
	public void findTarget(ArrayList<Monster> monsters){	// allows the tower to find a target through the list of monsters
		if (target == null){							// only search for target when the tower does not already have one
			for (int i=0; i<monsters.size(); i++){
				if (inRange(monsters.get(i))&& monsters.get(i).isAlive()){
					int attri = monsters.get(i).getAttribute();		// the tower's target's attribute must not conflict with the tower's attribute (e.g. air tower cannot atk ground units)
					if (attri == attribute || attribute == 3){
						target = monsters.get(i);
						rotate();
						break;
					}
									
				}
			}	
		}	
	}	
			
	public void setTarget(Monster m){				// sets the Tower's target
		target = m;
	}
}