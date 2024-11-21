package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.constant.CommonConstant;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author tuyen
 */
public class Utils {

    private static final Random RANDOM;
    private static final DateTimeFormatter DATE_TIME_FORMATTER;
    private static final short[] SIN_DATA;
    private static final short[] COS_DATA;
    private static final int[] TAN_DATA;

    static {
        RANDOM = new Random();
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(CommonConstant.PATTERN_DATE_TIME);
        SIN_DATA = new short[]{
                0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265, 282, 299, 316, 333, 350, 367, 384, 400,
                416, 433, 449, 465, 481, 496, 512, 527, 543, 558, 573, 587, 602, 616, 630, 644, 658, 672, 685, 698, 711, 724, 737,
                749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859, 868, 878, 887, 896, 904, 912, 920, 928, 935, 943, 949, 956,
                962, 968, 974, 979, 984, 989, 994, 998, 1002, 1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024,
                1024,
        };
        COS_DATA = new short[91];
        TAN_DATA = new int[91];
        for (int i = 0; i <= 90; i++) {
            COS_DATA[i] = SIN_DATA[90 - i];
            if (COS_DATA[i] == 0) {
                TAN_DATA[i] = 0x7fffffff;
            } else {
                TAN_DATA[i] = (SIN_DATA[i] << 10) / COS_DATA[i];
            }
        }
    }

    /**
     * Generates a random integer within the specified range using a non-linear distribution.
     * The generated number is more likely to be closer to the minimum value than to the maximum value.
     *
     * @param min the lower bound of the range (inclusive)
     * @param max the upper bound of the range (inclusive)
     * @return a random integer between {@code min} and {@code max} (inclusive)
     * @throws IllegalArgumentException if {@code min} is greater than {@code max}
     */
    public static int getNonLinearRandom(int min, int max) {
        double rand = RANDOM.nextDouble();
        return (int) (min + (1 - Math.sqrt(1 - rand)) * (max - min));
    }

    /**
     * Generates a random integer within the specified range (inclusive).
     *
     * @param x1 the lower bound of the range (inclusive)
     * @param x2 the upper bound of the range (inclusive)
     * @return a random integer between {@code x1} and {@code x2} (inclusive)
     * @throws IllegalArgumentException if {@code x1} is greater than {@code x2}
     */
    public static int nextInt(int x1, int x2) {
        return x1 + RANDOM.nextInt(x2 - x1 + 1);
    }

    public static int nextInt(int max) {
        return RANDOM.nextInt(max);
    }

    public static int nextInt(int[] probabilities) {
        int sum = 0;
        for (int prob : probabilities) {
            sum += prob;
        }

        int randomNumber = RANDOM.nextInt(sum);

        int cumulativeSum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulativeSum += probabilities[i];
            if (randomNumber < cumulativeSum) {
                return i;
            }
        }
        return -1;
    }

    public static String formatThousands(int number) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        return numberFormat.format(number);
    }

    public static String getStringNumber(float num) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        format.setRoundingMode(RoundingMode.DOWN);
        if (num >= 1000000000) {
            return format.format((num / 1000000000)) + "b";
        } else if (num >= 1000000) {
            return format.format((num / 1000000)) + "m";
        } else if (num >= 1000) {
            return format.format((num / 1000)) + "k";
        } else {
            return String.valueOf((int) num);
        }
    }

    public static String getStringTimeBySecond(long s) {
        s = Math.abs(s);
        if (s >= 31104000) {
            return (s / 31104000) + " năm";
        } else if (s >= 2592000) {
            return (s / 2592000) + " tháng";
        } else if (s >= 604800) {
            return (s / 604800) + " tuần";
        } else if (s >= 86400) {
            return (s / 86400) + " ngày";
        } else if (s >= 3600) {
            return (s / 3600) + " giờ";
        } else if (s >= 60) {
            return (s / 60) + " phút";
        } else {
            return s + " giây";
        }
    }

    public static byte[] getFile(String url) {
        Path path = Paths.get(url);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveFile(String url, byte[] data) {
        Path path = Paths.get(url);
        try {
            Files.createDirectories(path.getParent());
            try (FileOutputStream fos = new FileOutputStream(url)) {
                fos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte calculateLevelPercent(float currentXp, float requiredXp) {
        if (requiredXp == 0) {
            return 0;
        }
        int percentage = (int) Math.floor((currentXp / requiredXp) * 100);
        if (percentage < 0) {
            return 0;
        }
        if (percentage > 100) {
            return 100;
        }
        return (byte) percentage;
    }

    public static boolean hasLoggedInOnNewDay(LocalDateTime lastOnline, LocalDateTime now) {
        if (lastOnline.isAfter(now)) {
            return false;
        }
        return !lastOnline.toLocalDate().isEqual(now.toLocalDate());
    }

    public static float getArgXY(float Ax, float Ay, float Bx, float By) {
        float K = Math.abs(Ay - By);
        float D = Math.abs(Ax - Bx);
        float tan = (D / K);
        float IntArg = (float) (Math.toDegrees(Math.atan(tan)));
        if (Ax >= Bx && Ay > By) {
            IntArg += 90;
        } else if (Ax > Bx && Ay <= By) {
            IntArg = (270 - IntArg);
        } else if (Ax <= Bx && Ay < By) {
            IntArg -= 90;
        } else if (Ax < Bx && Ay >= By) {
            IntArg = (270 + IntArg);
        }
        return IntArg;
    }

    public static int toArg0_360(int arg) {
        if (arg >= 360) {
            arg -= 360;
        }
        if (arg < 0) {
            arg += 360;
        }
        return arg;
    }

    public static int cos(int arg) {
        if ((arg = toArg0_360(arg)) >= 0 && arg < 90) {
            return COS_DATA[arg];
        }
        if (arg >= 90 && arg < 180) {
            return -COS_DATA[180 - arg];
        }
        if (arg >= 180 && arg < 270) {
            return -COS_DATA[arg - 180];
        } else {
            return COS_DATA[360 - arg];
        }
    }

    public static int sin(int arg) {
        if ((arg = toArg0_360(arg)) >= 0 && arg < 90) {
            return SIN_DATA[arg];
        }
        if (arg >= 90 && arg < 180) {
            return SIN_DATA[180 - arg];
        }
        if (arg >= 180 && arg < 270) {
            return -SIN_DATA[arg - 180];
        } else {
            return -SIN_DATA[360 - arg];
        }
    }

    public static boolean inRegion(int x, int y, int x0, int y0, int w, int h) {
        return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h;
    }

    public static boolean intersectRegions(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return Math.abs(x1 - x2) <= w2 + w1 && Math.abs(y1 - y2) <= h2 + h1;
    }

    public static boolean isNotAlpha(int rgb) {
        return (rgb >> 24) != 0;
    }

    public static int getArg(int cos, int sin) {
        if (cos == 0) {
            return sin == 0 ? 0 : (sin < 0 ? 270 : 90);
        }
        int arg;
        label2:
        {
            arg = Math.abs((sin << 10) / cos);
            for (int i = 0; i <= 90; i++) {
                if (TAN_DATA[i] < arg) {
                    continue;
                }
                arg = i;
                break label2;
            }
            arg = 0;
        }
        if (sin >= 0 && cos < 0) {
            arg = 180 - arg;
        }
        if (sin < 0 && cos < 0) {
            arg += 180;
        }
        if (sin < 0 && cos >= 0) {
            arg = 360 - arg;
        }
        return arg;
    }

    public static short getShort(byte[] ab, int off) {
        return (short) ((ab[off] & 0xff) << 8 | ab[off + 1] & 0xff);
    }

    /**
     * Formats the given LocalDateTime to a string using the specified pattern.
     *
     * @param dateTime the LocalDateTime to format
     * @return a formatted date-time string
     */
    public static String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Clamps the given value to the specified range [min, max].
     * If the value is less than the minimum, it returns the minimum.
     * If the value is greater than the maximum, it returns the maximum.
     * Otherwise, it returns the value itself.
     *
     * @param value the value to clamp
     * @param min   the minimum value of the range
     * @param max   the maximum value of the range
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

}
