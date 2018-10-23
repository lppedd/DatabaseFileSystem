package filesystem.exceptions;

/**
 * @author Edoardo Luppi
 */
public class FileSystemRuntimeException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public FileSystemRuntimeException(final String message) {
      super(message);
   }
}
