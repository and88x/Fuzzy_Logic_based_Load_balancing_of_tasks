
package updated_HBLoadbalancer;
import org.cloudbus.cloudsim.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
public class VmAllocationPolicyHoneyBee extends VmAllocationPolicy{
	
	
private Map<String, Host> vmTable;
private Map<String, Integer> usedPes;
private List<Double> freePes;
private double PThostTotal=0.0;
private double requestused =0.0;
private  double PThostAvg=0.0;
private  int Totalnumber;

	public VmAllocationPolicyHoneyBee(List<? extends Host> list) {
		super(list);
			
		setFreePes(new ArrayList<Double>());
		for (Host host : getHostList()) {
			requestused = (host.getTotalMips()- host.getAvailableMips())/(host.getNumberOfPes()*host.getTotalMips());
			PThost().add(requestused);
			PThostTotal += requestused;
		}
		Totalnumber= getHostList().size();
		PThostAvg =PThostTotal/Totalnumber;	 
		setVmTable(new HashMap<String, Host>());
		setUsedPes(new HashMap<String, Integer>());
	}
	
	public  boolean allocateHostForVm(Vm vm) {
		 
		int requiredPes = vm.getNumberOfPes();
		boolean result  = false;
		int tries       = 0;
		
		List<Double> freePesTmp = new ArrayList<Double>();
		
		for (Double freePes : PThost()) {
			freePesTmp.add(freePes);	
		}
	
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				Double moreFree = Double.MAX_VALUE;
				int idx 		= -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) < moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}
				
				Host host = getHostList().get(idx);
				result    = host.vmCreate(vm);
				
			//	Log.formatLine(
						//"%.2f: VM #" + vm.getId() + "  has been allocated by Honeybee_loadbalancer to the host #" + host.getId(),
						//CloudSim.clock());
				if (result) {//check creation of VMs
					getVmTable().put(vm.getUid(), host);
					
					getUsedPes().put(vm.getUid(), requiredPes);
					PThost().set(idx, PThost().get(idx) - requiredPes);
					result = true;
					break;
				} else {
					freePesTmp.set(idx, Double.MAX_VALUE);
				}
				tries++;
			} while (!result && tries < PThost().size());
		}
		return result;
	}

	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx   = getHostList().indexOf(host);
		int pes   = getUsedPes().remove(vm.getUid());
		
		if (host != null) {
			host.vmDestroy(vm);
			PThost().set(idx, PThost().get(idx) + pes);
		}
	}

	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	
	protected List<Double> PThost() {
		return freePes;
	}

	
	protected void setFreePes(List<Double> freePes) {
		this.freePes = freePes;
	}

	
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public boolean allocateHostForVm(Vm vm, Host host) {
		
		Log.formatLine( " VmAllocationPolicyHoneyBee stated ....................created." );
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);

			int requiredPes = vm.getNumberOfPes();
			int idx 		= getHostList().indexOf(host);
			
			getUsedPes().put(vm.getUid(), requiredPes);
			PThost().set(idx, PThost().get(idx) - requiredPes);

			//Log.formatLine(
					//"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					//CloudSim.clock());
			return true;
		}

		return false;
	}
}
