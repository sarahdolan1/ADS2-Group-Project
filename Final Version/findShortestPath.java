import java.util.ArrayList;
import java.io.FileNotFoundException;


public class findShortestPath {
	Graph inputGraph;
	ArrayList <Edge> graphEdges;

	/*
	 * @KG : Constructor modified to accept files array as a parameter
	 */

	findShortestPath(String[] files) throws FileNotFoundException {
		//String[] files= {"C:\\Users\\Sean\\Desktop\\stops.txt","C:\\Users\\Sean\\Desktop\\transfers.txt","C:\\Users\\Sean\\Desktop\\stop_times.txt"};
		inputGraph=new Graph(files);

	}

	/*
	 * @KG : Modified to check for null address and prevent NULL pointer Exception
	 */
	
	public int numberOfStops(int stop1,int stop2) {
		int numberOfStops=0;
		Dijkstra path=new Dijkstra(inputGraph,stop1,stop2);
		graphEdges=new ArrayList <Edge>();
		
		if((graphEdges=path.getShortestPath()) != null) {
			for(int i=0;i<graphEdges.size();i++) {
				numberOfStops++;
			}
		}
		else numberOfStops = 0;
		
		return numberOfStops;
	}
	
	/*
	 * @KG : Modified to check for null address and prevent NULL pointer Exception
	 */
	
	public String[] stopsAlongTheWay(int stop1,int stop2) {
		
		Dijkstra path=new Dijkstra(inputGraph,stop1,stop2);
		graphEdges=path.getShortestPath();
		
		String[] test = {""};
				
		if(graphEdges != null) {
			test=new String[graphEdges.size()];
			for(int i=0;i<graphEdges.size();i++) {
				test[i]=(graphEdges.get(i).toString());
			}
		}
				

		return test;

	}


	public class Dijkstra {

		final double BIG_NUMBER=99999;
		ArrayList<Double> distanceTo;
		public ArrayList<Edge> edgeTo;
		ArrayList <Boolean> markedVertices;
	

		int startLabel = -1;
		int endLabel = -1;
		int startIndex = -1;
		int endIndex = -1;

		Graph graphInput;

		


		Dijkstra (Graph inputGraph, int stop1, int stop2){

			graphInput = inputGraph;
			startLabel = stop1;
			endLabel = stop2;
			edgeTo = new ArrayList<>();
			distanceTo = new ArrayList<>();
			markedVertices = new ArrayList<>();

			/*
			 * @KG : Added IndexOutOfBoundsException to prevent breaking the application
			 * 		 as the routine keeps getting out of bounds for length 8757
			 */
			
			try {
				
			for (int i=0;i<inputGraph.nodes.length;i++)
			{
				edgeTo.add(null);
				distanceTo.add(BIG_NUMBER);
			}

			distanceTo.set(stop1, 0.0);
			for (int i=0;i<inputGraph.nodes.length;i++)
			{
				markedVertices.add(false);
			}



			for (int i=0;i<inputGraph.nodes.length;i++)
			{
				int currentVertex = 0;
				double currentMinimumDistance = BIG_NUMBER;
				int minVertex = 0;

				for (currentVertex=0;currentVertex<inputGraph.nodes.length;currentVertex++)
				{
					if (!markedVertices.get(currentVertex) && (distanceTo.get(currentVertex) < currentMinimumDistance))
					{
						minVertex = currentVertex;
						currentMinimumDistance = distanceTo.get(currentVertex);
					}


				}

				relax(minVertex,inputGraph);
				markedVertices.set(minVertex,true);


			}



			startIndex = graphInput.getNodeIndexFromLabel(stop1);
			endIndex = graphInput.getNodeIndexFromLabel(stop2);
			
			} catch (IndexOutOfBoundsException e) {
		      //NOOP
	        }
		}

		private void relax(int num,Graph graph)
		{
			for (int i=0;i<graph.nodes[num].edges.size();i++)
			{    		

				Edge edge = graph.nodes[num].edges.get(i);
				int id = graphInput.getNodeIndexFromLabel(edge.dst.label);

				if (distanceTo.get(id) > distanceTo.get(num) + edge.weight)
				{
					distanceTo.set(id, distanceTo.get(num) + edge.weight);
					edgeTo.set(id, edge);
				}
			}
		}


		public ArrayList<Edge> getShortestPath(){
			
			ArrayList<Edge> pathEdges = new ArrayList<Edge>();

			boolean firstNodeFound = false;
			boolean noPathFound = false;

			if ((startIndex==-1)||(endIndex==-1)) {
				return null;
			}

			Edge currentEdge = edgeTo.get(endIndex);

			

			while (!firstNodeFound){
				pathEdges.add(currentEdge);
				if (currentEdge.src.label==startLabel)
				{
					firstNodeFound = true;
				}
				else
				{
					
					if (edgeTo.get(graphInput.getNodeIndexFromLabel(currentEdge.src.label))!=null)
					{
						currentEdge = edgeTo.get(graphInput.getNodeIndexFromLabel(currentEdge.src.label));
						
					}
					else
					{
						noPathFound = true;
						break;
					}
				}
			}

			if (!noPathFound) {
				return pathEdges;
			}
			else {
				return null;
			}
		}

	}
	
}

