package filesystem.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import filesystem.DirectoryPath;
import filesystem.FilePath;
import filesystem.FileSystem;
import filesystem.Path;

/**
 * Represents a directory in a database file system.
 *
 * @author Edoardo Luppi
 */
public class DbDirectory extends DbObject implements DirectoryPath
{
   /**
    * The paths which resides under this path.
    */
   private Collection<Path> children;
   
   /**
    * Construct a new directory in the belonging file system.
    * This will only be stored in memory until {@link #create()} is called.
    *
    * @param fileSystem
    *           The file system to which this directory belongs to
    * @param parent
    *           The parent directory of this directory
    * @param name
    *           The name of this directory
    */
   public DbDirectory(final FileSystem fileSystem, final DirectoryPath parent, final String name) {
      super(fileSystem, parent, name);
   }
   
   @Override
   public DirectoryPath create() {
      return (DirectoryPath) super.create();
   }
   
   @Override
   public DirectoryPath delete() {
      final Collection<Path> children = getChildren();
      
      for (final Path child : children) {
         child.delete();
      }
      
      children.clear();
      return super.delete();
   }
   
   @Override
   public DirectoryPath moveTo(final DirectoryPath path) {
      return (DirectoryPath) super.moveTo(path);
   }
   
   @Override
   public DirectoryPath createDirectory(final String name) {
      // The first entry represents this directory new child path while
      // the second entry represents the remaining paths to be created.
      final String[] paths = name.split("/", 2);
      
      // We check if there is already a child path with that name.
      final Collection<Path> children = getChildren();
      final Optional<Path> childPath = children.stream()
            .filter(cp -> cp instanceof DirectoryPath && cp.getName().equals(paths[0]))
            .findFirst();
      DirectoryPath child = null;
      
      if (childPath.isPresent()) {
         child = (DirectoryPath) childPath.get();
         
         if (paths.length == 1) {
            // Being that this directory is the last in the chain (see above),
            // we have to let the user know that it already exists.
            // The user will then take the appropriate decision (overwriting/merging/deleting).
            FileSystem.error(FileSystem.PATH_ALREADY_EXISTS);
         }
      } else {
         child = new DbDirectory(getFileSystem(), this, paths[0]).create();
         children.add(child);
         
         if (paths.length == 1) {
            return child;
         }
      }
      
      return child.createDirectory(paths[1]);
   }
   
   @Override
   public FilePath createFile(final String name) {
      // Being that a name could be as follow: dir/dir2/dir3/myfile
      // we have to split it in two parts:
      // 1. dir/dir2/dir3
      // 2. myfile
      // Then, we have to create the parent directory structure with the first chunk
      // and the file with the second chunk.
      final int lastSeparatorIndex = name.lastIndexOf("/");
      
      if (lastSeparatorIndex < 0) {
         final Collection<Path> children = getChildren();
         
         // We check if a path with the same name already exists under this path.
         final Optional<Path> childPath = children.stream()
               .filter(cp -> cp.getName().equals(name))
               .findFirst();
         
         if (childPath.isPresent()) {
            FileSystem.error(FileSystem.PATH_ALREADY_EXISTS);
         }
         
         final FilePath file = new DbFile(getFileSystem(), this, name).create();
         children.add(file);
         return file;
      }
      
      return createDirectory(name.substring(0, lastSeparatorIndex))
            .createFile(name.substring(lastSeparatorIndex + 1));
   }
   
   @Override
   public DirectoryPath copyTo(final DirectoryPath path) {
      final DirectoryPath copiedDirectory = new DbDirectory(getFileSystem(), path, getName()).create();
      final Collection<Path> copiedDirectoryChildren = copiedDirectory.getChildren();
      
      for (final Path child : getChildren()) {
         copiedDirectoryChildren.add(child.copyTo(copiedDirectory));
      }
      
      return copiedDirectory;
   }
   
   @Override
   public Optional<Path> getPath(final String name) {
      // The first entry represents the path to search for under this path while
      // the second entry represents the remaining paths to go through.
      final String[] paths = name.split("/", 2);
      final Optional<Path> childPath = getChildren().stream()
            .filter(cp -> cp.getName().equals(paths[0]))
            .findFirst();
      final boolean found = childPath.isPresent();
      
      // If the requested path doesn't consist of multiple child paths or
      // if the first path in the path chain doesn't exists or
      // if the requested path is a file, we can stop here.
      if (paths.length == 1 || !found || found && childPath.get() instanceof FilePath) {
         return childPath;
      }
      
      return ((DirectoryPath) childPath.get()).getPath(paths[1]);
   }
   
   @Override
   public Collection<Path> getChildren() {
      if (children == null) {
         children = new ArrayList<>();
         
         new DbSandbox() {
            @Override
            public void run() {
               sqlSelect();
            }
         }.execute(false);
      }
      
      return children;
   }
   
   @Override
   public DirectoryPath rename(final String newName) {
      return (DirectoryPath) super.rename(newName);
   }

   /**
    * Persists this directory instance into the appropriate database table(s).
    */
   @Override
   protected boolean sqlInsert() {
      return true;
   }
   
   /**
    * Queries the appropriate database table(s) for retrieving the child paths.
    */
   @Override
   protected boolean sqlSelect() {
      return true;
   }
   
   /**
    * Updates this directory instance representation in the appropriate database
    * table(s)
    */
   @Override
   protected boolean sqlUpdate() {
      return true;
   }
   
   /**
    * Deletes this directory instance representation in the appropriate database
    * table(s).
    */
   @Override
   protected boolean sqlDelete() {
      return true;
   }
}
