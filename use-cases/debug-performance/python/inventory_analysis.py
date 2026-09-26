# inventory_analysis.py
def find_product_combinations(products, target_price, price_margin=10):
    """
    Find all pairs of products where the combined price is within
    the target_price ± price_margin range.

    Args:
        products: List of dictionaries with 'id', 'name', and 'price' keys
        target_price: The ideal combined price
        price_margin: Acceptable deviation from the target price

    Returns:
        List of dictionaries with product pairs and their combined price
    """
    results = []

    # For each possible pair of products
    for i in range(len(products)):
        if i % 100 == 0:
            print(f"Processing product {i+1} of {len(products)}")
        for j in range(len(products)):
            # Skip comparing a product with itself
            if i != j:
                product1 = products[i]
                product2 = products[j]

                # Calculate combined price
                combined_price = product1['price'] + product2['price']

                # Check if the combined price is within the target range
                if (target_price - price_margin) <= combined_price <= (target_price + price_margin):
                    # Avoid duplicates like (product1, product2) and (product2, product1)
                    if not any(r['product1']['id'] == product2['id'] and
                               r['product2']['id'] == product1['id'] for r in results):

                        pair = {
                            'product1': product1,
                            'product2': product2,
                            'combined_price': combined_price,
                            'price_difference': abs(target_price - combined_price)
                        }
                        results.append(pair)

    # Sort by price difference from target
    results.sort(key=lambda x: x['price_difference'])
    return results

# Example usage
if __name__ == "__main__":
    import time
    import random

    # Generate a large list of products
    print("Generating Product List")
    product_list = []
    for i in range(5000):
        product_list.append({
            'id': i,
            'name': f'Product {i}',
            'price': random.randint(5, 500)
        })

    # Measure execution time
    print(f"Finding product combinations for {len(product_list)} products")
    start_time = time.time()
    combinations = find_product_combinations(product_list, 500, 50)
    end_time = time.time()

    print(f"Found {len(combinations)} product combinations")
    print(f"Execution time: {end_time - start_time:.2f} seconds")