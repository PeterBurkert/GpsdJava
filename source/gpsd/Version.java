package gpsd;


public class Version {
	@ParseName("release")
	public double release;
	@ParseName("rev")
	public double revision;
	@ParseName("proto_major")
	public double protocolMajor;
	@ParseName("proto_minor")
	public double protocolMinor;
	
	
	@Override
	public String toString(){
		return String.format("Release: %s\r Revision: %s\r Protocol Major: %s\r Protocol Minor: %s\r",
				release, revision, protocolMajor, protocolMinor);
	}
}
