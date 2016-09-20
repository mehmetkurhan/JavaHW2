//***************************************************************************************************************************************************

// import ...
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
//***************************************************************************************************************************************************




//***************************************************************************************************************************************************

public class JobShop
{
  //=================================================================================================================================================
  public List<Customer>                         customers               ;
  public  final Queue< Part []                > productRequests         ;
  public  final Queue< Order                  > workingOrders           ;
  public  final Queue< Order                  > completedOrders         ;
  public  final Queue< String                 > missingParts            ;
  private final Queue< CustomerRepresentative > waitingRepresentatives  ;
  private final Queue< Customer               > waitingCustomers        ;

  public  final List < CustomerRepresentative > customerRepresentatives ;
  public  final List < Worker                 > workers                 ;
  public  final List < StockManager           > stockManagers           ;

  public        Clock                           clock                   ;
  public        boolean                         isOpen                  ;
  public        DBServices                      db                      ;
  private       boolean                         isAvailable             ;
  private       boolean                         productListAvailable    ;
  private       boolean                         workingOrderAvailable   ; 

  //-------------------------------------------------------------------------------------------------------------------------------------------------

  private       Integer                         nextOrderID             ;
  private final Timer                           timer                   ;

  //=================================================================================================================================================

  public JobShop
  (
    List< String> customerRepresentativeNames ,
    List< String> workerNames                 ,
    List< String> stockManagerNames
  )
    throws Exception
  {
    this.productRequests         = new LinkedList<>() ;
    this.workingOrders           = new LinkedList<>() ;
    this.completedOrders         = new LinkedList<>() ;
    this.missingParts            = new LinkedList<>() ;
    this.customerRepresentatives = new ArrayList <>() ;
    this.workers                 = new ArrayList <>() ;
    this.stockManagers           = new ArrayList <>() ;
    this.customers               = new ArrayList< >();
    this.clock                   = new Clock( 8 , 0 ) ;
    this.isOpen                  = true               ;
    this.db                      = new DBServices()   ;
    this.nextOrderID             = 1                  ;
    this.isAvailable             = false              ;
    this.productListAvailable    = false              ;
    this.workingOrderAvailable   = false              ;
    this.waitingRepresentatives  = new LinkedList<>() ;
    this.waitingCustomers        = new LinkedList<>() ;

    db.setPartCount( "A" , 50 ) ;
    db.setPartCount( "B" , 50 ) ;
    db.setPartCount( "C" , 50 ) ;
    db.setPartCount( "D" , 50 ) ;
    db.setPartCount( "E" , 50 ) ;

    for ( String name : customerRepresentativeNames )  { customerRepresentatives.add( new CustomerRepresentative( name , this ) ) ; }
    for ( String name : workerNames                 )  { workers                .add( new Worker                ( name , this ) ) ; }
    for ( String name : stockManagerNames           )  { stockManagers          .add( new StockManager          ( name , this ) ) ; }

    ActionListener clockTicker = new ActionListener()
                                 {
                                     
                              public void actionPerformed ( ActionEvent e )
                                   {
                                     int [] time   = clock.tick().getTime() ;
                                     int    hour   = time[0]                ;
                                     int    minute = time[1]                ;

                                     isOpen = ( ( hour >= 8 ) && ( hour < 17 ) ) ;

                                     if ( minute % 30 == 0 )  { 
                                         print() ; 
                             
                                     }

                                     if ( ( hour == 17 ) && ( minute == 0 ) )
                                     {
                                       synchronized( productRequests )  { productRequests.notifyAll() ; }
                                       synchronized( workingOrders   )  { workingOrders  .notifyAll() ; }
                                       synchronized( missingParts    )  { missingParts   .notifyAll() ; }
                                       for(CustomerRepresentative c : customerRepresentatives) {
                                           synchronized(c) {
                                               
                                               c.notifyAll();
                                           }
                                       }
                                       for(Worker w : workers) {
                                           synchronized(w) {
                                           w.notifyAll();
                                           }
                                       }
                                       for(StockManager s: stockManagers) {
                                           synchronized(s) {
                                              s.notifyAll();
                                           }
                                       }
                                       for(Customer c : customers) {
                                           synchronized(c) {
                                               c.notifyAll();
                                           }
                                       }
                                       synchronized (customerRepresentatives ) { customerRepresentatives.notifyAll(); }
                                       synchronized (workers ) { workers.notifyAll(); }
                                       synchronized(stockManagers) {stockManagers.notifyAll(); }

                                       timer.stop();
                                     }
                                   }
                                 } ;
    
    timer = new Timer( 50 , clockTicker ) ;
   
    timer.setInitialDelay( 0 ) ;
    timer.start          (   ) ;

    Thread.sleep( 50 ) ;

    print() ;

    for ( Employee e  : customerRepresentatives     )  { e.start() ; }
    for ( Employee e  : workers                     )  { e.start() ; }
    for ( Employee e  : stockManagers               )  { e.start() ; }
    
  }

  //=================================================================================================================================================
  public int generateNewOrderID ()  { synchronized ( nextOrderID )  { return ( nextOrderID++ ) ; } }
  public synchronized void addProductRequest ( Part [] pr    ) throws InterruptedException  { 
      
      while(this.isAvailable) {
          this.wait();
      }
      this.isAvailable = true;
      this.notify();
      this.productRequests.add(pr);
     
      if(this.waitingRepresentatives.size() == this.customerRepresentatives.size())
      {
          CustomerRepresentative c = this.waitingRepresentatives.poll();
          synchronized(c) {
              c.notifyAll();
          }
      }
     
 
  }
  public synchronized void addWorkingOrder   ( Order   order ) {
     
      this.workingOrders.add(order);
      
  }
  public synchronized void addCompletedOrder ( Order   order ) {
      this.workingOrders.remove(order);
     
      this.completedOrders.add(order);
      Customer c = this.waitingCustomers.poll();
      synchronized(c) {
          c.notifyAll();
      }
  }
  public synchronized void addMissingPart    ( String  part  ) {
  
      this.missingParts.add(part);
      for(StockManager s : stockManagers){
          synchronized(s) {
              s.notifyAll();
          }
      }
  }

  //=================================================================================================================================================

  public synchronized Part [] getNextProductRequest () throws InterruptedException   { 
      while(!isAvailable) {
          this.wait();
      }
      this.isAvailable = false;
      this.notifyAll();
      return productRequests.poll();
  }
  public synchronized Order   getNextWorkingOrder   () throws InterruptedException  {
      return workingOrders.element();
  }
  public synchronized String  getNextMissingPart    ()  { 
      
      return missingParts.poll();
  }
  public synchronized void addToCustomerRepresentativeWaitingQueue(CustomerRepresentative c) {
      this.waitingRepresentatives.add(c);
  }
  public synchronized void addToCustomerWaitingQueue(Customer c) {
      this.waitingCustomers.add(c);
  }

  //=================================================================================================================================================



  //=================================================================================================================================================

  public void print ()
  {
    synchronized ( System.out )  { System.out.println( this.toString() ) ;  System.out.flush() ; }
  }

  //=================================================================================================================================================

  @Override
  public String toString ()
  {
    synchronized ( this )
    {
      String result = String.format( "-------------------%n"                                                                            +
                                     "JobShop (%s : Time = %s , Employees = < R:%d , W:%d , S:%d > , Orders = < R:%d , W:%d , C:%d >%n" +
                                     ">>>> Inventory    :"                                                                              ,
                                     isOpen ? "Open)   " : "Closed) "                                                                   ,
                                     clock                                                                                              ,
                                     customerRepresentatives.size()                                                                     ,
                                     workers                .size()                                                                     ,
                                     stockManagers          .size()                                                                     ,
                                     productRequests        .size()                                                                     ,
                                     workingOrders          .size()                                                                     ,
                                     completedOrders        .size()                                                                     ) ;

      List< Pair< String , Integer > > inventory = null ;

      try                    { inventory = db.getInventory() ; }
      catch ( Exception e )  { /* Do nothing */                }

      if ( inventory != null )
      {
        for ( int i = 0 ; i < inventory.size() ; i++ )
        {
          Pair< String , Integer > pair = inventory.get( i ) ;

          result += " " + pair.first + " : " + pair.second ;

          if ( i < inventory.size() - 1 )  { result += " ," ; }
        }
      }

      result += "\n-------------------" ;

      return result ;
    }
  }

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

