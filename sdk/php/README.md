## Changes to php.ini
There have been some changes made in the 'php.ini' file. This file is used to configure the PHP environment and the changes made to it are described below.

The following changes have been made to the php.ini file:

* Uncomment: extension=curl
* Uncomment: extension=openssl
* Uncomment: extension_dir = "c:\php\ext"
* Uncomment: extension=gd


**cacert.pem** is a file containing trusted root certificates that are used for verifying SSL/TLS connections. When a client makes an HTTPS request to a server, it needs to verify that the server's SSL/TLS certificate is valid and has been issued by a trusted certificate authority (CA).
```
[curl]
; A default value for the CURLOPT_CAINFO option. This is required to be an
; absolute path.
curl.cainfo = "C:\php\cacert.pem"
```

```
[openssl]
; The location of a Certificate Authority (CA) file on the local filesystem
; to use when verifying the identity of SSL/TLS peers. Most users should
; not specify a value for this directive as PHP will attempt to use the
; OS-managed cert stores in its absence. If specified, this value may still
; be overridden on a per-stream basis via the "cafile" SSL stream context
; option.
openssl.cafile="C:\php\cacert.pem"
```

The **cacert.pem** file is often used in PHP projects to provide a set of trusted root certificates that can be used by the curl library (which is commonly used for making HTTPS requests). 

```
[gd]
(Graphics Draw) extension is a PHP extension that provides a set of functions for image manipulation and image generation. It allows you to work with different types of image formats, such as JPEG, PNG, GIF and many others. Commonly used GD functions such as imagecreatefromjpeg(), imagecreatefrompng(), imagecreatefromgif(), and imagejpeg()
```
