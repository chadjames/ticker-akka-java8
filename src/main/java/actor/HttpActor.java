package actor;

import akka.actor.UntypedActor;
import messages.JsonMsg;
import messages.TickerCounterMsg;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class HttpActor extends UntypedActor {
    final String yahooUrl = "http://finance.yahoo.com/webservice/v1/symbols/";
    private TickerCounterMsg currentMsg = null;

    private String passedSymbol;


    public String getPassedSymbol() {
        return passedSymbol;
    }

    public void setPassedSymbol(String symbol) {
        this.passedSymbol = symbol;
    }

    @Override
    public void preStart(){
        if(currentMsg != null){
            reprocessMsg();
        }
    }

    private void reprocessMsg(){
        getSelf().tell(currentMsg, getSender());
    }

    @Override
    public void onReceive(Object o) throws Exception {

        if(o instanceof TickerCounterMsg){
            TickerCounterMsg msg = (TickerCounterMsg) o;

            this.currentMsg = msg;
            this.passedSymbol = msg.getSymbol();

            String constructedUrl = buildUrl(msg.getSymbol());
            String result;
            try {
               if (msg.getCount() != 0 && msg.getCount() % 20  == 0){
                   throw new ArithmeticException(msg.getSymbol());
               }else{
                   result = retreiveData(constructedUrl);
               }
            }catch (Exception e){
                throw e;
            }

            if(result != null){
                JsonMsg jsonMsg = new JsonMsg(result);
                jsonMsg.setPassedSymbol(msg.getSymbol());
                getSender().tell(jsonMsg,self());
                this.currentMsg = null;
            }

        }
    }

    private String buildUrl(String string){

       String url = yahooUrl + string + "/quote?format=json&view=detail";
        return url;
    }

    private String retreiveData(String url) throws Exception{
        Thread.sleep(new Random().nextInt(((1000 - 100) + 1) + 100));
//        StringBuffer buffer = new StringBuffer();
//            URL tickerURL = new URL(url);
//            URLConnection tickerConnection = tickerURL.openConnection();
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    tickerConnection.getInputStream()));
//            String inputLine;
//            while ((inputLine = in.readLine()) != null)
//                buffer.append(inputLine);
//            in.close();
       String content = new String(Files.readAllBytes(Paths.get("/tmp","data1.txt")));



        return content;
    }
}
