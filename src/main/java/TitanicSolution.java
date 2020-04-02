
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

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


    private static void generateModel() {
        try {
            Runtime.getRuntime().exec("python " + MODEL_SCRIPT_FILE + " " + PATH_TO_DATA + TRAIN_DATA_FILE +
                    " " + PATH_TO_DATA + TRAIN_TARGET_FILE + " " + PATH_TO_DATA + MODEL_OUTPUT_FILE);
            System.out.println("python " + MODEL_SCRIPT_FILE + " " + PATH_TO_DATA + TRAIN_DATA_FILE +
                    " " + PATH_TO_DATA + TRAIN_TARGET_FILE + " " + PATH_TO_DATA + MODEL_OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Object> encodeColumn(List<Object> column){
        Hashtable<Object, Integer> encoding = new Hashtable<>();
        ArrayList<Object> encodedColumn = new ArrayList<>();
        int k = 0;
        for (Object value : column) {
            if (!encoding.containsKey(value)) {
                encoding.put(value, k);
                k++;
            }
            encodedColumn.add(encoding.get(value));
        }
        return encodedColumn;
    }

    private static DataFrame<Object> preprocess(DataFrame<Object> data){
        String[] columnsToDrop ={"Name","Ticket", "Cabin"};
        for (String column:columnsToDrop) {
            data = data.drop(column);
        }
        data = data.dropna();
        data = data.add("encodedSex", encodeColumn(data.col("Sex")));
        data = data.add("encodedEmbarked", encodeColumn(data.col("Embarked")));
        columnsToDrop = new String[]{"Sex", "Embarked"};
        for (String column:columnsToDrop) {
            data = data.drop(column);
        }
        return data;
    }

    public static void main(String[] args) throws IOException {
        DataFrame<Object> train = DataFrame.readCsv(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(GIVEN_TRAIN_FILE)));
        DataFrame<Object> test = DataFrame.readCsv(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(GIVEN_TEST_FILE)));
        DataFrame<Object> sampleSubmission = DataFrame.readCsv(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(SUBMISSION_SAMPLE_FILE)));

        train = preprocess(train);
        DataFrame<Object> trainTarget = new DataFrame<>().add("Survived", train.col("Survived"));
        train = train.drop("Survived");

        test = preprocess(test);
        train.writeCsv(PATH_TO_DATA + TRAIN_DATA_FILE);
        trainTarget.writeCsv(PATH_TO_DATA + TRAIN_TARGET_FILE);
        generateModel();


    }
}
