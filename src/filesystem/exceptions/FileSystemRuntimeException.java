package filesystem.exceptions;

public class FileSystemRuntimeException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public FileSystemRuntimeException() {
      super();
   }

   public FileSystemRuntimeException(final String message) {
      super(message);
   }
}
