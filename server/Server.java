
package com.shobia.server;

import static com.shobia.server.RunServer.mylogger;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

public class Server {

    public static final int mywellknownport = 4334;
    private Selector            selector;
    private  ServerProcFactory  factory;
    private RunServer           runner;
    public Server(RunServer run, ServerProcFactory fac) {
        runner  = run;
        factory = fac;
    } 
    /**
     * Accept a new client and set it up for reading.
     */
    private void doAccept(SelectionKey sk)
    {
        ServerSocketChannel server = (ServerSocketChannel)sk.channel();
        SocketChannel clientChannel;
        try {
            clientChannel = server.accept();
            clientChannel.configureBlocking(false);
            
            // Register this channel for reading.
            SelectionKey clientKey = 
                clientChannel.register(selector, SelectionKey.OP_READ);
             
            // Allocate an Data instance and attach it to this selection key.
            ServerProc data = factory.create();
            data.runner = runner;
            clientKey.attach(data);
        }
        catch (IOException ex) {
            mylogger.log(Level.SEVERE, ex.getMessage(), ex);
        }     
    }

    /**
     * Read from a client. Enqueue the data on the clients output
     * queue and set the selector to notify on OP_WRITE.
     */
    private void doRead(SelectionKey sk)
    {
        SocketChannel channel = (SocketChannel)sk.channel();
        ByteBuffer bb = ByteBuffer.allocate(8192);
        int len;

        try {
            len = channel.read(bb);
            if (len < 0) {
                disconnect(sk);
                return;
            }
        }
        catch (IOException ex) {
            mylogger.log(Level.WARNING, ex.getMessage(), ex);
            return;
        }

        // Flip the buffer.
        bb.flip();
        
        ServerProc data = (ServerProc)sk.attachment();
        data.add(bb);

        // We've enqueued data to be written to the client, we must
        // not set interest in OP_WRITE.
        int opts = data.readyOps();
        sk.interestOps(opts);
    }

    /**
     * Called when a SelectionKey is ready for writing.
     */
    private void doWrite(SelectionKey sk)
    {
        SocketChannel channel = (SocketChannel)sk.channel();
        ServerProc data = (ServerProc)sk.attachment();
        
        ByteBuffer bb = data.currentBuffer;
        if(bb == null) {
             bb = data.currentBuffer = data.get();
        }
        if(bb==null){
            disconnect(sk);
            return;  
        }
        try {
            int len = channel.write(bb);
            if (len == -1) {
                disconnect(sk);
                return;
            }
            
            if (bb.remaining() == 0) {
                // The buffer was completely written, remove it.
                data.currentBuffer = null;
            }
        }
        catch (Exception e) {
            mylogger.log(Level.WARNING, "Failed to write to client.");
            e.printStackTrace();
        }

        // If there is no more data to be written, remove interest in
        // OP_WRITE.
        int opts = data.readyOps();
        sk.interestOps(opts);
    }

    public void disconnect(SelectionKey sk)
    {
        SocketChannel channel = (SocketChannel)sk.channel();

        try {
            channel.close();
        }
        catch (Exception e) {
            mylogger.log(Level.WARNING, "Failed to close client socket channel.");
            e.printStackTrace();
        }
    }
    
    private volatile boolean cont = true;
    private volatile boolean isInterrupted = false;
    
    public void stop() {
        cont = false;
    }
    public void interrupt(){
        isInterrupted = true;
    }
                
    public void startServer() throws Exception
    {
        selector = SelectorProvider.provider().openSelector();

        // Create non-blocking server socket.
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // Bind the server socket to localhost.
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(),
                                                      mywellknownport);
        ssc.socket().bind(isa);

        // Register the socket for select events.
        SelectionKey acceptKey =  ssc.register(selector, SelectionKey.OP_ACCEPT);

        // Loop forever.
        while(cont) {
            selector.select();          
                
            Set readyKeys = selector.selectedKeys();
            Iterator keyIt = readyKeys.iterator();

            while (keyIt.hasNext()) {
                SelectionKey sk = (SelectionKey)keyIt.next();
                keyIt.remove();

                if (sk.isAcceptable()) {
                    doAccept(sk);
                }
                if (sk.isValid() && sk.isReadable()) {
                    doRead(sk);
                }
                if (sk.isValid() && sk.isWritable()) {
                    doWrite(sk);
                    if(!cont)
                        break;
                }
            }
        }
        mylogger.info("Exiting Word Server");
        selector.close();
        ssc.close();
    }
}
