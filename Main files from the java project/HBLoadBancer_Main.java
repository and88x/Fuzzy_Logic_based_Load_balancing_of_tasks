package updated_HBLoadbalancer;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;

public class HBLoadBancer_Main {

	private static int reqdatacener = 10;
	private static int requestVms   = 50;
	static double  avgtime=0.0;
	//
	public static void main(String[] args) {
		 String data = "";
		 data += ("========== OUTPUT =========="+" \n");
                 
		 long num              = 2000;
		 DecimalFormat dp      = new DecimalFormat("###.##");
		 int experiment_number = 0;
    
		 try {
            FileWriter results = new FileWriter("results.txt");

            for ( long j=2000;j <= 20000;j=j+2000) {
            	 data += ("Task \t Instruction length \tAverage Time \n"); 
            	 
            	 for ( int i=100; i <= 1000; i=i+100) { // i => ntasks, j => task_length						
					avgtime = new Honeybee().simulateHoneybee(reqdatacener, i, requestVms, j, experiment_number);
            		data   += (i + "     \t" + j +  "\t\t\t\t" + dp.format(avgtime)+" \n");
            		experiment_number++;
            	 }
            	 data += ("\n\n\n");
            }
            results.write(data);
            results.close();
           
          } catch (IOException e) {
        	  e.printStackTrace();
          }
	}

}
