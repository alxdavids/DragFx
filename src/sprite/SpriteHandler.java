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
							
							if (Math.abs(x-corrX) < Wall.BUFFER
							  && Math.abs(y-corrY) < Wall.BUFFER)
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
					double rndY = Wall.getRandomYCoordinate(Main.getRoadNumberCoefficient() + 1);
					sprite.setPosY(rndY);

					double rndX = Wall.getRandomXCoordinate();
					sprite.setPosX(rndX);

					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Resolve collisions
	 */
	public boolean resolveCollisions()
	{
		boolean collisionHappened = false;
		
		Car car = (Car) this.elementAt(0);
		double carX = car.getPosX();
		double carY = car.getPosY();
		double carXMove = car.getXMove();
		double carYMove = car.getYMove();
		
		for (int i=1; i<this.size(); i++)
		{
			Sprite sprite = this.elementAt(i);
			if (sprite instanceof Wall)
			{
				double wallX = sprite.getPosX();
				double wallY = sprite.getPosY();
				
				double yDiff = Math.abs(carY-wallY);
				double xDiff = Math.abs(carX-wallX);
				
				if (yDiff < Wall.HEIGHT
				  && (xDiff < Wall.WIDTH
					&& carX + Car.HEIGHT > wallX))
				{
					boolean applyX = false;
					boolean applyY = false;
					if (yDiff < xDiff)
					{
						if ((carX < wallX && carXMove > 0)
						  || (carX > wallX && carXMove < 0))
						{
							applyX = true;
						}
					}
					else if (xDiff < yDiff)
					{
						if ((carY < wallY && carYMove < 0)
						  || (carY > wallY && carYMove > 0))
						{
							applyY = true;
						}
					}

					if (applyY || applyX)
					{
						car.setPosY(carY + carYMove);
						car.setPosX(carX - carXMove);
						car.setYMove(-carYMove);
						car.setXMove(-carXMove);

						collisionHappened = true;

						System.out.println("carX = " + carX);
						System.out.println("carY = " + carY);
						System.out.println("wallX = " + wallX);
						System.out.println("wallY = " + wallY);
					}
				}
			}
		}
		
		return collisionHappened;
	}
}
