package com.example.nutandroid.model;

import java.text.MessageFormat;

public class BarItem
{
	protected String belowText;
	protected float barHeightPercent;
	protected String topText;

	public BarItem(String belowText, float barHeightPercent, String topText)
	{
		this.belowText = belowText;
		this.barHeightPercent = barHeightPercent;
		this.topText = topText;
	}

	public String getBelowText()
	{
		return belowText;
	}

	public float getBarHeightPercent()
	{
		return barHeightPercent;
	}

	public String getTopText()
	{
		return topText;
	}

	@Override
	public String toString()
	{
		return MessageFormat.format("belowText:{0},barHeightPercent:{1},topText:{2}\r\n", belowText, barHeightPercent, topText);
	}
}
