package com.example.jaeseok.airshare.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FilesClientInterface {

	/**
	 * Returns the Account associated with the URL
	 * 
	 * @return The account name
	 */
	public abstract String getAccount();

	/**
	 * Set the Account value and reassemble the Authentication URL.
	 *
	 * @param account
	 */
	public abstract void setAccount(String account);

	/**
	 * Log in to CloudFiles.  This method performs the authentication and sets up the client's internal state.
	 * 
	 * @return true if the login was successful, false otherwise.
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 */
	public abstract boolean login() throws IOException, HttpException;

	/**
	 * Log in to CloudFiles.  This method performs the authentication and sets up the client's internal state.
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 */
	public abstract boolean login(String authToken, String storageURL,
								  String cdnManagmentUrl) throws IOException, HttpException;

	/**
	 * List all of the containers available in an account, ordered by container name.
	 *
	 * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
	 *         if there are no containers in the account, the list will be zero length.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesContainerInfo> listContainersInfo()
			throws IOException, HttpException, FilesAuthorizationException,
			FilesException;

	/**
	 * List the containers available in an account, ordered by container name.
	 * 
	 * @param limit The maximum number of containers to return.  -1 returns an unlimited number.
	 *
	 * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
	 *         if there are no containers in the account, the list will be zero length.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesContainerInfo> listContainersInfo(int limit)
			throws IOException, HttpException, FilesAuthorizationException,
			FilesException;

	/**
	 * List the containers available in an account, ordered by container name.
	 *
	 *  @param limit The maximum number of containers to return.  -1 returns an unlimited number.
	 *  @param marker Return containers that occur after this lexicographically.  
	 *  
	 *  @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
	 *         if there are no containers in the account, the list will be zero length.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesContainerInfo> listContainersInfo(int limit,
																String marker) throws IOException, HttpException,
			FilesAuthorizationException, FilesException;

	/**
	 * List the containers available in an account.
	 *
	 * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.  
	 *         if there are no containers in the account, the list will be zero length.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesContainer> listContainers() throws IOException,
			HttpException, FilesAuthorizationException, FilesException;

	/**
	 * List the containers available in an account.
	 *
	 * @param limit The maximum number of containers to return.  -1 denotes no limit.
	
	 * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.  
	 *         if there are no containers in the account, the list will be zero length.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesContainer> listContainers(int limit)
			throws IOException, HttpException, FilesAuthorizationException,
			FilesException;

	/**
	 * List the containers available in an account.
	 * 
	 * @param limit The maximum number of containers to return.  -1 denotes no limit.
	 * @param marker Only return containers after this container.  Null denotes starting at the beginning (lexicographically).  
	 *
	 * @return A List of FilesContainer with all of the containers in the account.  
	 *         if there are no containers in the account, the list will be zero length.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesContainer> listContainers(int limit, String marker)
			throws IOException, HttpException, FilesException;

	/**
	 * List all of the objects in a container with the given starting string.
	 * 
	 * @param container  The container name
	 * @param startsWith The string to start with
	 * @param path Only look for objects in this path
	 * @param limit Return at most <code>limit</code> objects
	 * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjectsStartingWith(String container,
															  String startsWith, String path, int limit, String marker)
			throws IOException, FilesException;

	/**
	 * List all of the objects in a container with the given starting string.
	 * 
	 * @param container  The container name
	 * @param startsWith The string to start with
	 * @param path Only look for objects in this path
	 * @param limit Return at most <code>limit</code> objects
	 * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
	 * @param delimter Use this argument as the delimiter that separates "directories"
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjectsStartingWith(String container,
															  String startsWith, String path, int limit, String marker,
															  Character delimiter) throws IOException, FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjects(String container)
			throws IOException, FilesAuthorizationException, FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param delimter Use this argument as the delimiter that separates "directories"
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjects(String container,
												  Character delimiter) throws IOException,
			FilesAuthorizationException, FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param limit Return at most <code>limit</code> objects
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjects(String container, int limit)
			throws IOException, HttpException, FilesAuthorizationException,
			FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param path Only look for objects in this path
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 */
	public abstract List<FilesObject> listObjects(String container, String path)
			throws IOException, HttpException, FilesAuthorizationException,
			FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param path Only look for objects in this path
	 * @param delimter Use this argument as the delimiter that separates "directories"
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 */
	public abstract List<FilesObject> listObjects(String container,
												  String path, Character delimiter) throws IOException,
			HttpException, FilesAuthorizationException, FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param path Only look for objects in this path
	 * @param limit Return at most <code>limit</code> objects
	 * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjects(String container,
												  String path, int limit) throws IOException, HttpException,
			FilesAuthorizationException, FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param path Only look for objects in this path
	 * @param limit Return at most <code>limit</code> objects
	 * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 */
	public abstract List<FilesObject> listObjects(String container,
												  String path, int limit, String marker) throws IOException,
			HttpException, FilesAuthorizationException, FilesException;

	/**
	 * List the objects in a container in lexicographic order.  
	 * 
	 * @param container  The container name
	 * @param limit Return at most <code>limit</code> objects
	 * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
	 * 
	 * @return A list of FilesObjects starting with the given string
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract List<FilesObject> listObjects(String container, int limit,
												  String marker) throws IOException, HttpException,
			FilesAuthorizationException, FilesException;

	/**
	 * Convenience method to test for the existence of a container in Cloud Files.
	 * 
	 * @param container
	 * @return true if the container exists.  false otherwise.
	 * @throws IOException
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 */
	public abstract boolean containerExists(String container)
			throws IOException, HttpException;

	/**
	 * Gets information for the given account.
	 * 
	 * @return The FilesAccountInfo with information about the number of containers and number of bytes used
	 *         by the given account.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was another error in the request to the server.
	 * @throws FilesAuthorizationException The client's login was invalid.
	 */
	public abstract FilesAccountInfo getAccountInfo() throws IOException,
			HttpException, FilesAuthorizationException, FilesException;

	/**
	 * Get basic information on a container (number of items and the total size).
	 *
	 * @param container The container to get information for
	 * @return ContainerInfo object of the container is present or null if its not present
	 * @throws IOException  There was a socket level exception while talking to CloudFiles
	 * @throws HttpException There was an protocol level exception while talking to Cloudfiles
	 * @throws FilesNotFoundException The container was not found
	 * @throws FilesAuthorizationException The client was not logged in or the log in expired.
	 */
	public abstract FilesContainerInfo getContainerInfo(String container)
			throws IOException, HttpException, FilesException;

	/**
	 * Creates a container
	 *
	 * @param name The name of the container to be created
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesAuthorizationException The client was not property logged in
	 * @throws FilesInvalidNameException The container name was invalid
	 */
	public abstract void createContainer(String name) throws IOException,
			HttpException, FilesAuthorizationException, FilesException;

	/**
	 * Deletes a container
	 * 
	 * @param name  The name of the container
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesAuthorizationException The user is not Logged in
	 * @throws FilesInvalidNameException   The container name is invalid
	 * @throws FilesNotFoundException      The container doesn't exist
	 * @throws FilesContainerNotEmptyException The container was not empty
	 */
	public abstract boolean deleteContainer(String name) throws IOException,
			HttpException, FilesAuthorizationException,
			FilesInvalidNameException, FilesNotFoundException,
			FilesContainerNotEmptyException;

	/**
	 * Enables access of files in this container via the Content Delivery Network.
	 * 
	 * @param name The name of the container to enable
	 * @return The CDN Url of the container
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was an error talking to the CDN Server.
	 */
	public abstract String cdnEnableContainer(String name) throws IOException,
			HttpException, FilesException;

	public abstract String cdnUpdateContainer(String name, int ttl,
											  boolean enabled, boolean retainLogs) throws IOException,
			HttpException, FilesException;

	/**
	 * Gets current CDN sharing status of the container
	 * 
	 * @param name The name of the container to enable
	 * @return Information on the container
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was an error talking to the CloudFiles Server
	 * @throws FilesNotFoundException The Container has never been CDN enabled
	 */
	public abstract FilesCDNContainer getCDNContainerInfo(String container)
			throws IOException, FilesNotFoundException, HttpException,
			FilesException;

	/**
	 * Gets current CDN sharing status of the container
	 * 
	 * @param name The name of the container to enable
	 * @return Information on the container
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException There was an error talking to the CloudFiles Server
	 * @throws FilesNotFoundException The Container has never been CDN enabled
	 */
	public abstract boolean isCDNEnabled(String container) throws IOException,
			HttpException, FilesException;

	/**
	 * Creates a path (but not any of the sub portions of the path)
	 * 
	 * @param container The name of the container.
	 * @param path  The name of the Path
	 * @throws HttpException There was an error at the protocol layer while talking to CloudFiles
	 * @throws IOException There was an error at the socket layer while talking to CloudFiles
	 * @throws FilesException There was another error while taking to the CloudFiles server
	 */
	public abstract void createPath(String container, String path)
			throws HttpException, IOException, FilesException;

	/**
	 * Create all of the path elements for the entire tree for a given path.  Thus, <code>createFullPath("myContainer", "foo/bar/baz")</code> 
	 * creates the paths "foo", "foo/bar" and "foo/bar/baz".
	 * 
	 * @param container The name of the container
	 * @param path The full name of the path
	 * @throws HttpException There was an error at the protocol layer while talking to CloudFiles
	 * @throws IOException There was an error at the socket layer while talking to CloudFiles
	 * @throws FilesException There was another error while taking to the CloudFiles server
	 */
	public abstract void createFullPath(String container, String path)
			throws HttpException, IOException, FilesException;

	/**
	 * Gets the names of all of the containers associated with this account.
	 * 
	 * @param limit The maximum number of container names to return
	 * @return A list of container names
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract List<String> listCdnContainers(int limit)
			throws IOException, HttpException, FilesException;

	/**
	 * Gets the names of all of the containers associated with this account.
	 * 
	 * @return A list of container names
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract List<String> listCdnContainers() throws IOException,
			HttpException, FilesException;

	/**
	 * Gets the names of all of the containers associated with this account.
	 * 
	 * @param limit The maximum number of container names to return
	 * @param marker All of the results will come after <code>marker</code> lexicographically.
	 * @return A list of container names
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract List<String> listCdnContainers(int limit, String marker)
			throws IOException, HttpException, FilesException;

	/**
	 * Purges all items from a given container from the CDN
	 * 
	 * @param container The name of the container
	 * @param emailAddresses An optional comma separated list of email addresses to be notified when the purge is complete. 
	 *                       <code>null</code> if desired.
	 * @throws IOException Error talking to the cdn management server
	 * @throws HttpException Error with HTTP
	 * @throws FilesAuthorizationException Log in was not successful, or account is suspended 
	 * @throws FilesException Other error
	 */
	public abstract void purgeCDNContainer(String container,
										   String emailAddresses) throws IOException, HttpException,
			FilesAuthorizationException, FilesException;

	/**
	 * Purges all items from a given container from the CDN
	 * 
	 * @param container The name of the container
	 * @param object The name of the object
	 * @param emailAddresses An optional comma separated list of email addresses to be notified when the purge is complete. 
	 *                       <code>null</code> if desired.
	 * @throws IOException Error talking to the cdn management server
	 * @throws HttpException Error with HTTP
	 * @throws FilesAuthorizationException Log in was not successful, or account is suspended 
	 * @throws FilesException Other error
	 */
	public abstract void purgeCDNObject(String container, String object,
										String emailAddresses) throws IOException, HttpException,
			FilesAuthorizationException, FilesException;

	/**
	 * Gets list of all of the containers associated with this account.
	 * 
	 * @return A list of containers
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract List<FilesCDNContainer> listCdnContainerInfo()
			throws IOException, HttpException, FilesException;

	/**
	 * Gets list of all of the containers associated with this account.
	 * 
	 * @param limit The maximum number of container names to return
	 * @return A list of containers
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract List<FilesCDNContainer> listCdnContainerInfo(int limit)
			throws IOException, HttpException, FilesException;

	/**
	 * Gets list of all of the containers associated with this account.
	 * 
	 * @param limit The maximum number of container names to return
	 * @param marker All of the names will come after <code>marker</code> lexicographically.
	 * @return A list of containers
	 * 
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract List<FilesCDNContainer> listCdnContainerInfo(int limit,
																 String marker) throws IOException, HttpException, FilesException;

	/**
	 * Create a manifest on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param manifest    Set manifest content here
	 * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract boolean createManifestObject(String container,
												 String contentType, String name, String manifest,
												 IFilesTransferCallback callback) throws IOException, HttpException,
			FilesException;

	/**
	 * Create a manifest on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param manifest    Set manifest content here
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract boolean createManifestObject(String container,
												 String contentType, String name, String manifest,
												 Map<String, String> metadata) throws IOException, HttpException,
			FilesException;

	/**
	 * Create a manifest on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param manifest    Set manifest content here
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract boolean createManifestObject(String container,
												 String contentType, String name, String manifest,
												 Map<String, String> metadata, IFilesTransferCallback callback)
			throws IOException, HttpException, FilesException;

	/**
	 * Store a file on the server
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @return The ETAG if the save was successful, null otherwise
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract String storeObjectAs(String container, File obj,
										 String contentType, String name) throws IOException, HttpException,
			FilesException;

	/**
	 * Store a file on the server
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @return The ETAG if the save was successful, null otherwise
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract String storeObjectAs(String container, File obj,
										 String contentType, String name, IFilesTransferCallback callback)
			throws IOException, HttpException, FilesException;

	/**
	 * Store a file on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @return The ETAG if the save was successful, null otherwise
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesAuthorizationException 
	 */
	public abstract String storeObjectAs(String container, File obj,
										 String contentType, String name, Map<String, String> metadata)
			throws IOException, HttpException, FilesException;

	/**
	 * Store a file on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @param metadata    The callback object that will be called as the data is sent
	 * @return The ETAG if the save was successful, null otherwise
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract String storeObjectAs(String container, File obj,
										 String contentType, String name, Map<String, String> metadata,
										 IFilesTransferCallback callback) throws IOException, HttpException,
			FilesException;

	/**
	 * Copies the file to Cloud Files, keeping the original file name in Cloud Files.
	 * 
	 * @param container    The name of the container to place the file in
	 * @param obj          The File to transfer
	 * @param contentType  The file's MIME type
	 * @return The ETAG if the save was successful, null otherwise
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract String storeObject(String container, File obj,
									   String contentType) throws IOException, HttpException,
			FilesException;

	/**
	 * Store a file on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract boolean storeObject(String container, byte obj[],
										String contentType, String name, Map<String, String> metadata)
			throws IOException, HttpException, FilesException;

	/**
	 * Store a file on the server, including metadata
	 * 
	 * @param container   The name of the container
	 * @param obj         The File containing the file to copy over
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract boolean storeObject(String container, byte obj[],
										String contentType, String name, Map<String, String> metadata,
										IFilesTransferCallback callback) throws IOException, HttpException,
			FilesException;

	/**
	 * Store a file on the server, including metadata, with the contents coming from an input stream.  This allows you to 
	 * not know the entire length of your content when you start to write it.  Nor do you have to hold it entirely in memory
	 * at the same time.
	 * 
	 * @param container   The name of the container
	 * @param data        Any object that implements InputStream
	 * @param contentType The MIME type of the file
	 * @param name        The name of the file on the server
	 * @param metadata    A map with the metadata as key names and values as the metadata values
	 * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract String storeStreamedObject(String container,
											   InputStream data, String contentType, String name,
											   Map<String, String> metadata) throws IOException, HttpException,
			FilesException;

	/**
	 * 
	 * 
	 * @param container The name of the container
	 * @param name The name of the object
	 * @param entity The name of the request entity (make sure to set the Content-Type
	 * @param metadata The metadata for the object
	 * @param md5sum The 32 character hex encoded MD5 sum of the data
	 * @return The ETAG if the save was successful, null otherwise
	 * @throws IOException There was a socket level exception talking to CloudFiles
	 * @throws HttpException There was a protocol level error talking to CloudFiles
	 * @throws FilesException There was an error talking to CloudFiles.
	 */
	public abstract String storeObjectAs(String container, String name,
										 HttpEntity entity, Map<String, String> metadata, String md5sum)
			throws IOException, HttpException, FilesException;

	/**
	 * This method copies the object found in the source container with the
	 * source object name to the destination container with the destination
	 * object name.
	 * @param sourceContainer of object to copy
	 * @param sourceObjName of object to copy
	 * @param destContainer where object copy will be copied
	 * @param destObjName of object copy
	 * @return ETAG if successful, else null
	 * @throws IOException indicates a socket level error talking to CloudFiles
	 * @throws HttpException indicates a protocol level error talking to CloudFiles
	 * @throws FilesException indicates an error talking to CloudFiles
	 */
	public abstract String copyObject(String sourceContainer,
									  String sourceObjName, String destContainer, String destObjName)
			throws HttpException, IOException;

	/**
	 * Delete the given object from it's container.
	 * 
	 * @param container  The container name
	 * @param objName    The object name
	 * @return FilesConstants.OBJECT_DELETED
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesException 
	 */
	public abstract void deleteObject(String container, String objName)
			throws IOException, FilesNotFoundException, HttpException,
			FilesException;

	/**
	 * Get an object's metadata
	 * 
	 * @param container The name of the container
	 * @param objName   The name of the object
	 * @return The object's metadata
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesAuthorizationException The Client's Login was invalid.  
	 * @throws FilesInvalidNameException The container or object name was not valid
	 * @throws FilesNotFoundException The file was not found
	 */
	public abstract FilesObjectMetaData getObjectMetaData(String container,
														  String objName) throws IOException, FilesNotFoundException,
			HttpException, FilesAuthorizationException,
			FilesInvalidNameException;

	/**
	 * Get the content of the given object
	 * 
	 * @param container  The name of the container
	 * @param objName    The name of the object
	 * @return The content of the object
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesAuthorizationException 
	 * @throws FilesInvalidNameException 
	 * @throws FilesNotFoundException 
	 */
	public abstract byte[] getObject(String container, String objName)
			throws IOException, HttpException, FilesAuthorizationException,
			FilesInvalidNameException, FilesNotFoundException;

	/**
	 * Get's the given object's content as a stream
	 * 
	 * @param container  The name of the container
	 * @param objName    The name of the object
	 * @return An input stream that will give the objects content when read from.
	 * @throws IOException   There was an IO error doing network communication
	 * @throws HttpException There was an error with the http protocol
	 * @throws FilesAuthorizationException 
	 * @throws FilesNotFoundException The container does not exist
	 * @throws FilesInvalidNameException 
	 */
	public abstract InputStream getObjectAsStream(String container,
												  String objName) throws IOException, HttpException,
			FilesAuthorizationException, FilesInvalidNameException,
			FilesNotFoundException;

	/**
	 * @return The connection timeout used for communicating with the server (in milliseconds)
	 */
	public abstract int getConnectionTimeOut();

	/**
	 * The timeout we will use for communicating with the server (in milliseconds)
	 * 
	 * @param connectionTimeOut The new timeout for this connection
	 */
	public abstract void setConnectionTimeOut(int connectionTimeOut);

	/**
	 * @return The storage URL on the other end of the ReST api
	 */
	public abstract String getStorageURL();

	/**
	 * @return Get's our storage token.
	 */
	@Deprecated
	public abstract String getStorageToken();

	/**
	 * @return Get's our storage token.
	 */
	public abstract String getAuthToken();

	/**
	 * Has this instance of the client authenticated itself?  Note, this does not mean that a call 
	 * right now will work, if the auth token has timed out, you will need to re-auth.
	 * 
	 * @return True if we logged in, false otherwise.
	 */
	public abstract boolean isLoggedin();

	/**
	 * The username we are logged in with.
	 * 
	 * @return The username
	 */
	public abstract String getUserName();

	/**
	 * Set's the username for this client. Note, setting this after login has no real impact unless the <code>login()</code>
	 * method is called again.
	 * 
	 * @param userName the username
	 */
	public abstract void setUserName(String userName);

	/**
	 * The password the client will use for the login.
	 * 
	 * @return The password
	 */
	public abstract String getPassword();

	/**
	 * Set's the password for this client. Note, setting this after login has no real impact unless the <code>login()</code>
	 * method is called again.
	 *
	 * @param password The new password
	 */
	public abstract void setPassword(String password);

	/**
	 * The URL we will use for Authentication
	 * 
	 * @return The URL (represented as a string)
	 */
	public abstract String getAuthenticationURL();

	/**
	 * Changes the URL of the authentication service.  Note, if one is logged in, this doesn't have an effect unless one calls login again.
	 * 
	 * @param authenticationURL The new authentication URL
	 */
	public abstract void setAuthenticationURL(String authenticationURL);

	/**
	 * @return the useETag
	 */
	public abstract boolean getUseETag();

	/**
	 * @param useETag the useETag to set
	 */
	public abstract void setUseETag(boolean useETag);

	public abstract void setUserAgent(String userAgent);

	public abstract String getUserAgent();

	/**
	 * @return the cdnManagementURL
	 */
	public abstract String getCdnManagementURL();

	/**
	 * @param config
	 */
	public abstract boolean updateObjectManifest(String container,
												 String object, String manifest) throws FilesAuthorizationException,
			HttpException, IOException, FilesInvalidNameException;

	/**
	 * @param config
	 */
	public abstract boolean updateObjectMetadata(String container,
												 String object, Map<String, String> metadata)
			throws FilesAuthorizationException, HttpException, IOException,
			FilesInvalidNameException;

	/**
	 * @param config
	 */
	public abstract boolean updateObjectMetadataAndManifest(String container,
															String object, Map<String, String> metadata, String manifest)
			throws FilesAuthorizationException, HttpException, IOException,
			FilesInvalidNameException;

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
	public abstract void useSnet();

	public abstract void usePublic();

	public abstract boolean usingSnet();

}