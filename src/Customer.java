//***************************************************************************************************************************************************

import java.util.Random ;
import java.util.logging.Level;
import java.util.logging.Logger;

//***************************************************************************************************************************************************




//***************************************************************************************************************************************************

public class Customer extends Thread implements Person
{
  //=================================================================================================================================================

  public        String  title                   ;
  public        String  name                    ;
  public        JobShop jobShop                 ;

  //-------------------------------------------------------------------------------------------------------------------------------------------------

  private       int     numberOfProductRequests ;
  private final Random  random                  ;

  //=================================================================================================================================================

  public Customer ( String name , JobShop jobShop )
  {
    this.title                   = "Customer      " ;
    this.name                    = name             ;
    this.jobShop                 = jobShop          ;
    this.numberOfProductRequests = 0                ;
    this.random                  = new Random()     ;
    

    talk( "%s %s : (Constructor finished)" , title , name ) ;
    this.jobShop.customers.add(this);
    
  }

  //=================================================================================================================================================

  @Override
  public void talk ( String format , Object ... args )  // This is a synchronized wrapper for printf method
  {
    synchronized ( System.out )  { System.out.printf( format + "%n" , args ) ;  System.out.flush() ; }
  }

  //=================================================================================================================================================

  @Override
  public void spendTime ( int minMilliseconds , int maxMilliseconds )  // This is a wrapper for Thread.sleep
  {
    int duration = minMilliseconds + (int) ( Math.random() * ( maxMilliseconds - minMilliseconds ) ) ;

    try { Thread.sleep( duration ) ; } catch ( InterruptedException ex ) { /* Do nothing */ }
  }

  //=================================================================================================================================================

  private Part generateRandomPart ()
  {
    int partNo = random.nextInt(4);
    if(partNo == 0)
    {
        return new PartA();
    }
    else if(partNo == 1)
    {
        return new PartB();
    }
    else if(partNo == 2)
    {
        return new PartC();
    }
    else
    {
        return new PartD();
    }
  }

  //=================================================================================================================================================

  private Part [] generateRandomProductRequest ()
  {
    int     numberOfParts = 2 + random.nextInt( 4 )   ;
    Part [] parts         = new Part[ numberOfParts ] ;
    this.numberOfProductRequests = numberOfParts;
    for ( int i = 0 ; i < numberOfParts ; i++ )  { parts[ i ] = generateRandomPart() ; }

    return parts ;
  }

  //=================================================================================================================================================

  @Override
  public void run () 
  {
     while(this.jobShop.isOpen){
       if(this.jobShop.productRequests.isEmpty()) {
         try { 
             this.jobShop.addProductRequest(this.generateRandomProductRequest());
         } catch (InterruptedException ex) {
             Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
         }
      this.talk("%s %s : Submitting a product request of %d parts.", this.title,this.name,this.numberOfProductRequests);
       }
    
     
     
        synchronized(this) {
         try {
             this.jobShop.addToCustomerWaitingQueue(this);
             wait();
         } catch (InterruptedException ex) {
             Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
         }
        }
        
     
         }
  }

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

