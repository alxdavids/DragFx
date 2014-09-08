package sprite;

import javafx.scene.image.Image;


public class Boost extends PowerUp
{
	public Boost(Image image, double posX, double posY)
	{
		super.image = image;
		super.posX = posX;
		super.posY = posY;
	}
}
