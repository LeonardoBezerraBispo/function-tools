package com.function;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.everit.json.schema.ValidationException;
import org.json.JSONException;

public class FunctionPadronizadaPOST {

    //Variavel de ambiente que recebe X presente em local.settings.json
    private static final String hostnamePega = System.getenv("HOSTNAME_PEGA");


    private static final HttpClient client = HttpClient.newHttpClient();

    @FunctionName("nome-da-funcao-aqui2")
    /*
    * Nosso aqui (@FunctionName) padrão é separação por hifén
    * Exemplo: invés de "ClaimDetails" ou "claim details", o correto é "claim-details"
    * Sem caracteres especiais, apenas o hifén para separação
    */ 


    public HttpResponseMessage run(@HttpTrigger(
            name = "req",
            // Nome da requisição, pode manter como "req"
            
            methods = {HttpMethod.POST},
            // Aqui vão os métodos da função (podem ser mais de um)

            authLevel = AuthorizationLevel.ANONYMOUS,
            // Nivel de autorização, pode manter este 

            route = "route-aqui"
            /* Route da função (http://localhost:7071/api/route-é-aqui.)
            * Não começar com "/", ela já é incluida automaticamente quando você inclui um route
            */
            
            ) HttpRequestMessage<String> request,
            final ExecutionContext context) {
        FunctionTools function = new FunctionTools(request, context);
        function.Logger("INICIO");

        //Obtendo Header, No caso "useridentifier" e guardando em uma String
        String userIdentifier = request.getHeaders().get("useridentifier");

        try {

            function.Validate("aqui vai o nome do arquivo que será utilizado como base na validação");

            // este arquivo deve estar presente na pasta resources
            // formato .json
            // deve ser um JSONSchema
            // recomendo utilizar nomeação hifenizada quando houver espaços
            // recomendo utilizar mesmo nome da função


            //aqui faz outra requisição
            HttpRequest requestPega = HttpRequest.newBuilder()
                    .uri(new URI("aqui é a url"))

                    //Adição de headers + valor
                    .headers("UserIdentifier", userIdentifier)

                    // Está recebendo o corpo da requisição dentro
                    .POST(HttpRequest.BodyPublishers.ofString(request.getBody())).build();
                                                                                            //////////////////
            // function.Logger("aqui vai o texto")
            // Vai printar no console no formato ([NOME-DA-FUNCAO] "string")

            function.Logger("Iniciando requisição:");

            // obtendo informações

            // metodo e url
            function.Logger("Conectando-se a: " + requestPega.method() + " " + requestPega.uri());

            // headers
            function.Logger("Headers: " + requestPega.headers().toString());

            // body
            function.Logger("Body: " + request.getBody());

            HttpResponse<String> responsePega;
            responsePega = client.send(requestPega, HttpResponse.BodyHandlers.ofString());

            //obtendo informações sobre response


            function.Logger("Conexão bem sucedida!");

            // url do response
            function.Logger("Resposta de : " + responsePega.uri());

            // status code do response
            function.Logger("Status http: " + responsePega.statusCode());

            // headers do response 
            function.Logger("Headers: " + responsePega.headers().map());

            // body do response 
            function.Logger("Body: " + responsePega.body());


            function.Logger("Conexão finalizada!");

            // toda function tem que retornar um status.

            // neste caso abaixo, eu pego o status code response do backend e repasso direto como status code do front-end (aqui)
            return request.createResponseBuilder(HttpStatus.valueOf(responsePega.statusCode()))
                    .body(responsePega.body()).build();

        // Caso pegue erro na estrutura do JSON no body, cairá na exceção abaixo e retornará 400, com a exceção detalhada no body
        } catch (JSONException e) {

            //esse faz a mesma função do logger normal, porém aparece em vermelho no console, comumente utilizado para erros.
            function.SevereLogger("Erro na estrutura");
            function.SevereLogger(e.getCause().getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(e.getCause().getMessage()).build();

        // Caso pegue erro na validação do JSON no body, cairá na exceção abaixo e retornará 400, com a exceção detalhada no body
        // Em formato JSON
        } catch (ValidationException e) {
            function.SevereLogger("Erro na validação");
            function.SevereLogger(function.exceptionTransformer(e.getAllMessages()));
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(function.exceptionTransformer(e.getAllMessages())).build();

        // Caso pegue erro na conexão com back-end, cairá na exceção abaixo e retornará 500, com a exceção detalhada no body
        } catch (Exception e) {
            function.SevereLogger("Erro na conexão com back-end");
            function.SevereLogger(e.getCause().getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getCause().getMessage())
                    .build();
        } finally {
            function.Logger("FIM");
        }
    }
}