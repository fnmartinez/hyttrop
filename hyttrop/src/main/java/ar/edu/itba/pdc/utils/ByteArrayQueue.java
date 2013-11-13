package ar.edu.itba.pdc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ArrayUtils;

public class ByteArrayQueue extends InputStream {

	private AtomicReference<byte[]> arr;
	
	public ByteArrayQueue() {
		arr = new AtomicReference<byte[]>(new byte[0]);
	}

	public synchronized void write(byte[] elems) {
		arr.set(ArrayUtils.addAll(arr.get(), elems));
	}

	public synchronized int read(byte[] resp) {
		int size = 0;

		if(arr.get().length <= resp.length){
			size = arr.get().length;
		}else{
			size = resp.length;
		}

		byte[] newArr = ArrayUtils.subarray(arr.get(), 0, size);
		for(int i = 0; i < size; i++){
			resp[i] = newArr[i];
		}
		
		if(size < arr.get().length){
			arr.set(ArrayUtils.subarray(arr.get(), size, arr.get().length));
		}else{
			arr.set(new byte[0]);
		}
		return size;
	}
	
	public int available(){
		return arr.get().length;
	}

	public void close(){
	}
	
	public void mark(int readlimit){
	}
	
	public boolean markSupported(){
		return false;
	}
	
	public int read(byte[] b, int off, int len){
		if(arr.get().length <= len){
			len = arr.get().length;
		}

		byte[] newArr = ArrayUtils.subarray(arr.get(), 0, len);
		for(int i = 0; i < len; i++){
			b[i] = newArr[i];
		}
		
		if(len < arr.get().length){
			arr.set(ArrayUtils.subarray(arr.get(), len, arr.get().length));
		}else{
			arr.set(new byte[0]);
		}
		return len;
	}
	
	public void reset(){
	}
	
	public long skip(long n){
		return 0l;
	}
	
	
	@Override
	public int read() throws IOException {
		int data = (arr.get()[0] & 0xff);
		arr.set(ArrayUtils.remove(arr.get(), 0));
		return data;
	}

}
