import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
//import java.util.concurrent.TimeUnit;
import java.io.*;

public class Router {
	
	Queue<DatagramPacket> queue;
	ArrayList<InetAddress> rTable;

	public Router(){
		queue = new Queue<DatagramPacket>();
		rTable = new ArrayList<InetAddress>(4);
		//also create interfaces for sending packets out
	}
	
	public void processPacket(){
		if(!this.queue.isEmpty()){
			DatagramPacket packet = this.queue.dequeue();
			InetAddress address = packet.getAddress();
			for(InetAddress ad: this.rTable){
				if(ad.equals(address)){
					//do nothing
				}
			}
		}
	}
	
	public void emptyQueue(){
		while(!this.queue.isEmpty()){
			this.queue.dequeue();
		}
	}
	
	
	public static void main(String [] args){
		
		//parsing the arguments
		
		int rounds = Integer.parseInt(args[0]);
		int p = Integer.parseInt(args[1]);
		int qSize = Integer.parseInt(args[2]);
		int window = Integer.parseInt(args[3]);
		int memSize = Integer.parseInt(args[4]);
		int memSample = Integer.parseInt(args[5]);
		int rPercent = Integer.parseInt(args[6]);
		double gValue = Double.parseDouble(args[7]);
		
		int add = Integer.parseInt(args[8]);
		double mult = Double.parseDouble(args[9]);
		//Variable used to determine whether we have AIMD or ID players
		int playerCombo = Integer.parseInt(args[10]);
		
		Router router = new Router();
		Random rand = new Random();
		int r;
		
		Player[] players = new Player[p];
		for(int i=0;i<p;i++){
			players[i]= new Player();
			players[i].wSize = window;
			players[i].mSize = memSize;
			players[i].mSample = memSample;
			players[i].randPercent = rPercent;
			players[i].a = add;
			players[i].b = mult;
			players[i].g = gValue;
		}

		int[] bigW = new int[rounds];
		int[] batchSelect = new int[p];
		//this loop determines the number of rounds
		for(int i=0;i<rounds;i++){
			//create a batch of packets for each player before each round
			int totalWindow = 0;
			if(playerCombo==-1){
				for(int j=0;j<p;j++){
					players[j].createBatchAIMD();
					totalWindow += players[j].batch.size();
					batchSelect[j]=totalWindow;
				}
			}
			else if(playerCombo==1){
				for(int j=0;j<p;j++){
					players[j].createBatchID();
					totalWindow += players[j].batch.size();
					batchSelect[j]=totalWindow;
				}
			}
			else{
				for(int j=0;j<(p/2);j++){
					players[j].createBatchID();
					totalWindow += players[j].batch.size();
					batchSelect[j]=totalWindow;
				}
				for(int j=(p/2);j<p;j++){
					//System.out.println(j+", "+p);
					players[j].createBatchAIMD();
					totalWindow += players[j].batch.size();
					batchSelect[j]=totalWindow;
				}
			}
			
			bigW[i] = totalWindow;
			//System.out.println(totalWindow);
			//this iteration chooses 100 packets to fill the router queue
			for (int k=0;k<qSize;k++) {
				r = rand.nextInt(totalWindow);
				for(int s=0;s<p;s++){
					if(r<=batchSelect[s]){
						r = s;
						break;
					}
				}
				if(!players[r].batch.isEmpty()){
					router.queue.enqueue(players[r].batch.dequeue());
				}
			}
			
			if(playerCombo==-1){
				for(int j=0;j<p;j++){
					players[j].getResultAIMD();
				}
			}
			else if(playerCombo==1){
				for(int j=0;j<p;j++){
					players[j].getResultID();
				}
			}
			else{
				for(int j=0;j<(p/2);j++){
					players[j].getResultID();
				}
				for(int j=(p/2);j<p;j++){
					players[j].getResultAIMD();
				}
			}
			//empty all the batches before next round.
			for(int m=0;m<p;m++){
				players[m].emptyBatch();
			}
			while(!router.queue.isEmpty()){
				router.queue.dequeue();
			}
		}
		//int[] temp = new int[2];
		double[] temp2 = new double[2];
		Iterator<double[]> itr;
		Iterator<double[]> itr2;
		//String used to store all parameters used in the experiment
		String params ="";
		
		if(playerCombo==-1){
			params = "Rounds: "+rounds+"\nPlayers: "+p+"\nQueue Size: "+qSize+"\n";
			params+= "Window Size: "+window+"\nMemory Size: "+memSize+"\nG Value: "+gValue+"\n";
			params+= "Add: "+add+"\nMultiply: "+mult+"\n\n";
		}
		else if(playerCombo==1){
			params = "Rounds: "+rounds+"\nPlayers: "+p+"\nQueue Size: "+qSize+"\n";
			params +="Window Size: "+window+"\nMemory Size: "+memSize+"\nMemory Sample: "+memSample+"\n";
			params +="Randomnes: "+rPercent+"\nG Value: "+gValue+"\n\n";
		}
		else{
			params = "Rounds: "+rounds+"\nPlayers: "+p+"\nQueue Size: "+qSize+"\n";
			params +="Window Size: "+window+"\nMemory Size: "+memSize+"\nMemory Sample: "+memSample+"\n";
			params +="Randomnes: "+rPercent+"\nG Value: "+gValue+"\n";
			params+= "Add: "+add+"\nMultiply: "+mult+"\n\n";
		}
		
		try {
			File file1 =new File("Actions.txt");
			File file2 =new File("PayOffs.txt");
			File file3 =new File("Window.txt");
			if(!file1.exists()){
    			file1.createNewFile();
    		}
			if(!file2.exists()){
    			file2.createNewFile();
    		}
			if(!file3.exists()){
    			file3.createNewFile();
    		}
			
			
			BufferedWriter out = new BufferedWriter(new FileWriter(file1.getName(),true));
			BufferedWriter out2 = new BufferedWriter(new FileWriter(file2.getName(),true));
			BufferedWriter out3 = new BufferedWriter(new FileWriter(file3.getName(),true));

			out.write(params);
			out2.write(params);
			out3.write(params);
			
			for(int j=0;j<bigW.length;j++){
				out3.write(bigW[j]+"\n");
			}
			out3.close();
			
			for(int i=0;i<p;i++){
				itr=players[i].memory.iterator();
				while(itr.hasNext()){
					temp2 = itr.next();
					temp2[1] = Math.round(temp2[1]*100);		//rounding the payOff to 2 decimals
					temp2[1] = temp2[1]/100;
					out.write(temp2[0]+", ");
					out2.write(temp2[1]+", ");
				}
				out.write("\n");
				out2.write("\n");
			}
			out.write("\n");
			out2.write("\n");
			for(int i=0;i<p;i++){
				itr2=players[i].SecondMemory.iterator();
				while(itr2.hasNext()){
					temp2 = itr2.next();
					temp2[1] = Math.round(temp2[1]*100);		//rounding the payOff to 2 decimals
					temp2[1] = temp2[1]/100;
					out.write(temp2[0]+", ");
					out2.write(temp2[1]+", ");
				}
			out.write("\n");
			out2.write("\n");
			}
			out.write("\n");
			out2.write("\n");
			out.close();
			out2.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Finish");
	}
}
