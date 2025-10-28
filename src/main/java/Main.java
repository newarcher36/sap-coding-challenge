import com.sap_coding_challenge.co2.cli.Co2CalculatorCommand;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        int exit = new CommandLine(new Co2CalculatorCommand()).execute(args);
        System.exit(exit);
    }
}