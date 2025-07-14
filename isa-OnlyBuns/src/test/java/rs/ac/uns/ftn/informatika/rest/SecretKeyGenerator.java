package rs.ac.uns.ftn.informatika.rest;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // Generiše 256-bitni ključ za HmacSHA256 algoritam
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256); // 256 bita je preporučeno za HS256 JWT-ove
        SecretKey secretKey = keyGen.generateKey();

        // Enkodira ključ u Base64 string
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("-------------------------------------------------------------");
        System.out.println("Tvoj novi, Base64-enkodirani JWT secret ključ je:");
        System.out.println(encodedKey);
        System.out.println("-------------------------------------------------------------");
    }
}