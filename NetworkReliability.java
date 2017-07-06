package Atn;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NetworkReliability {
	
	public static Map<Integer,Integer> indexMap= new HashMap<Integer,Integer>() ;// Hashmap of links present
	public static int counter = 0;
	public static double reliability = 0; //Reliability of the system
	public static double p ;// reliability of each link
    public static boolean[] stateCondition = new boolean[1024]; //condition of all possible 1024 states
    public static double[] statesProbability = new double[1024]; // Reliability of all possible 1024 states
    public static int statesCounter = 0;
	
    /*Function to generate the graph of the network*/
    public static int[][] graphGenerator(int nodes) {
    	
    	int[][] graph = new int[nodes][nodes];
    	for(int i=0; i<nodes; ++i)
    		for(int j=0; j<nodes; ++j){
    			if(i==j)
    				graph[i][j]=0; // to exclude self-loop
    			else
    				graph[i][j]=1;
    		}
    	return graph;
    }
    
    /* Function to calculate the total links present */
    public static int calcEdges(int[][] graph, int nodes){
    	
         int edgeCount =0;
         for(int i=0; i<nodes; ++i)
     		for(int j=0; j<nodes; ++j){
     			if(graph[i][j] != 0)
     				edgeCount++;
     		}
         return edgeCount;
    }
	
    /* Function to generate all possible link */
    public static void createMap(int[][] graph, int nodes){
    	
    	int key = 1;
    	int value =0;
    	for(int i=1; i<nodes+1; ++i)
     		for(int j=i+1; j<nodes+1; ++j){
     			value = (10*i) + j;
     			indexMap.put(key, value);// The possible links are stored in a HaspMap with a key
     			++key;
     		}
    }
	
    
    /* Function to find the path between the source and every other nodes in the network */
    static int[] checkStateCondition(int[][] graph, int nodes ,int src){

        int dist[] = new int[nodes]; // The output array. dist[i] will hold
                                 // the shortest distance from src to i
 
        // sptSet[i] will true if vertex i is included in shortest
        // path tree or shortest distance from src to i is finalized
        Boolean sptSet[] = new Boolean[nodes];
 
        // Initialize all distances as INFINITE and stpSet[] as false
        for (int i = 0; i < nodes; i++)
        {
            dist[i] = 1000;
            sptSet[i] = false;
        }
 
        // Distance of source vertex from itself is always 0
        dist[src] = 0;
 
        // Find shortest path for all vertices
        for (int count = 0; count < nodes-1; count++)
        {
            // Pick the minimum distance vertex from the set of vertices
            // not yet processed. u is always equal to src in first
            // iteration.
            int u = minDistance(dist, sptSet);
 
            // Mark the picked vertex as processed
            sptSet[u] = true;
 
            // Update dist value of the adjacent vertices of the
            // picked vertex.
            for (int v = 0; v < nodes; v++)
 
                // Update dist[v] only if is not in sptSet, there is an
                // edge from u to v, and total weight of path from src to
                // v through u is smaller than current value of dist[v]
                if (!sptSet[v] && graph[u][v]!=0 &&
                        dist[u] != Integer.MAX_VALUE &&
                        dist[u]+graph[u][v] < dist[v])
                    dist[v] = dist[u] + graph[u][v];
        }
        
        return dist;
    }	
   
    public static int minDistance(int dist[], Boolean sptSet[])
    {
        // Initialize min value
        int min = Integer.MAX_VALUE, min_index=-1;
 
        for (int v = 0; v < 5; v++)
            if (sptSet[v] == false && dist[v] <= min)
            {
                min = dist[v];
                min_index = v;
            }
 
        return min_index;
    }
    
    /* Function to determine all possible combination of links failure states given number of links failed */
    static void linkFailureCombination(int arr[], int data[], int start,int end, int index, int r ,int[][] graph , int nodes ,int edgeCount)
    {
         if (index == r) { // r is the number of link failure
        	 ++counter;
        	 int[][] tempGraph = new int[nodes][nodes];// initialize temp graph to original graph matrix
        	 for(int i=0; i<nodes; ++i)
        		 for(int j=0; j<nodes; ++j)
        			 tempGraph[i][j] = graph[i][j];
        	 
             for (int j=0; j<r; j++){
            	int value = indexMap.get(data[j]);
            	int index_j = (value % 10);
            	int index_i = (value-index_j)/10;
             
                tempGraph[index_i-1][index_j-1] = 0; // setting the link to fail
                tempGraph[index_j-1][index_i-1] = 0; // setting the link to fail
                
            }
             int[][] matrix = new int[nodes][nodes];
             /*Finding the distance between every node to every other node*/
             for(int src =0; src<nodes; ++src){
                matrix[src] = checkStateCondition(tempGraph, nodes, src);
             }
             
             boolean state = true;// set the state condition initally up
             
             for(int i=0; i<nodes; ++i){
          		for(int j=0; j<nodes; ++j){   
            	  if(i==j){
            		  continue;
            	  }
            	  else if(matrix[i][j] == 0 || matrix[i][j] >= 1000 ) //if there is no path between any two nodes
            		  state = false;// set the state condition to down
          		}
             }
             stateCondition[statesCounter] = state;
             statesProbability[statesCounter++] = (Math.pow((1-p),r)*Math.pow(p,(edgeCount-r)));
            
             
             if(state){ // if the state condition is up calculate reliability for the state
                	reliability += (Math.pow((1-p),r)*Math.pow(p,(edgeCount-r)));
                }
             return;
          }
          
          /*possible combination of r down links*/
          for (int i=start; i<=end && end-i+1 >= r-index; i++)
          {
              data[index] = arr[i];
              linkFailureCombination(arr, data, i+1, end, index+1, r, graph,nodes ,edgeCount);
          }
     }
    
    
    static void linkCombination(int arr[], int n, int r, int[][] graph ,int nodes ,int edgeCount)
    {
        int data[]=new int[r];
        linkFailureCombination(arr, data, 0, n-1, 0, r ,graph ,nodes ,edgeCount);
    }
    
    public static void main(String[] args) {

		int nodes = 5; // number of nodes
		int edgeCount ; // number of links 
		p = 0.05;  // initailaizing probability
		DecimalFormat f = new DecimalFormat("#.##"); // Reliability output formatter
		
		while(p<=1){ // for p from 0.005 t0 1
			
		int[][] graph = new int[nodes][nodes];
		graph = graphGenerator(nodes); // function call to generate network graph
		edgeCount = calcEdges(graph,nodes) / 2; // function call to generate the links present
		createMap(graph,nodes); // generate hashmap of possible links
		int[] indexArray = new int[edgeCount];
		for(int i=0; i<edgeCount; ++i){
			indexArray[i] = i+1;
		}
		/*number of link failure varied from 0 to 10*/
		for(int i=0; i<=edgeCount; ++i){
			linkCombination(indexArray,edgeCount, i,graph ,nodes, edgeCount);
		}
		
		System.out.println("p : " + f.format(p) + "  Reliability : " + reliability);
		
		p += 0.05;
		reliability = 0; 
		statesCounter = 0;
		}
		
		/* Flipping K system condition*/
		
		p = 0.85; 
		int k=0;
		
		ArrayList<Integer> stateNum = new ArrayList();
		/*Array containg state numbers for random selection*/
		for(int i=0; i<1024; ++i){
		   stateNum.add(i);
		}
		
		double[] BefReliability = new double[21]; //Reliability of the system before flipping k states
		double[] AfterReliability = new double[21];//Reliability of the system after flipping k states
		
		int counter = 0;
		
		/* Taking Average of 1024 trials for each value of k to reduce the effect of randomness*/
	    
	    while(counter < 1024){
	    k=0;
		while(k<=20)
		{
			int[][] graph = new int[nodes][nodes];
			graph = graphGenerator(nodes);
			edgeCount = calcEdges(graph,nodes) / 2;
			createMap(graph,nodes);
			int[] indexArray = new int[edgeCount];
			for(int i=0; i<edgeCount; ++i){
				indexArray[i] = i+1;
			}	
			
			/*number of link failure varied from 0 to 10*/
			for(int i=0; i<=edgeCount; ++i){
				linkCombination(indexArray,edgeCount, i,graph ,nodes, edgeCount);
			}
			
			
			Collections.shuffle(stateNum);// randomizing the states
			
			int[] kArray = new int[k];
			/*selecting the first k states of from random states list for flipping their condition*/
			for(int i=0; i<k;++i){
				kArray[i] = stateNum.get(i);
			}
	        
			/*Calculating reliability of the system before flipping k states*/
			double sum=0;
		    reliability = 0;
			for(int i=0; i<1024;++i){
				sum+=statesProbability[i];
				if(stateCondition[i]) // if system condition is up calculate reliability
					reliability += statesProbability[i];
			}	
		    
		    BefReliability[k] += reliability;
			
		    /*Fliping the condition of k states*/
		    for(int i=0; i<k; ++i){
		    	
		    	boolean state = stateCondition[kArray[i]];
		    	stateCondition[kArray[i]] = (!state) ; // change the state condition
		    }
		    
			reliability = 0;
			sum=0;
			
			/*Calculating reliability of the system before flipping k states*/
			for(int i=0; i<1024;++i){
				sum+=statesProbability[i];
				if(stateCondition[i]) // if the system condition is up calculate reliability
					reliability += statesProbability[i];
			}	
			AfterReliability[k] += reliability;
			reliability = 0; 
			statesCounter = 0;
			++k; // k is varied from 0 to 20
		}
	        counter++;// update counter
	    }	
		
	   k=0;
	   while(k<=20){
		   System.out.print("\n Before Applying K-Reliability = " + BefReliability[k]/1024);
		   System.out.print("     After Applying K-Reliability = " + (AfterReliability[k]/1024));
		   System.out.print("     Change in Reliability = " + ((BefReliability[k] - AfterReliability[k])/1024));
	       k++;
	   }
    
    }
}