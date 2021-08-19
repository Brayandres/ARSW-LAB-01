package edu.eci.arsw.blacklistvalidator;

import java.util.ArrayList;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class CheckSegmentHost implements Runnable{
	
	public final Thread ownThread;
    
    private int start;
    private int finish;
    private boolean hasBeenFinalized;
    private String ipAddress;
    private HostBlacklistsDataSourceFacade skds;
    private ArrayList<Integer> blackListOcurrences;
    
    public CheckSegmentHost(int start, int finish, String ipAddress) {
        ownThread = new Thread(this);
        this.start = start;
        this.finish = finish;
        this.ipAddress = ipAddress;
        hasBeenFinalized = false;
        blackListOcurrences = new ArrayList<>();
        skds = HostBlacklistsDataSourceFacade.getInstance();
    }

    @Override
    public void run() {
        for (int i = start; i <= finish; i++){
            if (skds.isInBlackListServer(i, ipAddress)){
                blackListOcurrences.add(i);
            }
        }
        synchronized (this) {
        	hasBeenFinalized = true;
        	notify();
        }
    }

    public synchronized int getOcurrencesCount() {
    	try {
    		while (!hasBeenFinalized) {
    			wait();
    		}
    	} catch (InterruptedException e) {
    		System.out.println(ownThread.getName()+" Interrupted...");
    	}
        return blackListOcurrences.size();
    }

    public ArrayList<Integer> getBlackListOcurrences(){
    	try {
    		while (!hasBeenFinalized) {
    			wait();
    		}
    	} catch (InterruptedException e) {
    		System.out.println(ownThread.getName()+" Interrupted...");
    	}
    	return blackListOcurrences;
    }
    
    /*
    public static void main(String a[]) throws InterruptedException {
        CheckSegmentHost search1 = new CheckSegmentHost(20, 1000, "200.24.34.55");
        search1.ownThread.start();
        System.out.println("The host was found : "+search1.getOcurrencesCount()+" times.");
        System.out.println("The host was found in : "+search1.getBlackListOcurrences());
    }*/
}