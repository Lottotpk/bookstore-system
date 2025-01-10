import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class CustomerInterface {

    private static int getUserChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
        }
        int a = scanner.nextInt();
        scanner.nextLine();
        return a;
    }

    // ISBN format: x-xxxx-xxxx-x
    private static boolean validISBN(String ISBN) {
        String pattern = "\\d-\\d{4}-\\d{4}-\\d";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(ISBN);
        return matcher.matches();
    }

    private static String getUserISBN(Scanner scanner) {
        while (true) {
            String ISBN = scanner.nextLine();
            if (validISBN(ISBN)) {
                return ISBN;           
            }
            else { 
                System.out.println("Invalid ISBN. Please enter a valid ISBN.");
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
            System.out.print("Please enter the orderID that you want to change: ");
            String oid = scanner.nextLine();
            if (validOid(oid) && oidInDB(stmt, oid))
                return oid;
            else
                System.out.println("Invalid orderID. Please enter a valid orderID");
        }
    }
    
    public static void handleCustomerInterface(Scanner scanner, Statement stmt) {
        int choice = 0;

        while (choice != 5) {
            displayCustomerInterfaceMenu();
            choice = getUserChoice(scanner);

            switch (choice) {
                case 1:
                    bookSearch(scanner, stmt);
                    break;
                case 2:
                    orderCreate(scanner, stmt);
                    break;
                case 3:
                    orderAlter(scanner, stmt);
                    break;
                case 4:
                    orderQuery(scanner, stmt);
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

    private static void displayCustomerInterfaceMenu() {
        System.out.println("<This is the customer interface.>");
        System.out.println("---------------------------------");
        System.out.println("1. Book Search.");
        System.out.println("2. Order Creation.");
        System.out.println("3. Order Altering.");
        System.out.println("4. Order Query.");
        System.out.println("5. Back to main menu.");
        System.out.println();
        System.out.print("What is your choice??..");
    }

    private static void bookSearch(Scanner scanner, Statement stmt) {
        int choice = 0;

        while (choice != 4) {
            displayBookSearchMenu();
            choice = getUserChoice(scanner);
            ResultSet rs;

            switch (choice) {
                case 1:
                    String ISBN;
                    System.out.print("Input the ISBN: ");
                    ISBN = getUserISBN(scanner);
                    rs = queryISBN(stmt, ISBN);
                    printResult(stmt, rs);
                    break;
                case 2:
                    String bookTitle;
                    System.out.print("Input the book title: ");
                    bookTitle = scanner.nextLine();
                    rs = queryTitle(stmt, bookTitle);
                    printResult(stmt, rs);
                    break;
                case 3:
                    String authorName;
                    System.out.print("Input the book author: ");
                    authorName = scanner.nextLine();
                    rs = queryAuthorName(stmt, authorName);
                    printResult(stmt, rs);
                    break;
                case 4:
                    System.out.println("Returning to the customer interface...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 4.");
                    break;
            }
        }
    }

    private static void displayBookSearchMenu() {
        System.out.println("What do you want to search??");
        System.out.println("1 ISBN");
        System.out.println("2 Book Title");
        System.out.println("3 Author name");
        System.out.println("4 Exit");
        System.out.print("Your Choice?...");
    }

    private static void printAuthors(Statement stmt, ArrayList<String> bookFound, ArrayList<String> ISBNFound) {
        try {
            if (bookFound.size() == 0)
                System.out.println("Book not found.");
            int i = 0;
            while (i < bookFound.size()) {
                System.out.println(bookFound.get(i));
                String findAuthor = "SELECT author_name\n" +
                    "FROM book_author\n" +
                    "WHERE ISBN = \'" + ISBNFound.get(i) + "\'\n" +
                    "ORDER BY author_name";
                ResultSet rs = stmt.executeQuery(findAuthor);
                System.out.println("Authors:");
                int count = 1;
                while(rs.next()) {
                    System.out.println(Integer.toString(count) + ": " + rs.getString("author_name"));
                    count++;
                }
                i++;
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error printing list of authors: " + e.getMessage());
        }
    }

    private static void printResult(Statement stmt, ResultSet rs) {
        if (rs == null) {
            System.out.println("No matching result");
            return;
        }
        int record = 1;
        try {
            ArrayList<String> bookFound = new ArrayList<String>();
            ArrayList<String> ISBNFound = new ArrayList<String>();
            while(rs.next()) {
                String addBook = "\n" + "Record " + Integer.toString(record) + "\n" +
                    "ISBN: " + rs.getString("ISBN") + "\n" +
                    "Book Title: " + rs.getString("title") + "\n" +
                    "Unit Price: " + rs.getInt("unit_price") + "\n" +
                    "No Of Available: " + rs.getInt("no_of_copies");
                bookFound.add(addBook);
                ISBNFound.add(rs.getString("ISBN"));
                record++;
            }
            printAuthors(stmt, bookFound, ISBNFound);
        } catch (SQLException e) {
            System.out.println("Error printing the result: " + e.getMessage());
        }
    }

    private static ResultSet queryISBN(Statement stmt, String ISBN) {
        String findQuery = "SELECT *\n" +
            "FROM book\n" +
            "WHERE ISBN = \'" + ISBN + "\'\n" +
            "ORDER BY title, ISBN";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
        } catch (SQLException e) {
            System.out.println("Unable to perform a query: " + e.getMessage());
        }
        return rs;
    }

    private static ResultSet queryTitle(Statement stmt, String bookTitle) {
        String findQuery = "SELECT *\n" +
            "FROM book\n" +
            "WHERE title LIKE \'" + bookTitle + "\'\n" +
            "ORDER BY title, ISBN";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
        } catch (SQLException e) {
            System.out.println("Unable to perform a query: " + e.getMessage());
        }
        return rs;
    }

    private static ResultSet queryAuthorName(Statement stmt, String authorName) {
        String findQuery = "SELECT DISTINCT book.ISBN, book.title, book.unit_price, book.no_of_copies\n" +
            "FROM book, book_author\n" +
            "WHERE book.ISBN = book_author.ISBN AND book_author.author_name LIKE \'" + authorName + "\'\n" +
            "ORDER BY book.title, book.ISBN";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
        } catch (SQLException e) {
            System.out.println("Unable to perform a query: " + e.getMessage());
        }
        return rs;
    }

    private static void orderCreate(Scanner scanner, Statement stmt) {
        String customerID;
        while (true) {
            System.out.print("Please enter your customer ID: ");
            customerID = scanner.nextLine();
            if (customerInDB(stmt, customerID))
                break;
            else {
                System.out.println("Customer ID is not available Please input a valid Customer ID");
            }
        }
        System.out.println(">> What books do you want to order??");
        System.out.println(">> Input ISBN and then the quantity");
        System.out.println(">> You can press \'L\' to see the ordered list, or \'F\' to finish ordering.");
        int bookOrdered = 0;
        String orderID = getMaxOrderID(stmt);
        orderID = String.format("%0" + orderID.length() + "d", Long.parseLong(orderID) + 1);
        while (true) {
            System.out.print("Please enter the book's ISBN: ");
            String ISBN = scanner.nextLine();
            if (ISBN.equals("F"))
                break;
            else if (ISBN.equals("L"))
                printOrderedList(stmt, orderID);
            else if (validISBN(ISBN)) {
                if (!enoughBookInDB(stmt, ISBN, 1)) {
                    System.out.println("This book is not available. Please input again.");
                    continue;
                }
                else if (alreadyOrdered(stmt, ISBN, orderID)) {
                    System.out.println("You already ordered this book. Please enter a different book.");
                    continue;
                }
                int quantity = 0;
                while (true) {
                    System.out.print("Please enter the quantity of the order: ");
                    quantity = getUserChoice(scanner);
                    if (quantity < 1) {
                        System.out.println("Please put an integer that is greater than or equal to 1.");
                    }
                    else if (!enoughBookInDB(stmt, ISBN, quantity)) {
                        System.out.println("The quantity exceed the no. of copies available. Please try again.");
                    }
                    else {
                        if (bookOrdered == 0) {
                            int totalCharge = calTotalCharge(stmt, orderID);
                            String insert = "INSERT INTO orders\n" +
                                "VALUES (\'" + orderID + "\',date " + "\'" + project.currentDate() + "\'" + ",\'N\'," + totalCharge + ",\'" + customerID + "\')";
                            try {
                                stmt.executeUpdate(insert);
                            } catch (SQLException e) {
                                System.out.println("Cannot insert the orders: " + e.getMessage());
                            }
                        }
                        String insert = "INSERT INTO ordering\n" +
                            "VALUES (\'" + orderID + "\'" + ",\'" + ISBN + "\'," + quantity + ")";
                        String decrement = "UPDATE book\n" +
                            "SET no_of_copies = no_of_copies - " + quantity + "\n" +
                            "WHERE ISBN = \'" + ISBN + "\'";
                        try {
                            stmt.executeUpdate(insert);
                            stmt.executeUpdate(decrement);
                            System.out.println("Order placed successfully");
                            bookOrdered++;
                        } catch (SQLException e) {
                            System.out.println("Cannot insert into ordering table: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
            else {
                System.out.println("Invalid input. Please enter a valid input");
            }
        }
        try {
            String update = "UPDATE orders\n" +
                "SET charge = " + calTotalCharge(stmt, orderID) + "\n" +
                "WHERE order_id = \'" + orderID + "\'";
            stmt.executeUpdate(update);
        } catch (SQLException e) {
            System.out.println("Cannot update the orders: " + e.getMessage());
        }
    }

    private static String getMaxOrderID(Statement stmt) {
        String findQuery = "SELECT MAX(order_id) AS maxOid\n" +
            "FROM ordering\n";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
            if (rs.next())
                return rs.getString("maxOid");
        } catch (SQLException e) {
            System.out.println("Cannot find max orderID: " + e.getMessage());
        }
        return "00000000";
    }

    private static boolean alreadyOrdered(Statement stmt, String ISBN, String orderID) {
        String findQuery = "SELECT *\n" +
            "FROM ordering\n" +
            "WHERE order_id = \'" + orderID + "\' AND ISBN = \'" + ISBN + "\'";
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            if (rs.next())
                return true;
        } catch (SQLException e) {
            System.out.println("Cannot find the ordered book from the inputted ISBN: " + e.getMessage());
        }
        return false;
    }

    private static boolean customerInDB(Statement stmt, String customerID) {
        String findQuery = "SELECT customer_id\n" +
            "FROM customer\n" +
            "WHERE customer_id = \'" + customerID + "\'";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
            if (rs.next())
                return true;
            else
                return false;
        } catch (SQLException e) {
            System.out.println("Cannot perform finding customer method: " + e.getMessage());
        }
        return false;
    }

    private static void printOrderedList(Statement stmt, String orderID) {
        String findQuery = "SELECT ISBN, quantity\n" +
            "FROM ordering\n" +
            "WHERE ordering.order_id = \'" + orderID + "\'";
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            System.out.println("ISBN          Number:");
            while (rs.next()) {
                System.out.println(rs.getString("ISBN") + "   " + rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            System.out.println("Cannot print the ordered list: " + e.getMessage());
        }
    }

    private static boolean enoughBookInDB(Statement stmt, String ISBN, int quantity) {
        String findQuery = "SELECT ISBN, no_of_copies\n" +
            "FROM book\n" +
            "WHERE ISBN = \'" + ISBN + "\'";
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(findQuery);
            if (rs.next() && rs.getInt("no_of_copies") >= quantity)
                return true;
            else
                return false;
        } catch (SQLException e) {
            System.out.println("Cannot perform finding book and checking no. of copies method: " + e.getMessage());
        }
        return false;
    }

    private static int calTotalCharge(Statement stmt, String oid) {
        String findQuery = "SELECT book.ISBN, book.unit_price, ordering.quantity\n" +
            "FROM book, ordering\n" +
            "WHERE book.ISBN = ordering.ISBN and ordering.order_id = \'" + oid + "\'";
        int numBooks = 0;
        int price = 0;
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            while (rs.next()) {
                numBooks += rs.getInt("quantity");
                price += rs.getInt("unit_price") * rs.getInt("quantity");
            }
        } catch (SQLException e) {
            System.out.println("Cannot calculate total charge: " + e.getMessage());
        }
        if (numBooks == 0)
            return 0;
        else
            return price + numBooks*10 + 10;
    }

    private static void orderAlter(Scanner scanner, Statement stmt) {
        String oid = getUserOid(scanner, stmt);
        if (isShipped(stmt, oid)) {
            System.out.println("The books in the order are shipped.");
            return;
        }
        displayOidStatus(stmt, oid);
        int[] countBookTmp = {0};
        ResultSet rs = bookList(stmt, oid, countBookTmp);
        int numBooks = countBookTmp[0];
        int idxBook = 0;
        while (idxBook < 1 || idxBook > numBooks) {
            System.out.print("Which book you want to alter (input book no.): ");
            idxBook = getUserChoice(scanner);
            if (idxBook < 1 || idxBook > numBooks) {
                System.out.println("Number is not in the range. Please try again.");
            }
        }
        try {
            for (int i = 0; i < idxBook; i++) {
                rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Cannot move the cursor: " + e.getMessage());
        }
        String operation = "";
        int numChange = 0;
        try {
            while (true) {
                System.out.print("input add or remove: ");
                operation = scanner.nextLine();
                if (!operation.equals("add") && !operation.equals("remove")) {
                    System.out.println("The input is neither add nor remove. Please try again.");
                }
                else if (operation.equals("add") && rs.getInt("no_of_copies") <= 0) {
                    System.out.println("All of the copies of this book have been reserved. Cannot add the book.");
                }
                else if (operation.equals("remove") && rs.getInt("quantity") <= 0) {
                    System.out.println("The number of ordered book is zero. Cannot remove.");
                }
                else {
                    break;
                }
            }
            while (true) {
                System.out.print("Input the number: ");
                numChange = getUserChoice(scanner);
                if (numChange < 1) {
                    System.out.println("Please input the positive integer.");
                }
                else if (operation.equals("add") && numChange > rs.getInt("no_of_copies")) {
                    System.out.println("The number exceeds the available number of copies.");
                }
                else if (operation.equals("remove") && numChange > rs.getInt("quantity")) {
                    System.out.println("The number exceeds the amount of ordered book.");
                }
                else {
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("Cannot retrieve the value from the query: " + e.getMessage());
        }
        try {
            if (operation.equals("add")) {
                updateOrder(stmt, oid, rs.getString("ISBN"), "+", numChange);
            }
            else {
                updateOrder(stmt, oid, rs.getString("ISBN"), "-", numChange);
            }
        } catch (SQLException e) {
            System.out.println("Cannot get the ISBN: " + e.getMessage());
        }
        displayOidStatus(stmt, oid);
        bookList(stmt, oid, countBookTmp);
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

    private static void displayOidStatus(Statement stmt, String oid) {
        String findQuery = "SELECT *\n" +
            "FROM orders\n" +
            "WHERE order_id = \'" + oid + "\'";
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            while (rs.next()) {
                System.out.println("order_id: " + rs.getString("order_id") + "  " +
                                    "shipping: " + rs.getString("shipping_status") + "  " +
                                    "charge: " + rs.getInt("charge") + "  " +
                                    "customerID: " + rs.getString("customer_id"));
            }
        } catch (SQLException e) {
            System.out.println("Cannot display orderID status: " + e.getMessage());
        }
    }

    private static ResultSet bookList(Statement stmt, String oid, int[] countBookTmp) {
        String findQuery = "SELECT ordering.ISBN, ordering.quantity, book.no_of_copies\n" +
            "FROM orders, ordering, book\n" +
            "WHERE orders.order_id = \'" + oid + "\' AND orders.order_id = ordering.order_id AND book.ISBN = ordering.ISBN\n" +
            "ORDER BY ordering.ISBN";
        int count = 1;
        try {
            ResultSet rs = stmt.executeQuery(findQuery);
            while (rs.next()) {
                System.out.println("book no: " + count + "  " +
                                    "ISBN = " + rs.getString("ISBN") + "  " +
                                    "quantity = " + rs.getInt("quantity"));
                count++;
            }
        } catch (SQLException e) {
            System.out.println("Cannot print out the list of book: " + e.getMessage());
        }
        countBookTmp[0] = count - 1;
        try {
            return stmt.executeQuery(findQuery);
        } catch (SQLException e) {
            System.out.println("Cannot return the query: " + e.getMessage());
        }
        return null;
    }

    private static void updateOrder(Statement stmt, String oid, String ISBN, String sign, int numChange) {
        String oppsign = "";
        if (sign.equals("+"))
            oppsign = "-";
        else
            oppsign = "+";
        try {
            String changeOrdering = "UPDATE ordering\n" +
                "SET quantity = quantity " + sign + " " + numChange + "\n" +
                "WHERE order_id = \'" + oid + "\' AND ISBN = \'" + ISBN + "\'";
            stmt.executeUpdate(changeOrdering);
            String changeOrders = "UPDATE orders\n" +
                "SET charge = " + calTotalCharge(stmt, oid) + ", o_date = date " + "\'" + project.currentDate() + "\'" + "\n" +
                "WHERE order_id = \'" + oid + "\'";
            stmt.executeUpdate(changeOrders);
            String changeCopies = "UPDATE book\n" +
                "SET no_of_copies = no_of_copies " + oppsign + " " + numChange + "\n" +
                "WHERE ISBN = \'" + ISBN + "\'";
            stmt.executeUpdate(changeCopies);
        } catch (SQLException e) {
            System.out.println("Cannot update the query: " + e.getMessage());
        } 
    }

    private static void orderQuery(Scanner scanner, Statement stmt) {
        String customerID, year;
        customerID = getUserCid(scanner);
        year = getUserYear(scanner);
        String startDate = year + "-01-01";
        String endDate = year + "-12-31";
        System.out.println();
        try {
            String findQuery = "SELECT *\n" +
                "FROM orders\n" +
                "WHERE o_date >= date \'" + startDate + "\' AND o_date <= date \'" + endDate + "\' AND customer_id = \'" + customerID + "\'\n" + 
                "ORDER BY order_id";
            ResultSet rs = stmt.executeQuery(findQuery);
            int numRec = 1;
            while (rs.next()) {
                System.out.println("Record : " + numRec);
                System.out.println("OrderID : " + rs.getString("order_id"));
                System.out.println("OrderDate : " + rs.getDate("o_date"));
                System.out.println("charge : " + rs.getInt("charge"));
                System.out.println("shipping status : " + rs.getString("shipping_status"));
                System.out.println();
                numRec++;
            }
            if (numRec == 1)
                System.out.println("Record not found.");
        } catch (SQLException e) {
            System.out.println("Cannot query the orders: " + e.getMessage());
        }
    }

    private static String getUserCid(Scanner scanner) {
        String cid;
        while (true) {
            System.out.print("Please input Customer ID: ");
            cid = scanner.nextLine();
            if (cid.length() > 10 || cid.length() == 0) {
                System.out.println("The input should not be empty and has the length longer than 10.");
            }
            else {
                break;
            }
        }
        return cid;
    }

    private static String getUserYear(Scanner scanner) {
        String year;
        while (true) {
            System.out.print("Please input the year: ");
            year = scanner.nextLine();
            String pattern = "\\d{4}";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(year);
            if(!matcher.matches() || year.equals("0000")) {
                System.out.println("The year is invalid. Please try again.");
            }
            else {
                break;
            }
        }
        return year;
    }

    // FOR UNIT TESTING ONLY
    /*
    public static String dbAddress = "jdbc:oracle:thin://@db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
    public static String dbUsername = "xxxx";
    public static String dbPassword = "xxxxxxxx";
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
            CustomerInterface.handleCustomerInterface(scanner, stmt);
        } catch (SQLException e) {
            System.out.println("Error executing SQL statement or connecting to the MySQL server: " + e.getMessage());
        }
    }
    */
}
