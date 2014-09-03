package sprite;

import java.util.Random;

import javafx.scene.image.Image;

public class Wall extends Sprite
{
	public final static double WIDTH = 68;
	public final static double HEIGHT = 29;
		
	public Wall(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
	
	public static double getRandomYCoordinate(int roadNumberCoefficient)
	{
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return roadNumberCoefficient*Road.HEIGHT + (rndD*Road.HEIGHT*(1 - roadNumberCoefficient));
	}
	
	public static double getRandomXCoordinate()
	{
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return rndD*(Road.WIDTH - Wall.WIDTH);
	}
}
