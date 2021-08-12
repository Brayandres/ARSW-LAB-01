package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SegmentSearch extends Thread{

    private static final int BLACK_LIST_ALARM_COUNT=5;
    private int start;
    private int finish;
    private String ipaddress;
    private static volatile int blackCount;

    public SegmentSearch(int start, int finish, String ipaddress) {
        this.start = start;
        this.finish = finish;
        this.ipaddress = ipaddress;
    }

    @Override
    public void run() {
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();

        int ocurrencesCount=0;

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

        int checkedListsCount=0;

        for (int i=start;i<=finish && ocurrencesCount<BLACK_LIST_ALARM_COUNT;i++){
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)){

                blackListOcurrences.add(i);

                ocurrencesCount++;


            }
            System.out.println(i);
        }

        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }
        blackCount += ocurrencesCount;
        stop();
    }

    public static int getBlackCount() {
        return blackCount;
    }

    public static void main(String a[]) throws InterruptedException {
        SegmentSearch search1= new SegmentSearch(20,50, "200.24.34.55");
        SegmentSearch search2= new SegmentSearch(500,79000, "200.24.34.55");
        search1.run();
        search2.run();
        System.out.println("The host was found :"+blackCount);
    }
}
