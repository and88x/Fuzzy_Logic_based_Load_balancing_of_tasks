package updated_HBLoadbalancer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class Create_Cloudlets {
	//													  i+2000		  j+100
	public List<Cloudlet> createCloudlet(int userId, int cloudlets, long length){
		
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		
		//long length = 2000;
		long fileSize   = 300;
		long outputSize = 300;
		int pesNumber   = 1;

		// UtilizationModelFull() is a simple model, according to which a Cloudlet 
		// always utilize all the available CPU capacity
		UtilizationModel utilizationModel = new UtilizationModelFull(); 
		Cloudlet[] cloudlet 			  = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}
		Log.printLine("successful creation of  "+ cloudlets + " cloudlets");
		return list;
	}
}
