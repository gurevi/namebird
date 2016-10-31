/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.words;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author Gurevich
 */
public class QualityControl implements Filter {
    WordGenerator wordgen;
    
    ArrayList<Integer> tokenArr = new ArrayList<Integer>();
    
    public QualityControl(WordGenerator wg){
        wordgen = wg;
    }
    
    public boolean test(String word, boolean addsuff, boolean addpref, boolean connectdots, String root) {
        
        if(!connectdots) {
            if(!wordTokenizer(word, addsuff, addpref, root)) {
                return false;
            } 
        }
        else {
            if(!qualForDots(word)) {
                return false;
            }
        }
        return true; // 'o' == word.charAt(3);
    }
    
    public boolean wordTokenizer(String inword, boolean addsuff, boolean addpref, String root) {
        //System.out.println(inword);
        boolean hasqualityending;
        boolean hasqualityendingbe;
        boolean endsin4gramword = false;
        boolean starts3gram = false;
        int len = inword.length(); 
        int lenroot = root.length();
        int lendif = len - lenroot;
        boolean greater3dif = false;
        boolean greater2dif = false;
        if(lendif > 3) greater3dif = true;
        if(lendif > 2) greater2dif = true;
        if(len <= 5) {
            return false;
        }
        if(lenroot > 3) {
            if(wordgen.dictionary.contains(root)) { 
                if(addpref == false) {
                    String lastletters = inword.substring(lenroot);
                    if (lastletters.length() > 3){
                        if(wordgen.dictionary.contains(lastletters)) {
                            return true;
                        }
                    }
                }
                if(addsuff == false) {
                    String firstletters = inword.substring(0, lendif);
                    if (firstletters.length() > 3){
                        if(wordgen.dictionary.contains(firstletters)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        
        if(len > 7 || addpref){
            String last4gram = inword.substring(inword.length() - 4);
            Ngram endingcheck4let = wordgen.ngramMap.get(last4gram);
            if(endingcheck4let != null) {
                if(wordgen.dictionary.contains(last4gram) || endingcheck4let.q4ending && len > 6) {
                    endsin4gramword = true;
                }
            }
        }
        
        String[] gram2array = new String[len - 1];
        String[] gram3array = new String[len - 2];
        int[] gram3iarray = new int[len - 2];
        int[] gram4iarray = new int[len - 3];
        int[] gram5iarray = new int[len - 4];
        int[] gram2iarray = new int[4];
        String[] gram4array = new String[len - 3];
        String[] gram5array = new String[len - 4];
        
        String last2gram = inword.substring(inword.length() - 2);
        String first2gram = inword.substring(0,2);
        Ngram endingcheck = wordgen.ngramMap.get(last2gram);
        Ngram startingcheck = wordgen.ngramMap.get(first2gram);
        if(endingcheck != null) {
            if(endingcheck.q2ending || endingcheck.q2endingbe) {
                hasqualityending = true;
            }
            else {
                hasqualityending = false;
            }

            if(endingcheck.q2endingbe) {
                hasqualityendingbe = true;
            }
            else {
                hasqualityendingbe = false;
            }
        }
        else {
            hasqualityending = false;
            hasqualityendingbe = false;
        }
        
        if(addpref == true && startingcheck != null) {
            if(startingcheck.gtotal < 1000) {
                //System.out.println("Lox");
                //System.out.println(inword);
                return false;
            }
            if(startingcheck.gfirst < 30) {
                //System.out.println("FLox");
                //System.out.println(inword);
                return false;
            }
            if(startingcheck.gtotal > 75000 && startingcheck.gfirst < 230) {
                //System.out.println("SaLox");
                //System.out.println(inword);
                return false;
            }
            if(startingcheck.q2badstart) {
                //System.out.println("Stae");
                //System.out.println(inword);
                return false;
                       
            }
        }
        
        
        
        
        for(int vi = 2; vi <= 5; vi++) {
            int BASELENGTH = vi;
            if (len < BASELENGTH) {
                break;
            }
            int extra = len - BASELENGTH;
            for (int ii = 0; ii <= extra; ++ii) {
                String base = inword.substring(ii, ii + BASELENGTH);
                
                //System.out.println(base);
                if(vi ==2) {
                    gram2array[ii] = base;
                }
                if(vi == 3) {
                    gram3array[ii] = base;
                }
                if(vi == 4) {
                    gram4array[ii] = base;
                } 
                if(vi == 5) {
                    gram5array[ii] = base;
                } 
               
               
            }
        }
        int extra = len - 3;
        for (int ii = 0; ii <= extra; ++ii) {
            String curbase = gram3array[ii];
           
            Ngram ngram = wordgen.ngramMap.get(curbase);
            
            if(ngram == null){
               gram3iarray[ii] = 0;
            }
            else {
               gram3iarray[ii] = ngram.gtotal;
            }
        }
        String curbase3 = gram3array[len - 3];
        Ngram fin3gram = wordgen.ngramMap.get(curbase3);
        String curstar3 = gram3array[0];
        Ngram first3gram = wordgen.ngramMap.get(curstar3);
        if(first3gram !=null) { if(first3gram.q3starter) { starts3gram = true; } }
        if(fin3gram != null) {
            
            if(addsuff != false) {
                    if(fin3gram.gtotal < 4500) {
                        int egg2score = 40;
                        if(len < 7) { egg2score = 10; }
                        if (fin3gram.glast < 40) {
                            //System.out.println("Egg2");
                            //System.out.println(inword);
                            return false;
                        }  
                    }
                    if(fin3gram.glast < 25) {
                        //System.out.println("Egg");
                        //System.out.println(inword);
                        return false; 
                    } 
            }
        }
        else if (addsuff){
              //System.out.println("NoEgg");
              //System.out.println(inword);
              return false; 
        }
        int bimMinval = 2950;
        if(len == 6) {
           bimMinval = bimMinval + 30;
        }
        if(starts3gram && len != 6) {
            if(addpref && addsuff) {
                //was 350 then 750
                bimMinval = bimMinval + 1550;
            }
            if(addpref && !addsuff) {
                //was 580 then 980
                bimMinval = bimMinval + 2580;     
            }
        }
        if(!addsuff) {
            bimMinval = bimMinval + 1350;
        }
        Arrays.sort(gram3iarray);
        if(!hasqualityending) {
            if(endsin4gramword) {
                //was 3500, switched to add to previous
                bimMinval = bimMinval + 550;
           }
            if(gram3iarray[0] > bimMinval) {
                //System.out.println("Bimt");
                //System.out.println(inword);
                return false;
            }
        }
        else {
           if(endsin4gramword) {
               //was 5100 7/9/14
                bimMinval = bimMinval + 2150;
                //System.out.println("HighBim");
                //System.out.println(inword);
           }
           else {
               //was 4250
                bimMinval = bimMinval + 1300;
           }
           if(gram3iarray[0] > bimMinval && (greater2dif || len == 6)) {
                //System.out.println("Bimr");
                //System.out.println(inword);
                return false;
            } 
        }
        
        if(len >= 9 && greater2dif) {
            
            int badge2min = 9500;
            int bimlongmin = 4800;
            if(!addsuff) {
                bimlongmin = bimlongmin + 1000;
            }
            if(endsin4gramword) {
                bimlongmin = bimlongmin + 650;
                badge2min = 6500;
                //if(gram3iarray[len - 5] < 9500 && gram3iarray[len - 5] > 6500) {
                    //System.out.println("B2Pass");  
                    //System.out.println(inword);
                //}
            }
            
            if(gram3iarray[1] > bimlongmin && lendif >= 5) {
                //System.out.println("BimLong");
                //System.out.println(inword);
                return false;
            }
            
            if(gram3iarray[len - 5] < badge2min && lendif > 4) {
              //System.out.println("Badge2");
              //System.out.println(inword);
              return false;  
            }
            int tootsMin = 9693;
            if(endsin4gramword) {
                tootsMin = 15000;
                //if(gram3iarray[2] > 9693 && gram3iarray[2] < 15000) {
                    //System.out.println("TPass");  
                    //System.out.println(inword);
                //}
            }
            if(gram3iarray[2] > tootsMin && greater3dif) {
              //System.out.println("Toots");
              //System.out.println(inword);
              return false;  
            }
        }
        else if(gram3iarray[len -3] < 9000 && greater2dif) {
           //System.out.println("Badge3");
           //System.out.println(inword); 
        }
        
        if(len >= 7) {
            extra = len - 2;
            for (int ii = extra -3; ii <= extra; ++ii) {
                String curbase = gram2array[ii];
                Ngram ngram = wordgen.ngramMap.get(curbase);
                
                if(ngram == null){
                    gram2iarray[ii - extra + 3] = 0;
                }
                else {
                    gram2iarray[ii - extra + 3] = ngram.gtotal;
                }
            }
            Arrays.sort(gram2iarray);
            
            int yarrvalue = 90000;
            
            if(hasqualityendingbe) {
                yarrvalue = 72000;
            }
            
            if(endsin4gramword) {
                yarrvalue = 0;
            }
            
            if(gram2iarray[2] < yarrvalue && greater2dif && addsuff) {
                //System.out.println("Yarr");
                //System.out.println(inword);
                return false;
            }
        }
        
        extra = len - 5;
        if(endsin4gramword) {
            //8/13 change to len - 7?
            if(addsuff == true) extra = len - 6;
            else if(lenroot < 3) extra = len -6;
        }
        int extravary = 0;
        if(addsuff == false && lenroot > 2) {
            extravary = lenroot - 2;
        }
        int addboostto3 = 0;
        if(starts3gram && addsuff == false) { 
            addboostto3 = 2;
        }
  //Experimental rule 7/25 - add that the root has to be in the first 3 points?
        /*
        if(lenroot == 3 && addsuff != false) {
            addboostto3 = 1;
        } */
        
        if (lenroot > 3 && addpref == false) {
            addboostto3 = lenroot - 2;
        }
       
        for (int ii = 0 + addboostto3; ii <= extra - extravary; ++ii) {
            String curbase = gram5array[ii];
            Ngram ngram = wordgen.ngramMap.get(curbase);
            boolean q5vcount = false;
            if(ngram == null){
               gram5iarray[ii] = 0;
               int countvowels = 0;
               for(int iva = 0; iva < 5; ++iva) {
                    char thisletter = curbase.charAt(iva);
                    if(thisletter == 'a' || thisletter == 'e' || thisletter == 'i' || thisletter == 'o' 
                            || thisletter == 'u' || thisletter == 'y') {
                        countvowels++;
                    }
                }
                if(countvowels == 1) {
                    q5vcount = true;
                }
            }
            else {
               gram5iarray[ii] = ngram.gtotal;
               q5vcount = ngram.q5vcount;
            }
            
            if(q5vcount){
                if(len <= 7) {
                    if(gram5iarray[ii] <= 2) {
                        //System.out.println("Wash1");
                        //System.out.println(inword);
                        return false;
                    }
                }
                else {
                    if(gram5iarray[ii] <= 25) {
                        //System.out.println("Wash");
                        //System.out.println(inword);
                        return false;
                    }
                }
            }
            
        }
        
        extra = len - 4;
        for (int ii = 0; ii <= extra; ++ii) {
            String curbase = gram4array[ii];
            Ngram ngram = wordgen.ngramMap.get(curbase);
            if(ii == 0 && addsuff == false) {
                if(wordgen.dictionary.contains(curbase)) {
                    addboostto3 = 2;
                }
            }
            if(ngram == null){
               gram4iarray[ii] = 0;
            }
            else {
               gram4iarray[ii] = ngram.gtotal;
            }
        }
        
        if(addsuff == false && lenroot < 3) {
            extravary = 1;
        }
        if(endsin4gramword) {
            if(addsuff == true) extra = len - 6;
            else if(lenroot < 3) extra = len - 6;
        } 
        
        for(int ii = 0 + addboostto3; ii <= extra - extravary; ++ii) {
            if(extra - ii >= 2 && len > 6) {
                
                if(((gram4iarray[ii] < 400) && (gram4iarray[ii] > 10)) && 
                   ((gram4iarray[ii + 1] < 330) && (gram4iarray[ii + 1] > 10)) &&
                   ((gram4iarray[ii + 2] < 330) && (gram4iarray[ii + 2] > 10))) {
                    //System.out.println("Skipper");
                    //Modified to skipper: was 330 10, 330 10, 330 10
                    //System.out.println(inword);
                    return false;
                }
            } /*
            if(extra - ii >= 2 && len > 6 && addsuff == false) {
                
                if(((gram4iarray[ii] < 400) && (gram4iarray[ii] > 10)) && 
                   ((gram4iarray[ii + 1] < 330) && (gram4iarray[ii + 1] > 10)) &&
                   ((gram4iarray[ii + 2] < 330) && (gram4iarray[ii + 2] > 10))) {
                    System.out.println("Skipper");
                    //Modified to skipper: was 330 10, 330 10, 330 10
                    System.out.println(inword);
                    return false;
                }
            } */
            if(extra - ii >= 2) {
                if(gram4iarray[ii] > 1000 && gram4iarray[ii + 1] > 1000) {
                    if(gram5iarray[ii] < 30) {
                      // System.out.println("Plop");
                        
                      //  System.out.println(inword);
                        return false; 
                    }
                }
                
                
                Ngram to4check = wordgen.ngramMap.get(gram4array[ii]);
                if(to4check != null) {
                        if(to4check.q4v1 && to4check.gtotal < 500 && to4check.gtotal > 50) {
                            Ngram to4check2 = wordgen.ngramMap.get(gram4array[ii + 1]);
                            if(to4check2 != null ){
                                if(to4check2.q4v2 && to4check2.gtotal < 500 && to4check2.gtotal > 50) {
                                    //System.out.println("WKorn");
                                    //System.out.println(inword);
                                    return false; 
                                }
                            }

                        }
                }
                
            }
            
            String curgram = gram4array[ii];
            Ngram ngram = wordgen.ngramMap.get(curgram);
            if(ngram != null) {
                if(ngram.q4v1 || ngram.q4v2 || ngram.q4v3) { 
                   if(gram4iarray[ii] < 91) {
                       if(ii > 0 ) {
                        int lowiminq = 800;
                        if(hasqualityendingbe && ii > len - 5) {
                            lowiminq = 300;
                        }   
                        if(gram4iarray[ii - 1] < lowiminq) {
                            //System.out.println("Lowi");
                            //System.out.println(inword);
                            return false;
                        }
                       }
                   }
                }
            }
            
        }
        
        if(len >=8 && greater3dif){
            Arrays.sort(gram4iarray);
            if(gram4iarray[0] < 20) {
                gram4iarray[0] = gram4iarray[0] + 480;
                Arrays.sort(gram4iarray);
            }
            if(gram4iarray[3] < 1000 && gram4iarray[1] < 500) {
                //System.out.println("Sigg");
                //System.out.println(inword);
                return false;
            }
        }
        
 
        
       // System.out.println(gram3iarray[2]);
        
        return true;
    }
    
    public boolean qualForDots(String inword) {
        
        int len = inword.length();
        
        if(len < 6) { return false; }
        String[] gram2array = new String[len - 1];
        String[] gram3array = new String[len - 2];
        int[] gram3iarray = new int[len - 2];
        int[] gram4iarray = new int[len - 3];
        int[] gram5iarray = new int[len - 4];
        int[] gram2iarray = new int[4];
        String[] gram4array = new String[len - 3];
        String[] gram5array = new String[len - 4];
        
        for(int vi = 2; vi <= 5; vi++) {
            int BASELENGTH = vi;
            if (len < BASELENGTH) {
                break;
            }
            int extra = len - BASELENGTH;
            for (int ii = 0; ii <= extra; ++ii) {
                String base = inword.substring(ii, ii + BASELENGTH);
                
                //System.out.println(base);
                if(vi ==2) {
                    gram2array[ii] = base;
                }
                if(vi == 3) {
                    gram3array[ii] = base;
                }
                if(vi == 4) {
                    gram4array[ii] = base;
                } 
                if(vi == 5) {
                    gram5array[ii] = base;
                } 
               
               
            }
        }
        
        int extra;
        extra = len - 4;
        
        for (int ii = 0; ii <= extra; ++ii) {
            String curbase = gram4array[ii];
            Ngram ngram = wordgen.ngramMap.get(curbase);
            
            if(ngram == null){
               gram4iarray[ii] = 0;
            }
            else {
               gram4iarray[ii] = ngram.gtotal;
            }
        }
        
        
        for(int ii = 1; ii <= extra; ++ii) {
            
            if(extra - ii >= 2 && len > 7) {
                
                if(((gram4iarray[ii] < 400) && (gram4iarray[ii] > 10)) && 
                   ((gram4iarray[ii + 1] < 330) && (gram4iarray[ii + 1] > 10)) &&
                   ((gram4iarray[ii + 2] < 330) && (gram4iarray[ii + 2] > 10))) {
                    //System.out.println("Skipper");
                    //Modified to skipper: was 330 10, 330 10, 330 10
                    //System.out.println(inword);
                    return false;
                }
            } 
            if(extra - ii >= 2) {

                Ngram to4check = wordgen.ngramMap.get(gram4array[ii]);
                if(to4check != null) {
                        if(to4check.q4v1 && to4check.gtotal < 500 && to4check.gtotal > 50) {
                            Ngram to4check2 = wordgen.ngramMap.get(gram4array[ii + 1]);
                            if(to4check2 != null ){
                                if(to4check2.q4v2 && to4check2.gtotal < 500 && to4check2.gtotal > 50) {
                                    //System.out.println("WKorn");
                                    //System.out.println(inword);
                                    return false; 
                                }
                            }

                        }
                }
                
            }
            
            String curgram = gram4array[ii];
            Ngram ngram = wordgen.ngramMap.get(curgram);
            if(ngram != null) {
                if(ngram.q4v1 || ngram.q4v2 || ngram.q4v3) { 
                   if(gram4iarray[ii] < 91) {
                       if(ii > 0 ) {
                        int lowiminq = 800;
                        if(gram4iarray[ii - 1] < lowiminq) {
                            //System.out.println("Lowi");
                            //System.out.println(inword);
                            return false;
                        }
                       }
                   }
                }
            }
            
        }  
        
        return true;
    }
}
