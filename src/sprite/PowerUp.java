package sprite;

import java.util.Random;

import application.Main;

public class PowerUp extends Sprite
{
	public enum Dimension {
		WIDTH(34), HEIGHT(34);
		
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
	
	public PowerUp()
	{
	}
	
	public static double getRandomYCoordinate(int roadNumberCoefficient)
	{
		double minValue = (roadNumberCoefficient+1)*Road.Dimension.HEIGHT.getValue() + Main.TOP_BUFFER/2;
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return  minValue + (rndD*(Road.Dimension.HEIGHT.getValue() - minValue));
	}
	
	public static double getRandomXCoordinate()
	{
		Random rnd = new Random();
		double rndD = rnd.nextDouble();
		return rndD*(Road.Dimension.WIDTH.getValue() - PowerUp.Dimension.WIDTH.getValue());
	}
}
