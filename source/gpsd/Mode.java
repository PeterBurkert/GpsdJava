package gpsd;

public enum Mode implements EnumCreatable{
	MODE_NO_FIX(1),
	MODE_2D(2),
	MODE_3D(3);
	
	
	private int id;
	Mode(int id){
		this.id = id;
	}
	public int getValue(){
		return id;
	}
}