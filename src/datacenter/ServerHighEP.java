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
 * A High EP server 
 *
 * @author Daniel Wong (dwong@ece.ucr.edu) 
 */
public class ServerHighEP extends PowerNapServer {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;


    /** EP Power Lookup */
    protected double[] lookupPower;

    /**
     * Creates a new ServerHighEP.
     *
     * @param theNumberOfSockets - the number of sockets the server has
     * @param coresPerSocket - the number of cores per sockets
     * @param experiment - the experiment the server is part of
     * @param arrivalGenerator - The interarrival generator for the server
     * @param serviceGenerator - The service time generator for the server
     */
    public ServerHighEP(final int theNumberOfSockets,
                          final int coresPerSocket,
                          final Experiment experiment,
                          final Generator arrivalGenerator,
                          final Generator serviceGenerator,
                          final double theNapTransitionTime,
                          final double theNapPower) {
        super(theNumberOfSockets, coresPerSocket, experiment, arrivalGenerator,
                serviceGenerator, theNapTransitionTime, theNapPower);
	this.lookupPower = new double[] { 34.2, 35.87, 37.54, 39.21, 40.88, 42.55, 44.22, 45.89, 47.56, 49.23, 
					  50.9, 51.73, 52.56, 53.39, 54.22, 55.05, 55.88, 56.71, 57.54, 58.37, 
					  59.2, 60.0, 60.8, 61.6, 62.4, 63.2, 64.0, 64.8, 65.6, 66.4, 
					  67.2, 67.96, 68.72, 69.48, 70.24, 71.0, 71.76, 72.52, 73.28, 74.04, 
					  74.8, 75.66, 76.52, 77.38, 78.24, 79.1, 79.96, 80.82, 81.68, 82.54, 
					  83.4, 84.76, 86.12, 87.48, 88.84, 90.2, 91.56, 92.92, 94.28, 95.64, 
					  97, 98.69, 100.38, 102.07, 103.76, 105.45, 107.14, 108.83, 110.52, 112.21, 
					  113.9, 116.12, 118.34, 120.56, 122.78, 125, 127.22, 129.44, 131.66, 133.88, 
					  136.1, 139.24, 142.38, 145.52, 148.66, 151.8, 154.94, 158.08, 161.22, 164.36, 
					  167.5, 171.2380716, 174.9761432, 178.7142148, 182.4522864, 186.190358, 189.9284296, 193.6665012, 197.4045728, 201.1426443, 
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
     * Gets the instantaneous power of the High EP server.
     *
     * @return the instantaneous power of the High EP server
     */
    @Override
    public double getPower() {

	if (this.powerNapState == PowerNapState.NAP) {
            return this.napPower;
        } else {
	    double util = this.getInstantUtilization();
	    int utilRoundUp = (int)Math.ceil(util * 100.0); 
	    return this.lookupPower[utilRoundUp];
	}
    }


}
