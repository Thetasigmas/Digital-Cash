import java.io.*;
import java.util.*;

public class Bank {
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
	 */
public static void main(String []args){
	Scanner keyIn = new Scanner(System.in);
	boolean cheat = false;
	double amount = 0;
	int option ;
	
	switch(option){
	
	case 1: 
	case 2:
	case 3:
	case 4:
	}
	System.out.println("Enter the directory containing the unsigned money orders: ");
	String dir = keyIn.next();
	for(int i = 1; i<101; i++){
		File fileName = new File(dir+"MO-"+i);
		try{
			Scanner fin = new Scanner(new FileReader(fileName));
			fin.nextLine();
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
			System.out.println("File not Found");
		}
		if(cheat == true){
			System.out.println("Money Order amounts inconsistent!");
				System.exit(0);
			}
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
		public static void verifyMO(Scanner fin, String dir){
			double amount = 0;
			for(int i = 1; i<101; i++){
				File fileName = new File(dir+"MO-"+i);
			try{
				fin.nextLine();
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
				System.out.println("File not Found");
				
			}
		}
}
