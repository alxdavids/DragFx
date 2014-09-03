package sprite;

import javafx.scene.image.Image;

public class Car extends Sprite
{
	public final static double WIDTH = 30;
	public final static double HEIGHT = 37;
	
	private double rotation;
	private double xMove = 0;
	private double yMove = 0;
	
	public Car(Image image, double posX, double posY, double rotation)
	{
		super.image = image;
		super.posX = posX;
		super.posY = posY;
		this.rotation = rotation;
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
}
