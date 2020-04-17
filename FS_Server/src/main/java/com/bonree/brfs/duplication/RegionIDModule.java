package com.bonree.brfs.duplication;

import com.bonree.brfs.common.ZookeeperPaths;
import com.bonree.brfs.common.lifecycle.ManageLifecycle;
import com.bonree.brfs.common.service.ServiceManager;
import com.bonree.brfs.duplication.datastream.connection.DiskNodeConnectionPool;
import com.bonree.brfs.duplication.filenode.FileNodeStorer;
import com.bonree.brfs.duplication.filenode.duplicates.DuplicateNodeSelector;
import com.bonree.brfs.duplication.filenode.duplicates.PartitionNodeSelector;
import com.bonree.brfs.duplication.filenode.duplicates.impl.SimplePartitionNodeSelecotr;
import com.bonree.brfs.duplication.filenode.duplicates.impl.refactor.DuplicateNodeFactory;
import com.bonree.brfs.identification.SecondIdsInterface;
import com.bonree.brfs.identification.VirtualServerID;
import com.bonree.brfs.identification.impl.SecondIDRelationShip;
import com.bonree.brfs.identification.impl.VirtualServerIDImpl;
import com.bonree.brfs.partition.DiskPartitionInfoManager;
import com.bonree.brfs.rebalance.route.RouteLoader;
import com.bonree.brfs.rebalance.route.impl.SimpleRouteZKLoader;
import com.google.inject.*;
import org.apache.curator.framework.CuratorFramework;

/*******************************************************************************
 * 版权信息： 北京博睿宏远数据科技股份有限公司
 * Copyright (c) 2007-2020 北京博睿宏远数据科技股份有限公司，Inc. All Rights Reserved.
 * @date 2020年04月07日 19:27:25
 * @author: <a href=mailto:zhucg@bonree.com>朱成岗</a>
 * @description:
 ******************************************************************************/

public class RegionIDModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(VirtualServerID.class).to(VirtualServerIDImpl.class).in(Scopes.SINGLETON);
        binder.bind(DiskPartitionInfoManager.class).in(ManageLifecycle.class);
    }

    @Provides
    @Singleton
    public VirtualServerIDImpl getVirtualServerId(CuratorFramework client, ZookeeperPaths path) {
        return new VirtualServerIDImpl(client, path.getBaseServerIdSeqPath());
    }

    @Provides
    @Singleton
    public RouteLoader getRouteLoader(CuratorFramework client, ZookeeperPaths zookeeperPaths) {
        return new SimpleRouteZKLoader(client, zookeeperPaths.getBaseRoutePath());
    }

    @Provides
    @Singleton
    public SecondIdsInterface getSecondIdsInterface(CuratorFramework client, ZookeeperPaths zookeeperPaths) {
        try {
            return new SecondIDRelationShip(client, zookeeperPaths.getBaseV2SecondIDPath());
        } catch (Exception e) {
            throw new RuntimeException("create secondIds happen error !", e);
        }
    }

    @Provides
    public PartitionNodeSelector getPartitionNodeSelecotr(DiskPartitionInfoManager diskPartitionInfoManager) {
        return new SimplePartitionNodeSelecotr(diskPartitionInfoManager);
    }

    @Provides
    public DuplicateNodeSelector getDuplicateNodeSelector(ServiceManager serviceManager, DiskNodeConnectionPool connectionPool, FileNodeStorer storer, PartitionNodeSelector pSelector, SecondIdsInterface secondIds, ZookeeperPaths zookeeperPaths, CuratorFramework client) {
        try {
            return DuplicateNodeFactory.create(serviceManager, connectionPool, storer, pSelector, secondIds, zookeeperPaths, client);
        } catch (Exception e) {
            throw new RuntimeException("create duplicateNodeSelector happen error ", e);
        }
    }
}
