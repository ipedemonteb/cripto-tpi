package ar.edu.itba.cripto.steganography;

public class WuLoPolynomial {

    private static final int PRIME = 257;
    private static final int MAX_ADJUSTMENT_ITERATIONS = 5000;

    private final int[] coefficients;

    public WuLoPolynomial(int[] coefficients) {
        this.coefficients = coefficients.clone();
    }

    public int[] computeShares(int n) {
        int[] shares = new int[n];
        boolean overflow;
        int iterations = 0;
        do {
            overflow = false;
            for (int i = 1; i <= n; i++) {
                shares[i - 1] = evaluate(i);
                if (shares[i - 1] == PRIME - 1) {
                    overflow = true;
                    if (!adjustCoefficients() || ++iterations > MAX_ADJUSTMENT_ITERATIONS) {
                        throw new IllegalStateException("Could not resolve coefficient overflow (256) without infinite loop.");
                    }
                    break;
                }
            }
        } while (overflow);
        return shares;
    }

    private int evaluate(int x) {
        long result = 0;
        long power = 1;
        for (int coeff : coefficients) {
            result = (result + coeff * power) % PRIME;
            power = power * x % PRIME;
        }
        return (int) result;
    }

    private boolean adjustCoefficients() {
        for (int i = 0; i < coefficients.length; i++) {
            if (coefficients[i] != 0) {
                coefficients[i]--;
                return true;
            }
        }
        return false;
    }

    /**
     * Recovers all k polynomial coefficients from k (x, y) pairs using
     * Gauss-Jordan elimination on the Vandermonde system, mod PRIME (257).
     *
     * @param xs  x values (shadow numbers), length k
     * @param ys  y values (share values), length k
     * @return    k coefficients [a0, a1, ..., a_{k-1}]
     */
    public static int[] recoverCoefficients(int[] xs, int[] ys) {
        int k = xs.length;
        long[][] mat = new long[k][k + 1];

        // Build augmented Vandermonde matrix
        for (int i = 0; i < k; i++) {
            long power = 1;
            for (int j = 0; j < k; j++) {
                mat[i][j] = power;
                power = power * xs[i] % PRIME;
            }
            mat[i][k] = ys[i] % PRIME;
        }

        // Gauss-Jordan elimination mod PRIME
        for (int col = 0; col < k; col++) {
            int pivotRow = -1;
            for (int row = col; row < k; row++) {
                if (mat[row][col] != 0) { pivotRow = row; break; }
            }
            if (pivotRow == -1) {
                throw new ArithmeticException("Singular system during recovery at column " + col);
            }
            long[] tmp = mat[col]; mat[col] = mat[pivotRow]; mat[pivotRow] = tmp;

            long inv = modPow(mat[col][col], PRIME - 2, PRIME);
            for (int j = col; j <= k; j++) {
                mat[col][j] = mat[col][j] * inv % PRIME;
            }

            for (int row = 0; row < k; row++) {
                if (row != col && mat[row][col] != 0) {
                    long factor = mat[row][col];
                    for (int j = col; j <= k; j++) {
                        mat[row][j] = ((mat[row][j] - factor * mat[col][j]) % PRIME + PRIME) % PRIME;
                    }
                }
            }
        }

        int[] result = new int[k];
        for (int i = 0; i < k; i++) {
            result[i] = (int) mat[i][k];
        }
        return result;
    }

    private static long modPow(long base, long exp, long mod) {
        long result = 1;
        base %= mod;
        while (exp > 0) {
            if ((exp & 1) == 1) result = result * base % mod;
            base = base * base % mod;
            exp >>= 1;
        }
        return result;
    }
}
