

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import joinery.*;

public class TitanicSolution {


    private static final String MODEL_SCRIPT_FILE = ".\\src\\main\\python\\model.py";
    private static final String PATH_TO_DATA = ".\\src\\main\\resources\\";
    private static final String GIVEN_TRAIN_FILE = "train.csv";
    private static final String GIVEN_TEST_FILE = "test.csv";
    private static final String SUBMISSION_SAMPLE_FILE = "gender_submission.csv";
    private static final String TRAIN_DATA_FILE = "x.csv";
    private static final String TRAIN_TARGET_FILE = "y.csv";
    private static final String MODEL_OUTPUT_FILE = "model.txt";


    private static DataFrame<Object> drop(DataFrame<Object> data, String[] columnsToDrop) {
        for (String column : columnsToDrop) {
            data = data.drop(column);
        }
        return data;
    }

    private static List<Object> encodeColumn(List<Object> column) {
        Hashtable<Object, Double> encoding = new Hashtable<>();
        ArrayList<Object> encodedColumn = new ArrayList<>();
        int k = 0;
        for (Object value : column) {
            if (!encoding.containsKey(value)) {
                encoding.put(value, (double) k);
                k++;
            }
            encodedColumn.add(encoding.get(value));
        }
        return encodedColumn;
    }

    private static DataFrame<Object> preprocess(DataFrame<Object> data) {
        String[] columnsToDrop = {"Name", "Ticket", "Cabin"};
        data = drop(data, columnsToDrop);
        data = data.fillna(0.0);
        data = data.add("encodedSex", encodeColumn(data.col("Sex")));
        data = data.add("encodedEmbarked", encodeColumn(data.col("Embarked")));
        columnsToDrop = new String[]{"Sex", "Embarked"};
        for (String column : columnsToDrop) {
            data = data.drop(column);
        }
        return data;
    }

    private static void generateModel() {
        try {
            Runtime.getRuntime().exec("python " + MODEL_SCRIPT_FILE + " " + PATH_TO_DATA + TRAIN_DATA_FILE +
                    " " + PATH_TO_DATA + TRAIN_TARGET_FILE + " " + PATH_TO_DATA + MODEL_OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Double> readCoef(String filename) throws FileNotFoundException {
        ArrayList<Double> coefs = new ArrayList<>();
        Scanner myReader = new Scanner(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(MODEL_OUTPUT_FILE)));
        myReader.useLocale(Locale.US);
        while (myReader.hasNext()) {
            coefs.add(myReader.nextDouble());
        }
        myReader.close();
        return coefs;
    }

    private static List<Object> predict(DataFrame testData, List<Double> coef) {
        ArrayList<Object> result = new ArrayList<>();
        for (int i = 0; i < testData.length(); i++) {
            double prediction = 0.0;
            List row = testData.row(i);
            for (int j = 0; j < testData.columns().size(); j++) {
                prediction = prediction + (coef.get(j) * (Double) (row.get(j)));
            }
            boolean answer = prediction > 0;
            if (answer) {
                result.add(1);
            } else {
                result.add(0);
            }
        }
        return result;
    }


    public static void main(String[] args) throws IOException {
        DataFrame<Object> train = DataFrame.readCsv(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(GIVEN_TRAIN_FILE)), ",", DataFrame.NumberDefault.DOUBLE_DEFAULT);
        DataFrame<Object> test = DataFrame.readCsv(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(GIVEN_TEST_FILE)), ",", DataFrame.NumberDefault.DOUBLE_DEFAULT);
        DataFrame<Object> sampleSubmission = DataFrame.readCsv(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(SUBMISSION_SAMPLE_FILE)));

        train = preprocess(train);
        DataFrame<Object> trainTarget = new DataFrame<>().add("Survived", train.col("Survived"));
        train = train.drop("Survived");

        test = preprocess(test);
        train.writeCsv(PATH_TO_DATA + TRAIN_DATA_FILE);
        trainTarget.writeCsv(PATH_TO_DATA + TRAIN_TARGET_FILE);
        generateModel();
        List<Double> modelCoefs = readCoef(MODEL_OUTPUT_FILE);
        sampleSubmission = sampleSubmission.drop("Survived");
        sampleSubmission = sampleSubmission.add("Survived", predict(test, modelCoefs));
        sampleSubmission.writeCsv(PATH_TO_DATA + "submission.csv");

    }
}
