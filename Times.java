import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;


 class Times {
	 //temp main of how it works 
	 public static void main(String[] args) {
			
			new Times("C:\\Users\\sarah\\OneDrive\\Desktop\\algosProject\\stop_times.txt");
			
			List<stopInformation>myStops= Times.getStopsInfo("20:19:27");
			//List<stopInformation>myStops= stopTimes.getStopsInfo("29:00:00");
			
			System.out.println("sorted in order of ID ");
			System.out.println("we have found "+myStops.size() + " stops");
			for(stopInformation stops:myStops){
				System.out.printf("trip_id:%d,arrival_time:%s,departure_time:%s,stop_id:%d,stop_sequence:%d,"
						+ "stop_headsign:%s,pickup_type:%d,drop_off_type:%d,shape_dist_traveled:%f%n",
						stops.trip_id,stops.arrival_time,stops.departure_time,stops.stop_id,stops.stop_sequence,stops.stop_headsign,stops.pickup_type,
						stops.drop_off_type,stops.shape_dist_traveled);
			}
		}
	 
	 
	 protected class stopInformation {
			protected int trip_id;
			protected Time arrival_time;
			protected Time departure_time;
			protected int stop_id;
			protected int stop_sequence;
			protected String stop_headsign;
			protected int pickup_type;
			protected int drop_off_type;
			protected double shape_dist_traveled;

			protected stopInformation(int trip_id, Time arrival_time, Time departure_time, int stop_id, int stop_sequence,
					String stop_headsign, int pickup_type, int drop_off_type, double shape_dist_traveled) {
				this.trip_id = trip_id;
				this.arrival_time = arrival_time;
				this.departure_time = departure_time;
				this.stop_id = stop_id;
				this.stop_sequence = stop_sequence;
				this.stop_headsign = stop_headsign;
				this.pickup_type = pickup_type;
				this.drop_off_type = drop_off_type;
				this.shape_dist_traveled = shape_dist_traveled;
			}
		}
	 
	 
	protected static List<stopInformation> inrformation;

	Times(String filename) {
		File myFile;
		try {
			if (filename != null) {
				inrformation = new ArrayList<>();
				double i = -1;
				int count=0;
				myFile = new File(filename);
				Scanner sc = new Scanner(myFile);
				sc.nextLine();
				while (sc.hasNextLine()) {
					String string[] = sc.nextLine().split("\\s+|,\\s*");
					System.out.println(count+=1);
					int first = (string[0] != "") ? Integer.parseInt(string[0]) : -1;
					Time second = Time.valueOf(string[1]);
					Time third = Time.valueOf(string[2]);
					int fourth = (string[3] != "") ? Integer.parseInt(string[3]) : -1;
					int fifth = (string[4] != "") ? Integer.parseInt(string[4]) : -1;
					String sixth = (string[5] != "") ? string[5] : null;
					int seveth = (string[6] != "") ? Integer.parseInt(string[6]) : -1;
					int eigth = (string[7] != "") ? Integer.parseInt(string[7]) : -1;
					try {
					i = (string[8] != "") ? Double.parseDouble(string[8]) : -1;
					
					}
					catch(ArrayIndexOutOfBoundsException e1) {
						i = -1;
						
					}
					inrformation.add(new stopInformation(first, second, third, fourth, fifth, sixth, seveth, eigth, i));
				}
				sc.close();
			} else {

			}

		} catch (FileNotFoundException e) {

		}
	}

	
	protected static Comparator<stopInformation> IDsort = new Comparator<stopInformation>() {

		@Override
		public int compare(stopInformation a, stopInformation b) {

			return a.trip_id - b.trip_id;

		}
	};


	public static List<stopInformation> getStopsInfo(String arrivalTime) {
		List<stopInformation>stopsByArrival= new ArrayList<>();
		try {
			Time arriveT= Time.valueOf(arrivalTime);

			
			for(int i=0;i<inrformation.size();i++) {

				if((inrformation.get(i).arrival_time).equals(arriveT)){
					stopsByArrival.add(inrformation.get(i));
					
				}
			}
			Collections.sort(stopsByArrival,Times.IDsort);			
			
			return stopsByArrival;
			
		}
		catch(Exception e) {
			System.out.println("error");
			return null;
		}
	}
	
	
	
	
}
