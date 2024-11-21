import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class GenEmoji {

    static public void main(String argv[]) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                new File("data/emoji-test.txt")), "UTF-8"));

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File("emoji.txt")), "UTF-8"));

        String line;
        while ((line = br.readLine()) != null) {
            line = line.strip();

            if (line.startsWith("# group")) {
                String group = "";
                switch (line) {
                    case "# group: Component":
                        continue;
                    case "# group: Smileys & Emotion":
                        group = "顔文字と感情";
                        break;
                    case "# group: People & Body":
                        group = "人と体";
                        break;
                    case "# group: Animals & Nature":
                        group = "動物と自然";
                        break;
                    case "# group: Food & Drink":
                        group = "食べ物と飲み物";
                        break;
                    case "# group: Travel & Places":
                        group = "旅行と場所";
                        break;
                    case "# group: Activities":
                        group = "活動";
                        break;
                    case "# group: Objects":
                        group = "物";
                        break;
                    case "# group: Symbols":
                        group = "記号";
                        break;
                     case "# group: Flags":
                        group = "旗";
                        break;
                }
                System.out.println(line + " : " + group);
                bw.write("# " + group + "\n");
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            String[] ss = line.split("#");
            if (ss.length != 2) {
                continue;
            }
            if (!ss[0].contains("fully-qualified")) {
                continue;
            }
            // component
            if (ss[0].contains("1F3FB")) {
                continue;
            }
            if (ss[0].contains("1F3FC")) {
                continue;
            }
            if (ss[0].contains("1F3FD")) {
                continue;
            }
            if (ss[0].contains("1F3FE")) {
                continue;
            }
            if (ss[0].contains("1F3FF")) {
                continue;
            }
            if (ss[0].contains("1F9B0")) {
                continue;
            }
            if (ss[0].contains("1F9B1")) {
                continue;
            }
            if (ss[0].contains("1F9B2")) {
                continue;
            }
            if (ss[0].contains("1F9B3")) {
                continue;
            }

            String[] ss2 = ss[1].strip().split(" ");
            bw.write(ss2[0] + "\n");

        }
        bw.flush();
        bw.close();

        br.close();
    }
}
