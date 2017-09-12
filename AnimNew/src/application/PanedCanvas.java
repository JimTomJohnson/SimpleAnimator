package application;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class PanedCanvas {
	
	private Pane pane;
	private Canvas canvas;
	private GraphicsContext gc;
	
	public PanedCanvas(int w, int h) {
		
		pane = new Pane();
		pane.setMinSize(w, h);
		pane.setMaxSize(w, h);
		
		canvas = new Canvas(w, h);
		gc = canvas.getGraphicsContext2D();
		
		pane.getChildren().add(canvas);
	}
	
	public PanedCanvas(int w, int h, Color c) {
		
		pane = new Pane();
		pane.setMinSize(w, h);
		pane.setMaxSize(w, h);
		
		canvas = new Canvas(w, h);
		gc = canvas.getGraphicsContext2D();
		
		gc.setFill(c);
		gc.fillRect(0, 0, w, h);
		
		pane.getChildren().add(canvas);
	}
	
	public Pane getPane() {
		
		return pane;
	}
	
	public Canvas getCanvas() {
		
		return canvas;
	}
	
	public GraphicsContext getGc() {
		
		return gc;
	}
}
