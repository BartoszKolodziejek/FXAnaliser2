package com.FxAnaliser.FxAnaliser;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

@Component
public class RMainLoopCallback  implements RMainLoopCallbacks {

    org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RMainLoopCallback.class);
    @Value("${R.Log}")
    private String RLog;



    @Override
    public void rWriteConsole(Rengine rengine, String s, int i) {
        logger.info(s);
        try {FileWriter fileWriter = new FileWriter(System.getProperty("user.dir")+"/logs/RLog.log", true);
            fileWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void rBusy(Rengine rengine, int i) {
        logger.info("rBusy: "+i);
        try {FileWriter fileWriter = new FileWriter("logs/RLog.log", true);
            fileWriter.write("rBusy: "+i);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String rReadConsole(Rengine rengine, String s, int i) {
        logger.info(s);
        try {FileWriter fileWriter = new FileWriter("logs/RLog.log", true);
            fileWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public void rShowMessage(Rengine rengine, String s) {
        logger.info(s);
        try {
            FileWriter fileWriter = new FileWriter("logs/RLog.log", true);
            fileWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String rChooseFile(Rengine rengine, int i) {
        return null;
    }

    @Override
    public void rFlushConsole(Rengine rengine) {

    }

    @Override
    public void rSaveHistory(Rengine rengine, String s) {

    }

    @Override
    public void rLoadHistory(Rengine rengine, String s) {

    }
}
