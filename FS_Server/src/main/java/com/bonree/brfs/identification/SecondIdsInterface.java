package com.bonree.brfs.identification;

import java.util.Collection;
import java.util.Map;

/*******************************************************************************
 * 版权信息： 北京博睿宏远数据科技股份有限公司
 * Copyright (c) 2007-2020 北京博睿宏远数据科技股份有限公司，Inc. All Rights Reserved.
 * @date 2020年03月30日 16:46:59
 * @author: <a href=mailto:zhucg@bonree.com>朱成岗</a>
 * @description: 磁盘信息接口 主要根据一级serverid storageid，获取磁盘节点信息
 ******************************************************************************/

public interface SecondIdsInterface {

    /**
     * 根据storageRegionId与一级server 获取磁盘二级serverid
     *
     * @param serverId
     * @param storageRegionId
     *
     * @return
     */
    Collection<String> getSecondIds(String serverId, int storageRegionId);

    /**
     * 获取二级serverid
     *
     * @param partitionId
     * @param storageRegionId
     *
     * @return
     */
    String getSecondId(String partitionId, int storageRegionId);

    /**
     * 根据 storageId+secondid 获取一级serverId
     *
     * @param secondId
     * @param storageRegionId
     *
     * @return
     */
    String getFirstId(String secondId, int storageRegionId);

    /**
     * 获取磁盘id
     *
     * @param secondId
     * @param storageRegionId
     *
     * @return
     */
    String getPartitionId(String secondId, int storageRegionId);

    /**
     * 获取二级serverid与一级serverid的关系
     * @param storageRegionId
     * @return
     */
    Map<String, String> getSecondFirstRelationship(int storageRegionId);

}
