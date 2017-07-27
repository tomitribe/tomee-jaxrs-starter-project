package org.superbiz.cluster;


/**
 * ActivePassiveCluster interface which will be implemented by Active/Passive clustering implementation
 * 
 * 
 * @author Ezhil/Murali
 */

public interface ActivePassiveCluster extends DataStore
{
    void setSingletonListener(Singleton singleton);

    boolean isActive();
}
