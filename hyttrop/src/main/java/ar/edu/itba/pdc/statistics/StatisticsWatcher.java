package ar.edu.itba.pdc.statistics;

import java.util.HashMap;

public class StatisticsWatcher {

    private static StatisticsWatcher INSTANCE = null;
    private static boolean running = false;
    private int timeRunning = 0;
    private int time;
    private HashMap<Integer, Integer> statusCodeStatistics = new HashMap<>();
    private int bytesTransferred = 0;
    private int connectionsQty = 0;

    private StatisticsWatcher(){
    }

    public void resetStatistics(){

        this.timeRunning = 0;
        this.bytesTransferred = 0;
        this.connectionsQty = 0;
        this.statusCodeStatistics = new HashMap<>();
    }

    private synchronized static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StatisticsWatcher();
        }
    }

    public static StatisticsWatcher getInstance() {
        createInstance();
        return INSTANCE;
    }

    public boolean isRunning(){
        return this.running;
    }

    public void start(){
        time = ((int)System.currentTimeMillis())/1000;
        this.running = true;
    }

    public void stop(){
        time = (((int)System.currentTimeMillis())/1000) - time;
        timeRunning += time;
        time = 0;
        this.running = false;
    }

    public void updateStatusCodeStatistics(int statusCode){
        if(!this.statusCodeStatistics.containsKey(statusCode)){
            this.statusCodeStatistics.put(statusCode, 0);
        }
        this.statusCodeStatistics.put(statusCode, this.statusCodeStatistics.get(statusCode) + 1);
    }

    public void updateBytesTransferred(int transferred){
        this.bytesTransferred += transferred;
    }

    public void updateConnectionsQty(){
        this.connectionsQty += 1;
    }

    public String getStatistics(){
        int currentTime = (int)System.currentTimeMillis()/1000;
        String statistics = "\n\t>>>>>>>>>>>>Tiempo de muestreo: " + ((currentTime - time) + timeRunning) + " segundos.\n";
        statistics = statistics.concat("\n\t>>>>>>>>>>>>Bytes transferidos: " + bytesTransferred + "\n");
        statistics = statistics.concat("\n\t>>>>>>>>>>>>Cantidad de conexiones: " + connectionsQty + "\n");
        statistics = statistics.concat("\n\t>>>>>>>>>>>>Histograma de status codes: \n");
        if(statusCodeStatistics.size() == 0){
            statistics = statistics.concat("\n\t\tSin capturas.\n");
        }

        for(int code : statusCodeStatistics.keySet()){
            statistics = statistics.concat("\n\t\tStatus code: " + code + " -> " + statusCodeStatistics.get(code));
        }
        statistics = statistics.concat("\n");
        return statistics;
    }
}
