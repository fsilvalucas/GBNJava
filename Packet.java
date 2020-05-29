import java.io.Serializable;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Packet implements Serializable{

    private int seqNo;
    private long checkSum;
    private byte[] content;
    private int len;

    public Packet(int seqNo, String data){
        this.seqNo = seqNo;
        this.content = data.getBytes();
        this.len = data.length();
        this.checkSum = check_sum(this.content);
    }

    public Packet (int seqNo){
        this.seqNo = seqNo;
        this.content = "ack".getBytes();
        this.len = 0;
        this.checkSum = 0;
    }

    private long check_sum(byte[] msg) {
        Checksum check = new CRC32();
        check.update(msg, 0, msg.length);
        return check.getValue();
    }

    public static Packet createACK_response(int seqNum) throws Exception {
        return new Packet(seqNum);
    }

    public long get_checksum(){
        return this.checkSum;
    }

    public byte[] get_content() {
        return this.content;
    }

    public int get_seqNo(){
        return this.seqNo;
    }

    public static void main(String[] args) {
        Packet p = new Packet(0, "Ola");
        System.out.println(p.get_checksum());
    }
}

