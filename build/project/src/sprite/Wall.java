package sprite;

import java.util.Random;

import application.Main;
import javafx.scene.image.Image;

public class Wall extends Sprite
{
	public enum Dimension {
		WIDTH(68), HEIGHT(29);
		
		private double value;
		
		private Dimension(double value)
		{
			this.value = value;
		}	
		public double getValue()
		{
			return value;
		}
	}
	public final static double BUFFER = Dimension.WIDTH.getValue()*1.5;
		
	public Wall(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
	
	public static double getRandomYCoordinate(int roadNumberCoefficient)
	{
		double minValue = roadNumberCoefficient*Road.Dimension.HEIGHT.getValue() + Main.TOP_BUFFER/2;
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return  minValue + (rndD*(Road.Dimension.HEIGHT.getValue() - minValue));
	}
	
	public static double getRandomXCoordinate()
	{
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return rndD*(Road.Dimension.WIDTH.getValue() - Wall.Dimension.WIDTH.getValue());
	}
}
