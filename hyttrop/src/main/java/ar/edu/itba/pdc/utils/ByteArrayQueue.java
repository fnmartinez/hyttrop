package ar.edu.itba.pdc.utils;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ArrayUtils;

public class ByteArrayQueue {

	private AtomicReference<byte[]> arr;
	
	public ByteArrayQueue() {
		arr = new AtomicReference<byte[]>(new byte[0]);
	}

	public synchronized void write(byte[] elems) {
		arr.set(ArrayUtils.addAll(arr.get(), elems));
		System.out.println("Array size: " + arr.get().length);
	}

	public synchronized Integer read(byte[] resp) {
		int size = 0;
		System.out.println(resp.length);
		System.out.println(arr.get().length);
		
		if(arr.get().length <= resp.length){
			size = arr.get().length;
		}else{
			size = resp.length;
		}
		System.out.println(size);
		
		byte[] newArr = ArrayUtils.subarray(arr.get(), 0, size);
		System.out.println(newArr.length);
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
	
	public Integer available(){
		return arr.get().length;
	}

}
