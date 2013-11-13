package ar.edu.itba.pdc.transformations;

import java.util.LinkedList;
import java.util.List;

public class TransformationChain {
    private List<Transformation> transformations;
    
    private List<String> classes;

    private static TransformationChain INSTANCE = null;

    private TransformationChain(){
        this.transformations = new LinkedList<Transformation>();
        this.classes = new LinkedList<>();
    }

    private synchronized static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TransformationChain();
        }
    }

    public static TransformationChain getInstance() {
        createInstance();
        return INSTANCE;
    }

    public void transform(byte[] message) {
        for(Transformation t: transformations) {
            t.transform(message);
        }
    }
    
    public void addL33t(){
    	this.classes.add("L33t");
    }
    
    public boolean transformationActivated(String name){
    	return !this.classes.contains(name);
    }
    
    public void removeL33t(){
    	this.classes.remove("L33t");
    }
    
    public void add(Transformation t) {
        this.transformations.add(t);
    }

    public void remove(Transformation t) {
        this.transformations.remove(t);
    }

    public int countTransformations(){
        return transformations.size();
    }
}