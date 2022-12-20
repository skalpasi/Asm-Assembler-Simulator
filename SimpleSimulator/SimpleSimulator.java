import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SimpleSimulator {
    public static void main(String args[]) throws IOException {

        /* Get instructions and registers information from ISA class */
        ISA isa = new ISA();
        isa.getInstructions();
        isa.getRegisters();

        int codeLines = 0;

        /* Open code.txt and output.bin file */
        String machineCode = "";
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String input = in.nextLine();
            if (!input.isEmpty()) {
                machineCode += input;
                machineCode += "\n";
                codeLines++;
            }
        }

        /* If code file is empty */
        if (machineCode.equals("")) {
            System.out.println("");
            return;
        }

        String instruction = "";
        int PC = 0;

        String codeArr[] = machineCode.split("\n");
        ArrayList<Integer> trace = new ArrayList<>();

        String mem[] = new String[256];
        for (int i = 0; i < 256; i++) {
            if (i < codeArr.length) {
                mem[i] = codeArr[i];
            } else {
                mem[i] = "0000000000000000";
            }
        }

        int cycle = 0;

        // TODO - handling floating point overflow
        while (!instruction.equals("0101000000000000")) {

            if (cycle > 65535) break;

            System.out.print(intToBinary(PC, 8) + " ");

            instruction = codeArr[PC];
            String opCode = instruction.substring(0, 5);
            trace.add(PC);

            /* Check if op code is valid -> instruction is valid */
            if (!isa.isValidOpCode(opCode)) {
                System.out.println("Error: Invalid opcode");
                break;
            } else {
                if (opCode.equals("10000")) {
                    // add, type a
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    int op1, op2, sum = 0;
                    try {
                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        sum = op1 + op2;
                        if (sum < 65536)
                            isa.setRegValue(reg3, sum, "i");
                        else {
                            // System.out.println("Warning: Overflow detected");
                            isa.setRegValue(reg3, (int) (sum % Math.pow(2, 16)), "i");
                        }
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("10001")) {
                    // sub, type a
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    int op1, op2, diff = 0;
                    try {
                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        diff = op1 - op2;
                        if (diff < 0) {
                            // System.out.println("Warning: Underflow detected");
                            isa.setRegValue(reg3, 0, "i");
                            // return;
                        } else {
                            isa.setRegValue(reg3, diff, "i");
                        }
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("10010")) {
                    // movi, type b
                    String reg1 = instruction.substring(5, 8);
                    String imm = instruction.substring(8, 16);

                    isa.setRegValue(reg1, intToBinary(Integer.parseInt(imm, 2), 16), "i");
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("00010")) {
                    // movf, type b
                    String reg1 = instruction.substring(5, 8);
                    String imm = instruction.substring(8, 16);
                    imm = "00000000" + imm;

                    isa.setRegValue(reg1, imm, "d");
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("10011")) {
                    // mov, type c
                    String reg1 = instruction.substring(10, 13);
                    String reg2 = instruction.substring(13, 16);

                    isa.setRegValue(reg2, isa.getRegValue(reg1), "i"); // reg2 <- reg1
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("10100")) {
                    // ld, type d
                    String reg1 = instruction.substring(5, 8);
                    String addr = instruction.substring(8, 16);

                    isa.setRegValue(reg1, mem[Integer.parseInt(addr, 2)], "i");
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("10101")) {
                    // st, type d
                    String reg1 = instruction.substring(5, 8);
                    String addr = instruction.substring(8, 16);
                    int memloc = Integer.parseInt(addr, 2);
                    try {
                        int val = Integer.parseInt(isa.getRegValue(reg1), 2);
                        mem[memloc] = intToBinary(val, 16);
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        double val = binToFloat(isa.getRegValue(reg1));
                        mem[memloc] = "00000000" + binToIEEE(decimalToBinary(val, 5));
                        isa.setFlag("0000000000000000");
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("10110")) {
                    // mul, type a
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);
                    int op1, op2, mul = 0;
                    try {
                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        mul = op1 * op2;
                        if (mul < 65536)
                            isa.setRegValue(reg3, mul, "i");
                        else {
                            // System.out.println("Warning: Overflow detected");
                            isa.setRegValue(reg3, (int) (mul % Math.pow(2, 16)), "i");
                            // return;
                        }
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("10111")) {
                    // div, type c
                    String reg1 = instruction.substring(10, 13);
                    String reg2 = instruction.substring(13, 16);

                    int op1, op2, quo, rem;
                    try {
                        if (isa.getRegValue(reg2).equals("0000000000000000")) {
                            System.out.println("Error: division by zero not possible");
                            return;
                        }

                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);

                        quo = op1 / op2;
                        rem = op1 % op2;
                        isa.setRegValue("000", quo, "i"); // R0 -> quotient
                        isa.setRegValue("001", rem, "i"); // R1 -> remainder
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("11000")) {
                    // rs, type b
                    String reg1 = instruction.substring(5, 8);
                    String imm = instruction.substring(8, 16);

                    // op1 = op1 >> op2
                    int op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                    int op2 = Integer.parseInt(imm, 2);

                    int rs = op1 >> op2;
                    isa.setRegValue(reg1, rs, "i");
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("11001")) {
                    // ls, type b
                    String reg1 = instruction.substring(5, 8);
                    String imm = instruction.substring(8, 16);

                    // op1 = op1 << op2
                    int op1 = Integer.parseInt(isa.getRegValue(reg1));
                    int op2 = Integer.parseInt(imm, 2);

                    int ls = op1 << op2;
                    isa.setRegValue(reg1, ls, "i");
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("11010")) {
                    // xor, type a
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    try {
                        int op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        int op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        int xor = op1 ^ op2;
                        isa.setRegValue(reg3, xor, "i");
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        double op1 = binToFloat(isa.getRegValue(reg1));
                        double op2 = binToFloat(isa.getRegValue(reg2));
                        String val1 = "00000000" + binToIEEE(decimalToBinary(op1, 5));
                        String val2 = "00000000" + binToIEEE(decimalToBinary(op2, 5));
                        String xor = "";
                        for (int i = 0; i < 8; i++) {
                            if (val1.charAt(i) == val2.charAt(i))
                                xor += 0;
                            else
                                xor += 1;
                        }
                        isa.setRegValue(reg3, binToFloat(xor), "d");
                        isa.setFlag("0000000000000000");
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    } catch (Exception e) {
                        System.out.println("Unknown error");
                        return;
                    }

                } else if (opCode.equals("11011")) {
                    // or, type a
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    int op1, op2, or = 0;
                    try {
                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        or = op1 | op2;
                        isa.setRegValue(reg3, or, "i");
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("11100")) {
                    // and, type a
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    int op1, op2, and = 0;
                    try {
                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        and = op1 & op2;
                        isa.setRegValue(reg3, and, "i");
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("11101")) {
                    // not, type c
                    String reg1 = instruction.substring(10, 13);
                    String reg2 = instruction.substring(13, 16);

                    try {
                        String not = "";
                        String num = isa.getRegValue(reg1);
                        for (int i = 0; i < num.length(); i++) {
                            if (num.charAt(i) == '0')
                                not += "1";
                            else
                                not += "0";
                        }
                        isa.setRegValue(reg2, Integer.parseInt(not, 2), "i");
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not an integer");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }

                } else if (opCode.equals("11110")) {
                    // cmp, type c
                    String reg1 = instruction.substring(10, 13);
                    String reg2 = instruction.substring(13, 16);

                    try {
                        int op1, op2;
                        op1 = Integer.parseInt(isa.getRegValue(reg1), 2);
                        op2 = Integer.parseInt(isa.getRegValue(reg2), 2);
                        String flag = isa.getRegValue("111");

                        if (op1 < op2) {
                            flag = setCharAt(flag, 13, '1');
                            isa.setFlag(flag);
                        } else if (op1 > op2) {
                            flag = setCharAt(flag, 14, '1');
                            isa.setFlag(flag);
                        } else {
                            flag = setCharAt(flag, 15, '1');
                            isa.setFlag(flag);
                        }
                    } catch (NumberFormatException e) {
                        double op1, op2;
                        op1 = binToFloat(isa.getRegValue(reg1));
                        op2 = binToFloat(isa.getRegValue(reg2));
                        String flag = isa.getRegValue("111");

                        if (op1 < op2) {
                            flag = setCharAt(flag, 13, '1');
                            isa.setFlag(flag);
                        } else if (op1 > op2) {
                            flag = setCharAt(flag, 14, '1');
                            isa.setFlag(flag);
                        } else {
                            flag = setCharAt(flag, 15, '1');
                            isa.setFlag(flag);
                        }
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    } catch (Exception e) {
                        System.out.println("Error");
                        return;
                    }

                } else if (opCode.equals("11111")) {
                    // jmp, type e
                    String addr = instruction.substring(8, 16);
                    int newPC = Integer.parseInt(addr, 2);

                    PC = newPC;
                    PC--; // cuz we are doing PC++ at the end of loop
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("01100")) {
                    // jlt, type e
                    String addr = instruction.substring(8, 16);

                    if (isa.getRegValue("111").charAt(13) == '1') {
                        int newPC = Integer.parseInt(addr, 2);
                        PC = newPC;
                        PC--;
                    }
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("01101")) {
                    // jgt, type e
                    String addr = instruction.substring(8, 16);
                    if (isa.getRegValue("111").charAt(14) == '1') {
                        int newPC = Integer.parseInt(addr, 2);
                        PC = newPC;
                        PC--;
                    }
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("01111")) {
                    // je, type e
                    String addr = instruction.substring(8, 16);

                    if (isa.getRegValue("111").charAt(15) == '1') {
                        int newPC = Integer.parseInt(addr, 2);
                        PC = newPC;
                        PC--;
                    }
                    isa.setFlag("0000000000000000");

                } else if (opCode.equals("00000")) {
                    // addf
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    try {
                        // parse foat value from reg1 and reg2
                        // sum = op1 + op2;
                        double op1, op2, sum = 0;
                        op1 = binToFloat(isa.getRegValue(reg1));
                        op2 = binToFloat(isa.getRegValue(reg2));

                        sum = op1 + op2;
                        isa.setRegValue(reg3, "00000000" + binToIEEE(decimalToBinary(sum, 5)), "d");
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not a floating point number");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }
                } else if (opCode.equals("00001")) {
                    // subf
                    String reg1 = instruction.substring(7, 10);
                    String reg2 = instruction.substring(10, 13);
                    String reg3 = instruction.substring(13, 16);

                    double op1, op2, diff = 0;
                    try {
                        // parse foat value from reg1 and reg2
                        // diff = op1 - op2;
                        op1 = binToFloat(isa.getRegValue(reg1));
                        op2 = binToFloat(isa.getRegValue(reg2));
                        // System.out.println(op1 + " " + op2);
                        diff = op1 - op2;
                        isa.setRegValue(reg3, diff, "d");
                        isa.setFlag("0000000000000000");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: register value is not a floating point number");
                        return;
                    } catch (NullPointerException e) {
                        System.out.println("Error: register value not initialised");
                        return;
                    }
                }

                for (int i = 0; i < 7; i++) {
                    String key = intToBinary(i, 3);
                    if (isa.registers.get(key)[1] == null)
                        System.out.print("0000000000000000 ");
                    else {
                        System.out.print(isa.getRegValue(key) + " ");
                    }
                }
                System.out.print(isa.registers.get("111")[1]);
                System.out.print("\n");

                PC++;
                cycle++;
            }
        }

        for (String memory : mem) {
            System.out.println(memory);
        }

        in.close();
        memtrace(trace);
    }

    public static String setCharAt(String flag, int i, char newChar) {
        char[] chars = flag.toCharArray();
        chars[i] = newChar;
        flag = String.valueOf(chars);
        return flag;
    }

    public static String intToBinary(int num, int bits) {
        String bin = Integer.toBinaryString(num);
        String padding = "";
        for (int i = 0; i < bits - bin.length(); i++) {
            padding += "0";
        }
        return padding + bin;
    }

    public static void memtrace(ArrayList<Integer> mem) throws IOException {
        int len = mem.size();
        String pycode[] = { "import matplotlib.pyplot as plt",
                "x = [i for i in range(" + len + ")]",
                "y = ",
                "plt.scatter(x, y)",
                "plt.show()" };
        pycode[2] += "[";
        for (Integer addr : mem) {
            pycode[2] += "\"" + intToBinary(addr, 16) + "\"" + ",";
        }
        pycode[2] += "]";

        FileWriter output = new FileWriter("memtrace.py");
        output.write(String.join("\n", pycode));
        output.close();
    }

    public static double binToFloat(String fnum) {
        fnum = fnum.substring(8, 16);
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
}
