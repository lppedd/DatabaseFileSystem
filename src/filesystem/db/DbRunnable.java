package filesystem.db;

/**
 * @author Edoardo Luppi
 */
public interface DbRunnable extends Runnable
{
   /**
    * This method is meant to execute activities before the main one
    * is run. What is done here will always be synchronized.
    */
   void beforeRun();

   /**
    * This method is meant to execute activities in case the main one
    * fails. What is done here will always be synchronized.
    */
   void onFail();
}
