import gpsd.Device;
import gpsd.GpsPosition;
import gpsd.GpsdJava;
import gpsd.Sky;
import gpsd.Version;
import gpsd.Watch;
import gpsd.interfaces.IDevicesChanged;
import gpsd.interfaces.IGpsPositionChanged;
import gpsd.interfaces.ISkyChanged;
import gpsd.interfaces.IVersionChanged;
import gpsd.interfaces.IWatchChanged;

import java.io.IOException;
import java.net.UnknownHostException;


public class Test implements IDevicesChanged, IGpsPositionChanged, ISkyChanged, IVersionChanged, IWatchChanged {
	@SuppressWarnings("unused")
	public static void main(String[] args){
		try {
			GpsdJava inst = new GpsdJava();
			Test t = new Test();
			inst.onPositionChangedAdd(t);
			inst.onDeviceChangedAdd(t);
			inst.onSkyChangedAdd(t);
			inst.onVersionChangedAdd(t);
			inst.onWatchChangedAdd(t);
			
			inst.start();
			
			
			int dummy = 1;
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void watchChanged(Watch w) {
		System.out.println("Watch");
		
	}

	@Override
	public void versionChanged(Version v) {
		System.out.println("Version");
		
	}

	@Override
	public void SkyChanged(Sky newSky) {
		System.out.println("Sky");
		
	}

	@Override
	public void GpsChanged(GpsPosition current) {
		System.out.println(current.toString());
		
	}

	@Override
	public void DevicesChanged(Device[] devices) {
		System.out.println("devices");
		
	}
}
