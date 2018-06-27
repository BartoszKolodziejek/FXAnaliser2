package com.FxAnaliser.FxAnaliser;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;
import org.rosuda.JRI.Rengine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.WebQuerySender;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RequestController {

    private RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    private ScriptEngine engine = factory.getScriptEngine();

    @Autowired
    Statistician statistician;
    @Value("${R.Log}")
    private String RLog;




    @RequestMapping(value = "prognose_variance_and_expected_payoff", method = RequestMethod.GET)
    public Map<String, String> prognoseVarianceAndExpectedPayOff(@RequestParam String strategy, @RequestParam String symbol, @RequestParam String toDate ){

        Map<String, String> params = new HashMap<>();
        params.put("strategy", strategy);
        params.put("symbol", symbol);
        params.put("todate", toDate);
        Map<String, String> results = new HashMap<>();
        try {
            Map<Integer, String> dataset = new ObjectMapper().readValue(WebQuerySender.getInstance().getJson("http://localhost:8090", params, "getdata").toString(), HashMap.class);
            List<Double> returns = new ArrayList<>();
            dataset.values().forEach(r -> returns.add((new BigDecimal(r)).doubleValue()));
            Double[] set = new Double[returns.size()];
            returns.toArray(set);
            double expected_payoff;
            double variance;
           if (statistician.isStationary(set)){
                expected_payoff = statistician.prognoseLogReturn(set);
        }
           else {
               expected_payoff = statistician.prognoseLogReturn(set);
           }
           if (statistician.isArch(set)){
               variance = statistician.progoseVariance(set);
               if (variance <= 0){
                   engine.put("dataset", set);
                   Double var = ((SEXP) engine.eval("var <- var(dataset);")).asReal();
                   variance = var.doubleValue();
               }

           }
           else {
               engine.put("dataset", set);
               Double var = ((SEXP) engine.eval("var <- var(dataset);")).asReal();
               variance = var.doubleValue();
           }

           Map<String, String> mean_and_var = getMeanAndVariance(strategy, symbol, toDate);
            if(expected_payoff!=0)
            results.put("e_payoff", Double.toString(expected_payoff));
           else
                results.put("e_payoff", mean_and_var.get("mean"));
            if (variance!=0)
            results.put("var", Double.toString(variance));
            else
                results.put("e_payoff", mean_and_var.get("var"));

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        return results;
    }

    @RequestMapping(value = "/calculate_mean_and_variance", method = RequestMethod.GET)
    public Map<String, String> getMeanAndVariance(@RequestParam String strategy, @RequestParam String symbol, @RequestParam String  toDate){
        Map<String, String> params = new HashMap<>();
        params.put("strategy", strategy);
        params.put("symbol", symbol);
        params.put("todate", toDate);
        Map<String, String> results = new HashMap<>();
        try {
            Map<Integer, String> dataset = new ObjectMapper().readValue(WebQuerySender.getInstance().getJson("http://localhost:8090", params, "getdata").toString(), HashMap.class);

            List<Double> returns = new ArrayList<>();
            dataset.values().forEach(r -> returns.add((new BigDecimal(r)).doubleValue()));
            Double[] set = new Double[returns.size()];
            returns.toArray(set);
            engine.put("dataset",set);
            Double mean = ((SEXP) engine.eval("mean <- mean(dataset);")).asReal();
            Double var = ((SEXP) engine.eval("var <- var(dataset);")).asReal();
            results.put("mean", mean.toString());
            results.put("var", var.toString());



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        return results;
    }

   @RequestMapping(value = "/getaccountresult", method = RequestMethod.GET)
   public Map<String, String> getAccountResult(@RequestParam String before, @RequestParam String status ){
       Map<String, String> results = new HashMap<>();
       results.put("balance", new BigDecimal(before).add(new BigDecimal(status)).toString());
       BigDecimal after = new BigDecimal(before).add(new BigDecimal(status));
       if(after.compareTo(new BigDecimal(0))>0)
       results.put("effect", Double.toString(Math.log(after.divide(new BigDecimal(before), 5, RoundingMode.HALF_DOWN).doubleValue())));
       else  results.put("effect", Double.toString(Math.log((new BigDecimal(0.1)).divide(new BigDecimal(before), 5, RoundingMode.HALF_DOWN).doubleValue())));
       return results;
   }

    @RequestMapping(value = "/calculate_safelevel", method = RequestMethod.GET)
    public Map<String,String> getSafeLevel(@RequestParam String size, @RequestParam String rate, @RequestParam String lavarage){
        Map<String,String> sl = new HashMap<>();
        sl.put("result", ((new BigDecimal(size)).multiply(new BigDecimal(rate))).divide(new BigDecimal(lavarage), 5, RoundingMode.HALF_DOWN).toString());
        return sl;
    }

    @RequestMapping(value = "/calculate_point", method = RequestMethod.GET)
    public Map<String, String> getPoint(@RequestParam String size, @RequestParam String rate, @RequestParam String step){
        Map<String,String> result = new HashMap<>();
        BigDecimal point = ((new BigDecimal(size)).multiply(new BigDecimal(rate))).multiply(new BigDecimal(step));
        result.put("point", point.toString());
        return result;
    }



    @RequestMapping(value = "/get_result", method = RequestMethod.GET)
    public Map<String, String> getResult(@RequestParam String open, @RequestParam String close, @RequestParam String type, @RequestParam String point,  @RequestParam String step){
        Map<String, String> result = new HashMap<>();
        if(type.equals("BUY"))
        result.put("result", (new BigDecimal(close).subtract(new BigDecimal(open))).multiply(new BigDecimal(point)).divide(new BigDecimal(step), RoundingMode.HALF_DOWN).toString());
        if(type.equals("SELL"))
            result.put("result", (new BigDecimal(open).subtract(new BigDecimal(close))).multiply(new BigDecimal(point)).divide(new BigDecimal(step), RoundingMode.HALF_DOWN).toString());

        return result;
    }

}
