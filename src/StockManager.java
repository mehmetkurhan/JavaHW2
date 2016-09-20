
import java.util.logging.Level;
import java.util.logging.Logger;

//***************************************************************************************************************************************************

public class StockManager extends Employee
{
  //=================================================================================================================================================

  private int numberOfPartsSupplied ;

  //=================================================================================================================================================

  public StockManager ( String name , JobShop jobShop )
  {
    super( name , jobShop ) ;

    title                 = "Stock Manager " ;
    numberOfPartsSupplied = 0                ;

    talk( "%s %s : (Constructor finished)" , title , name ) ;
  }

  //=================================================================================================================================================

  @Override
  public void run ()
  {
      
    while(this.jobShop.isOpen) {
        this.talk("%s %s : Checking for reporting missing parts", this.title,this.name);
        if(this.jobShop.missingParts.isEmpty()) {
            this.talk("%s %s : There are no reported missing parts,so I'm wating.", this.title,this.name);
            synchronized(this) {
                
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(StockManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else {
            
           
                String missingPart =  this.jobShop.getNextMissingPart();
                this.talk("%s %s : Part %s is ordered.", this.title,this.name, missingPart);
            try {
                this.jobShop.db.incrementPartCount(this.jobShop.getNextMissingPart());
            } catch (Exception ex) {
                Logger.getLogger(StockManager.class.getName()).log(Level.SEVERE, null, ex);
            }
                this.talk("%s %s : Part %s is supplied", this.title,this.name,missingPart);
                for(Worker worker : this.jobShop.workers) {
                    synchronized(worker) {
                    worker.notifyAll(); }
                }
            }
        }  
    }      
  }

  //=================================================================================================================================================}

//***************************************************************************************************************************************************

