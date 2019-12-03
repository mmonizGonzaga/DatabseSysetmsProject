/*Gage Gutmann
CPSC 321
Homework 9
This program connects to the individuals database in ADA and 
works off of the CIA database used in Homeworks 6 and 7.
It then prompts the user to make changes to the database
or ask for specific information.
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
                    System.out.println("HI");

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
                System.out.println(" User already exists");
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

            boolean check2 = userExists(con, first_name, last_name);
            if(!check2){
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
    
}
