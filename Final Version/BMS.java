/*
 * Bus Management System
 * 
 * CSU22012: Data Structures and Algorithms Group Project
 * 
 * Denisa Costinas :	class TernarySearchTree.java
 * Sarah Dolan :		class Times.java
 * Keira Gatt :			class BMS.java
 * Sean Langan :		class findShortestPath.java
 * 						class TransfersAndTimes.java
 * 						class Graph.java
 * 
 * 30/04/2021
 * 
 */

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JSpinner;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.util.regex.*;


@SuppressWarnings("serial")
public class BMS extends JFrame implements ActionListener{

	// Constant declarations
	static final int FILE_STOPT = 0;
	static final int FILE_STOPS = 1;
	static final int FILE_XFERS = 2;
	static final int FILE_MAX = 3;
	static final String DATA_FILES[] = { "stop_times.txt", "stops.txt", "transfers.txt" };
	
	static final int CLR_ALL = 0;
	static final int CLR_TIME = 1;
	static final int CLR_PATH = 2;
	static final int CLR_BUS = 3;
	
	static final int INP_NULL = 0;
	static final int INP_BUS = 1;
	static final int INP_PATH = 2;
	static final int INP_TIME = 3;
	static final int INP_DATA_PATH = 4;
	
	static final int OS_WIN = 0;
	static final int OS_UXMAC = 1;
		
	static final String BUS_VALID = "^[a-zA-Z0-9-@ ]+$";
	static final String PATH_VALID = "^[0-9]+$";
	static final String DATA_PATH_VALID_WIN = "^[a-zA-Z]:(?:[^:*?\"<>|\r\n])*$";
	static final String DATA_PATH_VALID_UXMAC = "^/(?:[^:*?\"<>|\r\n])*$";
	
	// Data files, path and field size declarations
	private static int osType;
	private static String dataPath, newPath;
	private static boolean fileStatus[] = { false, false, false };
	private static int fieldSize[] = { 0, 35, 20, 0, 50 };
	
	// GUI declarations
	private static JSpinner jSpinner1;
	private static CardLayout cardLayout;
	private static JTextPane busPane, pathPane, timePane;
	private static JLabel busResLbl, pathResLbl, timeResLbl;
	private static JTextField pathTF1, pathTF2, busTF, pathField;
	private static JPanel mainPanel, busPanel, timePanel, timePanelResults, busPanelResults, pathPanel, pathPanelResults, menuPanel;

	// External class declarations
	static Times timeTripInfo;
	static TernarySearchTree busStopInfo;
	static findShortestPath shortestPathInfo;

	
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		
		// Determine runtime environment
		
		if(System.getProperty("os.name").contains("Win")) {
			osType = OS_WIN;
		}
		else osType = OS_UXMAC;
		
		dataPath = System.getProperty("user.dir");

		checkFileStatus(dataPath);
		initData(dataPath);
								
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				
				try {
					
					BMS frame = new BMS();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
				
	}

	
	/**
	 * 
	 * Constructor - Create GUI frame
	 * 
	 */
	
	public BMS() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 700);
		
		cardLayout = new CardLayout();
		mainPanel = new JPanel();
		
		mainPanel.setBackground(new Color(255, 255, 255));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPanel);
		mainPanel.setLayout(cardLayout);
		
		// Menu Panel
		menuPanel = new JPanel();
		
		// Panel 2 Arrival Time Query
		timePanel = new JPanel(); 
	
        // Panel 3 Bus Name Query
		busPanel = new JPanel();
		
		// Panel 4 Shortest Path Query
		pathPanel = new JPanel();
	
		// Panel 5 Bus Name Query Results
		busPanelResults = new JPanel();
		
		// Panel 6 Arrival Time Query Results
		timePanelResults = new JPanel();
		
		// Panel 7 Shortest Path Query Results
		pathPanelResults = new JPanel();
		
		// Set up menu buttons
		menuPanelConfig(menuPanel);
		
		// Configure all panels
		pathPanelConfig(pathPanel);
		timePanelConfig(timePanel);
		busPanelConfig(busPanel);
		pathPanelResultsConfig(pathPanelResults);
		timePanelResultsConfig(timePanelResults);
		busPanelResultsConfig(busPanelResults);
		
		// Add all panels to Main Panel Card Layout
		mainPanel.add(menuPanel, "MENU");
		mainPanel.add(busPanel, "BUS");
		mainPanel.add(pathPanel, "PATH");
		mainPanel.add(timePanel, "TIME");
		mainPanel.add(busPanelResults, "BUS_RES");
		mainPanel.add(timePanelResults, "TIME_RES");
		mainPanel.add(pathPanelResults, "PATH_RES");
		
	}

	
	/*
	 * userValidation()
	 * 
	 * Validate user field input
	 * 
	 * Parameters:
	 * String userInput: user-entered query input
	 * int mode : validation selector
	 * 
	 * Return:
	 * boolean: true if input is valid, false otherwise
	 *  
	 */
	
	private boolean userValidation(String userInput, int mode) {
 
    	boolean valid = true;
    	
    	// Check if Empty Query
    	if(userInput.length() == 0) {
    		JOptionPane.showMessageDialog(this,"No Input Supplied", "Error",JOptionPane.ERROR_MESSAGE);
    		mode = INP_NULL;
    		valid = false;
    	}
    	
    	// Check input length
    	if(mode != INP_NULL && mode != INP_TIME) {
    		if(userInput.length() > fieldSize[mode]) {
    			JOptionPane.showMessageDialog(this,"Input Exceeds Max Size (" + fieldSize[mode] + ")", "Error",JOptionPane.ERROR_MESSAGE);
    			mode = INP_NULL;
    			valid = false;
    		}
    	}
    	
    	// Validate Bus Stop Query Input
    	if(mode == INP_BUS) {
    		if(!(valid = checkUserInput(userInput, BUS_VALID))) {
    			JOptionPane.showMessageDialog(this,"Invalid Bus Keyword", "Error",JOptionPane.ERROR_MESSAGE);
    		}
    	}
    	
    	// Validate Shortest Path Query Input
    	if(mode == INP_PATH) {
    		if(!(valid = checkUserInput(userInput, PATH_VALID))) {
    			JOptionPane.showMessageDialog(this,"Invalid Bus Stop", "Error",JOptionPane.ERROR_MESSAGE);
    		}
    	}
    	
    	// Check if time is valid & in ISO format
    	if(mode == INP_TIME) {
    		
    		try {
    			DateTimeFormatter.ISO_TIME.parse(userInput);
            } catch (DateTimeParseException e) {
            	JOptionPane.showMessageDialog(this,"Invalid Time Input", "Error",JOptionPane.ERROR_MESSAGE); 
            	valid = false;
            }
    	}
    	
    	// Validate New Data Path Input
    	if(mode == INP_DATA_PATH) {
    		String dataPathValid = osType == OS_WIN ? DATA_PATH_VALID_WIN : DATA_PATH_VALID_UXMAC;
    		if(!(valid = checkUserInput(userInput, dataPathValid))) {
    			JOptionPane.showMessageDialog(this,"Invalid Characters in Path", "Error",JOptionPane.ERROR_MESSAGE);
    		}
    	}
    	
    	return valid;
    }
	
	
	/*
	 * busQuery()
	 * 
	 * Perform search by Bus Stop Name
	 * 
	 * Parameters:
	 * String stopInput : Bus Stop Name keyword
	 * 
	 * Return:
	 * String : HTML-formatted results
	 *  
	 */
	
	private String busQuery(String stopInput) {
		
		int resCnt;
		String[] stopStr;
		String busResult, rowStyle;
		String noResult = "No information is available for this search criteria";
		
		String newLine = System.getProperty("line.separator");
		
		// Call method from class TernarySearchTree
		ArrayList<String> myStops=busStopInfo.search(stopInput.toUpperCase());
		
		resCnt = myStops.size();
		
		// Format output as HTML
		busResult = "<html><head><style>table, th, td { border: 1px solid black; border-collapse: collapse; }" + newLine +
				"th, td { padding: 10px; } th { text-align: center; } #t01 th { background-color: #eac3ca; color: black; }" + newLine +
				"</style></head><body>" + newLine +
								
			     "<table id=\"t01\", style=\"width: 100%\"><caption><h1>" + resCnt + " Stop(s) Matching Bus Stop Name</h1></caption>" + newLine +
			     "<tr><th>#</th><th>Stop Name</th><th>Stop ID</th><th>Stop Code</th><th>Stop Description</th><th>Stop Latitude</th><th>Stop Longitude</th><th>Zone ID</th></tr>" + newLine;
		
		for (int i = 0; i < resCnt; i++) {
		
			rowStyle = i % 2 == 0 ? "#fce3c0" : "#fce3cf";
			
			stopStr = myStops.get(i).split(" - ");		// tokenise by string delimiter
			
			busResult = busResult + "<tr style=\"background-color: " + rowStyle + ";\"><td align=\"center\">" + (i + 1) + "</td>" +
								  	  "<td align=\"center\">" + stopStr[0] + "</td><td align=\"center\">" + stopStr[1] + "</td><td align=\"center\">" + stopStr[2] + "</td>" +
								  	  "<td align=\"center\">" + stopStr[3] + "</td><td align=\"center\">" + stopStr[4] + "</td><td align=\"center\">" + stopStr[5] + "</td>" +
								  	  "<td align=\"center\">" + stopStr[6] + "</td></tr>" + newLine;
	
		}
					
		busResult = busResult + " </table>";
		
		if(resCnt == 0) {
			
			busResult = busResult + "<table style=\"width: 100%; border: none \"><caption style =\"background-color: #81e9c6;\"><h2>" + noResult + "</h2></caption></table>" + newLine;
		
		}
		
		busResult = busResult + "</body></html>";
						
		return busResult;
	
	}

	
	/*
	 * timeQuery()
	 * 
	 * Perform search by Arrival Time
	 * 
	 * Parameters:
	 * String timeInput : Arrival Time user input
	 * 
	 * Return:
	 * String : HTML-formatted results
	 *  
	 */
	
	private String timeQuery(String timeInput) {
		
		int resCnt;
		String timeResult, rowStyle;
		String noResult = "No information is available for this search criteria";
		
		String newLine = System.getProperty("line.separator");
		
		// Call method from class Times
		List<Times.stopInformation>myStops= timeTripInfo.getStopsInfo(timeInput);
		
		resCnt = myStops.size();
		
		// Format output as HTML
		timeResult = "<html><head><style>table, th, td { border: 1px solid black; border-collapse: collapse; }" + newLine +
					"th, td { padding: 10px; } th { text-align: center; } #t01 th { background-color: #BDB76B; color: black; }" + newLine +
					"</style></head><body>" + newLine +

				     "<table id=\"t01\", style=\"width: 100%\"><caption><h1>" + resCnt + " Trip(s) Matching Arrival Time</h1></caption>" + newLine +
				     "<tr><th>#</th><th>Trip ID</th><th>Arrival Time</th><th>Departure Time</th><th>Stop ID</th><th>Stop Seq</th><th>Shape Dist Travelled</th></tr>" + newLine;
			
		for (int i = 0; i < resCnt; i++) {
			
			rowStyle = i % 2 == 0 ? "#FFFFE0" : "#FFFFEF";
				
			timeResult = timeResult + "<tr style=\"background-color: " + rowStyle + ";\"><td align=\"center\">" + (i + 1) + "</td>" +
									  "<td align=\"center\">" + myStops.get(i).trip_id + "</td><td align=\"center\">" + myStops.get(i).arrival_time + "</td>" +
									  "<td align=\"center\">" + myStops.get(i).departure_time + "</td><td align=\"center\">" + myStops.get(i).stop_id + "</td>" + 
									  "<td align=\"center\">" + myStops.get(i).stop_sequence + "</td><td align=\"center\">" + myStops.get(i).shape_dist_traveled + "</td></tr>" + newLine;
		}
						
		timeResult = timeResult + " </table>";
			
		if(resCnt == 0) {
				
			timeResult = timeResult + "<table style=\"width: 100%; border: none \"><caption style =\"background-color: #81e9c6;\"><h2>" + noResult + "</h2></caption></table>" + newLine;
			
		}
			
		timeResult = timeResult + "</body></html>";
							
		return timeResult;
		
	}
	

	/*
	 * shortestPathQuery()
	 * 
	 * Perform search for Shortest Path
	 * 
	 * Parameters:
	 * String stop1 : From Bus Stop ID
	 * String stop 2 : To Bus Stop ID
	 * 
	 * Return:
	 * String : HTML-formatted results
	 *  
	 */
	
	private String shortestPathQuery(String stop1, String stop2) {
		
		int busStop1, busStop2, stopCnt;
		String pathResult, rowStyle, stopStr[];
		String noResult = "No information is available for this search criteria";
		
		String stopList[] = {};
		
		String newLine = System.getProperty("line.separator");
		
		busStop1 = Integer.parseInt(stop1);
		busStop2 = Integer.parseInt(stop2);
		
		if((stopCnt = shortestPathInfo.numberOfStops(busStop1, busStop2)) > 0) {
			stopList = shortestPathInfo.stopsAlongTheWay(busStop1, busStop2);
		}
						
		// Format output as HTML
		pathResult = "<html><head><style>table, th, td { border: 1px solid black; border-collapse: collapse; }" + newLine +
					"th, td { padding: 10px; } th { text-align: center; } #t01 th { background-color: #87d900; color: black; }" + newLine +
					"</style></head><body>" + newLine +

				     "<table id=\"t01\", style=\"width: 100%\"><caption><h1>" + stopCnt + " Stop(s) En Route</h1></caption>" + newLine +
				     "<tr><th>Stop #</th><th>From Stop ID</th><th>To Stop ID</th><th>Cost</th></tr>" + newLine;
			
		for (int i = (stopCnt - 1); i >= 0; i--) {
			
			rowStyle = i % 2 == 0 ? "#fff4c0" : "#fff4cf";
			
			stopStr = stopList[i].split(",");					// tokenise by string delimiter
				
			pathResult = pathResult + "<tr style=\"background-color: " + rowStyle + ";\"><td align=\"center\">" + (stopCnt - i) + "</td>" +
									  "<td align=\"center\">" + stopStr[0] + "</td><td align=\"center\">" + stopStr[1] + "</td>" +
									  "<td align=\"center\">" + stopStr[2] + "</td></tr>" + newLine;
		}
						
		pathResult = pathResult + " </table>";
			
		if(stopCnt == 0) {
				
			pathResult = pathResult + "<table style=\"width: 100%; border: none \"><caption style =\"background-color: #81e9c6;\"><h2>" + noResult + "</h2></caption></table>" + newLine;
			
		}
			
		pathResult = pathResult + "</body></html>";

		return pathResult;
	}
	
	
	/*
	 * actionPerformed()
	 * 
	 * GUI event handler
	 * 
	 * Parameters:
	 * ActionEvent ae : Action Event object
	 * 
	 * Return:
	 * <None>
	 *  
	 */
		
	@Override
    public void actionPerformed(ActionEvent ae) {
		
		String action = ae.getActionCommand();
		
		// Perform actions on Main Menu
		if(menuPanel.isShowing()) {
			
			if (action.equals("Shortest Path")) {		// Display Shortest Path Search data input screen if required files are available
	        	
	        	if(!fileStatus[FILE_XFERS] || !fileStatus[FILE_STOPS] || !fileStatus[FILE_STOPT]) {
	    			JOptionPane.showMessageDialog(this,"Cannot open required file(s)", "Error", JOptionPane.ERROR_MESSAGE);
	    		}
	        	else cardLayout.show(mainPanel, "PATH");
	 
	        }
	        else if (action.equals("Bus Stop Name")) {	// Display Bus Stop Search data input screen if required file is available
	        	
	        	if(!fileStatus[FILE_STOPS]) {
	    			JOptionPane.showMessageDialog(this,"Cannot opon file " + DATA_FILES[FILE_STOPS], "Error", JOptionPane.ERROR_MESSAGE);
	    		}
	        	else cardLayout.show(mainPanel, "BUS");
	            
	        }
	        else if (action.equals("Arrival Time")) {	// Display Search by Arrival Time data input screen if required file is available
	        	
	        	if(!fileStatus[FILE_STOPT]) {
	    			JOptionPane.showMessageDialog(this,"Cannot open file " + DATA_FILES[FILE_STOPT], "Error", JOptionPane.ERROR_MESSAGE);
	    		}
	        	else cardLayout.show(mainPanel, "TIME");
	           
	        }
	        else if(action.equals("Set Data Path")) {	// Change Data Path
	        	
	        	newPath = pathField.getText();
	        	
	        	if(userValidation(newPath, INP_DATA_PATH)) {
	        		if(!checkNewPath(newPath)) {
	        			JOptionPane.showMessageDialog(this,"Cannot access new Data Path", "Error", JOptionPane.ERROR_MESSAGE);
	        		}
	        		else {
	        			pathField.setText("Loading Data....Please Wait");
	        			JOptionPane.showMessageDialog(this,"Press OK to load data...", "Info", JOptionPane.INFORMATION_MESSAGE);
	        			initData(dataPath);
	        			JOptionPane.showMessageDialog(this,"New Data Path enabled", "Info", JOptionPane.INFORMATION_MESSAGE);
	        		}
	        	}
	        	
	        	pathField.setText(dataPath);
	        	
	        }
	        else if (action.equals("Quit")) {			// Exit application
	        	
	            System.exit(0);
	            
	        }
		
		}
		else if(action.equals("Main Menu")) {		// Return to Main Menu and clear input fields
        	
        	cardLayout.show(mainPanel, "MENU");
        	clearInputFields(CLR_ALL);
        
        }
        else if(action.contains("Clear")) {			// Perform actions on a Clear event 
        	
        	if(action.contains("All")) {
        		clearInputFields(CLR_PATH);
        	}
        	else if(action.contains("Text")) {
        		clearInputFields(CLR_BUS);
        	}
        	else if(action.contains("Time")) {
        		clearInputFields(CLR_TIME);											
        	}
 
        }
        else if(action.equals("Return")) {			// Perform actions on a Return event
        	
        	if(timePanelResults.isShowing()) {
        		cardLayout.show(mainPanel, "TIME");
         	}
        	else if(busPanelResults.isShowing()) {
        		cardLayout.show(mainPanel, "BUS");
        	}
        	else {
        		cardLayout.show(mainPanel, "PATH");
        	}
        
        }
        else if(action.equals("Search")) {			// Perform actions on a Search event
        	
        	if(pathPanel.isShowing()) {				// Perform Shortest Path search and show results
        	
        		if((userValidation(pathTF1.getText(), INP_PATH)) && ((userValidation(pathTF2.getText(), INP_PATH)))) {
        		
        			pathPane.setText(shortestPathQuery(pathTF1.getText(), pathTF2.getText()));
        			pathResLbl.setText("Showing results for: " + pathTF1.getText() + " to " + pathTF2.getText());
        			cardLayout.show(mainPanel, "PATH_RES");
        			pathPane.setSelectionStart(0);
        			pathPane.setSelectionEnd(0);
        		}

        	}
        	else if(busPanel.isShowing()) {			// Perform Bus Stop search and show results
 
        		if(userValidation(busTF.getText(), INP_BUS)) {

        			busPane.setText(busQuery(busTF.getText()));
        			busResLbl.setText("Showing results for: " + busTF.getText());
        			cardLayout.show(mainPanel, "BUS_RES");
        			busPane.setSelectionStart(0);
        			busPane.setSelectionEnd(0);
        		}
        	}
        	else if(timePanel.isShowing()) {		// Perform search by Arrival Time and show results

       			String value = jSpinner1.getValue().toString();
       			String[] timeArray = value.split(" ");

       			if(userValidation(timeArray[3], INP_TIME)) {
            		
       				timePane.setText((timeQuery(timeArray[3])));
       				timeResLbl.setText("Showing results for: " + timeArray[3]);
       				cardLayout.show(mainPanel, "TIME_RES");
       				timePane.setSelectionStart(0);
       				timePane.setSelectionEnd(0);
        				
       			}
            }
        }
	}
	

	/*
	 * checkFileStatus()
	 * 
	 * Check if data files are readable and update file status array
	 * 
	 * Parameters:
	 * String dataPath : Current path on file system
	 * 
	 * Return:
	 * <None>
	 *  
	 */
		
	private static void checkFileStatus(String dataPath) {
		
		File fileObj;
		
		for(int i = 0; i < FILE_MAX; i++) {
	
			fileObj = new File(dataPath + "\\" + DATA_FILES[i]);
			
			if((fileStatus[i] = fileObj.exists())) {		// update boolean class array
				
				fileStatus[i] = fileObj.canRead();
								
			}
			
		}
	
	}

	
	/*
	 * checkNewPath()
	 * 
	 * Verify if new data path is valid and accessible
	 * 
	 * Parameters:
	 * String newPath : new path entered by user
	 * 
	 * Return:
	 * boolean true if new path is usable, false otherwise
	 *  
	 */
		
	private static boolean checkNewPath(String newPath) {
		
		File fileObj;
		boolean pathStatus;
		
		fileObj = new File(newPath);
		
		if((pathStatus = fileObj.exists())) {
			
			if((pathStatus = fileObj.canRead())) {
				
				dataPath = newPath;
				checkFileStatus(dataPath);
				
			}
		
		}
				
		return pathStatus;
	}
	

	/*
	 * initData()
	 * 
	 * Initialise data by calling external class constructors and methods
	 * 
	 * Parameters:
	 * String dataPath : current file system path
	 * 
	 * Return:
	 * <none>
	 *  
	 */
		
	@SuppressWarnings("static-access")
	private static void initData(String dataPath) {
		
		// Set current data structures eligible for garbage collection
		
		timeTripInfo = null;		
		busStopInfo = null;
		shortestPathInfo = null;
		System.gc();				
		
		// Create new instances for Search classes if required files are available
		
		if(fileStatus[FILE_STOPT]) {
			
			timeTripInfo = new Times(dataPath + "/" + DATA_FILES[FILE_STOPT]);
		
		}
				
		if(fileStatus[FILE_STOPS]) {
			
			busStopInfo = new TernarySearchTree();
			busStopInfo.initTst(dataPath + "/" + DATA_FILES[FILE_STOPS]);
		
		}
		
		if(fileStatus[FILE_STOPT] && fileStatus[FILE_STOPS] && fileStatus[FILE_XFERS]) {
			
			String fileList[] = {
					dataPath + "/" + DATA_FILES[FILE_STOPS],
					dataPath + "/" + DATA_FILES[FILE_XFERS],
					dataPath + "/" + DATA_FILES[FILE_STOPT]
			};
			
			try {
				shortestPathInfo = new findShortestPath(fileList);
			} catch (FileNotFoundException e) {
				
			}
		}
	}
	

	/*
	 * clearInputFields()
	 * 
	 * Reset user input fields
	 * 
	 * Parameters:
	 * int mode : field selector constant
	 * 
	 * Return:
	 * <none>
	 *  
	 */
		
	private void clearInputFields(int mode) {
	
		if(mode == CLR_PATH || mode == CLR_ALL) {
    		pathTF1.setText("");
            pathTF2.setText("");
    	}
		
		if(mode == CLR_BUS || mode == CLR_ALL) {
    		busTF.setText("");
    	}
		
		if(mode == CLR_TIME || mode == CLR_ALL) {
    		
    		long time = 0;
        	Date date = new Date(0); 												// Create a Date object
        	 
        	Integer offset  = ZonedDateTime.now().getOffset().getTotalSeconds(); 	//Get DST Offset from local time in s
          	date.setTime(time - (offset*1000));										// Set date to 00:00:00, account for time-zone
        	jSpinner1.setValue(date);												

    	}
	
	}
	

	/*
	 * checkUserInput()
	 * 
	 * Validate user input using regex
	 * 
	 * Parameters:
	 * String userInput : User input string
	 * String regexStr : Regex pattern constant
	 * 
	 * Return:
	 * boolean true if match, false otherwise
	 *  
	 */
	
	private boolean checkUserInput(String userInput, String regexStr) {
				
		Pattern regexObj = Pattern.compile(regexStr);
		Matcher matchObj = regexObj.matcher(userInput);
		
		return matchObj.matches();
	}
	

	/*
	 * busPanelResultsConfig()
	 * timePanelResultsConfig()
	 * pathPanelResultsConfig()
	 * busPanelConfig()
	 * pathPanelConfig()
	 * timePanelConfig()
	 * menuPanelConfig()
	 * 
	 * Set up of individual GUI panels
	 * 
	 * Parameters:
	 * JPanel Objects
	 *  
	 * Return:
	 * <None>
	 *  
	 */
	
	private void busPanelResultsConfig(JPanel busPanelResults) {
		
		// Bus Panel Results Panel Components & Settings
		
		busPanelResults.setBackground(new Color(245, 245, 245));
		GridBagLayout gbl_busPanelResults = new GridBagLayout();
		gbl_busPanelResults.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_busPanelResults.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_busPanelResults.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_busPanelResults.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		busPanelResults.setLayout(gbl_busPanelResults);
		
		// Return Button 
		JButton btnReturnBusRes = new JButton("Return");
		btnReturnBusRes.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnReturnBusRes.setForeground(new Color(255, 255, 255));
		btnReturnBusRes.setBackground(new Color(32, 178, 170));
		btnReturnBusRes.addActionListener(this);
		GridBagConstraints gbc_btnReturnBusRes = new GridBagConstraints();
		gbc_btnReturnBusRes.insets = new Insets(0, 0, 5, 0);
		gbc_btnReturnBusRes.gridx = 13;
		gbc_btnReturnBusRes.gridy = 0;
		busPanelResults.add(btnReturnBusRes, gbc_btnReturnBusRes);
		
		// Results Label
		busResLbl = new JLabel();
		busResLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		GridBagConstraints gbc_busResLbl = new GridBagConstraints();
		gbc_busResLbl.insets = new Insets(0, 0, 5, 0);
		gbc_busResLbl.gridx = 5;
		gbc_busResLbl.gridy = 0;
		busPanelResults.add(busResLbl, gbc_busResLbl);
		
		// Scroll pane
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.gridwidth = 14;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		busPanelResults.add(scrollPane, gbc_scrollPane);
				
		// Text pane to display query results
		busPane = new JTextPane();
		busPane.setEditable(false);
		busPane.setContentType("text/html");
		scrollPane.setViewportView(busPane);
				
	}
	
	private void timePanelResultsConfig(JPanel timePanelResults) {
	
		// Time Panel Results Panel Components & Settings
		
		timePanelResults.setBackground(new Color(245, 245, 245));
		GridBagLayout gbl_timePanelResults = new GridBagLayout();
		gbl_timePanelResults.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_timePanelResults.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_timePanelResults.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_timePanelResults.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		timePanelResults.setLayout(gbl_timePanelResults);
		
		// Return Button
	    JButton btnReturnTimeRes = new JButton("Return");
	    btnReturnTimeRes.setFont(new Font("Tahoma", Font.BOLD, 20));
	    btnReturnTimeRes.setForeground(new Color(255, 255, 255));
	    btnReturnTimeRes.setBackground(new Color(32, 178, 170));
		btnReturnTimeRes.addActionListener(this);
		GridBagConstraints gbc_btnReturnTimeRes = new GridBagConstraints();
		gbc_btnReturnTimeRes.insets = new Insets(0, 0, 5, 0);
		gbc_btnReturnTimeRes.gridx = 13;
		gbc_btnReturnTimeRes.gridy = 0;
		timePanelResults.add(btnReturnTimeRes, gbc_btnReturnTimeRes);
		
		// Scroll pane
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.gridwidth = 14;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		timePanelResults.add(scrollPane, gbc_scrollPane);
		
		// Results Label
		timeResLbl = new JLabel();
		timeResLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		GridBagConstraints gbc_timeResLbl = new GridBagConstraints();
		gbc_timeResLbl.insets = new Insets(0, 0, 5, 0);
		gbc_timeResLbl.gridx = 5;
		gbc_timeResLbl.gridy = 0;
		timePanelResults.add(timeResLbl, gbc_timeResLbl);
				
		// Text pane
		timePane = new JTextPane();
		timePane.setContentType("text/html");
		timePane.setEditable(false);
		scrollPane.setViewportView(timePane);
			
	}
	
	private void pathPanelResultsConfig(JPanel pathPanelResults) {
		
		// Time Panel Results Panel Components & Settings
		
		pathPanelResults.setBackground(Color.WHITE);
		GridBagLayout gbl_pathPanelResults = new GridBagLayout();
		gbl_pathPanelResults.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pathPanelResults.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_pathPanelResults.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pathPanelResults.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pathPanelResults.setLayout(gbl_pathPanelResults);
		
		// Return button
		JButton btnReturnPathRes = new JButton("Return");
		btnReturnPathRes.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnReturnPathRes.setForeground(new Color(255, 255, 255));
		btnReturnPathRes.setBackground(new Color(65, 105, 225));
		btnReturnPathRes.addActionListener(this);
		GridBagConstraints gbc_btnReturnPathRes = new GridBagConstraints();
		gbc_btnReturnPathRes.insets = new Insets(0, 0, 5, 0);
		gbc_btnReturnPathRes.gridx = 13;
		gbc_btnReturnPathRes.gridy = 0;
		pathPanelResults.add(btnReturnPathRes, gbc_btnReturnPathRes);
		
		// Scroll pane
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.gridwidth = 14;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		pathPanelResults.add(scrollPane, gbc_scrollPane);
		
		// Results Label
		pathResLbl = new JLabel();
		pathResLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		GridBagConstraints gbc_pathResLbl = new GridBagConstraints();
		gbc_pathResLbl.insets = new Insets(0, 0, 5, 0);
		gbc_pathResLbl.gridx = 5;
		gbc_pathResLbl.gridy = 0;
		pathPanelResults.add(pathResLbl, gbc_pathResLbl);
		
		// Text pane
		pathPane = new JTextPane();
		pathPane.setContentType("text/html");
		pathPane.setEditable(false);
		scrollPane.setViewportView(pathPane);
		
	}

	private void busPanelConfig(JPanel busPanel) {
		
		// Layout
		busPanel.setBackground(new Color(245, 245, 245));
		GridBagLayout gbl_busPanel = new GridBagLayout();
		gbl_busPanel.columnWidths = new int[]{73, 195, 186, 185, 97, 0};
		gbl_busPanel.rowHeights = new int[]{35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_busPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_busPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		busPanel.setLayout(gbl_busPanel);
		
		// --Labels--
		
		// Search by bus lbl
		JLabel lblSearchByBus = new JLabel("Search by Bus Stop Name");
		lblSearchByBus.setOpaque(true);
		lblSearchByBus.setHorizontalAlignment(SwingConstants.CENTER);
		lblSearchByBus.setForeground(Color.WHITE);
		lblSearchByBus.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblSearchByBus.setBackground(new Color(135, 206, 235));
		
		GridBagConstraints gbc_lblSearchByBus = new GridBagConstraints();
		gbc_lblSearchByBus.fill = GridBagConstraints.BOTH;
		gbc_lblSearchByBus.gridwidth = 5;
		gbc_lblSearchByBus.insets = new Insets(0, 0, 5, 0);
		gbc_lblSearchByBus.gridx = 0;
		gbc_lblSearchByBus.gridy = 0;
		busPanel.add(lblSearchByBus, gbc_lblSearchByBus);
		
		// Bus keyword lbl
		JLabel busLbl = new JLabel("Enter Bus Stop Keyword");
		busLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		
		GridBagConstraints gbc_busLbl = new GridBagConstraints();
		gbc_busLbl.gridheight = 9;
		gbc_busLbl.anchor = GridBagConstraints.SOUTHWEST;
		gbc_busLbl.insets = new Insets(0, 0, 5, 5);
		gbc_busLbl.gridx = 1;
		gbc_busLbl.gridy = 1;
		busPanel.add(busLbl, gbc_busLbl);
		
		// Invisible lbl
		JLabel busInvisibleLbl = new JLabel("");
		GridBagConstraints gbc_busInvisibleLbl = new GridBagConstraints();
		gbc_busInvisibleLbl.fill = GridBagConstraints.HORIZONTAL;
		gbc_busInvisibleLbl.insets = new Insets(0, 0, 5, 0);
		gbc_busInvisibleLbl.gridx = 4;
		gbc_busInvisibleLbl.gridy = 15;
		busPanel.add(busInvisibleLbl, gbc_busInvisibleLbl);
		
		// --Buttons--
		
		// Return btn
		JButton btnReturnBus = new JButton("Main Menu");
		btnReturnBus.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnReturnBus.setForeground(new Color(255, 255, 255));
		btnReturnBus.setBackground(new Color(135, 206, 235));
		btnReturnBus.addActionListener(this);
		
		GridBagConstraints gbc_btnReturnBus = new GridBagConstraints();
		gbc_btnReturnBus.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnReturnBus.gridx = 4;
		gbc_btnReturnBus.gridy = 20;
		busPanel.add(btnReturnBus, gbc_btnReturnBus);
		
		// Clear tf btn
		JButton btnClearTFBus = new JButton("Clear Text");
		btnClearTFBus.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnClearTFBus.setForeground(new Color(255, 255, 255));
		btnClearTFBus.setBackground(new Color(65, 105, 225));
		btnClearTFBus.addActionListener(this);
		
		GridBagConstraints gbc_btnClearTFBus = new GridBagConstraints();
		gbc_btnClearTFBus.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClearTFBus.anchor = GridBagConstraints.NORTH;
		gbc_btnClearTFBus.gridheight = 2;
		gbc_btnClearTFBus.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearTFBus.gridx = 2;
		gbc_btnClearTFBus.gridy = 10;
		busPanel.add(btnClearTFBus, gbc_btnClearTFBus);
		
		// Search btn
		JButton btnSearchTFBus = new JButton("Search");
		btnSearchTFBus.setForeground(Color.WHITE);
		btnSearchTFBus.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnSearchTFBus.setBackground(new Color(65, 105, 225));
		btnSearchTFBus.addActionListener(this);
		
		GridBagConstraints gbc_btnSearchTFBus = new GridBagConstraints();
		gbc_btnSearchTFBus.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSearchTFBus.anchor = GridBagConstraints.NORTH;
		gbc_btnSearchTFBus.insets = new Insets(0, 0, 5, 5);
		gbc_btnSearchTFBus.gridx = 3;
		gbc_btnSearchTFBus.gridy = 10;
		busPanel.add(btnSearchTFBus, gbc_btnSearchTFBus);
		
		// --Text Fields--
		
		// Bus name tf
		busTF = new JTextField();
		busTF.setBackground(new Color(220, 220, 220));
		busTF.setFont(new Font("Tahoma", Font.PLAIN, 18));
		busTF.addActionListener(this);
		
		GridBagConstraints gbc_busTF = new GridBagConstraints();
		gbc_busTF.anchor = GridBagConstraints.SOUTH;
		gbc_busTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_busTF.gridwidth = 2;
		gbc_busTF.gridheight = 9;
		gbc_busTF.insets = new Insets(0, 0, 5, 5);
		gbc_busTF.gridx = 2;
		gbc_busTF.gridy = 1;
		busPanel.add(busTF, gbc_busTF);
		busTF.setColumns(10);
		
	}

	private void pathPanelConfig(JPanel pathPanel) {
		
		// Layout
		pathPanel.setBackground(new Color(245, 245, 245));
		GridBagLayout gbl_pathPanel = new GridBagLayout();
		gbl_pathPanel.columnWidths = new int[]{65, 161, 147, 113, 113, 0};
		gbl_pathPanel.rowHeights = new int[]{35, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pathPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pathPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pathPanel.setLayout(gbl_pathPanel);
		
		// --Text Fields--
		
		// Stop #1 tf
		pathTF1 = new JTextField();
		pathTF1.setBackground(new Color(220, 220, 220));
		pathTF1.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pathTF1.addActionListener(this);
		
		GridBagConstraints gbc_pathTF1 = new GridBagConstraints();
		gbc_pathTF1.anchor = GridBagConstraints.SOUTH;
		gbc_pathTF1.fill = GridBagConstraints.HORIZONTAL;
		gbc_pathTF1.gridwidth = 2;
		gbc_pathTF1.insets = new Insets(0, 0, 5, 5);
		gbc_pathTF1.gridx = 2;
		gbc_pathTF1.gridy = 7;
		pathPanel.add(pathTF1, gbc_pathTF1);
		pathTF1.setColumns(10);
		
		// Stop #2 tf
		pathTF2 = new JTextField();
		pathTF2.setBackground(new Color(220, 220, 220));
		pathTF2.setFont(new Font("Tahoma", Font.PLAIN, 22));
		pathTF2.addActionListener(this);
		
		GridBagConstraints gbc_pathTF2 = new GridBagConstraints();
		gbc_pathTF2.fill = GridBagConstraints.BOTH;
		gbc_pathTF2.gridwidth = 2;
		gbc_pathTF2.insets = new Insets(0, 0, 5, 5);
		gbc_pathTF2.gridx = 2;
		gbc_pathTF2.gridy = 8;
		pathPanel.add(pathTF2, gbc_pathTF2);
		pathTF2.setColumns(10);
		
		// --Labels--
		
		// Search for Shortest Paths lbl
		JLabel lblSearchForShortest = new JLabel("Search for Shortest Paths");
		lblSearchForShortest.setOpaque(true);
		lblSearchForShortest.setHorizontalAlignment(SwingConstants.CENTER);
		lblSearchForShortest.setForeground(Color.WHITE);
		lblSearchForShortest.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblSearchForShortest.setBackground(new Color(135, 206, 235));
		
		GridBagConstraints gbc_lblSearchForShortest = new GridBagConstraints();
		gbc_lblSearchForShortest.fill = GridBagConstraints.BOTH;
		gbc_lblSearchForShortest.gridwidth = 5;
		gbc_lblSearchForShortest.insets = new Insets(0, 0, 5, 0);
		gbc_lblSearchForShortest.gridx = 0;
		gbc_lblSearchForShortest.gridy = 0;
		pathPanel.add(lblSearchForShortest, gbc_lblSearchForShortest);
		
		// Bus stop #1 lbl
		JLabel pathLbl1 = new JLabel("Enter Bus Stop 1");
		pathLbl1.setFont(new Font("Tahoma", Font.BOLD, 20));
		
		GridBagConstraints gbc_pathLbl1 = new GridBagConstraints();
		gbc_pathLbl1.gridheight = 7;
		gbc_pathLbl1.anchor = GridBagConstraints.SOUTHWEST;
		gbc_pathLbl1.insets = new Insets(0, 0, 5, 5);
		gbc_pathLbl1.gridx = 1;
		gbc_pathLbl1.gridy = 1;
		pathPanel.add(pathLbl1, gbc_pathLbl1);
		
		// Bus stop #2 lbl
		JLabel pathLbl2 = new JLabel("Enter Bus Stop 2");
		pathLbl2.setFont(new Font("Tahoma", Font.BOLD, 20));
		
		GridBagConstraints gbc_pathLbl2 = new GridBagConstraints();
		gbc_pathLbl2.fill = GridBagConstraints.VERTICAL;
		gbc_pathLbl2.anchor = GridBagConstraints.WEST;
		gbc_pathLbl2.insets = new Insets(0, 0, 5, 5);
		gbc_pathLbl2.gridx = 1;
		gbc_pathLbl2.gridy = 8;
		pathPanel.add(pathLbl2, gbc_pathLbl2);
		
		// Return btn
		JButton btnPathReturn = new JButton("Main Menu");
		btnPathReturn.setFont(new Font("Tahoma", Font.PLAIN, 22));
		btnPathReturn.setForeground(new Color(255, 255, 255));
		btnPathReturn.setBackground(new Color(135, 206, 250));
		btnPathReturn.addActionListener(this);
		
		GridBagConstraints gbc_btnPathReturn = new GridBagConstraints();
		gbc_btnPathReturn.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnPathReturn.gridx = 4;
		gbc_btnPathReturn.gridy = 19;
		pathPanel.add(btnPathReturn, gbc_btnPathReturn);
		
		// Clear tfs btn
		JButton btnClearPath = new JButton("Clear All");
		btnClearPath.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnClearPath.setForeground(new Color(255, 255, 255));
		btnClearPath.setBackground(new Color(65, 105, 225));
		btnClearPath.addActionListener(this);
		
		GridBagConstraints gbc_btnClearPath = new GridBagConstraints();
		gbc_btnClearPath.anchor = GridBagConstraints.NORTH;
		gbc_btnClearPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClearPath.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearPath.gridx = 2;
		gbc_btnClearPath.gridy = 10;
		pathPanel.add(btnClearPath, gbc_btnClearPath);
		
		// Search btn
		JButton btnSearchPath = new JButton("Search");
		btnSearchPath.setForeground(Color.WHITE);
		btnSearchPath.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnSearchPath.setBackground(new Color(65, 105, 225));
		btnSearchPath.addActionListener(this);
		
		GridBagConstraints gbc_btnSearchPath = new GridBagConstraints();
		gbc_btnSearchPath.anchor = GridBagConstraints.NORTH;
		gbc_btnSearchPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSearchPath.insets = new Insets(0, 0, 5, 5);
		gbc_btnSearchPath.gridx = 3;
		gbc_btnSearchPath.gridy = 10;
		pathPanel.add(btnSearchPath, gbc_btnSearchPath);
		
		
	}

	private void timePanelConfig(JPanel timePanel) {
		
		// Layout
		timePanel.setBackground(new Color(245, 245, 245));
		GridBagLayout gbl_timePanel = new GridBagLayout();
		gbl_timePanel.columnWidths = new int[]{74, 227, 112, 137, 83, 97, 0};
		gbl_timePanel.rowHeights = new int[]{35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_timePanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_timePanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		timePanel.setLayout(gbl_timePanel);
		
		
		// JSpinner
		Date date = new Date();
		SpinnerDateModel sm = new SpinnerDateModel(date, null, null, Calendar.HOUR_OF_DAY);
		jSpinner1 = new javax.swing.JSpinner(sm);
		jSpinner1.setFont(new Font("Tahoma", Font.PLAIN, 22));
		JSpinner.DateEditor de = new JSpinner.DateEditor(jSpinner1, "HH:mm:ss");
		de.setBorder(new EmptyBorder(3, 3, 3, 3));
		de.setBackground(new Color(220, 220, 220));
		jSpinner1.setEditor(de);
				
		JSpinner.DefaultEditor editor;
		editor = (JSpinner.DefaultEditor) jSpinner1.getEditor();
		editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
						
		timePanel.add(jSpinner1);		
		GridBagConstraints gbc_jSpinner1 = new GridBagConstraints();
		gbc_jSpinner1.fill = GridBagConstraints.HORIZONTAL;
		gbc_jSpinner1.gridwidth = 2;
		gbc_jSpinner1.gridheight = 9;
		gbc_jSpinner1.anchor = GridBagConstraints.SOUTH;
		gbc_jSpinner1.insets = new Insets(0, 0, 5, 5);
		gbc_jSpinner1.gridx = 2;
		gbc_jSpinner1.gridy = 1;
		timePanel.add(jSpinner1, gbc_jSpinner1);
		
		// --Buttons--
		
		// Clear tf bn
		JButton btnClearTFtime = new JButton("Clear Time");
		btnClearTFtime.setForeground(new Color(255, 255, 255));
		btnClearTFtime.setBackground(new Color(65, 105, 225));
		btnClearTFtime.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnClearTFtime.addActionListener(this);
		
		GridBagConstraints gbc_btnClearTFtime = new GridBagConstraints();
		gbc_btnClearTFtime.anchor = GridBagConstraints.NORTH;
		gbc_btnClearTFtime.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearTFtime.gridx = 2;
		gbc_btnClearTFtime.gridy = 10;
		timePanel.add(btnClearTFtime, gbc_btnClearTFtime);
		
		// Search btn
		JButton btnSearchTFPath = new JButton("Search");
		btnSearchTFPath.addActionListener(this);
		btnSearchTFPath.setForeground(new Color(255, 255, 255));
		btnSearchTFPath.setBackground(new Color(65, 105, 225));
		btnSearchTFPath.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnSearchTFPath.addActionListener(this);
		
		GridBagConstraints gbc_btnSearchTFPath = new GridBagConstraints();
		gbc_btnSearchTFPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSearchTFPath.anchor = GridBagConstraints.NORTH;
		gbc_btnSearchTFPath.insets = new Insets(0, 0, 5, 5);
		gbc_btnSearchTFPath.gridx = 3;
		gbc_btnSearchTFPath.gridy = 10;
		timePanel.add(btnSearchTFPath, gbc_btnSearchTFPath);
		
		// Return btn
		JButton btnReturnTime = new JButton("Main Menu");
		btnReturnTime.setBackground(new Color(135, 206, 235));
		btnReturnTime.setForeground(new Color(255, 255, 255));
		btnReturnTime.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnReturnTime.addActionListener(this);
		
		GridBagConstraints gbc_btnReturnTime = new GridBagConstraints();
		gbc_btnReturnTime.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnReturnTime.gridx = 5;
		gbc_btnReturnTime.gridy = 21;
		timePanel.add(btnReturnTime, gbc_btnReturnTime);
		
		// --Labels--
		
		// Search by Arrival Time lbl
		JLabel lblSearchByTime = new JLabel("Search by Arrival Time");
		lblSearchByTime.setHorizontalAlignment(SwingConstants.CENTER);
		lblSearchByTime.setOpaque(true);
		lblSearchByTime.setForeground(Color.WHITE);
		lblSearchByTime.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblSearchByTime.setBackground(new Color(135, 206, 235));
		
		GridBagConstraints gbc_lblSearchByTime = new GridBagConstraints();
		gbc_lblSearchByTime.fill = GridBagConstraints.BOTH;
		gbc_lblSearchByTime.gridwidth = 6;
		gbc_lblSearchByTime.insets = new Insets(0, 0, 5, 0);
		gbc_lblSearchByTime.gridx = 0;
		gbc_lblSearchByTime.gridy = 0;
		timePanel.add(lblSearchByTime, gbc_lblSearchByTime);
		
		// Enter time lbl
		JLabel timeLbl = new JLabel("Enter Arrival Time");
		timeLbl.setBorder(new EmptyBorder(0, 0, 10, 0));
		timeLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
		
		GridBagConstraints gbc_timeLbl = new GridBagConstraints();
		gbc_timeLbl.gridheight = 3;
		gbc_timeLbl.insets = new Insets(0, 0, 5, 5);
		gbc_timeLbl.gridx = 1;
		gbc_timeLbl.gridy = 7;
		timePanel.add(timeLbl, gbc_timeLbl);
	}
	
	private void menuPanelConfig(JPanel menuPanel) {
		
		// Layout
		menuPanel.setBackground(new Color(245, 245, 245));
		GridBagLayout gbl_menuPanel = new GridBagLayout();
		gbl_menuPanel.columnWidths = new int[]{116, 151, 167, 187, 116, 0};
		gbl_menuPanel.rowHeights = new int[]{35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_menuPanel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_menuPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		menuPanel.setLayout(gbl_menuPanel);
		
		// --Labels--
		
		// Bus System lbl
		JLabel busSystemLbl = new JLabel("Bus Management System");
		busSystemLbl.setHorizontalAlignment(SwingConstants.CENTER);
		busSystemLbl.setFont(new Font("Tahoma", Font.BOLD, 22));
		busSystemLbl.setOpaque(true);
		busSystemLbl.setForeground(new Color(255, 255, 255));
		busSystemLbl.setBackground(new Color(135, 206, 235));
		
		GridBagConstraints gbc_busSystemLbl = new GridBagConstraints();
		gbc_busSystemLbl.fill = GridBagConstraints.BOTH;
		gbc_busSystemLbl.gridwidth = 5;
		gbc_busSystemLbl.insets = new Insets(0, 0, 5, 0);
		gbc_busSystemLbl.gridx = 0;
		gbc_busSystemLbl.gridy = 0;
		menuPanel.add(busSystemLbl, gbc_busSystemLbl);
		
		// --Buttons--
		
		// Quit btn
		JButton btnQuit = new JButton("Quit");
		btnQuit.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnQuit.setForeground(new Color(255, 255, 255));
		btnQuit.setBackground(new Color(135, 206, 235));
		btnQuit.addActionListener(this);
		
		GridBagConstraints gbc_btnQuit = new GridBagConstraints();
		gbc_btnQuit.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnQuit.gridx = 4;
		gbc_btnQuit.gridy = 9;
		menuPanel.add(btnQuit, gbc_btnQuit);
		
		// Arrival Time btn
		JButton btnArrivalTime = new JButton("Arrival Time");
		btnArrivalTime.setForeground(new Color(255, 255, 255));
		btnArrivalTime.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnArrivalTime.setBackground(new Color(65, 105, 225));
		btnArrivalTime.addActionListener(this);
		
		GridBagConstraints gbc_btnArrivalTime = new GridBagConstraints();
		gbc_btnArrivalTime.insets = new Insets(0, 0, 5, 5);
		gbc_btnArrivalTime.gridx = 1;
		gbc_btnArrivalTime.gridy = 2;
		menuPanel.add(btnArrivalTime, gbc_btnArrivalTime);
		
		//Shortest Path btn
		JButton btnShortestPath = new JButton("Shortest Path");
		btnShortestPath.setForeground(new Color(255, 255, 255));
		btnShortestPath.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnShortestPath.setBackground(new Color(65, 105, 225));
		btnShortestPath.addActionListener(this);
		
		GridBagConstraints gbc_btnShortestPath = new GridBagConstraints();
		gbc_btnShortestPath.insets = new Insets(0, 0, 5, 5);
		gbc_btnShortestPath.gridx = 2;
		gbc_btnShortestPath.gridy = 2;
		menuPanel.add(btnShortestPath, gbc_btnShortestPath);
		
		// Bus time btn
		JButton btnBusTime = new JButton("Bus Stop Name");
		btnBusTime.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnBusTime.setForeground(new Color(255, 255, 255));
		btnBusTime.setBackground(new Color(65, 105, 225));
		btnBusTime.addActionListener(this);
		
		GridBagConstraints gbc_btnBusTime = new GridBagConstraints();
		gbc_btnBusTime.insets = new Insets(0, 0, 5, 5);
		gbc_btnBusTime.gridx = 3;
		gbc_btnBusTime.gridy = 2;
		menuPanel.add(btnBusTime, gbc_btnBusTime);
		
		// Data path btn
		JButton btnDataPath = new JButton("Set Data Path");
		btnDataPath.addActionListener(this);
		btnDataPath.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnDataPath.setBackground(new Color(65, 105, 225));
		btnDataPath.setForeground(new Color(255, 255, 255));
		
		GridBagConstraints gbc_btnDataPath = new GridBagConstraints();
		gbc_btnDataPath.insets = new Insets(0, 0, 0, 5);
		gbc_btnDataPath.gridx = 0;
		gbc_btnDataPath.gridy = 9;
		menuPanel.add(btnDataPath, gbc_btnDataPath);
		
		// --Text Fields--
		
		// Data path tf
		pathField = new JTextField();
		pathField.setText(dataPath);
		pathField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		GridBagConstraints gbc_pathField = new GridBagConstraints();
		gbc_pathField.gridwidth = 3;
		gbc_pathField.insets = new Insets(0, 0, 0, 5);
		gbc_pathField.fill = GridBagConstraints.BOTH;
		gbc_pathField.gridx = 1;
		gbc_pathField.gridy = 9;
		menuPanel.add(pathField, gbc_pathField);
		pathField.setColumns(10);
	}

}
