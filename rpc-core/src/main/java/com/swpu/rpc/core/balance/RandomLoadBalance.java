package com.swpu.rpc.core.balance;

import com.swpu.rpc.core.common.ServiceInfo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author lms
 * @Date 2022/8/15 8:48
 * @Description 按权重随机
 */
public class RandomLoadBalance implements LoadBalance {

    @Override
    public ServiceInfo chooseOne(List<ServiceInfo> serviceInfos) {
        // 计算权重总和 例：1 2 2 -> 5
        double originWeightSum = 0;
        for (ServiceInfo serviceInfo : serviceInfos) {
            double weight = serviceInfo.getWeight().doubleValue();
            if (weight <= 0) {
                continue;
            }
            if (Double.isInfinite(weight)) {
                weight = 10000.0D;
            }
            if (Double.isNaN(weight)) {
                weight = 1.0D;
            }
            originWeightSum += weight;
        }

        // 计算每个serviceInfo的实际权重比例 例：1 2 2 -> 0.2 0.4 0.4
        double[] actualWeightRatios = new double[serviceInfos.size()];
        int index = 0;
        for (ServiceInfo serviceInfo : serviceInfos) {
            double weight = serviceInfo.getWeight().doubleValue();
            if (weight <= 0) {
                continue;
            }
            actualWeightRatios[index++] = weight / originWeightSum;
        }
        // 计算每个serviceInfo的权重范围结束位置 例：1 2 2 -> 0.2 0.6 1
        double[] weights = new double[serviceInfos.size()];
        double weightRangeStartPos = 0;
        for (int i = 0; i < index; i++) {
            weights[i] = weightRangeStartPos + actualWeightRatios[i];
            weightRangeStartPos += actualWeightRatios[i];
        }

        // 按权重随机选择
        double random = ThreadLocalRandom.current().nextDouble();
        int i = Arrays.binarySearch(weights, random);
        if (i < 0) {
            i = -i - 1;
        } else {
            return serviceInfos.get(i);
        }

        if (i < weights.length && random < weights[i]) {
            return serviceInfos.get(i);
        }
        // 通常不会走到这里，为了保证能得到正确地返回，这里随便返回一个
        return serviceInfos.get(0);
    }
}
