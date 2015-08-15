package actor;

import akka.actor.*;
import akka.actor.SupervisorStrategy;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import static akka.actor.SupervisorStrategy.escalate;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;
import messages.*;
import scala.concurrent.duration.Duration;

import java.time.Instant;



/**
 * Created by cjames on 8/2/2015.
 */
public class MasterActor extends AbstractActor {
    static int responses;
    Instant start;


    ActorRef httpActor =
            getContext().actorOf(new RoundRobinPool(1).props(Props.create(HttpActor.class)),
                    "httpActor");

    ActorRef jsonActor = getContext().actorOf(Props.create(JsonActor.class), "jsonActor");
    ActorRef formattorActor = getContext().actorOf(Props.create(FormattorActor.class), "formattorActor");


    public MasterActor() {
        receive(ReceiveBuilder.
                match(SymbolMsg.class, msg ->
                {
                    start = Instant.now();
                    SymbolMsg message = msg;
                    responses = message.getSymbolList().size();
                    int count = 1;
                    for (String ticker : message.getSymbolList()) {
                        TickerCounterMsg t = new TickerCounterMsg(count, ticker);
                        t.setSymbol(ticker);
                        t.setCount(count);
                        httpActor.tell(t, self());
                        count++;
                    }
                }).
                match(JsonMsg.class, msg ->
                                jsonActor.tell(msg, self())

                ).
                match(PriceMsg.class, msg ->
                            formattorActor.tell(msg, self())
                ).
                match(FormattedMsg.class, msg ->
                        {
                            responses--;
                            System.out.println(msg.getFormattedResult());
                            if (responses == 0) {
                                shutdown("Formatted");
                            }
                        }

                )
                .build());
    }


    private static SupervisorStrategy strategy = new OneForOneStrategy(10,
            Duration.create("1 minute"),
            exception -> {
                if (exception instanceof ArithmeticException) {
                    responses--;
                    return resume();
                } else if (exception instanceof NullPointerException) {
                    return restart();
                } else if (exception instanceof IllegalArgumentException) {
                    //stop
                    return stop();
                } else {
                    return escalate();
                }
            });


    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }


//    @Override
//    public void onReceive(Object o) throws Exception {
//        if(o instanceof SymbolMsg){
//            start = Instant.now();
//            SymbolMsg message = (SymbolMsg) o;
//            responses = message.getSymbolList().size();
//            int count = 1;
//            for(String ticker: message.getSymbolList()){
//                TickerCounterMsg t = new TickerCounterMsg(count, ticker);
//                t.setSymbol(ticker);
//                t.setCount(count);
//                httpActor.tell(t, self());
//                count++;
//            }
//        } else if(o instanceof JsonMsg){
//            jsonActor.tell(o, self());
//        }else if(o instanceof PriceMsg){
//            formattorActor.tell(o, self());
//        } else if(o instanceof FormattedMsg){
//            responses--;
//            System.out.println(((FormattedMsg) o).getFormattedResult());
//            if(responses == 0){
//                shutdown("Formatted");
//            }
//        } else
//            unhandled(0);
//        }

    private void shutdown(String s){
        Instant end =  Instant.now();
        System.out.println("Total time : " + java.time.Duration.between(end,start) + " " + " Shutdown by " + s );
        getContext().system().shutdown();

    }
}
