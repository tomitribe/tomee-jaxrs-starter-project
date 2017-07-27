package org.superbiz.cluster;

/**
 * Exception thrown when an error occurs during communication with
 * a cluster.
 */
@SuppressWarnings("serial")
public class ClusterException
    extends Exception
{
	/**
     * Create a new ClusterException
     * 
     * @param errorMessage
     * @param t
     */
    public ClusterException(String errorMessage, Throwable t) {
        super(errorMessage, t);
    }
    
    /**
     * Create a new ClusterException
     * 
     * @param errorMessage
     */
    public ClusterException(String errorMessage) {
        super(errorMessage);
    }
}
