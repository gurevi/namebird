
package com.shobia.server;

import java.nio.ByteBuffer;

/**
 *
 * @author gurmi04
 */
abstract public class ServerProc {
    RunServer runner;
    // manipulated directly by Server
    public ByteBuffer          currentBuffer;
   
    public abstract int        readyOps();
    public abstract void       add(ByteBuffer bb);
    public abstract ByteBuffer get();
    //public abstract boolean    done(ByteBuffer bb);
}



