package org.superbiz.cluster;

import java.util.logging.Level;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.logging.ILogger;

/**
 * Cluster implementation using HazelCast API
 * 
 * Only Active passive capability is provided.
 * System property for cluster name "cluster.name" should be specified.
 * System property for HazelCast configuration "cluster.config.file" should also 
 * be specified.
 * 
 * @author Murali/Ezhil
 */

public class ActivePassiveClusterImpl
    implements ActivePassiveCluster
{
    public static final String DATA = "DATA";
    private HazelcastInstance hcinstance;
    private static ActivePassiveClusterImpl thisinstance;
    public ILogger logger;
    protected boolean isActive = false;
 
    private ActivePassiveClusterImpl(){
    }

    /**
     * This method returns instance of the HazelCastCluster
     * 
     * @return com.trimble.mrm.cluster.Cluster
     * @throws ClusterException
     */
    public static synchronized ActivePassiveCluster getInstance() throws ClusterException
    {
        if(thisinstance == null)
        {
            thisinstance = new ActivePassiveClusterImpl();
            thisinstance.init();
        }
        return thisinstance;
    }
 
    /**
     * This method initialises HazelCast cluster
     * 
     * @throws ClusterException
     */
    private void init() throws ClusterException
    {
        try {
        	String configFilePath = getConfigFilePath();
            Config config = new ClasspathXmlConfig(configFilePath);
            setClusterName(config, configFilePath);

            System.out.println("Starting hazelcast...");
            hcinstance = Hazelcast.newHazelcastInstance(config);
            System.out.println("Started hazelcast...");
            System.out.println("Instance is " + (hcinstance == null ? "null" : "not null"));
            logger = this.getLogger();
        }
        catch(ClusterException pClusterException) {
        	throw pClusterException;
        }
        catch(Exception pException) {
        	throw new ClusterException("Error while reading HazelCast cluster configuration", pException);
        }
    }

    /**
     * This method registers client application listener to the HazelCast runtime, 
     * which will use this listener object to inform cluster membership events to client application
     * 
     */
    public void setSingletonListener(Singleton singleton) {
        if(isActive())
        {
        	singleton.activated();
        	isActive = true;
        	logger.log(Level.INFO, String.format("Member %s has become active member in the cluster",
        											this.getAddressStr(getActiveMember())));
        }
        new HazelCastMembershipListener(thisinstance, singleton);
 	}
  
    /**
     * This method returns true if current member is active member in cluster
     * 
     * @return boolean true|false
     */
    public boolean isActive()
    {
    	Member activeMember = getActiveMember();
    	return activeMember.localMember();
    }
    
    /**
     * This method returns the active member address
     * 
     * @return com.hazelcast.core.Member
     */
    public Member getActiveMember() {
    	return (Member)hcinstance.getCluster().getMembers().iterator().next();
    }
    
    /**
     * This method returns the active member address in string format
     * 
     * @return java.lang.String
     */
    public String getAddressStr(Member member) {
    	StringBuffer addressBuf = new StringBuffer();
    	addressBuf.append(member.getInetSocketAddress().getAddress().getHostAddress());
    	addressBuf.append("[");
    	addressBuf.append(member.getInetSocketAddress().getHostName());
    	addressBuf.append("]");
    	return addressBuf.toString();
    	
    }
    
    /**
     * This method returns the cluster instance
     * 
     * @return com.hazelcast.core.Cluster
     */
    public com.hazelcast.core.Cluster getCluster() {
    	return hcinstance.getCluster();
    }
    
    /**
     * This method returns the HazelCast logger instance
     * 
     * @return com.hazelcast.core.ILogger
     */
    public ILogger getLogger() {
    	return hcinstance.getLoggingService().getLogger(ActivePassiveClusterImpl.class.getName());
    }
    
    /**
     * This method returns the HazelCast configuration file path
     * 
     * @return java.lang.String
     * @throws ClusterException
     */
    private String getConfigFilePath() throws ClusterException {
    	String configFilePath = System.getProperty("cluster.config.file","").trim();
    	if(configFilePath.isEmpty()) {
    		throw new ClusterException("Unable to find Configuration File, " +
    				"provide configuration file details by setting system property cluster.config.file");
    	}
    	return configFilePath;
    }
    
    /**
     * This method sets the configured cluster name
     * 
     * @param config
     * @param configFile
     * @throws ClusterException
     */
    private void setClusterName(Config config, String configFile) throws ClusterException {
    	if(config != null) {
    		String clusterName = System.getProperty("cluster.name","").trim();
        	//If cluster name is not provided as system property, verify whether its provided in configuration file or not
        	if(clusterName.isEmpty()) {
        		config.getGroupConfig();
    			if(config.getGroupConfig().getName().trim().equalsIgnoreCase(GroupConfig.DEFAULT_GROUP_NAME)) {
        			throw new ClusterException("Cluster name is mandatory, " +
            				"provide cluster name by setting system property cluster.name" + 
            				" or specify it in Hazelcast configuration file");
        		}
    	    } else {
    	    	//set cluster name and password
    	    	config.getGroupConfig().setName(clusterName);
                config.getGroupConfig().setPassword(clusterName);
        	}
    	} else {
    		throw new ClusterException(String.format("Unable to initialize HazelCast from the given configuration file - %s", configFile));
    	}
    }

    @Override
    public void set(String key, String value) {
        hcinstance.getMap(DATA).put(key, value);
    }

    @Override
    public String get(String key) {
        Object value = hcinstance.getMap(DATA).get(key);
        if (value == null) {
            return null;
        }

        return value.toString();
    }
}

