package com.teamobi.mobiarmy2.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    /**
     * Tạo một số nguyên ngẫu nhiên trong khoảng chỉ định bằng cách sử dụng phân phối không tuyến tính.
     * Số được tạo ra có khả năng gần với giá trị tối thiểu hơn là giá trị tối đa.
     *
     * @param min giới hạn dưới của khoảng (bao gồm cả min)
     * @param max giới hạn trên của khoảng (bao gồm cả max)
     * @return một số nguyên ngẫu nhiên giữa {@code min} và {@code max} (bao gồm cả hai)
     * @throws IllegalArgumentException nếu {@code min} lớn hơn {@code max}
     */
    public static int getNonLinearRandom(int min, int max) {
        double rand = ThreadLocalRandom.current().nextDouble();
        return (int) (min + (1 - Math.sqrt(1 - rand)) * (max - min));
    }

    /**
     * Tạo một số nguyên ngẫu nhiên trong khoảng chỉ định (bao gồm cả hai đầu).
     *
     * @param min giới hạn dưới (bao gồm cả min)
     * @param max giới hạn trên (bao gồm cả max)
     * @return một số nguyên ngẫu nhiên giữa {@code min} và {@code max} (bao gồm cả hai)
     * @throws IllegalArgumentException nếu {@code min} lớn hơn {@code max}
     */
    public static int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min phải nhỏ hơn hoặc bằng max");
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Tạo một số nguyên ngẫu nhiên từ 0 (bao gồm) đến max (không bao gồm).
     *
     * @param max giới hạn trên (phải lớn hơn 0)
     * @return một số nguyên ngẫu nhiên
     * @throws IllegalArgumentException nếu max <= 0
     */
    public static int nextInt(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("max phải lớn hơn 0 (hiện tại max = " + max + ")");
        }
        return ThreadLocalRandom.current().nextInt(max);
    }

    /**
     * Chọn một chỉ số (index) ngẫu nhiên từ mảng xác suất cung cấp.
     *
     * @param probabilities Mảng chứa các mức xác suất
     * @return chỉ số được chọn, hoặc -1 nếu không tìm thấy
     */
    public static int nextInt(int[] probabilities) {
        int sum = 0;
        for (int prob : probabilities) {
            sum += prob;
        }

        if (sum <= 0) {
            return -1;
        }

        int randomNumber = ThreadLocalRandom.current().nextInt(sum);

        int cumulativeSum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeSum += probabilities[i];
            if (randomNumber < cumulativeSum) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Trả về một giá trị boolean ngẫu nhiên.
     *
     * @return true hoặc false ngẫu nhiên
     */
    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
