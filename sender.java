import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class sender {

    private int port;
    private InetAddress address;
    private DatagramSocket socket;
    private int window = 3;
    private int n_packages = 10;
    private List<Integer> seqs = new ArrayList<>();

    public sender(DatagramSocket Socket, InetAddress addres, int port) throws SocketException {
        this.socket = Socket;
        this.address = addres;
        this.port = port;

        socket.setSoTimeout(100);
    }

    public void GBN2(String message, boolean order) throws IOException, ClassNotFoundException {

        int base = 0; // When base becomes equal to n_packages, all packets were sent.
        int tail = this.window - 1;

        while (base <= this.n_packages) {
            int head = base;

            while (head <= tail && head <= n_packages) {

                // Object Thread to send Messages
                packetSender ps = new packetSender(this.socket, message + Integer.toString(head), head, this.address,
                        this.port);

                ps.start();
                if (order){
                    try {
                        Thread.sleep(80); // 0.08 sec
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                head++;
            }

            try {
                while (true){
                    byte[] ack = new byte[1024];
                    DatagramPacket rPacket = new DatagramPacket(ack, ack.length);
                    System.out.println("Socket receiving on bellow");
                    this.socket.receive(rPacket);
                    byte[] data = rPacket.getData();
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    ObjectInputStream is = new ObjectInputStream(in);
                    Packet p = (Packet) is.readObject();
                    int seq = p.get_seqNo();
                    System.out.println(seq);
                    this.seqs.add(seq);
                }
            } catch (SocketTimeoutException e) {
                // e.printStackTrace();
                //int max = Collections.max(seqs);
                int max = seqs.get(seqs.size() - 1);
                if (max == base - 1){
                    continue;
                } else{
                    tail = max + this.window;
                    base = max + 1;
                }
            }
        }

        // The final Packet "end" will kill the server
        packetSender ps = new packetSender(this.socket, "end", -1, this.address,
                        this.port);

        ps.start();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        boolean order = false; 
        // the boolean order is to test the order of the packet;
        // The order is control by Thread execution;
        // So controls the order of threads, so it can be sent in order or not!

        sender p = new sender(new DatagramSocket(), InetAddress.getByName("localhost"), 9876);
        p.GBN2("oi", order);
    }
}