package gpsd;

public class Watch{
	@ParseName("enable")
	public boolean enable;
	@ParseName("json")
	public boolean json;
	@ParseName("nmea")
	public boolean nmea;
	@ParseName("raw")
	public long raw;
	@ParseName("scaled")
	public boolean scaled;
	@ParseName("timing")
	public boolean timing;
}