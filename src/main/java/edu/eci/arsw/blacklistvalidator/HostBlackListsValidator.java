/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
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
    public List<Integer> checkHost(String ipaddress, float N){

        LinkedList<Integer> blackListOcurrences=new LinkedList<>();

        int ocurrencesCount=0;

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

        int checkedListsCount=0;

        float nServers = skds.getRegisteredServersCount()/N;

        List<CheckSegmentHost> threads = new LinkedList<CheckSegmentHost>();

        if ((nServers % 1) != 0){
              nServers -= (nServers % 1);
              threads.add(new CheckSegmentHost((int)(N*nServers)+1,skds.getRegisteredServersCount(), ipaddress));
        }

        for (int i=0;i<N;i++){
              threads.add(new CheckSegmentHost((int)((i*nServers)+1),(int)((i+1)*nServers), ipaddress));
        }

        for (CheckSegmentHost t : threads) {
            t.start();
            System.out.println("Ejecutando Thread" + t);
        }

        for (CheckSegmentHost t : threads) {
          try {
                t.join();
          } catch (Exception e) {
                e.printStackTrace();
          }
            System.out.println("Terminó Thread" + t);
        }


        for (CheckSegmentHost t : threads) {
            ocurrencesCount += t.getOcurrencesCount();
            blackListOcurrences.addAll(t.getBlackListOcurrences());
        }

        /*
        for (int i=0;i<skds.getRegisteredServersCount() && ocurrencesCount<BLACK_LIST_ALARM_COUNT;i++){
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)){

                blackListOcurrences.add(i);

                ocurrencesCount++;
            }
        }
        */
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        //LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences;
    }


    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());



}
