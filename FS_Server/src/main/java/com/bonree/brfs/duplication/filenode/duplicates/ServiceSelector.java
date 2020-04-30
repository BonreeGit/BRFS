package com.bonree.brfs.duplication.filenode.duplicates;

import com.bonree.brfs.resource.vo.ResourceModel;
import java.util.Collection;

public interface ServiceSelector {
    /**
     * 过滤限制的服务的serviceids
     */
    Collection<ResourceModel> filterService(Collection<ResourceModel> resourceModels, String path);

    /**
     * 选择服务
     *
     * @param resources
     * @param path
     * @param num
     *
     * @return
     */
    Collection<ResourceModel> selector(Collection<ResourceModel> resources, String path, int num);
}
