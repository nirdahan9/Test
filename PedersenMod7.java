import java.util.Random;

/**
 * PedersenMod7 — Simple "Pedersen" commitment in the group G={1..6}, multiplication mod 7, g=5.
 *
 * Roles and flow:
 *  • Bob: chooses k ∈ {0..5} (secretly), computes h = g^k (mod 7), and publishes only h.
 *  • Alice: to commit to a bit m∈{0,1}, chooses r ∈ {0..5}, computes c = g^r · h^m (mod 7) and sends c (this is the commitment).
 *  • Reveal/Verify: Alice reveals (m,r); Bob checks that g^r * h^m ≡ c (mod 7).
 *
 */
public final class PedersenMod7 {

    // Fixed group parameters
    // G = {1,2,3,4,5,6} under multiplication mod 7
    public static final int P = 7;     // modulu
    public static final int G = 5;     // generator
    public static final int ORDER = 6; // |G| = 6 


    /* ------------------------ Bob's actions ------------------------ */

    /** Samples k uniformly from {0..5}. */
    public static int sampleK(Random rng) {
        return Math.floorMod(rng.nextInt(), ORDER); 
    }

    /** Computes h = g^k (mod 7). This is the value Bob publishes to Alice. */
    public static int hFromK(int k) {
        int kk = Math.floorMod(k, ORDER); // reduce mod 6
        return powMod(G, kk, P);          // h ∈ {1..6}
    }

    /* ------------------------ Alice's actions ----------------------- */

    /** Samples r uniformly from {0..5}. */
    public static int sampleR(Random rng) {
        return Math.floorMod(rng.nextInt(), ORDER); // 0..5
    }

    /**
     * Creates a commitment: c = g^r · h^m (mod 7)
     * @param m  the message bit (0 or 1)
     * @param r  the randomness (0..5)
     * @param h  the public value published by Bob: h = g^k (mod 7)
     * @return   c ∈ {1..6} — this is sent in the commit phase
     */
    public static int commit(int m, int r, int h) {
        validateBit(m);
        validateR(r);
        validateH(h);

        int gr = powMod(G, r % ORDER, P); // g^r
        int hm = (m == 0) ? 1 : h;        // h^m, since m∈{0,1}
        return (gr * hm) % P;             // 1..6
    }

    /**
     * Reveal verification: checks that c == g^r · h^m (mod 7).
     * @param c  the commitment (1..6)
     * @param m  0 or 1
     * @param r  0..5
     * @param h  public h
     * @return   true if verification passes; false otherwise
     */
    public static boolean verify(int c, int m, int r, int h) {
        if (c <= 0 || c >= P) return false; // must be an element of G
        if (!isBit(m)) return false;
        if (!isValidR(r)) return false;
        if (!isValidH(h)) return false;

        return c == commit(m, r, h);
    }
    
    /** Modular exponentiation (small and fast for p=7). */
    private static int powMod(int base, int exp, int mod) {
        int res = 1 % mod;
        int b = Math.floorMod(base, mod);
        int e = exp;
        while (e > 0) {
            if ((e & 1) == 1) res = (res * b) % mod;
            b = (b * b) % mod;
            e >>= 1;
        }
        return res;
    }

    /* ------------------------ Validation helpers ------------------------ */

    private static void validateBit(int m) {
        if (!isBit(m)) throw new IllegalArgumentException("m must be 0 or 1");
    }
    private static boolean isBit(int m) { return m == 0 || m == 1; }

    private static void validateR(int r) {
        if (!isValidR(r)) throw new IllegalArgumentException("r must be in 0..5 (mod 6)");
    }
    private static boolean isValidR(int r) { return Math.floorMod(r, ORDER) == r; }

    private static void validateH(int h) {
        if (!isValidH(h)) throw new IllegalArgumentException("h must be in {1..6}");
    }
    private static boolean isValidH(int h) { return h > 0 && h < P; }
}
