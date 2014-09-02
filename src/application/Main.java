package application;
	
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


public class Main extends Application 
{
	private static final double MOVEMENT_AMOUNT = 2.5;
	
	private Canvas gameCanvas = new Canvas(1280,720);
	private Car car;
	private AnimationTimer animTimer;
	private double rotMove = 0;
	private boolean rotationEnabled = false;
	private boolean longitudinalEnabled = false;	
	
	public void start(Stage primaryStage) 
	{
		try 
		{			
			Group gameNode = new Group(gameCanvas);
			Scene gameScene = new Scene(gameNode);
			
			Image carImage = new Image(this.getClass().getResource("CarPixlr.jpg").toString());
			car = new Car(carImage, 640, 360, 0);
			
			drawCar();			
			
			gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() 
			{
				public void handle(KeyEvent event) 
				{
					if (event.getCode() == KeyCode.UP) 
					{
						longitudinalEnabled = true;
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
			});
			
			gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() 
			{
				public void handle(KeyEvent event) 
				{
					if (event.getCode() == KeyCode.UP) 
					{
						longitudinalEnabled = false;
					}
					else if (event.getCode() == KeyCode.RIGHT
					  || event.getCode() == KeyCode.LEFT)
					{
						rotationEnabled = false;
						rotMove = 0;
					}
				}
			});
			
			primaryStage.setScene(gameScene);
			primaryStage.show();
			
			animTimer = new AnimationTimer() 
			{
				public void handle(long arg0) 
				{
					updatePosition();
				}
			};
			
			animTimer.start();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	private void drawCar()
	{
		double width = gameCanvas.getWidth();
		double height = gameCanvas.getHeight();
		GraphicsContext gc = gameCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, width, height);
		
		double x = car.getPosX();
		double y = car.getPosY();
		
		gc.save();
		gc.translate(x + (Car.WIDTH)/2, y + (Car.HEIGHT)/2);
		gc.rotate(car.getRotation());
		
		gc.drawImage(car.getCarImage(), -(Car.WIDTH)/2, -(Car.HEIGHT)/2);
		
		gc.restore();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	private void updatePosition()
	{
		if (longitudinalEnabled)
		{
			updateLogitudinalPosition();
		}
		
		if (rotationEnabled)
		{
			updateRotation();
		}

		drawCar();
	}

	private void updateRotation()
	{
		double oldRot = car.getRotation();
		double newRot = oldRot + rotMove;
		car.setRotation(newRot);
	}

	private void updateLogitudinalPosition()
	{
		double rotation = car.getRotation();
		double rotationRadians = Math.toRadians(rotation);
		
		double yMove = MOVEMENT_AMOUNT*Math.cos(rotationRadians);
		double xMove = MOVEMENT_AMOUNT*Math.sin(rotationRadians);

		double oldY = car.getPosY();
		double newY = oldY - yMove;
		car.setPosY(newY);

		double oldX = car.getPosX();
		double newX = oldX + xMove;
		car.setPosX(newX);
	}
}
