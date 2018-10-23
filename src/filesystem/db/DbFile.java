package filesystem.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import filesystem.Cluster;
import filesystem.DirectoryPath;
import filesystem.FilePath;
import filesystem.FileSystem;

/**
 * Represents a file in a database file system.
 *
 * @author Edoardo Luppi
 */
public class DbFile extends DbObject implements FilePath
{
   /**
    * In this database file system cluster implementation, the same concept of a generic
    * {@link Cluster} is applied.
    * Files data is stored in chunks of the same size and each chunk is represented
    * by an instance of this class. Each instance of this class has a corresponding row
    * in the appropriate database table.
    *
    * @author Edoardo Luppi
    */
   public static class DbCluster implements Cluster
   {
      private final FilePath parent;
      private byte[] data;
      
      /**
       * Constructs a new cluster which belongs to a one and only file.
       * This instance is only stored in memory until {@link #allocate()} is called.
       *
       * @param parent
       *           The file which this cluster belongs to
       * @param data
       *           The data stored by this cluster
       */
      private DbCluster(final FilePath parent, final byte[] data) {
         this.parent = parent;
         this.data = data;
      }
      
      @Override
      public FilePath getParent() {
         return parent;
      }
      
      @Override
      public byte[] getData() {
         return data;
      }
      
      @Override
      public Cluster allocate() {
         new DbSandbox() {
            @Override
            public void run() {
               sqlInsert();
            }
         }.execute(false);

         return this;
      }
      
      @Override
      public FilePath free() {
         new DbSandbox() {
            @Override
            public void run() {
               if (sqlDelete()) {
                  data = null;
               }
            }
         }.execute(false);
         
         return getParent();
      }
      
      private boolean sqlInsert() {
         return true;
      }
      
      private boolean sqlDelete() {
         return true;
      }
   }
   
   /**
    * The list of clusters used by this file.
    */
   private List<Cluster> clusters;
   
   /**
    * Constructs a new file in the belonging file system.
    * This instance is only stored in memory until {@link #create()} is called.
    *
    * @param fileSystem
    *           The file system which this file belongs to
    * @param parent
    *           The parent directory
    * @param name
    *           The file name
    */
   public DbFile(final FileSystem fileSystem, final DirectoryPath parent, final String name) {
      super(fileSystem, parent, name);
   }
   
   @Override
   public FilePath create() {
      return (FilePath) super.create();
   }
   
   @Override
   public DirectoryPath delete() {
      deleteClusters();
      return super.delete();
   }
   
   @Override
   public FilePath rename(final String newName) {
      return (FilePath) super.rename(newName);
   }
   
   @Override
   public FilePath moveTo(final DirectoryPath path) {
      return (FilePath) super.moveTo(path);
   }

   @Override
   public FilePath copyTo(final DirectoryPath path) {
      final FilePath copiedFile = new DbFile(getFileSystem(), path, getName()).create();
      path.getChildren().add(copiedFile);
      return copiedFile;
   }
   
   @Override
   public byte[] getData() {
      checkPath();

      if (clusters == null) {
         return new byte[0];
      }
      
      final int clusterSize = getFileSystem().getClusterSize();
      final ByteBuffer buffer = ByteBuffer.allocate(clusters.size() * clusterSize);
      
      for (int i = 0; i < clusters.size(); i++) {
         buffer.put(clusters.get(i).getData(), i * clusterSize, clusterSize);
      }
      
      return buffer.array();
   }

   public File getFile() {
      final int clusterSize = getFileSystem().getClusterSize();
      final File file = new File(getName());
      
      try (final OutputStream stream = new FileOutputStream(file)) {
         for (int i = 0; i < clusters.size(); i++) {
            stream.write(clusters.get(i).getData(), i * clusterSize, clusterSize);
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }
      
      return file;
   }
   
   @Override
   public FilePath setData(final byte[] file) {
      checkPath();
      deleteClusters();
      
      final int clusterSize = getFileSystem().getClusterSize();
      final ByteBuffer byteBuffer = ByteBuffer.wrap(file);
      final byte[] slice = new byte[clusterSize];
      final int slicesNumber = (int) Math.ceil((double) file.length / clusterSize);
      
      for (int i = 0; i < slicesNumber; i++) {
         byteBuffer.get(slice, i * clusterSize, clusterSize);
         clusters.add(new DbCluster(this, slice).allocate());
      }
      
      return this;
   }

   public FilePath setData(final File file) {
      checkPath();
      deleteClusters();

      try (FileInputStream stream = new FileInputStream(file)) {
         final long fileSize = file.length();
         final int clusterSize = getFileSystem().getClusterSize();
         final int clustersAmount = (int) Math.ceil((double) fileSize / clusterSize);
         final byte[] buffer = new byte[clusterSize];

         for (int i = 0; i < clustersAmount; i++) {
            stream.read(buffer, i * clusterSize, clusterSize);
            clusters.add(new DbCluster(this, buffer).allocate());
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }
      
      return this;
   }
   
   private Collection<Cluster> getClusters() {
      if (clusters == null) {
         clusters = new ArrayList<>();

         new DbSandbox() {
            @Override
            public void run() {
               sqlSelect();
            }
         }.execute(false);
      }

      return clusters;
   }
   
   private void deleteClusters() {
      for (final Cluster cluster : getClusters()) {
         cluster.free();
      }
      
      clusters.clear();
   }

   @Override
   protected boolean sqlSelect() {
      return true;
   }
   
   @Override
   protected boolean sqlInsert() {
      return true;
   }
   
   @Override
   protected boolean sqlUpdate() {
      return true;
   }
   
   @Override
   protected boolean sqlDelete() {
      return true;
   }
}
