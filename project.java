import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.Scanner;


public class project {
    public static String dbAddress = "jdbc:oracle:thin://@db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
    public static String dbUsername = "h062";
    public static String dbPassword = "jotLekHa";
    
    private static Date systemDate = Date.valueOf("2000-01-01");

    public static Connection connectToMySQL() {
        Connection con = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");

            con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
        } catch (ClassNotFoundException e) {
            System.out.println("[Error]: Java MySQL DB Driver not found!!");
            System.exit(0);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return con;
    }

    public static void main(String[] args) {
        // Using try-with-resources
        try (Connection con = connectToMySQL();
             Statement stmt = con.createStatement();      
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected to the Oracle server.");

            // Display Main Menu
            int choice = 0;

            while (choice != 5) {
                displayMainMenu();
                choice = getUserChoice(scanner);

                switch (choice) {
                    case 1:
                        // Perform system interface functions
                        System.out.println("You selected System interface.\n");
                        handleSystemInterface(scanner, stmt);
                        break;
                    case 2:
                        // Perform customer interface functions
                        System.out.println("You selected Customer interface.\n");
                        CustomerInterface.handleCustomerInterface(scanner, stmt);
                        break;
                    case 3:
                        // Perform bookstore interface functions
                        System.out.println("You selected Bookstore interface.\n");
                        BookstoreInterface.handleBookstoreInterface(scanner, stmt);
                        break;
                    case 4:
                        // Show system date
                        System.out.println("Today is " + systemDate);
                        break;
                    case 5:
                        // Quit the system
                        System.out.println("Quitting the system...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 5.");
                        break;
                }
                System.out.println();
            }

            // Close the scanner before exiting
            scanner.close();

        } catch (SQLException e) {
            System.out.println("Error executing SQL statement or connecting to the MySQL server: " + e.getMessage());
        }
    }

    private static void displayMainMenu() {
        System.out.println("The System Date is now: " + systemDate); // display system date
        System.out.println("<This is the Book Ordering System.>");
        System.out.println("-----------------------------------");
        System.out.println("1. System interface.");
        System.out.println("2. Customer interface.");
        System.out.println("3. Bookstore interface.");
        System.out.println("4. Show System Date.");
        System.out.println("5. Quit the system......");
        System.out.println();
        System.out.print("Please enter your choice??..");
    }

    private static int getUserChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        return choice;
    }

    private static void handleSystemInterface(Scanner scanner, Statement stmt) {
        int choice = 0;

        while (choice != 5) {
            displaySystemInterfaceMenu();
            choice = getUserChoice(scanner);

            switch (choice) {
                case 1:
                    createTable(stmt);
                    break;
                case 2:
                    deleteTable(stmt);
                    break;
                case 3:
                    insertTable(scanner, stmt);
                    break;
                case 4:
                    setSystemDate(scanner, stmt);
                    break;
                case 5:
                    System.out.println("Returning to the main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 5.");
                    break;
            }
            System.out.println();
        }
    }

    private static void displaySystemInterfaceMenu() {
        System.out.println("<This is the system interface.>");
        System.out.println("-------------------------------");
        System.out.println("1. Create Table.");
        System.out.println("2. Delete Table.");
        System.out.println("3. Insert Table.");
        System.out.println("4. Set System Date.");
        System.out.println("5. Back to main menu.");
        System.out.println();
        System.out.print("Please enter your choice: ");
    }

    private static void createTable(Statement stmt) {
        try {
            // Create book table
            String createBookTable = "CREATE TABLE book (" +
                    "ISBN CHAR(13) PRIMARY KEY, " +
                    "title VARCHAR2(100) NOT NULL, " +
                    "unit_price NUMBER(10) NOT NULL, " +
                    "no_of_copies NUMBER(10) NOT NULL)";
            stmt.executeUpdate(createBookTable);
            System.out.println("Created book table.");
    
            // Create customer table
            String createCustomerTable = "CREATE TABLE customer (" +
                    "customer_id VARCHAR2(10) PRIMARY KEY, " +
                    "name VARCHAR2(50) NOT NULL, " +
                    "shipping_address VARCHAR2(200) NOT NULL, " +
                    "credit_card_no CHAR(19))";
            stmt.executeUpdate(createCustomerTable);
            System.out.println("Created customer table.");
    
            // Create orders table
            String createOrdersTable = "CREATE TABLE orders (" +
                    "order_id CHAR(8) PRIMARY KEY, " +
                    "o_date DATE, " +
                    "shipping_status CHAR(1) CHECK (shipping_status IN ('Y','N')), " +
                    "charge NUMBER(10), " +
                    "customer_id VARCHAR2(10), " +
                    "FOREIGN KEY (customer_id) REFERENCES customer (customer_id))";
            stmt.executeUpdate(createOrdersTable);
            System.out.println("Created orders table.");
    
            // Create ordering table
            String createOrderingTable = "CREATE TABLE ordering (" +
                    "order_id CHAR(8), " +
                    "ISBN CHAR(13), " +
                    "quantity NUMBER(10), " +
                    "PRIMARY KEY (order_id, ISBN), " +
                    "FOREIGN KEY (order_id) REFERENCES orders (order_id), " + 
                    "FOREIGN KEY (ISBN) REFERENCES book (ISBN))";
            stmt.executeUpdate(createOrderingTable);
            System.out.println("Created ordering table.");
    
            // Create book_author table
            String createBookAuthorTable = "CREATE TABLE book_author (" +
                    "ISBN CHAR(13), " +
                    "author_name VARCHAR2(50), " +
                    "PRIMARY KEY (ISBN, author_name), " +
                    "FOREIGN KEY (ISBN) REFERENCES book (ISBN))";       
            stmt.executeUpdate(createBookAuthorTable);
            System.out.println("Created book_author table.");
    
        } catch (SQLException e) {
            System.out.println("Error creating all tables: " + e.getMessage());
        }
        
    }

    private static void deleteTable(Statement stmt) {
        try {
            String[] tableNames = {"book_author", "ordering", "orders", "book", "customer"};
    
            for (String tableName : tableNames) {
                // Execute the delete table SQL statement
                String deleteTableQuery = "DROP TABLE " + tableName;
                stmt.executeUpdate(deleteTableQuery);
                System.out.println("Table " + tableName + " deleted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting all tables: " + e.getMessage());
        }
    }

    private static void insertTable(Scanner scanner, Statement stmt) {
        try {
            System.out.print("Please enter the folder path: ");
            String folderPath = scanner.nextLine();
    
            // Load book data
            String bookFilePath = folderPath + "/book.txt";
            insertBookData(stmt, bookFilePath);
            
            // Load customer data
            String customerFilePath = folderPath + "/customer.txt";
            insertCustomerData(stmt, customerFilePath);
    
            // Load orders data
            String ordersFilePath = folderPath + "/orders.txt";
            insertOrdersData(stmt, ordersFilePath);
    
            // Load ordering data
            String orderingFilePath = folderPath + "/ordering.txt";
            insertOrderingData(stmt, orderingFilePath);
    
            // Load book_author data
            String bookAuthorFilePath = folderPath + "/book_author.txt";
            insertBookAuthorData(stmt, bookAuthorFilePath);
            
            System.out.println("Processing...Data is loaded!\n");
    
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
        }
    }
    
    private static void insertBookData(Statement stmt, String filePath) throws SQLException {
        String sql = "INSERT INTO book VALUES (?, ?, ?, ?)";
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmt = stmt.getConnection().prepareStatement(sql)) {
    
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                String ISBN = data[0];
                String title = data[1];
                int unit_price = Integer.parseInt(data[2]);
                int no_of_copies = Integer.parseInt(data[3]);
    
                pstmt.setString(1, ISBN);
                pstmt.setString(2, title);
                pstmt.setInt(3, unit_price);
                pstmt.setInt(4, no_of_copies);
    
                pstmt.executeUpdate();
            }
        } catch (IOException e) {
            System.out.println("Error reading book data file: " + e.getMessage());
        }
    }

    private static void insertCustomerData(Statement stmt, String filePath) throws SQLException {
        String sql = "INSERT INTO customer VALUES (?, ?, ?, ?)";
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmt = stmt.getConnection().prepareStatement(sql)) {
    
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                String customer_id = data[0];
                String name = data[1];
                String shipping_address = data[2];
                String credit_card_no = data[3];
    
                pstmt.setString(1, customer_id);
                pstmt.setString(2, name);
                pstmt.setString(3, shipping_address);
                pstmt.setString(4, credit_card_no);
    
                pstmt.executeUpdate();
            }
        } catch (IOException e) {
            System.out.println("Error reading customer data file: " + e.getMessage());
        }
    }

    private static void insertOrdersData(Statement stmt, String filePath) throws SQLException {
        String sql = "INSERT INTO orders VALUES (?, ?, ?, ?, ?)";
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmt = stmt.getConnection().prepareStatement(sql)) {
    
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                String order_id = data[0];
                java.sql.Date o_date = java.sql.Date.valueOf(data[1]); 
                String shipping_status = data[2];
                int charge = Integer.parseInt(data[3]);
                String customer_id = data[4];
    
                pstmt.setString(1, order_id);
                pstmt.setDate(2, o_date);
                pstmt.setString(3, shipping_status);
                pstmt.setInt(4, charge);
                pstmt.setString(5, customer_id);
    
                pstmt.executeUpdate();
            }
        } catch (IOException e) {
            System.out.println("Error reading orders data file: " + e.getMessage());
        }
    }

    private static void insertOrderingData(Statement stmt, String filePath) throws SQLException {
        String sql = "INSERT INTO ordering VALUES (?, ?, ?)";
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmt = stmt.getConnection().prepareStatement(sql)) {
    
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                String order_id = data[0];
                String ISBN = data[1];
                int quantity = Integer.parseInt(data[2]);
                
    
                pstmt.setString(1, order_id);
                pstmt.setString(2, ISBN);
                pstmt.setInt(3, quantity);
    
                pstmt.executeUpdate();
            }
        } catch (IOException e) {
            System.out.println("Error reading ordering data file: " + e.getMessage());
        }
    }

    private static void insertBookAuthorData(Statement stmt, String filePath) throws SQLException {
        String sql = "INSERT INTO book_author VALUES (?, ?)";
    
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmt = stmt.getConnection().prepareStatement(sql)) {
    
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                String ISBN = data[0];
                String author_name = data[1];
                
                pstmt.setString(1, ISBN);
                pstmt.setString(2, author_name);
    
                pstmt.executeUpdate();
            }
        } catch (IOException e) {
            System.out.println("Error reading book_author file: " + e.getMessage());
        }
    }
    
    
    /*private static void setSystemDate(Scanner scanner, Statement stmt) {
        java.sql.Date newSystemDate = null;
    
        try {
            // Retrieve the latest order date from the orders table
            ResultSet rs = stmt.executeQuery("SELECT MAX(o_date) FROM orders");
            if (rs.next()) {
                java.sql.Date latestOrderDate = rs.getDate(1);
                System.out.println("Latest date in orders: " + latestOrderDate);
    
                boolean isValid = false;
                while (!isValid) {
                    System.out.print("Please input the date (YYYYMMDD): ");
                    String dateString = scanner.nextLine();
    
                    // Validate the user input format
                    if (dateString.matches("\\d{8}")) {
                        // Parse the user input to a java.sql.Date object
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                        dateFormat.setLenient(false); // Disable lenient parsing
                        try {
                            java.util.Date parsedDate = dateFormat.parse(dateString);
                            newSystemDate = new java.sql.Date(parsedDate.getTime());
    
                            // Check if the new system date is on or after the latest order date
                            if (newSystemDate.compareTo(latestOrderDate) >= 0) {
                                // Update the system date
                                systemDate = newSystemDate;
                                System.out.println("Today is " + systemDate);
                                isValid = true;
                            } else {
                                System.out.println("Invalid date. The new system date must be on or after the latest order date.");
                            }
                        } catch (ParseException e) {
                            System.out.println("Invalid date. Please enter a valid date.");
                        }
                    } else {
                        System.out.println("Invalid date format. Please enter the date in the format YYYYMMDD.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving the latest order date: " + e.getMessage());
        }
    }*/
    private static void setSystemDate(Scanner scanner, Statement stmt) {
        java.sql.Date newSystemDate = null;
    
        try {
            // Retrieve the latest order date from the orders table
            ResultSet rs = stmt.executeQuery("SELECT MAX(o_date) FROM orders");
            if (rs.next()) {
                java.sql.Date latestOrderDate = rs.getDate(1);
                System.out.println("Latest date in orders: " + latestOrderDate);
    
                boolean isValid = false;
                while (!isValid) {
                    System.out.print("Please input the date (YYYYMMDD): ");
                    String dateString = scanner.nextLine();
    
                    // Validate the user input format
                    if (dateString.matches("\\d{8}")) {
                        // Parse the user input to a java.sql.Date object
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                        dateFormat.setLenient(false); // Disable lenient parsing
                        try {
                            java.util.Date parsedDate = dateFormat.parse(dateString);
                            newSystemDate = new java.sql.Date(parsedDate.getTime());
    
                            // Check if the new system date is on or after the latest order date
                            if (newSystemDate.compareTo(latestOrderDate) >= 0) {
                                // Check if the new system date is greater than the current system date
                                if (newSystemDate.compareTo(systemDate) > 0) {
                                    // Update the system date
                                    systemDate = newSystemDate;
                                    System.out.println("Today is " + systemDate);
                                    isValid = true;
                                } else {
                                    System.out.println("Invalid date. The new system date must be after the current system date.");
                                }
                            } else {
                                System.out.println("Invalid date. The new system date must be on or after the latest order date.");
                            }
                        } catch (ParseException e) {
                            System.out.println("Invalid date. Please enter a valid date.");
                        }
                    } else {
                        System.out.println("Invalid date format. Please enter the date in the format YYYYMMDD.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving the latest order date: " + e.getMessage());
        }
    } 

    public static Date currentDate() {
        return systemDate;
    }
}