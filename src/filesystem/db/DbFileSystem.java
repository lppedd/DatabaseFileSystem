package filesystem.db;

import java.sql.Connection;
import java.util.UUID;

import filesystem.DirectoryPath;
import filesystem.FileSystem;

/**
 * An implementation of a file system which stores informations in a database.
 *
 * <p>
 * A file system usually handle concurrent read-write operations by locking
 * the requested resources. If a resource is requested for reading, a subsequent
 * write operation
 * must wait for the read operation to be completed, and only then it is performed.
 * <p/>
 *
 * <p>
 * For a database file system implementation, concurrency can be handled by the
 * database itself
 * through the use of transactions and isolation levels.
 * A minimal level of synchronization could also be implemented in the Java layer,
 * but that
 * is not the case.
 * It is important to remember that a {@link Connection} is a transaction itself, so
 * this implies
 * a different connection object per thread. If this is not respected, a
 * {@link Connection#commit()}
 * or a {@link Connection#rollback()} might cause the action to propagate to
 * non-related statements.
 * </p>
 *
 * @author Edoardo Luppi
 */
public class DbFileSystem implements FileSystem
{
   private final String uniqueId;
   private final Property<String> volumeLabel;
   private final String rootPathName;
   private final int clusterSize;
   private DirectoryPath root;
   private final Property<Boolean> exists;
   private final Property<Boolean> existsChecked;
   
   public DbFileSystem(final String volumeLabel, final String rootPathName, final int clusterSize) {
      uniqueId = UUID.randomUUID().toString();
      this.volumeLabel = new Property<>(volumeLabel);
      this.clusterSize = clusterSize;
      this.rootPathName = rootPathName;
      exists = new Property<>(true);
      existsChecked = new Property<>(false);
   }
   
   @Override
   public FileSystem create() {
      if (exists()) {
         FileSystem.error(FileSystem.FILE_SYSTEM_ALREADY_EXIST);
      }
      
      new DbSandbox() {
         @Override
         public void run() {
            exists.set(sqlInsert());
            existsChecked.set(true);
         }

         @Override
         public void onFail() {
            exists.set(false);
            existsChecked.set(false);
         }
      }.execute(false);

      return this;
   }
   
   @Override
   public void close() {
      DbSandbox.close();
   }
   
   @Override
   public boolean exists() {
      new DbSandbox() {
         @Override
         public void run() {
            exists.set(sqlSelect());
            existsChecked.set(true);
         }
         
         @Override
         public void onFail() {
            exists.set(false);
            existsChecked.set(false);
         }
      }.execute(false);
      
      return exists.get();
   }

   @Override
   public String getUniqueId() {
      return uniqueId;
   }
   
   @Override
   public String getVolumeLabel() {
      return volumeLabel.get();
   }
   
   @Override
   public int getClusterSize() {
      return clusterSize;
   }
   
   @Override
   public DirectoryPath getRoot() {
      if (root == null) {
         new DbSandbox() {
            @Override
            public void run() {
               sqlSelect();
            }
         }.execute(false);

         // FIXME: for demo. Root should be created inside sqlSelect()
         root = new DbDirectory(this, null, rootPathName).create();
      }
      
      return root;
   }
   
   @Override
   public void setVolumeLabel(final String label) {
      new DbSandbox() {
         @Override
         public void beforeRun() {
            volumeLabel.set(label);
         }

         @Override
         public void run() {
            sqlUpdate();
         }

         @Override
         public void onFail() {
            volumeLabel.undo();
         }
      }.execute(false);
   }
   
   Connection getConnection() {
      // Return a Connection from a connection pool.
      return null;
   }

   private boolean sqlSelect() {
      return false;
   }
   
   private boolean sqlInsert() {
      return true;
   }
   
   private boolean sqlUpdate() {
      return true;
   }
}
