import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.utils.JavaUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.Key;

abstract class DocumentHandler {

    private static final String PATHNAME = "weatherdata.xml";
    static final File FILE = new File(PATHNAME);
    static Transformer aTransformer;
    static DocumentBuilder builder;
    static Document document;

    void init() {
        try {
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            aTransformer = tranFactory.newTransformer();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    void createNewDocument() {
        builder.reset();
        document = builder.newDocument();
        System.out.println("New document created");
    }

    void deleteDocument() {
        if (FILE.exists()) {
            if (!FILE.delete()) {
                System.err.println("Could not delete \"" + PATHNAME + "\". Retrying...");
                try {
                    Thread.sleep(1000);
                    deleteDocument();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Deleted " + PATHNAME + ".");
        }
    }

    void writeToFile() {
        try {
            System.out.println("Writing to file...");

            //start encryption
            com.sun.org.apache.xml.internal.security.Init.init();
            Source src = new DOMSource(document);
            FileWriter writer = new FileWriter(FILE);
            Result dest = new StreamResult(writer);
            aTransformer.transform(src, dest);
            Key kek = loadKeyEncryptionKey();
            Key symmetricKey = GenerateDataEncryptionKey();
            String algorithmURI = XMLCipher.TRIPLEDES_KeyWrap;
            XMLCipher keyCipher = XMLCipher.getInstance(algorithmURI);
            keyCipher.init(XMLCipher.WRAP_MODE, kek);
            EncryptedKey encryptedKey = keyCipher.encryptKey(document, symmetricKey);
            Element rootElement = document.getDocumentElement();
            algorithmURI = XMLCipher.AES_128;
            XMLCipher xmlCipher = XMLCipher.getInstance(algorithmURI);
            xmlCipher.init(XMLCipher.ENCRYPT_MODE, symmetricKey);
            EncryptedData encryptedData = xmlCipher.getEncryptedData();
            KeyInfo keyInfo = new KeyInfo(document);
            keyInfo.add(encryptedKey);
            encryptedData.setKeyInfo(keyInfo);
            xmlCipher.doFinal(document, rootElement, true);
            outputDocToFile(document, "weatherdata.xml");
            //encryption finished

            writer.close();
            System.out.println("Writing complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SecretKey loadKeyEncryptionKey() throws Exception {
        String fileName = "key.xml";
        String jceAlgorithmName = "DESede";
        File kekFile = new File(fileName);
        DESedeKeySpec keySpec = new DESedeKeySpec(JavaUtils.getBytesFromFile(fileName));
        SecretKeyFactory skf = SecretKeyFactory.getInstance(jceAlgorithmName);
        SecretKey key = skf.generateSecret(keySpec);

        System.out.println("Key encryption key loaded from " + kekFile.toURI().toURL().toString());
        return key;
    }

    private static SecretKey GenerateDataEncryptionKey() throws Exception {
        String jceAlgorithmName = "AES";
        KeyGenerator keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private static void outputDocToFile(Document doc, String fileName) throws Exception {
        File encryptionFile = new File(fileName);
        FileOutputStream f = new FileOutputStream(encryptionFile);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(f);
        transformer.transform(source, result);
        f.close();
        System.out.println("Wrote document containing encrypted data to " + encryptionFile.toURI().toURL().toString());
    }
}
