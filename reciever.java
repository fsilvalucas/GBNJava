import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class reciever {
    private Packet[] seq_array = new Packet[101];
    private List<Integer> sequence_list = new ArrayList<>();
    private int window = 3;
    private int n_packets = 20;
    private DatagramSocket socket;
    private byte[] buf = new byte[1024];

    public reciever(DatagramSocket Socket) {
        this.socket = Socket;
    }


    public void GBN() throws IOException, ClassNotFoundException {

        int base = 0;
        int seq_expected = 0;
        boolean run = true;

        while (run){

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);

            DatagramPacket recieved = new DatagramPacket(this.buf, this.buf.length);
            this.socket.receive(recieved); // Blocking;
            InetAddress addr = recieved.getAddress();
            int port = recieved.getPort();
            byte[] data = recieved.getData();

            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
        
            Packet p = (Packet) is.readObject();
            System.out.println("Packet object received = " + p);
            String a = new String(p.get_content(), 0, p.get_content().length);
            System.out.println(a);

            if (a.trim().equals("end")){
                run = false;
                System.out.println(sequence_list);
                
            }
            
            int seq = p.get_seqNo();
            System.out.println(seq);

            if (seq == seq_expected) {
                System.out.println("Seq == seq_expected");
                // Expecting the sequence nuber of the recieved packet?
                this.seq_array[seq] = p; // If it is, we add to our sequence on position
                this.sequence_list.add(seq); // Just for Sure, we gonna take the sequences and print all after;

                // Now that information is correct we 
                // Send the ack back to Sender.
                // Packet ack = new Packet(seq);
                // System.out.println("createdACK, to: addr, port: " + addr + port);
                // os.writeObject(ack);
                // byte[] dt = outputStream.toByteArray();
                // DatagramPacket sendPacket = new DatagramPacket(dt, dt.length,
                //                                         addr, port);

                
                // this.socket.send(sendPacket);
                packetSender ps = new packetSender(this.socket, "", seq, addr, port);
                ps.start();
                
                seq_expected++;

            } else {
                // Computes de sequence number of the most recently recieved
                // in-order packet
                System.out.println("!!!Seq == seq_expected");
                int last_seq_num = seq_expected - 1;
                
                if ( last_seq_num < 0) {
                    last_seq_num = -1;
                }
                // Now we have to send the ACK
                // Packet ack = new Packet(last_seq_num);
                // os.writeObject(ack);
                // byte[] dt = outputStream.toByteArray();
                // DatagramPacket sendPacket = new DatagramPacket(dt, dt.length,
                //                                         recieved.getAddress(), recieved.getPort());
                
                // this.socket.send(sendPacket);
                packetSender ps = new packetSender(this.socket, "", last_seq_num, addr, port);
                ps.start();
            }         
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        reciever p = new reciever(new DatagramSocket(9876));
        
        p.GBN();
        
    }
}