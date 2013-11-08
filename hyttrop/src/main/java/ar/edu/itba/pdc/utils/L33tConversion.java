package ar.edu.itba.pdc.utils;

public class L33tConversion {

	public static byte[] convert(byte[] bytes){
		for(int i = 0; i< bytes.length; i++){
			switch(bytes[i]){
			case 'a': bytes[i] = 4; break;
			case 'e': bytes[i] = 3; break;
			case 'i': bytes[i] = 1; break;
			case 'o': bytes[i] = 0; break;
			case 'c': bytes[i] = '<'; break;
			default: break;
			}
		}
		return bytes;
	}
	
}
