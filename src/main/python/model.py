import argparse
import pandas as pd
from sklearn.linear_model import LogisticRegression


def main(x_file, y_file, output_file):
    data = pd.read_csv(x_file)
    target = pd.read_csv(y_file)
    lr = LogisticRegression()
    lr.fit(data, target)
    with open(output_file, "w") as out:
        coef = lr.coef_[0]
        for i in range(len(coef)):
            out.write(str(coef[i])+"\n")


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("X", help="training data")
    parser.add_argument("y", help="training answers")
    parser.add_argument("output_file", help="file to save model")
    args = parser.parse_args()
    main(args.X, args.y, args.output_file)
