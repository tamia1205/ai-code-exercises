import sys
import unittest
from unittest.mock import patch
from io import StringIO

# Import the module to test
sys.path.append('src')
from database_connection import DatabaseConnection

class TestDatabaseConnection(unittest.TestCase):
    """Test cases for the DatabaseConnection class"""
    
    def setUp(self):
        """Set up test cases"""
        self.mysql_config = {
            'db_type': 'mysql',
            'host': 'localhost',
            'port': 3306,
            'username': 'test_user',
            'password': 'test_pass',
            'database': 'test_db'
        }
        
        self.postgres_config = {
            'db_type': 'postgresql',
            'host': 'pg.example.com',
            'port': 5432,
            'username': 'pg_user',
            'password': 'pg_pass',
            'database': 'pg_db'
        }
        
        self.mongodb_config = {
            'db_type': 'mongodb',
            'host': 'mongo.example.com',
            'port': 27017,
            'username': 'mongo_user',
            'password': 'mongo_pass',
            'database': 'mongo_db'
        }
        
        self.redis_config = {
            'db_type': 'redis',
            'host': 'redis.example.com',
            'port': 6379,
            'username': 'redis_user',
            'password': 'redis_pass',
            'database': 'redis_db'
        }
    
    def test_mysql_connection_string(self):
        """Test MySQL connection string generation"""
        conn = DatabaseConnection(**self.mysql_config)
        
        # Capture stdout to verify the connection string
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        # Check that the connection string is correctly formatted
        self.assertIn("mysql://test_user:test_pass@localhost:3306/test_db", output)
        self.assertIn("charset=utf8", output)
        
    def test_mysql_connection_with_ssl(self):
        """Test MySQL connection with SSL enabled"""
        conn = DatabaseConnection(**self.mysql_config, use_ssl=True)
        
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        self.assertIn("useSSL=true", output)
        
    def test_postgresql_connection_string(self):
        """Test PostgreSQL connection string generation"""
        conn = DatabaseConnection(**self.postgres_config)
        
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        self.assertIn("postgresql://pg_user:pg_pass@pg.example.com:5432/pg_db", output)
        
    def test_postgresql_connection_with_ssl(self):
        """Test PostgreSQL connection with SSL enabled"""
        conn = DatabaseConnection(**self.postgres_config, use_ssl=True)
        
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        self.assertIn("sslmode=require", output)
        
    def test_mongodb_connection_string(self):
        """Test MongoDB connection string generation"""
        conn = DatabaseConnection(**self.mongodb_config)
        
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        self.assertIn("mongodb://mongo_user:mongo_pass@mongo.example.com:27017/mongo_db", output)
        self.assertIn("retryAttempts=3", output)  # Default value
        self.assertIn("poolSize=5", output)  # Default value
        
    def test_mongodb_connection_with_custom_options(self):
        """Test MongoDB connection with custom retry and pool options"""
        conn = DatabaseConnection(**self.mongodb_config, retry_attempts=10, pool_size=20)
        
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        self.assertIn("retryAttempts=10", output)
        self.assertIn("poolSize=20", output)
        
    def test_redis_connection_string(self):
        """Test Redis connection string generation"""
        conn = DatabaseConnection(**self.redis_config)
        
        with patch('sys.stdout', new=StringIO()) as fake_out:
            conn.connect()
            output = fake_out.getvalue()
            
        self.assertIn("Redis Connection: redis.example.com:6379/redis_db", output)
        
    def test_unsupported_database_type(self):
        """Test that an unsupported database type raises a ValueError"""
        conn = DatabaseConnection(
            db_type='unsupported',
            host='test.example.com',
            port=1234,
            username='user',
            password='pass',
            database='db'
        )
        
        with self.assertRaises(ValueError) as context:
            conn.connect()
            
        self.assertIn("Unsupported database type", str(context.exception))

if __name__ == '__main__':
    unittest.main()