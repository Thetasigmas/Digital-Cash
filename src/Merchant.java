import java.util.Scanner;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author Devin Bauer
 * 	Self-Contained Merchant Digital Cash program
 * @throws FileNotFoundException 
 * @throws NoSuchAlgorithmException 
 */

public class Merchant {
	private static Key publicKey;	//object for the public key
	private static Key privateKey;	//object for the private key
/**
 * 
 * Input by Customer:
 * Signed unblinded money order file
 * Pseudo random integers to be used to reveal the halves of identity strings specified by the merchant
 * 
 * Output for Bank:
 * Signed unblinded money order file with the halves of identity strings revealed by Customer based on Merchant’s specified bit string
 */
	public static void main(String [] args) throws FileNotFoundException{
		Scanner keyboard = new Scanner(System.in);
		Scanner fin;
		
		System.out.println("Merchant Digital Cash program");
		System.out.print("Enter the name of the money order file:");
		String moFile = keyboard.next();
		fin = new Scanner(new FileReader(moFile));

	}
	//Method to generate RSA key pair
	//In practice a higher key length than 2048 would increase confidentiality of the money orders, 
	//however it would be time consuming to decrypt in use.
	private void setKeys() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException{
		//Create a private and public RSA key pair with a key size of 2048 bits
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(2048);
		KeyPair keyPair = keyPairGen.genKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		
		//Retrieve modulus and exponent of keys to store the keys for reference
		KeyFactory keyFact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = keyFact.getKeySpec(keyPair.getPublic(),
		  RSAPublicKeySpec.class);	//RSA public key object
		RSAPrivateKeySpec priv = keyFact.getKeySpec(keyPair.getPrivate(),
		  RSAPrivateKeySpec.class); //RSA private key object
		
		//Sends the file name, modulus, and exponents to file for future storage,
		//such that the Merchant can run the program multiple times once a key pair has been generated
		writeKey("./Merchant/public.key", pub.getModulus(),pub.getPublicExponent());
		writeKey("./Merchant/private.key", priv.getModulus(),priv.getPrivateExponent());
	}
	//Writes private and public key to file for future reference.
	//Modulus and Exponent values will be used to retrieve the keys,
	//It is assumed that the key values are stored securely
	public void writeKey(String fileName,BigInteger modulus, BigInteger exponent) throws IOException {
			  ObjectOutputStream privateDB = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			  ObjectOutputStream publicDB = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./PublicKeyDB/MerchantPublic")));
			  try {
				  privateDB.writeObject(modulus);
				  privateDB.writeObject(exponent);
				  publicDB.writeObject(modulus);
				  publicDB.writeObject(exponent);
			  } catch (Exception e) {
			    throw new IOException("File output error", e);
			  } finally {
				  privateDB.close();
				  publicDB.close();
			  }
			}
	private static void getKeys(){
		
	}
}
