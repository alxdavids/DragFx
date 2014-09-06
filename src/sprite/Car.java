package sprite;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
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
	public final static double GAME_CANVAS_WIDTH = 305;
	public final static double GAME_CANVAS_HEIGHT = 692;
	
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
	private Canvas gameCanvas = new Canvas(GAME_CANVAS_WIDTH,GAME_CANVAS_HEIGHT);
	private boolean collisionHappened = false;
	private double trackDisposition = 0;
	private double carModifier = 0;
	private double spriteModifier = 0;
	private SpriteHandler sprites = null;
	
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

	public double getTrackDisposition()
	{
		return trackDisposition;
	}
	public void setTrackDisposition(double highestY)
	{
		this.trackDisposition = highestY + Road.Dimension.HEIGHT.getValue() - GAME_CANVAS_HEIGHT; // We add the height of a road here as this is the lowest point of the track
	}
	public SpriteHandler getSprites()
	{
		return sprites;
	}
	public void setSprites(SpriteHandler sprites)
	{
		this.sprites = sprites;
	}
}
