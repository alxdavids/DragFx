package sprite;

import java.util.Random;

import application.Main;
import javafx.scene.image.Image;

public class Wall extends Sprite
{
	public final static double WIDTH = 68;
	public final static double HEIGHT = 29;
	public final static double BUFFER = WIDTH*1.5;
		
	public Wall(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
	
	public static double getRandomYCoordinate(int roadNumberCoefficient)
	{
		double minValue = roadNumberCoefficient*Road.HEIGHT + Main.TOP_BUFFER/2;
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return  minValue + (rndD*(Road.HEIGHT - minValue));
	}
	
	public static double getRandomXCoordinate()
	{
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return rndD*(Road.WIDTH - Wall.WIDTH);
	}
}
