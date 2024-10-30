import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;

import jdbm.btree.BTree;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

public class TestDic {

    static BufferedWriter bwLog;

    static String SYS_DIC_NAME = "system_dic";
    static String BTREE_NAME = "btree_dic";
    static RecordManager recman;
    static BTree tree;

    static short numId;
    static short[] connectionId;

    static void readConnection() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream("connection.bin"));
        byte[] buf = new byte[4 * 1024];
        int len;
        while ((len = bis.read(buf, 0, buf.length)) > 0) {
            baos.write(buf, 0, len);
        }
        bis.close();

        // byte[] byteArray = baos.toByteArray();
        ShortBuffer sb = ByteBuffer.wrap(baos.toByteArray()).asShortBuffer();
        numId = sb.get();
        System.out.println("numId=" + numId);
        connectionId = new short[numId * numId];
        sb.get(connectionId);
        System.out.println("connectionId[0]=" + connectionId[0]);
        System.out.println("connectionId[1]=" + connectionId[1]);
        System.out.println("connectionId[2]=" + connectionId[2]);
    }

    // ２つのノード間のエッジのコストを返す
    static int getEdgeCost(Node left, Node right) {
        return connectionId[left.word.id * numId + right.word.id];
    }

    // 辞書から語句を取得する
    static ArrayList<Word> findWords(String reading) {
        try {
            String value = (String) tree.find(reading);
            if (value == null) {
                return null;
            }
            ArrayList<Word> list = new ArrayList<>();
            for (String s : value.split("\\t")) {
                String[] ss = s.split(",", 3);
                int id = Integer.valueOf(ss[0]);
                int cost = Integer.valueOf(ss[1]);
                String surface = ss[2];
                list.add(new Word(surface, id, cost));
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<Node> convert(String str, int nBest) throws IOException {
        // グラフ作成
        int len = str.length();
        List<List<Node>> graph = new ArrayList<>();
        for (int i = 0; i <= (len + 1); i++) {
            graph.add(i, new ArrayList<>());
        }

        Node bos = new Node(0, new Word("BOS", 0, 0));
        Node eos = new Node(len + 1, new Word("EOS", 0, 0));

        graph.get(0).add(bos); // BOS
        graph.get(len + 1).add(eos); // EOS

        // endPos文字目で終わる単語リストを作成
        for (int startPos = 1; startPos <= len; startPos++) {
            for (int endPos = startPos; endPos <= len; endPos++) {
                String substr = str.substring(startPos - 1, endPos);
                ArrayList<Word> words = findWords(substr);
                if (words == null) {
                    // 辞書にない場合の取り扱い
                    continue;
                }
                for (Word word : words) {
                    Node node = new Node(startPos, word);
                    graph.get(endPos).add(node);
                }
            }
        }

        // 前半はviterbiアルゴリズムで前向き
        for (int endPos = 1; endPos <= len; endPos++) {
            // endPos文字目で終わるノードのリスト
            List<Node> nodes = graph.get(endPos);
            for (Node node : nodes) {
                int best_cost = Integer.MAX_VALUE;
                // このノードの開始位置の一つ前が終わりのノード
                List<Node> prevNodes = graph.get(node.startPos - 1);
                //
                for (Node prevNode : prevNodes) {
                    int edgeCost = getEdgeCost(prevNode, node);
                    int tmp_cost = prevNode.costFromStart + edgeCost + node.word.cost;
                    if (tmp_cost < best_cost) {
                        best_cost = tmp_cost;
                    }
                }
                // 注目中のノードのスタート側のコスト
                node.costFromStart = best_cost;
            }
        }
        // 後半は優先度キューを使ってたどるノードを選んでいく
        List<Node> result = new ArrayList<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();
        // まず、優先度キューにゴールノード(EOS)を挿入する
        Node goalNode = graph.get(len + 1).get(0);
        pq.add(goalNode);
        // ここからループ
        while (!pq.isEmpty()) {
            Node node = pq.poll();
            if (node.startPos == 0) {
                // 取り出したノードがスタートノードであった場合、そのノードを結果に追加する
                result.add(node);
                if (result.size() >= nBest) {
                    break; //
                }
            } else {
                // スタートノードではなかった場合、そのノードに隣接するスタート側のノードのリストを取り出す
                List<Node> prevNodes = graph.get(node.startPos - 1);
                for (Node prevNode : prevNodes) {
                    int edgeCost = getEdgeCost(prevNode, node);
                    prevNode.costToGoal = node.costToGoal + edgeCost + node.word.cost;
                    prevNode.next = node;
                    // 優先度キューに追加
                    Node queueNode = new Node(prevNode);
                    queueNode.prio = prevNode.costFromStart + prevNode.costToGoal;
                    pq.add(queueNode);
                }
            }
        }
        return result;
    }

    public static void main(String argv[]) throws Exception {

        bwLog = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File("_log.txt")), "UTF-8"));

        Properties props = new Properties();
        recman = RecordManagerFactory.createRecordManager(SYS_DIC_NAME, props);
        long recid = recman.getNamedObject(BTREE_NAME);
        tree = BTree.load(recman, recid);

        readConnection();

        // String str = "ここではきものをぬいでください";
        // String str = "にわにはにわにわとりがいる";
        // String str = "すもももももももものうち";
        String str = "かれはがくせいです";
        // String str = "とべないぶた";
        // String str = "わたしのなまえはなかのです";
        List<Node> results = convert(str, 50);

        for (Node node : results) {
            int cost = node.costToGoal;
            for (node = node.next; node.next != null; node = node.next) {
                System.out.print(node.word.surface + " ");
            }
            System.out.println("; cost=" + cost);
        }

        bwLog.flush();
        bwLog.close();

    }
}
