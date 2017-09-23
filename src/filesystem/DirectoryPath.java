package filesystem;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents a directory in a file system. Certain methods are overridden to match
 * the correct return type.
 *
 * @author Edoardo Luppi
 */
public interface DirectoryPath extends Path
{
   @Override
   DirectoryPath create();

   @Override
   DirectoryPath rename(final String name);
   
   @Override
   DirectoryPath moveTo(final DirectoryPath path);
   
   @Override
   DirectoryPath copyTo(final DirectoryPath path);

   /**
    * Creates a new directory under the current path.
    *
    * @param name
    *           The new directory name
    * @return The new directory
    */
   DirectoryPath createDirectory(final String name);
   
   /**
    * Creates a new file under the current path.
    *
    * @param name
    *           The new file name
    * @return The new file
    */
   FilePath createFile(final String name);
   
   /**
    * Returns an {@link Optional} representing a path (a directory or a file) under the
    * current path, if it is found,
    * or a an {@link Optional#empty()} if it not found.
    *
    * @param name
    *           The path name
    */
   Optional<Path> getPath(final String name);
   
   /**
    * Returns every path under the current path.
    */
   Collection<Path> getChildren();
}
