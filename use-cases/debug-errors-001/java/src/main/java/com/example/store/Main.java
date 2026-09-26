// Main.java
package com.example.store;

public class Main {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem(new Product("Laptop", 999.99));
        cart.addItem(new Product("Mouse", 25.99));
        cart.addItem(null);  // Adding null here will cause the issue
        cart.addItem(new Product("Keyboard", 45.99));

        cart.checkout();
    }
}