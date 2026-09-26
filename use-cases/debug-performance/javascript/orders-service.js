// orders-service.js
const { Pool } = require('pg');

// Database connection - use environment variables with fallbacks
const pool = new Pool({
  user: process.env.DB_USER || 'app_user',
  host: process.env.DB_HOST || 'localhost',
  database: process.env.DB_NAME || 'ecommerce',
  password: process.env.DB_PASSWORD || 'password123',
  port: parseInt(process.env.DB_PORT || '5432'),
});

// Log connection info when service starts
console.log(`Database connection: ${process.env.DB_USER || 'app_user'}@${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || '5432'}/${process.env.DB_NAME || 'ecommerce'}`);

async function getCustomerOrderDetails(customerId, startDate, endDate) {
  try {
    // Query to get all order details for a customer
    const result = await pool.query(`
      SELECT
        o.order_id,
        o.order_date,
        o.total_amount,
        o.status,
        c.customer_name,
        c.email,
        (
          SELECT json_agg(
            json_build_object(
              'product_id', p.product_id,
              'product_name', p.name,
              'quantity', oi.quantity,
              'unit_price', p.price,
              'subtotal', (oi.quantity * p.price)
            )
          )
          FROM order_items oi
          JOIN products p ON oi.product_id = p.product_id
          WHERE oi.order_id = o.order_id
        ) as items,
        (
          SELECT 
            array_to_json(
              array_agg(
                json_build_object(
                  'status', s.status,
                  'date', s.status_date,
                  'notes', s.notes
                )
                ORDER BY s.status_date DESC
              )
            )
          FROM order_status_history s
          WHERE s.order_id = o.order_id
        ) as status_history,
        a.street,
        a.city,
        a.state,
        a.postal_code,
        a.country
      FROM orders o
      JOIN customers c ON o.customer_id = c.customer_id
      LEFT JOIN addresses a ON o.shipping_address_id = a.address_id
      WHERE o.customer_id = $1
        AND o.order_date BETWEEN $2 AND $3
      ORDER BY o.order_date DESC
    `, [customerId, startDate, endDate]);

    return result.rows;
  } catch (err) {
    console.error('Database query error:', err);
    throw err;
  }
}

// Example usage in Express route handler
async function getOrdersHandler(req, res) {
  try {
    const { customerId } = req.params;
    const { startDate = '2023-01-01', endDate = '2023-12-31' } = req.query;

    const orders = await getCustomerOrderDetails(customerId, startDate, endDate);

    res.json({
      success: true,
      count: orders.length,
      data: orders
    });
  } catch (error) {
    console.error('Error in getOrdersHandler:', error);
    res.status(500).json({
      success: false,
      error: 'An error occurred while fetching orders'
    });
  }
}

module.exports = {
  getCustomerOrderDetails,
  getOrdersHandler
};