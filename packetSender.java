import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



public class packetSender extends Thread {
    
    private DatagramSocket socket;
    private String message;
    private int seq;
    private InetAddress addr;
    private int port;

    
    public packetSender(DatagramSocket so, String msg, int seq, InetAddress addr, int port){

        this.socket = so;
        this.message = msg;
        this.seq = seq;
        this.addr = addr;
        this.port = port;
    }

    public void run() {
        //System.out.println("Thread " + seq + " Started");

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);

            Packet p = new Packet(seq, message);
            os.writeObject(p);
            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                                                        this.addr, this.port);

            this.socket.send(sendPacket);
            // System.out.println("Packet " + this.seq + " sent");

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}