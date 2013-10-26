#!/usr/bin/env python

import math
import pandas

def correlation_of(col1, col2):
    common = col1.notnull() & col2.notnull()
    array1 = col1[common] - col1[common].mean()
    array2 = col2[common] - col2[common].mean()
    std1 = math.sqrt((array1 * array1).sum())
    std2 = math.sqrt((array2 * array2).sum())
    return (array1 * array2).sum() / (std1 * std2)

def create_correlation_matrix(x):
    ret_mat = pandas.DataFrame(columns=x.columns, index=x.columns)
    ind = x.columns
    for i in range(len(ind)):
        for j in range(i, len(ind)):
            col_ind1 = ind[i]
            col_ind2 = ind[j]
            corr = correlation_of(x[col_ind1], x[col_ind2])
            ret_mat[col_ind1][col_ind2] = corr
            ret_mat[col_ind2][col_ind1] = corr
    return ret_mat

if __name__ == "___main__":
    x = pandas.read_csv("recsys-data-sample-rating-matrix.csv", index_col=0)
    ret_mat = create_correlation_matrix(x)
    ret_mat.to_csv("correlation_matrix.csv", header=True, index=True)
