public class GeneralUtil {
  public static String path = "\\D:\\GF-EXP\\";
  public static Boolean isDstAsID = true;

  public static String dataStreamForFlowSpread = "\\D:\\CAIDA\\final_result_1\\output1.txt";
  public static String dataSummaryForFlowSpread = "\\D:\\CAIDA\\final_result_1\\dstCard1.txt";

  /** get flow id and element id for spread measurement in each row of a file. */
  public static String[] getSperadFlowIDAndElementID(String[] strs, Boolean isEncoding) {
    String[] res = new String[2];
    if (isEncoding) {
      if (isDstAsID) {res[0] = strs[1]; res[1] = strs[0];}
      else {res[0] = strs[0]; res[1] = strs[1];}
    } else {
      res[0] = strs[0];
    }
    return res;
  }

  public static int FNVHash1(long key) {
    key = (~key) + (key << 18); // key = (key << 18) - key - 1;
    key = key ^ (key >>> 31);
    key = key * 21; // key = (key + (key << 2)) + (key << 4);
    key = key ^ (key >>> 11);
    key = key + (key << 6);
    key = key ^ (key >>> 22);
    return (int) key;
  }

  /** Thomas Wang hash */
  public static int intHash(int key) {
    key += ~(key << 15);
    key ^= (key >>> 10);
    key += (key << 3);
    key ^= (key >>> 6);
    key += ~(key << 11);
    key ^= (key >>> 16);
    return key;
  }

}


