# JavaChat
Simple TCP chat with IRC style channels
![alt text](https://raw.githubusercontent.com/etsubu/JavaChat/master/Sample.PNG)

The chat now uses TLSv1.2 for communications. The default trusted root certificates can be extended if the user accepts custom certificate when connecting 
to a server with self-signed certificate

![alt text](https://raw.githubusercontent.com/etsubu/JavaChat/master/unknownCert.jpg)
The hostname in the certificate needs to match the host address that the client is connecting.
![alt text](https://raw.githubusercontent.com/etsubu/JavaChat/master/invalidCert.jpg)