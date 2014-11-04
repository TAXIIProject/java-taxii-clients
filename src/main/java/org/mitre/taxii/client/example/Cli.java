/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.taxii.client.example;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author jasenj1
 */
public class Cli {
    private final Options options = new Options();

    private CommandLine cmd;
    
    public Cli() {        
        // Options common to all client apps.
        options.addOption("h", "help", false, "show help.");
//        options.addOption("v", "verbose", false, "Turn on verbose mode.");
        Option urlOption = new Option("u", "url", true, "REQUIRED. The URL of the TAXII service provider to connect to. (e.g. http://taxiitest.mitre.org:80/services/discovery/");
        urlOption.setRequired(true);
        options.addOption(urlOption);
        
// TODO: Certificates and Key files is a rather rich and complicated subject. 
// The python library supports a very specific implementation. Eventually support whatever the Python library supports.        
//        options.addOption("c", "cert", true, "The file location of the certificate to use.");
//        options.addOption("k", "key", true, "The file location of the private key to use.");
        options.addOption("username", true, "The username to authenticate with.");
        options.addOption("pass", "password", true, "The password to authenticate with." );
        options.addOption("proxy", true, "A proxy to use (e.g., http://example.com:80/). Omit this to use the system proxy.");
        options.addOption("x","xmloutput", false, "If present, the raw XML of the response will be printed to standard out. Otherwise, a \"Rich\" output will be presented.");
    }
    
    public void parse(String[] args) {        
        CommandLineParser parser = new BasicParser();        
        this.cmd = null;        
        try {
            this.cmd = parser.parse(options, args);
        } catch (ParseException pe) {
            help();
        }        
    }
    
    private void help() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("TAXII Client", options); //TODO: Populate the command being executed.
        System.exit(0);
    }

    //========== Getters & Setters ============
    public Options getOptions() {
        return options;
    }

    public CommandLine getCmd() {
        return cmd;
    }
    
}
