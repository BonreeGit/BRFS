package com.bonree.brfs.server.identification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.bonree.brfs.common.zookeeper.curator.CuratorClient;
import com.bonree.brfs.server.identification.impl.ZookeeperIdentification;

public class IdentificationTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService threads = Executors.newFixedThreadPool(10);
        final List<String> sigleServerIdList = new ArrayList<String>();
        final List<String> multiServerIdList = new ArrayList<String>();
        final List<String> virtualServerIdList = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            threads.execute(new Runnable() {
                
                @Override
                public void run() {
                    CuratorClient client = CuratorClient.getClientInstance("192.168.101.86:2181");
                    int count = 0;
                    while(count<10) {
                        count++;
                        Identification instance = new ZookeeperIdentification(client, "/brfs/wz/serverID");
                        sigleServerIdList.add(instance.getSingleIdentification());
                        multiServerIdList.add(instance.getMultiIndentification());
                        virtualServerIdList.add(instance.getVirtureIdentification());
                    }
                    
                }
            });
        }
        
        threads.shutdown();
        threads.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        
        Set<String> singleSet = new HashSet<>();
        Set<String> multiSet = new HashSet<>();
        Set<String> virtualSet = new HashSet<>();
        singleSet.addAll(sigleServerIdList);
        multiSet.addAll(multiServerIdList);
        virtualSet.addAll(virtualServerIdList);
        
        System.out.println(sigleServerIdList.size() + "--" + singleSet.size());
        
        System.out.println(multiServerIdList.size() + "--" + multiSet.size());
        
        System.out.println(virtualServerIdList.size() + "--" + virtualSet.size());
    }

}