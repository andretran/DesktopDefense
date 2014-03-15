// Tile Class for Pathfinding

public class Tile implements Comparable{
	private int cx, cy;		// centre x of the tile, centre y of the tile
	private boolean empty = true;	// keeps track if the tile is empty or not
	private int g_cost=0, h_cost=0, steps, total;	// movement cost from tile to next tile, movement cost from the next tile to the final tile, total cost 
	private int horiMove = 10, diaMove = 14;	// movement costs for g_cost
	private Tile parent;			// keeps track of parent tile
	private int spotX, spotY;		// the x and y position of the tile on the coordinate plane
		
	public Tile(int spotY, int spotX){	// tile is initialized, variables established
		cx = spotX*14+11;
		cy = spotY*14+93;
	
		this.spotX = spotX;
		this.spotY = spotY;
		parent = null;
	}
	public void setSteps(int step){		// step value - number of steps the monster took to get to this tile
		steps = step*20;				// the step value is weighed slightly higher than g or h costs
		total += steps;					// I modified the pathfinding by adding # of steps the monster took. This is weighted so that the path is the shortest
	}
	public void setEmpty(boolean emp){	// sets the tile as empty or not
		empty = emp;
	}
	public boolean getEmpty(){			// returns whether the tile is empty or not
		return empty;
	}
	public void resetTotal(){			// reset the tile's total value
		total = 0;
	}
	public void setG(int x, int y, int xValue, int yValue){		// set G score
		if (x== xValue - 1 && y == yValue - 1 || x == xValue+1 && y == yValue -1 || x == xValue - 1 && y == yValue + 1 || x == xValue +1 && y == yValue +1){
			g_cost = 14;				// if its diagonal movement, g_cost = 14
		}
		else{
			g_cost = 10;				// else g_cost = 10;
		}
		total += g_cost;				// total is calcualted
	}
	public int returnG(int x, int y, int xValue, int yValue){	// returns a potential G value given the parameters
		int cost = g_cost;
		if (x== xValue - 1 && y == yValue - 1 || x == xValue+1 && y == yValue -1 || x == xValue - 1 && y == yValue + 1 || x == xValue +1 && y == yValue +1){
			cost = 14;
		}
		else{
			cost = 10;
		}
		return cost;
	}
	public int getG(){return g_cost;}		// returns the costs
	public int getH(){return h_cost;}
	public int getSteps(){return steps;}
	
	
	public double distance(double x1, double y1, double x2, double y2){	// distance formula
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	public void setH(Tile end){				// sets the H score
		int diffX = Math.abs(spotX - end.getSpotX());		// the verticle and horizonal distance between the 2 tiles are calculated
		int diffY = Math.abs(spotY - end.getSpotY());		
		
		h_cost = diffX + diffY;
		h_cost *= 10;						// h_cost is adjusted to have a bigger impact on the path
		total += h_cost;					// total is calculated
	}
	
	public void setTotal(int t){			// Set the tile' total
		total = t;
	}
	
	public int getTotal(){					// returns total
		return total;
	}
	
	public void setParent(Tile t){			// sets parent
		parent = t;
	}
	public int getSpotX(){return spotX;}	// returns variables
	public int getSpotY(){return spotY;}
	public int getCentreX(){return cx;}
	public int getCentreY(){return cy;}

	
	public int compareTo(Object other){		// comapre two tiles by their Total score
		Tile otherTile = (Tile)(other);
		int value1 = total;
		int value2 = otherTile.getTotal();
		return value1-value2;	
	}
	public Tile getParent(){				// returns the parent
		return parent;
	}
	public String toString(){				// help display the Tile
		return spotY+","+spotX;
	}	
}