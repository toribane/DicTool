import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
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
            if (data.length < 5) {
                continue;
            }
            String reading = toHiragana(data[0]).strip();
            short lid = Short.parseShort(data[1]);
            short rid = Short.parseShort(data[2]);
            short cost = Short.parseShort(data[3]);
            String surface = data[4].strip();

            if (reading.length() == 0 || surface.length() == 0) {
                continue;
            }

            if (isKanaOnly(reading) && isKanaOnly(surface)) {
                if (!reading.equals(toHiragana(surface))) {
                    // System.out.println(line);
                    reading = toHiragana(surface);
                }
            }

            ArrayList<Word> oldList = map.get(reading);
            if (oldList == null) {
                ArrayList<Word> newList = new ArrayList<>();
                newList.add(new Word(lid, rid, cost, surface));
                map.put(reading, newList);
            } else {
                ArrayList<Word> newList = new ArrayList<>();
                for (Word word : oldList) {
                    if (word.lid == lid && word.rid == rid && word.surface.equals(surface)) {
                        // cost以外は同じ
                        // System.out.println(word + "\t" + cost);
                        if (cost > word.cost) {
                            cost = word.cost;
                        }
                        // このwordは保留
                    } else {
                        newList.add(word);
                    }
                }
                newList.add(new Word(lid, rid, cost, surface));
                map.put(reading, newList);
            }
        }
        br.close();
    }

    static boolean isKanaOnly(String s) {
        return s.matches("^[ぁ-ゖァ-ヶー]+$");
    }

    static char toHiragana(char c) {
        if (c >= 'ァ' && c <= 'ヶ') {
            return (char) (c - 'ァ' + 'ぁ');
        }
        return c;
    }

    static String toHiragana(CharSequence cs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cs.length(); i++) {
            sb.append(toHiragana(cs.charAt(i)));
        }
        return sb.toString();
    }

    static void writeDic() throws IOException {
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

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (Word word : words) {
                dos.writeShort(word.lid);
                dos.writeShort(word.rid);
                dos.writeShort(word.cost);
                dos.writeUTF(word.surface);
            }
            byte[] byteArray = baos.toByteArray();
            tree.insert(reading, byteArray, true);
        }

        recman.commit();
        recman.close();
    }

    static void dumpDic() throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(SYS_DIC_NAME + ".txt")), "UTF-8"));

        RecordManager recman = RecordManagerFactory.createRecordManager(SYS_DIC_NAME);
        BTree tree = BTree.load(recman, recman.getNamedObject(BTREE_NAME));

        TupleBrowser browser = tree.browse();
        Tuple tuple = new Tuple();

        while (browser.getNext(tuple)) {
            StringBuilder sb = new StringBuilder((String) tuple.getKey());
            byte[] byteArray = (byte[]) tuple.getValue();
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(byteArray));
            while (dis.available() > 0) {
                short lid = dis.readShort();
                short rid = dis.readShort();
                short cost = dis.readShort();
                String surface = dis.readUTF();
                sb.append("\t" + lid + "," + rid + "," + cost + "," + surface);
            }
            bw.write(sb.toString() + "\n");
        }

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
        dumpDic();
    }

    static class Word implements Comparable<Word> {

        public short lid;
        public short rid;
        public short cost;
        public String surface;

        public Word(short lid, short rid, short cost, String surface) {
            this.lid = lid;
            this.rid = rid;
            this.cost = cost;
            this.surface = surface;
        }

        public Word(String s) {
            String[] ss = s.split(",", 4);
            this.lid = Short.parseShort(ss[0]);
            this.rid = Short.parseShort(ss[1]);
            this.cost = Short.parseShort(ss[2]);
            this.surface = ss[3].strip();
        }

        @Override
        public String toString() {
            return lid + "," + rid + "," + cost + "," + surface;
        }

        public int getCost() {
            return (int) cost;
        }

        @Override
        public int compareTo(Word word) {
            if (cost != word.cost) {
                return (int) (cost - word.cost);
            }
            return surface.compareTo(word.surface);
        }

    }

}
