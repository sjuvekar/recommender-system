import pandas;

X = pandas.read_csv("recsys-data-WA 1 Rating Matrix.csv")
print ( (X >= 4).sum().astype("float") / pandas.notnull(X).sum().astype("float") ).order(ascending=False)[0:6]
