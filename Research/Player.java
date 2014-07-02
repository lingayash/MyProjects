import java.net.*;
import java.util.Iterator;
import java.util.Random;

public class Player {
	
	Queue<double[]> memory;
	Queue<double[]> SecondMemory;
	Queue<DatagramPacket> batch;
	double lastAction;
	double lastResult;
	int mSize;
	int wSize;
	int mSample;
	int randPercent;
	double g;
	//AIMD Variables
	int a;
	double b;
	boolean loss = false;

	//constructs a queue to serve as memory for actions taken in previous rounds
	public Player(){
		memory = new Queue<double[]>();
		SecondMemory = new Queue<double[]>();
		batch = new Queue<DatagramPacket>();
		lastAction = 0;
		lastResult = 0;
	}
	
	//creates and returns a udp packet with random payload
	public DatagramPacket createPacket(){
		byte[] sendData = new byte[8];
		Random rand = new Random();
		rand.nextBytes(sendData);
		InetAddress IPAddress = null;
		try {
			IPAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
	    
	    return sendPacket;
	}
	
	//uses imitation dynamics to return an integer to specify the number of packets to send
	//@return number of packet to send the next round
	public double packetCountID(){
		Random rand = new Random();
		if(!memory.isEmpty()){
			Iterator<double[]> itr = memory.iterator();
			double[][] temp = new double[memory.size()][2];
			//temp is used to copy the memory from queue to an array
			//it's much easier to deal with an array
			double[][] store;
			//store - keeps the averages of the results
			double action;
			double result;
			int i=0;
			while(itr.hasNext()){
				temp[i] = itr.next();
				i++;
			}
			int count;
			if(temp.length<=this.mSample){
				store = new double[memory.size()][2];
			//	System.out.println("Less than sample size!");
				for(int j=0;j<temp.length;j++){
					action = temp[j][0];
					count=0;
					result = 0;
					for(int k=0;k<memory.size();k++){
						if(action==temp[k][0]){
							count++;
							result+=temp[k][1];
						}
					}
					double aveResult = result/count;
					store[j][0]=action;
					store[j][1]=aveResult;
				}
			}
			else{
				store = new double[this.mSample][2];
			//	System.out.println("More than sample size!");
				double[][] mem = new double[this.mSample][2];	//used to sample memory
				Random ran = new Random();
				int s;
				for(int k=0;k<this.mSample;k++){
					s = ran.nextInt(temp.length);
					mem[k]=temp[s];
				}
				for(int j=0;j<mem.length;j++){
					action = mem[j][0];
					//System.out.println("Out, "+temp[j][0]+" "+temp[j][1]);
					count=0;
					result = 0;
					for(int k=0;k<mem.length;k++){
						if(action==mem[k][0]){
							count++;
							result+=mem[k][1];
						}
					}
					double aveResult = result/count;
					store[j][0]=action;
					store[j][1]=aveResult;
				}
			}
			double max = store[0][1];
			double nextMove=-1;
			for(int m=0;m<store.length;m++){
				//System.out.println("In the Store "+store[m][0] +", "+ store[m][1]);
				if(store[m][1]>=max){
					max=store[m][1];
					nextMove = store[m][0];
				}
			}
			//System.out.println("----------------");
			this.lastAction = nextMove;
			return nextMove;
		}
		else{
			//System.out.println("-------+--------");
			double r = (double)rand.nextInt(this.wSize+1);
			this.lastAction = r;
			return r;
		}
	}
	
	public double packetCountAIMD(){
		int nextMove;
		if(!memory.isEmpty()){
			if(loss){
				if(this.lastAction>1){
					nextMove = (int)(this.lastAction*b);
					this.lastAction=nextMove;
					return nextMove;
				}
				else{
					return 1;
				}
			}
			else{
				nextMove=(int)this.lastAction+a;
				this.lastAction=nextMove;
				return nextMove;
			}
		}
		else{
			Random rand = new Random();
			//System.out.println("-------+--------");
			double r = (double)rand.nextInt(this.wSize+1);
			this.lastAction = r;
			return r;
		}
	}
	
	//using imitation dynamics and randomness this function creates a batch of packets to be sent
	public void createBatchID(){
		Random rand = new Random();
		int r = rand.nextInt(100);
		int p;
		if(r<this.randPercent){
			p = rand.nextInt(this.wSize+1);
			this.lastAction = p;
			for(int i=0;i<p;i++){
				this.batch.enqueue(createPacket());
			}
		}
		else{
			p = (int)packetCountID();
		//	System.out.println(p);
			for(int i=0;i<p;i++){
				this.batch.enqueue(createPacket());
			}
		}
	}
	public void createBatchAIMD(){
		int p;
		p = (int)packetCountAIMD();
		//	System.out.println(p);
		for(int i=0;i<p;i++){
			this.batch.enqueue(createPacket());
		}
	}
	
	public void getResultID(){
		double act = this.lastAction;
		double fail;
		double payOff;
		double leftOver = (double)this.batch.size();
		double[] push = {0,0};
		if(this.memory.size()>=mSize){
			this.memory.dequeue();
		}
		if(this.SecondMemory.size()>=1000){
			this.SecondMemory.dequeue();
		}
		if(act!=0){
			double success = ((act-leftOver)/act)*100;
			fail = 100 - success;
			payOff = act*success - g*act*fail;
			//System.out.println(act+", "+leftOver+", "+success+", "+payOff);
			this.lastResult = payOff;
			push[0] = this.lastAction;
			push[1] = this.lastResult;
			this.memory.enqueue(push);
			this.SecondMemory.enqueue(push);
		}
		else{
			this.memory.enqueue(push);
			this.SecondMemory.enqueue(push);
		}
		
	}
	public void getResultAIMD(){
		double act = this.lastAction;
		double fail;
		double payOff;
		double leftOver = (double)this.batch.size();
		if(leftOver>0){
			this.loss=true;
		}
		else{
			this.loss=false;
		}
		double[] push = {0,0};
		
		if(this.memory.size()>=mSize){
			this.memory.dequeue();
		}
		if(this.SecondMemory.size()>=1000){
			this.SecondMemory.dequeue();
		}
		if(act!=0){
			double success = ((act-leftOver)/act)*100;
			fail = 100 - success;
			payOff = act*success - g*act*fail;
			//System.out.println(act+", "+leftOver+", "+success+", "+payOff);
			this.lastResult = payOff;
			push[0] = this.lastAction;
			push[1] = this.lastResult;
			this.memory.enqueue(push);
			this.SecondMemory.enqueue(push);
		}
		else{
			this.memory.enqueue(push);
			this.SecondMemory.enqueue(push);
		}
	}
	
	
	public void emptyBatch(){
		while(!this.batch.isEmpty()){
			this.batch.dequeue();
		}
	}
	
}
