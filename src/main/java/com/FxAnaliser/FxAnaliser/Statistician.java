package com.FxAnaliser.FxAnaliser;



import org.apache.commons.lang3.ArrayUtils;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.Rengine;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
public class Statistician {
    private Rengine rengine = new Rengine(new String[] {}, false, new RMainLoopCallback());

    private RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    private ScriptEngine engine = factory.getScriptEngine();

    public Statistician(){
        try {
            engine.getContext().setWriter(new FileWriter("logs/RLog.log", true));
            rengine.eval("install.packages(\"forecast\")");
            rengine.eval("install.packages(\"aTSA\")");
            rengine.eval("install.packages(\"rugarch\")");
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

    public double progoseVariance(Double[] data){


        rengine.assign("y", ArrayUtils.toPrimitive(data));
        rengine.eval("library(rugarch)");
        rengine.eval("library(forecast)");
        rengine.assign("y", ArrayUtils.toPrimitive(data));
        REXP model = rengine.eval("m1.mean.model <- auto.arima(y);");
        rengine.eval("ar.comp <- arimaorder(m1.mean.model)[1];");
        rengine.eval("ma.comp <- arimaorder(m1.mean.model)[3];");
        rengine.eval("modelgarchfit <- ugarchfit(data=y, spec=ugarchspec(mean.model=list(armaOrder=c(ar.comp,ma.comp)), variance.model=list(garchOrder=c(1,1)), distribution.model = \"std\";),  solver = 'hybrid' );");
        rengine.eval("print(ugarchfit(data=y, spec=ugarchspec(mean.model=list(armaOrder=c(ar.comp,ma.comp)), variance.model=list(garchOrder=c(1,1)), distribution.model = \"std\";),  solver = 'hybrid' ))");
        REXP prognose = rengine.eval("ugarchforecast(ugarchfit(data=y, spec=ugarchspec(mean.model=list(armaOrder=c(ar.comp,ma.comp)), variance.model=list(garchOrder=c(1,1)), distribution.model = \"std\";),  solver = 'hybrid' ), data = NULL, n.ahead = 1, n.roll = length(y), out.sample = length(y));");
        try{
        return prognose.asDouble();}
        catch (Exception e){
            return -1;
        }
    }

    public boolean isArch(Double[] data){
        rengine.eval("library(forecast)");
        rengine.assign("y", ArrayUtils.toPrimitive(data));
        REXP model = rengine.eval("model <- forecast::auto.arima(y)");
        int[] arimaParams =model.asVector().at(6).asIntArray();
        arimaParams = Arrays.copyOfRange(arimaParams, 0, 3);
        rengine.assign("params", arimaParams);
        rengine.eval("library(aTSA)");
        REXP result = rengine.eval("arch.test(arima(y,order = params))");

        try{
            double[] testsValues = result.asDoubleArray();
        return testsValues[18] > testsValues[24] || testsValues[19] > testsValues[25] || testsValues[20] > testsValues[26] ||  testsValues[21] > testsValues[27] || testsValues[22] > testsValues[28] || testsValues[23] > testsValues[29];}
        catch (Exception e){
        return false;}
    }

    public double prognoseLogReturn(Double[] data){
        rengine.eval("library(forecast)");
        rengine.assign("y", ArrayUtils.toPrimitive(data));
        rengine.eval("model <- forecast::auto.arima(y)");
        rengine.eval("print(model)");
        REXP prognose =  rengine.eval("forecast::forecast(model, 1)");
        rengine.eval("print(forecast::forecast(model, 1))");
        return prognose.asVector().at(3).asDouble();
    }
    private boolean ADFTest(Double[] data){
        try {
            rengine.eval("library(aTSA);");
            rengine.assign("x",  ArrayUtils.toPrimitive(data));
            double[] adfResult =  rengine.eval("adf.test(x)").asVector().at(0).asDoubleArray();
            return adfResult[7] > adfResult[14];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }


    private boolean KPSSTest(Double[] data){
        try {

            rengine.eval("library(aTSA);");
            rengine.assign("x", ArrayUtils.toPrimitive(data));
           double[] kpssResults =  rengine.eval("kpss.test(x, FALSE, TRUE)").asDoubleArray();

            return kpssResults[3]>kpssResults[6] && kpssResults[4]>kpssResults[7] && kpssResults[5]>kpssResults[8];

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
}
