package application;

import javafx.scene.paint.Color;

public class Player
{
	private String name;
	private Color carColor;
	private boolean useAlternateControls = false;
	
	public Player()
	{}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public Color getCarColor()
	{
		return carColor;
	}
	public void setCarColor(Color carColor)
	{
		this.carColor = carColor;
	}
	public boolean getUseAlternateControls()
	{
		return useAlternateControls;
	}
	public void setUseAlternateControls(boolean useAlternateControls)
	{
		this.useAlternateControls = useAlternateControls;
	}
}
