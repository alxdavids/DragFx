package application;
	
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import sprite.Car;
import sprite.Road;
import sprite.Sprite;
import sprite.SpriteHandler;
import sprite.Wall;


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
	
	private static int roadNumberCoefficient = 1; //1 builds zero roads. Decrease to build more rows (see createCarAndRoads())
	
	public void start(Stage primaryStage) 
	{
		try 
		{			
			Group gameNode = new Group(gameCanvas);
			Scene gameScene = new Scene(gameNode);
			
			roadNumberCoefficient = -3;
			createCarAndRoads();				
			drawGame();			
			
			gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());			
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
					updatePosition();
					if (isCarCloseToTop && !endOfTrack)
					{
						scrollScreen();
					}
				}				
			};
			
			animTimer.start();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
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

	private void createCarAndRoads()
	{
		Image carImage = new Image(this.getClass().getResource("CarPixlr.png").toString());
		Image roadImage = new Image(this.getClass().getResource("Road.png").toString());
		Image wallImage = new Image(this.getClass().getResource("Wall.png").toString());
		car = new Car(carImage, 147.5, 650, 0);
		
		sprites = new SpriteHandler();
		sprites.add(car);
		
		for (int i=1; i>roadNumberCoefficient; i--)
		{
			Road road = new Road(0, i*Road.HEIGHT, roadImage);
			sprites.add(road);
		}
		
		for (int i=0; i<10; i++)
		{
			double rndY = Wall.getRandomYCoordinate(roadNumberCoefficient + 1);
			double rndX = Wall.getRandomXCoordinate();
						
			Wall wall = new Wall(rndX, rndY, wallImage);
			sprites.add(wall);
		}
		
		boolean wallsInValidPositions = false;
		while (!wallsInValidPositions)
		{
			wallsInValidPositions = sprites.checkWallsArePlacedCorrectly();
		}
	}
	
	private void scrollScreen()
	{
		sprites.scrollSprites(yMove);
		isCarCloseToTop = false;
		drawGame();
	}
	
	private void drawRoadAndWalls(GraphicsContext gc)
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
			if (sprite instanceof Wall)
			{
				Wall wall = (Wall) sprite;
				gc.drawImage(wall.getImage(), wall.getPosX(), wall.getPosY());
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
		drawRoadAndWalls(gc);
		
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
}
