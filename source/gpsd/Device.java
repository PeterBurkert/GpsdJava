package gpsd;

public class Device{
	@ParseName("path")
	public String path;
	@ParseName("activated")
	public String activated;//date
	@ParseName("flags")
	public WatchEnum flags;
	@ParseName("driver")
	public String driver;
	@ParseName("native")
	public long nativeVal;
	@ParseName("bps")
	public long bps; //Baudrate?
	@ParseName("parity")
	public String parity;
	@ParseName("stopbits")
	public long stopbits;
	@ParseName("cycle")
	public double cycle;
	
	
	
	
	
	public void update(Device d){
		this.path = d.path;
		this.activated = d.activated;
		this.flags = d.flags;
		this.driver = d.driver;
		this.nativeVal = d.nativeVal;
		this.bps = d.bps;
		this.parity = d.parity;
		this.stopbits = d.stopbits;
		this.cycle = d.cycle;
	}
}