
/* This file contains all information about the ISA in HashMaps
   and methods to access the HashMap */
import java.util.HashMap;

public class ISA {
    HashMap<String, String[]> instructions = new HashMap<>(20);
    HashMap<String, String[]> registers = new HashMap<>(8);

    public HashMap<String, String[]> getInstructions() {
        instructions.put("10000", new String[] { "add", "A" });
        instructions.put("10001", new String[] { "sub", "A" });
        instructions.put("10010", new String[] { "movi", "B" });
        instructions.put("10011", new String[] { "mov", "C" });
        instructions.put("10100", new String[] { "ld", "D" });
        instructions.put("10101", new String[] { "st", "D" });
        instructions.put("10110", new String[] { "mul", "A" });
        instructions.put("10111", new String[] { "div", "C" });
        instructions.put("11000", new String[] { "rs", "B" });
        instructions.put("11001", new String[] { "ls", "B" });
        instructions.put("11010", new String[] { "xor", "A" });
        instructions.put("11011", new String[] { "or", "A" });
        instructions.put("11100", new String[] { "and", "A" });
        instructions.put("11101", new String[] { "not", "C" });
        instructions.put("11110", new String[] { "cmp", "C" });
        instructions.put("11111", new String[] { "jmp", "E" });
        instructions.put("01100", new String[] { "jlt", "E" });
        instructions.put("01101", new String[] { "jgt", "E" });
        instructions.put("01111", new String[] { "je", "E" });
        instructions.put("01010", new String[] { "hlt", "F" });
        instructions.put("00000", new String[] { "addf", "A" });
        instructions.put("00001", new String[] { "subf", "A" });
        instructions.put("00010", new String[] { "movf", "B" });
        return instructions;
    }

    public HashMap<String, String[]> getRegisters() {
        registers.put("000", new String[] { "R0", "0000000000000000", "i" });
        registers.put("001", new String[] { "R1", "0000000000000000", "i" });
        registers.put("010", new String[] { "R2", "0000000000000000", "i" });
        registers.put("011", new String[] { "R3", "0000000000000000", "i" });
        registers.put("100", new String[] { "R4", "0000000000000000", "i" });
        registers.put("101", new String[] { "R5", "0000000000000000", "i" });
        registers.put("110", new String[] { "R6", "0000000000000000", "i" });
        registers.put("111", new String[] { "FLAGS", "0000000000000000", "f" }); // "0000 0000 0000 VLGE"
        return registers;
    }

    // --------------------------
    // TODO: Update these methods
    // --------------------------
    public String getRegName(String code) {
        return registers.get(code)[0];
    }

    public String getInstructionName(String code) {
        return instructions.get(code)[0];
    }

    public String getRegValue(String code) {
        return registers.get(code)[1];
    }

    public boolean isValidOpCode(String OpCode) {
        return instructions.containsKey((OpCode));
    }

    public void setRegValue(String code, int value, String type) {
        String reg = getRegName(code);
        String val[] = { reg, intToBinary(value, 16), type};
        registers.put(code, val);
    }

    public String getRegType(String code) {
        return registers.get(code)[2];
    }

    // public void setRegType(String code, String type) {
    //     String value[] = registers.get(code);        
    //     value[2] = type;
    //     registers.put(code, value);
    // }

    public void setRegValue(String code, double value, String type) {
        String reg = getRegName(code);
        String val[] = { reg, "00000000" + binToIEEE(decimalToBinary(value, 5)), type };
        registers.put(code, val);
    }

    public void setRegValue(String code, String value, String type) {
        String reg = getRegName(code);
        String val[] = { reg, value, type };
        registers.put(code, val);
    }

    public void setFlag(String value) {
        registers.put("111", new String[] { "FLAGS", value });
    }

    public static String binToIEEE(String decbin) {
        int exp = decbin.indexOf(".") - 1;
        String e = intToBinary(exp, 3);

        decbin = removeTrailingZeroes(decbin);
        decbin = decbin.replaceFirst("\\.", "");
        decbin = addChar(decbin, '.', 1);

        String decBinWithPadding = decbin.split("\\.")[1];
        String padding = "";
        for (int i = 0; i < 5 - decBinWithPadding.length(); i++) {
            padding += "0";
        }
        decBinWithPadding += padding;

        String binStr = e + decBinWithPadding;

        return binStr;
    }

    public static String intToBinary(int num, int bits) {
        String bin = Integer.toBinaryString(num);
        String padding = "";
        for (int i = 0; i < bits - bin.length(); i++) {
            padding += "0";
        }
        return padding + bin;
    }

    public static double binToFloat(String fnum) {
        String exp = fnum.substring(0, 3);
        String mantissa = fnum.substring(3, 8);
        int e = Integer.parseInt(exp, 2);
        StringBuilder m = new StringBuilder();
        for (int i = mantissa.length() - 1; i >= 0; i--) {
            m.append(mantissa.charAt(i));
        }
        m.reverse();
        m.insert(0, '1');
        m.insert(e + 1, '.');
        String n = m.toString();
        int intPart = Integer.parseInt(n.split("\\.")[0], 2);
        double decPart = 0;
        String dpart = n.split("\\.")[1];
        for (int i = 0; i < dpart.length(); i++) {
            int c = Character.getNumericValue(dpart.charAt(i));
            decPart += c * 1 / Math.pow(2, i + 1);
        }
        double res = intPart + decPart;
        return res;
    }

    public static String removeTrailingZeroes(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '0') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String addChar(String str, char ch, int position) {
        StringBuilder sb = new StringBuilder(str);
        sb.insert(position, ch);
        return sb.toString();
    }

    public static String decimalToBinary(double num, int k_prec) {
        String binary = "";
        int Integral = (int) num;
        double fractional = num - Integral;
        while (Integral > 0) {
            int rem = Integral % 2;
            binary += ((char) (rem + '0'));
            Integral /= 2;
        }
        binary = reverse(binary);
        binary += ('.');
        while (k_prec-- > 0) {
            fractional *= 2;
            int fract_bit = (int) fractional;
            if (fract_bit == 1) {
                fractional -= fract_bit;
                binary += (char) (1 + '0');
            } else {
                binary += (char) (0 + '0');
            }
        }
        return binary;
    }

    public static String reverse(String input) {
        char[] temparray = input.toCharArray();
        int left, right = 0;
        right = temparray.length - 1;
        for (left = 0; left < right; left++, right--) {
            char temp = temparray[left];
            temparray[left] = temparray[right];
            temparray[right] = temp;
        }
        return String.valueOf(temparray);
    }
}
