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

/** 
 * This package is only used during development
 * It's not a part of final SQS 
 */

package experiment;

//TODO delete this
import generator.EmpiricalGenerator;
import generator.MTRandom;
import math.EmpiricalDistribution;
import core.Experiment;
import core.ExperimentInput;
import core.ExperimentOutput;
import core.Constants.StatName;
import core.Constants.TimeWeightedStatName;
import datacenter.DataCenter;
import datacenter.PowerCappingEnforcer;
import datacenter.PowerNapServer;
import datacenter.Server;
import datacenter.Core.CorePowerPolicy;
import datacenter.Socket.SocketPowerPolicy;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.Long;
public class SingleMachine {

	public SingleMachine(){
		
	}
	
	public void run(String workloadDir, String workload) {
		

		ExperimentInput experimentInput = new ExperimentInput();		

		String arrivalFile = workloadDir+"workloads/"+workload+".arrival.cdf";
		String serviceFile = workloadDir+"workloads/"+workload+".service.cdf";

		int cores = 1;
		int sockets = 32;
		double targetRho = .1;
		
		EmpiricalDistribution arrivalDistribution = EmpiricalDistribution.loadDistribution(arrivalFile, 1e-3);
		EmpiricalDistribution serviceDistribution = EmpiricalDistribution.loadDistribution(serviceFile, 1e-3);

		double averageInterarrival = arrivalDistribution.getMean();
		double averageServiceTime = serviceDistribution.getMean();
		double qps = 1/averageInterarrival;
		double rho = qps/(cores*sockets*(1/averageServiceTime));
		double arrivalScale = rho/targetRho;
		averageInterarrival = averageInterarrival*arrivalScale;
		double serviceRate = 1/averageServiceTime;
		double scaledQps =(qps/arrivalScale);

		System.out.println("Cores " + cores*sockets);
		System.out.println("rho " + rho);		
		System.out.println("recalc rho " + scaledQps/(cores*sockets*(1/averageServiceTime)));
		System.out.println("arrivalScale " + arrivalScale);
		System.out.println("Average interarrival time " + averageInterarrival);
		System.out.println("QPS as is " +qps);
		System.out.println("Scaled QPS " +scaledQps);
		System.out.println("Service rate as is " + serviceRate);
		System.out.println("Service rate x" + cores*sockets + " is: "+ (serviceRate)*cores*sockets);
		System.out.println("\n------------------\n");

		MTRandom rand = new MTRandom(1);
		
		EmpiricalGenerator arrivalGenerator  = new EmpiricalGenerator(rand, arrivalDistribution, "arrival", arrivalScale);
		EmpiricalGenerator serviceGenerator  = new EmpiricalGenerator(rand, serviceDistribution, "service", 1.0);

		ExperimentOutput experimentOutput = new ExperimentOutput();
		experimentOutput.addOutput(StatName.SOJOURN_TIME, .05, .95, .05, 5000);
		experimentOutput.addTimeWeightedOutput(TimeWeightedStatName.SERVER_POWER, .05, .95, .05, 5000, 1);
		experimentOutput.addTimeWeightedOutput(TimeWeightedStatName.SERVER_UTILIZATION, .05, .95, .05, 5000, .1);

		Experiment experiment = new Experiment("Single Machine", rand, experimentInput, experimentOutput);
		
		DataCenter dataCenter = new DataCenter();		

		double primaryPeakPower = 17;
		double primaryIdlePower = 15;
		int nServers = 1;
		for(int i = 0; i < nServers; i++) {
			Server server = new Server(sockets, cores, experiment, arrivalGenerator, serviceGenerator);
//			Server server = new PowerNapServer(sockets, cores, experiment, arrivalGenerator, serviceGenerator, 0.001, 5);
			server.setSocketPolicy(SocketPowerPolicy.NO_MANAGEMENT);
			server.setCorePolicy(CorePowerPolicy.NO_MANAGEMENT);	
			double coreActivePower = (primaryPeakPower-primaryIdlePower)/cores;
			double coreHaltPower = 0; //coreActivePower*.2;
			double coreParkPower = 0;

			double socketActivePower = primaryIdlePower/sockets;
			double socketParkPower = 0;

			server.setCoreActivePower(coreActivePower);
			server.setCoreParkPower(coreParkPower);
			server.setCoreIdlePower(coreHaltPower);
			server.setSocketActivePower(socketActivePower);
			server.setSocketParkPower(socketParkPower);
			dataCenter.addServer(server);
		}//End for i
		
		
		experimentInput.setDataCenter(dataCenter);
		//experiment.setEventLimit(5000000);
		experiment.run();
		
		double responseTimeMean = experiment.getStats().getStat(StatName.SOJOURN_TIME).getAverage();
		System.out.println("Response Mean: " + responseTimeMean);
		double responseTime95th = experiment.getStats().getStat(StatName.SOJOURN_TIME).getQuantile(.95);
		System.out.println("Response 95: " + responseTime95th);
		double responseTime99th = experiment.getStats().getStat(StatName.SOJOURN_TIME).getQuantile(.99);
		System.out.println("Response 99: " + responseTime99th);

		double averagePower = experiment.getStats().getTimeWeightedStat(TimeWeightedStatName.SERVER_POWER).getAverage();
		System.out.println("Average Power: " + averagePower);
		double averageUtilization = experiment.getStats().getTimeWeightedStat(TimeWeightedStatName.SERVER_UTILIZATION).getAverage();
		System.out.println("Average Utilization: " + averageUtilization);

	}//End run()

	public static void getCSV(String fileName) {
		String line = "";
		String csvSplitChar = ",";
		BufferedReader bufReader = null;
		
		String timeInSec = "";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ArrayList<Long> timeStamps = new ArrayList<Long>();
		ArrayList<Double> regValues = new ArrayList<Double>();

		try {
			bufReader = new BufferedReader(new FileReader("workloads/" + fileName));
			while( (line = bufReader.readLine()) != null) {
				String[] getLine = line.split(csvSplitChar);
				if(getLine[1].equals("RegDTest")) { //FIXME: prone to errors if file doesnt start with "RegDTest"
					continue; // ignore this line
				}
				else {
					//getLine[0] is in sec
					timeInSec = getLine[0];	
					date = sdf.parse("1970-01-01 " + timeInSec);
					
					timeStamps.add(date.getTime() - 28800000); // subtract for timezone diff
					regValues.add(Double.parseDouble(getLine[1]) );
				}
			}
		
			System.out.println("File read complete, printing values:");
			for(int i = 0; i < timeStamps.size(); ++i) {
				System.out.println(timeStamps.get(i) + ", " + regValues.get(i));
			}
			System.out.println("End of values");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			System.out.println("Problem reading date\n");
		}
	}
	public static void main(String[] args) {
		SingleMachine exp  = new SingleMachine();
		exp.getCSV("reg-d.csv");
		exp.run(args[0],args[1]);
	}
	
}
