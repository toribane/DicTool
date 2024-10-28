import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class GenConnection {

    static public void main(String argv[]) throws Exception {

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream("connection.bin")));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                new File("data/connection_single_column.txt")), "UTF-8"));

        String line;
        while ((line = br.readLine()) != null) {
            dos.writeShort(Integer.parseInt(line));
        }

        br.close();
        dos.flush();
        dos.close();
    }
}
