package sprite;

import javafx.scene.image.Image;

public class Road extends Sprite
{
	public final static double WIDTH = 305;
	public final static double HEIGHT = 346;
		
	public Road(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
}
