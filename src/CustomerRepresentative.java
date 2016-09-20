
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

//***************************************************************************************************************************************************

public class CustomerRepresentative extends Employee
{
  //=================================================================================================================================================

  private int numberOfProductRequests ;
  private Order workingOrder;
  private static boolean isAvailable = true;

  //=================================================================================================================================================

  public CustomerRepresentative ( String name , JobShop jobShop )
  {
    super( name , jobShop ) ;

    title                   = "Representative" ;
    numberOfProductRequests = 0                ;

    talk( "%s %s : (Constructor finished)" , title , name ) ;
  }

  //=================================================================================================================================================

  private String identifyPartName ( Part part ) throws Exception
  {
     
      
      Field f;
      try{
        f = part.getClass().getField("name");
        if(f != null) {
          f.setAccessible(true);
          if(f.getType() == String.class) {
              return (String)f.get(part);
          }
        }
      
      }
      catch(NoSuchFieldException e) {
          try{
          f = part.getClass().getField("index");
          if(f != null) {
              f.setAccessible(true);
              if(f.getType() == Integer.class) {
                  return (String)f.get(part);
              }
          }
          }
          catch(NoSuchFieldException ex) {
              f = part.getClass().getField("method");
              Method [] m = part.getClass().getMethods();
              for( Method m1 : m) {
                  m1.setAccessible(true);
                  if(m1.getName().equals(f.get(part))) {
                      Object [] o = new Object[m1.getParameterCount()];
                     return (String)m1.invoke(null, o);
                  }
              }
          
          }
      }
               
     return "";  
  }

  //=================================================================================================================================================
  public void work() throws InterruptedException, Exception
  {
      
      
      while(this.jobShop.isOpen){
       
         this.talk("%s %s : Checking for a standing product request.",this.title,this.name);

         String [] arg;
         if(!this.jobShop.productRequests.isEmpty()){
             Part [] parts = null;
            try {
                parts = this.jobShop.getNextProductRequest();
            } catch (Exception ex) {
                Logger.getLogger(CustomerRepresentative.class.getName()).log(Level.SEVERE, null, ex);
            }
            
             String [] args = new String[parts.length];
            
             for (int i =0; i < parts.length; i++){
                args[i] = identifyPartName(parts[i]);
             }
             Order o = new Order(this.jobShop.generateNewOrderID(),args);
             this.talk("%s %s : I'm adding new order %s", this.title,this.name,o.toString());
             this.jobShop.addWorkingOrder(o);
            for(Worker w: this.jobShop.workers){
                 synchronized(w) {
                     w.notifyAll();
                 } 
             } 
        
         }
         else{

             this.talk("%s %s : There are no product request so I'm waiting. ",this.title,this.name);
              
             synchronized(this) {
            try {
               this.jobShop.addToCustomerRepresentativeWaitingQueue(this);
                this.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(CustomerRepresentative.class.getName()).log(Level.SEVERE, null, ex);
            
         }
             }
         } 
      }  
  }
  @Override
  public void run () 
  {
      try {
        
          work();
        
      } catch (InterruptedException ex) {
          Logger.getLogger(CustomerRepresentative.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch(Exception e) {
          e.printStackTrace();
      }
      
  }

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

