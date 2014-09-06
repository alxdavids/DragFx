package sprite;

import javafx.scene.image.Image;

public class Sprite
{
	protected double posX;
	protected double posY;
	protected Image image; 
	
	public Image getImage()
	{
		return image;
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
}
