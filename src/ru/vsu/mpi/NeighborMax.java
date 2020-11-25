package ru.vsu.mpi;

import mpi.MPI;

import java.util.Random;

/**
 * Текучев Олег Алексеевич
 * #6
 * Найти max X(i,j) X(i+1,j) j=0,1,..,m где m - размер локального вектора одного процесса
 *
 * Запуск: mpjrun.sh -np <num of processes> ru.vsu.mpi.NeighborMax <vector size> <value pos (j)>
 */
public class NeighborMax {

    public static final int MAX_MESS_TAG = 0;
    public static final String NUM_REGEX = "^\\d+$";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";


    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int processCount = MPI.COMM_WORLD.Size();
        if (processCount == 1) {
            System.out.println("Only one process is launched");
            return;
        }
        if (!isValidCla(args, rank == 0)) {
            return;
        }
        int[] localVector = randVector(Integer.parseInt(args[3]));
        int valueInd = Integer.parseInt(args[4]);
        System.out.println("Process " + rank + " vector: " + highlightArrayValueConsole(localVector, valueInd));
        if (rank != processCount - 1) {
            int[] buff = new int[] {localVector[valueInd]};
            MPI.COMM_WORLD.Send(buff, 0, buff.length, MPI.INT, rank + 1, MAX_MESS_TAG);
        }
        if (rank != 0) {
            int[] buff = new int[1];
            MPI.COMM_WORLD.Recv(buff, 0, buff.length, MPI.INT, rank - 1, MAX_MESS_TAG);
            System.out.println("Max between " + (rank - 1) + " and " + rank + " processes: " + Math.max(buff[0], localVector[valueInd]));
        }
        MPI.Finalize();
    }

    private static int[] randVector(int size) {
        Random random = new Random();
        int[] vector = new int[size];
        for (int i = 0; i < size; ++i) {
            vector[i] = random.nextInt(100);
        }
        return vector;
    }

    private static boolean isValidCla(String[] args, boolean printError) {
        if (args.length < 5) {
            if (printError) {
                System.out.println("Expected 2 arguments: <vector size> <value position>");
            }
            return false;
        }
        int vecSize;
        if (!args[3].matches(NUM_REGEX) || (vecSize = Integer.parseInt(args[3])) < 1) {
            if (printError) {
                System.out.println("Invalid vector size: " + args[3]);
            }
            return false;
        }
        if (!args[4].matches(NUM_REGEX) || Integer.parseInt(args[4]) - 1 > vecSize) {
            if (printError) {
                System.out.println("Invalid value position: " + args[4]);
            }
            return false;
        }
        return true;
    }

    private static String highlightArrayValueConsole(int[] array, int valPos) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < array.length; ++i) {
            if (i == valPos) {
                stringBuilder.append(ANSI_YELLOW)
                        .append(array[i])
                        .append(ANSI_RESET);
            } else {
                stringBuilder.append(array[i]);
            }
            if (i != array.length - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.append("]").toString();
    }
}
