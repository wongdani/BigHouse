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
 * A Low EP server 
 *
 * @author Daniel Wong (dwong@ece.ucr.edu) 
 */
public class ServerLowEP extends PowerNapServer {

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
    public ServerLowEP(final int theNumberOfSockets,
                          final int coresPerSocket,
                          final Experiment experiment,
                          final Generator arrivalGenerator,
                          final Generator serviceGenerator,
                          final double theNapTransitionTime,
                          final double theNapPower) {
        super(theNumberOfSockets, coresPerSocket, experiment, arrivalGenerator,
                serviceGenerator, theNapTransitionTime, theNapPower);

	this.lookupPower = new double[] {156.7528953, 157.2195044, 157.6861135, 158.1527225, 158.6193316, 159.0859407, 159.5525498, 160.0191588, 160.4857679, 160.952377, 
					 161.418986, 162.1175096, 162.8160331, 163.5145566, 164.2130801, 164.9116037, 165.6101272, 166.3086507, 167.0071743, 167.7056978, 
					 168.4042213, 168.6368873, 168.8695532, 169.1022191, 169.3348851, 169.567551, 169.8002169, 170.0328828, 170.2655488, 170.4982147, 
					 170.7308806, 171.1078233, 171.4847659, 171.8617086, 172.2386512, 172.6155939, 172.9925365, 173.3694792, 173.7464218, 174.1233645, 
					 174.5003071, 175.063386, 175.6264649, 176.1895438, 176.7526226, 177.3157015, 177.8787804, 178.4418593, 179.0049381, 179.568017, 
					 180.1310959, 180.8574724, 181.583849, 182.3102255, 183.036602, 183.7629786, 184.4893551, 185.2157316, 185.9421082, 186.6684847, 
					 187.3948612, 187.9603751, 188.525889, 189.0914029, 189.6569167, 190.2224306, 190.7879445, 191.3534584, 191.9189722, 192.4844861, 
					 193.05, 193.5040625, 193.958125, 194.4121875, 194.86625, 195.3203125, 195.774375, 196.2284375, 196.6825, 197.1365625, 
					 197.590625, 197.8365625, 198.0825, 198.3284375, 198.574375, 198.8203125, 199.06625, 199.3121875, 199.558125, 199.8040625, 
					 200.05, 200.5330716, 201.0161432, 201.4992148, 201.9822864, 202.465358, 202.9484296, 203.4315012, 203.9145728, 204.3976443, 
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
     * Gets the instantaneous power of the Low EP server.
     *
     * @return the instantaneous power of the Low EP server
     */
    @Override
    public double getPower() {
	
	if (this.powerNapState == PowerNapState.NAP) {
            return super.napPower;
        } else {
	    double util = this.getInstantUtilization();
	    int utilRoundUp = (int)Math.ceil(util * 100.0); 
	    return lookupPower[utilRoundUp];
	}

    }


}
