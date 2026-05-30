package ar.edu.itba.cripto.steganography;

public class WuLoPolynomial {

    private static final int PRIME = 257;

    private final int[] coefficients;

    public WuLoPolynomial(int[] coefficients) {
        this.coefficients = coefficients.clone();
    }

    public int[] computeShares(int n) {
        int[] shares = new int[n];
        boolean overflow;
        do {
            overflow = false;
            for (int i = 1; i <= n; i++) {
                shares[i - 1] = evaluate(i);
                if (shares[i - 1] == PRIME - 1) {
                    overflow = true;
                    adjustCoefficients();
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
            result += coeff * power;
            power *= x;
        }
        return (int) (result % PRIME);
    }

    private void adjustCoefficients() {
        for (int i = 0; i < coefficients.length; i++) {
            if (coefficients[i] != 0) {
                coefficients[i]--;
                return;
            }
        }
    }
}
