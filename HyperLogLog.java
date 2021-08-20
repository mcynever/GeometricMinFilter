import java.util.BitSet;

public class HyperLogLog {
  /** parameters for HLL */
  public int HLLSize; // unit size for each register
  public int m; // the number of registers in the HLL sketch
  public int maxRegisterValue;
  public double alpha;
  public BitSet[] HLLMatrix;

  public HyperLogLog(int m, int size) {
    HLLSize = size;
    this.m = m;
    maxRegisterValue = (int) (Math.pow(2, size) - 1);
    HLLMatrix = new BitSet[m];
    for (int i = 0; i < m; i++) {
      HLLMatrix[i] = new BitSet(size);
    }
    alpha = getAlpha(m);
  }

  public static int getBitSetValue(BitSet b) {
    int res = 0;
    for (int i = 0; i < b.length(); i++) {
      if (b.get(i)) res += Math.pow(2, i);
    }
    return res;
  }

  public int getValue() {
    return getValue(HLLMatrix);
  }
  public int getValue(BitSet[] sketch) {
    Double result = 0.0;
    int zeros = 0;
    int len = sketch.length;
    for (int i = 0; i < len; i++) {
      if (getBitSetValue(sketch[i]) == 0) zeros++;
      result += Math.pow(2, -1.0 * getBitSetValue(sketch[i]));
    }
    result = alpha * len * len / result;
    if (result <= 5.0 / 2.0 * len) { // small flows
      result = 1.0 * len * Math.log(1.0 * len / Math.max(zeros, 1));
    } else if (result > Integer.MAX_VALUE / 30.0) {
      result = -1.0 * Integer.MAX_VALUE * Math.log(1 - result / Integer.MAX_VALUE);
    }
    return result.intValue();
  }

  public void setBitSetValue(int index, int value) {
    int i = 0;
    while (value != 0 && i < HLLSize) {
      if ((value & 1) != 0) {
        HLLMatrix[index].set(i);
      } else {
        HLLMatrix[index].clear(i);
      }
      value = value >>> 1;
      i++;
    }
  }

  public double getAlpha(int m) {
    double a;
    if (m == 16) {
      a = 0.673;
    } else if (m == 32) {
      a = 0.697;
    } else if (m == 64) {
      a = 0.709;
    } else {
      a = 0.7213 / (1 + 1.079 / m);
    }
    return a;
  }

  public int getValueSegment(long flowID, int[] s, int w) {
    int ms = s.length;
    BitSet[] sketch = new BitSet[ms];
    for (int j = 0; j < ms; j++) {
      int i = j * w + (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowID) ^ s[j]) % w + w) % w;
      sketch[j] = HLLMatrix[i];
    }
    int result = getValue(sketch);
    return result;
  }

  public void encodeSegment(long flowID, long elementID, int[] s, int w) {
    int ms = s.length;

    int j = ((GeneralUtil.FNVHash1(flowID ^ elementID)) % ms + ms) % ms;

    int k = (GeneralUtil.intHash(GeneralUtil.FNVHash1(flowID) ^ s[j]) % w + w) % w;

    int i = j * w + k;
    int hash_val = GeneralUtil.FNVHash1(elementID);

    int leadingZeros = Integer.numberOfLeadingZeros(hash_val) + 1;
    leadingZeros = Math.min(leadingZeros, maxRegisterValue);
    int pp = getBitSetValue(HLLMatrix[i]);
    if (pp < leadingZeros) {
      setBitSetValue(i, leadingZeros);
    }
  }

  public HyperLogLog join(HyperLogLog hll, int w, int i) {
    for (int j = 0; j < w; j++) {
      if (getBitSetValue(HLLMatrix[i]) < getBitSetValue(hll.HLLMatrix[i * w + j]))
        HLLMatrix[i] = hll.HLLMatrix[i * w + j];
    }
    return this;
  }
}
