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

package experiment;

import generator.EmpiricalGenerator;
import generator.MTRandom;
import math.EmpiricalDistribution;
import core.Experiment;
import core.ExperimentInput;
import core.ExperimentOutput;
import core.Constants.StatName;
import core.Constants.TimeWeightedStatName;
import datacenter.DataCenter;
import datacenter.Server;
import datacenter.PowerNapServer;
import datacenter.KnightShiftServer;
import datacenter.Core.CorePowerPolicy;
import datacenter.Socket.SocketPowerPolicy;

public class KnightShiftExperiment {

	public KnightShiftExperiment(){

	}
	
	public void run(String workloadDir, String workload, int nServers) {

		// service file
		String arrivalFile = workloadDir+"workloads/"+workload+".arrival.cdf";
		String serviceFile = workloadDir+"workloads/"+workload+".service.cdf";

		// specify distribution
		int cores = 1;
		int sockets = 32;
		double targetRho = 16;
		
		EmpiricalDistribution arrivalDistribution = EmpiricalDistribution.loadDistribution(arrivalFile, 1e-3);
		EmpiricalDistribution serviceDistribution = EmpiricalDistribution.loadDistribution(serviceFile, 1e-3);

		double averageInterarrival = arrivalDistribution.getMean();
		double averageServiceTime = serviceDistribution.getMean();
		double qps = 1/averageInterarrival;
		double rho = qps/(cores*(1/averageServiceTime));
		double arrivalScale = rho/targetRho;
		averageInterarrival = averageInterarrival*arrivalScale;
		double serviceRate = 1/averageServiceTime;
		double scaledQps =(qps/arrivalScale);

//		System.out.println("Cores " + cores);
//		System.out.println("rho " + rho);		
//		System.out.println("recalc rho " + scaledQps/(cores*(1/averageServiceTime)));
//		System.out.println("arrivalScale " + arrivalScale);
//		System.out.println("Average interarrival time " + averageInterarrival);
//		System.out.println("QPS as is " +qps);
//		System.out.println("Scaled QPS " +scaledQps);
//		System.out.println("Service rate as is " + serviceRate);
//		System.out.println("Service rate x" + cores + " is: "+ (serviceRate)*cores);
//		System.out.println("\n------------------\n");

		// setup experiment
		ExperimentInput experimentInput = new ExperimentInput();		

		MTRandom rand = new MTRandom(1);
		EmpiricalGenerator arrivalGenerator  = new EmpiricalGenerator(rand, arrivalDistribution, "arrival", arrivalScale);
		EmpiricalGenerator serviceGenerator  = new EmpiricalGenerator(rand, serviceDistribution, "service", 1.0);

		// add experiment outputs
		ExperimentOutput experimentOutput = new ExperimentOutput();
		experimentOutput.addOutput(StatName.SOJOURN_TIME, .05, .95, .05, 5000);
		experimentOutput.addTimeWeightedOutput(TimeWeightedStatName.SERVER_POWER, .05, .95, .05, 5000, .1);

		Experiment experiment = new Experiment("KnightShift test", rand, experimentInput, experimentOutput);
		
		// setup datacenter
		DataCenter dataCenter = new DataCenter();

		// specify servers
		/*
		 * memory = 25+10
		 * disk = 10+1
		 * other = 5+5
		 */
		double primaryPeakPower = 17;
		double primaryIdlePower = 15;
		double knightSpeed = 1.0;
		double knightCapability = .15;
		double knightPower = 20;
		double transitionTime = 5;
		for(int i = 0; i < nServers; i++) {
		    //Server server = new Server(sockets, cores, experiment, arrivalGenerator, serviceGenerator);
		    //Server server = new PowerNapServer(sockets, cores, experiment, arrivalGenerator, serviceGenerator, 0.001, 5);
		    Server server = new KnightShiftServer(sockets, cores, experiment, arrivalGenerator, serviceGenerator, transitionTime, knightPower, knightCapability,knightSpeed);

			server.setSocketPolicy(SocketPowerPolicy.NO_MANAGEMENT);
			server.setCorePolicy(CorePowerPolicy.NO_MANAGEMENT);	
			double coreActivePower = (primaryPeakPower-primaryIdlePower)/cores;
			double coreHaltPower = 0;//coreActivePower*.2;
			double coreParkPower = 0;

			double socketActivePower = primaryIdlePower/sockets;
			double socketParkPower = 0;

			server.setCoreActivePower(coreActivePower);
			server.setCoreParkPower(coreParkPower);
			server.setCoreIdlePower(coreHaltPower);

			server.setSocketActivePower(socketActivePower);
			server.setSocketParkPower(socketParkPower);
			System.out.println("Max Power: " + server.getMaxPower());
			System.out.println("Idle Power: " + server.getIdlePower());
			dataCenter.addServer(server);
		}//End for i
		
		experimentInput.setDataCenter(dataCenter);

		// run the experiment
		//experiment.setEventLimit(5000000);
		experiment.run();

		// display results
		System.out.println("====== Results ======");
		double responseTimeMean = experiment.getStats().getStat(StatName.SOJOURN_TIME).getAverage();
		System.out.println("Response Mean: " + responseTimeMean);
		double responseTime95th = experiment.getStats().getStat(StatName.SOJOURN_TIME).getQuantile(.95);
		System.out.println("Response 95: " + responseTime95th);
		double responseTime99th = experiment.getStats().getStat(StatName.SOJOURN_TIME).getQuantile(.99);
		System.out.println("Response 99: " + responseTime99th);
		double averagePower = experiment.getStats().getTimeWeightedStat(TimeWeightedStatName.SERVER_POWER).getAverage();
		//double averagePower = experiment.getStats().getStat(StatName.POWER_ESTIMATE).getAverage();
		System.out.println("Average Power: " + averagePower);

		
	}//End run()
	
	public static void main(String[] args) {
		KnightShiftExperiment exp  = new KnightShiftExperiment();
		exp.run(args[0],args[1],Integer.valueOf(args[2]));
	}
	
}//End KnightShiftExperiment
