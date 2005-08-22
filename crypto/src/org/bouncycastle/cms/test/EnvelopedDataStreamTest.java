package org.bouncycastle.cms.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.crypto.SecretKey;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class EnvelopedDataStreamTest
    extends TestCase
{
    private static String          _signDN;
    private static KeyPair         _signKP;
    private static X509Certificate _signCert;

    private static String          _origDN;
    private static KeyPair         _origKP;
    private static X509Certificate _origCert;

    private static String          _reciDN;
    private static KeyPair         _reciKP;
    private static X509Certificate _reciCert;
    
    private static boolean         _initialised = false;
    
    public EnvelopedDataStreamTest()
    {
    }

    private static void init()
        throws Exception
    {
        if (!_initialised)
        {
            _initialised = true;
            
            _signDN   = "O=Bouncy Castle, C=AU";
            _signKP   = CMSTestUtil.makeKeyPair();  
            _signCert = CMSTestUtil.makeCertificate(_signKP, _signDN, _signKP, _signDN);
    
            _origDN   = "CN=Bob, OU=Sales, O=Bouncy Castle, C=AU";
            _origKP   = CMSTestUtil.makeKeyPair();
            _origCert = CMSTestUtil.makeCertificate(_origKP, _origDN, _signKP, _signDN);
    
            _reciDN   = "CN=Doug, OU=Sales, O=Bouncy Castle, C=AU";
            _reciKP   = CMSTestUtil.makeKeyPair();
            _reciCert = CMSTestUtil.makeCertificate(_reciKP, _reciDN, _signKP, _signDN);       
        }
    }
    
    public void testWorkingData()
        throws Exception
    {
        byte[]  keyData = Base64.decode(
                  "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKrAz/SQKrcQ" +
                  "nj9IxHIfKDbuXsMqUpI06s2gps6fp7RDNvtUDDMOciWGFhD45YSy8GO0mPx3" +
                  "Nkc7vKBqX4TLcqLUz7kXGOHGOwiPZoNF+9jBMPNROe/B0My0PkWg9tuq+nxN" +
                  "64oD47+JvDwrpNOS5wsYavXeAW8Anv9ZzHLU7KwZAgMBAAECgYA/fqdVt+5K" +
                  "WKGfwr1Z+oAHvSf7xtchiw/tGtosZ24DOCNP3fcTXUHQ9kVqVkNyzt9ZFCT3" +
                  "bJUAdBQ2SpfuV4DusVeQZVzcROKeA09nPkxBpTefWbSDQGhb+eZq9L8JDRSW" +
                  "HyYqs+MBoUpLw7GKtZiJkZyY6CsYkAnQ+uYVWq/TIQJBAP5zafO4HUV/w4KD" +
                  "VJi+ua+GYF1Sg1t/dYL1kXO9GP1p75YAmtm6LdnOCas7wj70/G1YlPGkOP0V" +
                  "GFzeG5KAmAUCQQCryvKU9nwWA+kypcQT9Yr1P4vGS0APYoBThnZq7jEPc5Cm" +
                  "ZI82yseSxSeea0+8KQbZ5mvh1p3qImDLEH/iNSQFAkAghS+tboKPN10NeSt+" +
                  "uiGRRWNbiggv0YJ7Uldcq3ZeLQPp7/naiekCRUsHD4Qr97OrZf7jQ1HlRqTu" +
                  "eZScjMLhAkBNUMZCQnhwFAyEzdPkQ7LpU1MdyEopYmRssuxijZao5JLqQAGw" +
                  "YCzXokGFa7hz72b09F4DQurJL/WuDlvvu4jdAkEAxwT9lylvfSfEQw4/qQgZ" +
                  "MFB26gqB6Gqs1pHIZCzdliKx5BO3VDeUGfXMI8yOkbXoWbYx5xPid/+N8R//" +
                  "+sxLBw==");
        
        byte[] envData = Base64.decode(
                  "MIAGCSqGSIb3DQEHA6CAMIACAQAxgcQwgcECAQAwKjAlMRYwFAYDVQQKEw1C" +
                  "b3VuY3kgQ2FzdGxlMQswCQYDVQQGEwJBVQIBHjANBgkqhkiG9w0BAQEFAASB" +
                  "gDmnaDZ0vDJNlaUSYyEXsgbaUH+itNTjCOgv77QTX2ImXj+kTctM19PQF2I1" +
                  "0/NL0fjakvCgBTHKmk13a7jqB6cX3bysenHNrglHsgNGgeXQ7ggAq5fV/JQQ" +
                  "T7rSxEtuwpbuHQnoVUZahOHVKy/a0uLr9iIh1A3y+yZTZaG505ZJMIAGCSqG" +
                  "SIb3DQEHATAdBglghkgBZQMEAQIEENmkYNbDXiZxJWtq82qIRZKggAQgkOGr" +
                  "1JcTsADStez1eY4+rO4DtyBIyUYQ3pilnbirfPkAAAAAAAAAAAAA");


        CMSEnvelopedDataParser     ep = new CMSEnvelopedDataParser(envData);

        RecipientInformationStore  recipients = ep.getRecipientInfos();

        assertEquals(ep.getEncryptionAlgOID(), CMSEnvelopedDataGenerator.AES128_CBC);
        
        Collection  c = recipients.getRecipients();
        Iterator    it = c.iterator();

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyData);
        KeyFactory          keyFact = KeyFactory.getInstance("RSA", "BC");
        Key                 priKey = keyFact.generatePrivate(keySpec);
        byte[]              data = Hex.decode("57616c6c6157616c6c6157617368696e67746f6e");

        while (it.hasNext())
        {
            RecipientInformation   recipient = (RecipientInformation)it.next();

            assertEquals(recipient.getKeyEncryptionAlgOID(), PKCSObjectIdentifiers.rsaEncryption.getId());
            
            CMSTypedStream recData = recipient.getContentStream(priKey, "BC");
            
            assertEquals(true, Arrays.equals(data, CMSTestUtil.streamToByteArray(recData.getContentStream())));
        }
    }
    
    public void testKeyTransAES128()
        throws Exception
    {
        byte[]          data     = "WallaWallaWashington".getBytes();
        
        CMSEnvelopedDataStreamGenerator edGen = new CMSEnvelopedDataStreamGenerator();

        edGen.addKeyTransRecipient(_reciCert);

        ByteArrayOutputStream  bOut = new ByteArrayOutputStream();
        
        OutputStream out = edGen.open(
                                bOut, CMSEnvelopedDataGenerator.AES128_CBC, "BC");

        out.write(data);
        
        out.close();
        
        CMSEnvelopedDataParser     ep = new CMSEnvelopedDataParser(bOut.toByteArray());

        RecipientInformationStore  recipients = ep.getRecipientInfos();

        assertEquals(ep.getEncryptionAlgOID(), CMSEnvelopedDataGenerator.AES128_CBC);
        
        Collection  c = recipients.getRecipients();
        Iterator    it = c.iterator();
        
        while (it.hasNext())
        {
            RecipientInformation   recipient = (RecipientInformation)it.next();

            assertEquals(recipient.getKeyEncryptionAlgOID(), PKCSObjectIdentifiers.rsaEncryption.getId());
            
            CMSTypedStream recData = recipient.getContentStream(_reciKP.getPrivate(), "BC");
            
            assertEquals(true, Arrays.equals(data, CMSTestUtil.streamToByteArray(recData.getContentStream())));
        }
    }
    
    public void testAESKEK()
        throws Exception
    {
        byte[]    data = "WallaWallaWashington".getBytes();
        SecretKey kek  = CMSTestUtil.makeAES192Key();
        
        CMSEnvelopedDataStreamGenerator edGen = new CMSEnvelopedDataStreamGenerator();

        byte[]  kekId = new byte[] { 1, 2, 3, 4, 5 };

        edGen.addKEKRecipient(kek, kekId);

        ByteArrayOutputStream  bOut = new ByteArrayOutputStream();
        
        OutputStream out = edGen.open(
                                bOut,
                                CMSEnvelopedDataGenerator.DES_EDE3_CBC, "BC");
        out.write(data);
        
        out.close();
         
        CMSEnvelopedDataParser     ep = new CMSEnvelopedDataParser(bOut.toByteArray());
        
        RecipientInformationStore  recipients = ep.getRecipientInfos();

        assertEquals(ep.getEncryptionAlgOID(), CMSEnvelopedDataGenerator.DES_EDE3_CBC);
        
        Collection  c = recipients.getRecipients();
        Iterator    it = c.iterator();

        while (it.hasNext())
        {
            RecipientInformation   recipient = (RecipientInformation)it.next();

            assertEquals(recipient.getKeyEncryptionAlgOID(), "2.16.840.1.101.3.4.1.25");
            
            CMSTypedStream recData = recipient.getContentStream(kek, "BC");
            
            assertEquals(true, Arrays.equals(data, CMSTestUtil.streamToByteArray(recData.getContentStream())));
        }
    }
    
    public static Test suite()
        throws Exception
    {
        init();
        
        return new CMSTestSetup(new TestSuite(EnvelopedDataStreamTest.class));
    }
}
