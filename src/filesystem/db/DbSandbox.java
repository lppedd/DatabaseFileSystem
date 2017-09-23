package filesystem.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filesystem.db.sql.Transaction;

public abstract class DbSandbox implements DbRunnable
{
   /**
    * This is the executor which contains the only thread(s) that manages database
    * operations assuring data consistency and integrity.
    */
   private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

   /**
    * Forcedly close the thread(s).
    */
   public static final void close() {
      if (!EXECUTOR.isShutdown()) {
         EXECUTOR.shutdownNow();
      }
   }

   /**
    * Executes the activity.
    */
   public final void execute(final boolean async) {
      beforeRun();

      final Transaction transaction = new Transaction();
      transaction.begin();
      
      try {
         // TODO: implement async choice with EXECUTOR
         run();
         transaction.commit();
      } catch (final Exception e) {
         e.printStackTrace();
         onFail();
         transaction.rollback();
      }
   }
   
   @Override
   public void beforeRun() {
      //
   }
   
   @Override
   public void onFail() {
      //
   }
}
