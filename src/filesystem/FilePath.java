package filesystem;

/**
 * Represents a file in a file system. Certain methods are overridden to match
 * the correct return type.
 *
 * @author Edoardo Luppi
 */
public interface FilePath extends Path
{
   @Override
   FilePath create();

   @Override
   FilePath rename(final String name);
   
   @Override
   FilePath moveTo(final DirectoryPath path);

   @Override
   FilePath copyTo(final DirectoryPath path);

   /**
    * Sets the file path data.
    *
    * @param file
    *           The local file from which to obtain the data
    * @return The current path
    */
   FilePath setData(final byte[] file);

   /**
    * Returns the current file path data.
    */
   byte[] getData();
}
