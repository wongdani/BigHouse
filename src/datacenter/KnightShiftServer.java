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

import generator.Generator;
import core.Experiment;
import core.Job;
import core.Sim;

/**
 * A KnightShift server transitions to a low-power Knight state at low utilization.
 *
 * @author Daniel Wong (wongdani@usc.edu)
 */
public class KnightShiftServer extends Server {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    //TODO check this is in fact in seconds not milliseconds
    /**
     * The transition time in and out of knight (in seconds).
     */
    protected double knightTransitionTime;

    /**
     * The power of the server while in knight mode (in watts).
     */
    private double knightPower;

    /**
     * The capability of the server while in knight mode (ex. .15).
     */
    private double knightCapability;

    /**
     * The speed of the server while in knight mode (ex. .15).
     */
    private double knightSpeed;

    /**
     * The power state of the KnightShift server.
     */
    public enum KnightShiftState {
        /** The KnightShift server is active. */
        ACTIVE,

        /** The KnightShift server is transitioning to active. */
        TRANSITIONING_TO_ACTIVE,

        /**  The KnightShift server is transitioning to knight. */
        TRANSITIONING_TO_KNIGHT,

        /** The KnightShift server is in knight. */
        KNIGHT
    };

    /** The current power state of the KnightShift server. */
    protected KnightShiftState knightshiftState;

    /** The transition event if the server is transitioning. */
    private KnightShiftTransitionedToKnightEvent transitionEvent;

    /** Whether the KnightShift server is transitioning to active. */
    private boolean transitioningToActive;

    /** Whether the KnightShift server is transitioning to knight. */
    private boolean transitioningToKnight;


    /**
     * Creates a new KnightShiftServer.
     *
     * @param theNumberOfSockets - the number of sockets the server has
     * @param coresPerSocket - the number of cores per sockets
     * @param experiment - the experiment the server is part of
     * @param arrivalGenerator - The interarrival generator for the server
     * @param serviceGenerator - The service time generator for the server
     * @param theKnightTransitionTime - the transition time in
     * and out of the the knight mode (in seconds)
     * @param theKnightPower - the power of the server while in knight mode (in watts)
     * @param theKnightCapability - the capability of the server while in knight mode
     */
    public KnightShiftServer(final int theNumberOfSockets,
                          final int coresPerSocket,
                          final Experiment experiment,
                          final Generator arrivalGenerator,
                          final Generator serviceGenerator,
                          final double theKnightTransitionTime,
			  final double theKnightPower,
			  final double theKnightCapability,
		          final double theKnightSpeed) {
        super(theNumberOfSockets, coresPerSocket, experiment, arrivalGenerator,
                serviceGenerator);
	
        this.knightTransitionTime = theKnightTransitionTime;
        this.knightPower = theKnightPower;
	this.knightSpeed = theKnightSpeed;
        this.knightshiftState = KnightShiftState.ACTIVE;
        this.transitioningToActive = false;
        this.transitioningToKnight = false;
	this.knightCapability = theKnightCapability;
        this.pauseProcessing(0);
    }

    /**
     * Checks if the KnightShift server is knight.
     *
     * @return if the KnightShift server is knight
     */
    public boolean isKnight() {
        return this.knightshiftState == KnightShiftState.KNIGHT
                || this.knightshiftState == KnightShiftState.TRANSITIONING_TO_KNIGHT
                || this.knightshiftState == KnightShiftState.TRANSITIONING_TO_ACTIVE
                || this.transitioningToActive
                || this.transitioningToKnight;
    }

    /**
     * Inserts the job into the server.
     *
     * @param time - the time the job is inserted
     * @param job - the job to be inserted
     */
    @Override
    public void insertJob(final double time, final Job job) {
	

	if(this.knightshiftState == KnightShiftState.ACTIVE || this.knightshiftState ==KnightShiftState.TRANSITIONING_TO_ACTIVE || super.getInstantUtilization()+1/super.sockets.size() <= this.knightCapability) {
	    // if active and high utilization, insertjob
	    // or if already in knight and low utilization, insertjob

	    super.insertJob(time, job);

	} else if ((this.transitioningToKnight || this.knightshiftState == KnightShiftState.KNIGHT) && (super.getInstantUtilization()+1/super.sockets.size()) > this.knightCapability) {
	    // else if knight and high, wakeup primary, insertjob.
	    //System.out.println("Here");
	    this.transistionToActive(time);
	    super.insertJob(time, job);

	} else {

            Sim.fatalError("Uknown power state");

	}


        // if (this.knightshiftState == KnightShiftState.ACTIVE) {

        //     super.insertJob(time, job);

        // } else if (this.knightshiftState == KnightShiftState.TRANSITIONING_TO_KNIGHT) {

        //     this.transistionToActive(time);
        //     this.queue.add(job);

        //     // Job has entered the system
        //     this.jobsInServerInvariant++;

        // } else if (this.knightshiftState
        //             == KnightShiftState.TRANSITIONING_TO_ACTIVE) {

        //     this.queue.add(job);
        //     // Job has entered the system
        //     this.jobsInServerInvariant++;

        // } else if (this.knightshiftState == KnightShiftState.KNIGHT) {

        //     this.transistionToActive(time);
        //     this.queue.add(job);
        //     // Job has entered the system
        //     this.jobsInServerInvariant++;

        // } else {

        //     Sim.fatalError("Uknown power state");

        // }
    }

    /**
     * Get the time for the KnightShift server to transition.
     *
     * @return the time for the KnightShift server to transition
     */
    public double getKnightTransitionTime() {
        return this.knightTransitionTime;
    }

    /**
     * Transitions the server to the active state.
     *
     * @param time - the time to start transitioning
     */
    public void transistionToActive(final double time) {
        if (!this.isKnight()) {
            Sim.fatalError("Trying to transition to active when not in knight");
        }

        // if (!this.isPaused()) {
        //     Sim.fatalError("Trying to transition to active when not paused");
        // }

        double extraDelay = 0;
        if (this.transitionEvent != null) {
            double timeServerWouldHaveReachedKnight = this.transitionEvent
                    .getTime();
            extraDelay += timeServerWouldHaveReachedKnight - time;
            this.getExperiment().cancelEvent(this.transitionEvent);
            this.transitioningToKnight = false;
        }
        this.transitioningToActive = true;
        this.knightshiftState = KnightShiftState.TRANSITIONING_TO_ACTIVE;
        double knightTime = time + extraDelay + this.knightTransitionTime;
        KnightShiftTransitionedToActiveEvent knightEvent
            = new KnightShiftTransitionedToActiveEvent(knightTime,
                                                    this.getExperiment(),
                                                    this);
        this.getExperiment().addEvent(knightEvent);
    }

    /**
     * Transition the server to the knight state.
     *
     * @param time - the time the server is transitioned
     */
    public void transistionToKnight(final double time) {
        // Make sure this transition is valid
        if (this.isKnight()) {
            Sim.fatalError("Trying to transition to knight when in knight");
        }
        // Make sure this transition is valid
        // if (this.isPaused()) {
        //     Sim.fatalError("Trying to transition to knight when paused");
        //}

        this.knightshiftState = KnightShiftState.TRANSITIONING_TO_KNIGHT;
        this.transitioningToKnight = true;
        double knightTime = time + this.knightTransitionTime;
        KnightShiftTransitionedToKnightEvent knightEvent
            = new KnightShiftTransitionedToKnightEvent(knightTime,
                                                 this.getExperiment(),
                                                 this);
        this.transitionEvent = knightEvent;
        this.getExperiment().addEvent(knightEvent);
	
	
	super.disableSockets(time,(int)(super.sockets.size()*knightCapability));
	//System.out.println("To Knight");
	super.setDvfsSpeed(time,knightSpeed);
        //this.pauseProcessing(time);
    }

    /**
     * Removes a job from the server.
     *
     * @param time
     *            - the time the job is removed
     * @param job
     *            - the job to be removed
     */
    @Override
    public void removeJob(final double time, final Job job) {
        super.removeJob(time, job);

	// Check utilization, if utilization is low, then transition to Knight
	if (super.getInstantUtilization() < this.knightCapability && this.knightshiftState == KnightShiftState.ACTIVE) {
	    this.transistionToKnight(time);
	}

        // if (this.getJobsInService() == 0) {
        //     this.transistionToKnight(time);
        //}

    }

    /**
     * Sets the server to active.
     *
     * @param time
     *            - the time the server becomes active
     */
    public void setToActive(final double time) {
        this.transitioningToActive = false;
        // Server is now fully in the active mode
        this.knightshiftState = KnightShiftState.ACTIVE;
        // Start all the jobs possible and queue the ones that aren't
	super.enableSockets(time);
	//System.out.println("To Main");
	super.setDvfsSpeed(time,1.0);
        //this.resumeProcessing(time);
    }

    /**
     * Checks if the server is currently transitioning to the active state.
     *
     * @return if the server is currently transitioning to the active state
     */
    public boolean isTransitioningToActive() {
        return transitioningToActive;
    }

    /**
     * Checks if the server is currently transitioning to the knight state.
     *
     * @return if the server is currently transitioning to the knight state
     */
    public boolean isTransitioningToKnight() {
        return transitioningToKnight;
    }

    /**
     * Puts the server in the knight mode.
     *
     * @param time - the time the server is put in the knight mode.
     */
    public void setToKnight(final double time) {
        // Server is now fully in the knight mode
        this.transitioningToKnight = false;
        this.knightshiftState = KnightShiftState.KNIGHT;
        this.transitionEvent = null;
    }

    /**
     * Gets the instantaneous power of the KnightShift server.
     *
     * @return the instantaneous power of the KnightShift server
     */
    @Override
    public double getPower() {
        double power = 0.0d;

        if (this.knightshiftState == KnightShiftState.ACTIVE) {

            power = super.getPower()+this.knightPower;

        } else if (this.knightshiftState
                    == KnightShiftState.TRANSITIONING_TO_ACTIVE) {

            power = super.getPower()+this.knightPower;

        } else if (this.knightshiftState == KnightShiftState.TRANSITIONING_TO_KNIGHT) {

            power = super.getPower()+this.knightPower;

        } else if (this.knightshiftState == KnightShiftState.KNIGHT) {

            power = this.knightPower;

        }

        return power;
    }

}
