/*
Credit to @TelepathicGrunt
This code was decompiled (and translated from C#) from MinecraftSeedHash.exe, file which can be found here:
https://aminoapps.com/c/minecraft/page/blog/mc-seed-number-to-word-form-generator/32uB_uG3N38Kj6wNLJnknMRveQYKX5
*/

package util;

public class HashCoding {
    private boolean simpleSeed = true;
    private static final int minChar = 65;
    private static final int middleMinChar = 91;
    private static final int middleMaxChar = 96;
    private static final int maxChar = 122;
    private static final int maxStrLength = 12;
    private static long[] minHashValues;
    private static long[] maxHashValues;

    public static void init() {
        HashCoding.minHashValues = new long[13];
        for (int exponent = 0; exponent <= 12; ++exponent)
            HashCoding.minHashValues[exponent] = ((long) Math.pow(31, exponent)) / 30L * minChar;
        HashCoding.maxHashValues = new long[13];
        for (int exponent = 0; exponent <= 12; ++exponent)
            HashCoding.maxHashValues[exponent] = ((long) Math.pow(31, exponent)) / 30L * maxChar;
    }

    public String getStrFromHash(int hash) { // I don't have any clue on how this works
        long hash1 = ((long) (Integer) hash);
        long num = 4294967296L;
        String result = "";

        for (int length = 0; length <= maxStrLength; ++length) {
            long maxHashValue = HashCoding.maxHashValues[length];

            if (hash1 <= maxHashValue) {
                long minHashValue = HashCoding.minHashValues[length];

                while (hash1 < minHashValue)
                    hash1 += num;

                for (; hash1 <= maxHashValue; hash1 += num) {
                    this.simpleSeed = true;
                    result = this.getStrFromLongHash(hash1, new char[length], length - 1);
                    if (!result.equals("")) return result;
                }
            }
        }
        return result;
    }

    private String getStrFromLongHash(long hash, char[] chars, int charPos) {
        if (hash <= maxChar) {
            if (hash < minChar || hash >= middleMinChar && hash <= middleMaxChar)
                return "";
            char ch1 = (char) hash;
            chars[charPos] = ch1;
            for (char ch2 : chars) {
                if ((ch2 < 'A' || ch2 > 'Z') && (ch2 < 'a' || ch2 > 'z')) {
                    this.simpleSeed = false;
                    break;
                }
            }
            if (this.simpleSeed)
                return new String(chars);
        }
        else {
            char ch = (char) ((Long) hash % new Long(31L));
            while (ch < 'A')
                ch += '\u001F';
            //noinspection LoopStatementThatDoesntLoop
            for (; ch <= 'z' && (charPos != chars.length - 1 || (ch < '[' || ch > '`')); ch += '\u001F') { // I think this loop isn't needed
                chars[charPos] = ch;
                return this.getStrFromLongHash( (hash - (long) ch) / 31L, chars, charPos - 1);
            }
        }
        return "";
    }
}
