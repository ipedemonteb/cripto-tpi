package ar.edu.itba.cripto.steganography;
import java.util.Random;

public class PermutationTable {

    private static final int RND_BOUND = 256;
    private final int[] table;

    public PermutationTable(int seed, int size) {
        this.table = new int[size];
        Random random = new Random();
        random.setSeed(seed);
        for (int i = 0; i < size; i++) {
            table[i] = random.nextInt(RND_BOUND);
        }
    }

    public int[] apply(int[] pixels) {
        if (pixels.length != table.length) {
            throw new IllegalArgumentException(
                    "Expected array length: " + table.length + '\n' +
                    "Actual array length:  " + pixels.length
            );
        }

        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = pixels[i] ^ table[i];
        }

        return result;
    }
}
