package org.superbiz.cluster;

import java.util.logging.Level;

import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.logging.ILogger;

/**
 * Membership listener which invokes activated and deactivated methods in the
 * client application which is implementing the singleton interface
 * 
 * 
 * @author Murali/Ezhil
 */

public class HazelCastMembershipListener
    implements MembershipListener
{
	Singleton instance;
    ActivePassiveClusterImpl hcinstance;
    ILogger logger;
  
    HazelCastMembershipListener(ActivePassiveClusterImpl hcinstance, Singleton instance)
    {
        this.instance = instance;
        this.hcinstance = hcinstance;
        this.hcinstance.getCluster().addMembershipListener(this);
        this.logger = this.hcinstance.getLogger();
    }

    /*
     * This method is called whenever the new member joins the cluster.
     * During the split brain scenario (with more than one master), one of the master 
     * would be deactivated.
     * 
     * (non-Javadoc)
     * @see com.hazelcast.core.MembershipListener#memberAdded(com.hazelcast.core.MembershipEvent)
     */
    public void memberAdded(MembershipEvent membershipEvent)
    {
    	synchronized (this)
    	{
    		int clusterSize = membershipEvent.getCluster().getMembers().size();
    		Member member = membershipEvent.getMember();
	    	logger.log(Level.INFO, String.format("Member %s has joined the cluster", hcinstance.getAddressStr(member)));
    		
	    	/*   
	    	 * Split-Brain Scenario - Multiple Master(Active) Nodes
	    	 * 
	    	 * Imagine that you have 3-node cluster {A,B,C} and for some reason the network is divided into two in a way that 2 servers cannot see the other 1.
	    	 * As a result you ended up having two separate clusters; 2-node cluster {A,B} and 1-node cluster {C}. Members in each sub-cluster are thinking that 
	    	 * the other nodes are dead even though they are not. 
	    	 * 
	    	 * Once the network restores HazelCast will fix this issue by merging these two clusters 
	    	 * 
	    	 * The below steps will be executed on the node {C}, which is going to be merged with the cluster {A,B}
	    	 * 		1. Node {C}, which is going to be merged with cluster will join first to the cluster 
	    	 * 		2. Master node {A} will be joined as second member to the cluster
	    	 * 		3. Other node {B} will be joined to the cluster
	    	 * 		4. memberAdded method will be called on nodes {A} and {B} to inform node {C} joined the cluster
	    	 * 
	    	 * HazelCast stores the nodes in the order they have joined. 
	    	 *  i.e membershipEvent.getCluster().getMembers().iterator().next() will always give oldest node in the cluster.
	    	 * 
	    	 * com.trimble.mrm.cluster.hazelcastimpl.HazelCastCluster use the same approach to decide whether node is active or not.
	    	 * 
	    	 * The below functionality will mark all active nodes , which are not active anymore as passive nodes
	    	 * The steps from 1-3 will be executed by multiple threads parallel by calling memberAdded method on node {C}, so we need minimum two nodes to decide
	    	 * whether current node is master(active) node or not. 
	    	 * 
	    	 */
	    	if(clusterSize > 1) {
		    	Member activeMember = (Member) membershipEvent.getCluster().getMembers().iterator().next();
				//Deactivate the active node which is not active anymore
		    	if(!activeMember.localMember() && hcinstance.isActive)
		        {
					instance.deactivated();
		            hcinstance.isActive = false;
		            String activeMemberAddress =  hcinstance.getAddressStr(activeMember);
		            String localMemberAddress =   hcinstance.getAddressStr(membershipEvent.getCluster().getLocalMember());
					logger.log(Level.INFO, String.format("Active member in cluster is %s ",  activeMemberAddress)); 
				    logger.log(Level.INFO, String.format("Current member %s has been marked as passive member",	localMemberAddress)); 
		        }
	    	}
    	}
    }

    /*
     * This method is called whenever any member leaves the cluster. If the master is down
     * then a master is elected and activated. 
     * 
     * (non-Javadoc)
     * @see com.hazelcast.core.MembershipListener#memberRemoved(com.hazelcast.core.MembershipEvent)
     */
    public void memberRemoved(MembershipEvent membershipEvent)
    {
       synchronized (this) {
    	   Member activeMember = membershipEvent.getCluster().getMembers().iterator().next();
    	   Member member = membershipEvent.getMember();
	       logger.log(Level.INFO, String.format("Member %s has left the cluster", hcinstance.getAddressStr(member)));
           if(activeMember.localMember() && !hcinstance.isActive)
	       {
	        	instance.activated();
	        	hcinstance.isActive = true;
	        	String activeMemberAddress = hcinstance.getAddressStr(membershipEvent.getCluster().getLocalMember());
	        	logger.log(Level.INFO, String.format("Current member %s has been marked as active member", activeMemberAddress));
	       }
       }
     }

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

	}
}

