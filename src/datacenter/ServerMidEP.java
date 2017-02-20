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
 * A Mid EP server 
 *
 * @author Daniel Wong (dwong@ece.ucr.edu) 
 */
public class ServerMidEP extends PowerNapServer {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;


    /** EP Power Lookup */
    protected double[] lookupPower;

    /**
     * Creates a new PowerNapServer.
     *
     * @param theNumberOfSockets - the number of sockets the server has
     * @param coresPerSocket - the number of cores per sockets
     * @param experiment - the experiment the server is part of
     * @param arrivalGenerator - The interarrival generator for the server
     * @param serviceGenerator - The service time generator for the server
     * @param theNapTransitionTime - the transition time in
     * and out of the the nap mode (in seconds)
     * @param theNapPower - the power of the server while in nap mode (in watts)
     */
    public ServerMidEP(final int theNumberOfSockets,
                          final int coresPerSocket,
                          final Experiment experiment,
                          final Generator arrivalGenerator,
                          final Generator serviceGenerator,
                          final double theNapTransitionTime,
                          final double theNapPower) {
        super(theNumberOfSockets, coresPerSocket, experiment, arrivalGenerator,
                serviceGenerator, theNapTransitionTime, theNapPower);

	this.lookupPower = new double[] { 55.6, 59.58, 63.56, 67.54, 71.52, 75.5, 79.48, 83.46, 87.44, 91.42, 
					  95.4, 96.56, 97.72, 98.88, 100.04, 101.2, 102.36, 103.52, 104.68, 105.84, 
					  107.0, 107.8, 108.6, 109.4, 110.2, 111.0, 111.8, 112.6, 113.4, 114.2, 
					  115.0, 115.9, 116.8, 117.7, 118.6, 119.5, 120.4, 121.3, 122.2, 123.1, 
					  124.0, 124.9, 125.8, 126.7, 127.6, 128.5, 129.4, 130.3, 131.2, 132.1, 
					  133.0, 133.9, 134.8, 135.7, 136.6, 137.5, 138.4, 139.3, 140.2, 141.1, 
					  142.0, 143.3, 144.6, 145.9, 147.2, 148.5, 149.8, 151.1, 152.4, 153.7, 
					  155.0, 156.8, 158.6, 160.4, 162.2, 164.0, 165.8, 167.6, 169.4, 171.2, 
					  173.0, 174.9, 176.8, 178.7, 180.6, 182.5, 184.4, 186.3, 188.2, 190.1, 
					  192.0, 193.2880716, 194.5761432, 195.8642148, 197.1522864, 198.440358, 199.7284296, 201.0165012, 202.3045728, 203.5926443, 
					  204.8807159};

	double peak = 0.0; 
	int peakUtil = 0;
	for (int i = 0; i < 101; i++){
		if ( (i/this.lookupPower[i]) > peak){
			peak = i/this.lookupPower[i];
			peakUtil = i;
		} 
	}
	this.peakEfficiencyUtilization = peakUtil/100.0;
	System.out.println("Peak efficiency Util: " + String.valueOf(this.peakEfficiencyUtilization));
	this.peakEfficiency = peak;
	System.out.println("Peak efficiency: " + String.valueOf(this.peakEfficiency));
    }
    /**
     * Gets the instantaneous power of the Mid EP server.
     *
     * @return the instantaneous power of the Mid EP server
     */
    @Override
    public double getPower() {

	if (this.powerNapState == PowerNapState.NAP) {
            return this.napPower;
        } else {
	    double util = this.getInstantUtilization();
	    int utilRoundUp = (int)Math.ceil(util * 100.0); 
	    return lookupPower[utilRoundUp];
	}

    }


}
