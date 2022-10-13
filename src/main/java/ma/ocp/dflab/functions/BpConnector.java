package ma.ocp.dflab.functions;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;



public class BpConnector {

    private static final Logger log = LoggerFactory.getLogger(BpConnector.class.getName());
    BasicCookieStore cookieStore;
    CloseableHttpClient httpclient;

    public void init() {
        this.cookieStore = new BasicCookieStore();
        this.httpclient = HttpClients.custom().setDefaultCookieStore(this.cookieStore).build();
    }



    public void login(String pwd, String identifiantContrat) throws URISyntaxException, IOException {
        final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
        final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
        init();

        String firstPageHtml = execute(new HttpGet("https://bpnet.gbp.ma/Dashboard"));

        String reqToken = extractRequestToken(firstPageHtml);
        String cid = extractCID(firstPageHtml);
        String ck = extractCK(firstPageHtml);
        String assfid = extractAsSfid(firstPageHtml);
        String asfid = extractAsFid(firstPageHtml);
        HttpUriRequest login = RequestBuilder
                .post()
                .setUri(new URI("https://oauth2.gbp.ma/Auth/Login"))
                .addParameter("__RequestVerificationToken",reqToken)
                .addParameter("cid",cid)
                .addParameter("ck",ck)
                .addParameter("UserName", identifiantContrat)
                .addParameter("Password", pwd)
                .addParameter("as_sfid",assfid)
                .addParameter("as_fid",asfid)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", USER_AGENT)
                .build();
        String loginResponseHtml = execute(login);

        String acs = extractAcs(loginResponseHtml);
        String act = extractAct(loginResponseHtml);
        String senderid = extractSenderId(loginResponseHtml);
        String activityID = extractActivityId(loginResponseHtml);
        String returnURL = extractReturnUrl(loginResponseHtml);
         assfid = extractAsSfid(loginResponseHtml);
         asfid = extractAsFid(loginResponseHtml);


        HttpUriRequest afterLogin = RequestBuilder
                .post()
                .setUri(new URI("https://bpnet.gbp.ma//Account/AfterLogin"))
                .addParameter("acs",acs)
                .addParameter("act",act)
                .addParameter("senderid",senderid)
                .addParameter("returnURL", returnURL)
                .addParameter("activityID", activityID)
                .addParameter("as_sfid",assfid)
                .addParameter("as_fid",asfid)
                .addHeader("Accept", HEADER_ACCEPT)
                .addHeader("Referer","https://oauth2.gbp.ma/Auth/Login")
                .addHeader("Origin","https://oauth2.gbp.ma")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept-Language", "en-US,en;q=0.8,fr;q=0.6,ar;q=0.4")
                .addHeader("Cache-Control", "max-age=0")
                .build();

        String afterLoginOut = execute(afterLogin);
        Asserts.check(afterLoginOut.contains("Object moved to <a href=\"/Dashboard\">here</a>"), " error login");
        log.info("login with success");

    }



    public String getStatements() throws URISyntaxException, IOException {
        log.info("Loading statements");
        HttpUriRequest tmp = RequestBuilder
                .post()
                .setUri(new URI("https://bpnet.gbp.ma/Compte/Compte/GetStatements"))
                .addParameter("x-ms-request-id","zwWF")
                .addParameter("x-ms-request-root-id","2SFvy")
                .addParameter("X-Requested-With","XMLHttpRequest")
                .addHeader("Content-Type", "application/json")
                .build();

        return execute(tmp);
    }

    public String balanceEvolution() throws URISyntaxException, IOException{
        log.info("Loading balance evolution");
        HttpUriRequest tmp = RequestBuilder
                .post()
                .setUri(new URI("https://bpnet.gbp.ma/DashBoard/GetUserAccountBalanceEvolution"))
                .addParameter("x-ms-request-id","zwWF")
                .addParameter("x-ms-request-root-id","2SFvy")
                .addParameter("X-Requested-With","XMLHttpRequest")
                .addHeader("Content-Type", "application/json")
                .build();
        return execute(tmp);
    }


    String execute(HttpUriRequest uriRequest) throws IOException {
        log.debug("call url {}", uriRequest);
        String out = "";
        CloseableHttpResponse response2 = this.httpclient.execute(uriRequest);
        try {
            HttpEntity entity = response2.getEntity();
            out = httpResp2String(response2);

            EntityUtils.consume(entity);
        } finally {
            response2.close();
        }
        log.trace("response HTML {}", out);
        return out;
    }

    String httpResp2String(HttpResponse loginresp)
            throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(loginresp.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private String extractAsFid(String loginOut) {
        return RegExHelper.findFirst("name=\"as_fid\"( *)value=\"([a-zA-Z0-9_\\-\\=]*?)\"", loginOut,false,2);
    }

    private String extractReturnUrl(String loginOut) {
        return RegExHelper.findFirst("name='returnURL'( *)value='([a-zA-Z0-9_\\-\\.\\:\\/]*?)'", loginOut,false,2);
    }

    private String extractActivityId(String loginOut) {
        return RegExHelper.findFirst("name='activityID'( *)value='([a-zA-Z0-9_\\-\\=\\.]*?)'", loginOut,false,2);
    }

    private String extractSenderId(String loginOut) {
        return RegExHelper.findFirst("name='senderid'( *)value='([a-zA-Z0-9_\\-\\=\\.]*?)'", loginOut,false,2);
    }

    private String extractAct(String loginOut) {
        return RegExHelper.findFirst("name='act'( *)value='([a-zA-Z0-9_\\-\\=\\.]*?)'", loginOut,false,2);
    }

    private String extractAcs(String loginOut) {
        return RegExHelper.findFirst("name='acs'( *)value='([a-zA-Z0-9_\\-\\=\\.]*?)'", loginOut,false,2);
    }


    private String extractAsSfid(String loginHtml) {
        return RegExHelper.findFirst("name=\"as_sfid\"( *)value=\"([a-zA-Z0-9_\\-\\=]*?)\"", loginHtml,false,2);
    }

    private String extractCK(String loginHtml) {
        return RegExHelper.findFirst("name=\"ck\" type=\"hidden\"( *)value=\"([a-zA-Z0-9_-]*?)\"", loginHtml,false,2);
    }

    private String extractCID(String loginHtml) {
        return RegExHelper.findFirst("name=\"cid\" type=\"hidden\"( *)value=\"([a-zA-Z0-9_-]*?)\"", loginHtml,false,2);
    }

    private String extractRequestToken(String loginHtml) {
        return RegExHelper.findFirst("name=\"__RequestVerificationToken\" type=\"hidden\"( *)value=\"([a-zA-Z0-9_-]*?)\"", loginHtml,false,2);
    }


}
