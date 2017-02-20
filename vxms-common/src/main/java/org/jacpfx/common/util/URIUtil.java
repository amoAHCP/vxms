package org.jacpfx.common.util;

/**
 * Created by amo on 20.02.17.
 */
public class URIUtil {
    public static final String ROOT = "/";
    public static String cleanPath(String path) {
        return path.startsWith(ROOT) ? path : ROOT + path;
    }


    public static String getCleanContextRoot(String contextRoot) {
        if (String.valueOf(contextRoot.charAt(contextRoot.length() - 1)).equals(ROOT)) {
            String _root = contextRoot.substring(0, contextRoot.length() - 1);
            return _root.startsWith(ROOT) ? _root : ROOT + _root;
        } else if (!contextRoot.startsWith(ROOT)) {
            return ROOT + contextRoot;
        }
        return contextRoot;
    }

    public static boolean isContextRootSet(String cRoot) {
        return !cRoot.trim().equals(ROOT) && cRoot.length() > 1;
    }
}
