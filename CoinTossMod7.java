import java.util.Random;
import java.util.Scanner;

/**
 * CoinTossMod7 — demo of Blum-style coin tossing
 * using a Pedersen commitment over G={1..6}, multiplication mod 7, g=5.
 *
 * Flow:
 *  1) Bob picks secret k∈{0..5}, computes h=g^k (mod 7), and publishes h.
 *  2) Alice picks her bit m∈{0,1} and samples r∈{0..5}.
 *  3) Alice computes commitment c = g^r * h^m (mod 7) and "sends" c to Bob.
 *  4) Bob picks his bit b∈{0,1}.
 *  5) Alice reveals (m,r).
 *  6) Bob verifies: c ?= g^r * h^m (mod 7).
 *  7) If verification passes → shared coin: coin = m XOR b; else → Abort.
 *  8) Winner: If coin = 0 then Bob wins; if coin = 1 then Alice wins.
 *
 */
public final class CoinTossMod7 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rng = new Random();

        System.out.println("===  Coin Toss (Blum's protocol , Pedersen mod 7)   ===");        
        System.out.println("=== By: Nir Dahan, Orine Bason, Hila Arnon & Noa Oz ===");
        System.out.println();
        System.out.println("Params: p=" + PedersenMod7.P  + ", G = {1,2,3,4,5,6} (|G|=" + PedersenMod7.ORDER +"), g=" + PedersenMod7.G);
        System.out.println("Winner rule: If coin = 0 then Bob wins; if coin = 1 then Alice wins.");
        System.out.println();

        int runs = readIntOrDefault(sc);

        System.out.println();

        for (int i = 1; i <= runs; i++) {
            System.out.println("---- Run #" + i + " ----");
            runOne(rng);
            System.out.println();
        }
    }

    /**
     * Reads a positive integer from Scanner with a clear prompt.
     * If the user presses Enter on an empty line, returns the provided default.
     * On invalid input, explains what is expected and re-prompts.
     */
    private static int readIntOrDefault(Scanner sc) {
        while (true) {
            System.out.print("Enter number of runs (positive integer). Press Enter to use default [1]: ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("Using default: " + 1);
                return 1;
            }
            try {
                int v = Integer.parseInt(line);
                if (v >= 1) return v;
                System.out.println("Please enter a positive integer (>= 1), or press Enter to use the default (1).");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a positive integer, or press Enter for the default (1).");
            }
        }
    }

    private static void runOne(Random rng) {
        // (1) Bob picks k and computes h=g^k (mod 7)
        int k = PedersenMod7.sampleK(rng);
        int h = PedersenMod7.hFromK(k);
        System.out.println("[Bob] chose secret k=" + k + "  ->  published h=g^k (mod 7) = " + h);

        // (2) Alice picks her bit m∈{0,1}
        int m = rng.nextBoolean() ? 1 : 0;
        System.out.println("[Alice] chose m=" + m);

        // (3) Alice samples r∈{0..5} and computes c=g^r*h^m (mod 7)
        int r = PedersenMod7.sampleR(rng);
        int c = PedersenMod7.commit(m, r, h);
        System.out.println("[Alice] computed commitment c = g^r * h^m (mod 7) = " + c + " (r=" + r + " hidden)");

        // (4) Alice -> Bob: send c
        System.out.println("[Alice->Bob] SEND COMMIT: c=" + c);

        // (5) Bob picks his bit b∈{0,1}
        int b = rng.nextBoolean() ? 1 : 0;
        System.out.println("[Bob] chose b=" + b + " and sends it to Alice");

        // (6) Alice -> Bob: reveal (m,r)
        System.out.println("[Alice->Bob] REVEAL: m=" + m + ", r=" + r);

        // (7) Bob verifies
        boolean ok = PedersenMod7.verify(c, m, r, h);
        System.out.println("[Bob] verify(c = " + c + ", m = " + m + ", r = " + r + ", h = " + h + ") = " + ok);

        if (!ok) {
            System.out.println("[Result] ABORT (verification failed)");
            return;
        }

        // (8) Shared result
        int coin = (m ^ b) & 1;
        System.out.println("[Result] coin = m XOR b = " + m + " XOR " + b + " = " + coin);
        String winner = (coin == 0) ? "Bob" : "Alice";
        System.out.println("[Winner] " + winner + " wins!");
    }
}
