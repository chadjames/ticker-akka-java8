package messages;

import java.io.Serializable;

/**
 * Created by cjames on 8/2/2015.
 */
public class JsonMsg implements Serializable {
    String json;

    private String passedSymbol;


    public String getPassedSymbol() {
        return passedSymbol;
    }

    public void setPassedSymbol(String symbol) {
        this.passedSymbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    String symbol;

    public JsonMsg(String json) {
        this.json = json;
    }
    public String getJson() {
        return json;
    }
}
