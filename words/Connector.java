/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.words;

import java.util.Collection;

/**
 *
 * @author Gurevich
 */
public class Connector {
    static final int MAXLENMAX = 60000;
    Collection<String> connectors;
    int maxlenReachedCount;
    Connector(Collection<String> c){
        connectors = c;
        maxlenReachedCount = 0;
    }
    
    void add() {
        ++maxlenReachedCount;
    }
    
    boolean limit() {
        return maxlenReachedCount >= MAXLENMAX;
    }
}
