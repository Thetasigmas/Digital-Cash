import java.util.*;
import java.io.*;
import java.nio.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;

public class Customer {
	private static Key publicKey; // object for the public key
	private static Key privateKey; // object for the private key

	public static void main(String[] args) {
		Scanner kb = new Scanner(System.in);
		Random random = new Random();
		PrintWriter fout = null;
		int mod = 0;
		int k1 = 0;
		try {
			fout = new PrintWriter(new FileOutputStream("./PublicKeyDB/PublicModulus"));
			mod = random.nextInt();
			fout.print(mod);
			fout.close();
			fout = new PrintWriter(new FileOutputStream("./Customer/k"));
			k1 = random.nextInt((mod - 1) + 1) + 1; // mod > k1 > 1
			fout.close();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		//Prompt user for number of orders and amount of money
		System.out.print("Number of orders: ");
		int numOrders = kb.nextInt();
		System.out.print("Amount of money: ");
		double amtMoney = kb.nextDouble();
		int pubInt = 0;
		try {
			setKeys();
		}catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}try {
			PublicKey pub = readPublicKey("./Customer/public.key");
			byte[] pubBytes = pub.getEncoded();
			pubInt = ByteBuffer.wrap(pubBytes).getInt();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//Creating random numbers and identity strings (Secret splitting)
		//This runtime... Please... Send help...
		for(int k=0;k<numOrders;k++) {
			long ident = Math.abs(random.nextLong());
			int[][] identStrings = new int[numOrders][numOrders];
			int[][] randBits = new int[numOrders][numOrders];
			int[][] left = new int[numOrders][numOrders];
			int[][] right = new int[numOrders][numOrders];

			for (int i = 0; i < numOrders; i++)
				for (int j = 0; j < numOrders; j++) {
					randBits[i][j] = randomBits();
				}
			for (int i = 0; i < numOrders; i++)
				for (int j = 0; j < numOrders; j++) {
					identStrings[i][j] = iStringGen(numOrders, amtMoney, randBits[i][j]);
				}

			for (int i = 0; i < numOrders; i++)
				for (int j = 0; j < numOrders; j++) {
					left[i][j] = encrypt(randBits[i][j]);

				}
			for (int i = 0; i < numOrders; i++)
				for (int j = 0; j < numOrders; j++) {
					right[i][j] = encrypt(identStrings[i][j]);
					right[i][j] = right[i][j] * (int) Math.pow(k1,pubInt) % mod;
				}

			for (int i = 0; i < numOrders; i++)
				for (int j = 0; j < numOrders; j++) {
					identStrings[i][j] = iStringGen(numOrders, amtMoney, randBits[i][j]);
				}
			try {
				fout = new PrintWriter(new FileOutputStream("./MO-unsigned/MO-"+k));
				fout.println(ident);
				fout.println(amtMoney);
				int n = 1;
				for (int i = 0; i < numOrders; i++)
					for (int j = 0; j < numOrders; j++) {
						fout.println(left[i][j] + "," + right[i][j]);
						n++;
					}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				fout.close();
			}
		}
		kb.close();
	}

	public static int iStringGen(int x, double y, int z) {
		int s = 0;
		s = z ^ (int) y;
		return s;
	}

	public static int randomBits() {
		Random random = new Random();
		byte[] ra = new byte[30];
		random.nextBytes(ra);
		int z = ByteBuffer.wrap(ra).getInt();
		return z;
	}

	// Method to generate RSA key pair
	// In practice a higher key length than 2048 would increase confidentiality
	// of the money orders,
	// however it would be time consuming to decrypt in use.
	private static void setKeys() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		// Create a private and public RSA key pair with a key size of 2048 bits
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(2048);
		KeyPair keyPair = keyPairGen.genKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();

		// Retrieve modulus and exponent of keys to store the keys for reference
		KeyFactory keyFact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = keyFact.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class); // RSA public key object
		RSAPrivateKeySpec priv = keyFact.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class); // RSA private key object
		// Sends the file name, modulus, and exponents to file for future
		// storage,
		// such that the Merchant can run the program multiple times once a key
		// pair has been generated
		writeKey("./Customer/public.key", pub.getModulus(), pub.getPublicExponent());
		writeKeyPublic("./Customer/public.key", pub.getModulus(), pub.getPublicExponent());
		writeKey("./Customer/private.key", priv.getModulus(), priv.getPrivateExponent());
	}

	// Writes private and public key to file for future reference.
	// Modulus and Exponent values will be used to retrieve the keys,
	// It is assumed that the key values are stored securely
	public static void writeKey(String fileName, BigInteger modulus, BigInteger exponent) throws IOException {
		ObjectOutputStream privateDB = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		ObjectOutputStream publicDB = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream("./PublicKeyDB/CustomerPublic")));
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

	// Similar method to writeKey, but the public key is written to a public
	// database instead
	public static void writeKeyPublic(String fileName, BigInteger modulus, BigInteger exponent) throws IOException {
		ObjectOutputStream publicDB = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream("./PublicKeyDB/CustomerPublic")));
		try {
			publicDB.writeObject(modulus);
			publicDB.writeObject(exponent);
		} catch (Exception e) {
			throw new IOException("File output error", e);
		} finally {
			publicDB.close();
		}
	}

	static PublicKey readPublicKey(String keyFileName) throws IOException {
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

	static PrivateKey readPrivateKey(String keyFileName) throws IOException {
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
	//Source: https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
	public static int encrypt(int text) {
		byte[] cipherText = null;
		try {
			PublicKey key = readPublicKey("./Customer/public.key");
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA");
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(ByteBuffer.allocate(4).putInt(text).array());
		} catch (Exception e) {
			e.printStackTrace();
		}
		int cipherInt = ByteBuffer.wrap(cipherText).getInt();
		return cipherInt;
	}

	/**
	 * Unblinds a money order matching given orderNum
	 * @param orderNum
	 * @param priv
     */
	public void unblind(int orderNum, PrivateKey priv){
		Scanner fin = null;
		String identString = "";
		PrintWriter fout = null;
		int c = 0;
		int privInt = 0;
		try {
			fin = new Scanner(new FileInputStream("./MO-unsigned/MO-" + orderNum));
			PublicKey pub = readPublicKey("./Customer/public.key");
			byte[] privBytes = pub.getEncoded();
			privInt = ByteBuffer.wrap(privBytes).getInt();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		String unique = fin.nextLine();
		String amt = fin.nextLine();
		while(fin.hasNextLine()){
			identString += fin.nextLine();
			c++;
		}
		String[] identStringArray = identString.split(",");
		fin.close();
		int[][] identInt = new int[c][2];
		for(int i=0;i<c;i+=2){
			identInt[i][0] = Integer.parseInt(identStringArray[i]);
			identInt[i][1] = Integer.parseInt(identStringArray[i+1]);
		}
		try {
			fout = new PrintWriter(new FileOutputStream("./MO-unblinded/MO-" + orderNum));
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		fout.println(unique + "\n" + amt);
		for(int i =0; i<c; i+=2){
			fout.print(identStringArray[i] + "," + identStringArray[i+1]);
		}
		fout.close();
	}
}
