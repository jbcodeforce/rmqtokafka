package ibm.gse.eda.poc.rmq;

public class PerformanceProducer {

 public static void main(String[] args) {
     parseArgs(args);
 }

 private static void parseArgs(String[] args) {
     if (args.length <= 1) {
         System.err.println("Usage PerformanceProducer config.properties");
     }
 }
}