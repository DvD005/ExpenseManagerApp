package Model;

public class Data {
    private int amt;
    private String type;
    private String note;
    private String id;
    private String date;

    public Data(){

    }
    public Data(int amt, String type, String note, String id, String date) {
        this.amt = amt;
        this.type = type;
        this.note = note;
        this.id = id;
        this.date = date;
    }

    public int getAmt() {
        return amt;
    }

    public void setAmt(int amt) {
        this.amt = amt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
