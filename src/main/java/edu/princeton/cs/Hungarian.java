package edu.princeton.cs;

import java.util.Random;

/******************************************************************************
 *  Compilation:  javac Hungarian.java
 *  Execution:    java Hungarian N
 *  Dependencies: FordFulkerson.java FlowNetwork.java FlowEdge.java 
 *
 *  Solve an N-by-N assignment problem. Bare-bones implementation:
 *     - takes N^5 time in worst case.
 *     - assumes weights are >= 0  (add a large constant if not)
 *  
 *  For N^4 version: http://pages.cs.wisc.edu/~m/cs787/hungarian.txt
 *  Can be improved to N^3
 *
 ******************************************************************************/

public class Hungarian {
    private int N;              // number of rows and columns
    private double[][] weight;  // the N-by-N weight matrix
    private double[] x;         // dual variables for rows
    private double[] y;         // dual variables for columns
    private int[] xy;           // xy[i] = j means i-j is a match
    private int[] yx;           // yx[j] = i means i-j is a match
 
    public Hungarian(double[][] weight) {
        this.weight = weight.clone();
        N = weight.length;
        x = new double[N];
        y = new double[N];
        xy = new int[N];
        yx = new int[N];
        for (int i = 0; i < N; i++)
            xy[i] = -1;
        for (int j = 0; j < N; j++)
            yx[j] = -1;

        while (true) {

            // build graph of 0-reduced cost edges
            FlowNetwork G = new FlowNetwork(2*N+2);
            int s = 2*N, t = 2*N+1;
            for (int i = 0; i < N; i++) {
                if (xy[i] == -1) G.addEdge(new FlowEdge(s, i, 1.0));
                else             G.addEdge(new FlowEdge(s, i, 1.0, 1.0));
            }
            for (int j = 0; j < N; j++) {
                if (yx[j] == -1) G.addEdge(new FlowEdge(N+j, t, 1.0));
                else             G.addEdge(new FlowEdge(N+j, t, 1.0, 1.0));
            }
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (reduced(i, j) == 0) {
                        if (xy[i] != j) G.addEdge(new FlowEdge(i, N+j, 1.0));
                        else            G.addEdge(new FlowEdge(i, N+j, 1.0, 1.0));
                    }
                }
            }

            // to make N^4, start from previous solution
            FordFulkerson ff = new FordFulkerson(G, s, t);

            // current matching
            for (int i = 0; i < N; i++)
                xy[i] = -1;
            for (int j = 0; j < N; j++)
                yx[j] = -1;
            for (int i = 0; i < N; i++) {
                for (FlowEdge e : G.adj(i)) {
                    if ((e.from() == i) && (e.flow() > 0)) {
                        xy[i] = e.to() - N;
                        yx[e.to() - N] = i;
                    }
                }
            }

            // perfect matching
            if (ff.value() == N) break;

            // find bottleneck weight
            double max = Double.POSITIVE_INFINITY;
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    if (ff.inCut(i) && !ff.inCut(N+j) && (reduced(i, j) < max))
                        max = reduced(i, j);

            // update dual variables
            for (int i = 0; i < N; i++)
                if (!ff.inCut(i))   x[i] -= max;
            for (int j = 0; j < N; j++)
                if (!ff.inCut(N+j)) y[j] += max;

            System.out.println("value = " + ff.value());
        }
        assert check();
    }

    // reduced cost of i-j
    private double reduced(int i, int j) {
        return weight[i][j] - x[i] - y[j];
    }

    private double weight() {
        double totalWeight = 0.0;
        for (int i = 0; i < N; i++)
            totalWeight += weight[i][xy[i]];
        return totalWeight;
    }

    private int sol(int i) {
        return xy[i];
    }


    // check optimality conditions
    private boolean check() {
        // check that xy[] is a permutation
        boolean[] perm = new boolean[N];
        for (int i = 0; i < N; i++) {
            if (perm[xy[i]]) {
                System.out.println("Not a perfect matching");
                return false;
            }
            perm[xy[i]] = true;
        }

        // check that all edges in xy[] have 0-reduced cost
        for (int i = 0; i < N; i++) {
            if (reduced(i, xy[i]) != 0) {
                System.out.println("Solution does not have 0 reduced cost");
                return false;
            }
        }

        // check that all edges have >= 0 reduced cost
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (reduced(i, j) < 0) {
                    System.out.println("Some edges have negative reduced cost");
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {

        int N = Integer.parseInt(args[0]);
        double[][] weight = new double[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                weight[i][j] = new Random().nextDouble();

        Hungarian assignment = new Hungarian(weight);
        System.out.println("weight = " + assignment.weight());
        for (int i = 0; i < N; i++)
            System.out.println(i + "-" + assignment.sol(i));
    }

}

/******************************************************************************
 *  Copyright 2002-2015, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
