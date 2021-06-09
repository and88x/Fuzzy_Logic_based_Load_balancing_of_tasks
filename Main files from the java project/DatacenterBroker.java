/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.*;  

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.stream.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

import java.util.concurrent.TimeUnit;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	ArrayList<Integer> VM_host_Id = new ArrayList<Integer>();	// host IDs of each VM
	ArrayList<Integer> VM_dataCenter_Id = new ArrayList<Integer>();	// datacenters IDs of each VM 
	List<Integer> diffHosts;	// list with the host IDs taking in consideration the datacenters
	List<Integer> diffRealHost;	// list for iterate the different Host IDs
	double[] quantiles;
	// Parameters
	public int requestTasks;
	public Long length;
	public int experiment_number;
	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		//Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
		//		+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());	
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
			
			VM_host_Id.add((int) VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
			VM_dataCenter_Id.add((int) datacenterId);
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}
		
		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();	// The fuzzy part is on this method
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			//Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
			//
		}
		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	
	public static void pause(int ms) {
	    try {
	        Thread.sleep(ms);
	    } catch (InterruptedException e) {
	        System.err.format("IOException: %s%n", e);
	    }
	}
	
	protected void submitCloudlets() {
		int vmIndex = 0;
		int cloudletVmId;
		
		// Charge the jFuzzyLogic object in the workspace
		String filename = "tipper.fcl";
		FIS fis = FIS.load(filename, true);
		//
		if (fis == null) {
			System.err.println("Can't load file: '" + filename + "'");
			System.exit(1);
		}
		//
		filename = "VM_selector.fcl";
		FIS fisVM = FIS.load(filename, true);
		//
		if (fisVM == null) {
			System.err.println("Can't load file: '" + filename + "'");
			System.exit(1);
		}
		//
		// Get jFuzzyLogic default function block
		FunctionBlock fb   = fis.getFunctionBlock(null);
		FunctionBlock fbVM = fisVM.getFunctionBlock(null);
		
		// VM_host
		List<Vm> VMs = getVmsCreatedList();
		
		// This method give an unique Host_identifier to each VM
		assignRealHostToVm(VMs);
		//
		List<Cloudlet> allCloudlets = getCloudletList();

		// This method check the overloaded VMs
		checkOverload(VMs);
		
		for (Cloudlet cloudlet : allCloudlets) {	// All cloudlets are checked if have an overloaded Host or VM
			Vm vm;
			
			// Performs the fuzzy block and calculate all parameters
			fuzzy_system(VMs, cloudlet, allCloudlets, fb, fbVM);
			
			// Load the cloudletID or re-assign a new VM to the cloudlet
			cloudletVmId = cloudlet.getVmId();

			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudletVmId == -1) {			
				
				vm = VMs.get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(VMs, cloudletVmId);
				
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId() 
					+ " Host " + vm.getRealHost()); 		// Broker: Sending cloudlet 0 to VM #47
			
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
			
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
		
	}
	
	protected void fuzzy_system(List<Vm> VMs, Cloudlet cloudlet, List<Cloudlet> allTasks, 
											FunctionBlock fb, FunctionBlock fbVM) {
		// On this method will be calculated all parameters needed for the host fuzzy selector 
		// and VM fuzzy selector
		int VmId      = cloudlet.getVmId();
		Vm assignedVm = VmList.getById(VMs, VmId);
		//
		if (checkOverloadByAssignedTime(assignedVm.totalAssignedTime)) {
		//if (true) {
			double capacityVM;
			int NHost = diffRealHost.size();
			
//			System.out.println("Assigned time = "+assignedVm.asignedTime);
			
			// List for save the parameters on each host (RH is for Real Host) 
			List<Double> capacityRH = new ArrayList<Double>(Collections.nCopies(NHost, 0.0));	
			List<Double> loadRH     = new ArrayList<Double>(Collections.nCopies(NHost, 0.0));
			List<Double> exTimeRH   = new ArrayList<Double>(Collections.nCopies(NHost, 0.0));
			//
			for (int j=0; j<VMs.size(); j++) {
				Vm vm           = getVmList().get(j);
				double loadCL   = 0;		// CL is for Cloudlet, that means that it is and parameter of a task
				int realHost    = vm.getRealHost();
				double exTimeCL = 0;
				double powerVM  = 0;
				double exTimeVM = 0;
				double costVM   = 0.0;
				double loadVM	= 0.0;
				
				// Capacity
				capacityVM = vm.getNumberOfPes()*vm.getMips();	//+vm.getBw()
				capacityRH.set(realHost, capacityVM + capacityRH.get(realHost));//(RH is for Real Host) 
				
				// Update the vector that contains the Real Host identifier
				diffHosts.set(j, realHost);
				//
				double cpuUtil, ramUtil;
				
				// Compute the Cloudlet and VM parameters
				for (int i : vm.cloudletIDs) {
					Cloudlet task = allTasks.get(i);
					loadCL        = task.getCloudletLength();
					loadVM       += loadCL;
					exTimeCL      = loadCL / capacityVM;
					exTimeVM     += exTimeCL;
					//
					Host host = vm.getHost();
					//
					ramUtil  = 5.0 * vm.getRam() / host.getRam() // power of a 4Gb DDR4 ram
								 * task.getUtilizationOfCpu(exTimeCL);	
					cpuUtil  = 51.0 * vm.getMips() / host.getTotalMips()  // power of a intel core i3 processor
								 * task.getUtilizationOfRam(exTimeCL); 
					powerVM += cpuUtil + ramUtil;
					costVM  += 1.0*task.getCloudletFileSize() / vm.getSize();
				}
				loadRH.set(realHost, loadVM + loadRH.get(realHost));
				exTimeRH.set(realHost, exTimeVM + exTimeRH.get(realHost));	
				vm.costPerStorage   = costVM;
				vm.powerCompsuption = powerVM;
				vm.exTime 			= exTimeVM;
				
				System.out.println("VM"+vm.getId()+" time = "+exTimeVM+" cost = "+cost
						+" powerC = "+powerC+" realHost = "+vm.getRealHost());
				System.out.printf("Time = %.4f	Cost = %.4f	  PowerC = %.4f %n", exTimeVM, cost, powerC);
			}
			
			int maxiArg = -1;	// List position of the best Host
			double maxi = -1; 	// Fitness give by the Host fuzzy selector
			double fitness;
			
			// 2000 is for the initial task length (2000->20000) and 100 is for the initial number of task (100->1000) 
			final double CORRECTOR = 1.0*2000*100/(requestTasks*length);
			
			// Compute the Host parameters
			for (int i : diffRealHost) {
				double C   = capacityRH.get(i);
				double L   = loadRH.get(i);
				double exT = exTimeRH.get(i);
				
				System.out.printf("C = %f   L = %f   exT = %f %n", C, L, exT);
				// Set inputs in the fuzzy block
				fb.setVariable("capacity", C);
				fb.setVariable("load", L*CORRECTOR);
				fb.setVariable("exTime", exT*CORRECTOR);
				// Evaluate
				fb.evaluate();
				fitness = fb.getVariable("fitness").defuzzify();
				
				System.out.printf("Capacity = %.4f ",C);
				System.out.printf("Load = %.4f ",L*CORRECTOR);
				System.out.printf("exTime = %.4f ",exT*CORRECTOR);
				System.out.println("fitness = "+fitness);
				
				// To select the best Host using the fuzzy information
				if (maxi < fitness) {
					maxi    = fitness;
					maxiArg = i;
				}
			}		
			// Get the VMIDs on the selected host
			List<Integer> VMs_on_host = getAllIndexes(diffHosts, maxiArg);
			// Reuse the maxi parameter
			maxi = 0.0;
			
			// To find the best VM
			for (int vmID : VMs_on_host) {
				Vm vm = getVmList().get(vmID);
				double pTime = vm.exTime;
				double sCost = vm.costPerStorage;
				double power = vm.powerCompsuption;
				
				fbVM.setVariable("powerC", power);
				fbVM.setVariable("costS", sCost);
				fbVM.setVariable("exTime", pTime);
				fbVM.evaluate();
				
				fitness = fbVM.getVariable("fitness").defuzzify();
				
				System.out.printf("vmID = %d ",vmID);
				System.out.printf("pTime = %.4f ",pTime*CORRECTOR);
				System.out.printf("sCost = %.4f ",sCost*CORRECTOR);
				System.out.printf("power = %.4f ",power*CORRECTOR);
				System.out.println("fitness = "+fitness);
				if (maxi < fitness) {
					maxi    = fitness;
					maxiArg = vmID;
				}
			}
			//
		//if (checkOverloadByAssignedTime(assignedVm.totalAssignedTime)) {
			boolean condition = reCalculateTime(assignedVm, cloudlet.getCloudletId());
			
			// If the cloudlet's VM will be changed, all parameters are re-calculated
			if (condition) {
			//if (false) {
				Vm newVm = getVmList().get(maxiArg); 
				//
				double aTime = cloudlet.getCloudletLength() / (newVm.getMips()*newVm.getNumberOfPes());
				//
				newVm.addTask(cloudlet.getCloudletId(), aTime);
				bindCloudletToVm(cloudlet.getCloudletId(),maxiArg);
				
				System.out.printf("Change VM%d -> ", VmId);
				System.out.printf(" VM%d %n", maxiArg);
				
				// Modify the parameters of the previous VM
				Host host       = assignedVm.getHost();
				Cloudlet task   = allTasks.get(cloudlet.getCloudletId());
				capacityVM      = assignedVm.getNumberOfPes()*assignedVm.getMips();	//+vm.getBw()
				double loadCL   = task.getCloudletLength();
				double exTimeCL = loadCL / capacityVM;
				double ramUtil  = 5.0 * assignedVm.getRam() / host.getRam() // power of a 4Gb DDR4 ram
									* task.getUtilizationOfCpu(exTimeCL);	
				double cpuUtil  = 51.0 * assignedVm.getMips() / host.getTotalMips()  // power of a intel core i3 processor
									* task.getUtilizationOfRam(exTimeCL); 
				//
				assignedVm.costPerStorage   -= 1.0*task.getCloudletFileSize() / assignedVm.getSize();
				assignedVm.powerCompsuption -= cpuUtil + ramUtil;
				assignedVm.exTime           -= exTimeCL;
			}
			//System.out.printf(" VM%d %n", maxiArg);
		}
		//}
	}
	
	protected List<Integer> getAllIndexes(List<Integer> arr, int HostID) {
		// This method returns the VM identifiers that are allocated on the Host
	    List<Integer> indexes = new ArrayList<Integer>();
	    for(int i = 0; i < arr.size(); i++)
	        if (arr.get(i) == HostID) {
	            indexes.add(i);
	        }
	    return indexes;
	}
	
	protected boolean reCalculateTime(Vm vm, int cId) {
		// remove the Cloudlet to the previous VM
		List<Integer> cloudletId = vm.cloudletIDs;
		int index = -1;
	    for(int i = 0; i < cloudletId.size(); i++) {
	    	if (cloudletId.get(i) == cId) {
	            index = i;
	            break;
	        }
	    }
	    if (index != -1) {
	    	vm.removeTask(index);
	    	return true;
	    } else {
	    	return false;
	    }

	}
	
	protected void checkOverload(List<Vm> VMs) {
		// This method is used to give a first instance of the overloaded VMs
		int numberOfVMs 	 = VMs.size();
		double dataset[] 	 = new double[numberOfVMs];
		boolean overloadVM[] = new boolean[numberOfVMs];
				
		for (int i=0; i<numberOfVMs; i++) {
			dataset[i] = VmList.getById(VMs, i).totalAssignedTime;
		}
		//Get the basic statistics
        DoubleSummaryStatistics stats = DoubleStream.of(dataset).summaryStatistics();
        //Let's get the total values in our dataset
        //stats.getCount() stats.getSum() stats.getAverage() stats.getMax() stats.getMin()
        // double meanTime = stats.getAverage();
        //
        quantiles    = Quartiles(dataset);
        quantiles[3] = stats.getMax();
        //
        for (int i=0; i<numberOfVMs; i++) {     
			VmList.getById(VMs, i).overloaded = checkOverloadByAssignedTime(dataset[i]);
			//overloadVM[i] 					  = dataset[i] > quantiles[2];
		}
	}
	
	public boolean checkOverloadByAssignedTime(double val) {
		return val > quantiles[2] || val == quantiles[3];
	}
		
	
	public double[] Quartiles(double[] val) {
		// Calulate the 1st, 2nd and 3rt Quartile of a dataset
	    double ans[] = new double[4];

	    for (int quartileType = 1; quartileType < 4; quartileType++) {
	        float length = val.length + 1;
	        double quartile;
	        float newArraySize = (length * ((float) (quartileType) * 25 / 100)) - 1;
	        Arrays.sort(val);
	        if (newArraySize % 1 == 0) {
	            quartile = val[(int) (newArraySize)];
	            } else {
	            int newArraySize1 = (int) (newArraySize);
	            quartile = (val[newArraySize1] + val[newArraySize1 + 1]) / 2;
	             }
	        ans[quartileType - 1] =  quartile;
	    }
	    return ans;
	}
	
	protected void assignRealHostToVm(List<Vm> VMs) {
		// Give a unique host ID to each VM, depending on its datacenter
		
		List<Integer> distH = VM_host_Id.stream().distinct().collect(Collectors.toList());		
		List<Integer> distD = VM_dataCenter_Id.stream().distinct().collect(Collectors.toList());		
		
		int H;	//for host
		int D;	//for datacenter
		Vm vm;
		
		diffHosts = new ArrayList<Integer>(Collections.nCopies(VM_host_Id.size(), -1));
		
		int counter = 0;
		
		for (int h : distH ) {
			for (int d : distD ) {
				// I go through all the positions comparing the HostID and DatacenterID
				boolean flag = false;
				
				for (int i=0; i<VM_host_Id.size();i++) {
					H  = VM_host_Id.get(i);
					D  = VM_dataCenter_Id.get(i);
					
					if (H == h && D == d) {
						vm = VmList.getById(VMs, i);
						vm.setRealHost(counter);
						diffHosts.set(i, counter);
						flag = true;
					}
				}
				if (flag) {
					// I only increase the identifier if the Host-Datacenter pair was found 
					counter++;
				}
			}
		}
		diffRealHost = diffHosts.stream().distinct().collect(Collectors.toList());	
	}
	
	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			// This lines are used to show the responce_time, cost, power_comsuption on the screen
			//System.out.printf("VMID = %d	Experiment = %d	Responce Time = %.3f		Cost = %.3f		Power = %.3f %n", experiment_number, vm.getId(), vm.exTime, vm.costPerStorage, vm.powerCompsuption);
			System.out.printf("FUZZY,%d,%d,%.3f,%.3f,%.3f%n", experiment_number, vm.getId(), vm.exTime, vm.costPerStorage, vm.powerCompsuption);
			//Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		System.out.println("Broker.......");
		//Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

}
