def generate_sales_dashboard(sales_data, output_file='sales_dashboard.html', time_period='monthly', highlight_threshold=None):
    """
    Generate a sales dashboard from the provided data.

    Args:
        sales_data: CSV file or DataFrame containing sales data
        output_file: HTML file to save the dashboard
        time_period: monthly or quarterly aggregation
        highlight_threshold: threshold for highlighting values
    """
    import pandas as pd
    import plotly.graph_objects as go
    from plotly.subplots import make_subplots
    import os

    # Load data
    if isinstance(sales_data, str):
        # Check if it's a CSV file
        if sales_data.endswith('.csv'):
            df = pd.read_csv(sales_data)
        else:
            raise ValueError("Unsupported file format. Only CSV files are supported.")
    elif isinstance(sales_data, pd.DataFrame):
        df = sales_data.copy()
    else:
        raise ValueError("sales_data must be a file path or a pandas DataFrame")

    # Ensure required columns exist
    required_columns = ['date', 'product', 'region', 'sales_amount']
    for col in required_columns:
        if col not in df.columns:
            raise ValueError(f"Missing required column: {col}")

    # Convert date to datetime
    df['date'] = pd.to_datetime(df['date'])

    # Aggregate data by time period
    if time_period == 'monthly':
        df['period'] = df['date'].dt.strftime('%Y-%m')
    elif time_period == 'quarterly':
        df['period'] = df['date'].dt.year.astype(str) + '-Q' + df['date'].dt.quarter.astype(str)
    else:
        raise ValueError("time_period must be 'monthly' or 'quarterly'")

    # Aggregate sales by period, product, and region
    period_sales = df.groupby(['period', 'product']).agg({'sales_amount': 'sum'}).reset_index()
    region_sales = df.groupby(['region', 'period']).agg({'sales_amount': 'sum'}).reset_index()

    # Create a figure with subplots
    fig = make_subplots(
        rows=2, cols=2,
        subplot_titles=("Sales by Period", "Sales by Product", "Sales by Region", "Top Products"),
        specs=[[{"type": "bar"}, {"type": "pie"}],
               [{"type": "bar"}, {"type": "table"}]]
    )

    # Add a bar chart for sales by period
    period_totals = df.groupby('period').agg({'sales_amount': 'sum'}).reset_index()
    fig.add_trace(
        go.Bar(x=period_totals['period'], y=period_totals['sales_amount'], name="Sales by Period"),
        row=1, col=1
    )

    # Add a pie chart for sales by product
    product_totals = df.groupby('product').agg({'sales_amount': 'sum'}).reset_index()
    fig.add_trace(
        go.Pie(labels=product_totals['product'], values=product_totals['sales_amount'], name="Sales by Product"),
        row=1, col=2
    )

    # Add a bar chart for sales by region
    fig.add_trace(
        go.Bar(x=region_sales['region'], y=region_sales['sales_amount'], name="Sales by Region"),
        row=2, col=1
    )

    # Add a table for top products
    top_products = product_totals.sort_values('sales_amount', ascending=False).head(5)
    fig.add_trace(
        go.Table(
            header=dict(values=["Product", "Sales Amount"]),
            cells=dict(values=[top_products['product'], top_products['sales_amount']])
        ),
        row=2, col=2
    )

    # Update layout
    fig.update_layout(
        title_text="Sales Dashboard",
        height=800,
        width=1200,
    )

    # Highlight values above threshold if provided
    if highlight_threshold is not None:
        for i, value in enumerate(period_totals['sales_amount']):
            if value > highlight_threshold:
                fig.add_annotation(
                    x=period_totals['period'][i],
                    y=value,
                    text="↑",
                    showarrow=False,
                    font=dict(size=20, color="red")
                )

    # Generate HTML file
    fig.write_html(output_file)
    print(f"Dashboard saved to {os.path.abspath(output_file)}")

    # Return figure for display
    return fig