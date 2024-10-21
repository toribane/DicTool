import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class GenDic {
    static String SYS_DIC_NAME = "system_dic";
    static String BTREE_NAME = "btree_dic";

    static Set<String> setLex = new TreeSet<>();
    static Set<String> setDic = new TreeSet<>();

    static void readLex(String filename) throws IOException {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            setLex.add(line);

            String[] data = line.split("\\t");
            if (data.length != 5) {
                continue;
            }
            String reading = data[0];
            int cost = Integer.parseInt(data[3]) + 10000;
            String surface = data[4];
            // 読み、コスト、表記順にソートして保存
            setDic.add(reading + "\t" + cost + "\t" + surface);

        }
        br.close();
    }

    static void writeDic() throws IOException {

        File f = new File(SYS_DIC_NAME + ".txt");
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);

        Files.deleteIfExists(Paths.get(SYS_DIC_NAME + ".db"));
        Files.deleteIfExists(Paths.get(SYS_DIC_NAME + ".lg"));

        Properties props = new Properties();
        RecordManager recman = RecordManagerFactory.createRecordManager(SYS_DIC_NAME, props);
        BTree tree = BTree.createInstance(recman, new StringComparator());

        recman.setNamedObject(BTREE_NAME, tree.getRecid());

        String key = "";
        Set<String> values = new LinkedHashSet<>();
        for (String line : setDic) {
            String data[] = line.split("\t");
            String reading = data[0];
            int cost = Integer.parseInt(data[1]) + 10000;
            String surface = data[2];

            if (reading.equals(key)) {
                values.add(surface);
            } else {
                if (values.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (String value : values) {
                        if (sb.length() != 0) {
                            sb.append("\t");
                        }
                        sb.append(value);
                    }
                    bw.write(key + "\t" + sb + "\n");
                    tree.insert(key, sb.toString(), true);
                }
                key = reading;
                values.clear();
                values.add(surface);
            }
        }
        // 
        if (values.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String value : values) {
                if (sb.length() != 0) {
                    sb.append("\t");
                }
                sb.append(value);
            }
            bw.write(key + "\t" + sb + "\n");
            tree.insert(key, sb.toString(), true);
        }
        recman.commit();
        recman.close();
        bw.close();
    }

    static public void main(String argv[]) throws Exception {

        readLex("./data/dictionary00.txt");
        readLex("./data/dictionary01.txt");
        readLex("./data/dictionary02.txt");
        readLex("./data/dictionary03.txt");
        readLex("./data/dictionary04.txt");
        readLex("./data/dictionary05.txt");
        readLex("./data/dictionary06.txt");
        readLex("./data/dictionary07.txt");
        readLex("./data/dictionary08.txt");
        readLex("./data/dictionary09.txt");
        readLex("./data/suffix.txt");

        writeDic();
    }
}
