package filesystem.db;

import filesystem.DirectoryPath;
import filesystem.FileSystem;
import filesystem.Path;

/**
 * Represents a database file system path, which could be a file or a directory.
 *
 * @author Edoardo Luppi
 */
public abstract class DbObject implements Path
{
   private final FileSystem fileSystem;
   private final Property<DirectoryPath> parent;
   private final Property<String> name;
   private final Property<Boolean> exists;
   private final Property<Boolean> existsChecked;
   
   public DbObject(final FileSystem fileSystem, final DirectoryPath parent, final String name) {
      this.fileSystem = fileSystem;
      this.parent = new Property<>(parent);
      this.name = new Property<>(name);
      exists = new Property<>(true);
      existsChecked = new Property<>(false);
   }
   
   @Override
   public Path create() {
      if (exists()) {
         FileSystem.error(FileSystem.PATH_ALREADY_EXISTS);
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
      };

      return this;
   }

   @Override
   public DirectoryPath delete() {
      checkPath();
      
      // We have to ensure the root directory is never deleted.
      if (parent == null) {
         FileSystem.error(FileSystem.PATH_CANNOT_BE_DELETED);
      }
      
      new DbSandbox() {
         @Override
         public void run() {
            exists.set(!sqlDelete());
            existsChecked.set(true);
         }
         
         @Override
         public void onFail() {
            exists.undo();
            existsChecked.set(false);
         }
      }.execute(false);

      return parent.get();
   }

   @Override
   public boolean exists() {
      if (!existsChecked.get()) {
         exists.set(sqlSelect());
         existsChecked.set(true);
      }
      
      return exists.get();
   }

   @Override
   public Path moveTo(final DirectoryPath path) {
      checkPath();
      
      new DbSandbox() {
         @Override
         public void beforeRun() {
            parent.set(path);
         }
         
         @Override
         public void run() {
            sqlUpdate();
         }
         
         @Override
         public void onFail() {
            parent.undo();
         }
      }.execute(false);
      
      return this;
   }

   @Override
   public FileSystem getFileSystem() {
      checkPath();
      return fileSystem;
   }
   
   @Override
   public DirectoryPath getParent() {
      checkPath();
      return parent.get();
   }
   
   @Override
   public String getName() {
      checkPath();
      return name.get();
   }
   
   @Override
   public Path rename(final String newName) {
      checkPath();
      
      if (getParent() == null) {
         FileSystem.error(FileSystem.ROOT_CANNOT_BE_RENAMED);
      }

      new DbSandbox() {
         @Override
         public void beforeRun() {
            name.set(newName);
         }
         
         @Override
         public void run() {
            sqlUpdate();
         }
         
         @Override
         public void onFail() {
            name.undo();
         }
      }.execute(false);
      
      return this;
   }
   
   @Override
   public String toString() {
      return (parent.get() == null ? name.get() : parent.toString() + "/" + name.get()).replaceAll("(\\/\\/+)", "/");
   }

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof Path)) {
         return false;
      }
      
      if (this == object) {
         return true;
      }

      final Path other = (Path) object;
      final Path otherParent = other.getParent();
      final Path parent = getParent();
      
      // Is this the root directory?
      if (parent == null) {
         return parent == otherParent;
      }
      
      return parent.getName().equals(otherParent.getName()) && getName().equals(other.getName());
   }
   
   @Override
   public int hashCode() {
      return (getParent() == null ? 0 : getParent().hashCode()) + getName().hashCode();
   }
   
   @Override
   public int compareTo(final Path other) {
      return getName().compareTo(other.getName());
   }
   
   protected final void checkPath() {
      if (!exists()) {
         FileSystem.error(FileSystem.PATH_DOES_NOT_EXIST);
      }
   }
   
   protected abstract boolean sqlSelect();
   
   protected abstract boolean sqlInsert();
   
   protected abstract boolean sqlUpdate();
   
   protected abstract boolean sqlDelete();
}
