package fop.assignment.pkgfinal.version;

public class AttendanceLog {
    String date, time, employeeID, action;

    public AttendanceLog(String date, String time, String employeeID, String action) {
        this.date = date; 
        this.time = time; 
        this.employeeID = employeeID; 
        this.action = action;
    }
    public String toCSV() { 
        return date + "," + time + "," + employeeID + "," + action; 
    }
}
