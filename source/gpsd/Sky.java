package gpsd;

import java.util.LinkedList;
import java.util.List;

public class Sky{
	@ParseName("tag")
	public String tag;
	@ParseName("device")
	public String device;
	@ParseName("xdop")
	public double xdop;
	@ParseName("ydop")
	public double ydop;
	@ParseName("vdop")
	public double vdop;
	@ParseName("tdop")
	public double tdop;
	@ParseName("hdop")
	public double hdop;
	@ParseName("gdop")
	public double gdop;
	@ParseName("pdop")
	public double pdop;
	@ParseName("satellites")
	public List<Satellite> satellites = new LinkedList<Satellite>();
}