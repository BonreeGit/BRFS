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
package com.bonree.brfs.client.data.read.connection;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;

public class DataConnection implements Closeable {
    private final URI uri;
    private final Socket socket;
    
    public DataConnection(URI uri, Socket socket) {
        this.uri = requireNonNull(uri);
        this.socket = requireNonNull(socket);
    }
    
    public URI uri() {
        return uri;
    }
    
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
