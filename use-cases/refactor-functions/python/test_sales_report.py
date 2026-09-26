import unittest
from datetime import datetime
from sales_report import generate_sales_report

class TestSalesReport(unittest.TestCase):
    def setUp(self):
        # Sample sales data for testing
        self.sample_data = [
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
            },
            {
                'id': '1002',
                'date': '2023-01-20',
                'amount': 120.00,
                'product': 'Monitor',
                'category': 'Electronics',
                'customer': 'Jane Doe',
                'region': 'South',
                'tax': 12.00,
                'cost': 80.00
            },
            {
                'id': '1003',
                'date': '2023-02-05',
                'amount': 45.00,
                'product': 'Headphones',
                'category': 'Accessories',
                'customer': 'Bob Johnson',
                'region': 'East',
                'tax': 4.50,
                'cost': 22.00
            },
            {
                'id': '1004',
                'date': '2023-02-15',
                'amount': 180.00,
                'product': 'Smartphone',
                'category': 'Electronics',
                'customer': 'Jane Doe',
                'region': 'North',
                'tax': 18.00,
                'cost': 120.00
            }
        ]
    
    def test_summary_report(self):
        """Test generating a summary report"""
        result = generate_sales_report(
            self.sample_data,
            report_type='summary',
            output_format='json'
        )
        
        self.assertEqual(result['report_type'], 'summary')
        self.assertEqual(result['summary']['total_sales'], 595.00)
        self.assertEqual(result['summary']['transaction_count'], 4)
        self.assertEqual(result['summary']['average_sale'], 148.75)
    
    def test_date_range_filtering(self):
        """Test filtering by date range"""
        result = generate_sales_report(
            self.sample_data,
            report_type='summary',
            date_range={'start': '2023-02-01', 'end': '2023-02-28'},
            output_format='json'
        )
        
        self.assertEqual(result['summary']['transaction_count'], 2)
        self.assertEqual(result['summary']['total_sales'], 225.00)
    
    def test_additional_filters(self):
        """Test applying additional filters"""
        result = generate_sales_report(
            self.sample_data,
            filters={'category': 'Electronics'},
            output_format='json'
        )
        
        self.assertEqual(result['summary']['transaction_count'], 3)
        self.assertEqual(result['summary']['total_sales'], 550.00)
    
    def test_grouped_data(self):
        """Test grouping data by a field"""
        result = generate_sales_report(
            self.sample_data,
            grouping='category',
            output_format='json'
        )
        
        self.assertIn('grouping', result)
        self.assertEqual(result['grouping']['by'], 'category')
        self.assertIn('Electronics', result['grouping']['groups'])
        self.assertIn('Accessories', result['grouping']['groups'])
        self.assertEqual(result['grouping']['groups']['Electronics']['count'], 3)
        self.assertEqual(result['grouping']['groups']['Accessories']['count'], 1)

    def test_detailed_report(self):
        """Test generating a detailed report"""
        result = generate_sales_report(
            self.sample_data,
            report_type='detailed',
            output_format='json'
        )
        
        self.assertEqual(result['report_type'], 'detailed')
        self.assertIn('transactions', result)
        self.assertEqual(len(result['transactions']), 4)
        
        # Test calculated fields in detailed report
        transaction = result['transactions'][0]
        self.assertIn('pre_tax', transaction)
        self.assertIn('profit', transaction)
        self.assertIn('margin', transaction)
    
    def test_forecast_report(self):
        """Test generating a forecast report"""
        result = generate_sales_report(
            self.sample_data,
            report_type='forecast',
            output_format='json'
        )
        
        self.assertEqual(result['report_type'], 'forecast')
        self.assertIn('forecast', result)
        self.assertIn('monthly_sales', result['forecast'])
        self.assertIn('projected_sales', result['forecast'])
        
        # Ensure our months are present
        self.assertIn('2023-01', result['forecast']['monthly_sales'])
        self.assertIn('2023-02', result['forecast']['monthly_sales'])
    
    def test_include_charts(self):
        """Test including charts in the report"""
        result = generate_sales_report(
            self.sample_data,
            include_charts=True,
            output_format='json'
        )
        
        self.assertIn('charts', result)
        self.assertIn('sales_over_time', result['charts'])
        self.assertEqual(len(result['charts']['sales_over_time']['labels']), 4)
    
    def test_empty_data_after_filtering(self):
        """Test report generation when filters result in empty data"""
        result = generate_sales_report(
            self.sample_data,
            filters={'product': 'Non-existent Product'},
            output_format='json'
        )
        
        self.assertEqual(result['message'], 'No data matches the specified criteria')
        self.assertEqual(result['data'], [])

if __name__ == '__main__':
    unittest.main()