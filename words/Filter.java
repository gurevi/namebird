/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.words;

import java.util.regex.Pattern;

/**
 *
 * @author Gurevich
 */
public interface Filter {
    boolean test(String word, boolean addsuff, boolean addpref, boolean connectdots, String root);
    
    public class Regexp implements Filter 
    {
        public Regexp(String p){
            pattern = Pattern.compile(p);
        }
        private Pattern pattern;
        public boolean test(String word, boolean addsuff, boolean addpref, boolean connectdots, String root) {
            return  pattern.matcher(word).matches();
        }
    }
}
