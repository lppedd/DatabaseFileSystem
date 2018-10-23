package filesystem.db;

import java.sql.Connection;
import java.util.UUID;

import filesystem.DirectoryPath;
import filesystem.FileSystem;

/**
 * An implementation of a file system which stores informations in a database.
 *
 * A file system usually handle concurrent read-write operations by locking
 * the requested resources. If a resource is requested for reading, a subsequent
 * write operation
 * must wait for the read operation to be completed, and only then it is performed.
 * <p/>
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
 *
 * @author Edoardo Luppi
 */
public class DbFileSystem implements FileSystem
{
   private final String uniqueId;
   private String volumeLabel;
   private final String rootPathName;
   private final int clusterSize;
   private DirectoryPath root;
   private boolean exists;

   public DbFileSystem(final String volumeLabel, final String rootPathName, final int clusterSize) {
      uniqueId = UUID.randomUUID().toString();
      this.volumeLabel = volumeLabel;
      this.clusterSize = clusterSize;
      this.rootPathName = rootPathName;
      exists = false;
   }

   @Override
   public FileSystem create() {
      if (exists()) {
         FileSystem.error(FileSystem.FILE_SYSTEM_ALREADY_EXIST);
      }

      new DbSandbox() {
         @Override
         public void run() {
            exists = sqlInsert();
         }

         @Override
         public void onFail() {
            exists = false;
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
      final boolean oldExists = exists;

      new DbSandbox() {
         @Override
         public void run() {
            exists = sqlSelect();
         }

         @Override
         public void onFail() {
            exists = oldExists;
         }
      }.execute(false);

      return exists;
   }

   @Override
   public String getUniqueId() {
      return uniqueId;
   }

   @Override
   public String getVolumeLabel() {
      return volumeLabel;
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
      final String oldLabel = volumeLabel;

      new DbSandbox() {
         @Override
         public void beforeRun() {
            volumeLabel = label;
         }

         @Override
         public void run() {
            sqlUpdate();
         }

         @Override
         public void onFail() {
            volumeLabel = oldLabel;
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
