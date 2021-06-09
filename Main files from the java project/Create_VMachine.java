package updated_HBLoadbalancer;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;;

public class Create_VMachine {
	//								50				12
	public List<Vm> createVms(int requestVms, int brokerId){
		
		LinkedList<Vm> list = new LinkedList<Vm>();

		long size = 1000; //image size (MB) (amount of storage)
		
		int[] ram        = {256, 512, 1024, 2048};
		int[] mips       = {500, 1000, 1500, 2000};
		long [] bw  	 = {500, 1000};
		int [] pesNumber = {1,2,3,4}; // number of cpus
		String vmm 		 = "Xen";     // VMM name (virtual machine monitor)
		Random r 		 = new Random();
		Vm[] vm 		 = new Vm[requestVms];

		for(int i=0;i<requestVms;i++){
			//vm[i] = new Vm(i, brokerId, mips[r.nextInt((4))],pesNumber[r.nextInt((4))], ram[r.nextInt((4))], bw[r.nextInt((2))], size, vmm, new CloudletSchedulerTimeShared());
			vm[i] = new Vm(i, brokerId, mips[i%4],pesNumber[i%4], ram[i%4], bw[i%2], size, vmm, new CloudletSchedulerTimeShared());
			
			list.add(vm[i]);	
		}
		Log.printLine("successful creation of  "+  requestVms + " VMs");
		return list;
	}
}
