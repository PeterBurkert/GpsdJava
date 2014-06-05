package gpsd.interfaces;

import gpsd.GpsPosition;

public interface IGpsPositionChanged{
	public void GpsChanged(GpsPosition current);
}