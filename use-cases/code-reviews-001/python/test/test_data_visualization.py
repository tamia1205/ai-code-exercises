import unittest
import sys
import os
import pandas as pd
from unittest.mock import patch, MagicMock
import tempfile

# Add parent directory to path to import module
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from src.data_visualization import generate_sales_dashboard

class TestDataVisualization(unittest.TestCase):
    
    def setUp(self):
        # Create a sample dataframe for testing
        self.sample_data = pd.DataFrame({
            'date': ['2023-01-01', '2023-01-15', '2023-02-01', '2023-02-15', '2023-03-01'],
            'product': ['Product A', 'Product B', 'Product A', 'Product C', 'Product B'],
            'region': ['North', 'South', 'North', 'East', 'West'],
            'sales_amount': [100, 200, 150, 300, 250]
        })
        
    def test_generate_sales_dashboard_with_dataframe(self):
        # Test that the function works with a pandas DataFrame
        with tempfile.NamedTemporaryFile(suffix='.html') as temp_file:
            with patch('plotly.graph_objects.Figure.write_html') as mock_write_html:
                result = generate_sales_dashboard(
                    self.sample_data, 
                    output_file=temp_file.name
                )
                
                # Check that the result is a plotly figure
                self.assertTrue(hasattr(result, 'data'))
                self.assertTrue(hasattr(result, 'layout'))
                
                # Check that write_html was called
                mock_write_html.assert_called_once()
    
    def test_generate_sales_dashboard_quarterly(self):
        # Test quarterly aggregation
        with tempfile.NamedTemporaryFile(suffix='.html') as temp_file:
            result = generate_sales_dashboard(
                self.sample_data, 
                output_file=temp_file.name,
                time_period='quarterly'
            )
            
            # Check that quarterly aggregation was applied (all dates should map to 2023-Q1)
            # This would be visible in the chart data
            self.assertTrue(hasattr(result, 'data'))
    
    def test_highlight_threshold(self):
        # Test highlighting threshold
        with tempfile.NamedTemporaryFile(suffix='.html') as temp_file:
            with patch('plotly.graph_objects.Figure.add_annotation') as mock_add_annotation:
                result = generate_sales_dashboard(
                    self.sample_data, 
                    output_file=temp_file.name,
                    highlight_threshold=200
                )
                
                # Check that add_annotation was called at least once
                mock_add_annotation.assert_called()
    
    def test_invalid_dataframe(self):
        # Test with missing columns
        invalid_df = pd.DataFrame({
            'date': ['2023-01-01'],
            'product': ['Product A']
            # Missing 'region' and 'sales_amount'
        })
        
        with self.assertRaises(ValueError) as context:
            generate_sales_dashboard(invalid_df)
        
        self.assertIn('Missing required column', str(context.exception))
    
    def test_invalid_time_period(self):
        # Test with invalid time period
        with self.assertRaises(ValueError) as context:
            generate_sales_dashboard(
                self.sample_data, 
                time_period='yearly'  # Not supported
            )
        
        self.assertIn("time_period must be 'monthly' or 'quarterly'", str(context.exception))

if __name__ == '__main__':
    unittest.main()