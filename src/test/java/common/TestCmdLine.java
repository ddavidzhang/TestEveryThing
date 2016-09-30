package common;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import java.util.Properties;

/**
 * Created by zhangjw on 6/20/16.
 */
public class TestCmdLine {
    public static void main(String[] args) {
        args = new String[]{"-n localhost"};
        Options options = ServerUtil.buildCommandlineOptions(new Options());
        ServerUtil.printCommandLineHelp("broker",options);
        CommandLine commandLine = ServerUtil.parseCmdLine("broker", args, options, new PosixParser());
        Properties properties = ServerUtil.commandLine2Properties(commandLine);
        System.out.println(properties);
    }

    public static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("c", "configFile", true, "Broker config properties file");
        opt.setRequired(false);

        options.addOption(opt);
        opt = new Option("p", "printConfigItem", false, "Print all config item");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("m", "printImportantConfig", false, "Print important config item");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }
}
