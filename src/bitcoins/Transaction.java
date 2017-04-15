package bitcoins;

/**
 *
 * @author Samuel Pelegrinello Caipers
 * Sistemas Distribuidos - Tarefa 01
 * 
 */
public class Transaction {
    private long id;
    private int bid; // buyer ID
    private int sid; // seller ID
    private int coinAmount;
    private String status; // status of transaction
    
    public Transaction(long id, int bid, int sid, int coinAmount) {
        this.id         = id;
        this.bid        = bid;
        this.sid        = sid;
        this.coinAmount = coinAmount;
    }
    
    // C   --> Confirmed
    // NC  --> Not Confirmed
    // I   --> Invalid
    public boolean setStatus(String status) {
        if (status.equals("C") || status.equals("NC") || status.equals("I")) {
            this.status = status;
            return true;
        } else {
            return false;
        }
    }
    
    public long getId() {
        return id;
    }
    
    public int getBid()  {
        return bid;
    }
    
    public int getSid() {
        return sid;
    }
    
    public int getCoinAmount() {
        return coinAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void printTransaction() {
        System.out.print("Transaction ID: " + id);
        System.out.print(", Buyer ID: " + bid);
        System.out.print(", Seller ID: " + sid);
        System.out.print(", Coin Amount: " + coinAmount);
        System.out.println(", Status: " + status);
    }
}
