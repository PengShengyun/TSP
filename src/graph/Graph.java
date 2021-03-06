package graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Yu-Ho Hsieh
 * Georgia Institute of Technology, Fall 2018
 *
 * CSE6140 Project, Travel Salesman Problem
 * Graph class for computing TSP
 *
 */

public class Graph {
    // For input
    private int size;
    private int[][] matrix;
    private PriorityQueue<Edge> edgeList;

    // For exact result
    private int currentBestCost;
    private List<Integer> currentBestRoutes; // Route <0,1,5,2,7,6,4,3,0>, please go BACK to the origin point

    // For approximation result
    private List<Double> timeStamps;
    private List<Integer> approxCosts;

    public Graph(int size, int[][] matrix, PriorityQueue<Edge> edgeList){
        this.size = size;
        this.matrix = matrix;
        this.edgeList = edgeList;
        this.timeStamps = new ArrayList<>();
        this.approxCosts = new ArrayList<>();
    }

    // Example <0,1,5,2,7,6,4,3,0>
    // Please go Back to the origin point
    public void setCurrentBestResult(int cost, List<Integer> routes){
        this.currentBestCost = cost;
        this.currentBestRoutes = routes;
    }

    public int getSize() {
    	return this.size;
    }
    
    public int[][] getMatrix(){
        return this.matrix;
    }
    
    public PriorityQueue<Edge> getEdgeList(){
    	return this.edgeList;
    }
    
    public void printEdges() {
    	PriorityQueue<Edge> copy = new PriorityQueue<Edge>();
    	copy.addAll(edgeList);
    	Edge cur = copy.poll();
    	while (cur != null) {
    		System.out.println(cur.toString());
    		cur = copy.poll();
    	}
    	copy.clear();
    }
    
    public void addApproxResult(int approxCost, double timeStamp){
        this.approxCosts.add(approxCost);
        this.timeStamps.add(timeStamp);
    }

    public int getCurrentBestCost(){
        return this.currentBestCost;
    }

    public List<Integer> getCurrentBestRoutes(){
        return this.currentBestRoutes;
    }

    public List<Integer> getApproxCosts(){
        return this.approxCosts;
    }

    public List<Double> getTimeStamps(){
        return this.timeStamps;
    }


}
