
import java.util.logging.Level;
import java.util.logging.Logger;

//***************************************************************************************************************************************************

public class Worker extends Employee
{
  //=================================================================================================================================================

  private int numberOfPartsAssembled ;
  private Order currentOrder;
 

  //=================================================================================================================================================

  public Worker ( String name , JobShop jobShop )
  {
    super( name , jobShop ) ;

    title                  = "Worker        " ;
    numberOfPartsAssembled = 0                ;

    talk( "%s %s : (Constructor finished)" , title , name ) ;
  }

  //=================================================================================================================================================

  @Override
  public void run ()
  {
     
    while(this.jobShop.isOpen){
        this.talk("%s %s : Checking for a working order.", this.title,this.name);
        if(!this.jobShop.workingOrders.isEmpty()){
            Order o = this.jobShop.workingOrders.element();
            this.talk("%s %s : Currently working on order %s",this.title,this.name, o.toString());
            try {
                if(this.jobShop.db.getPartCount(o.nextRemainingPart()) > 0) {
                this.jobShop.db.decrementPartCount(o.nextRemainingPart()); 
                o.completeNextRemainingPart(); 
                }
                else {
                    if(!o.isCompleted()) {
                    this.talk("%s %s : Cannot work order, report missing part %s", this.title,this.name,o.nextRemainingPart());
                    this.jobShop.addMissingPart(o.nextRemainingPart());
                    }
                    synchronized(this) {
                    this.wait(); }
                   
                }
            } catch (Exception ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(o.isCompleted()) {
                
                this.talk("%s %s : Assembled last part of order %s",this.title,this.name, o.toString()); 
                this.jobShop.addCompletedOrder(o);
            }
                
            else {
             this.talk("%s %s : Assembled next part of order %s",this.title,this.name, o.toString()); 
            }
        
        }
        else {
           this.talk("%s %s : There are no working orders, so I'm waiting.", this.title,this.name);
           synchronized(this) {
               
            try {
           
                this.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        } }
        this.spendTime(200, 400);  
    }      
    
  }

  //=================================================================================================================================================
  }


//***************************************************************************************************************************************************

