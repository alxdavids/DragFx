package sprite;

import java.util.Vector;

import application.Main;

public class SpriteHandler extends Vector<Sprite>
{
	private static final long serialVersionUID = 1L;
	private double trackEnd = 0;
	
	public SpriteHandler(int roadNumberCoefficient)
	{
		this.trackEnd = (roadNumberCoefficient+1)*Road.Dimension.HEIGHT.getValue() + Main.TOP_BUFFER;
	}
	
	public double getTrackEnd()
	{
		return trackEnd;
	}
	public void setTrackEnd(double value)
	{
		this.trackEnd = value + Main.TOP_BUFFER/2;
	}
	
	public void scrollSprites(Car car)
	{
		double lowestY = 0;
		double highestY = 0;
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
			if (newY > highestY)
			{
				highestY = newY;
			}
		}
		
		setTrackEnd(lowestY);
		car.setTrackDisposition(highestY);
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
	public void resolveCollisions()
	{
		for (int j=0; j<2; j++)
		{
			Sprite carSprite = this.elementAt(j);
			if (carSprite instanceof Car)
			{	
				Car car = (Car) carSprite;
				car.setCollisionHappened(false);
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

						if (yDiff < Wall.Dimension.HEIGHT.getValue()
								&& (xDiff < Wall.Dimension.WIDTH.getValue() && carX + Car.Dimension.HEIGHT.getValue() > wallX))
						{
							boolean applyX = false;
							boolean applyY = false;
							if (Wall.Dimension.HEIGHT.getValue() - yDiff > Wall.Dimension.WIDTH.getValue() - xDiff - Car.Dimension.WIDTH.getValue()/2)
							{
								if ((carX < wallX && carXMove > 0)
										|| (carX > wallX && carXMove < 0))
								{
									applyX = true;
								}
							}
							else if (Wall.Dimension.WIDTH.getValue() - xDiff > Wall.Dimension.HEIGHT.getValue() - yDiff - Car.Dimension.HEIGHT.getValue()/2)
							{
								if ((carY < wallY && carYMove < 0)
										|| (carY > wallY && carYMove > 0 && ((xDiff < Car.Dimension.WIDTH.getValue()) || (carX > wallX))))
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

								car.setCollisionHappened(true);
							}
						}
					}
					else if (sprite instanceof FinishLine)
					{
						double finishY = sprite.getPosY();
						double yDiff = Math.abs(carY-finishY);

						if (yDiff < FinishLine.Dimension.HEIGHT.getValue())
						{
							Main.setGameWon();
							car.setWinner(true);
						}
					}
				}
			}
		}
	}
}
