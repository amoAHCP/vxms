/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.common.util;

/**
 * Created by amo on 20.02.17.
 * Utility to clean up context root
 */
public class URIUtil {

  public static final String ROOT = "/";

  /**
   * clean the path and set correct root
   *
   * @param path the path to clean
   * @return the cleaned path
   */
  public static String cleanPath(String path) {
    return path.startsWith(ROOT) ? path : ROOT + path;
  }


  /**
   * clean the context root
   *
   * @param contextRoot the context root to clean
   * @return the cleaned context root
   */
  public static String getCleanContextRoot(String contextRoot) {
    if (String.valueOf(contextRoot.charAt(contextRoot.length() - 1)).equals(ROOT)) {
      String _root = contextRoot.substring(0, contextRoot.length() - 1);
      return _root.startsWith(ROOT) ? _root : ROOT + _root;
    } else if (!contextRoot.startsWith(ROOT)) {
      return ROOT + contextRoot;
    }
    return contextRoot;
  }

  /**
   * check if context root is set
   *
   * @param cRoot the URI string
   * @return true if context root is set
   */
  public static boolean isContextRootSet(String cRoot) {
    return !cRoot.trim().equals(ROOT) && cRoot.length() > 1;
  }
}
