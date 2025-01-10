import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BookstoreInterface {
    public static void handleBookstoreInterface(Scanner scanner, Statement stmt){
        int choice = 0;
        
        while (choice != 4){
            displayBookstoreInterfaceMenu();
            choice = getInt(scanner);
            
            switch (choice) {
                case 1:
                    orderUpdate(scanner, stmt);
                    break;
                case 2:
                    orderQuery(scanner, stmt);
                    break;
                case 3:
                    nMostPopularBooksQuery(scanner, stmt);
                    break;
                case 4:
                    System.out.println("Returning to the main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 4.");
                    break;
            }
            System.out.println();
        }
    }
    
    private static void displayBookstoreInterfaceMenu() {
        System.out.println("<This is the bookstore interface.>");
        System.out.println("---------------------------------");
        System.out.println("1. Order Update.");
        System.out.println("2. Order Query.");
        System.out.println("3. N most Popular Book Query.");
        System.out.println("4. Back to main menu.");
        System.out.println();
        System.out.print("What is your choice??..");
    }
    
    private static int getInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
        }
        int a = scanner.nextInt();
        scanner.nextLine();
        return a;
    }
    
    private static void orderUpdate(Scanner scanner, Statement stmt) {
        String oid = getUserOid(scanner, stmt);
        if (isShipped(stmt, oid))
            System.out.print("The Shipping Status of " + oid + " is Y. \nNo update is allowed. ");
        else{
            System.out.print("The Shipping Status of " + oid + " is N and ");
            int quantity = getQuantity(stmt, oid);
            if (quantity == 0)
                System.out.print("0 book ordered. \n");
            else if (quantity > 0){
                System.out.print(quantity + " book(s) ordered. \nAre you sure to update the shipping status? (Yes=Y) ");
                if (scanner.nextLine().equalsIgnoreCase("Y")){
                    try{
                        String updateShippingStatus = "UPDATE orders\n" +
                           "SET shipping_status = 'Y' " +
                           "WHERE order_id = \'" + oid + "\'";
                       stmt.executeUpdate(updateShippingStatus);
                       System.out.print("Shipping status updated.\n");
                    }
                    catch(SQLException e) {
                        System.out.println("Cannot update the order: " + e.getMessage());
                    } 
                }
                else
                    System.out.print("Shipping status not updated. \n");
            }
        }
    }
    
    private static boolean validOid(String oid) {
        String pattern = "\\d{8}";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(oid);
        return matcher.matches();
    }

    private static String getUserOid(Scanner scanner, Statement stmt) {
        while (true) {
            System.out.print("Please enter the order ID: ");
            String oid = scanner.nextLine();
            if (validOid(oid) && oidInDB(stmt, oid))
                return oid;
            else
                System.out.println("Invalid order ID. Please enter a valid order ID.");
        }
    }
    
    private static boolean oidInDB(Statement stmt, String oid) {
        String findQuery = "SELECT *\n" +
            "FROM orders\n" +
            "WHERE orders.order_id = \'" + oid + "\'";
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            if (rs.next())
                return true;
        } catch (SQLException e) {
            System.out.println("Cannot perform finding orderID: " + e.getMessage());
        }
        return false;
    }
    
    private static boolean isShipped(Statement stmt, String oid) {
        String findQuery = "SELECT *\n" +
            "FROM orders\n" +
            "WHERE order_id = \'" + oid + "\'";
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            if (rs.next() && rs.getString("shipping_status").equals("Y"))
                return true;
        } catch (SQLException e) {
            System.out.println("Cannot check the shipping status: " + e.getMessage());
        }
        return false;
    }
    
    private static int getQuantity(Statement stmt, String oid){
        String findQuery = "SELECT *\n" +
            "FROM ordering\n" +
            "WHERE order_id = \'" + oid + "\'";
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            int quantity = 0;
            while (rs.next()){
                quantity += rs.getInt("quantity");
            }
            return quantity;
        } catch (SQLException e) {
            System.out.println("Cannot read the quantity: " + e.getMessage());
        }
        return -1;
    }
    
    private static void orderQuery(Scanner scanner, Statement stmt) {
        ResultSet records_in_month = getRecordsInMonth(scanner, stmt);
        if (records_in_month == null)
            System.out.println("No orders made in the month.");
        else{
            int total_charge = 0;
            int i = 0;
            try {
                while (records_in_month.next()){
                    i++;
                    String oid = records_in_month.getString("order_id");
                    String cid = records_in_month.getString("customer_id");
                    String date = records_in_month.getString("formatted_date");
                    int charge = records_in_month.getInt("charge");
                    printOrders(i, oid, cid, date, charge);
                    total_charge += charge;
                }
            }catch(SQLException e) {
                System.out.println("Cannot read order query results: " + e.getMessage());
            }
            System.out.println("\n\nTotal charge of the month is " + total_charge);
        }
    }
    
    private static ResultSet getRecordsInMonth(Scanner scanner, Statement stmt){
        while (true){
            System.out.println("Please input the Month for Order Query (e.g.2005-09): ");
            String month = scanner.nextLine();
            if (validMonthFormat(month)){
                String findQuery = "SELECT order_id, customer_id, TO_CHAR(o_date, \'yyyy-mm-dd\') AS \"formatted_date\", charge \n" +
                    "FROM orders \n" +
                    "WHERE EXTRACT(YEAR FROM o_date) = " + Integer.valueOf(month.substring(0,4)) + " AND EXTRACT(MONTH FROM o_date) = " + Integer.valueOf(month.substring(5,7))+ " ";
                try {
                    ResultSet rs = null;
                    rs = stmt.executeQuery(findQuery);
                    if (rs.next())
                        return rs;
                    else 
                        return null;
                } catch (SQLException e) {
                    System.out.println("Cannot perform order query: " + e.getMessage());
                }
            }
            else
                System.out.println("Invalid date format. Please enter a valid date.");
        }
    }
    
    private static boolean validMonthFormat(String month){
        String pattern = "\\d{4}-\\d{2}";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(month);
        return matcher.matches();
    }
    
    private static void printOrders(int i, String oid, String cid, String date, int charge){
        System.out.println("\n\nRecord: " + i + "\norder_id: " + oid + "\ncustomer_id: " + cid + "\ndate: " + date + "\ncharge: " + charge);
    }
    
    private static void nMostPopularBooksQuery(Scanner scanner, Statement stmt){
        String findQuery = "SELECT ISBN, title, \"copies\" \n" +
            "FROM book NATURAL JOIN (SELECT ISBN, SUM(quantity) AS \"copies\" \n" +
            "FROM ordering \n" +
            "GROUP BY ISBN) \n" +
            "ORDER BY \"copies\" DESC, title ASC, ISBN ASC";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
        } catch (SQLException e) {
            System.out.println("Unable to find number of books ordered: " + e.getMessage());
        }
        
        System.out.println("Please input the N popular books number: ");
        int n = 0;
        while (n < 1){
            n = getInt(scanner); 
            if (n < 1)
                 System.out.println("Invalid input, please input a number greater or equal to 1. ");
        }
        
        int i = 1;
        int prev_copies = 0;
        if (rs == null)
            System.out.println("No book orders found.");
        else{
            System.out.println("ISBN            Title             copies");
            try{
                while (i <= n && rs.next()){
                    String ISBN = rs.getString("ISBN");
                    String title = rs.getString("title");
                    int copies = rs.getInt("copies");
                    if ((i == n && copies < prev_copies) || copies == 0)
                        break;
                    System.out.println(ISBN + " " + title + " " + copies);
                    if (i == n){
                        prev_copies = copies;
                        continue;
                    }
                    i++;
                }
            }catch (SQLException e) {
                System.out.println("Unable to read query results: " + e.getMessage());
            }
        }
    }
    
    
    // FOR UNIT TESTING ONLY
    /*
    public static String dbAddress = "jdbc:oracle:thin://@db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
    public static String dbUsername = "h045";
    public static String dbPassword = "CopvilEc";
    //public static String mesg = "Connected to server!";

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

    private static void createTable(Statement stmt) {
        try {
            // Create book table
            String createBookTable = "CREATE TABLE book (" +
                    "ISBN CHAR(13) PRIMARY KEY, " +
                    "title VARCHAR2(100) NOT NULL, " +
                    "unit_price NUMBER(10,2) NOT NULL, " +
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
                    "charge NUMBER(10,2), " +
                    "customer_id VARCHAR2(10))";
            stmt.executeUpdate(createOrdersTable);
            System.out.println("Created orders table.");
    
            // Create ordering table
            String createOrderingTable = "CREATE TABLE ordering (" +
                    "order_id CHAR(8), " +
                    "ISBN CHAR(13), " +
                    "quantity NUMBER(10), " +
                    "PRIMARY KEY (order_id, ISBN))";
            stmt.executeUpdate(createOrderingTable);
            System.out.println("Created ordering table.");
    
            // Create book_author table
            String createBookAuthorTable = "CREATE TABLE book_author (" +
                    "ISBN CHAR(13), " +
                    "author_name VARCHAR2(50), " +
                    "PRIMARY KEY (ISBN, author_name))";
            stmt.executeUpdate(createBookAuthorTable);
            System.out.println("Created book_author table.");
    
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating all tables: " + e.getMessage());
        }
        
    }

    public static void main (String[] args) {
        try (Connection con = connectToMySQL();
             Statement stmt = con.createStatement();      
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected to the Oracle server.");
            
            stmt.executeUpdate("DROP TABLE book_author");
            stmt.executeUpdate("DROP TABLE book");
            stmt.executeUpdate("DROP TABLE customer");
            stmt.executeUpdate("DROP TABLE ordering");
            stmt.executeUpdate("DROP TABLE orders");
            createTable(stmt);
            stmt.executeUpdate("INSERT INTO book VALUES (\'1-1234-1234-1\',\'Database I\',100,50)");
            stmt.executeUpdate("INSERT INTO book VALUES (\'2-2345-2345-2\',\'Database II\',110,40)");
            stmt.executeUpdate("INSERT INTO book VALUES (\'3-3456-3456-3\',\'Operating System\',130,20)");
            stmt.executeUpdate("INSERT INTO book VALUES (\'4-4567-4567-4\',\'Programming in C language\',140,10)");
            stmt.executeUpdate("INSERT INTO book VALUES (\'5-5678-5678-5\',\'Programming in Java language\',150,5)");
            System.out.println("Book inserted");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'1-1234-1234-1\',\'Ada\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES(\'1-1234-1234-1\',\'Raymond\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'1-1234-1234-1\',\'Willy\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'2-2345-2345-2\',\'Henry\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'2-2345-2345-2\',\'Teresa\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'3-3456-3456-3\',\'Ada\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'3-3456-3456-3\',\'Alan\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'4-4567-4567-4\',\'Magic\')");
            stmt.executeUpdate("INSERT INTO book_author VALUES (\'5-5678-5678-5\',\'Oscar\')");
            System.out.println("author inserted");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'adafu\',\'Ada\',\'222,Shatin,Hong Kong\',\'4444-4444-4444-4444\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'cwwong\',\'Raymond\',\'123,Shatin,Hong Kong\',\'1234-1234-1234-1234\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'hndai\',\'Henry\',\'567,Shatin,Hong Kong\',\'1111-1111-1111-1111\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'hyyue\',\'Willy\',\'234,Kwai Chung,Hong Kong\',\'4321-4321-4321-4321\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'raymond\',\'Raymond Wong\',\'999,Tai Wai,Hong Kong\',\'9999-9999-9999-9999\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'twleung\',\'Oscar\',\'890,Tin Shui Wai,Hong Kong\',\'2222-2222-2222-2222\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'wcliew\',\'Alan\',\'333,Tsuen Wan,Hong Kong\',\'5555-5555-5555-5555\')");
            stmt.executeUpdate("INSERT INTO customer VALUES (\'xcai\',\'Teresa\',\'111,Shatin,Hong Kong\',\'3333-3333-3333-3333\')");
            System.out.println("customer inserted");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000000\',TO_DATE('2005-09-01', 'YYYY-MM-DD'),\'Y\',120, \'cwwong\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000001\',TO_DATE('2005-09-02', 'YYYY-MM-DD'),\'Y\',120, \'cwwong\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000002\',TO_DATE('2005-09-07', 'YYYY-MM-DD'),\'N\',120, \'hyyue\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000003\',TO_DATE('2005-09-10', 'YYYY-MM-DD'),\'N\',120, \'hyyue\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000004\',TO_DATE('2005-09-12', 'YYYY-MM-DD'),\'N\',120, \'hndai\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000005\',TO_DATE('2005-09-20', 'YYYY-MM-DD'),\'N\',120, \'hndai\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000006\',TO_DATE('2005-09-30', 'YYYY-MM-DD'),\'N\',120, \'twleung\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000007\',TO_DATE('2005-10-01', 'YYYY-MM-DD'),\'Y\',130, \'twleung\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000008\',TO_DATE('2005-10-06', 'YYYY-MM-DD'),\'Y\',130, \'xcai\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000009\',TO_DATE('2005-10-09', 'YYYY-MM-DD'),\'Y\',130, \'xcai\')");
            stmt.executeUpdate("INSERT INTO orders VALUES (\'00000010\',TO_DATE('2005-10-13', 'YYYY-MM-DD'),\'N\',320, \'xcai\')");
            System.out.println("orders inserted");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000000\',\'1-1234-1234-1\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000001\',\'1-1234-1234-1\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000002\',\'1-1234-1234-1\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000003\',\'1-1234-1234-1\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000004\',\'1-1234-1234-1\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000005\',\'1-1234-1234-1\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000006\',\'2-2345-2345-2\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000007\',\'2-2345-2345-2\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000008\',\'2-2345-2345-2\',1)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000009\',\'4-4567-4567-4\',0)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000010\',\'4-4567-4567-4\',0)");
            stmt.executeUpdate("INSERT INTO ordering VALUES (\'00000010\',\'5-5678-5678-5\',0)");
            System.out.println("ordering inserted");
            BookstoreInterface.handleBookstoreInterface(scanner, stmt);
        } catch (SQLException e) {
            System.out.println("Error executing SQL statement or connecting to the MySQL server: " + e.getMessage());
        }
    }
    */
}



