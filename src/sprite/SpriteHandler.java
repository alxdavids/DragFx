package sprite;

import java.util.Vector;

import application.Main;

public class SpriteHandler extends Vector<Sprite>
{
	private static final long serialVersionUID = 1L;
	
	public SpriteHandler()
	{
	}
	
	public void scrollSprites(double yMove)
	{
		for (int i=0; i<this.size(); i++)
		{
			Sprite sprite = this.elementAt(i);
			double oldY = sprite.getPosY();
			double newY = oldY + yMove;
			sprite.setPosY(newY);
		}
	}

	/**
	 * Check that the walls aren't too close to each other.
	 */
	public boolean checkWallsArePlacedCorrectly()
	{
		boolean wallTooClose = false;
		
		int size = this.size();
		for (int i=0; i<size; i++)
		{
			Sprite sprite = this.elementAt(i);
			if (sprite instanceof Wall)
			{
				for (int j=0; j<size; j++)
				{
					if (j != i)
					{
						Sprite correspondingSprite = this.elementAt(j);
						if (correspondingSprite instanceof Wall)
						{
							double x = sprite.getPosX(); 
							double y = sprite.getPosY();
							
							double corrX = correspondingSprite.getPosX();
							double corrY = correspondingSprite.getPosY();
							
							if (Math.abs(x-corrX) < 30
							  && Math.abs(y-corrY) < 30)
							{
								wallTooClose = true;
							}
							
							if (wallTooClose)
							{
								break;
							}
						}
					}
				}
				
				/**
				 * Re-randomise the coordinates if we get two walls that are really close to each other.
				 */
				if (wallTooClose)
				{
					double rndY = Wall.getRandomYCoordinate(Main.getRoadNumberCoefficient());
					sprite.setPosY(rndY);

					double rndX = Wall.getRandomXCoordinate();
					sprite.setPosX(rndX);

					return false;
				}
			}
		}
		return true;
	}
}
