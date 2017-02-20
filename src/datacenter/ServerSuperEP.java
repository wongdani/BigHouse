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
 * A Super EP server 
 *
 * @author Daniel Wong (dwong@ece.ucr.edu) 
 */
public class ServerSuperEP extends PowerNapServer {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;


    /** EP Power Lookup */
    protected double[] lookupPower;

    /**
     * Creates a new ServerSuperEP.
     *
     * @param theNumberOfSockets - the number of sockets the server has
     * @param coresPerSocket - the number of cores per sockets
     * @param experiment - the experiment the server is part of
     * @param arrivalGenerator - The interarrival generator for the server
     * @param serviceGenerator - The service time generator for the server
     */
    public ServerSuperEP(final int theNumberOfSockets,
                          final int coresPerSocket,
                          final Experiment experiment,
                          final Generator arrivalGenerator,
                          final Generator serviceGenerator,
                          final double theNapTransitionTime,
                          final double theNapPower) {
        super(theNumberOfSockets, coresPerSocket, experiment, arrivalGenerator,
                serviceGenerator, theNapTransitionTime, theNapPower);

	this.lookupPower = new double[] { 15.75295556, 17.59892071, 19.44488585, 21.29085099, 23.13681613, 24.98278127, 26.82874642, 28.67471156, 30.5206767, 32.36664184, 
					  34.21260699, 35.00923091, 35.80585483, 36.60247876, 37.39910268, 38.19572661, 38.99235053, 39.78897445, 40.58559838, 41.3822223, 
					  42.17884623, 42.92777282, 43.67669942, 44.42562601, 45.17455261, 45.92347921, 46.6724058, 47.4213324, 48.17025899, 48.91918559, 
					  49.66811218, 50.36934145, 51.07057072, 51.77179999, 52.47302926, 53.17425852, 53.87548779, 54.57671706, 55.27794633, 55.9791756, 
					  56.68040486, 57.50087745, 58.32135004, 59.14182263, 59.96229522, 60.7827678, 61.60324039, 62.42371298, 63.24418557, 64.06465816, 
					  64.88513074, 66.34951726, 67.81390378, 69.2782903, 70.74267681, 72.20706333, 73.67144985, 75.13583637, 76.60022288, 78.0646094, 
					  79.52899592, 81.39880972, 83.26862353, 85.13843734, 87.00825114, 88.87806495, 90.74787875, 92.61769256, 94.48750637, 96.35732017, 
					  98.22713398, 100.7647104, 103.3022868, 105.8398632, 108.3774396, 110.915016, 113.4525924, 115.9901688, 118.5277452, 121.0653216, 
					  123.602898, 127.2852102, 130.9675225, 134.6498348, 138.3321471, 142.0144593, 145.6967716, 149.3790839, 153.0613962, 156.7437084, 
					  160.4260207, 164.8714902, 169.3169598, 173.7624293, 178.2078988, 182.6533683, 187.0988378, 191.5443074, 195.9897769, 200.4352464, 
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
     * Gets the instantaneous power of the Super EP server.
     *
     * @return the instantaneous power of the Super EP server
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
