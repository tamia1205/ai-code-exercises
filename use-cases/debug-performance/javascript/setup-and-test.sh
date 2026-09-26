#!/bin/bash

# setup-and-test.sh
# Script to initialize the database and test the getCustomerOrderDetails function

echo "=== Setting up and testing the Orders Service ==="
echo ""

# Check if PostgreSQL is running
echo "Step 1: Checking if PostgreSQL is running..."
if command -v docker &> /dev/null; then
  # Check if the orders-postgres container is running
  if docker ps | grep -q orders-postgres; then
    echo "PostgreSQL container is running."
  else
    echo "PostgreSQL container is not running. Starting it..."
    
    # Check if docker-compose is available
    if command -v docker-compose &> /dev/null; then
      echo "Starting PostgreSQL using docker-compose..."
      docker-compose up -d postgres
    else
      echo "Starting PostgreSQL using docker..."
      docker run -d --name orders-postgres -p 5432:5432 orders-service-db
    fi
    
    # Wait for PostgreSQL to start
    echo "Waiting for PostgreSQL to start..."
    sleep 10
  fi
else
  echo "Docker not found. Please make sure PostgreSQL is running manually."
  echo "You can start it using:"
  echo "  - docker-compose up -d postgres"
  echo "  - docker run -d --name orders-postgres -p 5432:5432 orders-service-db"
  echo "  - or start PostgreSQL manually"
  
  read -p "Press Enter to continue once PostgreSQL is running..."
fi

echo ""
echo "Step 2: Initializing the database with sample data..."

# Check if running inside Docker or locally
RUNNING_IN_DOCKER=false
if [ -f /.dockerenv ]; then
  RUNNING_IN_DOCKER=true
fi

# Set database connection environment variables
export DB_USER=app_user
export DB_PASSWORD=password123
export DB_NAME=ecommerce
export DB_PORT=5432

if [ "$RUNNING_IN_DOCKER" = true ]; then
  # Inside Docker container - execute scripts directly with service name as host
  export DB_HOST=postgres
  echo "Running inside Docker container"
  echo "Using database connection: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
  
  # Initialize the database
  PGPASSWORD=$DB_PASSWORD node init-db.js
  
  echo ""
  echo "Step 3: Testing the getCustomerOrderDetails function..."
  PGPASSWORD=$DB_PASSWORD node test-query.js
else
  # Local execution - use docker-compose exec to run inside the container
  echo "Running outside Docker container - using docker-compose exec"
  
  # Check if the app container is running - more reliable method
  APP_CONTAINER=$(docker-compose ps -q app)
  if [ -n "$APP_CONTAINER" ]; then
    echo "Using app container to execute scripts"
    
    # Initialize the database
    echo "Initializing database..."
    docker-compose exec -T app node init-db.js
    
    echo ""
    echo "Step 3: Testing the getCustomerOrderDetails function..."
    docker-compose exec -T app node test-query.js
  else
    echo "App container not running. Please start it with 'docker-compose up -d app'"
    exit 1
  fi
fi

echo ""
echo "=== Setup and test completed ==="