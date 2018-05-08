/**
 * @Author Mr.An
 * @Date 18/5/8 上午11:28
 * reverse the list 
 */
public class TestReverseList {

    public static Node reHead=null;
    public static Node Reverse1(Node head) {
        // head看作是前一结点，head.getNext()是当前结点，reHead是反转后新链表的头结点
        if (head == null || head.getNext() == null) {
            reHead=head;
            return head;// 若为空链或者当前结点在尾结点，则直接还回
        }
        Node reHead = Reverse1(head.getNext());// 先反转后续节点head.getNext()
        reHead.setNext(head);// 将当前结点的指针域指向前一结点
        head.setNext(null);
        return head;// 反转后新链表的头结点
    }

    public static void main(String[] args) {
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        Node node0=null;
        node1.setNext(node2);
        node2.setNext(node3);
        node3.setNext(node4);
        node4.setNext(null);

        reHead=node1;
        node0=reHead;
        while(node0!=null){
            System.out.println(node0.getData());
            node0=node0.getNext();
        }

        TestReverseList.Reverse1(node1);

        node0=reHead;
        while(node0!=null){
            System.out.println(node0.getData());
            node0=node0.getNext();
        }






    }

}
class Node {
    private int Data;// 数据域
    private Node Next;// 指针域

    public Node(int Data) {
        // super();
        this.Data = Data;
    }

    public int getData() {
        return Data;
    }

    public void setData(int Data) {
        this.Data = Data;
    }

    public Node getNext() {
        return Next;
    }

    public void setNext(Node Next) {
        this.Next = Next;
    }
}
/**
 * @program: test_fast
 * @description:
 * @author: Mr.An
 * @create: 2018-05-08 11:28
 **/
    
