package test.za.co.discoverylife.desktop.util;

import test.BaseTestCase;
import za.co.discoverylife.desktop.util.Encryption;


public class TestEncryption extends BaseTestCase {
  //  encode with default SALT, but encryption key is class name 'Encryption'
  Encryption codec;
  String ORIGINAL = "31415926535";
  String ENCODED = "ryUfYCl4Bx7prStnnSvWFQ==";

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    codec = new Encryption();
    codec.setEncryptionKey(Encryption.class.getSimpleName());// 'pi2Fs6OOyGRRjKHKc8gqAQ=='
  }
	
	public void testEncrypt() {
    try
    {
      codec.resetSalt();
      String test = codec.encrypt(ORIGINAL);
      wr(ORIGINAL + "' => encrypt => '" + test + "'");
      assertTrue("Encoding",test.compareTo(ENCODED)== 0 );
    }
    catch (Exception e)
    {
      fail("Failed encoding "+e.getMessage());
    }
	}
  
  public void testDecrypt() {
    try
    {
      codec.resetSalt();
      String test = codec.decrypt(ENCODED);
      wr( ENCODED + "' => decrypt => '" + test + "'");
      assertTrue("Decoding",test.compareTo(ORIGINAL)== 0 );
    }
    catch (Exception e)
    {
      fail("Failed decoding "+e.getMessage());
    }
  }
  
  public void testUserAuthenticate() {
    try
    {
      String saltKey = codec.generateRandomSalt();
      String encryptedPW = codec.getEncryptedPassword("password");
      wr("RandomSaltKey='" + saltKey+"' encrypts 'password' as '"+ encryptedPW+"'");
      assertTrue("Authenticate ok", codec.authenticate("password", encryptedPW));
      assertTrue("Authenticate invalid", ! codec.authenticate("p@ssword", encryptedPW));
      assertTrue("GetSalt", saltKey.compareTo(codec.getSalt())==0);
    }
    catch (Exception e)
    {
      fail("Failed authentication "+e.getMessage());
    }
  }

}
