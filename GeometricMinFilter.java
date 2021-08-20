package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class GeometricMinFilter {
  public static Random rand = new Random();

  public static int n = 0; // total number of packets
  public static GeometricFilter F;
  public static HyperLogLog H;
  public static HyperLogLog H1;

  public static long[] flowida = new long[60000000];
  public static long[] elementida = new long[60000000];
  public static int kkk = 0;
  /** parameters for sharing approach * */
  public static int u = 1;

  public static int w;
  public static int m = 1;
  public static int[] S;
  public static int[] S1;
  public static int[] SH;

  /** parameters for hyperLogLog * */
  public static int mValueHLL = 128;

  public static int HLLSize = 5;

  /** parameters for GeometricRegister * */
  public static int mValueGR = 4;

  public static int GRSize = 5;
  public static int GRBits = 2;

  public static int wh = 0;
  public static int Mbase = 1024;
  public static int M = 1700 * Mbase;
  public static int Mh = 1372 * Mbase;
  public static int tshold = 18;
  public static HashSet<Long> xset = new HashSet<>();

  public static void main(String[] args) throws FileNotFoundException {

    System.out.println("Start************************");
    Scanner sc = new Scanner(new File(GeneralUtil.dataStreamForFlowSpread));
    while (sc.hasNextLine()) {
      String entry = sc.nextLine();
      String[] strs = entry.split("\\s+");
      String[] res = GeneralUtil.getSperadFlowIDAndElementID(strs, true);
      flowida[kkk] = Long.parseLong(res[0]);
      elementida[kkk] = Long.parseLong(res[1]);
      kkk++;
      if (kkk % 1000000 == 0) {
        System.out.println(kkk);
      }
    }
    sc.close();

    initFilter();
    initvHLL();
    xset = new HashSet<>();
    encodeSpread(GeneralUtil.dataStreamForFlowSpread);
    estimateSpread(GeneralUtil.dataSummaryForFlowSpread);

    System.out.println("DONE!!!!!!!!!!!");
  }

  public static void initFilter() {
    m = mValueGR;
    u = GRSize;
    w = M / u;

    F = new GeometricFilter(w, GRSize, GRBits, mValueGR);
    generateSharingRandomSeeds();
    generateSharingRandomSeeds1();
    System.out.println("\nFilter Initialized!");
  }

  public static void initvHLL() {
    wh = Mh / HLLSize;
    H = new HyperLogLog(wh, HLLSize);
    H1 = new HyperLogLog(mValueHLL, HLLSize);
    SH = new int[mValueHLL];
    HashSet<Integer> seeds = new HashSet<Integer>();
    int num = mValueHLL;
    rand = new Random();
    while (num > 0) {
      int s = rand.nextInt();
      if (!seeds.contains(s)) {
        num--;
        SH[num] = s;
        seeds.add(s);
      }
    }
  }

  public static void generateSharingRandomSeeds() {
    HashSet<Integer> seeds = new HashSet<Integer>();
    S = new int[m];
    int num = m;
    while (num > 0) {
      int s = rand.nextInt();
      if (!seeds.contains(s)) {
        num--;
        S[num] = s;
        seeds.add(s);
      }
    }
  }

  public static void generateSharingRandomSeeds1() {
    HashSet<Integer> seeds = new HashSet<Integer>();
    S1 = new int[GRBits];
    int num = GRBits;
    while (num > 0) {
      int s = rand.nextInt();
      if (!seeds.contains(s)) {
        num--;
        S1[num] = s;
        seeds.add(s);
      }
    }
  }

  public static void encodeSpread(String filePath) throws FileNotFoundException {
    System.out.println("Encoding elements for SuperSpreader identification...");

    int nnn = 0;
    while (nnn < kkk) {
      if ((nnn) % 1000000 == 0) System.out.println(nnn);
      H.encodeSegment(flowida[nnn], elementida[nnn], SH, wh / mValueHLL);
      int[] y;

      F.encodefilter(flowida[nnn], elementida[nnn], S, S1, w / m);

      y = F.getestimate(flowida[nnn], elementida[nnn], S, w / m);

      Arrays.sort(y);
      int ppr = y[0];

      if (ppr >= tshold) {
        xset.add(flowida[nnn]);
      }

      nnn++;
    }
  }

  public static void estimateSpread(String filePath) throws FileNotFoundException {
    System.out.println("Estimating Flow CARDINALITY...");
    Scanner sc = new Scanner(new File(filePath));
    String resultFilePath =
        GeneralUtil.path
            + "FiltervHLL"
            + "_M_"
            + M / 1024
            + "_Mh_"
            + Mh / 1024
            + "_d_"
            + mValueGR
            + "_b_"
            + GRBits
            + "_sh_"
            + tshold;
    PrintWriter pw = new PrintWriter(new File(resultFilePath));
    for (int t = 0; t < m; t++) {
      H1 = H1.join(H, wh / mValueHLL, t);
    }
    int totalSum1 = H1.getValue();

    while (sc.hasNextLine()) {
      String entry = sc.nextLine();
      String[] strs = entry.split("\\s+");
      // String flowid = GeneralUtil.getSperadFlowIDAndElementID(strs, false)[0];
      long flowid = Long.parseLong(GeneralUtil.getSperadFlowIDAndElementID(strs, false)[0]);
      int num = Integer.parseInt(strs[strs.length - 1]);
      n += num;
      double xxx = 0.0;
      if (xset.contains(flowid)) {
        int virtualSum = H.getValueSegment(flowid, SH, wh / mValueHLL);
        Double estimate = Math.max(1.0 * (virtualSum - 1.0 * mValueHLL * totalSum1 / wh), 1);
        pw.println(entry + "\t" + estimate.intValue());
      } else {
        pw.println(entry + "\t" + "0");
      }
    }
    sc.close();
    pw.close();
  }
}
