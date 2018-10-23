package filesystem.test;

import filesystem.DirectoryPath;
import filesystem.FileSystem;
import filesystem.Path;
import filesystem.db.DbFileSystem;

import java.util.Optional;

public class Test {
   public static void main(final String... args) {
      try (final FileSystem fileSystem = new DbFileSystem("TEST", "root", 1024).create()) {
         final Path filePath = fileSystem
               .getRoot()
               .createDirectory("dir1/dir2/dir3")
               .createDirectory("dir4")
               .createFile("dir5/dir6/myfile");

         final Optional<Path> directoryPath = fileSystem.getRoot().getPath("dir1/dir2/dir3");

         if (directoryPath.isPresent()) {
            final Path copiedFilePath = filePath.getParent().copyTo((DirectoryPath) directoryPath.get());
         }
      }
   }
}
