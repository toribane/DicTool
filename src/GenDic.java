import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class GenDic {
    static String SYS_DIC_NAME = "system_dic";
    static String BTREE_NAME = "btree_dic";

    static Map<String, ArrayList<Word>> map = new TreeMap<>();

    static void readLex(String filename) throws IOException {
        System.err.println(filename);
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {

            String[] data = line.split("\t");
            if (data.length != 5) {
                continue;
            }
            String reading = data[0];
            short lid = Short.parseShort(data[1]);
            short rid = Short.parseShort(data[2]);
            short cost = Short.parseShort(data[3]);
            String surface = data[4];

            ArrayList<Word> list = map.get(reading);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(new Word(cost, lid, rid, surface));
            map.put(reading, list);

        }
        br.close();
    }

    static void writeDic() throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(SYS_DIC_NAME + ".txt")), "UTF-8"));

        Files.deleteIfExists(Paths.get(SYS_DIC_NAME + ".db"));
        Files.deleteIfExists(Paths.get(SYS_DIC_NAME + ".lg"));

        RecordManager recman = RecordManagerFactory.createRecordManager(SYS_DIC_NAME);
        BTree tree = BTree.createInstance(recman, new StringComparator());
        recman.setNamedObject(BTREE_NAME, tree.getRecid());

        for (Map.Entry<String, ArrayList<Word>> entry : map.entrySet()) {
            String reading = entry.getKey();
            ArrayList<Word> words = entry.getValue();
            // コストの低い順にソート(確認のためだけ)
            words.sort(Comparator.comparing(Word::getCost));
            StringBuilder sb = new StringBuilder();
            for (Word word : words) {
                if (sb.length() != 0) {
                    sb.append("\t");
                }
                sb.append(word.toString());
            }
            bw.write(reading + "\t" + sb.toString() + "\n");
            tree.insert(reading, sb.toString(), true);
        }

        recman.commit();
        recman.close();

        bw.flush();
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
