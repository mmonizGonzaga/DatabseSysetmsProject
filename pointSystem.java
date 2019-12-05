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

            //Global user id value
            int currentID = maxID(con) + 1;
			//int currentSingleTypeId = maxSingleTypeID(con) + 1;
			int currentSingleId = maxSingleID(con) + 1;


            menu();
            Scanner reader = new Scanner(System.in);
            int inputNumber = reader.nextInt();
            while(inputNumber != 5){
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
					System.out.print("Enter any key to add attendees or enter q to save event (no attendees): ");
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
							System.out.println("ID     First Name      Last Name");
							while(rs.next()){
								int u_id = rs.getInt("u_id");
								String first_name = rs.getString("first_name");
								String last_name = rs.getString("last_name");               
								System.out.println(u_id + "        " + first_name + "      " + last_name);

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
        

            //u_id = rs.getInt("u_id");

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
        

            //u_id = rs.getInt("u_id");

            rs.close();
            stmt.close();
            return one_time_id;

       }catch(Exception err) {
           err.printStackTrace();
       }
       return 0;
    }
	
	/*public static int maxSingleTypeID(Connection con){
        try{
            Statement stmt = con.createStatement();
            String q = "SELECT MAX(one_time_type_id) AS max_id FROM OneTimeTypes";
            ResultSet rs = stmt.executeQuery(q);
            int one_time_type_id = 0;
            
            while(rs.next()){
                one_time_type_id = rs.getInt("max_id");
            }
        

            //u_id = rs.getInt("u_id");

            rs.close();
            stmt.close();
            return one_time_type_id;

       }catch(Exception err) {
           err.printStackTrace();
       }
       return 0;
    }*/


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

    public static boolean singleEventExists(Connection con, String one_type_name){
        try{
            String q = "SELECT * FROM OneTimeTypes WHERE one_time_type_name=? ";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setString(1,one_type_name);
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
        System.out.println("4. Add Single Event");
        System.out.println("5. Exit");
        System.out.print("Enter your choice (1-5): ");
    }

    public static void listUsers(Connection con){
        try{
             //print table of countries
             Statement stmt = con.createStatement();
             String q = "SELECT * FROM Users";
             ResultSet rs = stmt.executeQuery(q);
 
             while(rs.next()){
                 int u_id = rs.getInt("u_id");
                 String first_name = rs.getString("first_name");
                 String last_name = rs.getString("last_name");
                 int grad_year = rs.getInt("grad_year");
                 boolean account_hold = rs.getBoolean("account_hold");
                 boolean active = rs.getBoolean("active");

                 System.out.println("ID     First Name      Last Name       Grad Year       Account Hold        Active");
                 System.out.println(u_id + "        " + first_name + "      " + last_name + "       " + grad_year + "       " + account_hold + "        " + active);

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
                System.out.println("User does not exist");
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
    

    
    
}
