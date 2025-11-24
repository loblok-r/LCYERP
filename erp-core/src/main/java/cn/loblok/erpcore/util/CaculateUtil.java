package cn.loblok.erpcore.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CaculateUtil {

    // 工具方法：限制在 [min, max] 区间
    public static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) return min;
        if (value.compareTo(max) > 0) return max;
        return value;
    }

    // 工具方法：四舍五入到分
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // 简化个税计算（仅支持3%档，实际需完整累进表）
    public static BigDecimal calculateProgressiveTax(BigDecimal taxable) {
        // 中国个税月度税率表（简化版）
        if (taxable.compareTo(BigDecimal.valueOf(36000 / 12)) <= 0) {
            return taxable.multiply(BigDecimal.valueOf(0.03));
        }
        // TODO: 完整实现7级累进（或调用税务 SDK）
        throw new UnsupportedOperationException("仅支持3%税率档位");
    }
}