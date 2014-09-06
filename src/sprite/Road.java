package sprite;

import javafx.scene.image.Image;

public class Road extends Sprite
{
	public enum Dimension {
		WIDTH(305), HEIGHT(346);
		
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
		
	public Road(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
}
