
package fop.assignment.pkgfinal.version;


public class Employee {
    String employeeID, name, password, role;

    public Employee(String employeeID, String name, String password, String role) {
        this.employeeID = employeeID;
        this.name = name;
        this.password = password;
        this.role = role;
    }
    public String getEmployeeID() { 
        return employeeID; 
    }
    public String getName() { 
        return name; 
    }
    public String getPassword() { 
        return password; 
    }
    public String getRole() { 
        return role; 
    }
    public String toCSV() { 
        return employeeID + "," + name + "," + password + "," + role; 
    }
}
