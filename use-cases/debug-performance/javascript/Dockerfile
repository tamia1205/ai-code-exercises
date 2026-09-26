FROM postgres:latest

# Set environment variables for PostgreSQL
ENV POSTGRES_USER=app_user
ENV POSTGRES_PASSWORD=password123
ENV POSTGRES_DB=ecommerce

# Expose PostgreSQL port
EXPOSE 5432

# Health check to verify the database is ready
HEALTHCHECK --interval=5s --timeout=5s --retries=5 \
  CMD pg_isready -U app_user -d ecommerce || exit 1