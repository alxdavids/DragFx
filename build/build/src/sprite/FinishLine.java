package sprite;

import javafx.scene.image.Image;

public class FinishLine extends Sprite
{
	public enum Dimension {
		WIDTH(Road.Dimension.WIDTH.getValue()), HEIGHT(20);
		
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
	
	public FinishLine(double posX, double posY, Image image)
	{
		super.posX = posX;
		super.posY = posY;
		super.image = image;
	}
}
