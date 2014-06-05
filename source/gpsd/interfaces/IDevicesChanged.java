package gpsd.interfaces;

import gpsd.Device;

public interface IDevicesChanged{
	public void DevicesChanged(Device[] devices);
}