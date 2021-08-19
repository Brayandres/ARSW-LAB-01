/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {

    public static void main(String a[]){
    	
    	
    	
        HostBlackListsValidator hblv = new HostBlackListsValidator();
        //List<Integer> blackListOcurrences = hblv.checkHost("200.24.34.55", 10);
        //List<Integer> blackListOcurrences = hblv.checkHost("202.24.34.55", 80);
        List<Integer> blackListOcurrences = hblv.checkHost("212.24.24.55", 80);
        System.out.println("The host was found in the following blacklists: "+blackListOcurrences);
        

        /*TIME TESTS
        Runtime r = Runtime.getRuntime();
    	System.out.println("Available Processors: "+r.availableProcessors());
        long initialTime, finalTime;
        initialTime = System.currentTimeMillis();
        List<Integer> blackListOcurrences0 = hblv.checkHost("212.24.24.55", 100);
        finalTime = System.currentTimeMillis();
        System.out.println("Tiempo en milisegundos para IP '212.24.24.55' con 100 Hilo(s): "+((finalTime - initialTime)));
        */
    }
}