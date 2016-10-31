/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shobia.words;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Gurevich
 */
public class ConnectRE {

    int reMin;
    int reMax;

    int begLen;
    int endLen;
    int constLen;

    String   from;
    String   to;

    RegExp   regexp;
    boolean  forward;

    boolean isStart;
    boolean  isEnd;
    
    ConnectRE(int begInd, int endInd, ArrayList<RegExp> list, List<String> anchors, boolean s, boolean e) {
        isStart = s;
        isEnd = e;
        
        from = anchors.get(begInd);
        begLen = 0;
        if (from != null) {
            begLen = from.length();
            begInd++;
        }

        to = anchors.get(endInd);
        endLen = 0;
        if (to != null) {
            endLen = to.length();
            endInd--;
        }

        constLen = begLen + endLen;

        // endInd+1 : inclusive-exclusive
        regexp = Util.concat(list, begInd, endInd + 1);

        forward = (begLen >= endLen);
        if (begLen > 1 || (begLen == 1 && endLen == 2)) {
            forward = true;
        }
        if (!forward) {
            regexp = Util.reverse(regexp);
        }

        // modify min and max
        reMin = Util.getMin(regexp);
        if (reMin > 20) {
            throw new RuntimeException(regexp.toString() + ": out of bound range: min > 20");
        }

        reMax = Util.getMax(regexp);
        if (reMin > reMax) {
            throw new RuntimeException(regexp.toString() + ": invalid range: min > max");
        }
    }

    Collection<String> run(WordGenerator w, int min, int max, int howmany) {
        State state = null;
        if (!Util.isAnyChar(regexp)) {
            Automaton aut = Util.toAutomaton(regexp);
            state = aut.getInitialState();
        }

        if (min < reMin) {
            min = reMin;
        }

        if (max > reMax) {
            max = reMax;
        }

        if (min > max) {
            throw new RuntimeException("min > max)");
        }

        return connectRE(w, forward, state, from, to, min, max, howmany, isStart, isEnd);
    }

    static int getMax(ArrayList<String> anchors, int from, int to) {
        int maxlen = -1;
        int maxind = -1;

        for (int ii = from; ii < to; ++ii) {
            String anchor = anchors.get(ii);
            if (anchor == null) {
                continue;
            }
            int curlen = anchor.length();
            if (curlen > maxlen) {
                maxlen = curlen;
                maxind = ii;
            }
        }
        return maxind;
    }

    enum ConnectType {OTHER, START, MIDDLE, END};
    static Collection<String> filterResult(WordGenerator w, ConnectType type, String root, int min, int max, Collection<String> result) {
        
        if(result == null || result.isEmpty())
              return result;
        
        Filter filter = new QualityControl(w);
        boolean addsuff=false;
        boolean addpref=false; 
        boolean connectdots=false; 
        
        switch(type) {
            case OTHER:
                addsuff = false; 
                addpref=false; 
                connectdots = true;
                break;
            case START:
                addsuff = true; 
                addpref=false; 
                connectdots = false;
                break;
            case MIDDLE:
                addsuff = true; 
                addpref=true; 
                connectdots = false;
                break;
            case END:
                addsuff = false; 
                addpref=true; 
                connectdots = false;
                break;
        }
        
        Iterator<String> it = result.iterator();
        
        while (it.hasNext()) {
            String name = it.next();
            int len = name.length();
            if (len >= min && len <= max && filter.test(name, addsuff, addpref, connectdots, root)) {
            } else {
                it.remove();
            }
        }
        return result;
    }
    
    public static Collection<String> matchRE(WordGenerator w, String regexpString, int min, int max, int howmany) {
         return matchRE(w, regexpString, min, max, howmany, true);
    }
    
    public static Collection<String> matchRE(WordGenerator w, String regexpString, int min, int max, int howmany, boolean filter) {
        int minlen = min;
        int maxlen = max;
        
        if (min > max) {
            throw new RuntimeException("min > max: min=" + min + " max=" + max);
        }
      
        if (regexpString == null || regexpString.isEmpty()) {
            throw new RuntimeException("empty input");
        }
      
        if (howmany <= 0) {
            return Collections.emptyList();
        }

        RegExp exp = null;
        try {
            exp = new RegExp(regexpString, RegExp.NONE);
        } catch (Exception ex) {
            throw new RuntimeException("invalid regexp " + regexpString + ": " + ex.getMessage());
        }

        if (!Util.toLower(exp)) {
            throw new RuntimeException("invalid regexp " + regexpString + ": " + "contains non  letters");
        }

        exp = Util.optimize(exp, min, max);
        
        ArrayList<RegExp> list = Util.split(exp);

        int len = list.size();

        ArrayList<String> anchors = Util.extractAnchors(list);
        if(len == 1) {
            String name = anchors.get(0);
            if(name != null){
                return Collections.singletonList(name);
            }
        }
        
        int lastInd = len - 1;

        // find the longest anchor
        int midInd = getMax(anchors, 1, lastInd);
        if (midInd < 0) {
            // no anchors. just connect the start with the end
            ConnectRE re = new ConnectRE(0, lastInd, list, anchors, true, true);

            if (max < re.constLen + re.reMin) {
                throw new RuntimeException(regexpString + ": too long, max length=" + max);
            }
            if (min > re.constLen + re.reMax) {
                throw new RuntimeException(regexpString + ": too short, min length=" + min);
            }

            min -= re.constLen;
            max -= re.constLen;
            if (min < 0) {
                min = 0;
            }

            Collection<String> result = re.run(w, min, max, howmany);
     
            if(filter) {
                ConnectType type = ConnectType.OTHER;
                String root = "";
                if(re.begLen > 0){
                    if(re.endLen == 0){
                        type = ConnectType.START;
                        root = re.from;
                    } 
                } else if(re.endLen > 0){  
                    type = ConnectType.END;
                    root = re.to;
                }
                filterResult(w, type, root, minlen, maxlen, result);
            }
            return result;
        }

        String center = anchors.get(midInd);
        int centerLen = center.length();
        
        ConnectRE re1 = new ConnectRE(0, midInd, list, anchors, true, false);
        ConnectRE re2 = new ConnectRE(midInd, lastInd, list, anchors, false, true);

        int constLen = re1.constLen + re2.endLen;
        if (max < constLen + re1.reMin + re2.reMin) {
            throw new RuntimeException(regexpString + ": too long, max length=" + max);
        }
        if (min > constLen + re1.reMax + re2.reMax) {
            throw new RuntimeException(regexpString + ": too short, min length=" + min);
        }

        min -= constLen;
        max -= constLen;
        if (min < 0) {
            min = 0;
        }
        // distribute min and max between heads and tails
        int min1 = -1, min2 = -1;
        int max1 = -1, max2 = -1;

        boolean minSet = false;
        boolean maxSet = false;

        if (re1.reMin == re1.reMax) {
            min1 = re1.reMin;
            max1 = re1.reMax;

            min2 = min - min1;
            max2 = max - max1;

            minSet = maxSet = true;
        } else if (re2.reMin == re2.reMax) {
            min2 = re2.reMin;
            max2 = re2.reMax;

            min1 = min - min2;
            max1 = max - max2;

            minSet = maxSet = true;
        } else {
            if (min <= re1.reMin + re2.reMin) {
                min1 = re1.reMin;
                min2 = re2.reMin;
                min = min1 + min2;
                minSet = true;
            }

            if (max >= re1.reMax + re2.reMax) {
                max1 = re1.reMax;
                max2 = re2.reMax;
                max = max1 + max2;
                maxSet = true;
            }
        }

        if (!minSet) {
            min1 = min2 = min / 3;
        }

        if (!maxSet) {
            max1 = max2 = max * 2 / 3;
            if (max1 < min1) {
                max1 = min1;
            }
            if (max2 < min2) {
                max2 = min2;
            }
        }

        int howmany1, howmany2;
        howmany1 = howmany2 = (int) Math.sqrt(2 * howmany);

        Collection<String> heads = re1.run(w, min1, max1, howmany1);
        Collection<String> tails = re2.run(w, min2, max2, howmany2);

        // combine
        Collection<String> result = null;
        if (heads == null) {
            result =  tails;
        }

        else if (tails == null) {
            result = heads;
        } else {
            result =  merge(heads, tails, centerLen);
        }
        
        if(filter){
            ConnectType type = ConnectType.OTHER;
            String root = "";
            if(re1.begLen == 0 && re2.endLen == 0 && centerLen > 1){
                type = ConnectType.MIDDLE;
                root = center;
            } 
          
            filterResult(w, type, root, minlen, maxlen, result);
        }
        return result;
    }

    static Collection<String> merge(Collection<String> heads, Collection<String> tails, int common) {
        Collection<String> result = new ArrayList<String>();
        for (String tail : tails) {
            tail = tail.substring(common);

            for (String head : heads) {
                result.add(head + tail);
            }
        }
        return result;
    }

    public static Collection<String> connectRE(WordGenerator w, boolean forward, State state,
            String from, String to, int min, int max, int howmany, boolean isStart, boolean isEnd) {
        
        Ngram sEntry = null;
        int fromLen = (from == null || from.isEmpty()) ? 0 : from.length();
        if (fromLen > 0) {
            sEntry = w.findEndEntry(from);
            if(fromLen >= WordGenerator.USEBASE)
                isStart = false;
            else if(sEntry.tag.length() != fromLen)
                isStart = false;
        }

        Ngram eEntry = null;
        int toLen = (to == null || to.isEmpty()) ? 0 : to.length();
        if (toLen > 0) {
            eEntry = w.findStartEntry(to);
            if(toLen >= WordGenerator.USEBASE )
                isEnd = false;
            else if(eEntry.tag.length() != toLen)
                isEnd = false;
        }

        Collection<String> connectors = new HashSet<String>();
        Connector con = new Connector(connectors);
        
        int chunk = howmany / 5;
        if(chunk == 0)
            chunk = 1;
        
        for (int ii = 0; ii < 10; ++ii) {
            if(con.limit())
                break;
            
            if (forward) {
                if(sEntry == null)
                    sEntry = Ngram.zeroGram;
                w.connectForward(con, state, sEntry, eEntry, min, max, chunk, "", isStart, isEnd);
            } else {
                if(eEntry == null)
                    eEntry = Ngram.zeroGram;
                w.connectBackward(con, state, sEntry, eEntry, min, max, chunk, "", isStart, isEnd);
            }

            if (connectors.size() >= howmany) {
                break;
            }
        }

        // System.out.printf("max len %d\n", con.maxlenReachedCount);
        
        if (from == null) {
            from = "";
        }
        if (to == null) {
            to = "";
        }
    
        Collection<String> result = new ArrayList<String>();
        for (String conn : connectors) {
            String name = from + conn + to;
            result.add(name);
        }

        return result;
    }
}
