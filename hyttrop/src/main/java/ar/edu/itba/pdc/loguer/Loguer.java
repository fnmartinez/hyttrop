package ar.edu.itba.pdc.loguer;

public class Loguer {
	
	private Loguer instance = null;
	
	public synchronized Loguer getInstance(){
		if(instance == null){
			this.instance = new Loguer();
		}
		return this.instance;
	}
	
	private Loguer(){}
	
	public void addConnection(String origin, String dest){
		
	}
	
	public void addBytes(String origin, Integer length){
		
	}
	
	public void addStatusCode(String origin, String code){
		
	}
	
	
}
