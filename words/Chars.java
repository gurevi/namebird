/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.words;

import java.util.Arrays;

/**
 *
 * @author Gurevich
 */
public class Chars {
    byte[] chars;
    byte[] weights;
    int [] counts = new int[27];
    
    void makeZeroChars(){
        chars = new byte[26];
        for(byte ii=0; ii<26; ++ii){
            chars[ii] = ii;
        }

        for(int ii=0; ii<26; ++ii){
            counts[ii] = 1;
        }
        counts[26] = 0;
        
        total = 26;
    }
    
    int total;
    int fendofword;

    void add(char pref) {
           ++ counts[WordGenerator.toByte(pref)];  
           ++ total;
    }
    
    void addend() {
        ++ counts[26];
        ++ total;
    }
    
    
    
    char randomChar() {
        if(chars==null || chars.length == 0)
            return WordGenerator.TERMINATOR;
        else if(chars.length == 1)
            return WordGenerator.charTable[chars[0]];
        
        int range = chars.length;
        int off = (range==1) ? 0 : WordGenerator.rand.nextInt(range);
        return WordGenerator.charTable[chars[off]];
    }
    
    char randomWeighedChar(int startorend){
        /*if(chars==null || chars.length == 0)
            return WordGenerator.TERMINATOR;
        else if(chars.length == 1)
            return WordGenerator.charTable[chars[0]]; */
            
        if(weights != null && weights.length <=1)
            return randomChar();
        
        int range = 0;
        
        if(total == 0) {
            return WordGenerator.TERMINATOR;
        }
        int randtofindl = WordGenerator.rand.nextInt(total) + 1;
       
        int chosenletter = 0;
        for(int ivi = 0; ivi <= 26; ++ivi) {
            randtofindl -= counts[ivi];
            if(randtofindl <= 0) {
                chosenletter = ivi;
                
                break;
            }
        }
        /*
        if(startorend==7) {
           if(counts[chosenletter] )  
        } */
        /*
        if(weights == null){
            //range = WordGenerator.rand.nextInt(chars.length) + 1; 
            range = chars.length;
        } else {
            int wlen = weights.length;
            range = weights[WordGenerator.rand.nextInt(wlen)];
        }

        int off = (range==1) ? 0 : WordGenerator.rand.nextInt(range);
                */
        if(startorend == 5 && chosenletter == 26) {
            randtofindl = randtofindl + 1;
        }
        if(chosenletter == 26) {
                    return WordGenerator.TERMINATOR;
        }
        return WordGenerator.charTable[chosenletter];
    }
    
    void normalize(){
        if(total == 0)
            return;

        byte[] result = new byte[27];
        for(byte ii=0; ii<27; ++ii)
            result[ii] = ii;

        // find most popular with bubble sort
        // perculate max to the top

        int cur;
        int cutoff = 0;
        //15
        for(cur=0; cur<15; ++cur ){
            for(int jj=25; jj > cur; --jj){
                if(counts[result[jj-1]] < counts[result[jj]] ){
                    byte val = result[jj-1];
                    result[jj-1] = result[jj];
                    result[jj] = val;
                }
            }
            int curmax = counts[result[cur]];
            if(cur == 0){
                //39
                cutoff = curmax / 39;
                if(cutoff == 0)
                    cutoff = 1;
            }
            else if(curmax < cutoff)
                break;          
        } 
        
        chars = Arrays.copyOf(result, cur);
    }
}

