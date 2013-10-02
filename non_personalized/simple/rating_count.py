import pandas;

X = pandas.read_csv("recsys-data-WA 1 Rating Matrix.csv")
print pandas.notnull(X).sum().order(ascending=False)[0:6]
