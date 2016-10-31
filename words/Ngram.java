/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.words;

import java.util.HashSet;
import java.util.Set;

public class Ngram implements Comparable<Ngram> {
    String tag;
    Ngram(String tg){
        tag = tg;
    }

    public boolean goodStart(){
        return (begCount >0);
    }
    
    public boolean goodEnd(){
        return (endCount > 0);
    }
    int endCount;
    int begCount;
    int fullCount;

    Chars prefs = new Chars();
    Chars suffs = new Chars();
    
    Chars prefsstart = new Chars();
    Chars suffsend = new Chars();
    
    // google data
    int gtotal;
    int gfirst;
    int glast;

    boolean q4v1;
    boolean q4v2;
    boolean q4v3;
    boolean q5vcount;
    boolean q2badstart;
    boolean q2ending;
    boolean q2endingbe;
    boolean q4ending;
    boolean q3starter;
    
    Set<String> good4ending = new HashSet<String>();
    
    @Override
    public String toString() {
        return tag + '(' + fullCount + ',' + prefs.total + ',' + suffs.total + ')';
    }

    @Override
    public int hashCode() {
        return tag.hashCode(); // tag_int;
    }

    @Override
    public boolean equals(Object obj) {
        Ngram other = (Ngram) obj;
        return this.tag.equals(other.tag);  
    }

    public int compareTo(Ngram that) {
        return this.tag.compareTo(that.tag);
    }

    boolean  normalize(){
        int len = tag.length();
        if(len==2){
            Set<String> goodending = new HashSet<String>();
            
            goodending.add("on");
            goodending.add("it");
            goodending.add("ia");
            goodending.add("ta");
            goodending.add("ca");
            goodending.add("el");
            goodending.add("se");
            goodending.add("ty");
            goodending.add("fy");
            goodending.add("ot");
            goodending.add("ce");
            goodending.add("ch");
            goodending.add("to");
            goodending.add("za");
            goodending.add("xa");
            goodending.add("am");
            goodending.add("id");
            goodending.add("ze");
            goodending.add("ly");
            goodending.add("le");
            goodending.add("ay");
            goodending.add("um");
            goodending.add("ry");
            goodending.add("zy");
            goodending.add("na");
            goodending.add("is");
            goodending.add("er");
            goodending.add("ra");
            goodending.add("ie");
            goodending.add("te");
            goodending.add("la");
            goodending.add("st");
            goodending.add("re");
            goodending.add("ic");
            goodending.add("us");
            q2endingbe = goodending.contains(tag);
            
            float percentage1 = ((float)glast/gtotal);
            if (percentage1*100 > 18 && gtotal > 1500) {
                q2ending = true;
            } 
            
            float percentage2 = ((float)gfirst/gtotal);
            if (percentage2*1000 < 32 && gtotal > 1000 && gfirst < 600) {
                q2badstart = true;
            } 
        }
        
        if(len == 3) {
            float percentage3 = ((float)gfirst/gtotal);
            if (percentage3*100 > 22 && gtotal > 200) {
                q3starter = true;
            } 
        }
   
        if(len==4){
            q4v1 = tag.matches("[aeiouy].[aeiouy]."); 
            q4v2 = tag.matches(".[aeiouy].[aeiouy]");
            q4v3 = tag.matches("[aeiouy]..[aeiouy]");
            good4ending.add("abra");
            good4ending.add("lize");
            good4ending.add("gona");
            good4ending.add("tion");
            good4ending.add("illa");
            good4ending.add("mity");
            good4ending.add("vina");
            good4ending.add("zzle");
            good4ending.add("nate");
            good4ending.add("cron");
            good4ending.add("eous");
            good4ending.add("rity");
            good4ending.add("iary");
            good4ending.add("duce");
            good4ending.add("sify");
            good4ending.add("stry");
            good4ending.add("aise");
            good4ending.add("tina");
            good4ending.add("lexa");
            good4ending.add("erra");
            good4ending.add("usly");
            good4ending.add("uela");
            good4ending.add("nate");
            good4ending.add("dora");
            good4ending.add("onia");
            good4ending.add("edia");
            good4ending.add("izer");
            good4ending.add("lary");
            good4ending.add("rify");
            good4ending.add("rchy");
            good4ending.add("nody");
            good4ending.add("atim");
            good4ending.add("lody");
            good4ending.add("nkle");
            
            good4ending.add("onal");
            good4ending.add("lism");
            good4ending.add("itic");
            good4ending.add("cify");
            good4ending.add("ated");
            good4ending.add("anum");
            good4ending.add("tary");
            good4ending.add("appy");
            good4ending.add("mika");
            good4ending.add("ryla");
            good4ending.add("resa");
            good4ending.add("tony");
            good4ending.add("olio");
            good4ending.add("leus");
            good4ending.add("cula");
            good4ending.add("ably");
            good4ending.add("uire");
            q4ending = good4ending.contains(tag);
        }
        if(len==5) {
            int countvowels = 0;
            for(int iva = 0; iva < 5; ++iva) {
                char thisletter = tag.charAt(iva);
                if(thisletter == 'A' || thisletter == 'E' || thisletter == 'I' || thisletter == 'O' 
                        || thisletter == 'U' || thisletter == 'Y') {
                    countvowels++;
                }
            }
            if(countvowels == 1) {
                q5vcount = true;
            }
        }
         prefs.normalize();
         suffs.normalize(); 
         prefsstart.normalize();
         suffsend.normalize(); 
         
         return true;
    }
    
    static Ngram makeZeroGram(){ 
        
        Ngram zg = new Ngram("");
        zg.begCount = 1;
        zg.endCount = 1;
        zg.fullCount = 1;
        
        zg.suffs.makeZeroChars();
        zg.prefs.makeZeroChars();
                
        return zg;
    }
    
    static Ngram zeroGram = makeZeroGram();
}
