package ar.edu.itba.pdc.utils;

import org.apache.log4j.Logger;

public class L33tConversion {

    private static Logger logger = Logger.getLogger(L33tConversion.class);

	public static byte[] convert(byte[] bytes){
        logger.info("Applying l33t conversion.");
		for(int i = 0; i< bytes.length; i++){
			switch(bytes[i]){
			case 'a': bytes[i] = '4'; break;
			case 'e': bytes[i] = '3'; break;
			case 'i': bytes[i] = '1'; break;
			case 'o': bytes[i] = '0'; break;
			case 'c': bytes[i] = '<'; break;
			case 'A': bytes[i] = '4'; break;
			case 'E': bytes[i] = '3'; break;
			case 'I': bytes[i] = '1'; break;
			case 'O': bytes[i] = '0'; break;
			case 'C': bytes[i] = '<'; break;
			default: break;
			}
		}
		return bytes;
	}
	
}
