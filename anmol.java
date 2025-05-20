import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class anmol {
    public static void main(String[] args) throws Exception {
        // Path to your debug.keystore
        String keystorePath = System.getProperty("user.home") + "/.android/debug.keystore";
        
        // Load the keystore
        FileInputStream fis = new FileInputStream(keystorePath);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
        
        // Get the SHA-1 fingerprint
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        
        // Convert to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X:", b & 0xff));
        }
        // Remove the last colon
        String sha1 = sb.substring(0, sb.length() - 1);
        
        System.out.println("SHA1: " + sha1);
    }
}