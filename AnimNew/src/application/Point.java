package application;

import java.util.ArrayList;

public class Point {
	
	public static final double Size = 20;
	
	private double xPos, yPos, angle, dist;
	
	private Point parent;
	
	private ArrayList<Point> links = new ArrayList<Point>();
	
	private boolean updated = false;
	
	public Point(double x, double y) {
		
		xPos = x;
		yPos = y;
		parent = null;
	}
	
	public Point(double x, double y, Point p) {
		
		xPos = x;
		yPos = y;
		parent = p;
		links.add(p);
	}
	
	public double getX() {
		
		return xPos;
	}
	
	public double getY() {
		
		return yPos;
	}
	
	public Point getParent() {
		
		return parent;
	}
	
	public ArrayList<Point> getLinks() {
		
		return links;
	}
	
	public boolean isUpdated() {
		
		return updated;
	}
	
	public double getAngle() {
		
		return angle;
	}
	
	public double getDist() {
		
		return dist;
	}
	
	public void moveTo(double x, double y) {
		
		xPos = x;
		yPos = y;
	}
	
	public void setParent(Point p) {
		
		parent = p;
	}
	
	public void setUpdated(boolean b) {
		
		updated = b;
	}
	
	public void setAngle(double a) {
		
		angle = a;
	}
	
	public void setDist(double d) {
		
		dist = d;
	}
}
