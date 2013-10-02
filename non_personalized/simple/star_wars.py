import pandas

X = pandas.read_csv("recsys-data-WA 1 Rating Matrix.csv")
star_wars = X["260: Star Wars: Episode IV - A New Hope (1977)"]

numerator = X.apply(lambda x: x.notnull() & star_wars.notnull())
denominator = pandas.notnull(star_wars)

print (numerator.sum().astype("float") / denominator.sum().astype("float")).order(ascending=False)[0:10]
