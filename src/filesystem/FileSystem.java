package filesystem;

import filesystem.exceptions.FileSystemRuntimeException;

/**
 * @author Edoardo Luppi
 */
public interface FileSystem
{
   /**
    * Creates the file system.
    *
    * @return The file system itself
    */
   FileSystem create();
   
   /**
    * Closes the file system, freeing any related resource.
    */
   void close();
   
   /**
    * Checks for the file system existence.
    */
   boolean exists();

   /**
    * Returns the unique volume identification.
    */
   String getUniqueId();

   /**
    * Returns the volume name.
    */
   String getVolumeLabel();

   /**
    * Returns the size of cluster in bytes.
    */
   int getClusterSize();

   /**
    * Sets the volume name.
    *
    * @param name
    *           The new volume name
    */
   void setVolumeLabel(final String name);
   
   /**
    * Returns the root directory.
    */
   DirectoryPath getRoot();
   
   /*********************************************************/

   static final int FILE_SYSTEM_ALREADY_EXIST = 0;
   static final int FILE_SYSTEM_DOES_NOT_EXIST = 1;
   static final int PATH_ALREADY_EXISTS = 2;
   static final int PATH_DOES_NOT_EXIST = 3;
   static final int PATH_CANNOT_BE_DELETED = 4;
   static final int ROOT_CANNOT_BE_RENAMED = 5;

   static void error(final int errorCode) {
      error(errorCode, null);
   }
   
   static void error(final int errorCode, String message) {
      if (message == null) {
         message = getErrorMessage(errorCode);
      }
      
      // TODO: throw appropriate exceptions
      switch (errorCode) {
         default:
            throw new FileSystemRuntimeException(message);
      }
   }

   static String getErrorMessage(final int errorCode) {
      // TODO: complete messages
      switch (errorCode) {
         case PATH_ALREADY_EXISTS:
            return "The specified path already exists";
         case PATH_DOES_NOT_EXIST:
            return "The specified path does not exist";
         case PATH_CANNOT_BE_DELETED:
            return "The specified path cannot be deleted";
         default:
            break;
      }

      return "Unknown error";
   }
}
