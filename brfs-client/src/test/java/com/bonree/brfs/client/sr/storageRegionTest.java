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
package com.bonree.brfs.client.sr;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.bonree.brfs.client.BRFSClient;
import com.bonree.brfs.client.BRFSClientBuilder;
import com.bonree.brfs.client.ClientConfigurationBuilder;
import com.bonree.brfs.client.storageregion.StorageRegionInfo;
import com.bonree.brfs.client.storageregion.UpdateStorageRegionRequest;

public class storageRegionTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try (BRFSClient client = new BRFSClientBuilder()
                .setConfiguration(new ClientConfigurationBuilder()
                        .setUser("root")
                        .setPasswd("12345")
                        .setRegionNodeAddresses(new URI[] {URI.create("http://localhost:8100")})
                        .build())
                .build()) {
            
            try {
//                StorageRegionID sr = client.createStorageRegion(CreateStorageRegionRequest.newBuilder()
//                        .setName("new_test")
//                        .build());
                
//                boolean exist = client.doesStorageRegionExists("new_test");
                
//                List<String> srs = client.listStorageRegions();
                
                StorageRegionInfo info = client.getStorageRegionInfo("new_test");
                
//                boolean updated = client.updateStorageRegion("new_test", UpdateStorageRegionRequest.newBuilder()
//                        .setEnabled(false)
//                        .setDataTTL("P20D")
//                        .setFileCapacity(100)
//                        .setFilePartition("PT2H")
//                        .setReplicateNum(4)
//                        .build());
                
//                client.deleteStorageRegion("new_test");
                System.out.println(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
