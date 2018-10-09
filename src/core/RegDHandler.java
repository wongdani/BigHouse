package core;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/** 
 * This class contains the functions used in fetching the regulation-D signals.
 */
public class RegDHandler {
    /**
     * An array containing the timestamps present in the regulation-D signal file.
     */
     private static ArrayList<Long> timeStamps = new ArrayList<Long>();

    /**
     * An array containing the regulation-D values present in the regulation-D signal file.
     */
     private static ArrayList<Double> regValues = new ArrayList<Double>();

    /**
     * Extracts regulation-D signals and corresponding timestamps from a file and populates
     * the respective arrays <timeStamps, regValues>
     *
     * @param fileName - the file from which the data will be extracted. NOTE: The file must be 
     * 			 present in the /workloads/ directory
     */
    public static void getRegDFromFile(String fileName) {
	String line = "";
	String csvSplitChar = ",";
	BufferedReader bufReader = null;
		
	String timeInSec = "";
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	try {
		bufReader = new BufferedReader(new FileReader("workloads/" + fileName));
		while( (line = bufReader.readLine()) != null) {
			String[] getLine = line.split(csvSplitChar);
			if(getLine[1].equals("RegDTest")) { //FIXME: prone to errors if file doesnt start with "RegDTest"
				continue; // ignore this line
			}
			else {
				//getLine[0] is in sec
				timeInSec = getLine[0];	
				date = sdf.parse("1970-01-01 " + timeInSec);
				
				timeStamps.add(date.getTime() - 28800000); // subtract for timezone diff
				regValues.add(Double.parseDouble(getLine[1]) );
			}
		}
	
		//System.out.println("File read complete, printing values:");
		/*for(int i = 0; i < timeStamps.size(); ++i) {
			System.out.println(timeStamps.get(i) + ", " + regValues.get(i));
		}*/
		System.out.println("End of values");
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		System.out.println("Problem reading date\n");
	}
    }
	
    /**
     * Gets the regulation-D signal corresponding to the given time
     *
     * @param time - the time for which we want to get the reg D signal
     *
     * @return the regD value corresponding to <time>
     */
     public static double getRegDSignal(long time) {
	if(regValues.isEmpty() || timeStamps.isEmpty()) {
		System.out.println("Error fetching regulation signal; array is empty!");
		System.exit(1);
	}
	boolean done = false;
	long diff = timeStamps.get(1) - timeStamps.get(0); // time difference between subsequent timeStamps
	long maxTime = timeStamps.get(timeStamps.size() - 1);
	long currentTime = 0;
	int index = 0;

	while(!done) {
		if( (currentTime > maxTime + (3*diff)) ) {
			System.out.println("Error: Unable to fetch accurate regualtion signal; maxTime exceeded!");
			System.out.println("maxTime is: " + maxTime + "\ndiff is: " + diff);
			System.exit(1);
		}
		if(currentTime > time) {
			// return reg signal corresponding to PREV timestamp
			return regValues.get(index - 1);
		}
		currentTime += diff; //increment currentTime to next timestamp
		++index;
	}
	System.out.println("Error: Unable to fetch regulation signal; while loop has exited"); // this should never be reached but just in case
	System.exit(1);
	return -1000.0;
    }
}
