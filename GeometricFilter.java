import java.util.BitSet;


public class GeometricFilter {

  public int RegisterSize; // unit size for each register
  public int m; // the number of registers in the Filter
  public int GeometrichashBits;
  public int mGeometric;
  public int maxRegisterValue;
  public BitSet[] RegisterArray;

  public GeometricFilter(int m, int size, int Mbit, int num) {
    RegisterSize = size;
    this.m = m;
    this.GeometrichashBits = Mbit;
    this.mGeometric = num;
    maxRegisterValue = (int) (Math.pow(2, size) - 1);
    RegisterArray = new BitSet[m];
    for (int i = 0; i < m; i++) {
      RegisterArray[i] = new BitSet(size);
    }
  }

  public static int getBitSetValue(BitSet b) {
    int res = 0;
    for (int i = 0; i < b.length(); i++) {
      if (b.get(i)) res += Math.pow(2, i);
    }
    return res;
  }

  public void setBitSetValue(int index, int value) {
    int i = 0;
    while (value != 0 && i < RegisterSize) {
      if ((value & 1) != 0) {
        RegisterArray[index].set(i);
      } else {
        RegisterArray[index].clear(i);
      }
      value = value >>> 1;
      i++;
    }
  }

  public int[] getestimate(long flowID, long elementID, int[] s, int w) {
    int ms = s.length;
    int[] res = new int[mGeometric];
    for (int j = 0; j < mGeometric; j++) {
      w = this.m;
      int k = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowID) ^ s[j]) % w + w) % w;
      res[j] = getBitSetValue(RegisterArray[k]);
      elementID = GeneralUtil.FNVHash1(elementID);
    }
    return res;
  }

  public int getleadingzeros(int[] h, int[] s) {
    int res = 1;
    while (res < maxRegisterValue) {
      int hh = 0;
      for (int l = 0; l < h.length; l++) {
        h[l] = GeneralUtil.intHash(h[l] ^ s[l]);
        if (l == 0) hh = h[l];
        else hh &= h[l];
      }
      int cur = Integer.numberOfLeadingZeros(hh);
      res += cur;
      if (cur < 32) break;
    }
    return res;
  }

  public void encodefilter(long flowID, long elementID, int[] s, int[] s1, int w) {
    int ms = s.length;
    for (int j = 0; j < mGeometric; j++) {
      w = this.m;
      int k = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowID) ^ s[j]) % w + w) % w;
      int hash_val = GeneralUtil.intHash(GeneralUtil.FNVHash1(elementID) ^ s[j]);
      int[] hashvalue = new int[s1.length];

      for (int l = 0; l < s1.length; l++) {
        hashvalue[l] = hash_val;
      }

      int leadingZeros = getleadingzeros(hashvalue, s1);
      leadingZeros = Math.min(leadingZeros, maxRegisterValue);
      int pp = getBitSetValue(RegisterArray[k]);
      if (pp < leadingZeros) {
        setBitSetValue(k, leadingZeros);
      }
    }
  }
}
