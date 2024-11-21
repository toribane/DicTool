import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class GenKigou {

    static BufferedWriter bw;

    static void outputKigou(String group, int from, int to) throws IOException {
        int[] codePoints = new int[1];
        bw.write("# " + group + "\n");
        for (int ch = from; ch <= to; ch++) {
            codePoints[0] = ch;
            bw.write(new String(codePoints, 0, 1) + "\n");
        }
    }

    static public void main(String argv[]) throws Exception {

        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File("kigou.txt")), "UTF-8"));

        // 矢印
        outputKigou("矢印", 0x2190, 0x21FF);
        outputKigou("数学記号", 0x2200, 0x22FF);
        outputKigou("囲み英数字", 0x2460, 0x24FF);
        outputKigou("罫線素片", 0x2500, 0x257F);
        outputKigou("幾何学模様", 0x25A0, 0x25FF);
        outputKigou("その他の記号", 0x2600, 0x26FF);
        outputKigou("装飾記号", 0x2700, 0x27BF);
        outputKigou("cjk互換", 0x3300, 0x33FF);

        bw.flush();
        bw.close();
    }
}
