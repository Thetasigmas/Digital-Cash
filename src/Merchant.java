import java.util.Random;
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
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String [] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		Scanner keyboard = new Scanner(System.in);
		Scanner fin = null;
		File keyFile = new File("./Merchant/public.key");
		if(!keyFile.exists())
			setKeys();
			
		//Prompt user for money order file from customer
		System.out.println("Merchant Digital Cash program");
		try{
			System.out.print("Enter the name of the money order file:");
			String moFile = keyboard.next();
			File file = new File(moFile);
			if(!file.exists())
				file.createNewFile();
			fin = new Scanner(new FileReader(moFile));
			Random rand = new Random(42000);
			int selectorString = rand.nextInt()*10;
			String selectorStringBin = Integer.toBinaryString(selectorString);
			BufferedWriter bw = new BufferedWriter(new FileWriter(("./SelectorString/"+moFile), true));
			bw.write(selectorStringBin);
			bw.close();
			
		}catch(FileNotFoundException e){
			System.out.println("Sorry, file not found");
		}finally{
			fin.close();
		}
		//Generate random integer to customer to determine halves of ID string to unblind
		
		
		//Output the signed, unblinded money order with ID halves for the bank

	}
	//Method to generate RSA key pair
	//In practice a higher key length than 2048 would increase confidentiality of the money orders, 
	//however it would be time consuming to decrypt in use.
	private static void setKeys() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException{
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
		writeKeyPublic("./Merchant/public.key", pub.getModulus(),pub.getPublicExponent());
		writeKey("./Merchant/private.key", priv.getModulus(),priv.getPrivateExponent());
	}
	//Writes private and public key to file for future reference.
	//Modulus and Exponent values will be used to retrieve the keys,
	//It is assumed that the key values are stored securely
	public static void writeKey(String fileName,BigInteger modulus, BigInteger exponent) throws IOException {
		ObjectOutputStream privateDB = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		ObjectOutputStream publicDB = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./PublicKeyDB/MerchantPublic")));
		try {
			privateDB.writeObject(modulus);
			privateDB.writeObject(exponent);
		} catch (Exception e) {
			throw new IOException("File output error", e);
		} finally {
			privateDB.close();
			publicDB.close();
		}
	}
	//Similar method to writeKey, but the public key is written to a public database instead
	public static void writeKeyPublic(String fileName,BigInteger modulus, BigInteger exponent) throws IOException {
		ObjectOutputStream publicDB = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./PublicKeyDB/MerchantPublic")));
		try {
			publicDB.writeObject(modulus);
			publicDB.writeObject(exponent);
		} catch (Exception e) {
			throw new IOException("File output error", e);
		} finally {
			publicDB.close();
		}
	}
	PublicKey readPublicKey(String keyFileName) throws IOException {
		InputStream in = new FileInputStream(keyFileName);
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
		try {
			BigInteger mod = (BigInteger) oin.readObject();
			BigInteger exp = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
			return pubKey;
		} catch (Exception e) {
			throw new RuntimeException("Error encountered", e);
		} finally {
			oin.close();
		}
	}
	PrivateKey readPrivateKey(String keyFileName) throws IOException {
		InputStream in = new FileInputStream(keyFileName);
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
		try {
			BigInteger mod = (BigInteger) oin.readObject();
			BigInteger exp = (BigInteger) oin.readObject();
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod, exp);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(keySpec);
			return privKey;
		} catch (Exception e) {
			throw new RuntimeException("Error encountered", e);
		} finally {
			oin.close();
		}
	}
}
