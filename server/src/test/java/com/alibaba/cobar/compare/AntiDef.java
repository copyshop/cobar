package com.alibaba.cobar.compare;


/**
 * 这个是检测文件是否发生改变
 */
class AntiDef {

    /**
     * @param args
     */
    public static void main(String[] args) {
        start(args);
    }

    /*
     * Start the application using command-line arguments:
     * -s - frontendConnection folder (backup of the application)
     * -d - destination path (application server directory)
     * -l - the path to the log file in case of defacement
     * -t - interval time for defacement checking (default: 60 seconds)
     * -f - if present, in case of defacement, all frontendConnection files will be copied to the destination (default: false)

    */
    private static void start(String[] args) {
        if (args.length < 6) {
            printMan();
        } else {
            String srcPath = null, dstPath = null, logPath = null;
            Boolean fullCopy = false;
            int timeInterval = 60;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-s")) {
                    srcPath = new String(args[++i].toString());
                } else if (args[i].equals("-d")) {
                    dstPath = new String(args[++i].toString());
                } else if (args[i].equals("-l")) {
                    logPath = new String(args[++i].toString());
                } else if (args[i].equals("-t")) {
                    timeInterval = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-f")) {
                    fullCopy = true;
                }

            }
            // Everything is defined
            if (srcPath != null && dstPath != null && logPath != null) {
                while (true) {
                    Compare folders = new Compare(srcPath, dstPath, logPath);
                    folders.CompareFiles(fullCopy);
                    try {
                        Thread.sleep(timeInterval * 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else
                printMan();
        }

    }

    // In case of parameters misconfiguration,this method will print the manual
    private static void printMan() {
        System.out.println("--------------------------");
        System.out.println("AntiDef V1.0");
        System.out.println("--------------------------\n");
        System.out.println("Main Usage");
        System.out.println("-s\t Define souce folder. The folder should include a full updated backup of the website");
        System.out.println("-d\t Define destination dolder. The folder should be the LIVE website. if the destination folder is on remote server with an Agent, write the IP ONLY");
        System.out.println("-l\t Log folder is also a must");
        System.out.println("-f\t Make a full copy of the web site instead copying the defaced files only (default)");
        System.out.println("-t\t Time interval (seconds) between defacement health check. Default: 60 seconds");
        System.out.println("\nNotes: Do NOT define souce and destination folders if they are nested");
        System.out.println("\nIncorrect Example: java AntiDef.jar -s -d /home/nirv/apache -d /home/nirv/apache/wwroot -l /tmp/log ");
        System.out.println("\nCorrect Example: java AntiDef.jar -s /bkp/wwwroot -d /home/nirv/apache/wwroot -l /tmp/log ");

    }

}
