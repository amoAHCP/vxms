/*
 * Copyright [2018] [Andy Moncsek]
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

package org.jacpfx.vxms.k8s.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to get default account token in a container
 * @author Andy Moncsek
 */
public class TokenUtil {
  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static Logger log = Logger.getLogger(TokenUtil.class.getName());

  public static String getAccountToken() {
    try {
      final Path path = Paths.get(IO_SERVICEACCOUNT_TOKEN);
      if (!path.toFile().exists()) {
        log.log(Level.WARNING, "no token found");
        return null;
      }
      return new String(Files.readAllBytes(path));

    } catch (IOException e) {
      throw new RuntimeException("Could not get token file", e);
    }
  }
}
