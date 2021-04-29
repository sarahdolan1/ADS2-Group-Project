
public class TransfersAndTimes
{
   
    int fromStopId; 
    int toStopId; 
    int transferType; 
    int minTransferTime;

    TransfersAndTimes (int fromStopId, int toStopId, int transferType, int minTransferTime){
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.transferType = transferType;
        this.minTransferTime = minTransferTime;
    }

    //Used to sort arraylist
    public int compareFrom(TransfersAndTimes temp) {
        return this.fromStopId - temp.fromStopId;
    }
    ///Used to sort arraylist
    public int compareTo(TransfersAndTimes temp) { 
    	return this.toStopId - temp.toStopId; }
    }

   
