// init-db.js
// Script to initialize the database schema and populate it with sample data
const { Pool } = require('pg');

// Database connection - use environment variables with fallbacks
const pool = new Pool({
  user: process.env.DB_USER || 'app_user',
  host: process.env.DB_HOST || 'localhost',
  database: process.env.DB_NAME || 'ecommerce',
  password: process.env.DB_PASSWORD || 'password123',
  port: parseInt(process.env.DB_PORT || '5432'),
});

// Log connection info for debugging
console.log(`Connecting to database: ${process.env.DB_USER || 'app_user'}@${process.env.DB_HOST || 'localhost'}:${process.env.DB_PORT || '5432'}/${process.env.DB_NAME || 'ecommerce'}`);

async function initializeDatabase() {
  const client = await pool.connect();
  
  try {
    console.log('Starting database initialization...');
    
    // Begin transaction
    await client.query('BEGIN');
    
    // Drop existing tables if they exist (in reverse order of dependencies)
    console.log('Dropping existing tables if they exist...');
    await client.query(`
      DROP TABLE IF EXISTS order_status_history CASCADE;
      DROP TABLE IF EXISTS order_items CASCADE;
      DROP TABLE IF EXISTS orders CASCADE;
      DROP TABLE IF EXISTS addresses CASCADE;
      DROP TABLE IF EXISTS products CASCADE;
      DROP TABLE IF EXISTS customers CASCADE;
    `);
    
    // Create tables
    console.log('Creating tables...');
    
    // Customers table
    await client.query(`
      CREATE TABLE customers (
        customer_id SERIAL PRIMARY KEY,
        customer_name VARCHAR(100) NOT NULL,
        email VARCHAR(100) UNIQUE NOT NULL
      );
    `);
    
    // Products table
    await client.query(`
      CREATE TABLE products (
        product_id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        price DECIMAL(10, 2) NOT NULL
      );
    `);
    
    // Addresses table
    await client.query(`
      CREATE TABLE addresses (
        address_id SERIAL PRIMARY KEY,
        street VARCHAR(100) NOT NULL,
        city VARCHAR(50) NOT NULL,
        state VARCHAR(50),
        postal_code VARCHAR(20) NOT NULL,
        country VARCHAR(50) NOT NULL
      );
    `);
    
    // Orders table
    await client.query(`
      CREATE TABLE orders (
        order_id SERIAL PRIMARY KEY,
        customer_id INTEGER REFERENCES customers(customer_id),
        order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        total_amount DECIMAL(10, 2) NOT NULL,
        status VARCHAR(20) NOT NULL,
        shipping_address_id INTEGER REFERENCES addresses(address_id)
      );
    `);
    
    // Order items table
    await client.query(`
      CREATE TABLE order_items (
        order_item_id SERIAL PRIMARY KEY,
        order_id INTEGER REFERENCES orders(order_id),
        product_id INTEGER REFERENCES products(product_id),
        quantity INTEGER NOT NULL,
        unit_price DECIMAL(10, 2) NOT NULL
      );
    `);
    
    // Order status history table
    await client.query(`
      CREATE TABLE order_status_history (
        status_id SERIAL PRIMARY KEY,
        order_id INTEGER REFERENCES orders(order_id),
        status VARCHAR(20) NOT NULL,
        status_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        notes TEXT
      );
    `);
    
    // Create indexes
    console.log('Creating indexes...');
    await client.query(`
      CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date);
      CREATE INDEX idx_order_items_order ON order_items(order_id);
      CREATE INDEX idx_products_id ON products(product_id);
    `);
    
    // Insert sample data
    console.log('Inserting sample data...');
    
    // Insert customers
    const customersResult = await client.query(`
      INSERT INTO customers (customer_name, email) VALUES
        ('John Doe', 'john.doe@example.com'),
        ('Jane Smith', 'jane.smith@example.com'),
        ('Bob Johnson', 'bob.johnson@example.com')
      RETURNING customer_id;
    `);
    
    // Insert products
    const productsResult = await client.query(`
      INSERT INTO products (name, price) VALUES
        ('Laptop', 1200.00),
        ('Smartphone', 800.00),
        ('Headphones', 150.00),
        ('Tablet', 500.00),
        ('Monitor', 300.00),
        ('Keyboard', 80.00),
        ('Mouse', 40.00),
        ('Speakers', 120.00)
      RETURNING product_id;
    `);
    
    // Insert addresses
    const addressesResult = await client.query(`
      INSERT INTO addresses (street, city, state, postal_code, country) VALUES
        ('123 Main St', 'New York', 'NY', '10001', 'USA'),
        ('456 Elm St', 'Los Angeles', 'CA', '90001', 'USA'),
        ('789 Oak St', 'Chicago', 'IL', '60601', 'USA'),
        ('321 Pine St', 'San Francisco', 'CA', '94101', 'USA'),
        ('654 Maple St', 'Boston', 'MA', '02101', 'USA')
      RETURNING address_id;
    `);
    
    // Insert orders for the first customer (John Doe)
    const johnDoeId = customersResult.rows[0].customer_id;
    
    // Order 1 - Placed in January 2023
    const order1Result = await client.query(`
      INSERT INTO orders (customer_id, order_date, total_amount, status, shipping_address_id) VALUES
        ($1, '2023-01-15 10:30:00', 1280.00, 'Delivered', $2)
      RETURNING order_id;
    `, [johnDoeId, addressesResult.rows[0].address_id]);
    
    const order1Id = order1Result.rows[0].order_id;
    
    // Order 1 items
    await client.query(`
      INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
        ($1, $2, 1, 1200.00),
        ($1, $3, 2, 40.00)
    `, [order1Id, productsResult.rows[0].product_id, productsResult.rows[6].product_id]);
    
    // Order 1 status history
    await client.query(`
      INSERT INTO order_status_history (order_id, status, status_date, notes) VALUES
        ($1, 'Placed', '2023-01-15 10:30:00', 'Order placed successfully'),
        ($1, 'Processing', '2023-01-16 09:15:00', 'Payment confirmed'),
        ($1, 'Shipped', '2023-01-17 14:20:00', 'Package shipped via Express'),
        ($1, 'Delivered', '2023-01-19 11:45:00', 'Package delivered')
    `, [order1Id]);
    
    // Order 2 - Placed in March 2023
    const order2Result = await client.query(`
      INSERT INTO orders (customer_id, order_date, total_amount, status, shipping_address_id) VALUES
        ($1, '2023-03-22 14:45:00', 950.00, 'Shipped', $2)
      RETURNING order_id;
    `, [johnDoeId, addressesResult.rows[0].address_id]);
    
    const order2Id = order2Result.rows[0].order_id;
    
    // Order 2 items
    await client.query(`
      INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
        ($1, $2, 1, 800.00),
        ($1, $3, 1, 150.00)
    `, [order2Id, productsResult.rows[1].product_id, productsResult.rows[2].product_id]);
    
    // Order 2 status history
    await client.query(`
      INSERT INTO order_status_history (order_id, status, status_date, notes) VALUES
        ($1, 'Placed', '2023-03-22 14:45:00', 'Order placed successfully'),
        ($1, 'Processing', '2023-03-23 10:30:00', 'Payment confirmed'),
        ($1, 'Shipped', '2023-03-24 16:10:00', 'Package shipped via Standard')
    `, [order2Id]);
    
    // Order 3 - Placed in June 2023
    const order3Result = await client.query(`
      INSERT INTO orders (customer_id, order_date, total_amount, status, shipping_address_id) VALUES
        ($1, '2023-06-10 09:15:00', 500.00, 'Delivered', $2)
      RETURNING order_id;
    `, [johnDoeId, addressesResult.rows[0].address_id]);
    
    const order3Id = order3Result.rows[0].order_id;
    
    // Order 3 items
    await client.query(`
      INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
        ($1, $2, 1, 500.00)
    `, [order3Id, productsResult.rows[3].product_id]);
    
    // Order 3 status history
    await client.query(`
      INSERT INTO order_status_history (order_id, status, status_date, notes) VALUES
        ($1, 'Placed', '2023-06-10 09:15:00', 'Order placed successfully'),
        ($1, 'Processing', '2023-06-11 11:20:00', 'Payment confirmed'),
        ($1, 'Shipped', '2023-06-12 13:45:00', 'Package shipped via Express'),
        ($1, 'Delivered', '2023-06-14 10:30:00', 'Package delivered')
    `, [order3Id]);
    
    // Insert orders for the second customer (Jane Smith)
    const janeSmithId = customersResult.rows[1].customer_id;
    
    // Order 4 - Placed in February 2023
    const order4Result = await client.query(`
      INSERT INTO orders (customer_id, order_date, total_amount, status, shipping_address_id) VALUES
        ($1, '2023-02-05 16:20:00', 420.00, 'Delivered', $2)
      RETURNING order_id;
    `, [janeSmithId, addressesResult.rows[1].address_id]);
    
    const order4Id = order4Result.rows[0].order_id;
    
    // Order 4 items
    await client.query(`
      INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
        ($1, $2, 1, 300.00),
        ($1, $3, 1, 80.00),
        ($1, $4, 1, 40.00)
    `, [order4Id, productsResult.rows[4].product_id, productsResult.rows[5].product_id, productsResult.rows[6].product_id]);
    
    // Order 4 status history
    await client.query(`
      INSERT INTO order_status_history (order_id, status, status_date, notes) VALUES
        ($1, 'Placed', '2023-02-05 16:20:00', 'Order placed successfully'),
        ($1, 'Processing', '2023-02-06 09:30:00', 'Payment confirmed'),
        ($1, 'Shipped', '2023-02-07 14:15:00', 'Package shipped via Standard'),
        ($1, 'Delivered', '2023-02-10 11:20:00', 'Package delivered')
    `, [order4Id]);
    
    // Commit transaction
    await client.query('COMMIT');
    
    console.log('Database initialization completed successfully!');
    console.log('Sample data summary:');
    console.log('- 3 customers created');
    console.log('- 8 products created');
    console.log('- 5 addresses created');
    console.log('- 4 orders created (3 for John Doe, 1 for Jane Smith)');
    console.log('- Each order has items and status history');
    console.log('\nYou can now test the getCustomerOrderDetails function with:');
    console.log(`- Customer ID: ${johnDoeId} (John Doe)`);
    console.log('- Start Date: 2023-01-01');
    console.log('- End Date: 2023-12-31');
    
  } catch (err) {
    // Rollback transaction in case of error
    await client.query('ROLLBACK');
    console.error('Error during database initialization:', err);
    throw err;
  } finally {
    // Release client back to the pool
    client.release();
    await pool.end();
  }
}

// Run the initialization function
initializeDatabase().catch(err => {
  console.error('Failed to initialize database:', err);
  process.exit(1);
});