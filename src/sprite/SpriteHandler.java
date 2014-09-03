package sprite;

import java.util.Vector;

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
}
