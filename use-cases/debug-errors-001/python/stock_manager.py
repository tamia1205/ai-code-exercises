# stock_manager.py
def print_inventory_report(items):
    print("===== INVENTORY REPORT =====")
    # Error occurs in this loop - classic off-by-one error
    for i in range(len(items) + 1):  # Notice the + 1 here
        print(f"Item {i+1}: {items[i]['name']} - Quantity: {items[i]['quantity']}")
    print("============================")

def main():
    items = [
        {"name": "Laptop", "quantity": 15},
        {"name": "Mouse", "quantity": 30},
        {"name": "Keyboard", "quantity": 25}
    ]
    print_inventory_report(items)

if __name__ == "__main__":
    main()