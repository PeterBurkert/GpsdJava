package gpsd;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GpsPosition{
	@ParseName("tag")
	public String tag;
	@ParseName("device")
	public String device;
	@ParseName("mode")
	public Mode mode;
	@ParseName("time")
	public String time;
	@ParseName("ept")
	public double ept;
	@ParseName("lat")
	public double latitude;
	@ParseName("lon")
	public double longitude;
	@ParseName("alt")
	public double altitude;
	@ParseName("epx")
	public double epx;
	@ParseName("epy")
	public double epy;
	@ParseName("epv")
	public double epv;
	@ParseName("track")
	public double track;
	@ParseName("speed")
	public double speed;
	@ParseName("climb")
	public double climb;
	@ParseName("eps")
	public double eps;
	
	
	public Date getDate(){
		if(this.time == null) return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	Date date;
    	try {
			date = dateFormat.parse(this.time);
			return date;
		} catch (java.text.ParseException e1) {
			return null;
		}
	}
	
	@Override
	public String toString(){
		return String.format("Longitude: %f, Latitude: %f, Altitude: %f\n", longitude, latitude, altitude);
	}
}