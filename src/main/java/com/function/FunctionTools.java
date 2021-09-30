package com.function;

import java.util.Base64;
import java.io.InputStream;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;

public class FunctionTools {

    private HttpRequestMessage<String> request;
    private ExecutionContext context;

    public FunctionTools(HttpRequestMessage<String> request, ExecutionContext context) {

        if (!(request == null)) {
            this.request = request;
        } else {
            NullPointerException npe = new NullPointerException();
            throw npe;
        }
        if (!(context == null)) {
            this.context = context;
        } else {
            NullPointerException npe = new NullPointerException();
            throw npe;
        }
    }

    public String base64encoder(final String username, final String password) {
        String credential = username + ":" + password;
        String encodedCredential = "Basic " + Base64.getEncoder().encodeToString(credential.getBytes());
        return encodedCredential;
    }
    
    public void Logger(String string) {
        context.getLogger().info("[FUNCTION(" + context.getFunctionName().toUpperCase() + ")] - " + string);
    }

    public void SevereLogger(String error) {
        context.getLogger().log(Level.SEVERE, "[FUNCTION(" + context.getFunctionName().toUpperCase() + ")] - " + error);
    }

    public void SevereLogger(JSONObject error) {
        context.getLogger().log(Level.SEVERE, "[FUNCTION(" + context.getFunctionName().toUpperCase() + ")] - " + error);
    }

    public String exceptionTransformer(List<String> erroLista) {
        JSONArray listaErro = new JSONArray();
        JSONObject badRequest = new JSONObject();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        Date date = new Date();
        badRequest.put("timestamp", dateFormat.format(date));
        badRequest.put("message", "JSON inválido");
        badRequest.put("code", "400");
        if (erroLista.size() > 0) {
            for (int i = 0; i < erroLista.size(); i++) {
                JSONObject objetoErro = new JSONObject();
                objetoErro.put("detail", erroLista.get(i).replace("#/", "$."));
                listaErro.put(objetoErro);
            }
        }
        badRequest.put("details", listaErro);
        return badRequest.toString();
    }

    public void Validate(String schemaPath) {
        JSONObject requestJSON = new JSONObject(request.getBody());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(schemaPath);
        JSONObject schemaBase = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(schemaBase);
        schema.validate(requestJSON); // throws ValidationException if invalid
    }
}