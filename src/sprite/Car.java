package sprite;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public class Car
{
	public final static double WIDTH = 30;
	public final static double HEIGHT = 37;
	
	private double posX;
	private double posY;
	private double rotation;
	private Image carImage; 
	
	public Car(Image carImage, double posX, double posY, double rotation)
	{
		this.carImage = carImage;
		this.posX = posX;
		this.posY = posY;
		this.rotation = rotation;
	}
	
	public Image getCarImage()
	{
		return carImage;
	}
	
	public double getPosX()
	{
		return posX;
	}
	public void setPosX(double posX)
	{
		this.posX = posX;
	}
	public double getPosY()
	{
		return posY;
	}
	public void setPosY(double posY)
	{
		this.posY = posY;
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
