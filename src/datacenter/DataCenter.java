/**
 * Copyright (c) 2011 The Regents of The University of Michigan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met: redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer;
 * redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution;
 * neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author David Meisner (meisner@umich.edu)
 *
 */
package datacenter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;
import core.Experiment;
import stat.TimeWeightedStatistic;
import core.Constants.TimeWeightedStatName;
import core.Constants;

/**
 * This class will hold all the physical objects in the datacenter for now.
 *
 * @author David Meisner (meisner@umich.edu)
 */
public final class DataCenter implements Serializable {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The servers in the datacenter.
     */
    private Vector<Server> servers;

    /**
     * The experiment the server is running in.
     */
    protected Experiment experiment;

    /**
     * The scheduling algorithm for assigning jobs to servers.
     */
    public static enum ClusterScheduler {

        UNIFORM,

        PACK,

        PEAK

    };


    /**
     * The scheduling algorithm currently used.
     */
    private ClusterScheduler scheduler;

    /**
     * Sets load balancing scheme
     * @param sched  - the load balancing schduling scheme
     */
    public void setClusterScheduler(final ClusterScheduler sched) {
        this.scheduler = sched;
    }

    public ClusterScheduler getClusterScheduler() {
        return this.scheduler;
    }


    /**
     * Creates a new datacenter.
     */
    public DataCenter(final Experiment anExperiment) {
	this.experiment = anExperiment;
        this.servers = new Vector<Server>();
    }

    /**
     * Adds a server to the datacenter.
     *
     * @param server - the server to add
     */
    public void addServer(final Server server) {
        this.servers.add(server);
    }

    /**
     * Gets the servers in the datacenter.
     *
     * @return the server in the datacenter
     */
    public Vector<Server> getServers() {
        return this.servers;
    }

    /**
     * Updates the statistics of all the objects in the datacenter.
     *
     * @param time
     *            - the time the statistics are updated
     */
    public void updateStatistics(final double time) {
	double clusterPower = 0.0;

	// Update each server's statistics (utilization, power, idleness
        Iterator<Server> iter = this.servers.iterator();
	Server server;
        while (iter.hasNext()) {
	    server = iter.next();
	    //server.updateStatistics(time);
            //iter.next().updateStatistics(time);
	    clusterPower += server.getPower();
        }

	// Update datacenter level statistics (cluster power)	
        TimeWeightedStatistic clusterPowerStat
            = this.experiment.getStats().getTimeWeightedStat(
                    Constants.TimeWeightedStatName.CLUSTER_POWER);
        clusterPowerStat.addSample(clusterPower, time);
    }

   public Server getPackingTargetServer(final Server originalServer){



	// First sort servers based on efficiency and utilization
	// Ascending order. Lowest util to highest util
	Collections.sort(this.servers,new ServerCompareUtilOnly());
	// Reverse order so highest util and efficiency is first
	//Collections.reverse(this.servers);
/*
	for(int i = 0; i <  this.servers.size(); i++){
		System.out.print(String.valueOf(servers.get(i).getInstantUtilization()) + "(" + String.valueOf(servers.get(i).getRemainingCapacity()) + ") ");
	}
	System.out.print("\n");
*/


   	Iterator<Server> iter = this.servers.iterator();
	Server server;
	Server lastActiveServer = originalServer;

	// Ensure at least a single standy-by server is on
	// if no idle server exists, find first paused server and issue
	if(numServersIdle() == 0){
		while(iter.hasNext()){
			server = iter.next();
			if( server.isPaused() ){
			    if ( (server.getJobsInService() > 0) || (server.getQueueLength() > 0) ) {
				// A server is sleeping and have job. already waking up
				break;
			    }
			    else {
				// A server is sleeping and have no jobs. 
				// Send request here to wake up server
			 	return server;
 			    }
			}
		}
	}

	iter = this.servers.iterator();
        while (iter.hasNext()) {
            server = iter.next();
	    if( !server.isPaused() )
		lastActiveServer = server;
	    if( (server.getRemainingCapacity() > 0) && !server.isPaused())
		return server;
        }

	return lastActiveServer;

  }

   class ServerCompareUtilOnly implements Comparator<Server> {

	@Override
	public int compare(Server s1, Server s2) {
		return Double.compare(s2.getInstantUtilizationWithQueue(),s1.getInstantUtilizationWithQueue());
	}

   }
   class ServerCompare implements Comparator<Server> {

	@Override
	public int compare(Server s1, Server s2) {
		
	    // First compare peak efficiency
	    // Then compare utilization
	    if ( Double.compare(s1.getPeakEfficiency(),s2.getPeakEfficiency()) == 0){
		return Double.compare(s2.getInstantUtilizationWithQueue(),s1.getInstantUtilizationWithQueue());
	    }
	    else {
		return Double.compare(s2.getPeakEfficiency(),s1.getPeakEfficiency()); 	   
	    }
	}

   }

   public Server getPeakTargetServer(final Server originalServer){

	// First sort servers based on efficiency and utilization
	// Ascending order. Lowest util and efficiency to highest util and efficiency
	Collections.sort(this.servers,new ServerCompare());
	// Reverse order so highest util and efficiency is first
	//Collections.reverse(this.servers);

	Server server;
	Server lowestAbovePeakServer = originalServer;
	Server lastActiveServer = originalServer;
   	Iterator<Server> iter = this.servers.iterator();

	// Ensure at least a single standy-by server is on
	// if no idle server exists, find first paused server and issue
	if(numServersIdle() == 0){
		while(iter.hasNext()){
			server = iter.next();
			if( server.isPaused() ){
			    if ( (server.getJobsInService() > 0) || (server.getQueueLength() > 0) ) {
				// A server is sleeping and have job. already waking up
				break;
			    }
			    else {
				// A server is sleeping and have no jobs. 
				// Send request here to wake up server
			 	return server;
 			    }
			}
		}
	}

	lowestAbovePeakServer = null;	
	iter = this.servers.iterator();
        while (iter.hasNext()) {
            server = iter.next();

	    if(!server.isPaused())
		lastActiveServer = server;

	    if(server.isAbovePeakEfficiencyUtilization() ){
		if(server.getRemainingCapacity() > 0)
			lowestAbovePeakServer = server; // may not be set if all servers are low/mid EP
		continue; // Skip issue to above peak. We know there exist some below peak.
	    }
	    // At this point, server is below peak, and has highest util and efficiency. Issue here.
	    //if (server.getRemainingCapacity() > 0) // Sanity check: make sure it has free slots
	    if(!server.isPaused() && server.getRemainingCapacity() > 0)
	    	return server; 
        }

	// No idle servers under peak left and a idle server is waking up
	// All servers are running 
	// 
	// No servers less than peak efficiency is available.  
	// A server is already waking up
	//
	// 1. Wakeup a server (isPaused() && queue=0
	// 2. Issue to lowest above peak server

	// Issue to lowest above peak server
	if(lowestAbovePeakServer != null) {
		return lowestAbovePeakServer;
	}else{
		return lastActiveServer;
	}
	
	//return originalServer;	
   }

   public boolean allServersAbovePeak(){

	Iterator<Server> iter = this.servers.iterator();
	Server server;
	while (iter.hasNext()) {
		server = iter.next();
		if ( !server.isAbovePeakEfficiencyUtilization() )
			return false;
	}	
	return true;
   }

   public int numServersIdle(){
	int idleServers = 0;
	Iterator<Server> iter = this.servers.iterator();
	Server server;
	while (iter.hasNext()) {
		server = iter.next();
		if ( (server.getJobsInService() == 0) && (server.getQueueLength() == 0) && !server.isPaused() ) {
			// Server is idle if no jobs in system and running
			idleServers++;
		}
	}

	return idleServers;
   }

}
