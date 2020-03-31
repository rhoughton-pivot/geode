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
 *
 */

package org.apache.geode.redis.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyHashIdentifierTest {

  @Test
  public void equals_shouldReturnTrue_givenDistinctObjectsWithSameValue() {
    KeyHashIdentifier key1 = new KeyHashIdentifier(new byte[] {0, 1, 2, 3});
    KeyHashIdentifier key2 = new KeyHashIdentifier(new byte[] {0, 1, 2, 3});

    assertThat(key1.equals(key2))
        .as("equals() method should return true")
        .isTrue();
  }

  @Test
  public void equals_shouldReturnFalse_givenDistinctObjectsWithDifferentValues() {
    KeyHashIdentifier key1 = new KeyHashIdentifier(new byte[] {0, 1, 2, 3});
    KeyHashIdentifier key2 = new KeyHashIdentifier(new byte[] {0, 1, 2, 4});

    assertThat(key1.equals(key2))
        .as("equals() method should return false")
        .isFalse();
  }
}
