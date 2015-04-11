package za.co.discoverylife.desktop.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Provides a number of encode/decode encrypt/decrypt functions.
 * 
 * @author Anton Schoultz - 2015
 *
 */
public class Encryption
{
  private byte[] salt = new byte[8];
  private SecretKey key = null;

  /** 
   * CONSTRUCTOR - default constructor uses a default SALT value 
   * <br>no key is set yet - see setEncryptionKey()
   */
  public Encryption() throws Exception
  {
    resetSalt();// sets a default SALT value
  }

  /**
   * CONSTRUCTOR which accepts a key to be used for encrypt/decrypt functions.
   * <br>(will use a default SALT value)
   * @param encryptionKey key to use for encrypt/decrypt
   * @throws Exception
   */
  public Encryption(String encryptionKey) throws Exception
  {
    resetSalt();
    setEncryptionKey(encryptionKey);
  }

  /**
   * Sets the key to be used for encrypt/decrypt functions
   * @param encryptionKey String key to be used for encrypt/decrypt functions
   * @throws Exception 
   */
  public void setEncryptionKey(String encryptionKey) throws Exception
  {
    try
    {
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      key = keyFactory.generateSecret(new PBEKeySpec(encryptionKey.toCharArray()));
    }
    catch (Exception e)
    {
      throw new Exception("Problem unlocking the codec : " + e.getMessage());
    }
  }

  /**
   * Encrypts the provided value using the current key and SLAT.
   * @param value String value to be encrypted
   * @return encrypted string
   * @throws Exception
   */
  public String encrypt(String value) throws Exception
  {
    if ( key == null )
    {
      throw new Exception("CodecUtil must be initialised with the storeKey first.");
    }
    try
    {
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
      pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, 20));
      return base64Encode(pbeCipher.doFinal(value.getBytes("UTF-8")));
    }
    catch (Exception e)
    {
      throw new Exception("Encryption error :" + e.getMessage());
    }
  }

  /**
   * Decrypts the provided string, using the current key and SALT.
   * 
   * @param encryptedString the encrypted string to be decrypted.
   * @return decrypted string
   * @throws Exception
   */
  public String decrypt(String encryptedString) throws Exception
  {
    if ( key == null )
    {
      throw new Exception("CodecUtil must be initialised with the storeKey first.");
    }
    try
    {
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
      pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(salt, 20));
      return new String(pbeCipher.doFinal(base64Decode(encryptedString)), "UTF-8");
    }
    catch (Exception e)
    {
      throw new Exception("Decryption error :" + e.getMessage());
    }
  }

  /**
   * Returns the Base64 encoded string for the bytes that make up the provided value
   * @param property String value to encode
   * @return string of base64encoded( input.bytes(UTF8) );
   * @throws IOException
   */
  public static String base64EncodeString(String input) throws Exception
  {
    return base64Encode(input.getBytes("UTF-8"));
  }

  /**
   * Returns the Base64 encoded string for the bytes provided.
   * @param bytes byte[] of data to be base64 encoded into string
   * @return base 64 encoded string
   */
  public static String base64Encode(byte[] bytes)
  {
    return DatatypeConverter.printBase64Binary(bytes);
  }

  /**
   * Decodes the provided base64 string into bytes, then returns
   * a new string from these bytes.
   * 
   * @param value Base64 encoded string to be decoded
   * @return decoded string value
   * @throws Exception
   */
  public static String base64DecodeString(String value) throws Exception
  {
    return new String(DatatypeConverter.parseBase64Binary(value), "UTF-8");
  }

  /**
   * Returns the Base64 encoded string for the bytes that make up the provided value
   * @param property String value to encode
   * @return base64( 
   * @throws IOException
   */
  public static byte[] base64Decode(String property) throws Exception
  {
    return DatatypeConverter.parseBase64Binary( property);
  }

  /**
   * Checks to see if the provided password matches the one in the encrypted string.
   * <br>These are done using the current SALT value.
   * 
   * @param attemptedPassword Plain text of the password to be tested
   * @param encryptedPasswordString encrypted string of the stored password
   * @return
   * @throws Exception
   */
  public boolean authenticate(String attemptedPassword, String encryptedPasswordString)
      throws Exception
  {
    // Encrypt the clear-text password using the same salt that was used to
    // encrypt the original password
    String encryptedAttemptedPasswordString = getEncryptedPassword(attemptedPassword);
    // Authentication succeeds if encrypted password that the user entered
    // is equal to the stored hash
    return encryptedPasswordString.compareTo(encryptedAttemptedPasswordString) == 0;
  }

  /**
   * Encrypts the provided password using the current SALT value.
   * 
   * @param password String to be encrypted
   * @return String of encrypted data (base64encoded)
   * @throws Exception
   */
  public String getEncryptedPassword(String password) throws Exception
  {
    // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
    // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
    String algorithm = "PBKDF2WithHmacSHA1";
    // SHA-1 generates 160 bit hashes, so that's what makes sense here
    int derivedKeyLength = 160;
    // Pick an iteration count that works for you. 
    // The NIST recommends at least 1,000 iterations:
    // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
    int iterations = 3145;
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);
    SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
    return base64Encode(f.generateSecret(spec).getEncoded());
  }

  /**
   * Generates random data which is then used for the SALT.
   * @return the new SALT value as a BASE64Encoded String.
   * @throws NoSuchAlgorithmException
   */
  public String generateRandomSalt() throws NoSuchAlgorithmException
  {
    // VERY important to use SecureRandom instead of just Random
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
    salt = new byte[8];
    random.nextBytes(salt);
    return getSalt();
  }

  /**
   * Returns the current SALT value as a BASE64Encoded String.
   * @return the current SALT value as a BASE64Encoded String.
   */
  public String getSalt()
  {
    return base64Encode(salt);
  }

  /**
   * Sets SALT from the provided base64Encoded string
   * 
   * @param saltString Base64Encoded byte[8] array of data
   * @throws IOException
   */
  public void setSalt(String saltString) throws Exception
  {
    salt = base64Decode(saltString);
  }

  /**
   * Resets SALT to a default value
   * <br>0xde, 0x33,  0x10,  0x12, 0xde, 0x33,  0x10, 0x12
   * @throws IOException
   */
  public void resetSalt() throws Exception
  {
    setSalt("3jMQEt4zEBI=");// default SALT value
    //   0xde, 0x33,  0x10,  0x12, 0xde, 0x33,  0x10, 0x12,
  }

  /**
   * Sets the encryption key based on the provided class.
   * <br>Uses the full class name.
   * @param k
   * @throws Exception
   */
  public void setEncryptionKey(Class<?> k) throws Exception
  {
    setEncryptionKey(k.getName());
  }

}
