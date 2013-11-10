package ar.edu.itba.pdc.transformations;

import java.util.LinkedList;
import java.util.List;

public class TransformationChain {
    private List<Transformation> transformations;

    private static TransformationChain INSTANCE = null;

    private TransformationChain(){
        this.transformations = new LinkedList<Transformation>();
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