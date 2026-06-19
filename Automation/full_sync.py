import pandas as pd
import pymysql
import snowflake.connector
import os

# --- 1. CONFIGURATION ---
mysql_config = {
    'user': 'root',
    'password': 'Cloud@123$',
    'host': 'localhost',
    'database': 'mts',
    'charset': 'utf8mb4'
}

sf_config = {
    'user': 'DEEPCHAN04',
    'password': 'Brasil@@1011810118',
    'account': 'HNSUABX-UB74610', 
    'warehouse': 'ANALYTICS_WH',
    'database': 'ANALYTICS_DB',
    'schema': 'ANALYTICS_SCHEMA'
}

# Path for the intermediate file
export_folder = r'.\Uploads'
export_file = os.path.join(export_folder, 'transaction.csv')
if not os.path.exists(export_folder):
    os.makedirs(export_folder)
    print(f"Created directory: {export_folder}")

try:
    # --- PHASE 1: MYSQL TO CSV ---
    print("Connecting to MySQL...")
    mysql_conn = pymysql.connect(**mysql_config)
    
    print("Fetching data from MySQL...")
    query = "SELECT * FROM transaction"
    df = pd.read_sql(query, mysql_conn)
    mysql_conn.close()
    
    print(f"Read {len(df)} rows. Exporting to CSV: {export_file}...")
    # Writing CSV (Overwrites existing, no index, no header)
    df.to_csv(
        export_file, 
        index=False, 
        header=False, 
        sep=',', 
        quoting=1, 
        lineterminator='\n'
    )
    print("CSV creation successful.")

    # --- PHASE 2: CSV TO SNOWFLAKE ---
    if os.path.exists(export_file):
        print("\nConnecting to Snowflake...")
        sf_conn = snowflake.connector.connect(**sf_config)
        cursor = sf_conn.cursor()

        table_name = "MTS_TRANSACTION"
        # Format path for Snowflake (forward slashes)
        clean_path = export_file.replace("\\", "/")
        snowflake_path = f"file://{clean_path}"

        print(f"Uploading {export_file} to Snowflake Stage...")
        cursor.execute(f"PUT '{snowflake_path}' @%{table_name} OVERWRITE=TRUE")

        print(f"Loading data into {table_name} table...")
        # Optional: Uncomment the next line if you want to clear the table first
        # cursor.execute(f"TRUNCATE TABLE {table_name}")
        
        cursor.execute(f"""
            COPY INTO {table_name}
            FILE_FORMAT = (
                TYPE = 'CSV'
                SKIP_HEADER = 0
                FIELD_OPTIONALLY_ENCLOSED_BY = '\"'
                ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE
                EMPTY_FIELD_AS_NULL = TRUE
                NULL_IF = ('NULL', 'null', '')
            )
            ON_ERROR = 'CONTINUE'
        """)
        
        print(f"[SUCCESS] Sync complete for {table_name}!")
        cursor.close()
        sf_conn.close()
    else:
        print("[ERROR] CSV file was not found. Skipping Snowflake upload.")

except Exception as e:
    print(f"\n[CRITICAL ERROR] {str(e)}")