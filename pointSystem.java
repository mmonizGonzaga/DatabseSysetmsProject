/*Gage Gutmann
CPSC 321
Project
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import com.mysql.jdbc.Driver;


public class pointSystem {
    public static void main(String[] args) {

        try {
            // connection info
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream("config.properties");
            prop.load(in);
            in.close();	    
    
            // connect to datbase
            String hst = prop.getProperty("host");
            String usr = prop.getProperty("user");
            String pwd = prop.getProperty("password");

            //Had to change dab from "DB" to "_DB" because my personal DB has an _ after my usr"
            String dab = usr + "_DB"; 
            String url = "jdbc:mysql://" + hst + "/" + dab;
            Connection con = DriverManager.getConnection(url, usr, pwd);

            //Global user id value, single event id, and reaccuring id
            int currentID = maxID(con) + 1;
            int currentSingleId = maxSingleID(con) + 1;
            int currentMultiId = maxMultiID(con) + 1;



            menu();
            Scanner reader = new Scanner(System.in);
            int inputNumber = reader.nextInt();
            while(inputNumber != 9){
                if(inputNumber == 1){
                    listUsers(con);
                }else if(inputNumber == 2){
                    System.out.print("First Name................: ");
                    String first_name = reader.next();
                    System.out.print("Last Name.................: ");
                    String last_name = reader.next();
                    System.out.print("Grad Year.................: ");
                    int grad_year = reader.nextInt();
                   

                    addNewUser(con, currentID,first_name, last_name, grad_year);

                }else if(inputNumber == 3){
                    System.out.print("First Name................: ");
                    String first_name = reader.next();
                    System.out.print("Last Name.................: ");
                    String last_name = reader.next();
                    System.out.print("Grad Year.................: ");
                    int grad_year = reader.nextInt();
                    System.out.print("Account Hold?(Y/N):.......: ");
                    String account_string = reader.next();
                    System.out.print("Account Hold?(Y/N):.......: ");
                    String active_string = reader.next();


                    boolean account_hold = false;
                    if(account_string.equals("Y")){
                        account_hold = true;
                    }

                    boolean active_account = true;
                    if(active_string.equals("N")){
                        account_hold = false;
                    }

                    updateUser(con, first_name, last_name, grad_year, account_hold, active_account);

                }else if(inputNumber == 4){
                    System.out.println("Your point types: Informal Meeting");
					// Note: needs to be hardcoded (for now)
					// Unfinished, but it works
                    String point_type = "Informal Meeting";
					System.out.print("Event Name................: ");
                    String one_time_type_name = reader.next();
					System.out.print("Event Description.................: ");
                    String one_time_type_description = reader.next();
					System.out.print("Event Date (Stylized YYYY-MM-DD).................: ");
                    String one_time_date = reader.next();
					
					//Add the event
					addSingleEvent(con, 0, point_type, one_time_type_name, one_time_type_description, currentSingleId, one_time_date);
					
					//Add users to an event
					System.out.println("\nAdd Attendees");
					System.out.print("Enter any key to continue adding attendees or enter q to save event (no attendees): ");
					Scanner input = new Scanner(System.in);
					String keepGoing = input.next();
					 while(!keepGoing.equals("q")){
						System.out.println("Avaliable users:");
						try{
							//Display the users that can be added
							String q = "SELECT u_id, first_name, last_name FROM Users EXCEPT SELECT u.u_id,u.first_name,u.last_name FROM Users u JOIN Present pr USING(u_id) WHERE pr.one_time_id = ?";
							PreparedStatement pstmt = con.prepareStatement(q);
							pstmt.setInt(1,currentSingleId);
							ResultSet rs = pstmt.executeQuery();
 
							//Pretty print them
							System.out.printf("%5s %20s %20s %n", "ID", "First Name", "Last Name");
							while(rs.next()){
								int u_id = rs.getInt("u_id");
								String first_name = rs.getString("first_name");
								String last_name = rs.getString("last_name");               
								System.out.printf("%5d %20s %20s %n", u_id, first_name, last_name);

							}
							System.out.println();

							rs.close();
							pstmt.close();
						}catch(Exception err) {
							err.printStackTrace();
						}
						
						//Insert user by ID
						System.out.print("ID of the User You Want to Add................: ");
						int u_id = reader.nextInt();
						String q = "INSERT INTO Present VALUES (?,?);";
						PreparedStatement pstmt = con.prepareStatement(q);
						pstmt.setInt(1,currentSingleId);
						pstmt.setInt(2,u_id);
						pstmt.execute();
						pstmt.close();
						
						//Asks the user if they want to keep adding or return to main screen
						System.out.print("Enter any key to keep adding and enter q to quit adding: ");
						keepGoing = input.next();
					} 

                }else if (inputNumber == 5){
                    System.out.print("Event Name.............................................: ");
                    String multi_type_name = reader.next();
                    reader.nextLine();
                    System.out.print("Point Type (Formal Meeting, Service Hour, Hosted Event): ");
                    String point_type = reader.next();
                    reader.nextLine();
                    System.out.print("Max Points.............................................:");
                    int max_points = reader.nextInt();

                    addRecurringEvent(con, multi_type_name, point_type, max_points);
                   
                }else if (inputNumber == 6){
                    addRecurringValue(con, currentMultiId);

                }else if(inputNumber == 7){
                    System.out.print("User ID...................: ");
                    int userID = reader.nextInt();
                    System.out.println();
                    displayUserBreakdown(con, userID);
    
                }else if(inputNumber == 8){
                    displayPointTotals(con);

                }else{
                    System.out.println("Invalid input");
                }

                menu();
                inputNumber = reader.nextInt();
            }

            reader.close();
            con.close();
            
        } catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static int maxID(Connection con){
        try{
            Statement stmt = con.createStatement();
            String q = "SELECT MAX(u_id) AS max_id FROM Users";
            ResultSet rs = stmt.executeQuery(q);
            int u_id = 0;
            
            while(rs.next()){
                u_id = rs.getInt("max_id");
            }
    
            rs.close();
            stmt.close();
            return u_id;

       }catch(Exception err) {
           err.printStackTrace();
       }
       return 0;
    }

    public static int maxSingleID(Connection con){
        try{
            Statement stmt = con.createStatement();
            String q = "SELECT MAX(one_time_id) AS max_id FROM OneTimeOcurrences";
            ResultSet rs = stmt.executeQuery(q);
            int one_time_id = 0;
            
            while(rs.next()){
                one_time_id = rs.getInt("max_id");
            }

            rs.close();
            stmt.close();
            return one_time_id;

       }catch(Exception err) {
           err.printStackTrace();
       }
       return 0;
    }

    public static int maxMultiID(Connection con){
        try{
            Statement stmt = con.createStatement();
            String q = "SELECT MAX(multi_id) AS max_id FROM MultiOccurences";
            ResultSet rs = stmt.executeQuery(q);
            int multi_id = 0;
            
            while(rs.next()){
                multi_id = rs.getInt("max_id");
            }
            //u_id = rs.getInt("u_id");

            rs.close();
            stmt.close();
            return multi_id;

       }catch(Exception err) {
           err.printStackTrace();
       }
       return 0;
    }


    public static boolean userExists(Connection con, String firstname, String lastname){
        try{
            String q = "SELECT * FROM Users WHERE first_name= ? AND last_name= ?";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setString(1,firstname);
            pstmt.setString(2,lastname);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                rs.close();//Close rs before return
                pstmt.close();//close pstmt before return
                return true;
            }

        }catch(Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    public static boolean multiEventExists(Connection con, String multi_type_name){
        try{
            String q = "SELECT * FROM MultiType WHERE multi_type_name=? ";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setString(1,multi_type_name);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                rs.close();//Close rs before return
                pstmt.close();//close pstmt before return
                return true;
            }
        }catch(Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    public static boolean singleEventExists(Connection con, String one_time_name){
        try{
            String q = "SELECT * FROM OneTimeTypes WHERE one_time_type_name=? ";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setString(1,one_time_name);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                rs.close();//Close rs before return
                pstmt.close();//close pstmt before return
                return true;
            }
        }catch(Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    public static void menu(){
        //Print out menu
        System.out.println("1. List Users");
        System.out.println("2. Add New User");
        System.out.println("3. Update User");
        System.out.println("4. Add Single Event Instance");
        System.out.println("5. Add New Recurring Event Type");
        System.out.println("6. Add a Recurring Event Instance");
        System.out.println("7. Display an Indivdual User's Breakdown");
        System.out.println("8. Display All Users Point Totals");
        System.out.println("9. Quit");
        System.out.print("Enter your choice (1-9): \n");
    }

    public static void listUsers(Connection con){
        try{
            //print table of countries
            Statement stmt = con.createStatement();
            String q = "SELECT * FROM Users";
            ResultSet rs = stmt.executeQuery(q);
            System.out.println("\nCurrent Users:");
            System.out.printf("%5s %15s %15s %15s %15s %12s %n", "ID", "First Name","Last Name","Grad Year", "Account Hold", "Active");
            while(rs.next()){
                int u_id = rs.getInt("u_id");
                String first_name = rs.getString("first_name");
                String last_name = rs.getString("last_name");
                int grad_year = rs.getInt("grad_year");
                boolean account_hold = rs.getBoolean("account_hold");
                boolean active = rs.getBoolean("active");

                System.out.printf("%5d %15s %15s %15d %15b %12b %n", u_id,first_name, last_name, grad_year, account_hold, active);
            }
            System.out.println();

            rs.close();
            stmt.close();

        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void addNewUser(Connection con, int userID, String first_name, String last_name, int grad_year){
        try{
            boolean check = userExists(con, first_name, last_name);
            if(check){
                System.out.println("User already exists");
            }else{
                String q = "INSERT INTO Users VALUES (?,?,?,?,?,?)";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setInt(1, userID);
                pstmt.setString(2, first_name);
                pstmt.setString(3, last_name);
                pstmt.setInt(4, grad_year);
                pstmt.setBoolean(5, false);
                pstmt.setBoolean(6,true);
                pstmt.execute();
                pstmt.close();
                System.out.println("\nUser Succesfully Added!");
            }
            System.out.println();

            //Increment userID for next user
            userID++;

        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void updateUser(Connection con, String first_name, String last_name, int grad_year, boolean account_hold, boolean active ){
        try{
            boolean check = userExists(con, first_name, last_name);

            if(!check){
                System.out.println("\nUser does not exist");
            }else{
                String q = "UPDATE Users SET grad_year=?, account_hold=?, active=? WHERE first_name=? AND last_name=?";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setInt(1,grad_year);
                pstmt.setBoolean(2,account_hold);
                pstmt.setBoolean(3,active);
                pstmt.setString(4,first_name);
                pstmt.setString(5,last_name);
                pstmt.execute();
                pstmt.close();
                System.out.println("\nUser Succesfully Updated!");

            }
            System.out.println();
            
        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void addSingleEvent(Connection con, int one_time_type_id, String point_type, String one_time_type_name, String one_time_type_description, int one_time_id, String one_time_date){
			/* try{
            boolean check = singleEventExists(con, one_time_type_name);
            if(check){
                System.out.println("Event already exists");
            }else{
                String q = "INSERT INTO OneTimeTypes VALUES (?,?,?,?)";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setInt(1, one_time_type_id);
                pstmt.setString(2, point_type);
                pstmt.setString(3, one_time_type_name);
                pstmt.setString(4, one_time_type_description);
                pstmt.execute();
                pstmt.close();
            }
            System.out.println();
            //Increment one_time_type_id for next user
            one_time_type_id++;
        }catch(Exception err) {
            err.printStackTrace();
        } */
		
		try{
			boolean check = singleEventExists(con, one_time_type_name);
            if(check){
                System.out.println("Event already exists");
            }else{
            String q = "INSERT INTO OneTimeOcurrences VALUES (?,?,?)";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setInt(1, one_time_id);
            pstmt.setString(2, one_time_date);
            pstmt.setInt(3, one_time_type_id);
            pstmt.execute();
            pstmt.close();
            System.out.println();
			}
            //Increment one_time_type_id for next user
            one_time_type_id++;

        }catch(Exception err) {
            err.printStackTrace();
        }
    }


    public static void addRecurringEvent(Connection con, String multi_type_name, String point_type, int max_points){ 
        try{
            boolean check = multiEventExists(con, multi_type_name);
            if(check){
                System.out.println("Event already exists");
            }else{
                String q = "INSERT INTO MultiType VALUES (?,?,?)";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setString(1, multi_type_name);
                pstmt.setString(2, point_type);
                pstmt.setInt(3, max_points);               
                pstmt.execute();
                pstmt.close();
            }
            System.out.println();

        }catch(Exception err) {
            err.printStackTrace();
        }
    }
    
   public static void addRecurringValue(Connection con, int multi_id){
        try{
             //print table of Reaccuring Events
             Statement stmt = con.createStatement();
             String q = "SELECT * FROM MultiType";
             ResultSet rs = stmt.executeQuery(q);
            
             System.out.println("Event Name     Point Type      Max Points");
             while(rs.next()){
                 String multi_type_name_menu = rs.getString("multi_type_name");
                 String point_type = rs.getString("point_type");
                 int max_points = rs.getInt("max_points");

                 System.out.println(multi_type_name_menu + "        " + point_type + "      " + max_points);

             }
             System.out.println();

             Scanner reader = new Scanner(System.in);
             System.out.print("Enter Event Name: ");
             String multi_type_name = reader.nextLine();
             System.out.println(multi_type_name);
             
             boolean check = multiEventExists(con, multi_type_name);
             if(!check){
                System.out.println("Event does not already exist");
             }else{
                System.out.print("Enter Date(YYYY-MM-DD): ");
                String multi_date = reader.next();
                System.out.println(multi_date);
                System.out.print("Enter User Id: ");
                int u_id = reader.nextInt();
                System.out.println(u_id);
                System.out.print("Enter Description: ");
                String multi_description = reader.next();
                System.out.println(multi_description);
                System.out.print("Enter Point Amount: ");
                int multi_amount = reader.nextInt();
                System.out.println(multi_amount);
                q = "INSERT INTO MultiOccurences VALUES (?,?,?,?,?,?)";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setInt(1, multi_id);
                pstmt.setString(2, multi_type_name);
                pstmt.setString(3, multi_date);
                pstmt.setInt(4, u_id);
                pstmt.setString(5, multi_description);   
                pstmt.setInt(6, multi_amount);          
                pstmt.execute();
                pstmt.close();
            }
            multi_id++;
            reader.close();
            rs.close();
            stmt.close();

        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void displayUserBreakdown(Connection con, int userID){
        try{
            boolean check = true;
            if(!check){
                System.out.println("User Doesn't exists");
            }else{
                String q = "SELECT ott.one_time_type_name, oto.one_time_date, p.point_value "+
                    "FROM Users u  JOIN Present pr USING(u_id) "+
                        "JOIN OneTimeOcurrences oto USING(one_time_id) "+
                        "JOIN OneTimeTypes ott USING(one_time_type_id) "+
                        "JOIN PointValues p USING(point_type) "+
                    "WHERE u.u_id = ? "+
                    "UNION ALL "+
                    "SELECT mt.multi_type_name, mo.multi_date, p.point_value "+
                    "FROM Users u JOIN MultiOccurences mo USING(u_id) "+
                        "JOIN MultiType mt USING(multi_type_name) "+
                        "JOIN PointValues p USING(point_type) "+
                    "WHERE u.u_id = ?";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setInt(1, userID);
                pstmt.setInt(2, userID);   
                ResultSet rs = pstmt.executeQuery();
                System.out.println("User's Breakdown:");
                System.out.printf("%20s %15s %12s %n", "Event", "Date", "Points");
                while(rs.next()){
                    String ott = rs.getString("one_time_type_name");
                    String date = rs.getString("one_time_date");
                    double points = rs.getDouble("point_value");
                    System.out.printf("%20s %15s %12f %n", ott, date, points );

                }
                pstmt.close();
            }
            System.out.println();

        }catch(Exception err) {
            err.printStackTrace();
        }
    }
    public static void displayPointTotals(Connection con){
        try{
            //print table of countries
            Statement stmt = con.createStatement();
            String q = "SELECT firstName, lastName, SUM(points) AS totalPoints "+
            "FROM "+
            "(SELECT u.first_name as firstName, u.last_name as lastName, SUM(p.point_value) AS points  "+
            "FROM Users u  JOIN Present pr USING(u_id) "+
                "JOIN OneTimeOcurrences oto USING(one_time_id) "+
                "JOIN OneTimeTypes ott USING(one_time_type_id) "+
                "JOIN PointValues p USING(point_type) "+
            "GROUP BY u.u_id "+
            "UNION ALL  "+
            "SELECT u.first_name, u.last_name, SUM(p.point_value * mo.multi_amount) AS points "+
            "FROM Users u JOIN MultiOccurences mo USING(u_id) "+
                "JOIN MultiType mt USING(multi_type_name) "+
                "JOIN PointValues p USING(point_type) "+
            "GROUP BY u.u_id) as temp "+
            "GROUP BY firstName, lastName;";
            ResultSet rs = stmt.executeQuery(q);
            System.out.println("Point Totals:");
            System.out.printf("%20s %20s %12s %n","First Name","Last Name", "Points");

            while(rs.next()){
                String first_name = rs.getString("firstName");
                String last_name = rs.getString("lastName");
                double points = rs.getDouble("totalPoints");
                System.out.printf("%20s %20s %12f %n",first_name, last_name, points);

            }
            System.out.println();

            rs.close();
            stmt.close();

        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    
    
}
