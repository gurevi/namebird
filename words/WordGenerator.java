package com.shobia.words;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Util;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WordGenerator {

    final static byte code_A = 65;
    final static byte code_Z = 90;
    final static byte code_a = 97;
    final static byte code_z = 122;

    final static int BASEMAX = 5; // longest
    final static int BASEMIN = 1; //shortest
    public static int USEBASE = 3;

    final static char TERMINATOR = 32;

    Map<String, Ngram> ngramMap = new HashMap<String, Ngram>();
    public ArrayList<Ngram> entryList = new ArrayList<Ngram>();

    public Set<String> dictionary = new HashSet<String>();
    //public Set<String> dictionarysmall = new HashSet<String>();
    public Set<Ngram> startgramsr = new HashSet<Ngram>();
    public ArrayList<Ngram> startgrams = new ArrayList<Ngram>();

    public int maxLength = 10;
    public int minLength = 6;

    boolean processTerminator = false;

    static final char[] charTable = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    static final byte nal = 31;

    static byte toByte(char val) {
        byte b;

        if (val >= code_a && val <= code_z) {
            b = (byte) (val - code_a);
        } else if (val >= code_A && val <= code_Z) {
            b = (byte) (val - code_A);
        } else {
            // not a letter
            b = nal;
        }
        return b;
    }

    public int setMaxLength(int max) {
        if (max > 4) {
            maxLength = max;
        }
        return maxLength;
    }

    BufferedReader getResource(String filename) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/" + filename);
        if (is == null) {
            throw new FileNotFoundException(filename);
        }
        Reader r = new InputStreamReader(is);
        return new BufferedReader(r);
    }

    public void loadDictionary(String filename) throws IOException {

        BufferedReader reader = getResource(filename);
        StreamTokenizer st = new StreamTokenizer(reader);

        boolean eof = false;
        do {
            int token = st.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                    eof = true;
                    break;

                case StreamTokenizer.TT_WORD:
                    addDictionaryWord(st.sval);
                    break;

                default:
                    break;
            }
        } while (!eof);
    }

    static int toInt(String tok) {

        int factor = 100000;
        //all the numbers are lager than factor
        //we do not need that much precision,

        long num = Long.parseLong(tok) / factor;
        return (int) num;
    }

    public boolean loadGData(String resource) throws IOException {
        BufferedReader reader = getResource(resource);

        String header = reader.readLine();

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            String[] toks = line.split(",");
            int size = toks.length;
            if (size < 5) {
                continue;
            }

            // find ngram
            String tag = toks[0].toLowerCase();
            Ngram ngram = ngramMap.get(tag);
            if (ngram == null) {
                continue;
            }

            int len = tag.length();
            ngram.gtotal = toInt(toks[1]);

            switch (len) {

                case 2:
                    ngram.gfirst = toInt(toks[46]);
                    ngram.glast = toInt(toks[62]);
                    break;

                case 3:
                    ngram.gfirst = toInt(toks[37]);
                    ngram.glast = toInt(toks[52]);
                    break;

                case 4:
                    ngram.gfirst = toInt(toks[29]);
                    ngram.glast = toInt(toks[43]);
                    break;

                case 5:
                    ngram.gfirst = toInt(toks[22]);
                    ngram.glast = toInt(toks[35]);
                    break;
            }

        }

        return true;
    }

    Ngram findEntry(String base, boolean atStart, boolean atEnd) {
        Ngram entry = ngramMap.get(base);
        if (entry == null) {
            entry = new Ngram(base);
            ngramMap.put(base, entry);
        }

        entry.fullCount++;

        if (atStart) {
            entry.begCount++;
        }
        if (atEnd) {
            entry.endCount++;
        }

        return entry;
    }

    void addDictionaryWord(String word) {

        word = word.toLowerCase();

        int len = word.length();

        if (len == 3 || len == 4) {
            dictionary.add(word);
        }
        if (len < 5) {
            return;
        }

        if (!dictionary.add(word)) {
            // already processed
            return;
        }

        /*
         // avoid double letters
         for(int ii=1; ii<len; ++ii){
         if(word.charAt(ii-1) == word.charAt(ii))
         return;
         }
         */
        processDictionaryWord(word);
    }

    void processDictionaryWord(String word) {
        final int wordlen = word.length();
        for (int baselen = BASEMIN; baselen <= BASEMAX; ++baselen) {
            int extra = wordlen - baselen;
            for (int ii = 0; ii <= extra; ++ii) {
                String base = word.substring(ii, ii + baselen);

                boolean atStart = (ii == 0);
                boolean atEnd = (ii == extra);

                Ngram entry = findEntry(base, atStart, atEnd);
                if (baselen == USEBASE) {
                    entryList.add(entry);
                }

                if (!atStart) {
                    char pref = word.charAt(ii - 1);
                    entry.prefs.add(pref);
                }

                if (!atEnd) {
                    char suff = word.charAt(ii + baselen);
                    entry.suffs.add(suff);
                }

                if (wordlen > 5) {
                    int suffadjuster = 0;
                    if (baselen == 4) {
                        suffadjuster = 1;
                    }
                    if (ii >= wordlen - 4 - suffadjuster) {
                        if (!atEnd) {
                            char suffend = word.charAt(ii + baselen);
                            entry.suffsend.add(suffend);
                        } else {
                            entry.suffsend.addend();
                        }
                    }
                    if (ii < 2) {
                        if (!atStart) {
                            char prefend = word.charAt(ii - 1);
                            entry.prefsstart.add(prefend);

                        } else {
                            entry.prefsstart.addend();

                        }
                    }
                }

                if (atEnd) {
                    entry.suffs.addend();
                }
                if (atStart) {
                    entry.prefs.addend();
                }
            }
        }
    }

    Ngram findStartEntry(String root) {
        int len = root.length();

        int max = len;
        if (max > USEBASE) {
            max = USEBASE;
        }

        Ngram sEntry = null;

        for (int ii = max; ii >= BASEMIN; --ii) {
            String base = root.substring(0, ii);
            sEntry = ngramMap.get(base);
            if (sEntry != null) {
                break;
            }
        }
        return sEntry;
    }

    Ngram findEndEntry(String root) {
        int len = root.length();

        int max = len;
        if (max > USEBASE) {
            max = USEBASE;
        }

        Ngram eEntry = null;

        for (int ii = max; ii >= BASEMIN; --ii) {
            String base = root.substring(len - ii);
            eEntry = ngramMap.get(base);
            if (eEntry != null) {
                break;
            }
        }
        return eEntry;
    }

    static int indexOf(byte[] bytes, char ch) {
        if (bytes == null || bytes.length == 0) {
            return -1;
        }
        byte bt = toByte(ch);
        for (int ii = 0; ii < bytes.length; ++ii) {
            if (bytes[ii] == bt) {
                return ii;
            }
        }
        return -1;
    }

    int connectOneStepForward(Connector con, State state, char nextch, String base, Ngram eEntry, int newmin, int newmax, 
            int howmany, String link, boolean isStart, boolean isEnd) {
        if (state != null) {
            state = state.step(nextch);

            if (state == null) {
                return 0;
            }
        }

        link += nextch;
        int bsz = base.length();
        String newbase = null;
        if (bsz < USEBASE) {
            newbase = base + nextch;
        } else {
            newbase = base.substring(1) + nextch;
            isStart = false;
        }
        Ngram sEntry = ngramMap.get(newbase);
        if (sEntry == null) {
            return 0;
        }
        if(isStart) {
            if(! sEntry.goodStart())
                return 0;
        }
        
        return connectForward(con, state, sEntry, eEntry, newmin, newmax, howmany, link, isStart, isEnd);
    }

    int connectOneStepBackward(Connector con, State state, char prevchar, String base,
            Ngram sEntry, int newmin, int newmax,
            int howmany, String link, boolean isStart, boolean isEnd) {
        if (state != null) {
            state = state.step(prevchar);

            if (state == null) {
                return 0;
            }
        }

        link = prevchar + link;
        int bsz = base.length();
        String newbase = null;
        if (bsz < USEBASE) {
            newbase = prevchar + base;
        } else {
            newbase = prevchar + base.substring(0, USEBASE - 1);
            isEnd = false;
        }
        Ngram eEntry = ngramMap.get(newbase);
        if (eEntry == null) {
            return 0;
        }
        
        if(isEnd){
            if(!eEntry.goodEnd())
                return 0;
        }
        
        return connectBackward(con, state, sEntry, eEntry, newmin, newmax, howmany, link, isStart, isEnd);
    }

    boolean canJoin(Ngram sEntry, Ngram eEntry, boolean isStart, boolean isEnd) {
        char sch = sEntry.tag.charAt(sEntry.tag.length() - 1);
        boolean can =  (indexOf(sEntry.suffs.chars, eEntry.tag.charAt(0)) >= 0 && indexOf(eEntry.prefs.chars, sch) >= 0);
        if(!can)
            return false;
        
        if(isStart){
            int sLen = sEntry.tag.length();
            if(sLen >= USEBASE)
              return true;
            
            String base = sEntry.tag + eEntry.tag;
            sLen = base.length();
            if(sLen > USEBASE)
                base = base.substring(0, USEBASE);
            
            Ngram ng = this.ngramMap.get(base);
            can = ( ng != null && ng.goodStart());
        } else if(isEnd){
            int eLen = eEntry.tag.length();
            if(eLen >= USEBASE)
              return true;
            
            String base = sEntry.tag + eEntry.tag;
            eLen = base.length();
            if(eLen > USEBASE)
                base = base.substring(eLen - USEBASE, eLen);
            
            Ngram ng = this.ngramMap.get(base);
            can = ( ng != null && ng.goodEnd());
        }
 
        return can;
    }

    static int howmanymore(int howmany, int len, int min, int max) {
        int howmore = 3 * howmany / len;
        if (howmore < 2) {
            howmore = (min > 0) ? 1 : 2;
        }
        return howmore;
    }

    int connectForward(Connector con, State state, Ngram sEntry, Ngram eEntry, int min, int max,
            int howmany, String link, boolean isStart, boolean isEnd) {
        if(con.limit())
            return 0;
        
        if (sEntry.suffs == null) {
            return 0;
        }
        if (eEntry != null && eEntry.prefs == null) {
            return 0;
        }

        if (howmany == 0) {
            return 0;
        }

        int count = 0;
        if (min == 0) {
            if (state == null || state.isAccept()) {
                // try concat
                if (eEntry == null ? (sEntry.goodEnd()) : canJoin(sEntry, eEntry, isStart, isEnd)) {
                    con.connectors.add(link);
                    ++count;
                }
            }
        }

        if (max == 0) {
            con.add();
            return count;
        }

        if (count >= howmany) {
            return count;
        }

        // advance start
        int newmin = (min == 0) ? min : min - 1;
        int newmax = max - 1;

        String base = sEntry.tag;
        byte[] suffchars = sEntry.suffs.chars;
        if (suffchars == null || suffchars.length == 0) {
            return count;
        }

        int len = suffchars.length + 1;

        int howmore = howmanymore(howmany, len, newmin, newmax);

        for (int ii = 0; ii < suffchars.length; ++ii) {
            char nextch = sEntry.suffs.randomWeighedChar(2);
            if (nextch == TERMINATOR) {
                nextch = sEntry.suffs.randomWeighedChar(2);
            }
            if (nextch == TERMINATOR) {
                nextch = sEntry.suffs.randomWeighedChar(2);
            }
            if (nextch == TERMINATOR) {
                break;
            }
            int found = connectOneStepForward(con, state, nextch, base, eEntry, newmin, newmax, howmore, link, isStart, isEnd);
            if (found > 0) {
                count += found;
                if (count >= howmany) {
                    break;
                }
            }
        }
        return count;
    }

    int connectBackward(Connector con, State state, Ngram sEntry, Ngram eEntry, int min, int max,
            int howmany, String link, boolean isStart, boolean isEnd) {

        if(con.limit())
            return 0;
        
        if (eEntry.prefs == null) {
            return 0;
        }

        if (howmany == 0) {
            return 0;
        }

        int count = 0;
        if (min == 0) {
            if (state == null || state.isAccept()) {
                // try concat
                if (sEntry == null ? (eEntry.goodStart()) : canJoin(sEntry, eEntry, isStart, isEnd)) {
                    con.connectors.add(link);
                    ++count;
                }
            }
        }

        if (max == 0) {
            con.add();
            return count;
        }

        if (count >= howmany) {
            return count;
        }

        // advance start
        int newmin = (min == 0) ? min : min - 1;
        int newmax = max - 1;

        String base = eEntry.tag;
        byte[] prefchars = eEntry.prefs.chars;
        if (prefchars == null || prefchars.length == 0) {
            return count;
        }

        int len = prefchars.length + 1;
        int howmore = howmanymore(howmany, len, newmin, newmax);

        for (int ii = 0; ii < len; ++ii) {
            char prevchar = eEntry.prefs.randomWeighedChar(2);
            if (prevchar == TERMINATOR) {
                prevchar = eEntry.prefs.randomWeighedChar(2);
            }
            if (prevchar == TERMINATOR) {
                break;
            }
            int found = connectOneStepBackward(con, state, prevchar, base, sEntry, newmin, newmax, howmore, link, isStart, isEnd);
            if (found > 0) {
                count += found;
                if (count >= howmany) {
                    break;
                }
            }
        }
        return count;
    }

    static void appendBytes(int[] to, int[] from) {
        int size = to.length;
        for (int ii = 0; ii < size; ++ii) {
            to[ii] += from[ii];
        }
    }

    public void finish() {
        Collections.sort(entryList);

        Iterator<Map.Entry<String, Ngram>> it = ngramMap.entrySet().iterator();
        while (it.hasNext()) {
            Ngram ngram = it.next().getValue();
            boolean ok = ngram.normalize();
            //if(!ok)
            // it.remove();
            if (ngram.tag.length() == 3 && ngram.q3starter && ngram.begCount > 8 && ngram.fullCount > 158) {
                startgrams.add(ngram);
            }
        }
    }

    static Random rand = new Random(System.currentTimeMillis());

    static Ngram randomEntry(ArrayList<Ngram> arr) {
        if (arr == null) {
            return null;
        }

        int size = arr.size();
        if (size == 0) {
            return null;
        }
        //
        int ind = rand.nextInt(size);
        return arr.get(ind);
    }

    private String generateWord(String root, Ngram sEntry, Ngram eEntry, int minlen, int maxlen) {
        StringBuilder sofar = new StringBuilder(root);
        int curlen = sofar.length();

        int min = curlen + 2;
        if (min < minlen) {
            min = minlen;
        }

        int max = maxlen;

        if (max < min) {
            max = min;
        }

        USEBASE = 3;

        while (curlen < max) {
            int randcharcontrol = 1;
            if (eEntry == null) {
                randcharcontrol = 7;
            }
            if (eEntry != null && sEntry != null) {
                float percentage1 = ((float) eEntry.glast / eEntry.gtotal);
                float percentage2 = ((float) sEntry.gfirst / sEntry.gtotal);
                if (percentage2 * 100 > 63 && percentage2 > percentage1) {
                    if (root.equals(sEntry.tag) && curlen == root.length()) {
                        if (rand.nextBoolean()) {
                            sEntry = null;
                        }
                    } else {
                        sEntry = null;
                    }
                } else {
                    if (percentage1 * 100 > 65 && percentage1 > percentage1) {
                        eEntry = null;
                    }
                }
            }
            /*
             if(curlen == 9) { 
             USEBASE = 3;   
             } 
             if(curlen == 10) {
             USEBASE = 2;
             }
             if(curlen==11) {
             USEBASE = 4;
             } */
            /*
             if(maxlen > 11 && sEntry == null) {
             USEBASE = 2; 
             }
             else {
             USEBASE = 5;
             }
             */
            if (eEntry != null && ((sEntry == null) || rand.nextBoolean())) {
                // extend the tail     
                char ch;
                if (curlen < maxLength - 1 || maxLength < 8) {
                    ch = eEntry.suffs.randomWeighedChar(1);
                } else {
                    ch = eEntry.suffsend.randomWeighedChar(1);
                }
                if (ch == TERMINATOR) {
                    eEntry = null;
                } else {
                    sofar.append(ch);
                    ++curlen;

                    int size = eEntry.tag.length();
                    if (size < USEBASE) {
                        ++size;
                    }

                    String nextBase = sofar.substring(curlen - size);
                    eEntry = ngramMap.get(nextBase);
                }
            } else if (sEntry != null) {
                // extend the head
                char ch;

                if (curlen < maxLength - 1) {
                    ch = sEntry.prefs.randomWeighedChar(randcharcontrol);
                } else {
                    ch = sEntry.prefsstart.randomWeighedChar(7);
                }

                if (ch == TERMINATOR) {
                    sEntry = null;
                } else {
                    sofar.insert(0, ch);
                    ++curlen;

                    int size = sEntry.tag.length();
                    if (size < USEBASE) {
                        ++size;
                    }
                    String nextBase = sofar.substring(0, size);
                    sEntry = ngramMap.get(nextBase);
                }
            } else {
                break;
            }

            /*
             if(curlen >= min){
             if(sEntry!=null && sEntry.begCount > 0)
             sEntry = null;
                
             if(eEntry!=null && eEntry.endCount > 0)
             eEntry = null;
             }
             */
            if (sEntry == null && eEntry == null) {

                break;
            }
        }

        return sofar.toString();
    }

    public Set<String> generateWords(String root, Filter filter, int minlen, int maxlen, int howmany) {
        return generateWords(root, true, true, filter, minlen, maxlen, howmany);
    }

    public Set<String> generateWords(String root, boolean addsuff, boolean addpref,
            Filter filter, int minlen, int maxlen, int howmany) {
        if (howmany < 1) {
            howmany = 1;
        }

        if (root != null) {
            if (root.length() > 0) {
                root = root.toLowerCase();
            } else {
                root = null;
            }
        }

        boolean wasnullroot = false;
        if (root == null) {
            root = randomEntry(startgrams).tag;
            wasnullroot = true;
            // root = randomEntry(startgrams3);
        }

        int len = root.length() + 1;
        if (len < 4) {
            len = 4;
        }

        Set<String> words = new HashSet<String>();
        int count = 0;
        int limit = 5000;

        Ngram sEntry = null;
        if (addpref) {
            sEntry = findStartEntry(root);
        }

        Ngram eEntry = null;
        if (addsuff) {
            eEntry = findEndEntry(root);
        }

        for (int ii = 0; ii < limit; ++ii) {
            String word = generateWord(root, sEntry, eEntry, minlen, maxlen);
            if (wasnullroot) {
                root = randomEntry(startgrams).tag;
            }
            if (word == null) {
                continue;
            }

            if (dictionary.contains(word)) {
                continue;
            }

            int wlen = word.length();

            if (wlen < len) {
                continue;
            }

            boolean connectdots = false;
            if (filter != null && !filter.test(word, addsuff, addpref, connectdots, root)) {
                continue;
            }

            if (words.add(word)) {
                if (++count == howmany) {
                    break;
                }
            }
        }

        return words;
    }

    public Set<String> generateRandomWords(Filter filter, int size) {
        return generateWords(null, filter, minLength, maxLength, size);
    }
}
