/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.server;

import static com.shobia.server.RunServer.mylogger;
import com.shobia.words.ConnectRE;
import com.shobia.words.Filter;
import com.shobia.words.QualityControl;
import com.shobia.words.WordGenerator;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author Gurevich
 */
public class WordGeneratorInterface {
    
    enum RequestType {CN, RE, ST, EN, MD}
    
    WordGenerator wordgen;
    Filter filter;
    public WordGenerator getWordGenerator() {
        return wordgen;
    }
    
    public  WordGeneratorInterface()  {
        wordgen = new WordGenerator();
        filter = new QualityControl(wordgen);
        
        try {
            wordgen.loadDictionary("words.txt");
            //w.loadDictionary("english2.txt");
            //w.loadDictionary("wlist_match10.txt");

            wordgen.loadGData("ngrams4.csv");
            wordgen.loadGData("ngrams3.csv");
            wordgen.loadGData("ngrams5.csv");
            wordgen.loadGData("ngrams2.csv");        

            wordgen.finish();
        } catch(IOException ex){
            wordgen = null;
            mylogger.log(Level.SEVERE, ex.toString(), ex);
        } 
    } 
    
    static String  toString(Collection<String> result) {
        StringBuilder sb = new StringBuilder();
        
        Iterator<String> it = result.iterator();
        if(it.hasNext())
            sb.append(it.next());
        while(it.hasNext())
            sb.append(',').append(it.next());
        return sb.toString();
    }
    
    public static final String FieldSeparator = "\u00b6";
    public String get(String input){
        
        try {
            String[] args = input.split(FieldSeparator);

            RequestType type = RequestType.valueOf(args[0]);
            int count  = Integer.parseInt(args[1]);
            int minlen = Integer.parseInt(args[2]);
            int maxlen = Integer.parseInt(args[3]);
            String base = null;

            if(args.length > 4)
                base = args[4];

            Collection<String> result = null;

            switch(type){
                case CN:
                    if(args.length < 6){
                        throw new RuntimeException("expected 7 parameters: " + input);
                    }
                    String to = args[5];
                    String reg = ".*";
                    if(args.length > 6)
                        reg = args[6];
                    
                    String regexp = base + reg + to;
                    result = ConnectRE.matchRE(wordgen, regexp, minlen, maxlen, count);
                    break;

                case RE:
                    if(args.length != 5){
                        throw new RuntimeException("expected 5 parameters: " + input);
                    }
                    result = ConnectRE.matchRE(wordgen, base, minlen, maxlen, count);
                    break;

                case ST:    
                    result = wordgen.generateWords(base, true, false, filter, minlen, maxlen, count);
                    break;

                case EN:   
                    result = wordgen.generateWords(base, false, true, filter, minlen, maxlen, count);
                    break;

                case MD:
                    result = wordgen.generateWords(base, true, true, filter, minlen, maxlen, count);
                    break;
            }

            return toString(result);
        } catch(Exception ex){
            return '#' + ex.getMessage();
        }
    }
}
