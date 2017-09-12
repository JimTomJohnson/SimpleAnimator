package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class Main extends Application {

	private static Stage stage;

	private static int canvasSize = 500;

	private static PanedCanvas mainPanedCanvas = new PanedCanvas(canvasSize, canvasSize, Color.WHITE);

	private static ArrayList<PanedCanvas> previewPanedCanvases = new ArrayList<PanedCanvas>();

	private static ArrayList<Frame> frames = new ArrayList<Frame>();

	private static int selectedFrame = 0;
	private static int firstPrevFrame = 0;
	private static int lastPrevFrame = 1;

	private static VBox layout;
	private static VBox previews;

	private static Label lblFrameNumber;
	
	private static Spinner<Double> xSpinner, ySpinner, lengthSpinner, angleSpinner;

	private static double mouseX, mouseY, oldMouseX, oldMouseY;

	private static boolean controlDown = false;
	private static boolean shiftDown = false;
	private static boolean altDown = false;
	private static boolean showGrid = true;
	private static boolean showPoints = true;
	private static boolean showShadow = true;

	private static Point hoverPoint = null;
	private static Point drawPoint = null;
	private static Point selectedPoint = null;

	private static MoveMode moveMode = MoveMode.NONE;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		buildStage(primaryStage);

		drawPreviews();
		drawMainFrame();
	}

	private static void buildStage(Stage s) {

		// CREATE LAYOUT
		layout = new VBox();

		// HEADER PART 1
		HBox headerPart1 = new HBox();
		headerPart1.setSpacing(5);
		headerPart1.setPadding(new Insets(5));
		headerPart1.setMinWidth(canvasSize);
		headerPart1.setMaxWidth(canvasSize);
		Button btnNew = new Button();
		Button btnImport = new Button();
		Button btnExport = new Button();
		Button btnToggleGrid = new Button();
		Button btnTogglePoints = new Button();
		Button btnToggleShadow = new Button();
		Button btnMirrorH = new Button();
		Button btnMirrorV = new Button();
		Button btnRotate = new Button();
		Button btnInsertFrame = new Button();
		Pane spacer = new Pane();
		Pane spacer2 = new Pane();
		Pane spacer3 = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox.setHgrow(spacer2, Priority.ALWAYS);
		HBox.setHgrow(spacer3, Priority.ALWAYS);
		headerPart1.getChildren().addAll(btnNew, btnImport, btnExport, spacer, btnToggleGrid, btnTogglePoints, btnToggleShadow,
				spacer2, btnMirrorH, btnMirrorV, btnRotate, spacer3, btnInsertFrame);

		try {
			btnNew.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/new.png"))));
			btnImport.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/import.png"))));
			btnExport.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/export.png"))));
			btnToggleGrid.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/toggleGrid.png"))));
			btnTogglePoints.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/togglePoints.png"))));
			btnToggleShadow.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/toggleShadow.png"))));
			btnMirrorH.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/mirrorH.png"))));
			btnMirrorV.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/mirrorV.png"))));
			btnRotate.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/rotate.png"))));
			btnInsertFrame.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/insert.png"))));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// HEADER PART 2
		HBox headerPart2 = new HBox();
		headerPart2.setSpacing(5);
		headerPart2.setPadding(new Insets(5));
		headerPart2.setMinWidth(canvasSize / 5);
		headerPart2.setMaxWidth(canvasSize / 5);
		Button btnPrevFrame = new Button();
		Button btnNextFrame = new Button();
		lblFrameNumber = new Label(String.valueOf(selectedFrame + 1));
		StackPane frameNumberPane = new StackPane();
		frameNumberPane.getChildren().add(lblFrameNumber);
		HBox.setHgrow(frameNumberPane, Priority.ALWAYS);
		headerPart2.getChildren().addAll(btnPrevFrame, frameNumberPane, btnNextFrame);
		
		try {
			btnPrevFrame.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/prevFrame.png"))));
			btnNextFrame.setGraphic(new ImageView(new Image(Main.class.getResourceAsStream("/nextFrame.png"))));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ADD HEADER PARTS
		HBox header = new HBox();
		header.getChildren().addAll(headerPart1, headerPart2);
		layout.getChildren().add(header);

		// ADD MAIN CANVAS
		HBox body = new HBox();
		body.getChildren().add(mainPanedCanvas.getPane());
		layout.getChildren().add(body);

		// ADD PREVIEWS
		previews = new VBox();
		previews.setMinSize(canvasSize / 5, canvasSize);
		previews.setMaxSize(canvasSize / 5, canvasSize);
		body.getChildren().add(previews);
		
		// POINT / LINK CONTROL HEADER
		HBox controlHeader = new HBox();
		controlHeader.setSpacing(5);
		controlHeader.setPadding(new Insets(5));
		xSpinner = new Spinner<>(0.0, canvasSize - 1.0, 250.0);
		ySpinner = new Spinner<>(0.0, canvasSize - 1.0, 250.0);
		lengthSpinner = new Spinner<>(0.0, canvasSize * 2.0, 0.0);
		angleSpinner = new Spinner<>(-1.0, 360.0, 0.0);
		xSpinner.setMaxWidth(100);
		ySpinner.setMaxWidth(100);
		lengthSpinner.setMaxWidth(100);
		angleSpinner.setMaxWidth(100);
		Label lblX = new Label("X");
		Label lblY = new Label("Y");
		Label lblLength = new Label("Length");
		Label lblAngle = new Label("Angle");
		StackPane xPane = new StackPane();
		StackPane yPane = new StackPane();
		StackPane lengthPane = new StackPane();
		StackPane anglePane = new StackPane();
		HBox xBox = new HBox();
		HBox yBox = new HBox();
		HBox lengthBox = new HBox();
		HBox angleBox = new HBox();
		xBox.setSpacing(5);
		yBox.setSpacing(5);
		lengthBox.setSpacing(5);
		angleBox.setSpacing(5);
		xPane.getChildren().add(lblX);
		yPane.getChildren().add(lblY);
		lengthPane.getChildren().add(lblLength);
		anglePane.getChildren().add(lblAngle);
		Pane spacer4 = new Pane();
		Pane spacer5 = new Pane();
		Pane spacer6 = new Pane();
		Pane spacer7 = new Pane();
		Pane spacer8 = new Pane();
		HBox.setHgrow(spacer4, Priority.ALWAYS);
		HBox.setHgrow(spacer5, Priority.ALWAYS);
		HBox.setHgrow(spacer6, Priority.ALWAYS);
		HBox.setHgrow(spacer7, Priority.ALWAYS);
		HBox.setHgrow(spacer8, Priority.ALWAYS);
		xBox.getChildren().addAll(xPane, xSpinner);
		yBox.getChildren().addAll(yPane, ySpinner);
		lengthBox.getChildren().addAll(lengthPane, lengthSpinner);
		angleBox.getChildren().addAll(anglePane, angleSpinner);
		controlHeader.getChildren().addAll(spacer4, xBox, spacer6, yBox, spacer5, lengthBox, spacer7, angleBox, spacer8);
		controlHeader.setMaxWidth(canvasSize + canvasSize / 5);
		
		// ADD CONTROL HEADER
		layout.getChildren().add(controlHeader);

		// SHOW STAGE
		Scene scene = new Scene(layout);
		stage = s;
		stage.setScene(scene);
		stage.setResizable(false);
		stage.sizeToScene();
		stage.show();
		stage.setTitle("Simple Animator");
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));

		// ADD DEFAULT FRAME
		frames.add(new Frame());
		previewPanedCanvases.add(new PanedCanvas(canvasSize / 5, canvasSize / 5, Color.WHITE));
		frames.get(0).getPoints().add(new Point(canvasSize / 2, canvasSize / 2));
		
		// DISABLE FOCUS
		for (Node n : headerPart1.getChildren())
			n.setFocusTraversable(false);
		for (Node n : headerPart2.getChildren())
			n.setFocusTraversable(false);
		for (Node n : controlHeader.getChildren())
			n.setFocusTraversable(false);
		xSpinner.setFocusTraversable(false);
		ySpinner.setFocusTraversable(false);
		lengthSpinner.setFocusTraversable(false);
		angleSpinner.setFocusTraversable(false);
		
		// GLOBAL EVENTS
		layout.setOnKeyPressed(e -> layoutKeyPressed(e));
		layout.setOnKeyReleased(e -> layoutKeyReleased(e));

		// MAIN CANVAS EVENTS
		mainPanedCanvas.getPane().setOnMousePressed(e -> mainPaneMousePressed(e));
		mainPanedCanvas.getPane().setOnMouseDragged(e -> mainPaneMouseDragged(e));
		mainPanedCanvas.getPane().setOnMouseReleased(e -> mainPaneMouseReleased(e));
		mainPanedCanvas.getPane().setOnMouseMoved(e -> mainPaneMouseMoved(e));

		// PREVIEW EVENTS
		previews.setOnScroll(e -> previewsScrolled(e));

		// BUTTON EVENTS
		btnNew.setOnAction(e -> newClicked());
		btnImport.setOnAction(e -> importClicked());
		btnExport.setOnAction(e -> exportClicked());
		btnToggleGrid.setOnAction(e -> toggleGridClicked());
		btnTogglePoints.setOnAction(e -> togglePointsClicked());
		btnToggleShadow.setOnAction(e -> toggleShadowClicked());
		btnMirrorH.setOnAction(e -> mirrorHClicked());
		btnMirrorV.setOnAction(e -> mirrorVClicked());
		btnRotate.setOnAction(e -> rotateClicked());
		btnInsertFrame.setOnAction(e -> addFrameClicked());
		btnPrevFrame.setOnAction(e -> prevFrameClicked());
		btnNextFrame.setOnAction(e -> nextFrameClicked());
		
		// SPINNER EVENTS
		xSpinner.valueProperty().addListener(e -> xSpinnerChanged());
		ySpinner.valueProperty().addListener(e -> ySpinnerChanged());
		lengthSpinner.valueProperty().addListener(e -> lengthSpinnerChanged());
		angleSpinner.valueProperty().addListener(e -> angleSpinnerChanged());
	}

	private static void layoutKeyPressed(KeyEvent e) {
		
		if (e.getCode() == KeyCode.CONTROL)
			controlDown = true;

		if (e.getCode() == KeyCode.SHIFT)
			shiftDown = true;

		if (e.getCode() == KeyCode.ALT)
			altDown = true;

		if (moveMode != MoveMode.NONE && moveMode != MoveMode.DRAW)
			setMoveMode();

		if (e.getCode() == KeyCode.DELETE) {

			if (!controlDown) {

				Point tmpPoint = null;

				for (Point p : frames.get(selectedFrame).getPoints()) {

					if (p.getParent() != null)
						continue;

					if (p.getLinks().size() != 1)
						continue;

					tmpPoint = p;
				}

				if (tmpPoint == null)
					return;
				
				if (tmpPoint == selectedPoint)
					selectedPoint = null;

				tmpPoint.getLinks().get(0).setParent(null);
				tmpPoint.getLinks().get(0).getLinks().remove(tmpPoint);
				frames.get(selectedFrame).getPoints().remove(tmpPoint);
				drawMainFrame();

				return;
			}

			if (frames.size() < 2)
				return;

			frames.remove(selectedFrame);

			if (selectedFrame > 0)
				selectedFrame--;

			if (firstPrevFrame > 0)
				firstPrevFrame--;

			if (lastPrevFrame > frames.size())
				lastPrevFrame--;

			drawMainFrame();
		}

		if (e.getCode() == KeyCode.UP) {

			if (controlDown && frames.size() > 1) {

				int swapFrame = selectedFrame > 0 ? selectedFrame - 1 : frames.size() - 1;
				Collections.swap(frames, selectedFrame, swapFrame);
				selectedFrame = swapFrame;

				if (selectedFrame < firstPrevFrame) {

					firstPrevFrame--;
					lastPrevFrame--;
				}

				if (selectedFrame == frames.size() - 1) {

					lastPrevFrame = frames.size();
					firstPrevFrame = frames.size() > 5 ? lastPrevFrame - 5 : 0;
				}

				drawMainFrame();

				return;
			}

			if (shiftDown) {

				for (Point p : frames.get(selectedFrame).getPoints())
					p.moveTo(p.getX(), p.getY() - 1);

				drawMainFrame();

				return;
			}

			if (frames.size() > 1)
				prevFrameClicked();
		}

		if (e.getCode() == KeyCode.DOWN) {

			if (controlDown && frames.size() > 1) {

				int swapFrame = selectedFrame < frames.size() - 1 ? selectedFrame + 1 : 0;
				Collections.swap(frames, selectedFrame, swapFrame);
				selectedFrame = swapFrame;

				if (selectedFrame > lastPrevFrame - 1) {

					firstPrevFrame++;
					lastPrevFrame++;
				}

				if (selectedFrame == 0) {

					firstPrevFrame = 0;
					lastPrevFrame = frames.size() > 5 ? 5 : frames.size();
				}

				drawMainFrame();

				return;
			}

			if (shiftDown) {

				for (Point p : frames.get(selectedFrame).getPoints())
					p.moveTo(p.getX(), p.getY() + 1);

				drawMainFrame();

				return;
			}

			if (frames.size() > 1)
				nextFrameClicked();
		}

		if (e.getCode() == KeyCode.LEFT) {

			if (shiftDown) {

				for (Point p : frames.get(selectedFrame).getPoints())
					p.moveTo(p.getX() - 1, p.getY());

				drawMainFrame();

				return;
			}
		}

		if (e.getCode() == KeyCode.RIGHT) {

			if (shiftDown) {

				for (Point p : frames.get(selectedFrame).getPoints())
					p.moveTo(p.getX() + 1, p.getY());

				drawMainFrame();

				return;
			}
		}
	}

	private static void layoutKeyReleased(KeyEvent e) {

		if (e.getCode() == KeyCode.CONTROL)
			controlDown = false;

		if (e.getCode() == KeyCode.SHIFT)
			shiftDown = false;

		if (e.getCode() == KeyCode.ALT)
			altDown = false;

		if (moveMode != MoveMode.NONE && moveMode != MoveMode.DRAW)
			setMoveMode();
	}

	private static void mainPaneMousePressed(MouseEvent e) {

		mouseX = e.getX();
		mouseY = e.getY();
		
		oldMouseX = mouseX;
		oldMouseY = mouseY;

		Frame frame = frames.get(selectedFrame);
		hoverPoint = getHoverPoint();

		if (hoverPoint == null)
			return;

		// SET POINT TO ANCHOR POINT
		if (e.getButton() == MouseButton.SECONDARY && moveMode == MoveMode.NONE) {

			for (Point p : frame.getPoints())
				p.setUpdated(false);

			hoverPoint.setParent(null);
			hoverPoint.setUpdated(true);

			updateLinks(hoverPoint);

			drawMainFrame();

			return;
		}

		// SET MOVE MODE
		setMoveMode();

		// ADD DRAW POINT
		if (moveMode == MoveMode.DRAW) {

			drawPoint = new Point(mouseX, mouseY, hoverPoint);
			hoverPoint.getLinks().add(drawPoint);
			frame.getPoints().add(drawPoint);
			selectedPoint = drawPoint;
		}
	}

	private static void mainPaneMouseDragged(MouseEvent e) {

		mouseX = e.getX();
		mouseY = e.getY();

		double oldAngle, angle, dist, newX, newY;

		switch (moveMode) {

		case DRAW:

			drawPoint.moveTo(mouseX, mouseY);

			break;

		case FREE:

			hoverPoint.moveTo(mouseX, mouseY);

			break;

		case MOVE:

			hoverPoint.moveTo(mouseX, mouseY);

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);
			hoverPoint.setUpdated(true);

			adjustPoints(hoverPoint, 0);

			break;

		case ANGLE:

			oldAngle = hoverPoint.getAngle();
			angle = getAngle(hoverPoint.getParent(), new Point(mouseX, mouseY));

			newX = hoverPoint.getParent().getX() + hoverPoint.getDist() * Math.cos(angle);
			newY = hoverPoint.getParent().getY() + hoverPoint.getDist() * Math.sin(angle);

			hoverPoint.moveTo(newX, newY);
			hoverPoint.setAngle(getAngle(hoverPoint, hoverPoint.getParent()));

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);
			hoverPoint.setUpdated(true);

			adjustPoints(hoverPoint, hoverPoint.getAngle() - oldAngle);

			break;

		case DIST:

			angle = hoverPoint.getAngle() + Math.toRadians(180);
			dist = getDist(hoverPoint.getParent(), new Point(mouseX, mouseY));

			newX = hoverPoint.getParent().getX() + dist * Math.cos(angle);
			newY = hoverPoint.getParent().getY() + dist * Math.sin(angle);

			hoverPoint.moveTo(newX, newY);

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);
			hoverPoint.setUpdated(true);

			adjustPoints(hoverPoint, 0);

			break;

		case ANGLEDIST:

			oldAngle = hoverPoint.getAngle();

			hoverPoint.moveTo(mouseX, mouseY);
			hoverPoint.setAngle(getAngle(hoverPoint, hoverPoint.getParent()));

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);
			hoverPoint.setUpdated(true);

			adjustPoints(hoverPoint, hoverPoint.getAngle() - oldAngle);

			break;

		case FREEANGLE:

			oldAngle = hoverPoint.getAngle();
			angle = getAngle(hoverPoint.getParent(), new Point(mouseX, mouseY));

			newX = hoverPoint.getParent().getX() + hoverPoint.getDist() * Math.cos(angle);
			newY = hoverPoint.getParent().getY() + hoverPoint.getDist() * Math.sin(angle);

			hoverPoint.moveTo(newX, newY);

			break;

		case FREEDIST:

			angle = hoverPoint.getAngle() + Math.toRadians(180);
			dist = getDist(hoverPoint.getParent(), new Point(mouseX, mouseY));

			newX = hoverPoint.getParent().getX() + dist * Math.cos(angle);
			newY = hoverPoint.getParent().getY() + dist * Math.sin(angle);

			hoverPoint.moveTo(newX, newY);

			break;

		case ROTATE:

			oldAngle = hoverPoint.getAngle();
			angle = getAngle(new Point(mouseX, mouseY), hoverPoint.getParent());

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);

			hoverPoint.getParent().setUpdated(true);

			adjustPointsIgnoreAnchor(hoverPoint.getParent(), angle - oldAngle);

			break;

		default:
			break;
		}

		drawMainFrame();
	}

	private static void mainPaneMouseMoved(MouseEvent e) {

		mouseX = e.getX();
		mouseY = e.getY();

		drawMainFrame();
	}

	private static void mainPaneMouseReleased(MouseEvent e) {

		moveMode = MoveMode.NONE;
		
		if (mouseX == oldMouseX && mouseY == oldMouseY && e.getButton() == MouseButton.PRIMARY) {
			
			if (hoverPoint != selectedPoint) {
				
				selectedPoint = hoverPoint;
				
			} else {
				
				selectedPoint = null;
			}
			
			setSpinnerValues();
		}

		drawMainFrame();
	}
	
	private static void xSpinnerChanged() {
		
		if (selectedPoint == null)
			return;
		
		if (selectedPoint.getX() != xSpinner.getValue()) {
			
			double oldAngle = selectedPoint.getAngle();

			selectedPoint.moveTo(xSpinner.getValue(), selectedPoint.getY());
			
			if (selectedPoint.getParent() != null) {
				
				selectedPoint.setAngle(getAngle(selectedPoint, selectedPoint.getParent()));
				
				for (Point p : frames.get(selectedFrame).getPoints())
					p.setUpdated(false);
				selectedPoint.setUpdated(true);

				adjustPoints(selectedPoint, selectedPoint.getAngle() - oldAngle);
			}
			
			drawMainFrame();
		}
	}
	
	private static void ySpinnerChanged() {
		
		if (selectedPoint == null)
			return;
		
		if (selectedPoint.getY() != ySpinner.getValue()) {
			
			double oldAngle = selectedPoint.getAngle();

			selectedPoint.moveTo(selectedPoint.getX(), ySpinner.getValue());
			
			if (selectedPoint.getParent() != null) {
				
				selectedPoint.setAngle(getAngle(selectedPoint, selectedPoint.getParent()));
				
				for (Point p : frames.get(selectedFrame).getPoints())
					p.setUpdated(false);
				selectedPoint.setUpdated(true);

				adjustPoints(selectedPoint, selectedPoint.getAngle() - oldAngle);
			}
			
			drawMainFrame();
		}
	}
	
	private static void lengthSpinnerChanged() {
		
		if (selectedPoint == null)
			return;
		
		if (selectedPoint.getParent() == null)
			return;
		
		if (selectedPoint.getDist() != lengthSpinner.getValue()) {
			
			double angle = selectedPoint.getAngle() + Math.toRadians(180);

			double newX = selectedPoint.getParent().getX() + lengthSpinner.getValue() * Math.cos(angle);
			double newY = selectedPoint.getParent().getY() + lengthSpinner.getValue() * Math.sin(angle);

			selectedPoint.moveTo(newX, newY);

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);
			selectedPoint.setUpdated(true);

			adjustPoints(selectedPoint, 0);
			
			drawMainFrame();
		}
	}
	
	private static void angleSpinnerChanged() {
		
		if (selectedPoint == null)
			return;
		
		if (selectedPoint.getParent() == null)
			return;
		
		if (moveMode != MoveMode.NONE)
			return;
		
		if (angleSpinner.getValue() < 0)
			angleSpinner.getValueFactory().setValue(angleSpinner.getValue() + 360);
		
		double checkAngle = Math.toRadians(angleSpinner.getValue() - 180);
		
		if (Math.toDegrees(checkAngle) < 0)
			checkAngle += Math.toRadians(360);
		
		if (Math.toDegrees(checkAngle) >= 360)
			checkAngle -= Math.toRadians(360);
		
		boolean compareAngles = ((int)(checkAngle*100)) != ((int)(selectedPoint.getAngle()*100));
		
		if (compareAngles) {
			
			double oldAngle = selectedPoint.getAngle();
			double angle = Math.toRadians(angleSpinner.getValue());

			double newX = selectedPoint.getParent().getX() + selectedPoint.getDist() * Math.cos(angle);
			double newY = selectedPoint.getParent().getY() + selectedPoint.getDist() * Math.sin(angle);

			selectedPoint.moveTo(newX, newY);
			selectedPoint.setAngle(angle + Math.toRadians(180));

			for (Point p : frames.get(selectedFrame).getPoints())
				p.setUpdated(false);
			selectedPoint.setUpdated(true);

			adjustPoints(selectedPoint, selectedPoint.getAngle() - oldAngle);
			
			drawMainFrame();
		}
	}
	
	private static void setSpinnerValues() {
		
		setAngleAndDist();
		
		double x = selectedPoint == null ? 0 : selectedPoint.getX();
		double y = selectedPoint == null ? 0 : selectedPoint.getY();
		double length = selectedPoint == null ? 0 : selectedPoint.getDist();
		double angle = selectedPoint == null ? 0 : Math.toDegrees(selectedPoint.getAngle());
		
		angle += 180;
		if (angle >= 360)
			angle -= 360;
		
		if (selectedPoint != null)
			if (selectedPoint.getParent() == null)
				angle -= 180;
		
		xSpinner.getValueFactory().setValue(x);
		ySpinner.getValueFactory().setValue(y);
		lengthSpinner.getValueFactory().setValue(length);
		angleSpinner.getValueFactory().setValue(angle);
	}

	private static void setMoveMode() {

		setAngleAndDist();

		if (controlDown && !shiftDown && !altDown && moveMode == MoveMode.NONE)
			moveMode = MoveMode.DRAW;
		else if (!controlDown && !shiftDown && !altDown && hoverPoint.getParent() != null)
			moveMode = MoveMode.ANGLE;
		else if (!controlDown && !shiftDown && !altDown && hoverPoint.getParent() == null)
			moveMode = MoveMode.MOVE;
		else if (!controlDown && !shiftDown && altDown && hoverPoint.getParent() != null)
			moveMode = MoveMode.ROTATE;
		else if (!controlDown && shiftDown && !altDown && hoverPoint.getParent() != null)
			moveMode = MoveMode.DIST;
		else if (!controlDown && shiftDown && altDown && hoverPoint.getParent() != null)
			moveMode = MoveMode.ANGLEDIST;
		else if (controlDown && shiftDown && altDown)
			moveMode = MoveMode.FREE;
		else if (controlDown && shiftDown && !altDown && hoverPoint.getParent() != null)
			moveMode = MoveMode.FREEDIST;
		else if (controlDown && !shiftDown && altDown && hoverPoint.getParent() != null)
			moveMode = MoveMode.FREEANGLE;
	}
	
	private static void setAngleAndDist() {
		
		for (Point p : frames.get(selectedFrame).getPoints()) {
			p.setAngle(p.getParent() == null ? 0 : getAngle(p, p.getParent()));
			p.setDist(p.getParent() == null ? 0 : getDist(p, p.getParent()));
		}
	}

	private static void previewsScrolled(ScrollEvent e) {

		if (e.getDeltaY() < 0) {

			nextFrameClicked();

		} else {

			prevFrameClicked();
		}
	}
	
	private static void newClicked() {
		
		frames = new ArrayList<Frame>();
		frames.add(new Frame());
		previewPanedCanvases.clear();
		previewPanedCanvases.add(new PanedCanvas(canvasSize / 5, canvasSize / 5, Color.WHITE));
		frames.get(0).getPoints().add(new Point(canvasSize / 2, canvasSize / 2));
		selectedFrame = 0;
		firstPrevFrame = 0;
		lastPrevFrame = 1;
		selectedPoint = null;
		drawMainFrame();
	}

	private static void importClicked() {

		FileChooser fc = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
		fc.getExtensionFilters().add(filter);
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		File file = fc.showOpenDialog(stage);

		if (file == null)
			return;

		String fileContent = "";

		try {

			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null)
				fileContent += line;
			br.close();

			ArrayList<Frame> importFrames = new ArrayList<Frame>();
			JSONArray json = new JSONArray(fileContent.replaceAll(" ", ""));

			for (int i = 0; i < json.length(); i++) {

				importFrames.add(new Frame());
				JSONArray frameData = json.getJSONArray(i);

				for (int j = 0; j < frameData.length(); j++) {

					double x1 = Double.parseDouble(frameData.getJSONArray(j).get(0).toString());
					double y1 = Double.parseDouble(frameData.getJSONArray(j).get(1).toString());

					importFrames.get(i).getPoints().add(new Point(x1, y1));
				}
			}

			for (int i = 0; i < importFrames.size(); i++) {

				Frame frame = importFrames.get(i);
				JSONArray frameJson = json.getJSONArray(i);
				Point rootPoint = null;

				for (int j = 0; j < frame.getPoints().size(); j++) {

					double parentX = Double.parseDouble(json.getJSONArray(i).getJSONArray(j).get(2).toString());
					double parentY = Double.parseDouble(json.getJSONArray(i).getJSONArray(j).get(3).toString());

					boolean isRootPoint = true;

					for (int k = 0; k < frameJson.length(); k++) {

						double checkX = Double.parseDouble(json.getJSONArray(i).getJSONArray(k).get(0).toString());
						double checkY = Double.parseDouble(json.getJSONArray(i).getJSONArray(k).get(1).toString());

						if (parentX == checkX && parentY == checkY)
							isRootPoint = false;
					}

					if (isRootPoint)
						rootPoint = new Point(parentX, parentY);
				}

				frame.getPoints().add(rootPoint);
			}

			for (int i = 0; i < importFrames.size(); i++) {

				Frame frame = importFrames.get(i);
				JSONArray frameJson = json.getJSONArray(i);

				for (int j = 0; j < frame.getPoints().size() - 1; j++) {

					Point p = frame.getPoints().get(j);

					double parentX = Double.parseDouble(frameJson.getJSONArray(j).get(2).toString());
					double parentY = Double.parseDouble(frameJson.getJSONArray(j).get(3).toString());

					for (int k = 0; k < frame.getPoints().size(); k++) {

						Point checkPoint = frame.getPoints().get(k);

						if (parentX == checkPoint.getX() && parentY == checkPoint.getY()) {

							p.setParent(checkPoint);
							checkPoint.getLinks().add(p);
						}
					}
				}

				for (int j = 0; j < frame.getPoints().size() - 1; j++) {

					Point p = frame.getPoints().get(j);

					if (!p.getLinks().contains(p.getParent()))
						p.getLinks().add(p.getParent());
				}
			}

			frames = importFrames;
			previewPanedCanvases.clear();
			for (int i = 0; i < frames.size(); i++) {
				previewPanedCanvases.add(new PanedCanvas(canvasSize / 5, canvasSize / 5, Color.WHITE));
			}
			selectedFrame = 0;
			firstPrevFrame = 0;
			lastPrevFrame = frames.size() > 5 ? 5 : frames.size();
			selectedPoint = null;
			drawMainFrame();

		} catch (Exception e) {

		}

	}

	private static void exportClicked() {

		JSONArray json = new JSONArray();

		for (Frame f : frames) {

			JSONArray frameData = new JSONArray();

			for (Point p : f.getPoints()) {

				if (p.getParent() == null)
					continue;

				JSONArray pointData = new JSONArray();
				pointData.put(String.valueOf(p.getX()));
				pointData.put(String.valueOf(p.getY()));
				pointData.put(String.valueOf(p.getParent().getX()));
				pointData.put(String.valueOf(p.getParent().getY()));
				frameData.put(pointData);
			}

			json.put(frameData);
		}

		FileChooser fc = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
		fc.getExtensionFilters().add(filter);
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		File file = fc.showSaveDialog(stage);

		if (file == null)
			return;

		try {

			FileWriter writer = new FileWriter(file);
			writer.write(json.toString(4));
			writer.close();

		} catch (Exception e) {

		}
	}

	private static void toggleGridClicked() {

		showGrid = !showGrid;
		drawMainFrame();
	}

	private static void togglePointsClicked() {

		showPoints = !showPoints;
		drawMainFrame();
	}

	private static void toggleShadowClicked() {

		showShadow = !showShadow;
		drawMainFrame();
	}
	
	private static void mirrorHClicked() {
		
		Point anchorPoint = getAnchorPoint();
		
		for (Point p: frames.get(selectedFrame).getPoints()) {
			
			if (p == anchorPoint)
				continue;
			
			p.moveTo(2 * anchorPoint.getX() - p.getX(), p.getY());
		}
		
		drawMainFrame();
	}
	
	private static void mirrorVClicked() {
		
		Point anchorPoint = getAnchorPoint();
		
		for (Point p: frames.get(selectedFrame).getPoints()) {
			
			if (p == anchorPoint)
				continue;
			
			p.moveTo(p.getX(), 2 * anchorPoint.getY() - p.getY());
		}
		
		drawMainFrame();
	}
	
	private static void rotateClicked() {
		
		Point anchorPoint = getAnchorPoint();
		
		for (Point p : frames.get(selectedFrame).getPoints())
			p.setUpdated(false);
		
		setAngleAndDist();
		
		adjustPoints(anchorPoint, Math.toRadians(90));
		
		drawMainFrame();
	}
	
	private static Point getAnchorPoint() {
		
		Point anchorPoint = null;
		
		for (Point p : frames.get(selectedFrame).getPoints()) {
			
			if (p.getParent() == null) {
				
				anchorPoint = p;
				break;
			}
		}
		
		return anchorPoint;
	}

	private static void addFrameClicked() {

		Frame newFrame = new Frame();
		ArrayList<Point> newPoints = newFrame.getPoints();
		ArrayList<Point> oldPoints = frames.get(selectedFrame).getPoints();

		for (int i = 0; i < oldPoints.size(); i++) {

			Point oldPoint = oldPoints.get(i);
			newPoints.add(new Point(oldPoint.getX(), oldPoint.getY()));
		}

		for (int i = 0; i < oldPoints.size(); i++) {

			Point oldPoint = oldPoints.get(i);

			for (int j = 0; j < oldPoints.size(); j++) {

				Point checkPoint = oldPoints.get(j);

				if (!oldPoint.getLinks().contains(checkPoint))
					continue;

				newPoints.get(i).getLinks().add(newPoints.get(j));

				if (checkPoint == oldPoint.getParent())
					newPoints.get(i).setParent(newPoints.get(j));
			}
		}

		previewPanedCanvases.add(new PanedCanvas(canvasSize / 5, canvasSize / 5, Color.WHITE));
		frames.add(selectedFrame, newFrame);
		selectedFrame++;

		if (frames.size() <= 5) {

			lastPrevFrame++;

		} else {

			if (selectedFrame > lastPrevFrame - 1) {

				firstPrevFrame++;
				lastPrevFrame++;
			}
		}
		
		selectedPoint = null;

		drawMainFrame();
	}

	private static void prevFrameClicked() {

		selectedFrame--;

		if (selectedFrame < 0) {

			selectedFrame += frames.size();
			lastPrevFrame = frames.size();
			firstPrevFrame = frames.size() > 5 ? lastPrevFrame - 5 : 0;
		}

		if (selectedFrame < firstPrevFrame) {

			firstPrevFrame--;
			lastPrevFrame--;
		}
		
		selectedPoint = null;

		drawMainFrame();
	}

	private static void nextFrameClicked() {

		selectedFrame++;

		if (selectedFrame == frames.size()) {

			selectedFrame = 0;
			firstPrevFrame = 0;
			lastPrevFrame = frames.size() > 5 ? 5 : frames.size();
		}

		if (selectedFrame > lastPrevFrame - 1) {

			firstPrevFrame++;
			lastPrevFrame++;
		}
		
		selectedPoint = null;

		drawMainFrame();
	}

	private static void adjustPoints(Point p, double relAngle) {

		for (Point c : p.getLinks()) {

			if (c.isUpdated() || c == p.getParent() || c.getParent() == null)
				continue;

			double angle = c.getAngle() + Math.toRadians(180) + relAngle;

			double newX = p.getX() + c.getDist() * Math.cos(angle);
			double newY = p.getY() + c.getDist() * Math.sin(angle);

			c.moveTo(newX, newY);
			c.setAngle(getAngle(c, p));

			c.setUpdated(true);
			adjustPoints(c, relAngle);
		}
	}

	private static void adjustPointsIgnoreAnchor(Point p, double relAngle) {

		for (Point c : p.getLinks()) {

			if (c.isUpdated() || c == p.getParent())
				continue;

			double angle = c.getAngle() + Math.toRadians(180) + relAngle;

			double newX = p.getX() + c.getDist() * Math.cos(angle);
			double newY = p.getY() + c.getDist() * Math.sin(angle);

			c.moveTo(newX, newY);
			c.setAngle(getAngle(c, p));

			c.setUpdated(true);
			adjustPointsIgnoreAnchor(c, relAngle);
		}
	}

	private static void updateLinks(Point p) {

		for (Point c : p.getLinks()) {

			if (c.isUpdated())
				continue;

			c.setParent(p);
			c.setUpdated(true);
			updateLinks(c);
		}
	}

	private static Point getHoverPoint() {

		if (moveMode == MoveMode.DRAW)
			return hoverPoint;

		boolean b1, b2;

		for (Point p : frames.get(selectedFrame).getPoints()) {

			b1 = mouseX >= p.getX() - Point.Size / 2 && mouseX <= p.getX() + Point.Size / 2;
			b2 = mouseY >= p.getY() - Point.Size / 2 && mouseY <= p.getY() + Point.Size / 2;

			if (b1 && b2)
				return p;
		}

		return null;
	}

	private static void drawGrid() {

		if (!showGrid)
			return;

		GraphicsContext gc = mainPanedCanvas.getGc();
		gc.setStroke(Color.GRAY);
		gc.setLineWidth(0.5);

		for (int i = 0; i < 25; i++) {

			gc.strokeLine(0, i * canvasSize / 25 + canvasSize / 50, canvasSize, i * canvasSize / 25 + canvasSize / 50);
			gc.strokeLine(i * canvasSize / 25 + canvasSize / 50, 0, i * canvasSize / 25 + canvasSize / 50, canvasSize);
		}
	}

	private static void clearMainCanvas() {

		GraphicsContext gc = mainPanedCanvas.getGc();
		gc.clearRect(0, 0, canvasSize, canvasSize);
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvasSize, canvasSize);
	}

	private static void drawPreviews() {

		previews.getChildren().clear();

		Pane pane;
		GraphicsContext gc;

		for (int i = firstPrevFrame; i < lastPrevFrame; i++) {

			pane = previewPanedCanvases.get(i).getPane();
			gc = previewPanedCanvases.get(i).getGc();

			gc.clearRect(0, 0, canvasSize / 5, canvasSize / 5);
			gc.setFill(Color.WHITE);
			gc.fillRect(0, 0, canvasSize / 5, canvasSize / 5);

			if (i == selectedFrame) {
				gc.setFill(Color.rgb(0, 100, 255, 0.2));
				gc.fillRect(0, 0, canvasSize / 5, canvasSize / 5);
			}

			drawPreviewFrame(i);

			previews.getChildren().add(pane);
		}
	}

	private static void drawPreviewFrame(int index) {

		GraphicsContext gc = previewPanedCanvases.get(index).getGc();
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		gc.setLineCap(StrokeLineCap.ROUND);

		for (Point p : frames.get(index).getPoints()) {

			if (p.getParent() == null)
				continue;

			double x1 = p.getParent().getX() / 5;
			double y1 = p.getParent().getY() / 5;
			double x2 = p.getX() / 5;
			double y2 = p.getY() / 5;

			gc.strokeLine(x1, y1, x2, y2);
		}
	}

	private static void drawMainFrame() {

		Frame frame = frames.get(selectedFrame);
		GraphicsContext gc = mainPanedCanvas.getGc();
		
		if (moveMode == MoveMode.NONE)
			hoverPoint = getHoverPoint();

		clearMainCanvas();
		drawGrid();
		drawPreviews();

		lblFrameNumber.setText(String.valueOf(selectedFrame + 1));
		
		setSpinnerValues();

		if (frames.size() > 0 && showShadow) {

			Frame shadowFrame = selectedFrame == 0 ? frames.get(frames.size() - 1) : frames.get(selectedFrame - 1);

			for (Point p : shadowFrame.getPoints()) {

				if (p.getParent() == null)
					continue;

				double x1 = p.getParent().getX();
				double y1 = p.getParent().getY();
				double x2 = p.getX();
				double y2 = p.getY();

				gc.setStroke(Color.GRAY);
				gc.setLineWidth(10);
				gc.setLineCap(StrokeLineCap.ROUND);
				gc.strokeLine(x1, y1, x2, y2);
			}
		}

		for (Point p : frame.getPoints()) {

			if (!showPoints)
				break;
			
			if (p == hoverPoint)
				continue;

			gc.setFill(Color.RED);

			if (p.getParent() == null)
				gc.setFill(Color.LAWNGREEN);

			gc.fillOval(p.getX() - Point.Size / 2, p.getY() - Point.Size / 2, Point.Size, Point.Size);
		}
		
		if (hoverPoint != null) {

			gc.setFill(Color.DEEPSKYBLUE);
			gc.fillOval(hoverPoint.getX() - Point.Size / 2, hoverPoint.getY() - Point.Size / 2, Point.Size, Point.Size);
		}

		for (Point p : frame.getPoints()) {

			if (p.getParent() == null)
				continue;

			double x1 = p.getParent().getX();
			double y1 = p.getParent().getY();
			double x2 = p.getX();
			double y2 = p.getY();

			gc.setStroke(Color.BLACK);
			gc.setLineWidth(10);
			gc.setLineCap(StrokeLineCap.ROUND);
			gc.strokeLine(x1, y1, x2, y2);
		}
		
		if (selectedPoint != null) {
			
			gc.setFill(Color.BLUE);
			gc.fillOval(selectedPoint.getX() - Point.Size / 1.35 / 2, selectedPoint.getY() - Point.Size / 1.35 / 2, Point.Size / 1.35, Point.Size / 1.35);
			
			if (selectedPoint.getParent() != null) {
				
				gc.setStroke(Color.BLUE);
				gc.setLineWidth(5);
				gc.strokeLine(selectedPoint.getX(), selectedPoint.getY(), selectedPoint.getParent().getX(), selectedPoint.getParent().getY());
			}
		}
	}

	private static double getDist(Point p1, Point p2) {

		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();

		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	private static double getAngle(Point p1, Point p2) {

		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();

		double angle = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));

		if (angle < 0)
			angle += 360;

		return Math.toRadians(angle);
	}
}
