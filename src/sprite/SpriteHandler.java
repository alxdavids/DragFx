package sprite;

import java.util.Vector;

import application.Main;

public class SpriteHandler extends Vector<Sprite>
{
	private static final long serialVersionUID = 1L;
	
	public SpriteHandler()
	{
	}
	
	public void scrollSprites(Car car)
	{
		double lowestY = 0;
		double yMove = car.getYMove();
		for (int i=0; i<this.size(); i++)
		{
			Sprite sprite = this.elementAt(i);
			double oldY = sprite.getPosY();
			double newY = oldY + yMove;
			sprite.setPosY(newY);
			if (newY < lowestY)
			{
				lowestY = newY;
			}
		}
		
		Main.setTrackEnd(lowestY);
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

		for (int j=0; j<2; j++)
		{
			Car car = (Car) this.elementAt(j);
			double carX = car.getPosX();
			double carY = car.getPosY();
			double carXMove = car.getXMove();
			double carYMove = car.getYMove();

			for (int i=2; i<this.size(); i++)
			{
				Sprite sprite = this.elementAt(i);
				if (sprite instanceof Wall)
				{
					double wallX = sprite.getPosX();
					double wallY = sprite.getPosY();

					double yDiff = Math.abs(carY-wallY);
					double xDiff = Math.abs(carX-wallX);

					if (yDiff < Wall.HEIGHT
							&& (xDiff < Wall.WIDTH && carX + Car.HEIGHT > wallX))
					{
						boolean applyX = false;
						boolean applyY = false;
						if (Wall.HEIGHT - yDiff > Wall.WIDTH - xDiff - Car.WIDTH/2)
						{
							if ((carX < wallX && carXMove > 0)
									|| (carX > wallX && carXMove < 0))
							{
								applyX = true;
							}
						}
						else if (Wall.WIDTH - xDiff > Wall.HEIGHT - yDiff - Car.HEIGHT/2)
						{
							if ((carY < wallY && carYMove < 0)
									|| (carY > wallY && carYMove > 0 && ((xDiff < Car.WIDTH) || (carX > wallX))))
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
						}
					}
				}
				else if (sprite instanceof FinishLine)
				{
					double finishY = sprite.getPosY();
					double yDiff = Math.abs(carY-finishY);

					if (yDiff < FinishLine.HEIGHT)
					{
						Main.setGameWon();
						car.setWinner(true);
					}
				}
			}
		}
		
		return collisionHappened;
	}
}
