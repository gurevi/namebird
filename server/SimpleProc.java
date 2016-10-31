/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shobia.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;

/**
 *
 * @author Gurevich
 */
public abstract class SimpleProc extends ServerProc {
    static Charset charset = Charset.forName("UTF-8");
    
    public abstract String process(String input);
    
    private char  terminator;
    private int   readyOps = SelectionKey.OP_READ;
    private String output;
    boolean done = false;
    
    private StringBuilder input = new StringBuilder();
    
    public SimpleProc(char t){
         terminator = t;
    }
    
    public int        readyOps(){
        return readyOps;
    }
    
    public void add(ByteBuffer bb) {
        CharBuffer cbuf = charset.decode(bb);
        while (cbuf.hasRemaining()) {
            char ch = cbuf.get();
            if (ch == terminator) {
                done = true;
                break;
            }
            input.append(ch);
        }

        if (done) {
            String msg = input.toString();
            if(msg.charAt(0) == '$'){
                msg = msg.substring(1);
                output = msg;
                if(msg.equals("STATUS")) {
                    output = "running";
                } else if(msg.equals("STOP")) {
                    runner.status = RunServer.ExitStatus.DONE;
                    runner.server.stop();
                } else if(msg.equals("RESTART")){
                    runner.status = RunServer.ExitStatus.RESTART;
                    runner.server.stop();
                } else {
                    output +=  "  unknown command";
                }
            } else {  
                 output = process(msg);
            }
            readyOps = SelectionKey.OP_WRITE;
        }
    }
    
    public ByteBuffer get(){
        if(output == null)
            return null;
        
        ByteBuffer bb = charset.encode(output);
        output = null;
        return bb;
    }
}
