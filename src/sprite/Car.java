package sprite;

import javafx.scene.Group;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import utils.UpwardProgress;
import application.Main;
import application.Player;

public class Car extends Sprite
{
	public final static double WIDTH = 30;
	public final static double HEIGHT = 37;
	
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
		if (player.getCarColor().equals(Main.CAR_YELLOW))
		{
			bar.setStyle("-fx-base: skyblue; -fx-accent: " + Main.CAR_YELLOW_HTML);
		}
		else
		{
			bar.setStyle("-fx-base: skyblue; -fx-accent: " + Main.CAR_BLUE_HTML);
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
		setProgressBar(1 - (posY - Main.getTrackEnd())/Main.getTrackDistance());
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
}
