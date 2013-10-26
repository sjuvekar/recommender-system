#!/usr/bin/env python

import numpy
import pandas
import sys

"""
Top 5 correlations, exclude same column which has a correlation of 1.0
"""
def top_five_neighbors(col, matrix):
    temp_col = col.copy()
    temp_col.sort(ascending=False)
    return temp_col[1:6].index


def neighbor_weights_and_ratings(col_id, neighbors, correlation_matrix, rating_matrix):
    neighbor_ratings = rating_matrix[neighbors]
    # Check if ratings null. Mask all weights to 0 if the rating is null
    neighbor_ratings_notnull = pandas.notnull(neighbor_ratings)
    neighbor_weights = correlation_matrix[col_id][neighbors] * neighbor_ratings_notnull
    return (neighbor_ratings, neighbor_weights)

def non_normalized_ratings(neighbor_ratings, neighbor_weights):
    numerator = (neighbor_ratings * neighbor_weights).sum(axis = 1)
    denominator = neighbor_weights.sum(axis=1)
    ratings = numerator / denominator
    ratings.sort(ascending=False)
    return ratings

def normalized_ratings(neighbor_ratings, neighbor_weights, col_id, rating_matrix):
    adjusted_neighbor_ratings = neighbor_ratings - neighbor_ratings.mean()
    numerator = (adjusted_neighbor_ratings * neighbor_weights).sum(axis = 1)
    denominator = neighbor_weights.sum(axis=1)
    adjusted_ratings = numerator / denominator
    user_average = rating_matrix[col_id].mean()
    ratings = user_average + adjusted_ratings
    ratings.sort(ascending=False)
    return ratings
    

if __name__ == "__main__":
    rating_matrix = pandas.read_csv("recsys-data-sample-rating-matrix.csv", index_col=0)
    correlation_matrix = pandas.read_csv("correlation_matrix.csv", index_col=0)

    # Change columns to numeric
    rating_matrix.columns = rating_matrix.columns.map(lambda x: int(x))
    correlation_matrix.columns = correlation_matrix.columns.map(lambda x: int(x))

    # Read column numbers
    col = int(sys.argv[1])
    
    neighbors = top_five_neighbors(correlation_matrix[col], correlation_matrix)
    (neighbor_ratings, neighbor_weights) = neighbor_weights_and_ratings(col, neighbors, correlation_matrix, rating_matrix)
    #print non_normalized_ratings(neighbor_ratings, neighbor_weights)
    print normalized_ratings(neighbor_ratings, neighbor_weights, col, rating_matrix)
