package common;

public class itemClass {
	
	public String name;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void decreaseQuantity() {
		this.quantity--;
	}


	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int quantity;
	public double price; 

	public itemClass(String n, int q, double p) {
		name = n;
		quantity = q;
		price = p;
	}
	
}
