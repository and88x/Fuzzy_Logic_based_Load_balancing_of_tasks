package updated_HBLoadbalancer;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.Arrays;



import java.util.*;  
import java.util.stream.*;



 /** A network of 10 datacenters, with 2-6 host and 50vm */

public class Honeybee {
	
	private  List<Cloudlet> cloudletList;
    private  List<Vm> vmlist;
	private  List<Datacenter> datacenter;
	private  double AVGTIME = 0.0;
	//										10				i+2000				50			 j+100
	public double simulateHoneybee( int reqdatacener, int requestTasks, int requestVms, Long length, int experiment_number){
		
		Log.printLine("Starting Honeybee load balancing runnning HoneybeeLoadbalancing...");

		try {
			int num_user       = 20;     // number of users
			Calendar calendar  = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events
			
			CloudSim.init(num_user, calendar, trace_flag);
			
			//@SuppressWarnings("unused");
			datacenter = new Create_Datacenter().createDatacenter(reqdatacener);//creating datacenter
			
			HoneyBeeLoadbalancer broker= createBroker(requestTasks, length);
			broker.experiment_number = experiment_number;
			
			int brokerId = broker.getId();		
			//												50				12
			vmlist       = new Create_VMachine().createVms(requestVms,  brokerId); //creating Vms
			cloudletList = new Create_Cloudlets().createCloudlet(brokerId, requestTasks, length);//creating cloudlet
			
			broker.submitVmList(vmlist);	
			broker.submitCloudletList(cloudletList);
			
			broker.HoneyBeeVmschedule();
			CloudSim.startSimulation();// starting the simulation

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			
			CloudSim.stopSimulation();

			//AVGTIME = printCloudletList(newList);

			Log.printLine("Honeybee load balancing completd successsfully!!!!!!!!!!!!!");
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
		//return AVGTIME;
		return 0.0;
	}
 
 private static HoneyBeeLoadbalancer createBroker(int requestTasks, Long length) {

	 HoneyBeeLoadbalancer broker = null;
     try {
    	 broker = new HoneyBeeLoadbalancer("Broker", requestTasks, length);
     } catch (Exception e) {
    	 e.printStackTrace();
    	 return null;
     }
   	 return broker;
   }
	
	
 private double printCloudletList(List<Cloudlet> list) throws IOException {
     int size = list.size();
     Cloudlet cloudlet;
     
     String indent = "     ";
     Log.printLine();
     Log.printLine("========== OUTPUT ==========");
     Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
             "Data center ID" + indent + "VM ID" + indent + "Time" 
    		 + indent + "Start Time" + indent + "Finish Time"+ indent + "Waiting Time");
     
     double waitTimeSum = 0.0;
     double CPUTimeSum  = 0.0;
     int totalValues    = 0;
     DecimalFormat dft  = new DecimalFormat("###.##");
     
     double responce_time[] = new double[size];
     
     for (int i = 0; i < size; i++) {
         cloudlet = list.get(i);
         Log.print(cloudlet.getCloudletId() + indent + indent);
        
         if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
        	 Log.print("SUCCESS");
             CPUTimeSum  = CPUTimeSum  + cloudlet.getActualCPUTime();
             waitTimeSum = waitTimeSum + cloudlet.getWaitingTime();
             totalValues++;
             
         	 Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                  indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                  indent + indent + dft.format(cloudlet.getFinishTime())+ indent + indent + dft.format(cloudlet.getWaitingTime()));   
         	 
         	 responce_time[i] = cloudlet.getActualCPUTime();
         	 
         }
     }
     DoubleSummaryStatistics stats = DoubleStream.of(responce_time).summaryStatistics();
     double degree_of_inbalance = (stats.getMax() - stats.getMin())/(CPUTimeSum/ totalValues);
     double makespan = stats.getMax();
     
     // Show the parameters of the questions 2 and 3
     System.out.println("min = "+stats.getMin());
     System.out.printf("Responce_Time_av,Makespan,DI%n");
     System.out.printf("%.3f,%.3f,%.3f%n", CPUTimeSum/totalValues, makespan, degree_of_inbalance);
     
     Log.printLine();
     Log.printLine();
     Log.printLine("TotalCPUTime : "             + CPUTimeSum);
     Log.printLine("TotalWaitTime : "            + waitTimeSum);
     Log.printLine("TotalCloudletsFinished : "   + totalValues);
     Log.printLine("AverageCloudletsFinished : " + (CPUTimeSum/ totalValues));
     Log.printLine();
     Log.printLine();
    
    return (CPUTimeSum/ totalValues);
 }
}
