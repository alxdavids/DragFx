package sprite;

import javafx.scene.image.Image;

public class FinishLine extends Sprite
{
	public final static double WIDTH = Road.WIDTH;
	public final static double HEIGHT = 20;
	
	public FinishLine(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
}
