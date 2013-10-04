import pandas
import operator
import sys

def rating_matrix(in_matrix):
	in_matrix.columns = ["user", "movie", "rating"]
	unique_movies = in_matrix["movie"].unique()
	unique_user = in_matrix["user"].unique()

	rating_mat = pandas.DataFrame(columns = unique_movies, index = unique_user)

	for i in range(len(in_matrix)):
		rating_mat[in_matrix["movie"][i]][in_matrix["user"][i]] = in_matrix["rating"][i]

	return rating_mat

'''
Return (x and y)/(x) for every y and given x
'''
def prob_ratio(x, rating_mat, negative):
	x_col = rating_mat[x]
	if negative:
		x_or_not = x_col.isnull()
	else:
		x_or_not = x_col.notnull()
	denominator = x_or_not.sum()
		
	metric_dict = dict()
	for y in rating_mat.columns:
		if y == x:
			continue
		y_col = rating_mat[y]
		numerator = (x_or_not & y_col.notnull()).sum()
		metric_dict[y] = float(numerator) / float(denominator)
	return metric_dict


'''
Advanced prob ratio = ration of (x and y)/(x) and (~x and y)/(~x)
'''
def advanced_prob_ratio(x, rating_mat):
	pos_ratio = prob_ratio(x, rating_mat, False)
	neg_ratio = prob_ratio(x, rating_mat, True)
	metric_dict = dict()
	for k in pos_ratio.keys():
		metric_dict[k] = pos_ratio[k] / neg_ratio[k]
	return metric_dict


def sort_prob_ratios(x, p):
	sorted_p = sorted(p.iteritems(), key=operator.itemgetter(1), reverse=True)[0:5]
	ret_str = "{0}".format(x)
	for pp in sorted_p:
		ret_str += ",%d,%.2f" % (pp[0], pp[1])
	return ret_str

''' 
Simple metric top five results
'''
def simple_metric(x, rating_mat):
	print sort_prob_ratios(x, prob_ratio(x, rating_mat, False))
		
''' 
Advanced metric top five results
'''
def advanced_metric(x, rating_mat):	
	print sort_prob_ratios(x, advanced_prob_ratio(x, rating_mat))

if __name__ == "__main__":
	in_mat = pandas.read_csv("recsys-data-ratings.csv")
	rat_mat = rating_matrix(in_mat)
	movie_id1 = int(sys.argv[1])
	movie_id2 = int(sys.argv[2])
	movie_id3 = int(sys.argv[3])

	if sys.argv[4] == "simple":
		simple_metric(movie_id1, rat_mat) 
		simple_metric(movie_id2, rat_mat) 
		simple_metric(movie_id3, rat_mat) 
	else:
		advanced_metric(movie_id1, rat_mat) 
		advanced_metric(movie_id2, rat_mat) 
		advanced_metric(movie_id3, rat_mat) 

