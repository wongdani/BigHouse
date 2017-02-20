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

import core.AbstractEvent;
import core.Experiment;
import datacenter.PowerNapServer.PowerNapState;
/**
 * Represents a PowerNap server transitioning from Active to Nap.
 *  
 * @author David Meisner (meisner@umich.edu)
 */
public final class NapTransitionEvent extends AbstractEvent {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The server that is transitioning.
     */
    private PowerNapServer server;

    /**
     * Creates a new NapTransitionEvent.
     *
     * @param time - the time the server transitioned
     * @param experiment - the experiment the event takes place in
     * @param aServer - the server that transitioned
     */
    public NapTransitionEvent(final double time,
                                          final Experiment experiment,
                                          final PowerNapServer aServer) {
        super(time, experiment);
        this.server = aServer;
    }


    /**
     * Sets the server to nap.
     */
    @Override
    public void process() {
    
        // Make sure this transition is valid
        if (this.server.isNapping()) {
            //Sim.fatalError("Trying to transition to nap when napping");
	    this.server.transitionNapEvent=null;
            return;
        }
        // Make sure this transition is valid
        if (this.server.isPaused()) {
            //Sim.fatalError("Trying to transition to nap when paused");
	    this.server.transitionNapEvent=null;
            return;
        }
	// Make sure no jobs in system and at least one server idling
 	if( (this.server.getJobsInService() != 0) || (this.server.getQueueLength() != 0) || 
	    ((this.getExperiment().getDataCenter().numServersIdle() <= 1) && (this.server.napTransitionTime >= 1) )) {
		// Job in system. do not nap
		//System.out.println("Server not busy" + String.valueOf(this.time));
		this.server.transitionNapEvent=null;
		return;
	}	
        this.server.powerNapState = PowerNapState.TRANSITIONING_TO_NAP;
        this.server.transitioningToNap = true;
        double napTime = this.time + this.server.napTransitionTime;
        PowerNapTransitionedToNapEvent napEvent
            = new PowerNapTransitionedToNapEvent(napTime,
                                                 this.getExperiment(),
                                                 this.server);
	//System.out.println("System napping " + String.valueOf(this.time) + " " + String.valueOf(napTime));
        this.server.transitionEvent = napEvent;
        this.getExperiment().addEvent(napEvent);
        this.server.pauseProcessing(time);
	this.server.transitionNapEvent=null;
    }

}
