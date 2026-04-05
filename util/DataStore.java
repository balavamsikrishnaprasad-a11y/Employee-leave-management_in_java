package util;

import java.sql.*;

public class DataStore {
    private static final String DB_URL = "jdbc:sqlite:hrms.db";
    private static Connection connection;

    public static void initializeDatabase() {
        try {
            // Suppress logging for SQLite
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
            System.setProperty("org.sqlite.skipLibraryLoading", "false");
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            // Set a busy timeout and enable WAL journal mode to reduce SQLITE_BUSY errors
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA busy_timeout = 10000");
                try (ResultSet rs = pragma.executeQuery("PRAGMA journal_mode = WAL")) {
                    // consume result
                    if (rs.next()) {
                        // journal_mode changed
                    }
                }
            } catch (SQLException ignore) {
                // If setting pragmas fails, continue — we'll rely on retries elsewhere
            }
            createTables();
            insertDefaultData();
            System.out.println("✓ Database initialized successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void createTables() throws SQLException {
        String employeeTable = "CREATE TABLE IF NOT EXISTS Employee (" +
                "employeeId INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "department TEXT NOT NULL," +
                "totalLeaveBalance INTEGER DEFAULT 20," +
                "password TEXT NOT NULL)";

        String adminTable = "CREATE TABLE IF NOT EXISTS Admin (" +
                "adminId INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL)";

        String leaveTable = "CREATE TABLE IF NOT EXISTS LeaveRequest (" +
                "leaveId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "employeeId INTEGER NOT NULL," +
                "employeeName TEXT NOT NULL," +
                "startDate TEXT NOT NULL," +
                "endDate TEXT NOT NULL," +
                "numberOfDays INTEGER NOT NULL," +
                "leaveType TEXT NOT NULL," +
                "reason TEXT," +
                "status TEXT DEFAULT 'Pending'," +
                "applicationDate TEXT NOT NULL," +
                "approvedBy TEXT," +
                "FOREIGN KEY (employeeId) REFERENCES Employee(employeeId))";

        Statement stmt = connection.createStatement();
        stmt.execute(employeeTable);
        stmt.execute(adminTable);
        stmt.execute(leaveTable);
        stmt.close();
    }

    private static void insertDefaultData() throws SQLException {
        // Check if data already exists
        String checkSql = "SELECT COUNT(*) FROM Employee";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(checkSql);
        rs.next();
        int count = rs.getInt(1);
        rs.close();

        if (count == 0) {
            String insertEmployees = "INSERT INTO Employee (employeeId, name, email, department, totalLeaveBalance, password) VALUES " +
                    "(101, 'Rahul Kumar', 'rahul@company.com', 'IT', 20, 'pass123')," +
                    "(102, 'Priya Singh', 'priya@company.com', 'HR', 20, 'pass123')," +
                    "(103, 'Amit Patel', 'amit@company.com', 'Finance', 18, 'pass123')," +
                    "(104, 'Neha Gupta', 'neha@company.com', 'IT', 20, 'pass123')";

            String insertAdmins = "INSERT INTO Admin (adminId, name, email, password) VALUES " +
                    "(1, 'Admin User', 'admin@company.com', 'admin123')";

            stmt.execute(insertEmployees);
            stmt.execute(insertAdmins);
            System.out.println("✓ Default data inserted successfully!");
        }
        stmt.close();
    }

    public static Connection getConnection() {
        if (connection == null) {
            initializeDatabase();
        }
        return connection;
    }

    /**
     * Open a new dedicated connection. Use for transactional operations to avoid
     * interference from the shared connection.
     */
    public static Connection getNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA busy_timeout = 10000");
            try (ResultSet rs = pragma.executeQuery("PRAGMA journal_mode = WAL")) {
                // consume result
                if (rs.next()) { }
            }
        } catch (SQLException ignore) {
        }
        return conn;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
