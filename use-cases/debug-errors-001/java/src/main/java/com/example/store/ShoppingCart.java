// ShoppingCart.java
package com.example.store;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<Product> items;

    public ShoppingCart() {
        items = new ArrayList<>();
    }

    public void addItem(Product product) {
        items.add(product);
    }

    public double calculateTotal() {
        double total = 0;
        for (Product product : items) {
            // Error occurs when a null product is in the list
            total += product.getPrice();
        }
        return total;
    }

    public void checkout() {
        System.out.println("Total price: $" + calculateTotal());
        // Process payment etc.
    }
}