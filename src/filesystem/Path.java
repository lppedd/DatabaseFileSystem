package filesystem;

/**
 * A path represents a point inside a file system.
 * Depending on the file system implementation, as is this one, the path concept can
 * be extended to more fine-grained entities.
 *
 * @author Edoardo Luppi
 */
public interface Path extends Comparable<Path>
{
   /**
    * Creates the path.
    *
    * @return The path itself
    */
   Path create();

   /**
    * Deletes the path and frees any related resource.
    *
    * @return The parent path
    */
   DirectoryPath delete();

   /**
    * Checks for the path existence in the belonging file system.
    */
   boolean exists();

   /**
    * Renames the path.
    *
    * @param name
    *           The new name
    * @return The current path
    */
   Path rename(final String name);

   /**
    * Moves the path under a different directory.
    *
    * @param path
    *           The target directory
    * @return The path itself
    */
   Path moveTo(final DirectoryPath path);

   /**
    * Copies the path under a different directory.
    *
    * @param path
    *           The target directory
    * @return The new path
    */
   Path copyTo(final DirectoryPath path);

   /**
    * Returns the file system to which the path belongs.
    */
   FileSystem getFileSystem();

   /**
    * Returns the parent directory.
    */
   DirectoryPath getParent();
   
   /**
    * Returns the name of the path.
    */
   String getName();
}
