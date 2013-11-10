package ar.edu.itba.pdc.transformations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


import ar.edu.itba.pdc.utils.ByteArrayQueue;
import org.apache.log4j.Logger;

public class L33tTransformation implements Transformation{

    private static Logger logger = Logger.getLogger(L33tTransformation.class);

    private static L33tTransformation INSTANCE = null;

    private L33tTransformation(){
    }

    private synchronized static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new L33tTransformation();
        }
    }

    public static L33tTransformation getInstance() {
        createInstance();
        return INSTANCE;
    }

	public void transform(byte[] bytes){
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
	}
	
	public static byte[] gzipedConvert(ByteArrayQueue arr) throws IOException{
		
//		try {
//			Deflater deflater = new Deflater();
//			Inflater inflater = new Inflater(true);
//			byte[] compressed = new byte[arr.available()];
//			System.out.println(arr.available());
//			arr.read(compressed);
//			inflater.setInput(compressed);
//			byte[] descompressed = new byte[inflater.getAdler()];
//			inflater.inflate(descompressed);
//			deflater.setInput(convert(descompressed));
//			System.out.println(deflater.deflate(descompressed));
//			return descompressed;
//		} catch (DataFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return new byte[0];
//		
		byte[] test = new byte[arr.available()];
		arr.read(test);
		GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(test));
		
		int data = 0;
		while(stream.available() != 0){
			data = stream.read();
			System.out.print((char)data);
		}
		System.out.println(data);
		
		return new byte[0];
	}
	
}
