import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;

/**
 * Class to create a graph
 */

public class Graph {
	private final int NO_TIME_PROVIDED=0;
	Node[] nodes;
	ArrayList<TransfersAndTimes> trans = new ArrayList<TransfersAndTimes>();
	ArrayList<Edge> edges;


	/**
	 * @param files: String array containing the three input files. (stops.txt=files[0],transfers.txt=files[1],stop_times.txt=files[2]
	 * @throws FileNotFoundException 
	 */


	Graph(String[] files) throws FileNotFoundException {

		if (files != null) {
			edges = new ArrayList<>();

			/*
			 * STOPS.TXT
			 */

			//Find #Stops in stops.txt
			int numberOfStops = 0;
			ArrayList<Integer> stopsID = new ArrayList<>();
			Scanner Scanner1 = new Scanner(new File(files[0]));

			//skip first line
			Scanner1.nextLine();

			/* The node.lable=stopID*/
			while(Scanner1.hasNextLine())
			{
				stopsID.add(Integer.parseInt(Scanner1.nextLine().trim().split(",")[0]));	//Just the stop_id
				numberOfStops++;
			}

			nodes = new Node[numberOfStops];

			//node.label=stopID
			for (int index = 0; index < numberOfStops; index++) {
				nodes[index] = new Node(index);
				nodes[index].label = stopsID.get(index);
			}

			//sort for efficiency
			Arrays.sort(nodes, Node::compareTo);



			/* TRANSFERS.TXT
			 * 
			 */
			Scanner Scanner2 = new Scanner(new File(files[1]));

			//skip first line
			Scanner2.nextLine();

			while(Scanner2.hasNextLine()) {
				String[] tempLine = Scanner2.nextLine().trim().split(",");

				//for cases where no time is provided, we set it to no time
				int time = NO_TIME_PROVIDED;
				if (tempLine.length==4) {
					time = Integer.parseInt(tempLine[3]);
				}

				trans.add(new TransfersAndTimes(Integer.parseInt(tempLine[0]),
						Integer.parseInt(tempLine[1]),Integer.parseInt(tempLine[2]),time));
			}

			/*
			 *    STOP_TIMES.TXT
			 */

			Scanner Scanner3 = new Scanner(new File(files[2]));

			/*Error handle for line0 which contains the tickers*/
			int previousTripId = -1;
			int previousStopId = -1;

			//skip first line
			Scanner3.nextLine();

			/*reading lines from stop_times.txt and adding them to transfers ArrayList.
                          if previousID==currentID, then we add to trans list
			 */


			while(Scanner3.hasNextLine()) {
				String[] tempLine = Scanner3.nextLine().trim().split(",");

				//read current line info
				int currentTripId = Integer.parseInt(tempLine[0]);
				int currentStopId = Integer.parseInt(tempLine[3]);

				if (currentTripId==previousTripId)
				{
					//add current transfer to transfers
					trans.add(new TransfersAndTimes(previousStopId, currentStopId,1,0));
				}
				previousTripId = currentTripId;
				previousStopId = currentStopId;
			}

			//Trans to graphs

			/* sort transfers by toStopIds, then by fromStopIds. this makes sure that all duplicates are later
                   removed (because simply sorting by fromStopIds leaves the toStopIds unsorted)*/
			Collections.sort(trans, TransfersAndTimes::compareTo);
			Collections.sort(trans, TransfersAndTimes::compareFrom);


			
			int j=0;
			boolean finish = false;


			//for each node find corresponding transfers from 
			for (int i=0;i<nodes.length;i++)
			{

				int previousToStopId = -1;


				
				if (finish) break;

				boolean allTransfersFound = false;

				while (!allTransfersFound) {
					
					if (j >= trans.size()) {
						allTransfersFound = true;
						finish = true;
						break;
					}

					/* if  the current transfer's fromStopId and node label are  same,
                           and it isn't a duplicate, add the transfer
                          as a node to the edge, and add the edge to edges */

					else if (trans.get(j).fromStopId == nodes[i].label) {
						if (trans.get(j).toStopId != previousToStopId) {

							//WEIGHT OF EDGE
							//transferTyper=0,cost=2
							//TransferType=2,cost=minTransferTime/100
							float weight = 0.0f;

							//cost is 2 if transfer type is 0 
							if (trans.get(j).transferType == 0) {
								weight = 2.0f;
							}
							//cost is 1 if transfer type is 1
							else if (trans.get(j).transferType == 1) {
								weight = 1.0f;
							}

							else if (trans.get(j).transferType == 2) {
								weight = (trans.get(j).minTransferTime / 100);
							}
							else {
								System.out.println("invalid transfer type" + trans.get(j).transferType);
							}

							//loop through nodes to get node corresponding to toStopId

							int toStopId = 0;
							for (int k = 0; k < nodes.length; k++) {
								if (nodes[k].label == trans.get(j).toStopId) {
									toStopId = k;
								}
							}




							//add edge to nodes and edges
							Edge newEdge = new Edge(nodes[i], nodes[toStopId], weight);

							nodes[i].addEdge(newEdge);
							edges.add(newEdge);

							previousToStopId = trans.get(j).toStopId;
						}
						//go to next transfer and repeat checks in the next iteration of the loop
						j++;
					}

					//go to next transfer and repeat checks
					else if (trans.get(j).fromStopId < nodes[i].label) {
						j++;
					}

					// all transfers have been added, or there were no transfers from the current node 
					else if (trans.get(j).fromStopId > nodes[i].label) {
						allTransfersFound = true;
					}
				}
			}



		}
	}

	public int getNodeIndexFromLabel(int label)
	{
		int index = 0;
		boolean indexFound = false;
		while (!indexFound&&(index<this.nodes.length))
		{
			if (this.nodes[index].label==label) { 
				indexFound = true;
			}
			else index++;
		}

		if (index==this.nodes.length) return -1;
		else return index;
	}

}


class Path {
	Node src, dst;
	Double distance;

	Path() {
		distance = -1.0;
	}

	Path(Node src, Node dst, Double distance) {
		this.src = src;
		this.dst = dst;
		this.distance = distance;
	}
}


class Node {
	int label;
	ArrayList<Edge> edges;

	Node(int label) {
		this.label = label;
		edges = new ArrayList<>();
	}


	void addEdge(Edge edge) {
		edges.add(edge);
	}


	Node[] getNeighbours() {
		Node[] output = new Node[edges.size()];

		for (int i = 0; i < edges.size(); i++){
			output[i] = edges.get(i).dst;
		}

		return output;
	}


	double length(Node dst) {
		for (Edge i : edges) {
			if (i.dst == dst) {
				return i.weight;
			}
		}

		return -1;
	}

	public int compareTo(Node temp) {
		return this.label - temp.label;
	}
}


class Edge {
	Node src, dst;
	double weight;


	Edge(Node src, Node dst, double weight) {
		this.src = src;
		this.dst = dst;
		this.weight = weight;
	}
	
	/*
	 * @KG : Removed enclosing round brackets
	 */
	
	public String toString()
    {
        //return "("+this.src.label+","+this.dst.label+","+this.weight+")";
		return this.src.label+","+this.dst.label+","+this.weight;
    }


}
