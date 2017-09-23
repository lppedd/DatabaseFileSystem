package filesystem.db;

import java.util.Stack;

/**
 * Represents an undo-able entity property.
 *
 * @author Edoardo Luppi
 */
public class Property<T>
{
   private final Stack<T> stack = new Stack<>();
   
   public Property(final T value) {
      stack.push(value);
   }

   public T get() {
      return stack.peek();
   }

   public void set(final T value) {
      stack.push(value);
   }

   public T undo() {
      return stack.pop();
   }
   
   @Override
   public String toString() {
      final T last = stack.peek();
      return last == null ? "" : last.toString();
   }

   @Override
   public int hashCode() {
      final T last = stack.peek();
      return last == null ? 0 : last.hashCode();
   }
}
