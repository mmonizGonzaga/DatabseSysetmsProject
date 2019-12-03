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


public class Assignment9 {
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

            menu();
            Scanner reader = new Scanner(System.in);
            int inputNumber = reader.nextInt();
            while(inputNumber != 5){
                if(inputNumber == 1){
                    listCountries(con);
                }else if(inputNumber == 2){
                    System.out.print("Country code................: ");
                    String country_code = reader.next();
                    System.out.print("Country name................: ");
                    String country_name = reader.next();
                    System.out.print("Country per capita gdp (USD): ");
                    int gdp = reader.nextInt();
                    System.out.print("Country inflation (pct).....: ");
                    double inflation = reader.nextDouble();

                    addCountry(con, country_code, country_name, gdp, inflation);

                }else if(inputNumber == 3){
                    System.out.print("Number of countries to display: ");
                    int limit = reader.nextInt();
                    System.out.print("Minimum per capita gdp (USD)..: ");
                    int gdp = reader.nextInt();
                    System.out.print("Maximum inflation (pct).......: ");
                    double inflation = reader.nextDouble();

                    countryByParams(con, gdp, inflation, limit);

                }else if(inputNumber == 4){
                    System.out.print("Country code................: ");
                    String country_code = reader.next();
                    System.out.print("Country per capita gdp (USD): ");
                    int gdp = reader.nextInt();
                    System.out.print("Country inflation (pct).....: ");
                    double inflation = reader.nextDouble();

                    updateCountry(con, gdp, inflation, country_code);

                }else{
                    System.out.println("Invalid input");
                }

                menu();
                inputNumber = reader.nextInt();
            }

            /*
            //For testing-delete China before entering it again
            String q = "DELETE FROM Country WHERE country_name=?";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setString(1,"China");
            pstmt.execute();

            //For testing-delete China before entering it again
            q = "DELETE FROM Country WHERE country_name=?";
            pstmt = con.prepareStatement(q);
            pstmt.setString(1,"Greece");
            pstmt.execute();
            */

            reader.close();
            con.close();
            
        } catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static boolean countryExists(Connection con, String check){
        try{
            String q = "SELECT * FROM Country WHERE country_code= ?";
            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setString(1,check);
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
        System.out.println("1. List countries");
        System.out.println("2. Add country");
        System.out.println("3. Find countries based on gdp and inflation");
        System.out.println("4. Update country's gdp and inflation");
        System.out.println("5. Exit");
        System.out.print("Enter your choice (1-5): ");
    }

    public static void listCountries(Connection con){
        try{
             //print table of countries
             Statement stmt = con.createStatement();
             String q = "SELECT country_name, country_code, gdp, inflation FROM Country";
             ResultSet rs = stmt.executeQuery(q);
 
             while(rs.next()){
                 String country_name = rs.getString("country_name");
                 String country_code = rs.getString("country_code");
                 //int gdp = rs.getInt("gdp");
                 //double inflation = rs.getDouble("inflation");
 
                 System.out.println(country_name + " " + country_code);
             }
             System.out.println();

             rs.close();
             stmt.close();

        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void addCountry(Connection con, String country_code, String country_name, int gdp, double inflation){
        try{
            boolean check = countryExists(con, country_code);
            if(check){
                System.out.println("Country already exists");
            }else{
                String q = "INSERT INTO Country VALUES (?,?,?,?)";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setString(1, country_code);
                pstmt.setString(2, country_name);
                pstmt.setInt(3, gdp);
                pstmt.setDouble(4, inflation);
                pstmt.execute();
                pstmt.close();
            }
            System.out.println();

            //listCountries(con);

        }catch(Exception err) {
            err.printStackTrace();
        }
    }

    public static void countryByParams(Connection con, int gdp, double inflation, int limit){
        try{

            //Select countries based on gdp and inflation
            String q = 
            "SELECT country_name, country_code, gdp, inflation "+
            "FROM Country " + 
            "WHERE gdp >= ? AND inflation <= ? " +
            "ORDER BY gdp DESC " +
            "LIMIT ?";

            PreparedStatement pstmt = con.prepareStatement(q);
            pstmt.setInt(1,gdp);
            pstmt.setDouble(2, inflation);
            pstmt.setInt(3,limit);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                String country_name = rs.getString("country_name");
                String country_code = rs.getString("country_code");
                gdp = rs.getInt("gdp");
                inflation = rs.getDouble("inflation");

                System.out.println(country_name + " " + country_code + " " + gdp + " " + inflation);
            }

            pstmt.close();
            rs.close();
            System.out.println();
            

        }catch(Exception err) {
            err.printStackTrace();
        }

    }

    public static void updateCountry(Connection con, int gdp, double inflation, String country_code){
        try{

            boolean check2 = countryExists(con, country_code);
            if(!check2){
                System.out.println("Country does not exist");
            }else{
                String q = "UPDATE Country SET gdp=?, inflation=? WHERE country_code=?";
                PreparedStatement pstmt = con.prepareStatement(q);
                pstmt.setInt(1,gdp);
                pstmt.setDouble(2,inflation);
                pstmt.setString(3,country_code);
                pstmt.execute();
                pstmt.close();
            }
            System.out.println();
            //listCountries(con);
            
        }catch(Exception err) {
            err.printStackTrace();
        }
    }
    
}
