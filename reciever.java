import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class reciever {
    private Packet[] seq_array = new Packet[11];
    private List<Integer> sequence_list = new ArrayList<>();
    private DatagramSocket socket;
    private byte[] buf = new byte[1024];

    public reciever(DatagramSocket Socket) {
        this.socket = Socket;
    }

    public boolean check_duplicates(List<Integer> lista, int valor) {
        boolean check = false;
        for (int i = 0; i < lista.size(); i++){
            if (valor == lista.get(i)){
                check = true;
            }
        }
        
        return check;
    }

    public void GBN() throws IOException, ClassNotFoundException {

        int seq_expected = 0;
        boolean run = true;
        boolean teste = true;

        while (run){

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
            System.out.println("The content is: " + a);

            if (a.trim().equals("end")){
                run = false;
                System.out.println(sequence_list);
                for (int i = 0; i < seq_array.length; i ++){
                    String mensagem = new String(seq_array[i].get_content(),
                                        0,seq_array[i].get_content().length);
                    System.out.println(mensagem);
                }
                System.out.println("Server off");
                break;
                
            }
            
            int seq = p.get_seqNo();

            if (seq == seq_expected) {
                System.out.println("Sequencia recebida igual a esperada: " + seq);
                if (!check_duplicates(sequence_list, seq)){
                    // Check if the seq is duplicated for every packet
                    this.seq_array[seq] = p; // If it is'not, we add to our sequence on position
                    this.sequence_list.add(seq); // Just for Sure, we gonna take the sequences and print all after;
                }
                // Now that information is correct we 
                // Send the ack back to Sender by Thread packetSender.

                String info = new String(p.get_content(), 0, p.get_content().length);
                
                if (info.trim().equals("oi7") && teste) {
                    // The test of lost package is implemented here
                    System.out.println("Teste de perda de pacote");
                    teste = !teste; // We negate the test for make sure it does not make infinite
                    // packetSender ps = new packetSender(this.socket, "", seq -1, addr, port);
                    // ps.start();

                } else {

                    packetSender ps = new packetSender(this.socket, "", seq, addr, port);
                    ps.start();
                    
                    seq_expected++;
                }
            } else {
                // Computes de sequence number of the most recently recieved
                // in-order packet
                System.out.println("Sequencia recebida diferente da esperada");
                System.out.println("recebida: " + seq + " esperada: " + seq_expected);
                int last_seq_num = seq_expected - 1;
                
                if ( last_seq_num < 0) {
                    last_seq_num = -1;
                }
                // Now we have to send the ACK
                packetSender ps = new packetSender(this.socket, "", last_seq_num, addr, port);
                ps.start();
            }
            System.out.println("----------------------------------");         
        }
        this.socket.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        reciever p = new reciever(new DatagramSocket(9876));
        
        p.GBN();
        
    }
}