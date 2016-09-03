package com.example.jaeseok.airshare.api;

import com.example.jaeseok.airshare.api.wrapper.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * A client for Cloud Files.  Here follows a basic example of logging in, creating a container and an
 * object, retrieving the object, and then deleting both the object and container.  For more examples,
 * see the code in com.rackspacecloud.client.cloudfiles.sample, which contains a series of examples.
 *
 * <pre>
 *
 *  //  Create the client object for username "jdoe", password "johnsdogsname".
 * 	FilesClient myClient = FilesClient("jdoe", "johnsdogsname");
 *
 *  // Log in (<code>login()</code> will return false if the login was unsuccessful.
 *  assert(myClient.login());
 *
 *  // Make sure there are no containers in the account
 *  assert(myClient.listContainers.length() == 0);
 *
 *  // Create the container
 *  assert(myClient.createContainer("myContainer"));
 *
 *  // Now we should have one
 *  assert(myClient.listContainers.length() == 1);
 *
 *  // Upload the file "alpaca.jpg"
 *  assert(myClient.storeObject("myContainer", new File("alapca.jpg"), "image/jpeg"));
 *
 *  // Download "alpaca.jpg"
 *  FilesObject obj = myClient.getObject("myContainer", "alpaca.jpg");
 *  byte data[] = obj.getObject();
 *
 *  // Clean up after ourselves.
 *  // Note:  Order here is important, you can't delete non-empty containers.
 *  assert(myClient.deleteObject("myContainer", "alpaca.jpg"));
 *  assert(myClient.deleteContainer("myContainer");
 * </pre>
 *
 * @see com.airshare.swiftclient.sample.FilesCli
 * @see com.airshare.swiftclient.sample.FilesAuth
 * @see com.airshare.swiftclient.sample.FilesCopy
 * @see com.airshare.swiftclient.sample.FilesList
 * @see com.airshare.swiftclient.sample.FilesRemove
 * @see com.airshare.swiftclient.sample.FilesMakeContainer
 *
 * @author lvaughn
 */
public class FilesClient implements FilesClientInterface
{
    public static final String VERSION = "v1";

    private String username = null;
    private String password = null;
    private String account = null;
    private String authenticationURL;
    private int connectionTimeOut;
    private String storageURL = null;
    private String cdnManagementURL = null;
    private String authToken = null;
    private boolean isLoggedin = false;
    private boolean useETag = true;
    private boolean snet = false;
    private String snetAddr = "https://snet-";

    private HttpClient client = null;

    private static Logger logger = Logger.getLogger(FilesClient.class);

    /**
     * @param client    The HttpClient to talk to Swift
     * @param username  The username to log in to
     * @param password  The password
     * @param account   The Cloud Files account to use
     * @param connectionTimeOut  The connection timeout, in ms.
     */
    public FilesClient(HttpClient client, String username, String password, String authUrl, String account, int connectionTimeOut) {
        this.client = client;
        this.username = username;
        this.password = password;
        this.account = account;
        if(authUrl == null) {
            authUrl = FilesUtil.getProperty("auth_url");
        }
        if(account != null && account.length() > 0) {
            this.authenticationURL = authUrl + VERSION + "/" + account + FilesUtil.getProperty("auth_url_post");
        }
        else {
            this.authenticationURL = authUrl;
        }
        this.connectionTimeOut = connectionTimeOut;

        setUserAgent(FilesConstants.USER_AGENT);

        if(logger.isDebugEnabled()) {
            logger.debug("UserName: " + this.username);
            logger.debug("AuthenticationURL: " + this.authenticationURL);
            logger.debug("ConnectionTimeOut: " + this.connectionTimeOut);
        }
    }

    /**
     * @param username  The username to log in to
     * @param password  The password
     * @param account   The Cloud Files account to use
     * @param connectionTimeOut  The connection timeout, in ms.
     */
    public FilesClient(String username, String password, String authUrl, String account, final int connectionTimeOut)
    {
        this(new DefaultHttpClient() {
            protected HttpParams createHttpParams() {
                BasicHttpParams params = new BasicHttpParams();
                org.apache.http.params.HttpConnectionParams.setSoTimeout(params, connectionTimeOut);
                params.setParameter("http.socket.timeout", connectionTimeOut);
                return params;
            }

            @Override
            protected ClientConnectionManager createClientConnectionManager() {
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(
                        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schemeRegistry.register(
                        new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                return new ThreadSafeClientConnManager(createHttpParams(), schemeRegistry);
            }
        }, username, password, authUrl, account, connectionTimeOut);

    }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT.  If <code>account</code>
     * is null, "Mosso Style" authentication is assumed, otherwise standard Cloud Files authentication is used.
     *
     * @param username
     * @param password
     * @param authUrl
     */
    public FilesClient(String username, String password, String authUrl)
    {
        this (username, password, authUrl, null, 30000);
    }

    /**
     * Mosso-style authentication (No accounts).
     *
     * @param username     Your CloudFiles username
     * @param apiAccessKey Your CloudFiles API Access Key
     */
    public FilesClient(String username, String apiAccessKey)
    {
        this (username, apiAccessKey, null, null, 30000);
        //lConnectionManagerogger.warn("LGV");
        //logger.debug("LGV:" + client.getHttpConnectionManager());
    }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT and username, password,
     * and account from FilesUtil
     *
     */
    public FilesClient()
    {
        this (FilesUtil.getProperty("username"),
                FilesUtil.getProperty("password"),
                null,
                FilesUtil.getProperty("account"),
                FilesUtil.getIntProperty("connection_timeout"));
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getAccount()
	 */
    public String getAccount()
    {
        return account;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setAccount(java.lang.String)
	 */
    public void setAccount(String account)
    {
        this.account = account;
        if (account != null && account.length() > 0) {
            this.authenticationURL = FilesUtil.getProperty("auth_url")+VERSION+"/"+account+FilesUtil.getProperty("auth_url_post");
        }
        else {
            this.authenticationURL = FilesUtil.getProperty("auth_url");
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#login()
	 */
    public boolean login() throws IOException, HttpException
    {
        HttpGet method = new HttpGet(authenticationURL);
        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);

        method.setHeader(FilesUtil.getProperty("auth_user_header", FilesConstants.X_STORAGE_USER_DEFAULT),
                username);
        method.setHeader(FilesUtil.getProperty("auth_pass_header", FilesConstants.X_STORAGE_PASS_DEFAULT),
                password);

        FilesResponse response = new FilesResponse(client.execute(method));

        if (response.loginSuccess())
        {
            isLoggedin   = true;
            if(usingSnet() || envSnet()){
                storageURL = snetAddr + response.getStorageURL().substring(8);
            }
            else{
                storageURL = response.getStorageURL();
            }
            cdnManagementURL = response.getCDNManagementURL();
            authToken = response.getAuthToken();
            logger.debug("storageURL: " + storageURL);
            logger.debug("authToken: " + authToken);
            logger.debug("cdnManagementURL:" + cdnManagementURL);
            logger.debug("ConnectionManager:" + client.getConnectionManager());
        }
        method.abort();

        return this.isLoggedin;
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#login(java.lang.String, java.lang.String, java.lang.String)
	 */
    public boolean login(String authToken, String storageURL, String cdnManagmentUrl) throws IOException, HttpException
    {
        isLoggedin   = true;
        this.storageURL = storageURL;
        this.cdnManagementURL = cdnManagmentUrl;
        this.authToken = authToken;
        return true;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listContainersInfo()
	 */
    public List<FilesContainerInfo> listContainersInfo() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        return listContainersInfo(-1, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listContainersInfo(int)
	 */
    public List<FilesContainerInfo> listContainersInfo(int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        return listContainersInfo(limit, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listContainersInfo(int, java.lang.String)
	 */
    public List<FilesContainerInfo> listContainersInfo(int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        if (!this.isLoggedin()) {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        HttpGet method = null;
        try {
            LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
            if(limit > 0) {
                parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
            }
            if(marker != null) {
                parameters.add(new BasicNameValuePair("marker", marker));
            }
            parameters.add(new BasicNameValuePair("format", "xml"));
            String uri = makeURI(storageURL, parameters);
            method = new HttpGet(uri);
            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            FilesResponse response = new FilesResponse(client.execute(method));

            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
                if(login()) {
                    method = new HttpGet(uri);
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    response = new FilesResponse(client.execute(method));
                }
                else {
                    throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                }
            }

            if (response.getStatusCode() == HttpStatus.SC_OK)
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(response.getResponseBodyAsStream());

                NodeList nodes = document.getChildNodes();
                Node accountNode = nodes.item(0);
                if (! "account".equals(accountNode.getNodeName())) {
                    logger.error("Got unexpected type of XML");
                    return null;
                }
                ArrayList <FilesContainerInfo> containerList = new ArrayList<FilesContainerInfo>();
                NodeList containerNodes = accountNode.getChildNodes();
                for(int i=0; i < containerNodes.getLength(); ++i) {
                    Node containerNode = containerNodes.item(i);
                    if(!"container".equals(containerNode.getNodeName())) continue;
                    String name = null;
                    int count = -1;
                    long size = -1;
                    NodeList objectData = containerNode.getChildNodes();
                    for(int j=0; j < objectData.getLength(); ++j) {
                        Node data = objectData.item(j);
                        if ("name".equals(data.getNodeName())) {
                            name = data.getTextContent();
                        }
                        else if ("bytes".equals(data.getNodeName())) {
                            size = Long.parseLong(data.getTextContent());
                        }
                        else if ("count".equals(data.getNodeName())) {
                            count = Integer.parseInt(data.getTextContent());
                        }
                        else {
                            logger.debug("Unexpected container-info tag:" + data.getNodeName());
                        }
                    }
                    if (name != null) {
                        FilesContainerInfo obj = new FilesContainerInfo(name, count, size);
                        containerList.add(obj);
                    }
                }
                return containerList;
            }
            else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
            {
                return new ArrayList<FilesContainerInfo>();
            }
            else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
            {
                throw new FilesNotFoundException("Account not Found", response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                throw new FilesException("Unexpected Return Code", response.getResponseHeaders(), response.getStatusLine());
            }
        }
        catch (Exception ex) {
            throw new FilesException("Unexpected problem, probably in parsing Server XML", ex);
        }
        finally {
            if (method != null)
                method.abort();
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listContainers()
	 */
    public List<FilesContainer> listContainers() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        return listContainers(-1, null);
    }
    /* (non-Javadoc)
  * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listContainers(int)
  */
    public List<FilesContainer> listContainers(int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        return listContainers(limit, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listContainers(int, java.lang.String)
	 */
    public List<FilesContainer> listContainers(int limit, String marker) throws IOException, HttpException, FilesException
    {
        if (!this.isLoggedin()) {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        HttpGet method = null;
        try {
            LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();

            if(limit > 0) {
                parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
            }
            if(marker != null) {
                parameters.add(new BasicNameValuePair("marker", marker));
            }

            String uri = parameters.size() > 0 ? makeURI(storageURL, parameters) : storageURL;
            method = new HttpGet(uri);
            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            FilesResponse response = new FilesResponse(client.execute(method));

            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                method.abort();
                if(login()) {
                    method = new HttpGet(uri);
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    response = new FilesResponse(client.execute(method));
                }
                else {
                    throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                }
            }

            if (response.getStatusCode() == HttpStatus.SC_OK)
            {
                // logger.warn(method.getResponseCharSet());
                StrTokenizer tokenize = new StrTokenizer(response.getResponseBodyAsString());
                tokenize.setDelimiterString("\n");
                String [] containers = tokenize.getTokenArray();
                ArrayList <FilesContainer> containerList = new ArrayList<FilesContainer>();
                for(String container : containers) {
                    containerList.add(new FilesContainer(container, this));
                }
                return containerList;
            }
            else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
            {
                return new ArrayList<FilesContainer>();
            }
            else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
            {
                throw new FilesNotFoundException("Account was not found", response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                throw new FilesException("Unexpected response from server", response.getResponseHeaders(), response.getStatusLine());
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new FilesException("Unexpected error, probably parsing Server XML", ex);
        }
        finally {
            if (method != null) method.abort();
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjectsStartingWith(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String)
	 */
    public List<FilesObject> listObjectsStartingWith (String container, String startsWith, String path, int limit, String marker) throws IOException, FilesException
    {
        return listObjectsStartingWith(container, startsWith, path, limit, marker, null);
    }
    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjectsStartingWith(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.Character)
	 */
    public List<FilesObject> listObjectsStartingWith (String container, String startsWith, String path, int limit, String marker, Character delimiter) throws IOException, FilesException
    {
        if (!this.isLoggedin()) {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        if (!isValidContainerName(container))  {
            throw new FilesInvalidNameException(container);
        }
        HttpGet method = null;
        try {
            LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
            parameters.add(new BasicNameValuePair ("format", "xml"));
            if (startsWith != null) {
                parameters.add(new BasicNameValuePair (FilesConstants.LIST_CONTAINER_NAME_QUERY, startsWith));    		}
            if(path != null) {
                parameters.add(new BasicNameValuePair("path", path));
            }
            if(limit > 0) {
                parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
            }
            if(marker != null) {
                parameters.add(new BasicNameValuePair("marker", marker));
            }
            if (delimiter != null) {
                parameters.add(new BasicNameValuePair("delimiter", delimiter.toString()));
            }

            String uri = parameters.size() > 0 ? makeURI(storageURL+"/"+sanitizeForURI(container), parameters) : storageURL;
            method = new HttpGet(uri);
            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            FilesResponse response = new FilesResponse(client.execute(method));

            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
                if(login()) {
                    method = new HttpGet(uri);
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    response = new FilesResponse(client.execute(method));
                }
                else {
                    throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                }
            }

            if (response.getStatusCode() == HttpStatus.SC_OK)
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(response.getResponseBodyAsStream());

                NodeList nodes = document.getChildNodes();
                Node containerList = nodes.item(0);
                if (! "container".equals(containerList.getNodeName())) {
                    logger.error("Got unexpected type of XML");
                    return null;
                }
                ArrayList <FilesObject> objectList = new ArrayList<FilesObject>();
                NodeList objectNodes = containerList.getChildNodes();
                for(int i=0; i < objectNodes.getLength(); ++i) {
                    Node objectNode = objectNodes.item(i);
                    String nodeName = objectNode.getNodeName();
                    if(!("object".equals(nodeName) || "subdir".equals(nodeName))) continue;
                    String name = null;
                    String eTag = null;
                    long size = -1;
                    String mimeType = null;
                    String lastModified = null;
                    NodeList objectData = objectNode.getChildNodes();
                    if ("subdir".equals(nodeName)) {
                        size = 0;
                        mimeType = "application/directory";
                        name = objectNode.getAttributes().getNamedItem("name").getNodeValue();
                    }
                    for(int j=0; j < objectData.getLength(); ++j) {
                        Node data = objectData.item(j);
                        if ("name".equals(data.getNodeName())) {
                            name = data.getTextContent();
                        }
                        else if ("content_type".equals(data.getNodeName())) {
                            mimeType = data.getTextContent();
                        }
                        else if ("hash".equals(data.getNodeName())) {
                            eTag = data.getTextContent();
                        }
                        else if ("bytes".equals(data.getNodeName())) {
                            size = Long.parseLong(data.getTextContent());
                        }
                        else if ("last_modified".equals(data.getNodeName())) {
                            lastModified = data.getTextContent();
                        }
                        else {
                            logger.warn("Unexpected tag:" + data.getNodeName());
                        }
                    }
                    if (name != null) {
                        FilesObject obj = new FilesObject(name, container, this);
                        if (eTag != null) obj.setMd5sum(eTag);
                        if (mimeType != null) obj.setMimeType(mimeType);
                        if (size >= 0) obj.setSize(size);
                        if (lastModified != null) obj.setLastModified(lastModified);
                        objectList.add(obj);
                    }
                }
                return objectList;
            }
            else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
            {
                logger.debug ("Container "+container+" has no Objects");
                return new ArrayList<FilesObject>();
            }
            else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
            {
                throw new FilesNotFoundException("Container was not found", response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                throw new FilesException("Unexpected Server Result", response.getResponseHeaders(), response.getStatusLine());
            }
        }
        catch (FilesNotFoundException fnfe) {
            throw fnfe;
        }
        catch (Exception ex) {
            logger.error("Error parsing xml", ex);
            throw new FilesException("Error parsing server resposne", ex);
        }
        finally {
            if (method != null) method.abort();
        }
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String)
     */
    public List<FilesObject> listObjects(String container) throws IOException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, null, -1, null, null);
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, java.lang.Character)
     */
    public List<FilesObject> listObjects(String container, Character delimiter) throws IOException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, null, -1, null, delimiter);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, int)
	 */
    public List<FilesObject> listObjects(String container, int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, null, limit, null, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, java.lang.String)
	 */
    public List<FilesObject> listObjects(String container, String path) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, path, -1, null, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, java.lang.String, java.lang.Character)
	 */
    public List<FilesObject> listObjects(String container, String path, Character delimiter) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, path, -1, null, delimiter);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, java.lang.String, int)
	 */
    public List<FilesObject> listObjects(String container, String path, int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, path, limit, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, java.lang.String, int, java.lang.String)
	 */
    public List<FilesObject> listObjects(String container, String path, int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, path, limit, marker);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listObjects(java.lang.String, int, java.lang.String)
	 */
    public List<FilesObject> listObjects(String container, int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        return listObjectsStartingWith(container, null, null, limit, marker);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#containerExists(java.lang.String)
	 */
    public boolean containerExists (String container) throws IOException, HttpException
    {
        try {
            this.getContainerInfo(container);
            return true;
        }
        catch(FilesException fnfe) {
            return false;
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getAccountInfo()
	 */
    public FilesAccountInfo getAccountInfo() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        if (this.isLoggedin()) {
            HttpHead method = null;

            try {
                method = new HttpHead(storageURL);
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                FilesResponse response = new FilesResponse(client.execute(method));
                if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
                    if(login()) {
                        method.abort();
                        method = new HttpHead(storageURL);
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        response = new FilesResponse(client.execute(method));
                    }
                    else {
                        throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                    }
                }

                if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
                {
                    int nContainers = response.getAccountContainerCount();
                    long totalSize  = response.getAccountBytesUsed();
                    return new FilesAccountInfo(totalSize,nContainers);
                }
                else {
                    throw new FilesException("Unexpected return from server", response.getResponseHeaders(), response.getStatusLine());
                }
            }
            finally {
                if (method != null) method.abort();
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getContainerInfo(java.lang.String)
	 */
    public FilesContainerInfo getContainerInfo (String container) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(container))
            {

                HttpHead method = null;
                try {
                    method = new HttpHead(storageURL+"/"+sanitizeForURI(container));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
                        if(login()) {
                            method = new HttpHead(storageURL+"/"+sanitizeForURI(container));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
                    {
                        int objCount = response.getContainerObjectCount();
                        long objSize  = response.getContainerBytesUsed();
                        return new FilesContainerInfo(container, objCount,objSize);
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                    {
                        throw new FilesNotFoundException("Container not found: " + container, response.getResponseHeaders(), response.getStatusLine());
                    }
                    else {
                        throw new FilesException("Unexpected result from server", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    if (method != null) method.abort();
                }
            }
            else
            {
                throw new FilesInvalidNameException(container);
            }
        }
        else
            throw new FilesAuthorizationException("You must be logged in", null, null);
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#createContainer(java.lang.String)
	 */
    public void createContainer(String name) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(name))
            {
                HttpPut method = new HttpPut(storageURL+"/"+sanitizeForURI(name));
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);

                try {
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method = new HttpPut(storageURL+"/"+sanitizeForURI(name));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED)
                    {
                        return;
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_ACCEPTED)
                    {
                        throw new FilesContainerExistsException(name, response.getResponseHeaders(), response.getStatusLine());
                    }
                    else {
                        throw new FilesException("Unexpected Response", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    method.abort();
                }
            }
            else
            {
                throw new FilesInvalidNameException(name);
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#deleteContainer(java.lang.String)
	 */
    public boolean deleteContainer(String name) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException, FilesContainerNotEmptyException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(name))
            {
                HttpDelete method = new HttpDelete(storageURL+"/"+sanitizeForURI(name));
                try {
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method = new HttpDelete(storageURL+"/"+sanitizeForURI(name));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
                    {
                        logger.debug ("Container Deleted : "+name);
                        return true;
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                    {
                        logger.debug ("Container does not exist !");
                        throw new FilesNotFoundException("You can't delete an non-empty container", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_CONFLICT)
                    {
                        logger.debug ("Container is not empty, can not delete a none empty container !");
                        throw new FilesContainerNotEmptyException("You can't delete an non-empty container", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    method.abort();
                }
            }
            else
            {
                throw new FilesInvalidNameException(name);
            }
        }
        else
        {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        return false;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#cdnEnableContainer(java.lang.String)
	 */
    public String cdnEnableContainer(String name) throws IOException, HttpException, FilesException
    {
        String returnValue = null;
        if (this.isLoggedin())
        {
            if (isValidContainerName(name))
            {
                HttpPut method = null;
                try {
                    method = new HttpPut(cdnManagementURL+"/"+sanitizeForURI(name));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method = new HttpPut(cdnManagementURL+"/"+sanitizeForURI(name));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_ACCEPTED)
                    {
                        returnValue = response.getCdnUrl();
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        logger.warn("Unauthorized access");
                        throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
                    }
                    else {
                        throw new FilesException("Unexpected Server Response",response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally	{
                    method.abort();
                }
            }
            else
            {
                throw new FilesInvalidNameException(name);
            }
        }
        else
        {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        return returnValue;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#cdnUpdateContainer(java.lang.String, int, boolean, boolean)
	 */
    public String cdnUpdateContainer(String name, int ttl, boolean enabled, boolean retainLogs)
            throws IOException, HttpException, FilesException
    {
        return cdnUpdateContainer(name, ttl, enabled, null, null, retainLogs);
    }

    /**
     * Enables access of files in this container via the Content Delivery Network.
     *
     * @param name The name of the container to enable
     * @param ttl How long the CDN can use the content before checking for an update.  A negative value will result in this not being changed.
     * @param enabled True if this container should be accessible, false otherwise
     * @param retainLogs True if cdn access logs should be kept for this container, false otherwise
     * @return The CDN Url of the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the http protocol
     * @throws FilesException There was an error talking to the CDN Service
     */
    /*
     * @param referrerAcl Unused for now
     * @param userAgentACL Unused for now
     */
    private String cdnUpdateContainer(String name, int ttl, boolean enabled, String referrerAcl, String userAgentACL, boolean retainLogs)
            throws IOException, HttpException, FilesException
    {
        String returnValue = null;
        if (this.isLoggedin())
        {
            if (isValidContainerName(name))
            {
                HttpPost method = null;
                try {
                    method = new HttpPost(cdnManagementURL+"/"+sanitizeForURI(name));

                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    // TTL
                    if (ttl > 0) {
                        method.setHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
                    }
                    // Enabled
                    method.setHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));

                    // Log Retention
                    method.setHeader(FilesConstants.X_CDN_RETAIN_LOGS, Boolean.toString(retainLogs));

                    // Referrer ACL
                    if(referrerAcl != null) {
                        method.setHeader(FilesConstants.X_CDN_REFERRER_ACL, referrerAcl);
                    }

                    // User Agent ACL
                    if(userAgentACL != null) {
                        method.setHeader(FilesConstants.X_CDN_USER_AGENT_ACL, userAgentACL);
                    }
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            new HttpPost(cdnManagementURL+"/"+sanitizeForURI(name));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            // TTL
                            if (ttl > 0) {
                                method.setHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
                            }
                            // Enabled
                            method.setHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_ACCEPTED)
                    {
                        returnValue = response.getCdnUrl();
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        logger.warn("Unauthorized access");
                        throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
                    }
                    else {
                        throw new FilesException("Unexpected Server Response",response.getResponseHeaders(), response.getStatusLine());
                    }
                } finally {
                    if (method != null) {
                        method.abort();
                    }
                }
            }
            else
            {
                throw new FilesInvalidNameException(name);
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        return returnValue;
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getCDNContainerInfo(java.lang.String)
	 */
    public FilesCDNContainer getCDNContainerInfo(String container) throws IOException, FilesNotFoundException, HttpException, FilesException
    {
        if (isLoggedin()) {
            if (isValidContainerName(container))
            {
                HttpHead method = null;
                try {
                    method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
                    {
                        FilesCDNContainer result = new FilesCDNContainer(response.getCdnUrl());
                        result.setName(container);
                        result.setSSLURL(response.getCdnSslUrl());
                        result.setStreamingURL(response.getCdnStreamingUrl());
                        for (Header hdr : response.getResponseHeaders()) {
                            String name = hdr.getName().toLowerCase();
                            if ("x-cdn-enabled".equals(name)) {
                                result.setEnabled(Boolean.valueOf(hdr.getValue()));
                            }
                            else if ("x-log-retention".equals(name)) {
                                result.setRetainLogs(Boolean.valueOf(hdr.getValue()));
                            }
                            else if ("x-ttl".equals(name)) {
                                result.setTtl(Integer.parseInt(hdr.getValue()));
                            }
                            else if ("x-referrer-acl".equals(name)) {
                                result.setReferrerACL(hdr.getValue());
                            }
                            else if ("x-user-agent-acl".equals(name)) {
                                result.setUserAgentACL(hdr.getValue());
                            }
                        }
                        return result;
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        logger.warn("Unauthorized access");
                        throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        throw new FilesNotFoundException("Container is not CDN enabled",response.getResponseHeaders(), response.getStatusLine());
                    }

                    else {
                        throw new FilesException("Unexpected result from server: ", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    if (method != null) {
                        method.abort();
                    }
                }
            }
            else
            {
                throw new FilesInvalidNameException(container);
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#isCDNEnabled(java.lang.String)
	 */
    public boolean isCDNEnabled(String container) throws IOException, HttpException, FilesException
    {
        if (isLoggedin()) {
            if (isValidContainerName(container))
            {
                HttpHead method = null;
                try {
                    method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
                    {
                        for (Header hdr : response.getResponseHeaders()) {
                            String name = hdr.getName().toLowerCase();
                            if ("x-cdn-enabled".equals(name)) {
                                return Boolean.valueOf(hdr.getValue());
                            }
                        }
                        throw new FilesException("Server did not return X-CDN-Enabled header: ", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        logger.warn("Unauthorized access");
                        throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        return false;
                    }

                    else {
                        throw new FilesException("Unexpected result from server: ", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    if (method != null) {
                        method.abort();
                    }
                }
            }
            else
            {
                throw new FilesInvalidNameException(container);
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#createPath(java.lang.String, java.lang.String)
	 */
    public void createPath(String container, String path) throws HttpException, IOException, FilesException {

        if (!isValidContainerName(container))
            throw new FilesInvalidNameException(container);
        if (!isValidObjectName(path))
            throw new FilesInvalidNameException(path);
        storeObject(container, new byte[0], "application/directory", path,
                new HashMap<String, String>());
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#createFullPath(java.lang.String, java.lang.String)
	 */
    public void createFullPath(String container, String path) throws HttpException, IOException, FilesException {
        String parts[] = path.split("/");

        for(int i=0; i < parts.length; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int j=0; j <= i; ++j) {
                if (sb.length() != 0)
                    sb.append("/");
                sb.append(parts[j]);
            }
            createPath(container, sb.toString());
        }

    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listCdnContainers(int)
	 */
    public List<String> listCdnContainers(int limit) throws IOException, HttpException, FilesException
    {
        return listCdnContainers(limit, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listCdnContainers()
	 */
    public List<String> listCdnContainers() throws IOException, HttpException, FilesException
    {
        return listCdnContainers(-1, null);
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listCdnContainers(int, java.lang.String)
	 */
    public List<String> listCdnContainers(int limit, String marker) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            HttpGet method = null;
            try {
                LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
                if (limit > 0) {
                    params.add(new BasicNameValuePair("limit", String.valueOf(limit)));
                }
                if (marker != null) {
                    params.add(new BasicNameValuePair("marker", marker));
                }
                String uri = (params.size() > 0) ? makeURI(cdnManagementURL, params) : cdnManagementURL;
                method = new HttpGet(uri);
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                FilesResponse response = new FilesResponse(client.execute(method));

                if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    method.abort();
                    if(login()) {
                        method = new HttpGet(uri);
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        response = new FilesResponse(client.execute(method));
                    }
                    else {
                        throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                    }
                }

                if (response.getStatusCode() == HttpStatus.SC_OK)
                {
                    StrTokenizer tokenize = new StrTokenizer(response.getResponseBodyAsString());
                    tokenize.setDelimiterString("\n");
                    String [] containers = tokenize.getTokenArray();
                    List<String> returnValue = new ArrayList<String>();
                    for (String containerName: containers)
                    {
                        returnValue.add(containerName);
                    }
                    return returnValue;
                }
                else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    logger.warn("Unauthorized access");
                    throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
                }
                else {
                    throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
                }
            }
            finally {
                if (method != null) method.abort();
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#purgeCDNContainer(java.lang.String, java.lang.String)
	 */
    public void purgeCDNContainer(String container, String emailAddresses) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        if (! isLoggedin) {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        if (!isValidContainerName(container))  {
            throw new FilesInvalidNameException(container);
        }
        HttpDelete method = null;
        try {
            String deleteUri = cdnManagementURL + "/" + sanitizeForURI(container);
            method = new HttpDelete(deleteUri);
            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            if (emailAddresses != null) {
                method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
            }

            FilesResponse response = new FilesResponse(client.execute(method));

            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                method.abort();
                if(login()) {
                    method = new HttpDelete(deleteUri);
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    if (emailAddresses != null) {
                        method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
                    }
                    response = new FilesResponse(client.execute(method));
                }
                else {
                    throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                }
            }

            if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
            {
                return;
            }
            else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
            }
        }
        finally {
            if (method != null) method.abort();
        }

    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#purgeCDNObject(java.lang.String, java.lang.String, java.lang.String)
	 */
    public void purgeCDNObject(String container, String object, String emailAddresses) throws IOException, HttpException, FilesAuthorizationException, FilesException {
        if (! isLoggedin) {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        if (!isValidContainerName(container))  {
            throw new FilesInvalidNameException(container);
        }
        HttpDelete method = null;
        try {
            String deleteUri = cdnManagementURL + "/" + sanitizeForURI(container) +"/"+sanitizeAndPreserveSlashes(object);
            method = new HttpDelete(deleteUri);
            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            if (emailAddresses != null) {
                method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
            }

            FilesResponse response = new FilesResponse(client.execute(method));

            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                method.abort();
                if(login()) {
                    method = new HttpDelete(deleteUri);
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    if (emailAddresses != null) {
                        method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
                    }
                    response = new FilesResponse(client.execute(method));
                }
                else {
                    throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                }
            }

            if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
            {
                return;
            }
            else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
            }
            else {
                System.out.println(response.getStatusLine());
                throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
            }
        }
        finally {
            if (method != null) method.abort();
        }

    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listCdnContainerInfo()
	 */
    public List<FilesCDNContainer> listCdnContainerInfo() throws IOException, HttpException, FilesException
    {
        return listCdnContainerInfo(-1, null);
    }
    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listCdnContainerInfo(int)
	 */
    public List<FilesCDNContainer> listCdnContainerInfo(int limit) throws IOException, HttpException, FilesException
    {
        return listCdnContainerInfo(limit, null);
    }
    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#listCdnContainerInfo(int, java.lang.String)
	 */
    public List<FilesCDNContainer> listCdnContainerInfo(int limit, String marker) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            HttpGet method = null;
            try {
                LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
                params.add(new BasicNameValuePair("format", "xml"));
                if (limit > 0) {
                    params.add(new BasicNameValuePair("limit", String.valueOf(limit)));
                }
                if (marker != null) {
                    params.add(new BasicNameValuePair("marker", marker));
                }
                String uri = params.size() > 0 ? makeURI(cdnManagementURL, params) : cdnManagementURL;
                method = new HttpGet(uri);
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);

                FilesResponse response = new FilesResponse(client.execute(method));

                if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    method.abort();
                    if(login()) {
                        method = new HttpGet(uri);
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);

                        response = new FilesResponse(client.execute(method));
                    }
                    else {
                        throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                    }
                }

                if (response.getStatusCode() == HttpStatus.SC_OK)
                {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(response.getResponseBodyAsStream());

                    NodeList nodes = document.getChildNodes();
                    Node accountNode = nodes.item(0);
                    if (! "account".equals(accountNode.getNodeName())) {
                        logger.error("Got unexpected type of XML");
                        return null;
                    }
                    ArrayList <FilesCDNContainer> containerList = new ArrayList<FilesCDNContainer>();
                    NodeList containerNodes = accountNode.getChildNodes();
                    for(int i=0; i < containerNodes.getLength(); ++i) {
                        Node containerNode = containerNodes.item(i);
                        if(!"container".equals(containerNode.getNodeName())) continue;
                        FilesCDNContainer container = new FilesCDNContainer();
                        NodeList objectData = containerNode.getChildNodes();
                        for(int j=0; j < objectData.getLength(); ++j) {
                            Node data = objectData.item(j);
                            if ("name".equals(data.getNodeName())) {
                                container.setName(data.getTextContent());
                            }
                            else if ("cdn_url".equals(data.getNodeName())) {
                                container.setCdnURL(data.getTextContent());
                            }
                            else if ("cdn_ssl_url".equals(data.getNodeName())) {
                                container.setSSLURL(data.getTextContent());
                            }
                            else if ("cdn_streaming_url".equals(data.getNodeName())) {
                                container.setStreamingURL(data.getTextContent());
                            }
                            else if ("cdn_enabled".equals(data.getNodeName())) {
                                container.setEnabled(Boolean.parseBoolean(data.getTextContent()));
                            }
                            else if ("log_retention".equals(data.getNodeName())) {
                                container.setRetainLogs(Boolean.parseBoolean(data.getTextContent()));
                            }
                            else if ("ttl".equals(data.getNodeName())) {
                                container.setTtl(Integer.parseInt(data.getTextContent()));
                            }
                            else if ("referrer_acl".equals(data.getNodeName())) {
                                container.setReferrerACL(data.getTextContent());
                            }
                            else if ("useragent_acl".equals(data.getNodeName())) {
                                container.setUserAgentACL(data.getTextContent());
                            }
                            else {
                                //logger.warn("Unexpected container-info tag:" + data.getNodeName());
                            }
                        }
                        if (container.getName() != null) {
                            containerList.add(container);
                        }
                    }
                    return containerList;
                }
                else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    logger.warn("Unauthorized access");
                    throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
                }
                else {
                    throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
                }
            }
            catch (SAXException ex) {
                // probably a problem parsing the XML
                throw new FilesException("Problem parsing XML", ex);
            }
            catch (ParserConfigurationException ex) {
                // probably a problem parsing the XML
                throw new FilesException("Problem parsing XML", ex);
            }
            finally {
                if (method != null) method.abort();
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }
    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#createManifestObject(java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.rackspacecloud.client.cloudfiles.IFilesTransferCallback)
	 */
    public boolean createManifestObject(String container, String contentType, String name, String manifest, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
        return createManifestObject(container, contentType, name, manifest, new HashMap<String, String>(), callback);
    }
    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#createManifestObject(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 */
    public boolean createManifestObject(String container, String contentType, String name, String manifest, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
        return createManifestObject(container, contentType, name, manifest, metadata, null);
    }
    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#createManifestObject(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, com.rackspacecloud.client.cloudfiles.IFilesTransferCallback)
	 */
    public boolean createManifestObject(String container, String contentType, String name, String manifest, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
        byte[] arr = new byte[0];
        if (this.isLoggedin())
        {
            String objName	 =  name;
            if (isValidContainerName(container) && isValidObjectName(objName))
            {

                HttpPut method = null;
                try {
                    method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    method.setHeader(FilesConstants.MANIFEST_HEADER, manifest);
                    ByteArrayEntity entity = new ByteArrayEntity (arr);
                    entity.setContentType(contentType);
                    method.setEntity(new RequestEntityWrapper(entity, callback));
                    for(String key : metadata.keySet()) {
                        // logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
                        method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                    }

                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            if (manifest != null){
                                method.setHeader(FilesConstants.MANIFEST_HEADER, manifest);
                            }
                            entity = new ByteArrayEntity (arr);
                            entity.setContentType(contentType);
                            method.setEntity(new RequestEntityWrapper(entity, callback));
                            for(String key : metadata.keySet()) {
                                method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                            }
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED)
                    {
                        return true;
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
                    {
                        throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
                    {
                        throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else
                    {
                        throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally{
                    if (method != null) method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObjectAs(java.lang.String, java.io.File, java.lang.String, java.lang.String)
	 */
    public String storeObjectAs (String container, File obj, String contentType, String name) throws IOException, HttpException, FilesException
    {
        return storeObjectAs(container, obj, contentType, name, new HashMap<String,String>(), null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObjectAs(java.lang.String, java.io.File, java.lang.String, java.lang.String, com.rackspacecloud.client.cloudfiles.IFilesTransferCallback)
	 */
    public String storeObjectAs (String container, File obj, String contentType, String name, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
        return storeObjectAs(container, obj, contentType, name, new HashMap<String,String>(), callback);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObjectAs(java.lang.String, java.io.File, java.lang.String, java.lang.String, java.util.Map)
	 */
    public String storeObjectAs (String container, File obj, String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
        return storeObjectAs (container, obj, contentType, name, metadata, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObjectAs(java.lang.String, java.io.File, java.lang.String, java.lang.String, java.util.Map, com.rackspacecloud.client.cloudfiles.IFilesTransferCallback)
	 */
    public String storeObjectAs (String container, File obj, String contentType, String name, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(container) && isValidObjectName(name) )
            {
                if (!obj.exists())
                {
                    throw new FileNotFoundException(name + " does not exist");
                }

                if (obj.isDirectory())
                {
                    throw new IOException("The alleged file was a directory");
                }

                HttpPut method = null;
                try {
                    method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(name));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    if (useETag) {
                        method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
                    }
                    method.setEntity( new RequestEntityWrapper(new FileEntity (obj, contentType), callback));
                    for(String key : metadata.keySet()) {
                        method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                    }
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(name));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            if (useETag) {
                                method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
                            }
                            method.setEntity( new RequestEntityWrapper(new FileEntity (obj, contentType), callback));
                            for(String key : metadata.keySet()) {
                                method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                            }
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }
                    if (response.getStatusCode() == HttpStatus.SC_CREATED)
                    {
                        return response.getResponseHeader(FilesConstants.E_TAG).getValue();
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
                    {
                        throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
                    {
                        throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else
                    {
                        throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    if (method != null) method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(name)) {
                    throw new FilesInvalidNameException(name);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObject(java.lang.String, java.io.File, java.lang.String)
	 */
    public String storeObject (String container, File obj, String contentType) throws IOException, HttpException, FilesException
    {
        return storeObjectAs(container, obj, contentType, obj.getName());
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObject(java.lang.String, byte[], java.lang.String, java.lang.String, java.util.Map)
	 */
    public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
        return storeObject(container, obj, contentType, name, metadata, null);
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObject(java.lang.String, byte[], java.lang.String, java.lang.String, java.util.Map, com.rackspacecloud.client.cloudfiles.IFilesTransferCallback)
	 */
    public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            String objName	 =  name;
            if (isValidContainerName(container) && isValidObjectName(objName))
            {

                HttpPut method = null;
                try {
                    method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    if (useETag) {
                        method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
                    }
                    ByteArrayEntity entity = new ByteArrayEntity (obj);
                    entity.setContentType(contentType);
                    method.setEntity(new RequestEntityWrapper(entity, callback));
                    for(String key : metadata.keySet()) {
                        // logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
                        method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                    }

                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        if(login()) {
                            method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                            if (useETag) {
                                method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
                            }
                            entity = new ByteArrayEntity (obj);
                            entity.setContentType(contentType);
                            method.setEntity(new RequestEntityWrapper(entity, callback));
                            for(String key : metadata.keySet()) {
                                method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                            }
                            response = new FilesResponse(client.execute(method));
                        }
                        else {
                            throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
                        }
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED)
                    {
                        return true;
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
                    {
                        throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
                    {
                        throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
                    }
                    else
                    {
                        throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally{
                    if (method != null) method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeStreamedObject(java.lang.String, java.io.InputStream, java.lang.String, java.lang.String, java.util.Map)
	 */
    public String storeStreamedObject(String container, InputStream data, String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            String objName	 =  name;
            if (isValidContainerName(container) && isValidObjectName(objName))
            {
                HttpPut method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                InputStreamEntity entity = new InputStreamEntity(data, -1);
                entity.setChunked(true);
                entity.setContentType(contentType);
                method.setEntity(entity);
                for(String key : metadata.keySet()) {
                    // logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
                    method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                }
                method.removeHeaders("Content-Length");


                try {
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_CREATED)
                    {
                        return response.getResponseHeader(FilesConstants.E_TAG).getValue();
                    }
                    else {
                        logger.error(response.getStatusLine());
                        throw new FilesException("Unexpected result", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
  * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#storeObjectAs(java.lang.String, java.lang.String, org.apache.http.HttpEntity, java.util.Map, java.lang.String)
  */
    public String storeObjectAs(String container, String name, HttpEntity entity, Map<String,String> metadata, String md5sum) throws IOException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            String objName	 =  name;
            if (isValidContainerName(container) && isValidObjectName(objName))
            {
                HttpPut method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                method.setEntity(entity);
                if (useETag && md5sum != null) {
                    method.setHeader(FilesConstants.E_TAG, md5sum);
                }
                method.setHeader(entity.getContentType());

                for(String key : metadata.keySet()) {
                    method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                }

                try {
                    FilesResponse response = new FilesResponse(client.execute(method));
                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        login();
                        method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        method.setEntity(entity);
                        method.setHeader(entity.getContentType());
                        for(String key : metadata.keySet()) {
                            method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
                        }
                        response = new FilesResponse(client.execute(method));
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED)
                    {
                        return response.getResponseHeader(FilesConstants.E_TAG).getValue();
                    }
                    else {
                        logger.debug(response.getStatusLine());
                        throw new FilesException("Unexpected result", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#copyObject(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
    public String copyObject(String sourceContainer,
                             String sourceObjName,
                             String destContainer,
                             String destObjName)
            throws HttpException, IOException {
        String etag = null;
        if (this.isLoggedin()) {

            if (isValidContainerName(sourceContainer) &&
                    isValidObjectName(sourceObjName) &&
                    isValidContainerName(destContainer) &&
                    isValidObjectName(destObjName)) {

                HttpPut method = null;
                try {
                    String sourceURI = sanitizeForURI(sourceContainer) +
                            "/" + sanitizeForURI(sourceObjName);
                    String destinationURI = sanitizeForURI(destContainer) +
                            "/" + sanitizeForURI(destObjName);

                    method = new HttpPut(storageURL + "/" + destinationURI);
                    method.getParams().setIntParameter("http.socket.timeout",
                            connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    method.setHeader(FilesConstants.X_COPY_FROM, sourceURI);

                    FilesResponse response = new FilesResponse(client.execute(
                            method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();

                        login();
                        method = new HttpPut(storageURL + "/" + destinationURI);
                        method.getParams().setIntParameter("http.socket.timeout",
                                connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        method.setHeader(FilesConstants.X_COPY_FROM, sourceURI);

                        response = new FilesResponse(client.execute(method));
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED) {
                        etag = response.getResponseHeader(FilesConstants.E_TAG)
                                .getValue();

                    } else {
                        throw new FilesException("Unexpected status from server",
                                response.getResponseHeaders(),
                                response.getStatusLine());
                    }

                } finally {
                    if (method != null) {
                        method.abort();
                    }
                }
            } else {
                if (!isValidContainerName(sourceContainer)) {
                    throw new FilesInvalidNameException(sourceContainer);
                } else if (!isValidObjectName(sourceObjName)) {
                    throw new FilesInvalidNameException(sourceObjName);
                } else if (!isValidContainerName(destContainer)) {
                    throw new FilesInvalidNameException(destContainer);
                } else {
                    throw new FilesInvalidNameException(destObjName);
                }
            }
        } else {
            throw new FilesAuthorizationException("You must be logged in",
                    null,
                    null);
        }

        return etag;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#deleteObject(java.lang.String, java.lang.String)
	 */
    public void deleteObject (String container, String objName) throws IOException, FilesNotFoundException, HttpException, FilesException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(container) && isValidObjectName(objName))
            {
                HttpDelete method = null;
                try {
                    method = new HttpDelete(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        login();
                        method = new HttpDelete(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        response = new FilesResponse(client.execute(method));
                    }


                    if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
                    {
                        logger.debug ("Object Deleted : "+objName);
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                    {
                        throw new FilesNotFoundException("Object was not found " + objName, response.getResponseHeaders(), response.getStatusLine());
                    }
                    else {
                        throw new FilesException("Unexpected status from server", response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    if (method != null) method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getObjectMetaData(java.lang.String, java.lang.String)
	 */
    public FilesObjectMetaData getObjectMetaData (String container, String objName) throws IOException, FilesNotFoundException, HttpException, FilesAuthorizationException, FilesInvalidNameException
    {
        FilesObjectMetaData metaData;
        if (this.isLoggedin())
        {
            if (isValidContainerName(container) && isValidObjectName(objName))
            {
                HttpHead method = new HttpHead(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                try {
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();
                        login();
                        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        response = new FilesResponse(client.execute(method));
                    }

                    if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT ||
                            response.getStatusCode() == HttpStatus.SC_OK)
                    {
                        logger.debug ("Object metadata retreived  : "+objName);
                        String mimeType = response.getContentType();
                        String lastModified = response.getLastModified();
                        String eTag = response.getETag();
                        String contentLength = response.getContentLength();

                        metaData = new FilesObjectMetaData(mimeType, contentLength, eTag, lastModified);

                        Header [] headers = response.getResponseHeaders();
                        HashMap<String,String> headerMap = new HashMap<String,String>();

                        for (Header h: headers)
                        {
                            if ( h.getName().startsWith(FilesConstants.X_OBJECT_META) )
                            {
                                headerMap.put(h.getName().substring(FilesConstants.X_OBJECT_META.length()), unencodeURI(h.getValue()));
                            }
                        }
                        if (headerMap.size() > 0)
                            metaData.setMetaData(headerMap);

                        return metaData;
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                    {
                        throw new FilesNotFoundException("Container: " + container + " did not have object " + objName,
                                response.getResponseHeaders(), response.getStatusLine());
                    }
                    else {
                        throw new FilesException("Unexpected Return Code from Server",
                                response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getObject(java.lang.String, java.lang.String)
	 */
    public byte[] getObject (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(container) && isValidObjectName(objName))
            {
                HttpGet method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);

                try {
                    FilesResponse response = new FilesResponse(client.execute(method));

                    if (response.getStatusCode() == HttpStatus.SC_OK)
                    {
                        logger.debug ("Object data retreived  : "+objName);
                        return response.getResponseBody();
                    }
                    else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                    {
                        throw new FilesNotFoundException("Container: " + container + " did not have object " + objName,
                                response.getResponseHeaders(), response.getStatusLine());
                    }
                }
                finally {
                    method.abort();
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getObjectAsStream(java.lang.String, java.lang.String)
	 */
    public InputStream getObjectAsStream (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
        if (this.isLoggedin())
        {
            if (isValidContainerName(container) && isValidObjectName(objName))
            {
                if (objName.length() > FilesConstants.OBJECT_NAME_LENGTH)
                {
                    logger.warn ("Object Name supplied was truncated to Max allowed of " + FilesConstants.OBJECT_NAME_LENGTH + " characters !");
                    objName = objName.substring(0, FilesConstants.OBJECT_NAME_LENGTH);
                    logger.warn ("Truncated Object Name is: " + objName);
                }

                HttpGet method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                FilesResponse response = new FilesResponse(client.execute(method));

                if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    method.abort();
                    login();
                    method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    response = new FilesResponse(client.execute(method));
                }

                if (response.getStatusCode() == HttpStatus.SC_OK)
                {
                    logger.info ("Object data retreived  : "+objName);

                    // DO NOT RELEASE THIS CONNECTION
                    return response.getResponseBodyAsStream();
                }
                else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                {
                    method.abort();
                    throw new FilesNotFoundException("Container: " + container + " did not have object " + objName,
                            response.getResponseHeaders(), response.getStatusLine());
                }
            }
            else
            {
                if (!isValidObjectName(objName)) {
                    throw new FilesInvalidNameException(objName);
                }
                else {
                    throw new FilesInvalidNameException(container);
                }
            }
        }
        else {
            throw new FilesAuthorizationException("You must be logged in", null, null);
        }
        return null;
    }

    /**
     * Utility function to write an InputStream to a file
     *
     * @param is
     * @param f
     * @throws IOException
     */
    static void writeInputStreamToFile (InputStream is, File f) throws IOException
    {
        BufferedOutputStream bf = new BufferedOutputStream (new FileOutputStream (f));
        byte[] buffer = new byte [1024];
        int read = 0;

        while ((read = is.read(buffer)) > 0)
        {
            bf.write(buffer, 0, read);
        }

        is.close();
        bf.flush();
        bf.close();
    }

    /**
     * Reads an input stream into a stream
     *
     * @param is The input stream
     * @return The contents of the stream stored in a string.
     * @throws IOException
     */
    static String inputStreamToString(InputStream stream, String encoding) throws IOException {
        char buffer[] = new char[4096];
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(stream, "utf-8"); // For now, assume utf-8 to work around server bug

        int nRead = 0;
        while((nRead = isr.read(buffer)) >= 0) {
            sb.append(buffer, 0, nRead);
        }
        isr.close();

        return sb.toString();
    }

    /**
     * Calculates the MD5 checksum of a file, returned as a hex encoded string
     *
     * @param f The file
     * @return The MD5 checksum, as a base 16 encoded string
     * @throws IOException
     */
    public static String md5Sum (File f) throws IOException
    {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            int read = 0;

            while( (read = is.read(buffer)) > 0)
            {
                digest.update(buffer, 0, read);
            }

            is.close ();

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);

            // Front load any zeros cut off by BigInteger
            String md5 = bigInt.toString(16);
            while (md5.length() != 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (NoSuchAlgorithmException e) {
            logger.fatal("The JRE is misconfigured on this computer", e);
            return null;
        }
    }

    /**
     * Calculates the MD5 checksum of an array of data
     *
     * @param data The data to checksum
     * @return The checksum, represented as a base 16 encoded string.
     * @throws IOException
     */
    public static String md5Sum (byte[] data) throws IOException
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5sum = digest.digest(data);
            BigInteger bigInt = new BigInteger(1, md5sum);

            // Front load any zeros cut off by BigInteger
            String md5 = bigInt.toString(16);
            while (md5.length() != 32) {
                md5 = "0" + md5;
            }
            return md5;
        }
        catch (NoSuchAlgorithmException nsae) {
            logger.fatal("Major problems with your Java configuration", nsae);
            return null;
        }

    }

    /**
     * Encode any unicode characters that will cause us problems.
     *
     * @param str
     * @return The string encoded for a URI
     */
    public static String sanitizeForURI(String str) {
        URLCodec codec= new URLCodec();
        try {
            return codec.encode(str).replaceAll("\\+", "%20");
        }
        catch (EncoderException ee) {
            logger.warn("Error trying to encode string for URI", ee);
            return str;
        }
    }

    public static String sanitizeAndPreserveSlashes(String str) {
        URLCodec codec= new URLCodec();
        try {
            return codec.encode(str).replaceAll("\\+", "%20").replaceAll("%2F", "/");
        }
        catch (EncoderException ee) {
            logger.warn("Error trying to encode string for URI", ee);
            return str;
        }
    }

    public static String unencodeURI(String str) {
        URLCodec codec= new URLCodec();
        try {
            return codec.decode(str);
        }
        catch (DecoderException ee) {
            logger.warn("Error trying to encode string for URI", ee);
            return str;
        }

    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getConnectionTimeOut()
	 */
    public int getConnectionTimeOut()
    {
        return connectionTimeOut;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setConnectionTimeOut(int)
	 */
    public void setConnectionTimeOut(int connectionTimeOut)
    {
        this.connectionTimeOut = connectionTimeOut;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getStorageURL()
	 */
    public String getStorageURL()
    {
        return storageURL;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getStorageToken()
	 */
    @Deprecated
    public String getStorageToken()
    {
        return authToken;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getAuthToken()
	 */
    public String getAuthToken()
    {
        return authToken;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#isLoggedin()
	 */
    public boolean isLoggedin()
    {
        return isLoggedin;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getUserName()
	 */
    public String getUserName()
    {
        return username;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setUserName(java.lang.String)
	 */
    public void setUserName(String userName)
    {
        this.username = userName;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getPassword()
	 */
    public String getPassword()
    {
        return password;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setPassword(java.lang.String)
	 */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getAuthenticationURL()
	 */
    public String getAuthenticationURL()
    {
        return authenticationURL;
    }

    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setAuthenticationURL(java.lang.String)
	 */
    public void setAuthenticationURL(String authenticationURL)
    {
        this.authenticationURL = authenticationURL;
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getUseETag()
     */
    public boolean getUseETag() {
        return useETag;
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setUseETag(boolean)
     */
    public void setUseETag(boolean useETag) {
        this.useETag = useETag;
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#setUserAgent(java.lang.String)
     */
    public void setUserAgent(String userAgent) {
        client.getParams().setParameter(HTTP.USER_AGENT, userAgent);
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getUserAgent()
     */
    public String getUserAgent() {
        return client.getParams().getParameter(HTTP.USER_AGENT).toString();
    }

    private boolean isValidContainerName(String name) {
        if (name == null) return false;
        int length = name.length();
        if (length == 0 || length > FilesConstants.CONTAINER_NAME_LENGTH) return false;
        if (name.indexOf('/') != -1) return false;
        //if (name.indexOf('?') != -1) return false;
        return true;
    }
    private boolean isValidObjectName(String name) {
        if (name == null) return false;
        int length = name.length();
        if (length == 0 || length > FilesConstants.OBJECT_NAME_LENGTH) return false;
        //if (name.indexOf('?') != -1) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#getCdnManagementURL()
     */
    public String getCdnManagementURL() {
        return cdnManagementURL;
    }


    /* (non-Javadoc)
	 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#updateObjectManifest(java.lang.String, java.lang.String, java.lang.String)
	 */
    public boolean updateObjectManifest(String container, String object, String manifest) throws FilesAuthorizationException,
            HttpException, IOException, FilesInvalidNameException
    {
        return updateObjectMetadataAndManifest(container, object, new HashMap<String, String>(), manifest);
    }
    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#updateObjectMetadata(java.lang.String, java.lang.String, java.util.Map)
     */
    public boolean updateObjectMetadata(String container, String object,
                                        Map<String,String> metadata) throws FilesAuthorizationException,
            HttpException, IOException, FilesInvalidNameException
    {
        return updateObjectMetadataAndManifest(container, object, metadata, null);
    }
    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#updateObjectMetadataAndManifest(java.lang.String, java.lang.String, java.util.Map, java.lang.String)
     */
    public boolean updateObjectMetadataAndManifest(String container, String object,
                                                   Map<String,String> metadata, String manifest) throws FilesAuthorizationException,
            HttpException, IOException, FilesInvalidNameException {
        FilesResponse response;

        if (!isLoggedin) {
            throw new FilesAuthorizationException("You must be logged in",
                    null, null);
        }
        if (!isValidContainerName(container))
            throw new FilesInvalidNameException(container);
        if (!isValidObjectName(object))
            throw new FilesInvalidNameException(object);

        String postUrl = storageURL + "/"+FilesClient.sanitizeForURI(container) +
                "/"+FilesClient.sanitizeForURI(object);

        HttpPost method = null;
        try {
            method = new HttpPost(postUrl);
            if (manifest != null){
                method.setHeader(FilesConstants.MANIFEST_HEADER, manifest);
            }
            method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            if (!(metadata == null || metadata.isEmpty())) {
                for(String key:metadata.keySet())
                    method.setHeader(FilesConstants.X_OBJECT_META+key,
                            FilesClient.sanitizeForURI(metadata.get(key)));
            }
            HttpResponse resp = client.execute(method);
            response = new FilesResponse(resp);
            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                method.abort();
                if(login()) {
                    method = new HttpPost(postUrl);
                    method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    if (!(metadata == null || metadata.isEmpty())) {
                        for(String key:metadata.keySet())
                            method.setHeader(FilesConstants.X_OBJECT_META+key,
                                    FilesClient.sanitizeForURI(metadata.get(key)));
                    }
                    client.execute(method);
                }
            }

            return true;
        } finally {
            if (method != null)
                method.abort();
        }

    }

    private String makeURI(String base, List<NameValuePair> parameters) {
        return base + "?" + URLEncodedUtils.format(parameters, "UTF-8");
    }

    /*
     *
     *private void setQueryParameters(HttpRequestBase method, List<NameValuePair> parameters) throws FilesException{
        URI oldURI = method.getURI();
        try {
            URI newURI = URIUtils.createURI(oldURI.getScheme(), oldURI.getHost(), -1,
                    URLEncoder.encode(oldURI.getPath(), "UTF-8"), URLEncodedUtils.format(parameters, "UTF-8"), null);
            logger.warn("Old Path: " + oldURI.getPath());
            logger.warn("New URI: " + newURI);
            method.setURI(newURI);
        }
        catch (UnsupportedEncodingException uee) {
            logger.error("Somehow, we don't have UTF-8, this is quite a surprise", uee);
            throw new FilesException("Somehow, we don't have UTF-8, this is quite a surprise", uee);
        }
        catch (URISyntaxException use) {
            logger.error("Bad Syntax", use);
            throw new FilesException("Bad URL Syntax", use);
        }
    }
    */
		/* (non-Javadoc)
		 * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#useSnet()
		 */
    public void useSnet(){
        if(snet){
        }
        else{
            snet = true;
            if(storageURL != null){
                storageURL = snetAddr + storageURL.substring(8);
            }
        }
    }
    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#usePublic()
     */
    public void usePublic(){
        if(!snet){
        }
        else{
            snet = false;
            if(storageURL != null){
                storageURL = "https://" + storageURL.substring(snetAddr.length());
            }
        }
    }
    /* (non-Javadoc)
     * @see com.rackspacecloud.client.cloudfiles.FilesClientInterface#usingSnet()
     */
    public boolean usingSnet(){
        return snet;
    }
    private boolean envSnet(){
        if (System.getenv("RACKSPACE_SERVICENET") == null) {
            return false;
        }
        else{
            snet = true;
            return true;
        }
    }
}

