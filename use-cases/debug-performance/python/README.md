# Inventory Analyzer - Performance Optimization Challenge

A Python tool for analyzing product inventory to find combinations of products that match specific price targets.

## Features

- Find pairs of products whose combined price matches a target price within a specified margin
- Handles large product inventories efficiently
- Progress tracking for long-running analyses
- Sorted results by closest match to target price
- Duplicate combination prevention

## Usage

The main function `find_product_combinations` takes the following parameters:

```python
def find_product_combinations(products, target_price, price_margin=10):
    """
    Args:
        products: List of dictionaries with 'id', 'name', and 'price' keys
        target_price: The ideal combined price
        price_margin: Acceptable deviation from the target price
    """
```

### Example Usage

```python
product_list = [
    {'id': 1, 'name': 'Product 1', 'price': 100},
    {'id': 2, 'name': 'Product 2', 'price': 200},
    # ... more products
]

combinations = find_product_combinations(product_list, 500, 50)
```

Each result contains:
- Product 1 details
- Product 2 details
- Combined price
- Price difference from target

## How to run

`python inventory_analysis.py`

## Performance

- Capable of processing large inventories (5000+ products)
- Provides progress updates every 100 products
- Returns results sorted by closest match to target price