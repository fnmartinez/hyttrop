package ar.edu.itba.pdc.transformations;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import ar.edu.itba.pdc.utils.ByteArrayQueue;

import org.apache.log4j.Logger;

public class L33tTransformation implements Transformation{

	private GZIPInputStream stream;
	private ByteArrayQueue queue; 

	public L33tTransformation(){
    	stream = null;
    	queue = new ByteArrayQueue();
    }

	public void transform(byte[] bytes){
		if(TransformationChain.getInstance().transformationActivated("L33t")){
			return;
		}
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
	
	public void addElements(byte[] data){
		queue.write(data);
	}
	
	//force: true => the queue contains the EOF, in this case it will consume the whole
	//queue.
	public byte[] gzipedConvert(boolean force) throws IOException{
		if(stream == null){
			stream = new GZIPInputStream(queue);
		}
		byte[] test = new byte[1024];
		int datasize = 0;
		//512 size of buffer
		if(queue.available() > 512 || force){
			datasize = stream.read(test, 0, 1024);
			transform(test);
		}
		if(datasize == -1){
			return new byte[0];
		}
		return Arrays.copyOf(test, datasize);
	}
	
}
