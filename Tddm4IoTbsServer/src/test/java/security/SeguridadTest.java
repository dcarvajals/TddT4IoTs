/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author geova
 */
public class SeguridadTest {

    public SeguridadTest() {
    }

    @Test
    public void cifrado() {
//        String palabraclave = "BioForest";
//        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
//        textEncryptor.setPassword(palabraclave);
//        String myEncryptedText = textEncryptor.encrypt("Cauje de rio en quebrada perpendicular").replace('/', '+');
//        System.out.println("Texto encriptado:\n" + myEncryptedText);
//        /**
//         * Desencriptando
//         */
//        String plainText = textEncryptor.decrypt(myEncryptedText.replace('+', '/'));
//        System.out.println("Texto desencriptado:\n" + plainText);
//        assertEquals(plainText, "2");
    }

    @Test
    public void MD5() {
        String Cifrar = "@InfoP@$$W0rd@";
        String base64EncryptedString = "";
        try {
            String secretKey = "EasyIot-UML Diagram Tools - TDDM4IoTbs"; //llave para desenciptar datos
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);

            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] plainTextBytes = Cifrar.getBytes("utf-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encodeBase64(buf);
            base64EncryptedString = new String(base64Bytes);

            String base64DesecryptedString = base64EncryptedString;

            byte[] message = Base64.decodeBase64(base64DesecryptedString.getBytes("utf-8"));
            MessageDigest mdD = MessageDigest.getInstance("MD5");
            byte[] digestOfPasswordD = mdD.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytesD = Arrays.copyOf(digestOfPasswordD, 24);
            SecretKey keyD = new SecretKeySpec(keyBytesD, "DESede");

            Cipher decipher = Cipher.getInstance("DESede");
            decipher.init(Cipher.DECRYPT_MODE, keyD);

            byte[] plainText = decipher.doFinal(message);

            base64DesecryptedString = new String(plainText, "UTF-8");
            System.out.println(Cifrar);
            System.out.println(base64EncryptedString);
            System.out.println(base64DesecryptedString);

            assertEquals(base64DesecryptedString, Cifrar);

        } catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(SeguridadTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
