package sprite;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import utils.UpwardProgress;
import application.Main;
import application.Player;

public class Car extends Sprite
{
	public enum Dimension {
		WIDTH(30), HEIGHT(37);
		
		private double value;
		
		private Dimension(double value)
		{
			this.value = value;
		}	
		public double getValue()
		{
			return value;
		}
	}
	public enum CanvasDimension {
		GAME_CANVAS_WIDTH(305), GAME_CANVAS_HEIGHT(692);
		
		private double value;
		
		private CanvasDimension(double value)
		{
			this.value = value;
		}
		public double getValue()
		{
			return value;
		}
	}
	public enum Speed {
		SLOW_MOVEMENT_SPEED(2), NORMAL_MOVEMENT_SPEED(4.5), BOOST_MOVEMENT_SPEED(6.5);
		
		private double value;
		
		private Speed(double value)
		{
			this.value = value;
		}
		public double getValue()
		{
			return value;
		}
	}
	
	private double rotation;
	private double xMove = 0;
	private double yMove = 0;
	private double rotMove = 0;
	private boolean verticalEnabled = false;
	private boolean rotationEnabled = false;
	private boolean carCloseToTop = false;
	private ProgressIndicator bar = null;
	private Player player = null;
	private boolean winner = false;
	private Canvas gameCanvas = new Canvas(CanvasDimension.GAME_CANVAS_WIDTH.getValue(),
									CanvasDimension.GAME_CANVAS_HEIGHT.getValue());
	private boolean collisionHappened = false;
	private SpriteHandler sprites = null;
	private PowerUp powerUp = null;
	private double currentSpeed = 0;
	private double timePowerUpReceived = 0;
	
	public Car(Image image, double posX, double posY, double rotation, Player player)
	{
		super.image = image;
		super.posX = posX;
		super.posY = posY;
		this.rotation = rotation;
		this.player = player;
	}
	
	public Group initProgressBar() 
	{
		UpwardProgress upwardProgress = new UpwardProgress(15, 692);
		bar = upwardProgress.getProgressBar();
		if (player.getCarColor().equals(Main.CarColor.CAR_YELLOW.getColor()))
		{
			bar.setStyle("-fx-base: skyblue; -fx-accent: " + Main.CarColorHtml.CAR_YELLOW_HTML.getColorHexCode());
		}
		else
		{
			bar.setStyle("-fx-base: skyblue; -fx-accent: " + Main.CarColorHtml.CAR_BLUE_HTML.getColorHexCode());
		}
		bar.setProgress(0);
		
		return upwardProgress.getProgressHolder();
    }
	
	public double getRotation()
	{
		return rotation;
	}
	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}
	
	public double getXMove()
	{
		return xMove;
	}
	public void setXMove(double xMove)
	{
		this.xMove = xMove;
	}
	public double getYMove()
	{
		return yMove;
	}
	public void setYMove(double yMove)
	{
		this.yMove = yMove;
	}
	public double getRotMove()
	{
		return rotMove;
	}
	public void setRotMove(double rotMove)
	{
		this.rotMove = rotMove;
	}
	
	public boolean getVerticalEnabled()
	{
		return verticalEnabled;
	}
	public void setVerticalEnabled(boolean verticalEnabled)
	{
		this.verticalEnabled = verticalEnabled;
	}
	public boolean getRotationEnabled()
	{
		return rotationEnabled;
	}
	public void setRotationEnabled(boolean rotationEnabled)
	{
		this.rotationEnabled = rotationEnabled;
	}
	
	public Canvas getGameCanvas()
	{
		return gameCanvas;
	}
	public void setGameCanvas(Canvas gameCanvas)
	{
		this.gameCanvas = gameCanvas;
	}
	
	public boolean getCarCloseToTop()
	{
		return carCloseToTop;
	}
	public void setCarCloseToTop(boolean carCloseToTop)
	{
		this.carCloseToTop = carCloseToTop;
	}

	public ProgressIndicator getProgressIndicator()
	{
		return bar;
	}
	public void setProgressBar(double progress)
	{
		bar.setProgress(progress);
	}
	public void updateProgressBar()
	{
		setProgressBar(1 - (posY - sprites.getTrackEnd())/Main.getTrackDistance());
	}	
	public Player getPlayer()
	{
		return player;
	}
	public void setPlayer(Player player)
	{
		this.player = player;
	}	
	public boolean getWinner()
	{
		return winner;
	}
	public void setWinner(boolean winner)
	{
		this.winner = winner;
	}	
	public boolean getCollisionHappened()
	{
		return collisionHappened;
	}
	public void setCollisionHappened(boolean collisionHappened)
	{
		this.collisionHappened = collisionHappened;
	}
	public SpriteHandler getSprites()
	{
		return sprites;
	}
	public void setSprites(SpriteHandler sprites)
	{
		this.sprites = sprites;
	}
	public PowerUp getPowerUp()
	{
		return powerUp;
	}
	public void setPowerUp(PowerUp powerUp)
	{
		this.powerUp = powerUp;
	}
	public double getCurrentSpeed()
	{
		return currentSpeed;
	}
	public void setCurrentSpeed(double currentSpeed)
	{
		this.currentSpeed = currentSpeed;
	}
	public double getTimePowerUpReceived()
	{
		return timePowerUpReceived;
	}
	public void setTimePowerUpReceived(double time)
	{
		this.timePowerUpReceived = time;
	}
	
	public Rectangle getRectangle()
	{
		double width = Car.Dimension.WIDTH.getValue();
		double height = Car.Dimension.HEIGHT.getValue();
		Rectangle rect = new Rectangle(posX, posY, width, height);
		rect.getTransforms().add(new Rotate(Math.toRadians(rotation), width/2, height/2));
		return rect;
	}
}
