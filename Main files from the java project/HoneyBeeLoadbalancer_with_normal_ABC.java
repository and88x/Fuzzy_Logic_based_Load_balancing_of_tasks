package updated_HBLoadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.*;  

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.lang.Math;
import java.util.List;
import java.util.stream.DoubleStream;


public class HoneyBeeLoadbalancer extends DatacenterBroker {
    double PTavg              = 0.0;
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
        length       = l;
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
    
        for (Cloudlet cloudlet : getCloudletList())     {       
            AvailVMlist = getAvailVM(cloudlet, vlist);      // only in the creation of the cloudlet
            
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
    
    
//      for (Cloudlet cloudlet : getCloudletList())     {       
//          AvailVMlist = getAvailVM(cloudlet, vlist);      // food sources
//          
//          if(tempList.size()== 0) {   
//              for (int i=0;i<AvailVMlist.size();i++) {
//                  tempList.add(AvailVMlist.get(i)) ;
//              }
//          }
//          Vm smallestVm = tempList.get(0); // initial condition
//          
//          for(Vm checkVm: tempList){
//              time4smallerVM = getExecTime(cloudlet,smallestVm); // fitness
//              time4checkVM   = getExecTime(cloudlet,checkVm);
//              
//              if( time4smallerVM >= time4checkVM )
//              {
//                  smallestVm  = checkVm;
//                  vmmin       = checkVm.getId();
//                  asignedTime = time4checkVM;
//              }
//          }
//          // Registry the cloudlet and its respective time to finish in the respective VM
//          smallestVm.addTask(cloudlet.getCloudletId(), asignedTime);
//          //
//          bindCloudletToVm(cloudlet.getCloudletId(),vmmin);
//          tempList.remove(smallestVm);
//      }
        
//      // Phase 1: Input parameters
//      int FoodSource = requestTasks;
//      int D = 1;
//      int lb = 0;
//      int ub = 49;
//      int max_iter = 10;
//      int N = 5;
//      int limit = N/5;
////        ArrayList<Double> sum_times = new ArrayList<Double>(Collections.nCopies(ub+1, 0.0));
//      int rand_number;
//      
//      
//      int[] Gbest = new int[FoodSource];
//      int best_particle;
//      double fBest = 100000000.0, bestFitness = 0.0;
//      
//      
//      int [][] swarm = new int[cList.size()][N];
//      double [][] rTime = new double[cList.size()][N];
//      double [] fx = new double[N];
//      double [] fit = new double[N];
//      double [] prob = new double[N];
//      int [] trial = {0,0,0,0,0};
//      DoubleSummaryStatistics stats;
//      
//      double [][] sum_times = new double[N][ub+1];
//      
//      long seed = Long.parseLong("25");
//      Random random = new Random(seed);
//      
//      for (int j = 0; j<N; j++) {
//          for (int i = 0; i<cList.size();i++) {
//              // Phase 3: Generate initial population randomly
////                swarm[i][j] = (int) Math.floor(number_of_VMs*Math.random());
//              swarm[i][j] = random.nextInt(number_of_VMs);
//              rTime[i][j] = getExecTime(cList.get(i) , vlist.get(swarm[i][j]));
//              sum_times[j][swarm[i][j]] += rTime[i][j];
//          }
//          stats = DoubleStream.of(sum_times[j]).summaryStatistics();
//          fx[j] = stats.getMax()+stats.getAverage();
//          fit[j] = fitness(fx[j]);            
////            System.out.println("Max = "+stats.getMax());
//      }
//      int partner, rCloudlet;
//      double aux_rTime, aux_fitness;
//      int [][] aux_swarm = swarm.clone();
//      double aux_fx = 0.0;
//
//  for (int iter=0;iter<max_iter;iter++) {
//      double sum;
//      // Phase 4: Employer bee phase
//      for (int i = 0; i<N; i++) {
////            System.out.println("---------------------------------");
//          partner = select_partner(i, N, random);
////            rCloudlet = (int)Math.floor(FoodSource*Math.random());
//          rCloudlet = random.nextInt(FoodSource);
////            System.out.println("Prev rTime="+rTime[rCloudlet][i]);
////            System.out.println("Prev sum_time="+sum_times[i][swarm[rCloudlet][i]]);
//          stats = DoubleStream.of(sum_times[i]).summaryStatistics();
////            System.out.println("statistics = "+stats.getMax()+" and "+stats.getAverage());
//          sum_times[i][swarm[rCloudlet][i]] -= rTime[rCloudlet][i];
////            System.out.println("new sum_time="+sum_times[i][swarm[rCloudlet][i]]);
//          
//          aux_rTime = getExecTime(cList.get(rCloudlet) , vlist.get(swarm[rCloudlet][partner]));
////            System.out.println("new rTime="+aux_rTime);
////            System.out.println("Prev + sum_time="+sum_times[i][swarm[rCloudlet][partner]]);
//          sum_times[i][swarm[rCloudlet][partner]] += aux_rTime;
////            System.out.println("new + sum_time="+sum_times[i][swarm[rCloudlet][partner]]);
//          
////            swarm[rCloudlet][i] = swarm[rCloudlet][partner];
////            rTime[rCloudlet][i] = getExecTime(cList.get(rCloudlet) , vlist.get(swarm[rCloudlet][i]));
////            sum_times[i][swarm[rCloudlet][i]] += rTime[rCloudlet][i]; 
//          
//          stats = DoubleStream.of(sum_times[i]).summaryStatistics();
////            System.out.println("statistics = "+stats.getMax()+" and "+stats.getAverage());
////            fx[i] = stats.getMax()+stats.getAverage();
////            fit[i] = fitness(fx[i]);
//          
////            System.out.println("Prev fitness="+fit[i]);
//          aux_fx = stats.getMax()+stats.getAverage();
//          aux_fitness = fitness(aux_fx);
////            System.out.println("new  fitness="+aux_fitness);
//          
//          if (aux_fitness > fit[i]) {
////                System.out.println("Change");
//              aux_swarm[rCloudlet][i] = swarm[rCloudlet][partner];
//              rTime[rCloudlet][i] = aux_rTime;
//              fx[i] = aux_fx;
//              fit[i] = aux_fitness; 
//          } else {
//              trial[i] += 1;
//              sum_times[i][swarm[rCloudlet][i]] += rTime[rCloudlet][i];
//              sum_times[i][swarm[rCloudlet][partner]] -= aux_rTime; 
//          }
//      }
//      
//      // Phase 5: Onlooker bee phase
//      stats = DoubleStream.of(fit).summaryStatistics();
//      sum = stats.getAverage();
//      prob = DoubleStream.of(fit).map(p->p/sum).toArray();
//      
//  int tries = 0, cont = 0;
////    while (tries < N || cont < 100) {
//      cont++;
//      for (int i=0; i<N;i++) {
//          double r = random.nextInt(1000000)/1000000.0;
//          if (r<prob[i]) {
////                System.out.println("try to change "+i+"---------------------------------");
//              partner = select_partner(i, N, random);
////                rCloudlet = (int)Math.floor(FoodSource*Math.random());
//              rCloudlet = random.nextInt(FoodSource);
////                System.out.println("Prev rTime="+rTime[rCloudlet][i]);
////                    System.out.println("Prev sum_time="+sum_times[i][swarm[rCloudlet][i]]);
//              stats = DoubleStream.of(sum_times[i]).summaryStatistics();
////                System.out.println("statistics = "+stats.getMax()+" and "+stats.getAverage());
//              sum_times[i][swarm[rCloudlet][i]] -= rTime[rCloudlet][i];
////                    System.out.println("new sum_time="+sum_times[i][swarm[rCloudlet][i]]);
//              
//              aux_rTime = getExecTime(cList.get(rCloudlet) , vlist.get(swarm[rCloudlet][partner]));
////                System.out.println("new rTime="+aux_rTime);
////                    System.out.println("Prev + sum_time="+sum_times[i][swarm[rCloudlet][partner]]);
//              sum_times[i][swarm[rCloudlet][partner]] += aux_rTime;
////                    System.out.println("new + sum_time="+sum_times[i][swarm[rCloudlet][partner]]);
//              
////                    swarm[rCloudlet][i] = swarm[rCloudlet][partner];
////                    rTime[rCloudlet][i] = getExecTime(cList.get(rCloudlet) , vlist.get(swarm[rCloudlet][i]));
////                    sum_times[i][swarm[rCloudlet][i]] += rTime[rCloudlet][i]; 
//              
//              stats = DoubleStream.of(sum_times[i]).summaryStatistics();
////                System.out.println("statistics = "+stats.getMax()+" and "+stats.getAverage());
////                    fx[i] = stats.getMax()+stats.getAverage();
////                    fit[i] = fitness(fx[i]);
//              
////                    System.out.println("Prev fitness="+fit[i]);
//              aux_fx = stats.getMax()+stats.getAverage();
//              aux_fitness = fitness(aux_fx);
////                    System.out.println("new  fitness="+aux_fitness);
//              
//              if (aux_fitness > fit[i]) {
//                  tries++;
////                    System.out.println("Change");
//                  aux_swarm[rCloudlet][i] = swarm[rCloudlet][partner];
//                  rTime[rCloudlet][i] = aux_rTime;
//                  fx[i] = aux_fx;
//                  fit[i] = aux_fitness; 
//              } else {
//                  trial[i] += 1;
//                  sum_times[i][swarm[rCloudlet][i]] += rTime[rCloudlet][i];
//                  sum_times[i][swarm[rCloudlet][partner]] -= aux_rTime; 
//              }
//          }
//      }
////    }
//      // Phase 6: Memorize the best answer
//      best_particle = getMin(fx);
//      if (fBest > fx[best_particle]) {
//          for(int i=0;i<cList.size();i++) {
//              Gbest[i] = swarm[i][best_particle];
//          }
//          fBest = fx[best_particle];
//          bestFitness = fit[best_particle];
//      }
//
//      
//      // Phase 7: Scout phase
//      for (int j=0; j<N; j++) {
//          if (limit<trial[j] && j != best_particle) {
//              for (int i = 0; i<cList.size();i++) {
//                  swarm[i][j] = random.nextInt(number_of_VMs);
//                  rTime[i][j] = getExecTime(cList.get(i) , vlist.get(swarm[i][j]));
//                  sum_times[j][swarm[i][j]] += rTime[i][j];
//              }
//              stats = DoubleStream.of(sum_times[j]).summaryStatistics();
//              fx[j] = stats.getMax()+stats.getAverage();
//              fit[j] = fitness(fx[j]);    
//          }
//      }
//      
//      System.out.println("Iteration "+iter+ " with fitness = "+fBest);
//  
//  }
//      System.out.println("Finished ABC algorithm");
//      int cont = 0;
//      Vm smallestVm;
//      List<Cloudlet> cloudletList = getCloudletList();
//      
//      if (cloudletList.size()>100) {
//          System.out.println("Finished ABC algorithm");           
//      }
//      
//      for (int i=0; i<cloudletList.size();i++)        {  
//          Cloudlet cloudlet = cloudletList.get(i);
//          smallestVm = vlist.get(Gbest[i]);
//          asignedTime = getExecTime(cloudlet,smallestVm);
//          // Registry the cloudlet and its respective time to finish in the respective VM
//          smallestVm.addTask(cloudlet.getCloudletId(), asignedTime);
//          //
//          bindCloudletToVm(cloudlet.getCloudletId(),smallestVm.getId());
//          tempList.remove(smallestVm);
//          cont++;
//      }

    }   
    
    
    
    public static int getMin(double[] inputArray){ 
        double minValue = inputArray[0]; 
        int index = 0;
        for(int i=1;i<inputArray.length;i++){ 
          if(inputArray[i] < minValue){ 
            minValue = inputArray[i]; 
            index = i;
          } 
        } 
        return index; 
    }
   
    private int select_partner(int current, int limit, Random random) {
        int rand_number;
        while (true){
//          rand_number = (int)Math.floor(limit*Math.random());
            rand_number = random.nextInt(limit);
            if (rand_number != current) {
                break;
            }
        }
        return rand_number;
    }
    private int new_candidate(int X, int Xn) {
        return (int)(X + (2*Math.random()-1)*(X - Xn));
    }
    
    // Phase 2: Defining the objective function & Fitness function
    private double fitness(double fx) {
        // Maximize
        if (fx >= 0) {
            return 1/(1+fx);
        } 
        return 1 + Math.abs(fx);
    }
    
    private ArrayList<Vm>  getAvailVM(Cloudlet cloudlet, ArrayList<Vm> vlist) {
        /* Gets the available VMs in all datacenters*/
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
