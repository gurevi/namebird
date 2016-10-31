/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.server;

/**
 *
 * @author Gurevich
 */

class WordFactory implements ServerProcFactory {
    
    WordGeneratorInterface wgi;
    public WordFactory() {
        wgi = new WordGeneratorInterface();
    }
    
    @Override
    public ServerProc create() {
        return new SimpleProc('\n'){
            @Override
            public String process(String input) {
                return wgi.get(input);
            }        
        };
    } 
}
