package com.agp.cloud.search.core.service.supports.resovler;

import com.google.common.collect.Lists;
import com.agp.cloud.search.cache.config.supports.ConfigCacheSupport;
import com.agp.cloud.search.core.service.IApiService;
import com.agp.cloud.search.core.service.supports.IDataNodeResolver;
import com.agp.cloud.search.core.service.supports.StartParamsHolder;
import com.agp.cloud.search.support.base.utils.ListUtils;
import com.agp.cloud.search.support.base.utils.StringUtils;
import com.agp.cloud.search.support.enums.BranchCombineType;
import com.agp.cloud.search.support.enums.FlowNodeType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 并行分支/合并节点解析.
 *
 * @CreateDate: 2019/9/5 16:22
 * @Version: 1.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class ParallelBranchNodeResovler implements IDataNodeResolver {

    private IApiService apiService;

    private ConfigCacheSupport configCacheSupport;

    private ExecutorService executorService;

    @Override
    public List<Map<String, String>> process(String flowId, String nodeKey, List<Map<String, String>> params) throws Exception {
        Map<String, String> nodeInfo = configCacheSupport.getNonStartNodeInfo(flowId, nodeKey);
        String nextNodeKey = nodeInfo.get("toNodeKey");
        if (StringUtils.isNotBlank(nextNodeKey)) {
            Map<String, List<Map<String, String>>> branchResult = new ConcurrentHashMap<>();
            String startParamsContent = StartParamsHolder.get();
            CompletableFuture[] futures = Arrays.stream(StringUtils.split(nextNodeKey, ",")).map(key -> CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            StartParamsHolder.set(startParamsContent);
                            Map<String, String> branchStartNodeInfo = configCacheSupport.getNonStartNodeInfo(flowId, key);
                            return apiService.resolveNode(flowId, branchStartNodeInfo, params);
                        } catch (Exception e) {
                            log.error("分支节点线{}解析失败:{}", key, e);
                            return null;
                        }
                    }, executorService)
                    .whenComplete((data, exception) -> {
                        if (CollectionUtils.isNotEmpty(data)) {
                            branchResult.put(key, data);
                        }
                    }))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
            return combineData(branchResult, nodeInfo.get("combineType"), nodeInfo.get("combineKeys"));
        }
        return null;
    }

    /**
     * 分支合并节点合并关键字为开始节点入参的标记.
     */
    private final String COMBINE_KEY_START_NODE_TAG = "start.";

    /**
     * 数据合并处理.
     *
     * @param branchResult  分支执行结果数据
     * @param combineTypeCode  合并类型
     * @return
     */
    private List<Map<String, String>> combineData(Map<String, List<Map<String, String>>> branchResult,
                                                  String combineTypeCode, String combineKeys) {
        if (MapUtils.isNotEmpty(branchResult)) {
            BranchCombineType combineType = BranchCombineType.valueOf(combineTypeCode);
            if (combineType == BranchCombineType.UNION_ALL) {
                List<Map<String, String>> combineResult = new ArrayList<>();
                branchResult.forEach((key, data) -> combineResult.addAll(data));
                return combineResult;
            } else if (combineType == BranchCombineType.NATURAL_JOIN) {
                List<List<Map<String, String>>> allResults = Lists.newArrayList(branchResult.values());
                String[] combineKeysArr = StringUtils.isBlank(combineKeys)
                        || combineKeys.contains(COMBINE_KEY_START_NODE_TAG) ? null : combineKeys.split(",");
                List<Map<String, String>> combineResult = new ArrayList<>();
                for (int i = 0; i < allResults.size(); i++) {
                    if (i == 0) {
                        combineResult = combineData(allResults.get(0), null, combineType, combineKeysArr);
                    } else {
                        combineResult = combineData(combineResult, allResults.get(i), combineType, combineKeysArr);
                    }
                }
                return combineResult;
            }
        }
        return null;
    }

    /**
     * 两集合合并.
     *
     * @param left  左侧集合
     * @param right  右侧集合
     * @param combineType  合并方式
     * @return
     */
    private List<Map<String, String>> combineData(List<Map<String, String>> left, List<Map<String , String>> right,
                                                  BranchCombineType combineType, String[] combineKeys) {
        if (CollectionUtils.isEmpty(left) && CollectionUtils.isEmpty(right)) {
            return null;
        }
        if (CollectionUtils.isEmpty(left) || CollectionUtils.isEmpty(right)) {
            return CollectionUtils.isEmpty(left) ? right : left;
        }
        List<Map<String, String>> mainData = left.size() >= right.size() ? left : right;
        List<Map<String, String>> lessData = left.size() >= right.size() ? right : left;
        List<Map<String, String>> result = new ArrayList<>();
        if (combineType == BranchCombineType.NATURAL_JOIN) {
            if (ArrayUtils.isEmpty(combineKeys)) {
                for (Map<String, String> leftData : mainData) {
                    lessData.forEach(rightData -> {
                        leftData.putAll(rightData);
                        result.add(leftData);
                    });
                }
            } else {
                Map<String, List<Map<String, String>>> rightMap = ListUtils.toListMap(lessData, rightData -> buildDataKey(rightData, combineKeys));
                for (Map<String, String> leftData : mainData) {
                    String dataKey = buildDataKey(leftData, combineKeys);
                    List<Map<String, String>> list = rightMap.get(dataKey);
                    if (CollectionUtils.isNotEmpty(list)) {
                        list.forEach(leftData::putAll);
                    }
                    result.add(leftData);
                }
            }
        }
        return result;
    }

    /**
     * 根据具体数据生成合并Key.
     *
     * @param data  集合数据
     * @param combineKeys  合并关键字
     * @return
     */
    private String buildDataKey(Map<String , String> data, String[] combineKeys) {
        if (ArrayUtils.isEmpty(combineKeys)) {
            return "";
        }
        String dataKey = data.get(combineKeys[0]);
        for (int i = 1; i < combineKeys.length; i++) {
            String value = data.get(combineKeys[i]);
            if (StringUtils.isNotBlank(value)) {
                dataKey = String.format("%s_%s", dataKey, value);
            }
        }
        return dataKey;
    }

    @Override
    public String getType() {
        return FlowNodeType.PARALLEL_BRANCH.name();
    }
}
