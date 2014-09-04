package application;
	
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sprite.Car;
import sprite.FinishLine;
import sprite.Road;
import sprite.Sprite;
import sprite.SpriteHandler;
import sprite.Wall;
import utils.RestrictiveTextField;


public class Main extends Application 
{
	private static final double MOVEMENT_AMOUNT = 4.5;
	public static final int TOP_BUFFER = 200;
	
	private SpriteHandler sprites = null;
	private Canvas gameCanvas = new Canvas(305,692);
	private Car car;
	private AnimationTimer animTimer;
	private double rotMove = 0;
	private boolean rotationEnabled = false;
	private boolean verticalEnabled = false;	
	private boolean isCarCloseToTop = false;
	private boolean endOfTrack = false;
	private double yMove = 0;
	private double xMove = 0;
	private boolean longGame = false;
	private static boolean gameWon = false;
	private boolean testMode = true;
	
	private String playerName = "";
	
	private static int roadNumberCoefficient = 1; //1 builds zero roads. Decrease to build more rows (see createCarAndRoads())
	
	public void start(Stage primaryStage) 
	{
		try 
		{			
			primaryStage.setTitle("DragFx");
			
			GridPane entryGrid = new GridPane();
			entryGrid.setAlignment(Pos.CENTER);
	        entryGrid.setHgap(10);
	        entryGrid.setVgap(10);
	        entryGrid.setPadding(new Insets(25, 25, 25, 25));
			
	        initialiseEntryPane(entryGrid, primaryStage);
	        
			Scene entryScene = new Scene(entryGrid, 300, 275);
			entryScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(entryScene);
			primaryStage.show();	
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	private void startGame(Stage primaryStage)
	{
		Group gameNode = new Group(gameCanvas);
		Scene gameScene = new Scene(gameNode);	
		
		gameCanvas.setStyle("-fx-text-fill: #FE9A2E;");
		
		if (testMode)
		{
			roadNumberCoefficient = -2;
		}
		else if (longGame)
		{
			roadNumberCoefficient = -10;
		}
		else
		{
			roadNumberCoefficient = -5;
		}
		
		createSprites();				
		drawGame();			
				
		gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() 
		{
			public void handle(KeyEvent event) 
			{
				handleKeyPressed(event);						
			}				
		});
		
		gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() 
		{
			public void handle(KeyEvent event) 
			{
				handleKeyReleased(event);
			}
		});
		
		primaryStage.setScene(gameScene);
		primaryStage.show();			
		
		animTimer = new AnimationTimer() 
		{
			public void handle(long arg0) 
			{
				if (!gameWon)
				{
					updatePosition();
					if (isCarCloseToTop && !endOfTrack)
					{
						scrollScreen();
					}
				}
				else 
				{
					gameScene.setOnKeyPressed(null);
					gameScene.setOnKeyReleased(null);
					car.setXMove(0);
					car.setYMove(0);
				}
			}				
		};
		
		animTimer.start();
	}

	private void initialiseEntryPane(GridPane entryGrid, Stage primaryStage)
	{
		Text scenetitle = new Text("DragFx");
		scenetitle.setId("dragfx-text");
		entryGrid.add(scenetitle, 0, 0, 2, 1);
		
		Label name = new Label("Name:");
		entryGrid.add(name, 0, 1);

		RestrictiveTextField nameTextField = new RestrictiveTextField();
		nameTextField.setMaxLength(12);
		entryGrid.add(nameTextField, 1, 1);
		
		Button btnLong = new Button("Long game");
		btnLong.setDefaultButton(true);
		HBox hbBtnLong = new HBox(10);
		hbBtnLong.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtnLong.getChildren().add(btnLong);
		entryGrid.add(hbBtnLong, 1, 4);
		
		Button btnShort = new Button("Short game");
		btnShort.setDefaultButton(true);
		HBox hbBtnShort = new HBox(10);
		hbBtnShort.setAlignment(Pos.BOTTOM_LEFT);
		hbBtnShort.getChildren().add(btnShort);
		entryGrid.add(hbBtnShort, 0, 4);
		
		final Text errorAction = new Text();
        entryGrid.add(errorAction, 1, 6);
		
		btnLong.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{
				errorAction.setText("");				
				longGame = true;
				
				String name = nameTextField.getText();
				if (!checkNameEntered(errorAction, name))
				{
					return;
				}
				
				playerName = name;
				startGame(primaryStage);
			}
		});
		
		btnShort.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{
				errorAction.setText("");
				
				String name = nameTextField.getText();
				if (!checkNameEntered(errorAction, name))
				{
					return;
				}
				
				if (testMode)
				{
					playerName = "Test subject";
				}
				else
				{
					playerName = name;
				}
				
				startGame(primaryStage);
			}
		});
	}
	
	private boolean checkNameEntered(final Text errorAction, String name)
	{
		if (name.isEmpty())
		{
			errorAction.setFill(Color.FIREBRICK);
			errorAction.setText("You must enter a name!");
			return false;
		}
		else if (name.equals("t"))
		{
			testMode = true;
		}
		
		return true;
	}

	private void handleKeyReleased(KeyEvent event)
	{
		if (event.getCode() == KeyCode.UP) 
		{
			verticalEnabled = false;
		}
		else if (event.getCode() == KeyCode.RIGHT
		  || event.getCode() == KeyCode.LEFT)
		{
			rotationEnabled = false;
			rotMove = 0;
		}
	}
	
	private void handleKeyPressed(KeyEvent event)
	{
		if (event.getCode() == KeyCode.UP) 
		{
			verticalEnabled = true;
		}
		else if (event.getCode() == KeyCode.RIGHT)
		{
			rotationEnabled = true;
			rotMove = 5; 						
		}
		else if (event.getCode() == KeyCode.LEFT)
		{
			rotationEnabled = true;
			rotMove = -5;
		}
	}

	private void createSprites()
	{
		Image carImage = new Image(this.getClass().getResource("CarPixlr.png").toString());
		Image roadImage = new Image(this.getClass().getResource("Road.png").toString());
		Image wallImage = new Image(this.getClass().getResource("Wall.png").toString());
		Image finishLineImage = new Image(this.getClass().getResource("FinishLine.png").toString());
		car = new Car(carImage, 147.5, 650, 0);
		
		sprites = new SpriteHandler();
		sprites.add(car);
		
		//Remember that anything that you want to add here also has to be added to the method that draws the components
		addRoads(roadImage);		
		addWalls(wallImage);		
		checkWallsArePlacedCorrectly();
		addFinishLine(finishLineImage);
	}

	private void addFinishLine(Image finishLineImage)
	{
		FinishLine finishLine = new FinishLine(0, (roadNumberCoefficient+1)*Road.HEIGHT + Car.HEIGHT, finishLineImage);
		sprites.add(finishLine);
	}

	private void checkWallsArePlacedCorrectly()
	{
		boolean wallsInValidPositions = false;
		while (!wallsInValidPositions)
		{
			wallsInValidPositions = sprites.checkWallsArePlacedCorrectly();
		}
	}

	private void addRoads(Image roadImage)
	{
		for (int i=1; i>roadNumberCoefficient; i--)
		{
			Road road = new Road(0, i*Road.HEIGHT, roadImage);
			sprites.add(road);
		}
	}

	private void addWalls(Image wallImage)
	{
		int numberOfWalls = (Math.abs(roadNumberCoefficient) + 2)*2;
		
		for (int i=0; i<numberOfWalls; i++)
		{
			double rndY = Wall.getRandomYCoordinate(roadNumberCoefficient + 1);
			double rndX = Wall.getRandomXCoordinate();
						
			Wall wall = new Wall(rndX, rndY, wallImage);
			sprites.add(wall);
		}
	}
	
	private void scrollScreen()
	{
		sprites.scrollSprites(yMove);
		isCarCloseToTop = false;
		drawGame();
	}
	
	private void drawSprites(GraphicsContext gc)
	{	
		boolean reachedEndOfTrack = true;
		int size = sprites.size();
		for (int i=0; i<size; i++)
		{
			Sprite sprite = sprites.elementAt(i);
			if (sprite instanceof Road)
			{
				Road road = (Road) sprite;
				gc.drawImage(road.getImage(), road.getPosX(), road.getPosY());				
				if (road.getPosY() < 0)
				{
					reachedEndOfTrack = false;
				}
			}
		}
		for (int i=0; i<size; i++)
		{
			Sprite sprite = sprites.elementAt(i);
			if (!(sprite instanceof Road)
			  && !(sprite instanceof Car))
			{
				gc.drawImage(sprite.getImage(), sprite.getPosX(), sprite.getPosY());
			}
		}
		
		if (reachedEndOfTrack)
		{
			endOfTrack = true;
		}
	}

	private void drawGame()
	{
		GraphicsContext gc = gameCanvas.getGraphicsContext2D();
		
		double width = gameCanvas.getWidth();
		double height = gameCanvas.getHeight();
		
		gc.clearRect(0, 0, width, height);
		drawSprites(gc);
		if (gameWon)
		{
			gc.setFont(new Font("Verdana", 20));
			gc.fillText(playerName + " is the winner!", 10, Car.HEIGHT*3);			
		}
		
		double x = car.getPosX() + xMove;
		double y = car.getPosY() - yMove;
		
		gc.save();
		gc.translate(x + (Car.WIDTH)/2, y + (Car.HEIGHT)/2);
		gc.rotate(car.getRotation());
		
		gc.drawImage(car.getImage(), -(Car.WIDTH)/2, -(Car.HEIGHT)/2);
		
		gc.restore();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	private void updatePosition()
	{
		if (verticalEnabled)
		{
			updateVerticalPosition(true);
		}
		
		if (rotationEnabled)
		{
			updateRotation();
		}
		
		boolean inMotion = yMove > 0 || yMove < 0 || xMove < 0 || xMove > 0;
		if (!verticalEnabled && inMotion)
		{
			calculateYMovement();
			calculateXMovement();
			
			car.setXMove(xMove);
			car.setYMove(yMove);
			
			updateVerticalPosition(false);
		}
		
		drawGame();
	}

	private void calculateYMovement()
	{
		double newYMove;
		if (yMove > 0)
		{
			newYMove = yMove-0.25;
			if (newYMove > 0)
			{
				yMove = newYMove;
			}
			else
			{
				yMove = 0;
			}
		}
		else if (yMove < 0)
		{
			newYMove = yMove+0.25;
			if (newYMove < 0)
			{
				yMove = newYMove;
			}
			else
			{
				yMove = 0;
			}
		}
	}
	
	private void calculateXMovement()
	{
		double newXMove;
		if (xMove > 0)
		{
			newXMove = xMove-0.25;
			if (newXMove > 0)
			{
				xMove = newXMove;
			}
			else
			{
				xMove = 0;
			}
		}
		else if (xMove < 0)
		{
			newXMove = xMove+0.25;
			if (newXMove < 0)
			{
				xMove = newXMove;
			}
			else
			{
				xMove = 0;
			}
		}
	}

	private void updateRotation()
	{
		double oldRot = car.getRotation();
		double newRot = oldRot + rotMove;
		car.setRotation(newRot);
	}

	private void updateVerticalPosition(boolean accelerating)
	{
		double rotation = car.getRotation();
		double rotationRadians = Math.toRadians(rotation);
		
		if (accelerating)
		{
			yMove = MOVEMENT_AMOUNT*Math.cos(rotationRadians);
			xMove = MOVEMENT_AMOUNT*Math.sin(rotationRadians);
		}
		else
		{
			yMove = Math.abs(yMove)*Math.cos(rotationRadians);
			xMove = Math.abs(xMove)*Math.sin(rotationRadians);
		}
		car.setYMove(yMove);
		car.setXMove(xMove);
		
		boolean collision = sprites.resolveCollisions();

		double oldY = car.getPosY();
		double newY = oldY - yMove;
		double oldX = car.getPosX();
		double newX = oldX + xMove;
		
		if (!collision)
		{
			if (newY > 0 && newY < gameCanvas.getHeight() - Car.HEIGHT)
			{
				car.setPosY(newY);
			}
			if (newX > 0 && newX < gameCanvas.getWidth() - Car.HEIGHT)
			{
				car.setPosX(newX);
			}
		}
		
		if (newY < TOP_BUFFER)
		{
			isCarCloseToTop = true;
		}		
	}
	
	public static int getRoadNumberCoefficient()
	{
		return roadNumberCoefficient;
	}
	public static boolean getGameWon()
	{
		return gameWon;
	}
	public static void setGameWon()
	{
		gameWon = true;
	}
}
