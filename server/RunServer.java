/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shobia.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Gurevich
 */
public class RunServer implements Runnable {
    enum ExitStatus { NONE, RUNNING, RESTART, DONE};
    
    Server     server;
    Exception  exeption;
    ExitStatus status = ExitStatus.NONE; 
    
    @Override
    public void run() {
        server = new Server(this, new WordFactory());
        try {
            server.startServer();
            mylogger.info("started");
        } catch (Exception ex) {
            mylogger.log( Level.SEVERE, ex.getMessage(), ex );
        }
        mylogger.info("done");
    }

    static Logger mylogger = Logger.getLogger("server");

    // -Djava.util.logging.SimpleFormatter.format=
    // '%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n'
    public static void main(String... args) throws IOException {
        boolean isDaemon = args.length > 0;
        Handler handler;
        RunServer runner;
        
        if (isDaemon) {
            System.in.close();
            System.out.close();
            System.err.close();

            String logfile = args[0];
            FileHandler fh = new FileHandler(logfile);
            fh.setFormatter(new SimpleFormatter());
            handler = fh;
        } else {
            handler = new ConsoleHandler();
        }

        mylogger.setUseParentHandlers(false);
        mylogger.addHandler(handler);
        mylogger.setLevel(Level.ALL);
        
        runner = new RunServer();
        
        Thread serverThread = new Thread(runner);
        serverThread.setDaemon(true);
        serverThread.start();
        
        if (isDaemon) {
            mylogger.info("server started");
            try {
                serverThread.join();
            } catch (InterruptedException ex) {
                mylogger.info("server interrupted");
            }
        } else {
            mylogger.info("Started server. Enter any key to stop");
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            String line = r.readLine();
                
            //runner.server.interrupt();
            //serverThread.interrupt();
            ///mylogger.info("Stopped server");
        }
        
        // thread.interrupt();
        mylogger.log(Level.INFO, runner.status.name());
    }
}
