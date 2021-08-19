/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int N){
    	
    	int ocurrencesCount = 0;
        LinkedList<Integer> blackListOcurrences = new LinkedList<>();
        ArrayList<CheckSegmentHost> threads = new ArrayList<CheckSegmentHost>();
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        
        int serversPerThread = skds.getRegisteredServersCount()/N;
        System.out.println("serversPerThread: "+serversPerThread);
        int remainingServers = skds.getRegisteredServersCount() - (serversPerThread*N);
        System.out.println("remainingServers: "+remainingServers);
        
        if (remainingServers == 0) {
        	for (int i = 0; i < N; i++) {
        		threads.add(new CheckSegmentHost((i*serversPerThread)+1, (i+1)*serversPerThread, ipaddress));
        	}
        }
        else {
        	int starterSegment;
        	int lastSegment = 0;
        	for (int i = 0; i < remainingServers; i++) {
        		starterSegment = lastSegment + 1;
        		lastSegment = starterSegment + serversPerThread;
        		threads.add(new CheckSegmentHost(starterSegment, lastSegment, ipaddress));
        	}
        	starterSegment = remainingServers*(serversPerThread+1);
        	lastSegment = starterSegment + (serversPerThread-1);
        	for (int j = remainingServers; j < N; j++) {
        		threads.add(new CheckSegmentHost(starterSegment, lastSegment, ipaddress));
        		starterSegment = lastSegment + 1;
        		lastSegment = starterSegment + (serversPerThread-1);
        	}
        }
        
        for (CheckSegmentHost t : threads) {
            t.ownThread.start();
            System.out.println("Ejecutando Thread: " + t.ownThread.getName());
        }

        for (CheckSegmentHost t : threads) {
        	try {
            	t.ownThread.join();
            } catch (Exception e) {
        	    e.printStackTrace();
            }
            System.out.println("Terminó Thread: " + t.ownThread.getName());
        }    

        for (CheckSegmentHost t : threads) {
            ocurrencesCount += t.getOcurrencesCount();
            blackListOcurrences.addAll(t.getBlackListOcurrences());
        }
        
        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{skds.getRegisteredServersCount(), skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
 
}