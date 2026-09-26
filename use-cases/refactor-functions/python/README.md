# Sales Report Generator

This project contains a sales report generator that creates various types of sales reports based on provided sales data and parameters.

## Features

- Generate summary, detailed, or forecast sales reports
- Filter data by date range and custom criteria
- Group data by product, category, customer, or region
- Include charts and visualizations
- Output in various formats (PDF, Excel, HTML, JSON)

## Installation

No specific installation is required beyond a standard Python environment. Helper functions for generating reports in different formats are stubbed out in the current implementation.

## Usage

```python
from sales_report import generate_sales_report

# Sample data
sales_data = [
    {
        'id': '1001',
        'date': '2023-01-15',
        'amount': 250.00,
        'product': 'Laptop',
        'category': 'Electronics',
        'customer': 'John Smith',
        'region': 'North',
        'tax': 25.00,
        'cost': 175.00
    }
    # ... more sales data
]

# Generate a simple summary report
report = generate_sales_report(sales_data, output_format='json')

# Generate a detailed report with date range filtering
detailed_report = generate_sales_report(
    sales_data,
    report_type='detailed',
    date_range={'start': '2023-01-01', 'end': '2023-12-31'},
    output_format='json'
)

# Generate a forecast report with grouping by category
forecast_report = generate_sales_report(
    sales_data,
    report_type='forecast',
    grouping='category',
    include_charts=True,
    output_format='json'
)
```

## Running Tests

The project includes unit tests to verify the functionality of the sales report generator.

Run all tests:
```bash
python -m unittest test_sales_report
```

Run a specific test:
```bash
python -m unittest test_sales_report.TestSalesReport.test_summary_report
```

## Development

This project is designed as a refactoring exercise. The main `generate_sales_report` function in `sales_report.py` can be refactored into smaller, more focused functions to improve readability, maintainability, and testability.

## Refactoring Goals

- Break down the monolithic `generate_sales_report` function into smaller functions
- Improve code organization and separation of concerns
- Maintain the same API for backward compatibility
- Ensure all tests still pass after refactoring