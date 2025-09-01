import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class polynomialSolver {

    public static void main(String[] args) {
        String filePath = "jsonFiles\\a.json";

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));

            JSONObject keys = (JSONObject) jsonObject.get("keys");
            long n = (long) keys.get("n");
            long k = (long) keys.get("k");

            if (k < 2) {
                System.out.println("Polynomial degree must be at least 1.");
                return;
            }

            List<double[]> points = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                String key = String.valueOf(i);
                if (jsonObject.containsKey(key)) {
                    JSONObject pointData = (JSONObject) jsonObject.get(key);
                    double x = Double.parseDouble((String) pointData.get("base"));
                    double y = Double.parseDouble((String) pointData.get("value"));
                    points.add(new double[]{x, y});
                }
            }
            
            if (points.size() < k) {
                System.out.println("Not enough points provided to solve for the polynomial.");
                return;
            }

            double[][] matrix = new double[(int) k][(int) k];
            double[] vector = new double[(int) k];

            // Use the first k unique points to form the system of equations
            for (int i = 0; i < k; i++) {
                double x = points.get(i)[0];
                double y = points.get(i)[1];
                vector[i] = y;
                for (int j = 0; j < k; j++) {
                    matrix[i][j] = Math.pow(x, k - 1 - j);
                }
            }
            
            double[] coefficients = solveLinearSystem(matrix, vector);
            
            // The constant c is the last coefficient
            if (coefficients != null) {
                double c = coefficients[coefficients.length - 1];
                System.out.println("The value of the constant c is: " + c);
            } else {
                System.out.println("Could not solve the system of equations.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double[] solveLinearSystem(double[][] A, double[] b) {
        int n = b.length;
        double[][] augmented = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, augmented[i], 0, n);
            augmented[i][n] = b[i];
        }

        // Gaussian Elimination with partial pivoting
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(augmented[j][i]) > Math.abs(augmented[maxRow][i])) {
                    maxRow = j;
                }
            }
            
            double[] temp = augmented[i];
            augmented[i] = augmented[maxRow];
            augmented[maxRow] = temp;

            if (Math.abs(augmented[i][i]) < 1e-10) {
                return null; // System has no unique solution
            }

            for (int j = i + 1; j < n; j++) {
                double factor = augmented[j][i] / augmented[i][i];
                for (int k = i; k < n + 1; k++) {
                    augmented[j][k] -= factor * augmented[i][k];
                }
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += augmented[i][j] * x[j];
            }
            x[i] = (augmented[i][n] - sum) / augmented[i][i];
        }
        return x;
    }
}
