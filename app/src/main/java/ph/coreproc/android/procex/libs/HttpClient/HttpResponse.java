package ph.coreproc.android.procex.libs.HttpClient;

/**
 * Created by chrisbjr on 10/1/14.
 */
public class HttpResponse {

    public static String getHttpResponseMessage(int httpStatusCode) {
        String message = "";
        switch (httpStatusCode) {
            case 0:
                message = "No Internet Connection";
                break;
            case 100:
                message = "Continue";
                break;
            case 101:
                message = "Switching Protocols";
                break;
            case 102:
                message = "Processing";
                break;
            case 200:
                message = "OK";
                break;
            case 201:
                message = "Created";
                break;
            case 202:
                message = "Accepted";
                break;
            case 203:
                message = "Non-Authoritative Information";
                break;
            case 204:
                message = "No Content";
                break;
            case 205:
                message = "Reset Content";
                break;
            case 206:
                message = "Partial Content";
                break;
            case 207:
                message = "Multi-Status";
                break;
            case 208:
                message = "Already Reported";
                break;
            case 226:
                message = "IM Used";
                break;
            case 300:
                message = "Multiple Choices";
                break;
            case 301:
                message = "Moved Permanently";
                break;
            case 302:
                message = "Found";
                break;
            case 303:
                message = "See Other";
                break;
            case 304:
                message = "Not Modified";
                break;
            case 305:
                message = "Use Proxy";
                break;
            case 306:
                message = "Switch Proxy";
                break;
            case 307:
                message = "Temporary Redirect";
                break;
            case 308:
                message = "Permanent Redirect";
                break;
            case 400:
                message = "Bad Request";
                break;
            case 401:
                message = "Unauthorized";
                break;
            case 402:
                message = "Payment Required";
                break;
            case 403:
                message = "Forbidden";
                break;
            case 404:
                message = "Not Found";
                break;
            case 405:
                message = "Method Not Allowed";
                break;
            case 406:
                message = "Not Acceptable";
                break;
            case 407:
                message = "Proxy Authentication Required";
                break;
            case 408:
                message = "Request Timeout";
                break;
            case 409:
                message = "Conflict";
                break;
            case 410:
                message = "Gone";
                break;
            case 411:
                message = "Length Required";
                break;
            case 412:
                message = "Precondition Failed";
                break;
            case 413:
                message = "Request Entity Too Large";
                break;
            case 414:
                message = "Request-URI Too Long";
                break;
            case 415:
                message = "Unsupported Media Type";
                break;
            case 416:
                message = "Requested Range Not Satisfiable";
                break;
            case 417:
                message = "Expectation Failed";
                break;
            case 418:
                message = "I'm a teapot! April Fools!"; // April Fools!
                break;
            case 419:
                message = "Authentication Timeout";
                break;
            case 420:
                message = "Method Failure (Spring Framework) or Enhance Your Calm (Twitter)";
                break;
            case 422:
                message = "Unprocessable Entity";
                break;
            case 423:
                message = "Locked";
                break;
            case 424:
                message = "Failed Dependency";
                break;
            case 426:
                message = "Upgrade Required";
                break;
            case 428:
                message = "Precondition Required";
                break;
            case 429:
                message = "Too Many Requests";
                break;
            case 431:
                message = "Request Header Fields Too Large";
                break;
            case 440:
                message = "Login Timeout (Microsoft)";
                break;
            case 444:
                message = "No Response";
                break;
            case 449:
                message = "Retry WIth";
                break;
            case 450:
                message = "Blocked by Windows Parental Controls (Microsoft)";
                break;
            case 451:
                message = "Unavailable For Legal Reasons";
                break;
            case 494:
                message = "Request Header Too Large (Nginx)";
                break;
            case 495:
                message = "Cert Error (Nginx)";
                break;
            case 496:
                message = "No Cert (Nginx)";
                break;
            case 497:
                message = "HTTP to HTTPS (Nginx)";
                break;
            case 498:
                message = "Token expired/invalid (Esri)";
                break;
            case 499:
                message = "Client Closed Request (Nginx)";
                break;
        }
        return message;
    }

}
