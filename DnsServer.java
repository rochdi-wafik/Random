import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
class DnsParser {

    public DnsParser DnsParser(){
        return this;
    }

    /**
     * Extract Hostname From DNS Qyery
     */
    public String getHostname(byte[] dnsQuery) {
        int position = 12; // Start after the DNS header (12 bytes)
        StringBuilder hostnameBuilder = new StringBuilder();

        while (position < dnsQuery.length) {
            int labelLength = dnsQuery[position];

            if (labelLength == 0) {
                break; // End of hostname
            }

            if ((labelLength & 0xC0) == 0xC0) {
                // Compressed label, jump to the offset specified in the next byte
                int offset = ((labelLength & 0x3F) << 8) | (dnsQuery[position + 1] & 0xFF);
                position = offset;
            } else {
                // Regular label
                for (int i = 0; i < labelLength; i++) {
                    hostnameBuilder.append((char) dnsQuery[position + 1 + i]);
                }
                hostnameBuilder.append(".");
                position += labelLength + 1;
            }
        }

        return hostnameBuilder.toString();
    }
    
}

class Dns {

    public String google_ip = "142.251.40.110";
    public String localhost_ip = "127.0.0.1";
    

    public void start(int port) throws IOException{

        // Create UDP Socket
        DatagramSocket socket = new DatagramSocket(port);
        byte[] data = new byte[1024];
        System.out.println("Listening on port: "+port);

        while(true){

            // Create packet container
            DatagramPacket packet = new DatagramPacket(data, data.length);

            // Receive packet
            socket.receive(packet);
            byte[] packetData = packet.getData();
            System.out.println("New request comming..");

            // Extract host from packet
            String hostname = new DnsParser().getHostname(packetData);
            System.out.println("Request domain: "+hostname);


            // Return response
            String replyMessage = localhost_ip;
            if(hostname.equals("google.com")){
                replyMessage = google_ip;
            }
            else if(hostname.equals("iorgana.com")){
                replyMessage = google_ip;
            }
            else if(hostname.equals("exit")){
                break;
            }
            else{
                replyMessage = "There is no ip related to this domain hhh";
            }
            byte[] replyData = replyMessage.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(replyData, replyData.length, packet.getAddress(), packet.getPort());
            socket.send(replyPacket);
            System.out.println("Response ip: "+replyMessage);


        }

        socket.close();

    }

    
}

public class DnsServer {
    public static void main(String[] args) throws Exception {

            

            Dns dns = new Dns();
            try {
                dns.start(53);
            } catch (IOException e) {
                System.out.println("Error: "+e.getMessage());
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


    }
}
