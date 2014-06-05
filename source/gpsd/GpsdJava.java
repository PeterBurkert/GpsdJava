package gpsd;

import gpsd.interfaces.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.StringBuilder;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;




public class GpsdJava extends Thread{
	private Socket ConnectionSocket;
	private InputStream inp;
	private OutputStream outp = null;
	

	private Version version;
	private List<Device> devices = new LinkedList<Device>();
	private Watch watch;
	private volatile GpsPosition currentData = null;
	private Sky sky;
	
	
	@SuppressWarnings("unchecked")
	private static <T> T getShallowCopy(T elem){
		try {
			T result = (T)elem.getClass().newInstance();
			for(Field publicField : elem.getClass().getFields()){
				publicField.set(result, publicField.get(elem));
			}
			return result;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private static <T> T[] getShallowCopy(List<T> elements, Class<T> Tclass){
		T[] result = (T[]) Array.newInstance(Tclass, elements.size());
		int i = 0;
		for(T el : elements){
			result[i] = getShallowCopy(el);
		}
		return result;
	}
	
	
	//EVENTHANDLER
	LinkedList<IDevicesChanged> deviceChanged = new LinkedList<IDevicesChanged>();
	LinkedList<IGpsPositionChanged> positionChanged = new LinkedList<IGpsPositionChanged>();
	LinkedList<ISkyChanged> skyChanged = new LinkedList<ISkyChanged>();
	LinkedList<IVersionChanged> versionChanged = new LinkedList<IVersionChanged>();
	LinkedList<IWatchChanged> watchChanged = new LinkedList<IWatchChanged>();
	//End events, setter/remover:
	public void onDeviceChangedAdd(IDevicesChanged eventHandler){
		deviceChanged.add(eventHandler);
	}
	public void onDeviceChangedRemove(IDevicesChanged eventHandler){
		deviceChanged.remove(eventHandler);
	}
	public void onPositionChangedAdd(IGpsPositionChanged eventHandler){
		positionChanged.add(eventHandler);
	}
	public void onPositionChangedRemove(IGpsPositionChanged eventHandler){
		positionChanged.remove(eventHandler);
	}
	public void onSkyChangedAdd(ISkyChanged eventHandler){
		skyChanged.add(eventHandler);
	}
	public void onSkyChangedRemove(ISkyChanged eventHandler){
		skyChanged.remove(eventHandler);
	}
	public void onVersionChangedAdd(IVersionChanged eventHandler){
		versionChanged.add(eventHandler);
	}
	public void onVersionChangedRemove(IVersionChanged eventHandler){
		versionChanged.remove(eventHandler);
	}
	public void onWatchChangedAdd(IWatchChanged eventHandler){
		watchChanged.add(eventHandler);
	}
	public void onWatchChangedRemove(IWatchChanged eventHandler){
		watchChanged.remove(eventHandler);
	}
	
	
	//END EVENTHANDLER, event caller:
	
	//END EVENTCALLER
	private void callDevicesChanged(){
		Device[] devs = getShallowCopy(this.devices, Device.class);
		for(IDevicesChanged dC : deviceChanged){
			dC.DevicesChanged(devs);
		}
	}
	private void callPositionChanged(){
		GpsPosition pos = getShallowCopy(this.currentData);
		for(IGpsPositionChanged posit : positionChanged){
			posit.GpsChanged(pos);
		}
	}
	private void callSkyChanged(){
		Sky sky = getShallowCopy(this.sky);
		for(ISkyChanged sc : this.skyChanged){
			sc.SkyChanged(sky);
		}
	}
	private void callVersionChanged(){
		Version vers = getShallowCopy(this.version);
		for(IVersionChanged v : this.versionChanged){
			v.versionChanged(vers);
		}
	}
	public void callWatchChanged(){
		Watch w = getShallowCopy(this.watch);
		for(IWatchChanged wc : this.watchChanged){
			wc.watchChanged(w);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Class<T> wrap(Class<T> c) {
	    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
	  }
	
	private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS;
    
	static{
		PRIMITIVES_TO_WRAPPERS = new LinkedHashMap<Class<?>, Class<?>>();
		PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
		PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
		PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
		PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
		PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
		PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
		PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
		PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
		PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
	}
	
	public GpsdJava() throws UnknownHostException, IOException{
		this("127.0.0.1", 2947);
	}
	
	private String getNextJson() throws IOException{
		byte[] data = new byte[10000];
    	//System.out.println("starting read");
    	int read = inp.read(data);
    	//System.out.println("read finnished: " + (new Integer(read).toString()));
    	
    	if(read>0){
    		Charset cset = Charset.forName("US-ASCII");
    		ByteBuffer bBuf = ByteBuffer.wrap(data, 0, read);
    		CharBuffer outChars = cset.decode(bBuf);
    		
    		String text = outChars.toString();
    		return text;
    		
    		//useJson(text);
    	}
    	else{
    		return "";
    	}
    		
	}
	
	private String getArgs(EnumSet<WatchEnum> flags, String devPath){
		StringBuilder b = new StringBuilder();
		if(flags.contains(WatchEnum.WATCH_DISABLE)){
			b.append("?WATCH={\"enable\":false");
			if(flags.contains(WatchEnum.WATCH_JSON)){
				b.append(",\"json\":false\"");
			}
			if(flags.contains(WatchEnum.WATCH_NMEA)){
				b.append(",\"nmea\":false");
			}
			if(flags.contains(WatchEnum.WATCH_RARE)){
				b.append(",\"raw\":1");
			}
			if(flags.contains(WatchEnum.WATCH_RAW)){
				b.append(",\"raw\":2");
			}
			if(flags.contains(WatchEnum.WATCH_SCALED)){
				b.append(",\"scaled\":false");
			}
			if(flags.contains(WatchEnum.WATCH_TIMING)){
				b.append(",\"timing\":false");
			}
		}
		else{
			b.append("?WATCH={\"enable\":true");
            if (flags.contains(WatchEnum.WATCH_JSON)){
                b.append(",\"json\":true");
            }
            if (flags.contains(WatchEnum.WATCH_NMEA)){
                b.append(",\"nmea\":true");
            }
            if (flags.contains(WatchEnum.WATCH_RAW)){
                b.append(",\"raw\":1");
            }
            if (flags.contains(WatchEnum.WATCH_RARE)){
                b.append(",\"raw\":0");
            }
            if (flags.contains(WatchEnum.WATCH_SCALED)){
                b.append(",\"scaled\":true");
            }
            if (flags.contains(WatchEnum.WATCH_TIMING)){
                b.append(",\"timing\":true");
            }
            if (flags.contains(WatchEnum.WATCH_DEVICE)){
                b.append(String.format(",\"device\":\"%s\"", devPath));
            }
		}
		b.append("}");
		return b.toString();
	}
	
	private void send(String command) throws IOException{
		if(!command.endsWith("\n")){
			command += "\n";
		}
		if(outp == null){
			outp = ConnectionSocket.getOutputStream();
		}
		Charset cset = Charset.forName("US-ASCII");
		ByteBuffer outBuff = cset.encode(command);
		byte[] outB = outBuff.array();
		outp.write(outB);
		
	}
	
    public GpsdJava(String address, int port) throws UnknownHostException, IOException{
    	ConnectionSocket = new Socket(address, port);
    	inp = ConnectionSocket.getInputStream();
    	
    	String json = getNextJson();
    	if(json.length() > 0){
    		useJson(json);
    	}
    	
    	send(getArgs(EnumSet.of(WatchEnum.WATCH_ENABLE, WatchEnum.WATCH_JSON), null));
    	
    }
    
    public void listenInfinite(){
    	while(true){
    		try {
				useJson(getNextJson());
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
    private void useJson(String s){
    	JSONParser parser = new JSONParser();
    	boolean newPositionFound = false;
		ContainerFactory containerFactory = new ContainerFactory(){
		    public List creatArrayContainer() {
		      return new LinkedList();
		    }
	
		    public Map createObjectContainer() {
		      return new LinkedHashMap();
		    }
		                        
		};
		List<Map> jsons = new LinkedList<Map>();
		int i = 0;
		try{
			
			try{
				jsons.add((Map)parser.parse(s, containerFactory));
			}
			catch(ParseException peErr){
				String[] jsonStrings = splitJson(s);
				for(String splJ : jsonStrings){
					jsons.add((Map)parser.parse(splJ, containerFactory));
				}
			}
			for(Map json : jsons){
			    if(json.containsKey("class")){
			    	String classString = (String)json.get("class");
			    	if(classString.equals("VERSION")){
			    		version = getObject(json, Version.class);
			    		(new Thread(){
			    			@Override
			    			public void run(){
			    				callVersionChanged();
			    			}
			    		}).start();
			    		
			    	}
			    	else if(classString.equals("DEVICE") || classString.equals("DEVICES")){
			    		List<Map<String,Object>> dev;
			    		if(classString.equals("DEVICE")){
			    			dev = new LinkedList<Map<String,Object>>();
			    			dev.add(json);
			    		}
			    		else{
			    			dev = (List<Map<String,Object>>)json.get("devices");
			    		}
			    		for(Map<String,Object> elems : dev){
			    			Device newElem = getObject(elems, Device.class);
			    			//check if this device is already part of the list. if yes onl update
			    			boolean found = false;
			    			for(Device d : devices){
			    				if(d.path.equals(newElem.path)){
			    					found = true;
			    					d.update(newElem);
			    					break;
			    				}
			    			}
			    			if(!found){
			    				devices.add(newElem);
			    			}
			    		}
			    		
			    		(new Thread(){
			    			@Override
			    			public void run(){
			    				callDevicesChanged();
			    			}
			    		}).start();
			    		
			    	}
			    	else if(classString.equals("WATCH")){
			    		watch = getObject(json, Watch.class);
			    		(new Thread(){
			    			@Override
			    			public void run(){
			    				callWatchChanged();
			    			}
			    		}).start();
			    	}
			    	else if(classString.equals("TPV")){
			    		GpsPosition t = getObject(json, GpsPosition.class);
			    		if((t.getDate() != null) && (this.currentData == null || this.currentData.getDate().before(t.getDate()))){
			    			this.currentData = t;
			    			newPositionFound = true;
			    		}
			    	}
			    	else if(classString.equals("SKY")){
			    		this.sky = getObject(json, Sky.class);
			    		(new Thread(){
			    			@Override
			    			public void run(){
			    				callSkyChanged();
			    			}
			    		}).start();
			    	}
			    	else{
			    		int dummy = 1;
			    	}
			    }
			    i++;
			}
		    
		  }
		  catch(ParseException pe){
		  } catch (IllegalArgumentException e) {
		  } catch (SecurityException e) {
		  } catch (NullPointerException np){
			  np.printStackTrace(); //this occurs when the program is started before the gps module is plugged in 
			  Map o = jsons.get(i);
			  System.err.println(o.toString());
		  }
		
		if(newPositionFound){
			(new Thread(){
    			@Override
    			public void run(){
    				callPositionChanged();
    			}
    		}).start();
		}
		
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private <T> T getObject(Map json, Class<T> classToGet)
	{
		T el = null;
		try {
			el = (T)classToGet.newInstance();
			
			for(Field publicField : classToGet.getFields()){
				
    			Annotation[] annots = publicField.getAnnotations();
    			String query = ((ParseName)annots[0]).value();
    			if(json.containsKey(query)){
    				Object cont = json.get(query);
    				Class fieldClass = publicField.getType();
    				int dummy = 1;
    				if(fieldClass.equals(String.class)){
    					try {
							publicField.set(el, cont);
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
    				}
    				else if (fieldClass.isPrimitive()){
    					try{
	    					Class toCall = wrap(publicField.getType());
							Method parseMethod = toCall.getMethod("valueOf", new Class[]{String.class});
							try{
								publicField.set(el, parseMethod.invoke(publicField, cont));
							}
							catch (IllegalArgumentException ie){
								publicField.set(el, cont);
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
    					} catch(NoSuchMethodException mExc){
    					} catch(IllegalAccessException iAcc){
    					}
    				}
    				else if(fieldClass.isEnum()){
    					Long index = (Long)cont;
    					Object en = getEnumVal(index.intValue(), fieldClass);
    					try {
							publicField.set(el, en);
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
    				}
    				else if(fieldClass.isAssignableFrom(List.class) && classToGet == Sky.class){
    					LinkedList<LinkedHashMap<String, Object>> satStrings = (LinkedList<LinkedHashMap<String, Object>>)cont;
    					for(Map m : satStrings){
    						Satellite satObj = getObject(m, Satellite.class);
    						((Sky)el).satellites.add(satObj);
    					}
    				}
    			}
    		}
		} catch (InstantiationException e) {
		} catch (IllegalAccessException ilEx){
		}
		
		return el;
	}
    
	private String[] splitJson(String json){
		String [] val = json.split("\\}(\n|\r)*\\{");
		if(val.length>1){
			val[0] += "}";
			val[val.length-1] = "{" + val[val.length-1];
			
			for(int i = 1; i<(val.length-1); i++){
				val[i] = "{" + val[i] + "}";
			}
		}
		return val;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends EnumCreatable> T getEnumVal(int id, Class<T> enumClass){
		try {
			Method getVals = enumClass.getMethod("values");
			T[] poss = (T[]) getVals.invoke(null, new Object[0]);
			for(T b : poss){
				if(b.getValue() == id){
					return b;
				}
			}
			return poss[0];
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return null;
	}
    
	@Override
	public void run(){
		this.listenInfinite();
	}
	
}











