package fop.assignment.pkgfinal.version;

public class WatchModel {
    String modelName;
    double price;
    int quantity;

    public WatchModel(String modelName, double price, int quantity) {
        this.modelName = modelName;
        this.price = price;
        this.quantity = quantity;
    }
    public String getModelName() { 
        return modelName; 
    }
    public double getPrice() { 
        return price; 
    }
    public int getQuantity() { 
        return quantity; 
    }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }
    public String toCSV() { 
        return modelName + "," + price + "," + quantity; 
    }
    public String toString() { 
        return modelName + " | RM" + price + " | Qty: " + quantity; 
    }
}
