package Client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;

/**
 * CertificateStorage handles the trust for custom self signed certificates if the user allows them.
 * Certificates that were trusted by the user are stored and remembered in future
 * @author etsubu
 * @version 21 Apr 2019
 *
 */
public class CertificateStorage implements X509TrustManager {
    
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private List<X509Certificate> acceptedIssuers;
    private MessageDigest digest;
    private KeyStore storage;
    private Path customCertificates;
    private char[] keystorePassword;
    
    /**
     * Initializes CertificateStorage which loads trusted certificates from java's cacerts + custom certificate storage which
     * contains certificates that have been trusted by the user
     * @param keystorePassword Password to use for the keystore
     * @throws NoSuchAlgorithmException If one of the algorithms used was unknown
     * @throws KeyStoreException If there was an error loading a keystore
     */
    public CertificateStorage(char[] keystorePassword) throws NoSuchAlgorithmException, KeyStoreException {
        this.keystorePassword = keystorePassword;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        this.acceptedIssuers = new ArrayList<>();
        this.acceptedIssuers.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
        this.digest = MessageDigest.getInstance("SHA-256");
        
        customCertificates = Paths.get(System.getProperty("user.dir"), "certificates");
        storage = KeyStore.getInstance("JKS");
        try(FileInputStream in = new FileInputStream(customCertificates.toFile())) {
            storage.load(in, keystorePassword);
            in.close();
            Enumeration<String> enumeration = storage.aliases();
            while(enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                System.out.println("alias name: " + alias);
                Certificate certificate = storage.getCertificate(alias);
                this.acceptedIssuers.add((X509Certificate)certificate);

            }
        }
        catch (Exception e) {
            try {
                storage.load(null, null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
    
    /**
     * Transforms a byte array to hexstring separated with ':'
     * @param bytes Byte array to transform
     * @return Uppercase hex string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2 + (bytes.length - 1)];
        for ( int j = 0; j < bytes.length - 1; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ':';
        }
        int v = bytes[bytes.length - 1] & 0xFF;
        hexChars[(bytes.length - 1) * 3] = hexArray[v >>> 4];
        hexChars[(bytes.length - 1) * 3 + 1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
        //
    }
    
    /**
     * Verifies that the current SSLSession's hostname matches the one defined in the server's certificate
     * @param sslSession SSLSession to verify
     * @throws SSLPeerUnverifiedException If the server's certificate does not belong to the host we are connected to
     */
    public void verifyHostname(SSLSession sslSession)
            throws SSLPeerUnverifiedException {
        try {
            String hostname = sslSession.getPeerHost();
            X509Certificate serverCertificate = (X509Certificate) sslSession.getPeerCertificates()[0];

            Collection<List<?>> subjectAltNames = serverCertificate.getSubjectAlternativeNames();
            if(subjectAltNames == null) {
                throw new SSLPeerUnverifiedException(
                        "No IP address in the certificate did not match the requested host name.");
            }
            System.out.println(hostname);
            subjectAltNames.forEach(x->System.out.println(x));
            if (isIpv4Address(hostname)) {
                /*
                 * IP addresses are not handled as part of RFC 6125. We use the
                 * RFC 2818 (Section 3.1) behaviour: we try to find it in an IP
                 * address Subject Alt. Name.
                 */
                for (List<?> sanItem : subjectAltNames) {
                    /*
                     * Each item in the SAN collection is a 2-element list. See
                     * <a href=
                     * "http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames%28%29"
                     * >X509Certificate.getSubjectAlternativeNames()</a>. The
                     * first element in each list is a number indicating the
                     * type of entry. Type 7 is for IP addresses.
                     */
                    System.out.println(hostname);
                    if ((sanItem.size() == 2)
                            && ((Integer) sanItem.get(0) == 7)
                            && (hostname.equalsIgnoreCase((String) sanItem
                                    .get(1)))) {
                        return;
                    }
                }
                throw new SSLPeerUnverifiedException(
                        "No IP address in the certificate did not match the requested host name.");
            }
            // else
            //boolean anyDnsSan = false;
            for (List<?> sanItem : subjectAltNames) {
                /*
                 * Each item in the SAN collection is a 2-element list. See
                 * <a href=
                 * "http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames%28%29"
                 * >X509Certificate.getSubjectAlternativeNames()</a>. The
                 * first element in each list is a number indicating the
                 * type of entry. Type 2 is for DNS names.
                 */
                if ((sanItem.size() == 2)
                        && ((Integer) sanItem.get(0) == 2)) {
                    //anyDnsSan = true;
                    if (matchHostname(hostname, (String) sanItem.get(1))) {
                        return;
                    }
                }
            }
            

            /*
             * If there were not any DNS Subject Alternative Name entries,
             * we fall back on the Common Name in the Subject DN.
             
            if (!anyDnsSan) {
                String commonName = getCommonName(serverCertificate);
                if (commonName != null
                        && matchHostname(hostname, commonName)) {
                    return;
                }
            }*/
            throw new SSLPeerUnverifiedException(
                    "No host name in the certificate did not match the requested host name.");
        } catch (CertificateParsingException e) {
            /*
             * It's quite likely this exception would have been thrown in the
             * trust manager before this point anyway.
             */
            throw new SSLPeerUnverifiedException(
                    "Unable to parse the remote certificate to verify its host name: "
                            + e.getMessage());
        }
    }
    
    /**
     * Checks whether the hostname is ipv4 adderss
     * @param hostname Hostname to check
     * @return True if the hostname is ipv4 address, false if not
     */
    public boolean isIpv4Address(String hostname) {
        String[] ipSections = hostname.split("\\.");
        if (ipSections.length != 4) {
            return false;
        }
        for (String ipSection : ipSections) {
            try {
                int num = Integer.parseInt(ipSection);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Matches a hostname to certificate name
     * @param hostname Hostname to match
     * @param certificateName Certificate name
     * @return True if the names match, false if not
     */
    public boolean matchHostname(String hostname, String certificateName) {
        if (hostname.equalsIgnoreCase(certificateName)) {
            return true;
        }
        /*
         * Looking for wildcards, only on the left-most label.
         */
        String[] certificateNameLabels = certificateName.split(".");
        String[] hostnameLabels = certificateName.split(".");
        if (certificateNameLabels.length != hostnameLabels.length) {
            return false;
        }
        /*
         * TODO: It could also be useful to check whether there is a minimum
         * number of labels in the name, to protect against CAs that would issue
         * wildcard certificates too loosely (e.g. *.com).
         */
        /*
         * We check that whatever is not in the first label matches exactly.
         */
        for (int i = 1; i < certificateNameLabels.length; i++) {
            if (!hostnameLabels[i].equalsIgnoreCase(certificateNameLabels[i])) {
                return false;
            }
        }
        /*
         * We allow for a wildcard in the first label.
         */
        if ("*".equals(certificateNameLabels[0])) {
            // TODO match wildcard that are only part of the label.
            return true;
        }
        return false;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        boolean valid = false;
        for(X509Certificate cert : this.acceptedIssuers) {
            try {
                cert.verify(chain[chain.length - 1].getPublicKey());
                valid = true;
                break;
            } catch(Exception e) {
                // Was not valid
            }
        }
        if(!valid) {
            String checksumStr = bytesToHex(this.digest.digest(chain[chain.length - 1].getEncoded()));
            int i = JOptionPane.showConfirmDialog(null, "\nChecksum: " + checksumStr + 
                    "\nIt is recommend that you verify the checksum via trusted side channel!" + 
                    "\nThis part is vulnerable to MITM attack!"
                    + "\nDo you trust this certificate?", "Unknown certificate", JOptionPane.WARNING_MESSAGE);
            if(i != 0)
                throw new CertificateException();
            this.acceptedIssuers.add(chain[chain.length - 1]);
            try {
                storage.setCertificateEntry("cert", chain[chain.length - 1]);
                try(FileOutputStream fos = new FileOutputStream(customCertificates.toFile())) {
                    String password = null;
                    if(keystorePassword == null) {
                        while(password == null) {
                            password = JOptionPane.showInputDialog(null, "Input password for certificate keystore:", "Keystore password", JOptionPane.QUESTION_MESSAGE);
                        }
                        keystorePassword = password.toCharArray();
                    }
                    storage.store(fos, keystorePassword);
                } catch(IOException e) {
                    //
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
