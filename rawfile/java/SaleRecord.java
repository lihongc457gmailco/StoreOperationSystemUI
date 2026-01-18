package fop.assignment.pkgfinal.version;

public class SaleRecord {

    String date, time, customerName, modelName, paymentMethod, employeeID;
    int quantity;
    double totalPrice;
    
    public SaleRecord(String date, String time, String customerName, String modelName, int quantity, double totalPrice, String paymentMethod, String employeeID) {
        this.date = date; 
        this.time = time; 
        this.customerName = customerName; 
        this.modelName = modelName;
        this.quantity = quantity; 
        this.totalPrice = totalPrice; 
        this.paymentMethod = paymentMethod; 
        this.employeeID = employeeID;
    }
    
    public String getDate() { 
        return date; 
    }
    public String getCustomerName() { 
        return customerName; 
    }
    public String getModelName() { 
        return modelName; 
    }
    public double getTotalPrice() { 
        return totalPrice; 
    }
    public String getEmployeeID() { 
        return employeeID; 
    }
    public void setPaymentMethod(String m) { 
        this.paymentMethod = m; 
    }
    public String getTime() { 
        return time;
    }
    public int getQuantity() { 
        return quantity;
    }
    public String getPaymentMethod() { 
        return paymentMethod; 
    }
    public void setCustomerName(String name){
        this.customerName = name;
    }
    public void setModelName(String model){
        this.modelName = model;
    }
    public void setQuantity(int qty){
        this.quantity = qty;
    }
    public void setTotalPrice(double price){
        this.totalPrice = price;
    }
    
   public String toCSV() {
        return String.join(",", date, time, customerName, modelName, String.valueOf(quantity), String.valueOf(totalPrice), paymentMethod, employeeID);
    }
    public String toString() { 
        return date + " | " + customerName + " | " + modelName + " | RM" + totalPrice; 
    }
}

