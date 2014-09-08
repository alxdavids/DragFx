package sprite;

import java.util.Vector;

import application.Main;

public class SpriteHandler extends Vector<Sprite>
{
	private enum SpriteType {
		WALL(0),POWERUP(1);
		
		private int value;
		
		private SpriteType(int value)
		{
			this.value = value;
		}
		public int getValue()
		{
			return value;
		}
	}
	
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
		double yMove = car.getYMove();
		for (Sprite sprite : this)
		{
			double oldY = sprite.getPosY();
			double newY = oldY + yMove;
			sprite.setPosY(newY);
			if (newY < lowestY)
			{
				lowestY = newY;
			}
		}
		
		setTrackEnd(lowestY);
	}

	/**
	 * Check that the walls aren't too close to each other.
	 */
	
	public boolean checkSpritesArePlacedCorrectly()
	{
		boolean spriteTooClose = false;
		
		int size = this.size();
		for (int i=0; i<size; i++)
		{
			Sprite sprite = this.elementAt(i);
			if (sprite instanceof Wall)
			{
				if(!validSpritePosition(spriteTooClose, size, i, sprite, SpriteType.WALL.getValue()))
				{
					return false;
				}
			}
			else if (sprite instanceof PowerUp)
			{
				if ((!validSpritePosition(spriteTooClose, size, i, sprite, SpriteType.WALL.getValue())
				  || (!validSpritePosition(spriteTooClose, size, i, sprite, SpriteType.POWERUP.getValue()))))
				{
					return false;
				}
			}
		}
		return true;
	}

	private boolean validSpritePosition(boolean spriteTooClose, int size, int i, Sprite sprite, int type)
	{
		for (int j=0; j<size; j++)
		{
			if (j != i)
			{
				Sprite correspondingSprite = this.elementAt(j);
				if (correspondingSprite instanceof Wall
				  && type == SpriteType.WALL.getValue())
				{
					double x = sprite.getPosX(); 
					double y = sprite.getPosY();
					
					double corrX = correspondingSprite.getPosX();
					double corrY = correspondingSprite.getPosY();
					
					double diffX = Math.abs(x-corrX);
					double diffY = Math.abs(y-corrY);
					
					boolean closeToWall = (type == SpriteType.WALL.getValue() && diffX < Wall.BUFFER && diffY < Wall.BUFFER);				
					if (closeToWall)
					{
						spriteTooClose = true;
						break;
					}
				}
				else if (correspondingSprite instanceof PowerUp
				  || type == SpriteType.POWERUP.getValue())
				{
					double x = sprite.getPosX(); 
					double y = sprite.getPosY();
					
					double corrX = correspondingSprite.getPosX();
					double corrY = correspondingSprite.getPosY();
					
					boolean closeToPowerUp = type == SpriteType.POWERUP.getValue()
							&& Math.abs(x-corrX) < PowerUp.BUFFER
							&& Math.abs(y-corrY) < PowerUp.BUFFER;
					if (closeToPowerUp)
					{
						spriteTooClose = true;
						break;
					}
				}
			}
		}
		
		/**
		 * Re-randomise the coordinates if we get two walls that are really close to each other.
		 */
		if (spriteTooClose)
		{
			double rndY = 0;
			double rndX = 0;
			
			if (sprite instanceof Wall)
			{
				rndY = Wall.getRandomYCoordinate(Main.getRoadNumberCoefficient() + 1);
				rndX = Wall.getRandomXCoordinate();
			}
			else if (sprite instanceof PowerUp)
			{
				rndY = PowerUp.getRandomYCoordinate(Main.getRoadNumberCoefficient() + 1);
				rndX = PowerUp.getRandomXCoordinate();
			}
			
			sprite.setPosY(rndY);
			sprite.setPosX(rndX);

			return false;
		}
		return true;
	}

	/**
	 * Resolve collisions
	 */
	public void resolveCollisions(double time)
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

				for (int i=this.size()-1; i>0; i--)
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
					else if (sprite instanceof PowerUp)
					{
						double powerX = sprite.getPosX();
						double powerY = sprite.getPosY();
						double xDiff = Math.abs(carX-powerX);
						double yDiff = Math.abs(carY-powerY);
						
						if (xDiff < PowerUp.Dimension.WIDTH.getValue()
						  && yDiff < PowerUp.Dimension.HEIGHT.getValue())
						{
							car.setTimePowerUpReceived(time);
							if (sprite instanceof Boost)
							{
								car.setPowerUp((Boost) sprite);
							}
							else if (sprite instanceof SlowDown)
							{
								car.setPowerUp((SlowDown) sprite);
							}
							this.remove(i);
						}
					}
				}
			}
		}
	}
}
