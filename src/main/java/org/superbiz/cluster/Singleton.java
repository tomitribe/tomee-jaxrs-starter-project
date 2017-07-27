package org.superbiz.cluster;


/**
 * Singleton (active passive clustering) interface provides callback methods
 * which will be called by underlying clustering implementation. 
 * 
 * 
 * @author Ezhil/Murali
 */

public interface Singleton
{

    public abstract void activated();

    public abstract void deactivated();
}
