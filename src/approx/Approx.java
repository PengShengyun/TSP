package approx;

import graph.Graph;
import graph.AdjacencyList;
import graph.Edge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;

/**
 * @author Courtney
 * Georgia Institute of Technology, Fall 2018
 *
 * CSE6140 Project, Travel Salesman Problem
 * Approximated computation using MST approx
 *
 */

public class Approx {
	
	public static final int TOL = -1; // e.g. 100000
		// Number of iterations before checking cost and terminating if no improvement made since last check (good for debugging)
		// Set to -1 to run for the full 10 minutes without improvement checks
	
    public static Graph compute(Graph g, int cutTime, int seed) {
    	double t0 = System.nanoTime();
    	g.setCurrentBestResult(Integer.MAX_VALUE, null);
    	Random rand = new Random(seed);
    	int n = g.getSize();
    	
    	// Create Minimum Spanning Tree
    	AdjacencyList<Integer> mst = Approx.computeMST(g, rand);
    	
    	// Obtain odd-degree nodes
    	List<Integer> odds =  findOdds(n, mst);   	
    	
    	AdjacencyList<Integer> mstMatched;
    	int cost;
    	List<Integer> route;
    	double runtime;
    	int counter = 1; // check to see if improvement has been made in the past TOL iterations
    	int costComp = Integer.MAX_VALUE;
    	while((System.nanoTime()-t0)/1000000000 <= cutTime) { // in seconds
    		
    		// Obtain minimum weight perfect matching
    		mstMatched = minWeightPerfectMatching(g, rand, mst, odds);
    		
    		// Obtain Eulerian traversal route
    		route = eulerTraversal(n, rand, mstMatched);
        	
        	// Calculate cost of the route
        	cost = 0;
        	for (int ii = 1; ii < route.size(); ii++) {
        		cost+= g.getMatrix()[route.get(ii-1)][route.get(ii)];
        	}
        	
        	// Record
        	runtime = (System.nanoTime()-t0)/ 1000000000;
        	if (cost < g.getCurrentBestCost() && runtime <= cutTime) {
        		g.addApproxResult(cost, runtime);
        		g.setCurrentBestResult(cost, route);
        	}
        	
        	// Shuffle MST adjacency list order for new randomized DFS
        	mst.shuffleNeighbors(rand);
        	
        	// Check if improvements have been made every TOL iterations
        	if (counter % TOL == 0 && TOL != -1) {
        		if (g.getCurrentBestCost() == costComp) { // no improvements made
        			break; // stop searching for better cost
        		} else {
        			costComp = g.getCurrentBestCost(); // update
        			counter = 1; // reset the ten-counter
        		}
        	} else {
        		counter++; // increment counter
        	}
    	}
    	
    	return g;
    }
    
    /**
     * Computes the minimum spanning tree of a given graph g using Kruskal's algorithm and randomizes neighbor order of each node in order to randomize DFS
     * @param g
     * @param rand random stream generated by a seed
     * @return minimum spanning tree adjacency list
     */
    private static AdjacencyList<Integer> computeMST(Graph g, Random rand) {
    	PriorityQueue<Edge> edgeList = g.getEdgeList();
		AdjacencyList<Integer> mst = new AdjacencyList<Integer>(g.getSize());
		UnionFind uf = new UnionFind(g.getSize()); // Initialize n subsets containing singletons
		Edge cur;
		int x, y;
		
		while (mst.numEdges() < g.getSize()-1) { // number of edges in tree = # nodes - 1
			cur = edgeList.poll();
			x = uf.find(cur.getNode1());
			y = uf.find(cur.getNode2());
			if (x != y) {
				mst.addEdge(cur.getNode1(), cur.getNode2(), rand, cur.getCost());
				uf.union(x, y);
			}
		}
		
		return mst;
    }
    
    /**
     * Find the odd-degree nodes in a minimum spanning tree
     * @param n number of nodes in mst (g.getSize())
     * @param mst minimum spanning tree adjacency list of g
     * @return list of nodes that have an odd degree
     */
    private static List<Integer> findOdds(int n, AdjacencyList<Integer> mst) {
        List<Integer> odds = new ArrayList<>(n); // cap is n e.g. an odd-star graph
        for (int ii = 0; ii < n; ii++) {
        	if (mst.getEdgeList(ii).size() % 2 == 1) {
        		odds.add(ii);
        	}
        }
//        System.out.println(odds.toString());
        
        return odds;
    }
    
    private static AdjacencyList<Integer> minWeightPerfectMatching(Graph g, Random rand, AdjacencyList<Integer> mst, List<Integer> odds) {
    	// Obtain minimum-weight perfect matching of odd-deg nodes using a Greedy approximation
        int cur; // current node we are Greedily matching
        int min, min_ind; // distance to and index of closest neighbor of current node
        List<Integer> oddsCopy = new ArrayList<Integer>();
        oddsCopy.addAll(odds);
        AdjacencyList<Integer> mstMatched = new AdjacencyList<Integer>(mst.numNodes());
        mstMatched.addAll(mst);
        while (!oddsCopy.isEmpty()) { // need to perfectly pair together all odd-degree nodes
        	min = Integer.MAX_VALUE;
        	min_ind = -1;
        	cur = oddsCopy.get(rand.nextInt(oddsCopy.size())); // randomly choose a node from the remaining odds list to begin at
        	oddsCopy.remove(Integer.valueOf(cur)); // remove the object cur, not the element at index cur
        	for (int ii : oddsCopy) {
        		if (g.getMatrix()[cur][ii] < min) {
        			min = g.getMatrix()[cur][ii];
        			min_ind = ii;
        		}
        	}
        	
        	if (!mstMatched.isConnected(cur, min_ind)) {
        		mstMatched.addEdge(cur, min_ind, rand, g.getMatrix()[cur][min_ind]); // add edge between cur and its nearest neighbor if not already connected
        	}
        	oddsCopy.remove(Integer.valueOf(min_ind)); // remove this neighbor from consideration
        }
        
        return mstMatched;
    }
    
    /**
     * Obtains the Eulerian traversal route list of a given minimum spanning tree using DFS
     * @param n number of nodes in mst (g.getSize())
     * @param rand random stream generated by a seed
     * @param mst minimum spanning tree adjacency list of g
     * @return an Euler traversal route list
     */
    private static List<Integer> eulerTraversal(int n, Random rand, AdjacencyList<Integer> mst) {
        // Randomized Depth First Search Eulerian traversal
    	Stack<Integer> lifo = new Stack<>();
    	int[] visited = new int[n]; // initialize all 0
    	
    	int cur = rand.nextInt(n); // start at a random node
    	lifo.push(cur);
    	LinkedList<Integer> neighbors = new LinkedList<>();
    	List<Integer> route = new ArrayList<>();
    	
    	while (!lifo.isEmpty()) {
    		cur = lifo.pop();
    		visited[cur] = 1;
    		route.add(cur);
    		neighbors = mst.getEdgeList(cur);
    		for (int ii = 0; ii < neighbors.size(); ii++) {
    			if (visited[neighbors.get(ii)] == 0 && !lifo.contains(neighbors.get(ii))) { // lifo may already contain it since cycles exist
    				lifo.push(neighbors.get(ii));
    			}
    		}
    	}
    	route.add(route.get(0)); // close the circuit by returning to the initial node
    	
    	return route;
    }
}
