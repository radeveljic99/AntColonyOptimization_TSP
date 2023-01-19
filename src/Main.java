import java.util.Arrays;
import java.util.Random;

public class Main {

    static Random random = new Random();
    static int alfa = 3;
    static int beta = 2;
    static double ro = 0.01;
    static double Q = 2.0;


    public static void stampajGraf(int[][] graf) {
        for (int[] ints : graf) {
            for (int anInt : ints) {
                System.out.print(anInt + "\t");
            }
            System.out.println();
        }
    }

    public static void stampajFeromone(double[][] feromoni) {
        for (double[] doubles : feromoni) {
            for (double aDouble : doubles) {
                System.out.print(String.format("%.2f", aDouble) + "\t");
            }
            System.out.println();
        }
    }


    public static int[][] napraviGraf(int brojGradova) {
        int graf[][] = new int[brojGradova][];
        for (int i = 0; i < brojGradova; ++i) {
            graf[i] = new int[brojGradova];
        }
        for (int i = 0; i < brojGradova; ++i) {
            for (int j = i + 1; j < brojGradova; ++j) {
                int d = random.nextInt(1, 9);
                graf[i][j] = d;
                graf[j][i] = d;
            }
        }

        return graf;
    }

    public static int distanca(int gradX, int gradY, int graf[][]) {
        return graf[gradX][gradY];
    }

    public static int[][] inicijalizacijaMrava(int brojMrava, int brojGradova) {
        int[][] mravi = new int[brojMrava][];
        for (int k = 0; k < brojMrava; k++) {
            int pocetniCvor = random.nextInt(0, brojGradova);
            mravi[k] = nasumaicanPut(pocetniCvor, brojGradova);
        }

        return mravi;
    }

    public static int[] nasumaicanPut(int pocetniCvor, int brojGradova) {
        int put[] = new int[brojGradova];

        for (int i = 0; i < brojGradova; i++) {
            put[i] = i;
        }

        // Fisher Yates shuffle alghortim

        for (int i = 0; i < brojGradova - 1; i++) {
            int r = random.nextInt(i, brojGradova);
            int tmp = put[r];
            put[r] = put[i];
            put[i] = tmp;
        }

        int indeks = indeksElementa(put, pocetniCvor);

        int temp = put[0];
        put[0] = put[indeks];
        put[indeks] = temp;

        return put;
    }

    public static int indeksElementa(int[] put, int element) {
        for (int i = 0; i < put.length; i++) {
            if (put[i] == element) {
                return i;
            }
        }
        return -1;
    }

    public static double[][] inicijalizujFeromone(int brojGradova) {
        double[][] feromoni = new double[brojGradova][];
        for (int i = 0; i < brojGradova; i++) {
            feromoni[i] = new double[brojGradova];
        }

        for (double[] doubles : feromoni) {
            Arrays.fill(doubles, 0.01);
        }

        return feromoni;
    }

    public static void azurirajMrave(int[][] mravi, double[][] feromoni, int[][] graf) {
        int brojGradova = feromoni.length;
        for (int k = 0; k < mravi.length; k++) {
            int start = random.nextInt(0, brojGradova);
            int[] noviPut = napraviPut(k, start, feromoni, graf);
            mravi[k] = noviPut;
        }
    }

    public static int[] napraviPut(int k, int start, double[][] feromoni, int[][] graf) {
        int brojGradova = feromoni.length;
        int[] put = new int[brojGradova];
        boolean[] posjeceni = new boolean[brojGradova];
        put[0] = start;
        posjeceni[start] = true;
        for (int i = 0; i < brojGradova - 1; i++) {
            int gradX = put[i];
            int next = sledeciGrad(k, gradX, posjeceni, feromoni, graf);
            put[i + 1] = next;
            posjeceni[next] = true;
        }
        return put;
    }


    public static int sledeciGrad(int k, int gradX, boolean[] posjeceni, double[][] feromoni, int[][] graf) {
        double probs[] = azurirajVjerovatnoce(k, gradX, posjeceni, feromoni, graf);
        double[] cumul = new double[probs.length + 1];
        for (int i = 0; i < probs.length; i++) {
            cumul[i + 1] = cumul[i] + probs[i];
        }

        double p = random.nextDouble();
        for (int i = 0; i < cumul.length - 1; i++) {
            if (p >= cumul[i] && p <= cumul[i + 1]) {
                return i;
            }
        }

        return -1;

    }

    public static double[] azurirajVjerovatnoce(int k, int gradX, boolean[] posjeceni, double[][] feromoni, int[][] graf) {
        int brojGradova = feromoni.length;
        double[] taueta = new double[brojGradova];
        double suma = 0.0;
        for (int i = 0; i < taueta.length; i++) {
            if (i == gradX) {
                taueta[i] = 0.0;
            } else if (posjeceni[i]) {
                taueta[i] = 0.0;
            } else {
                taueta[i] = Math.pow(feromoni[gradX][i], alfa) * Math.pow((1.0 / distanca(gradX, i, graf)), beta);
                if (taueta[i] < 0.0001) {
                    taueta[i] = 0.0001;
                } else if (taueta[i] > (Double.MAX_VALUE / (brojGradova * 100))) {
                    taueta[i] = Double.MAX_VALUE / (brojGradova * 100);
                }
            }
            suma += taueta[i];
        }

        double probs[] = new double[brojGradova];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = taueta[i] / suma;
        }

        return probs;
    }

    public static void azurirajFeromone(double[][] feromoni, int[][] mravi, int[][] graf) {
        for (int i = 0; i < feromoni.length; i++) {
            for (int j = i + 1; j < feromoni[i].length; j++) {
                for (int[] trenPutMrava : mravi) {
                    double duzina = duzinaPuta(trenPutMrava, graf);
                    double smanjenje = (1.0 - ro) * feromoni[i][j];
                    double povecanje = 0.0;
                    if (granaNaPutu(i, j, trenPutMrava)) {
                        povecanje = (Q / duzina);
                    }

                    feromoni[i][j] = smanjenje + povecanje;
                    if (feromoni[i][j] < 0.0001) {
                        feromoni[i][j] = 0.0001;
                    } else if (feromoni[i][j] > 100000.0) {
                        feromoni[i][j] = 100000.0;
                    }
                    feromoni[j][i] = feromoni[i][j];
                }
            }
        }
    }

    private static boolean granaNaPutu(int gradX, int gradY, int[] put) {
        int poslednjiIndeks = put.length - 1;
        int indeksPocetnogGrada = indeksElementa(put, gradX);

        if (indeksPocetnogGrada == 0 && put[1] == gradY) {
            return true;
        } else if (indeksPocetnogGrada == 0 && put[poslednjiIndeks] == gradY) {
            return true;
        } else if (indeksPocetnogGrada == 0) {
            return false;
        } else if (indeksPocetnogGrada == poslednjiIndeks && put[poslednjiIndeks - 1] == gradY) {
            return true;
        } else if (indeksPocetnogGrada == poslednjiIndeks && put[0] == gradY) {
            return true;
        } else if (indeksPocetnogGrada == poslednjiIndeks) {
            return false;
        } else if (put[indeksPocetnogGrada - 1] == gradY) {
            return true;
        } else if (put[indeksPocetnogGrada + 1] == gradY) {
            return true;
        } else {
            return false;
        }
    }

    public static int[] najboljiPut(int[][] mravi, int graf[][]) {
        double najboljiPut = duzinaPuta(mravi[0], graf);
        int indeksNajboljegPuta = 0;
        for (int k = 1; k < mravi.length; k++) {
            double duzinaPuta = duzinaPuta(mravi[k], graf);
            if (duzinaPuta < najboljiPut) {
                najboljiPut = duzinaPuta;
                indeksNajboljegPuta = k;
            }
        }
        int brojGradova = mravi[0].length;
        int[] resNajboljiPut = new int[brojGradova];
        resNajboljiPut = Arrays.copyOf(mravi[indeksNajboljegPuta], mravi[indeksNajboljegPuta].length);
        return resNajboljiPut;
    }

    private static double duzinaPuta(int[] put, int[][] graf) {
        double duzina = 0.0;
        for (int i = 0; i < put.length - 1; i++) {
            duzina += distanca(put[i], put[i + 1], graf);
        }
        return duzina;
    }

    public static void stampajNiz(int[] arr) {
        for (int j : arr) {
            System.out.print(j + " ");
        }
        System.out.println();
    }


    public static void main(String[] args) {
        int brojGradova = 5;
        int brojMrava = 4;
        int maxVrijeme = 1000;

//        int graf[][] = napraviGraf(brojGradova);
        int[][] graf = {
                {0, 4, 1, 6, 2},
                {4, 0, 5, 5, 6},
                {1, 5, 0, 3, 3},
                {6, 5, 3, 0, 8},
                {2, 6, 3, 8, 0}
        };
        System.out.println("Graf : ");
        stampajGraf(graf);
        int mravi[][] = inicijalizacijaMrava(brojMrava, brojGradova);

        int[] najboljiPut = najboljiPut(mravi, graf);
        stampajNiz(najboljiPut);
        System.out.println();
        double duzinaNajkracegPuta = duzinaPuta(najboljiPut, graf);

        double[][] feromoni = inicijalizujFeromone(brojGradova);

        int vrijeme = 0;
        while (vrijeme < maxVrijeme) {
            azurirajMrave(mravi, feromoni, graf);
            azurirajFeromone(feromoni, mravi, graf);
            int[] trenNajboljiPut = najboljiPut(mravi, graf);
            double duzinaTrenNajboljegPuta = duzinaPuta(trenNajboljiPut, graf);
            if (duzinaTrenNajboljegPuta < duzinaNajkracegPuta) {
                duzinaNajkracegPuta = duzinaTrenNajboljegPuta;
                najboljiPut = trenNajboljiPut;
            }
            vrijeme++;
        }
        System.out.println("Duzina najkraceg puta" +
                " = " + duzinaNajkracegPuta);
        System.out.println("Feromoni");
        stampajFeromone(feromoni);

    }

}
