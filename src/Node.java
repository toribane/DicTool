public class Node implements Comparable<Node> {

    int startPos;
    Word word;
    int costFromStart; // スタートからこのノードまでの最小コスト
    int costToGoal; // このノードからゴールまでのコスト
    Node next;
    int prio;   // 優先度付きキューへの登録に使用する優先度

    public Node(int startPos, Word word) {
        this.startPos = startPos;
        this.word = word;
    }

    public Node(Node node) {
        this.startPos = node.startPos;
        this.word = node.word;
        this.costFromStart = node.costFromStart;
        this.costToGoal = node.costToGoal;
        this.next = node.next;
        this.prio = node.prio;
    }

    @Override
    public int compareTo(Node node) {
        return this.prio - node.prio;
    }

    @Override
    public String toString() {
        return "[" + word.surface + "(" + costFromStart + ")" + word.cost +"]";
    }

}
