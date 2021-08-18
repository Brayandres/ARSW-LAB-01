package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//Ejecutar clase con maven
//mvn exec:java -D"exec.mainClass"="edu.eci.arsw.blacklistvalidator.CheckSegmentHost"

public class CheckSegmentHost extends Thread{

    private static final int BLACK_LIST_ALARM_COUNT=5;
    private int start;
    private int finish;
    private String ipaddress;
    private int ocurrencesCount;
    private LinkedList<Integer> blackListOcurrences=new LinkedList<>();

    public CheckSegmentHost(int start, int finish, String ipaddress) {
        this.start = start;
        this.finish = finish;
        this.ipaddress = ipaddress;
        this.ocurrencesCount=0;
    }

    @Override
    public void run() {

        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

        int checkedListsCount=0;

        for (int i=start;i<=finish && ocurrencesCount<BLACK_LIST_ALARM_COUNT;i++){
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)){

                blackListOcurrences.add(i);

                ocurrencesCount++;

            }
            //System.out.println(i);
        }
        /*
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }*/
    }

    public int getOcurrencesCount() {
        return ocurrencesCount;
    }

    public LinkedList<Integer> getBlackListOcurrences(){
      return blackListOcurrences;
    }

    /*
    public static void main(String a[]) throws InterruptedException {
        CheckSegmentHost search1= new CheckSegmentHost(20,50, "200.24.34.55");
        CheckSegmentHost search2= new CheckSegmentHost(50000,80000, "200.24.34.55");
        search1.run();
        search2.run();
        System.out.println("The host was found :"+search1.getOcurrencesCount());
        System.out.println("The host was found :"+search2.getOcurrencesCount());
    }*/
}
