package updated_HBLoadbalancer;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;


public class HoneyBeeLoadbalancer extends DatacenterBroker {
	double PTavg			  = 0.0;
	double PTTotal            = 0.0;
	double Standard_deviation = 0.0;
	double threshold          = 0.1;
	
	public HoneyBeeLoadbalancer(String name) throws Exception {
		super(name);	
	}
	
	public HoneyBeeLoadbalancer(String name, int requestT, Long l) throws Exception {
		super(name);	
		// This variables are neccesary to normalize the fuzzy inputs  
		requestTasks = requestT;
		length 		 = l;
	}
	
	public void HoneyBeeVmschedule(){
		double time4smallerVM, time4checkVM, asignedTime=0;
		
		ArrayList<Vm> AvailVMlist = new ArrayList<Vm>();
		ArrayList<Vm> vlist       = new ArrayList<Vm>();
		ArrayList<Vm> tempList    = new ArrayList<Vm>();
		
		int vmmin  = -1;
		double min = Double.MAX_VALUE;
		
		for (Vm vm : getVmList()) {
    		vlist.add(vm);
		}
	
		for (Cloudlet cloudlet : getCloudletList())		{       
			AvailVMlist = getAvailVM(cloudlet, vlist);		// only in the creation of the cloudlet
			
			if(tempList.size()== 0) {
	     		for (int i=0;i<AvailVMlist.size();i++) {
	     			tempList.add(AvailVMlist.get(i)) ;
	     		}
			}
			Vm smallestVm = tempList.get(0);
			
			for(Vm checkVm: tempList){
				time4smallerVM = getExecTime(cloudlet,smallestVm);
				time4checkVM   = getExecTime(cloudlet,checkVm);
				
				if( time4smallerVM >= time4checkVM )
				{
					smallestVm  = checkVm;
					vmmin       = checkVm.getId();
					asignedTime = time4checkVM;
				}
			}
			// Registry the cloudlet and its respective time to finish in the respective VM
			smallestVm.addTask(cloudlet.getCloudletId(), asignedTime);
			//
			bindCloudletToVm(cloudlet.getCloudletId(),vmmin);
			tempList.remove(smallestVm);
		}
	}	
	
	
	private ArrayList<Vm>  getAvailVM(Cloudlet cloudlet, ArrayList<Vm> vlist) {
		
		ArrayList<Vm> availlist = new ArrayList<Vm>();
		double PTVm[]           = new double[vlist.size()];
		
		PTTotal            = 0.0;
        double time        = 0.0;
        Standard_deviation = 0.0;
        int l = 0;
        
		for(int j=0; j<vlist.size(); j++){
			time     = getExecTime(cloudlet , vlist.get(j));
			PTTotal += time;
			PTVm[j]  = time;
		}
		Standard_deviation=getstandarddevaition(PTTotal,PTVm, vlist);
        l++;
        for(int i=0; i<vlist.size(); i++){
        	  if (( (PTVm[i] - PTavg)/Standard_deviation)<threshold)
        		  availlist.add(vlist.get(i));
		}
		return  availlist;
	}

	private double getstandarddevaition(double PTTotal, double [] PTVm, ArrayList<Vm> vlist) {

		PTavg         = PTTotal/vlist.size();
		double sqDiff = 0.0;
		
		for(int i=0;i<vlist.size();i++) {
			sqDiff+=( PTVm[i] - PTavg ) * ( PTVm[i]  - PTavg);
		}
		double variance=  sqDiff/vlist.size();
		double Standard_deviation = Math.sqrt(variance);
		return Standard_deviation;
	}
	
	
	private double getExecTime(Cloudlet cloudlet, Vm vm){
		return cloudlet.getCloudletLength() / (vm.getMips()*vm.getNumberOfPes());
	}
}
