# test_stock_manager.py
import unittest
import io
import sys
from unittest.mock import patch

from stock_manager import print_inventory_report, main


class TestStockManager(unittest.TestCase):
    def test_print_inventory_report(self):
        # Test data
        items = [
            {"name": "Test Item 1", "quantity": 10},
            {"name": "Test Item 2", "quantity": 20}
        ]

        # Capture stdout
        captured_output = io.StringIO()
        sys.stdout = captured_output

        # This will crash with the bug in place
        try:
            print_inventory_report(items)
            result = captured_output.getvalue()
            self.assertIn("Test Item 1", result)
            self.assertIn("Test Item 2", result)
            self.assertIn("Quantity: 10", result)
            self.assertIn("Quantity: 20", result)
        except IndexError:
            self.fail("print_inventory_report raised IndexError unexpectedly!")
        finally:
            sys.stdout = sys.__stdout__

    def test_main_function(self):
        # Patch the print_inventory_report function to avoid the error
        with patch('stock_manager.print_inventory_report') as mock_print:
            main()
            # Check that print_inventory_report was called once
            self.assertEqual(mock_print.call_count, 1)

            # Check that it was called with the correct data
            args, _ = mock_print.call_args
            items = args[0]
            self.assertEqual(len(items), 3)  # Three items in the list
            self.assertEqual(items[0]["name"], "Laptop")
            self.assertEqual(items[1]["quantity"], 30)

if __name__ == '__main__':
    unittest.main()
