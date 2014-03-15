// Shot Class
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
//import java.util.*;

public class Shot{
	// Variables
	private Monster target;						// shots's target
	private double x,y,newX,newY,vx,vy;			// the shot's positions, new positions, velocities
	private int type, timer, rotateAng;			// shots's type, timer and the angle it should rotate
	private Image shot;							// image of the shot
	private int picX, picY;						// the shot image's x and y co ordiantes
	private boolean shotMove;					// keeps track if the shot is moving or not
	private GamePanel gPane;					// the gamePanel
	private double ang2;						// another ang
	private Tower tower;						// the tower that shot the shot
	private double time2;						// another timer
	private boolean type5Shot = false;			// keeps track if tower5 shot its shot (special shot)

	public Shot(int x, int y, int newX, int newY, int type, GamePanel gPane, Tower tower){		// shot fires from x,y to newX,newY, type of tower
		shot = new ImageIcon("images/Shot"+type+".png").getImage();		// variables and images are assigned
		picX = shot.getWidth(gPane);
		picY = shot.getHeight(gPane);
		this.x = x - picX/2;
		this.y = y - picY/2;
		this.newX = newX;
		this.newY = newY;
		this.type = type;
		findVelocity(newX, newY);		
		shotMove = false;
		gPane = gPane;				
		rotateAng = 0;
		this.tower = tower;
		if(type == 5){			
			time2 = System.currentTimeMillis();
			type5Shot = true;		
		}
		
	}
	public void setTarget(Monster m){			// sets the shot's target
		target = m;
	}
	public Monster returnTarget(){				// returns the shot's target
		return target;
	}
	public double distance(double x1, double y1, double x2, double y2){		// distance formula
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	public void findVelocity(int tx, int ty){	// finds the velocity of the shot. The shot moves toward its enemy (it homes) 
		double distance = distance(x,y,tx,ty);	// distance is calcualted
		double dx = tx-x;
		double dy = ty-y;
		ang2 = Math.atan2(dy,dx);				// finds the angle	
		vx = 5*Math.cos(ang2);					// shot moves toward the target
		vy = 5*Math.sin(ang2);	
		if(type == 5){							// if its type 5, the shot does not move
			vx = 0;			
			vy = 0;
		}
		
	}
	public void drawShot(Graphics g){			// Draws the shots
		if (type == 2){							// shot type 2 is drawn differently
			rotateAng ++;						// the shot rotates constantly
			Graphics2D g2D = (Graphics2D)g;		// Graphics 2D is used to rotate the image and then it is drawn onto the screen
			AffineTransform saveXform = g2D.getTransform();
			AffineTransform at = new AffineTransform();
			at.rotate(Math.toRadians(45*rotateAng),x+picX/2,y+picY/2);
			g2D.transform(at);
			g2D.drawImage(shot, (int)x, (int)y, gPane);
			g2D.setTransform(saveXform);
		}
		if(type == 5){							// shot type 5 is drawn differently
			if (type5Shot){						
				g.drawImage(shot,(int)(x),(int)(y),gPane);		// draws the shot
				if (System.currentTimeMillis()-time2 > 800){	// Even after the shot hits the enemy, it is drawn for 800 milliseconds
					type5Shot = false;				// then shots stop drawing
					shotMove = false;
					target.loseHP(tower.getDmg());	// target loses HP			
					target = null;											
				}
			}
			
		}
		else{									// Draws all the other type shots							
			Graphics2D g2D = (Graphics2D)g;	
			AffineTransform saveXform = g2D.getTransform();
			AffineTransform at = new AffineTransform();
			at.rotate(ang2,x+picX/2,y+picY/2);
			g2D.transform(at);
			g2D.drawImage(shot, (int)x, (int)y, gPane);
			g2D.setTransform(saveXform);		
		}
	}
	
	public void move(){							// moves the shots
		if (target != null){					// as long as the shot has a target, move the shot toward it
			int tx = target.getCX();
			int ty = target.getCY();		
			findVelocity(tx, ty);				// finds the velocity
			shotMove = true;
			x += vx;							// shot's position is updated
			y += vy;
			if (shotInRange(tx,ty) && type!= 5){	// if the shot is in range with the target
				shotMove = false;					// the shot hits. Stops moving. target loses HP. target is reset
				target.loseHP(tower.getDmg());
				target = null;				
			}		
		}
		
	}
	public boolean shotInRange(int tx, int ty){		// checks if the shot is in range with the target given its x and y co ordinates
		double dis = distance(x,y,tx,ty);			// distance formula
		if (dis<3){
			return true;
		}
		return false;
	}
	public boolean shotMove(){						// returns whether the shot is moving or not
		return shotMove;
	}
}