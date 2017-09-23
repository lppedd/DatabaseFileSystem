package filesystem;

/**
 * In file systems concepts, a cluster is the minimum usable amount of space
 * to store data. It is composed of a fixed number of sectors and its size can be
 * chosen at formatting time only.
 *
 * @author Edoardo Luppi
 */
public interface Cluster
{
   /**
    * Allocates space for this cluster in the file system.
    *
    * @return The cluster itself
    */
   Cluster allocate();
   
   /**
    * Frees the memory allocated by this cluster in the file system.
    *
    * @return The parent file
    */
   FilePath free();

   /**
    * Returns the parent file of the cluster.
    */
   FilePath getParent();

   /**
    * Returns the data associated with the cluster.
    */
   byte[] getData();
}
