import java.util.Random;

public class Rand {
    public static int genNormalInt(int mean, int min, int max) {
        Random random = new Random();
        double sigma = Math.max(mean - min, max - mean) / 3.0;
        int randVal = (int) Math.round(random.nextGaussian() * sigma) + mean;
        if (randVal < min) randVal = min;
        if (randVal > max) randVal = max;
        return randVal;
    }

    public static int genInt(int end) {
        return genInt(0, end - 1);
    }

    public static int genInt(int start, int end) {
        Random random = new Random();
        return start + random.nextInt(end - start + 1);
    }
}
