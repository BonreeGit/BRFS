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

import java.io.IOException;
import java.util.concurrent.Executors;

import com.bonree.brfs.client.discovery.CachedDiscovery;
import com.bonree.brfs.client.discovery.Discovery;
import com.bonree.brfs.client.discovery.HttpDiscovery;
import com.bonree.brfs.client.json.JsonCodec;
import com.bonree.brfs.client.utils.DaemonThreadFactory;
import com.bonree.brfs.client.utils.SocketChannelSocketFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class BRFSClientBuilder {
    private ClientConfiguration config;
    
    public BRFSClientBuilder setConfiguration(ClientConfiguration config) {
        this.config = config;
        return this;
    }
    
    public BRFSClient build() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new AuthorizationIterceptor(config))
                .socketFactory(new SocketChannelSocketFactory())
                .build();
        
        JsonCodec codec = new JsonCodec(new ObjectMapper());
        
        Discovery discovery = new CachedDiscovery(
                new HttpDiscovery(httpClient, config.getRegionNodeAddresses(), codec),
                Executors.newSingleThreadExecutor(new DaemonThreadFactory("brfs-discovery-%s")),
                config.getDiscoveryExpiredDuration(),
                config.getDiscoreryRefreshDuration());
                
        return new BRFS(config, httpClient, discovery, codec);
    }
    
    /**
     * TODO It's dangerous to transfer plain text of password in header
     */
    public static class AuthorizationIterceptor implements Interceptor {
        private final String user;
        private final String passwd;
        
        public AuthorizationIterceptor(String user, String passwd) {
            this.user = user;
            this.passwd = passwd;
        }
        
        private AuthorizationIterceptor(ClientConfiguration config) {
            this.user = config.getUser();
            this.passwd = config.getPasswd();
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request()
                    .newBuilder()
                    .header("username", user)
                    .header("password", passwd)
                    .build());
        }
        
    }
}
