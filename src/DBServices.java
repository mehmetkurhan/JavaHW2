//***************************************************************************************************************************************************

// import ...
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//***************************************************************************************************************************************************




//***************************************************************************************************************************************************

public class DBServices
{
  //=================================================================================================================================================

  public  String     driver     ;
  public  String     url        ;
  public  String     database   ;
  public  String     username   ;
  public  String     password   ;

  //-------------------------------------------------------------------------------------------------------------------------------------------------

  private Connection connection ;
  private Statement  statement  ;
  
  
  
   

  //=================================================================================================================================================

  public DBServices () throws Exception
  {
    driver   = "com.mysql.jdbc.Driver"        ;
    url      = "jdbc:mysql://localhost:3306/" ;
    database = "ceng443"                      ;
    username = "root"                         ;
    password = "1234"                         ;

    Class.forName( driver ) ;

    connection = DriverManager.getConnection( url + database , username , password ) ;
    statement  = connection.createStatement()                                        ; 
    
    
  }

  //=================================================================================================================================================

  public List< Pair< String , Integer > > getInventory () throws Exception
  {
    List<Pair<String,Integer>> l = new ArrayList<Pair<String,Integer>>();
    ResultSet rs = statement.executeQuery("SELECT * FROM inventory");
    while(rs.next()) {
        String pName = rs.getString("Part");
        int pCount = rs.getInt("Count");
        Pair <String,Integer> p = new Pair<String,Integer>(pName,pCount);
        l.add(p);
    }
    return l;
  }

  //=================================================================================================================================================

  public synchronized void setPartCount ( String partName , int partCount ) throws Exception
  {

    statement.executeUpdate("UPDATE inventory SET Count=" + partCount + " WHERE Part='" + partName + "'");

  }

  //=================================================================================================================================================

public synchronized void incrementPartCount ( String partName ) throws Exception
  {
     
     statement.executeUpdate("UPDATE inventory SET Count=Count + 1 WHERE Part='" + partName + "'");
  
  }

  //=================================================================================================================================================

  public synchronized void decrementPartCount ( String partName ) throws Exception
  {
     
      statement.executeUpdate("UPDATE inventory SET Count=Count - 1 WHERE Part='" + partName + "' AND Count > 0");
    
    
  }
  public synchronized int getPartCount(String partName) throws Exception {
     
      ResultSet rs = statement.executeQuery("SELECT * FROM inventory WHERE Part='" + partName + "'");
      if(rs.next()) {
         return rs.getInt("Count");
      }
      
      return -1;
  }

  //=================================================================================================================================================

  public void close () throws Exception
  {
    if ( statement  != null )  { statement .close() ; }
    if ( connection != null )  { connection.close() ; }
  }

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

