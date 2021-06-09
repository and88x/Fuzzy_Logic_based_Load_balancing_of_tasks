package updated_HBLoadbalancer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Create_Datacenter  {
	
	public List<Datacenter> createDatacenter(int number ){
		
		LinkedList<Datacenter> list = new LinkedList<Datacenter>();

		for(int i=0;i<number;i++){
			list.add(cDatacenter("Datacenter"+(i)));
		}
		Log.printLine("successful creation of  "+ number +" Datacenter");
		return list;
	}
	
	
	private static Datacenter cDatacenter(String name){
		
		Random r            = new Random();
		int NOofhost        = r.nextInt(4) + 2;
		List<Host> hostList = new ArrayList<Host>();
		List<Pe> peList1    = new ArrayList<Pe>();
		int mips 			= 10000;
		
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		int hostId   = 0;
        int ram      = 4096;   //host memory (MB)
        long storage = 100000; //host storage
        int bw 		 = 80000;	
	
        for(int i=0;i<NOofhost;i++) {
        	hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)	// allocates one or more Pe to a VM, and allows sharing of PEs by multiple VMs
    			)
    		); // This is our first machine
        	hostId++;
        }
        
        String arch       = "x86";      	// system architecture
		String os         = "Linux";        // operating system
		String vmm        = "Xen";
		double time_zone  = 10.0;           // time zone this resource located
		double cost       = 3.0;            // the cost of using processing in this resource
		double costPerMem = 0.05;		    // the cost of using memory in this resource
		
		double costPerStorage = 0.1;	    // the cost of using storage in this resource
		double costPerBw      = 0.1;		// the cost of using bw in this resource
		
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			//datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyHoneyBee(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	} 
}
