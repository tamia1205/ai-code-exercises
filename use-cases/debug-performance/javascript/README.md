# JavaScript Performance Example - Order Service

This example demonstrates performance optimization techniques for database operations in a Node.js application using PostgreSQL.

## Prerequisites
- Node.js 14.x or higher
- PostgreSQL database

## Dependencies
All dependencies are managed through the package.json file:
- `express` - Web server framework
- `pg` (node-postgres) - PostgreSQL client for Node.js

## Setup

### Option 1: Using Docker Compose (Recommended)

This repository includes Docker configuration files to quickly set up both the PostgreSQL database and the Node.js application.

```bash
# Start both the database and application with a single command
docker-compose up

# The database will be available with these default settings:
# - Host: localhost (or 'postgres' from within the application container)
# - Port: 5432
# - Database: ecommerce
# - Username: app_user
# - Password: password123

# The application will be available at:
# - http://localhost:3000
```

### Option 2: Using Docker for Database Only

If you prefer to run only the database in Docker and the application directly on your host:

```bash
# Build the Docker image
docker build -t orders-service-db .

# Run the PostgreSQL container
docker run -d --name orders-postgres -p 5432:5432 orders-service-db

# The database will be available with these default settings:
# - Host: localhost
# - Port: 5432
# - Database: ecommerce
# - Username: app_user
# - Password: password123
```

### Option 3: Manual Setup (No Docker)

If you prefer not to use Docker at all, you'll need to set up PostgreSQL manually:

1. Install PostgreSQL on your system
2. Create a database named 'ecommerce' (or your preferred name)
3. Create a user 'app_user' (or your preferred username) with appropriate permissions
4. Set up the environment variables to match your configuration

```bash
# Install dependencies
npm install

# Set up environment variables to match your PostgreSQL configuration
export DB_USER=app_user  # or your custom username
export DB_HOST=localhost
export DB_NAME=ecommerce  # or your custom database name
export DB_PASSWORD=password123  # or your custom password
export DB_PORT=5432

# Start the application
npm start
```

You'll also need to create the necessary database tables and indexes as described in the "Database Initialization" section.

## Running the Application

### With Docker Compose
If you're using Option 1 (Docker Compose), both the database and application will start automatically with:
```bash
docker-compose up
```

### With Docker for Database Only
If you're using Option 2 (Docker for Database Only), start the application with:
```bash
npm start
```
The application will connect to the Docker database with the default settings. No environment variables need to be set.

### Manual Setup
If you're using Option 3 (Manual Setup), start the application after setting the environment variables:
```bash
npm start
```

## Database Initialization

Regardless of which setup option you choose, you'll need to create the necessary database tables and indexes. The PostgreSQL server will be initialized with the correct user and database name, but the schema needs to be created separately.

### Option 1: Using the All-in-One Setup Script (Recommended)

This repository includes a shell script that will automatically:
1. Check if PostgreSQL is running and start it if needed
2. Initialize the database with all necessary tables, indexes, and sample data
3. Test the `getCustomerOrderDetails` function

```bash
# Make the script executable
chmod +x setup-and-test.sh

# Run the setup and test script
./setup-and-test.sh
```

### Option 2: Using the Individual Scripts

If you prefer to run the steps individually, you can use the provided Node.js scripts:

```bash
# Make sure the database is running first
# If using Docker Compose:
docker-compose up -d postgres

# If using Docker for Database Only:
docker run -d --name orders-postgres -p 5432:5432 orders-service-db

# Then run the initialization script
node init-db.js

# After initialization, test the getCustomerOrderDetails function
node test-query.js
```

The initialization script will:
1. Create all necessary tables (customers, products, addresses, orders, order_items, order_status_history)
2. Create recommended indexes for better performance
3. Insert sample data including:
   - 3 customers
   - 8 products
   - 5 addresses
   - 4 orders with items and status history

The test script will fetch and display all orders for customer ID 1 (John Doe) from January to December 2023.

### Option 3: Manual Database Setup

If you prefer to set up the database manually, you can connect to the PostgreSQL server and run the SQL commands yourself.

#### With Docker Compose or Docker for Database Only

```bash
# Connect to the PostgreSQL container
docker exec -it orders-postgres psql -U app_user -d ecommerce
```

#### With Manual Setup

```bash
# Connect to your PostgreSQL server
psql -U app_user -d ecommerce
```

#### Creating Tables and Indexes

Once connected to the database, create the necessary tables:

```sql
-- Create tables
CREATE TABLE customers (
  customer_id SERIAL PRIMARY KEY,
  customer_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE products (
  product_id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE addresses (
  address_id SERIAL PRIMARY KEY,
  street VARCHAR(100) NOT NULL,
  city VARCHAR(50) NOT NULL,
  state VARCHAR(50),
  postal_code VARCHAR(20) NOT NULL,
  country VARCHAR(50) NOT NULL
);

CREATE TABLE orders (
  order_id SERIAL PRIMARY KEY,
  customer_id INTEGER REFERENCES customers(customer_id),
  order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  shipping_address_id INTEGER REFERENCES addresses(address_id)
);

CREATE TABLE order_items (
  order_item_id SERIAL PRIMARY KEY,
  order_id INTEGER REFERENCES orders(order_id),
  product_id INTEGER REFERENCES products(product_id),
  quantity INTEGER NOT NULL,
  unit_price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE order_status_history (
  status_id SERIAL PRIMARY KEY,
  order_id INTEGER REFERENCES orders(order_id),
  status VARCHAR(20) NOT NULL,
  status_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  notes TEXT
);

-- Then create the recommended indexes as described in the "Database Optimization" section
```

## Performance Monitoring

### Using Node.js Profiling
```bash
# Run with profiling
node --prof app.js

# Process the log file
node --prof-process isolate-*.log > processed.txt
```

### Using Performance Hooks
```javascript
const { performance, PerformanceObserver } = require('perf_hooks');

const obs = new PerformanceObserver((list) => {
  const entries = list.getEntries();
  entries.forEach((entry) => {
    console.log(`${entry.name}: ${entry.duration}`);
  });
});
obs.observe({ entryTypes: ['measure'], buffered: true });
```

## Connection Pool Management
```javascript
const pool = new Pool({
  max: 20,               // Maximum pool size
  idleTimeoutMillis: 30000,  // Close idle connections
  connectionTimeoutMillis: 2000  // Connection timeout
});
```

## Monitoring Tools
- Node.js --prof profiler
- Node.js Performance Hooks
- PostgreSQL EXPLAIN ANALYZE
- pg-monitor for query monitoring
- New Relic or DataDog for production monitoring
