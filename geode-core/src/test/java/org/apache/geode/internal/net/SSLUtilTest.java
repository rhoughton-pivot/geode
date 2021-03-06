/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.internal.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

public class SSLUtilTest {

  @Test(expected = NoSuchAlgorithmException.class)
  public void failWhenNothingIsRequested() throws Exception {
    SSLConfig sslConfig = mock(SSLConfig.class);
    when(sslConfig.getProtocolsAsStringArray())
        .thenReturn(new String[0]);
    SSLUtil.getSSLContextInstance(sslConfig);
  }

  @Test(expected = NoSuchAlgorithmException.class)
  public void failWithAnUnknownProtocol() throws Exception {
    SSLConfig sslConfig = mock(SSLConfig.class);
    when(sslConfig.getProtocolsAsStringArray())
        .thenReturn(new String[] {"boulevard of broken dreams"});
    SSLUtil.getSSLContextInstance(sslConfig);
  }

  @Test
  public void getASpecificProtocol() throws Exception {
    SSLConfig sslConfig = mock(SSLConfig.class);
    when(sslConfig.getProtocolsAsStringArray()).thenReturn(new String[] {"TLSv1.2"});
    final SSLContext sslContextInstance = SSLUtil.getSSLContextInstance(sslConfig);
    assertThat(sslContextInstance.getProtocol().equalsIgnoreCase("TLSv1.2")).isTrue();
  }

  @Test
  public void getAnyProtocolWithAnUnknownInTheList() throws Exception {
    SSLConfig sslConfig = mock(SSLConfig.class);
    when(sslConfig.getProtocolsAsStringArray())
        .thenReturn(new String[] {"the dream of the blue turtles", "any", "SSL"});
    final SSLContext sslContextInstance = SSLUtil.getSSLContextInstance(sslConfig);
    // make sure that we don't continue past "any" and use the following protocol (SSL)
    assertThat(sslContextInstance.getProtocol().equalsIgnoreCase("SSL")).isFalse();
    String selectedProtocol = sslContextInstance.getProtocol();
    String matchedProtocol = null;
    for (String algorithm : SSLUtil.DEFAULT_ALGORITMS) {
      if (algorithm.equalsIgnoreCase(selectedProtocol)) {
        matchedProtocol = algorithm;
      }
    }
    assertThat(matchedProtocol).isNotNull().withFailMessage("selected protocol ("
        + selectedProtocol +
        ") is not in the list of default algorithms, "
        + "indicating that the \"any\" setting did not work correctly");
  }

  @Test
  public void testAnyAndTLSv1_3() throws NoSuchAlgorithmException {
    final String[] algorithms = {"any"};
    final SSLContext sslContextInstance =
        SSLUtil.findSSLContextForProtocols(algorithms, SSLUtil.getDefaultAlgorithms());
    if (sslContextInstance.getProtocol().equalsIgnoreCase("tlsv1.3")) {
      // GEODE-8463: TLSV1.3 is not supported by Geode until Java 11
      assertThat(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_11));
    }
  }

  @Test
  public void testTLSv1_3_specified() {
    final String[] algorithms = {"TLSv1.3"};
    try {
      final SSLContext sslContextInstance =
          SSLUtil.findSSLContextForProtocols(algorithms, SSLUtil.getDefaultAlgorithms());
      if (sslContextInstance.getProtocol().equalsIgnoreCase("tlsv1.3")) {
        // GEODE-8463: TLSV1.3 is not supported by Geode until Java 11
        assertThat(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_11));
      }
    } catch (IllegalStateException e) {
      assertThat(SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_10));
    } catch (NoSuchAlgorithmException e) {
      // TLSv1.3 is not available in this JVM
      try {
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        fail("Expected TLSv1.3 to not be supported in this JVM but found " + context);
      } catch (NoSuchAlgorithmException e2) {
        // expected
      }
    }
  }

  @Test
  public void getARealProtocolAfterProcessingAny() throws Exception {
    final String[] algorithms = {"dream weaver", "any", "TLSv1.2"};
    final String[] algorithmsForAny = new String[] {"sweet dreams (are made of this)"};
    final SSLContext sslContextInstance = SSLUtil.findSSLContextForProtocols(algorithms,
        algorithmsForAny);
    assertThat(sslContextInstance.getProtocol().equalsIgnoreCase("TLSv1.2")).isTrue();
  }

}
