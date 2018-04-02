package com.FxAnaliser.FxAnaliser;



import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileWriter;
import java.io.IOException;


@Component
public class Statistician {

    private RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    private ScriptEngine engine = factory.getScriptEngine();

    public Statistician(){
        try {
            engine.getContext().setWriter(new FileWriter("logs/RLog.log", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public boolean isStationary(Double[] data){
        return !ADFTest(data) && KPSSTest(data);
    }


    private boolean ADFTest(Double[] data){
        try {
            engine.eval("library(aTSA);");
            engine.put("x", data);
            double[] adfResult = ((DoubleArrayVector)((ListVector) engine.eval("adf.test(x)")).get(0)).toDoubleArray();
            return adfResult[4] > adfResult[8]  && adfResult[5] > adfResult[9] &&  adfResult[6] > adfResult[10]  && adfResult[7] > adfResult[11];
        } catch (ScriptException e) {
            e.printStackTrace();
            return false;
        }


    }


    private boolean KPSSTest(Double[] data){
        try {

            engine.eval("library(aTSA);");
            engine.put("x", data);
           double[] kpssResults = ((DoubleArrayVector) engine.eval("kpss.test(x, FALSE, TRUE)")).toDoubleArray();

            return kpssResults[3]<kpssResults[6] && kpssResults[4]<kpssResults[7] && kpssResults[5]<kpssResults[8];

        } catch (ScriptException e) {
            e.printStackTrace();
            return false;
        }


    }
}
