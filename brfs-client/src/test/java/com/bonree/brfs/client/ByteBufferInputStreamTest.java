/*
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

package com.bonree.brfs.client;

import com.bonree.brfs.client.utils.ByteBufferInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class ByteBufferInputStreamTest {

    /**
     * @param args
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ByteBufferInputStream input = new ByteBufferInputStream(ByteBuffer.wrap("123456".getBytes()));

        System.out.println(new BufferedReader(new InputStreamReader(input)).readLine());
    }

}
