import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

public class Bank {
	static PublicKey publicKey;
	static PrivateKey privateKey;
	/**
Input by Customer:
N blinded text files representing the money order
Pseudo random integers to be used to unblind and reveal the identity strings from the N-1 money order files

Output for Customer:
Signed blinded money order file

Input by Merchant:
Signed unblinded money order file with the halves of identity strings revealed by Customer based on Merchant’s specified bit string

Output for Merchant:
Verification of signature Validation of money order
	 * @throws FileNotFoundException 
	 */
public static void main(String []args) throws FileNotFoundException{
	Scanner keyIn = new Scanner(System.in);
	double amount = 0;
	int option ;
	String dir = "";
	int blindMO = 0;
	
	System.out.println("Bank program:");
	printMenu();
	System.out.print("\nChoose an option: ");
	option = keyIn.nextInt();
	switch(option){
	
	case 1: //Generate random number to indicate which money order not to unblind
		Random rand = new Random();
		blindMO = rand.nextInt(10);
		File blindFile = new File("Blind/blindFile.txt");
		writeBlindInt(blindFile, blindMO);
		
	case 2:	//Check unblinded Money orders for inconsistent amounts
		boolean cheat = false;
		boolean unique = true;
		long tmp = 0;
		for(int i = 0; i<10; i++){
			File fileName = new File("./MO-unsigned/"+"MO-"+i);
			try{
				Scanner fin = new Scanner(new FileReader(fileName));
				long UID = fin.nextLong();
				if(UID == tmp)
					System.out.println("Duplicate UID detected");
				tmp = UID;
				if(amount == 0){
					amount = fin.nextDouble();
					continue;
				}
				double amountTemp = fin.nextDouble();
				if (amountTemp != amount){
					cheat = true;
					break;
				}

			}catch(FileNotFoundException e){
				continue;
			}
		}
			if(cheat == true){
				System.out.println("Money Order amounts inconsistent!");
					System.exit(0);
				}else
					System.out.println("Money Order amounts consistent");
			break;
			
	case 3://Test for blind signature
		FileInputStream fin = new FileInputStream("./MO-unsigned/MO-"+blindMO);
		fin.
	case 4:
	}
	}
		public static void printMenu(){
			System.out.println("1: Generate random integer for customer");
			System.out.println("2: Verify the unblinded Money Orders");
			System.out.println("3: Sign blind Money Order");
			System.out.println("4: Check if Money order ID has been used");
	}
		public static int randGen(){
			Random rand = new Random();
			return rand.nextInt(42000);
		}
		
		//Method to write the random int to file for the customer not to reveal
		private static  void writeBlindInt(File fout,int blind){
			try {
				FileOutputStream blindFile = new FileOutputStream(fout);
				blindFile.write(blind);
				blindFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
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