package tv.lycam.server.api.utils;

import java.util.Random;

/**
 * Created by lycamandroid on 2017/6/22.
 */
public class RandomUtils {

    public static String getRandNum(int charCount) {

        String charValue = "";
        for (int i = 0; i < charCount; i++) {
            char c = (char) (randomInt(0, 10) + '0');
            charValue += String.valueOf(c);
        }
        return charValue;
    }


    private static int randomInt(int from, int to) {
        Random r = new Random();
        return from + r.nextInt(to - from);
    }


}
