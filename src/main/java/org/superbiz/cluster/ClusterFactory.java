package org.superbiz.cluster;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class ClusterFactory
{

    public static ActivePassiveCluster getClusterInstance(ClusterType type) throws ClusterException
    {
    	ActivePassiveCluster provider = null;
    	try{
    		if(type == ClusterType.HAZELCAST)
            {
                Class<?> clazz = Class.forName("org.superbiz.cluster.ActivePassiveClusterImpl");
                Method getInstanceMethod = clazz.getMethod("getInstance", new Class[0]);
                provider = (ActivePassiveCluster)getInstanceMethod.invoke(null, new Object[0]);
            } else if(type == ClusterType.JGROUPS) {
                Class<?> clazz = Class.forName("com.trimble.mrm.cluster.jgroupsimpl.ActivePassiveClusterImpl");
                Method getInstanceMethod = clazz.getMethod("getInstance", new Class[0]);
                provider = (ActivePassiveCluster)getInstanceMethod.invoke(null, new Object[0]);
            }
        }
    	catch(InvocationTargetException pException) {
        	throw new ClusterException("Exception while getting cluster instance in ClusterFactory:getClusterInstance() method", pException);
        }
        catch(Exception pException) {
        	throw new ClusterException("Exception while getting cluster instance in ClusterFactory:getClusterInstance() method", pException);
        }
        return provider;
    }
    
    public static ActivePassiveCluster getClusterInstance(Properties properties) throws ClusterException
    {
    	ActivePassiveCluster provider = null;
    	try{
    		PropertyUtil.loadSystemProperties(properties);
    		String clusterImplType = System.getProperty("cluster.impl.type", "HAZELCAST").trim().toUpperCase();
            provider = getClusterInstance(ClusterType.valueOf(clusterImplType));
        }
    	catch(ClusterException pClusterException) {
        	throw pClusterException;
        }
        catch(Exception pException) {
        	throw new ClusterException("Exception while getting cluster instance in ClusterFactory:getClusterInstance() method", pException);
        }
        return provider;
    }
}