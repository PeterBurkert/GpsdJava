package gpsd;

public class Satellite{
	@ParseName("PRN")
	public long prn;
	@ParseName("el")
	public long elevation;
	@ParseName("az")
	public long azimuth;
	@ParseName("ss")
	public long ss;
	@ParseName("used")
	public boolean used;
}