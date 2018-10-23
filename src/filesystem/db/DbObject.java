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
   private DirectoryPath parent;
   private String name;
   private boolean exists;

	DbObject(final FileSystem fileSystem, final DirectoryPath parent, final String name) {
      this.fileSystem = fileSystem;
      this.parent = parent;
      this.name = name;
      exists = false;
   }

   @Override
   public Path create() {
      if (exists()) {
         FileSystem.error(FileSystem.PATH_ALREADY_EXISTS);
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
            exists = !sqlDelete();
         }

         @Override
         public void onFail() {
            exists = true;
         }
      }.execute(false);

      return parent;
   }

   @Override
   public boolean exists() {
      exists = sqlSelect();
      return exists;
   }

   @Override
   public Path moveTo(final DirectoryPath path) {
      checkPath();

      final DirectoryPath oldParent = parent;

      new DbSandbox() {
         @Override
         public void beforeRun() {
            parent = path;
         }

         @Override
         public void run() {
            sqlUpdate();
         }

         @Override
         public void onFail() {
            parent = oldParent;
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
      return parent;
   }

   @Override
   public String getName() {
      checkPath();
      return name;
   }

   @Override
   public Path rename(final String newName) {
      checkPath();

      if (getParent() == null) {
         FileSystem.error(FileSystem.ROOT_CANNOT_BE_RENAMED);
      }

      final String oldName = name;

      new DbSandbox() {
         @Override
         public void beforeRun() {
            name = newName;
         }

         @Override
         public void run() {
            sqlUpdate();
         }

         @Override
         public void onFail() {
            name = oldName;
         }
      }.execute(false);

      return this;
   }

   @Override
   public String toString() {
      return (parent == null ? name : parent + "/" + name).replaceAll("(//+)", "/");
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

   final void checkPath() {
      if (!exists()) {
         FileSystem.error(FileSystem.PATH_DOES_NOT_EXIST);
      }
   }

   protected abstract boolean sqlSelect();

   protected abstract boolean sqlInsert();

   protected abstract boolean sqlUpdate();

   protected abstract boolean sqlDelete();
}
