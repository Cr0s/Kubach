package kubach.ac;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Cr0s
 *
 * @author Cr0s
 */
public interface Cr0s extends Library {

    public int check(String realm, String path, Pointer buf, int maxlen);
}
