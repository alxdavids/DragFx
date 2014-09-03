package sprite;

import javafx.scene.image.Image;

public class Car extends Sprite
{
	public final static double WIDTH = 30;
	public final static double HEIGHT = 37;
	
	private double rotation;
	
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
}
