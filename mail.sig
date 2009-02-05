//Java signature file
//date: Wed Feb 04 18:07:20 PST 2009
//classpath: mail/target/mail.jar
//package: javax


package javax.mail;

public abstract class Address implements java.io.Serializable {
	public Address();
	public abstract boolean equals(Object);
	public abstract String getType();
	public abstract String toString();
}

public class AuthenticationFailedException extends javax.mail.MessagingException {
	public AuthenticationFailedException();
	public AuthenticationFailedException(String);
}

public abstract class Authenticator {
	public Authenticator();
	protected final String getDefaultUserName();
	protected javax.mail.PasswordAuthentication getPasswordAuthentication();
	protected final int getRequestingPort();
	protected final String getRequestingPrompt();
	protected final String getRequestingProtocol();
	protected final java.net.InetAddress getRequestingSite();
}

public abstract class BodyPart implements javax.mail.Part {
	public BodyPart();
	public javax.mail.Multipart getParent();
	protected javax.mail.Multipart parent;
}

public class FetchProfile {
	public FetchProfile();
	public void add(String);
	public void add(javax.mail.FetchProfile.Item);
	public boolean contains(String);
	public boolean contains(javax.mail.FetchProfile.Item);
	public String[] getHeaderNames();
	public javax.mail.FetchProfile.Item[] getItems();

	public static class Item {
		protected Item(String);
		public String toString();
		public static final javax.mail.FetchProfile.Item CONTENT_INFO;
		public static final javax.mail.FetchProfile.Item ENVELOPE;
		public static final javax.mail.FetchProfile.Item FLAGS;
	}
}

public class Flags implements Cloneable, java.io.Serializable {
	public Flags();
	public Flags(String);
	public Flags(javax.mail.Flags);
	public Flags(javax.mail.Flags.Flag);
	public void add(String);
	public void add(javax.mail.Flags);
	public void add(javax.mail.Flags.Flag);
	public Object clone();
	public boolean contains(String);
	public boolean contains(javax.mail.Flags);
	public boolean contains(javax.mail.Flags.Flag);
	public boolean equals(Object);
	public javax.mail.Flags.Flag[] getSystemFlags();
	public String[] getUserFlags();
	public int hashCode();
	public void remove(String);
	public void remove(javax.mail.Flags);
	public void remove(javax.mail.Flags.Flag);

	public static final class Flag {
		public static final javax.mail.Flags.Flag ANSWERED;
		public static final javax.mail.Flags.Flag DELETED;
		public static final javax.mail.Flags.Flag DRAFT;
		public static final javax.mail.Flags.Flag FLAGGED;
		public static final javax.mail.Flags.Flag RECENT;
		public static final javax.mail.Flags.Flag SEEN;
		public static final javax.mail.Flags.Flag USER;
	}
}

public abstract class Folder {
	protected Folder(javax.mail.Store);
	public synchronized void addConnectionListener(javax.mail.event.ConnectionListener);
	public synchronized void addFolderListener(javax.mail.event.FolderListener);
	public synchronized void addMessageChangedListener(javax.mail.event.MessageChangedListener);
	public synchronized void addMessageCountListener(javax.mail.event.MessageCountListener);
	public abstract void appendMessages(javax.mail.Message[]) throws javax.mail.MessagingException;
	public abstract void close(boolean) throws javax.mail.MessagingException;
	public void copyMessages(javax.mail.Message[], javax.mail.Folder) throws javax.mail.MessagingException;
	public abstract boolean create(int) throws javax.mail.MessagingException;
	public abstract boolean delete(boolean) throws javax.mail.MessagingException;
	public abstract boolean exists() throws javax.mail.MessagingException;
	public abstract javax.mail.Message[] expunge() throws javax.mail.MessagingException;
	public void fetch(javax.mail.Message[], javax.mail.FetchProfile) throws javax.mail.MessagingException;
	protected void finalize() throws Throwable;
	public synchronized int getDeletedMessageCount() throws javax.mail.MessagingException;
	public abstract javax.mail.Folder getFolder(String) throws javax.mail.MessagingException;
	public abstract String getFullName();
	public abstract javax.mail.Message getMessage(int) throws javax.mail.MessagingException;
	public abstract int getMessageCount() throws javax.mail.MessagingException;
	public synchronized javax.mail.Message[] getMessages() throws javax.mail.MessagingException;
	public synchronized javax.mail.Message[] getMessages(int, int) throws javax.mail.MessagingException;
	public synchronized javax.mail.Message[] getMessages(int[]) throws javax.mail.MessagingException;
	public int getMode();
	public abstract String getName();
	public synchronized int getNewMessageCount() throws javax.mail.MessagingException;
	public abstract javax.mail.Folder getParent() throws javax.mail.MessagingException;
	public abstract javax.mail.Flags getPermanentFlags();
	public abstract char getSeparator() throws javax.mail.MessagingException;
	public javax.mail.Store getStore();
	public abstract int getType() throws javax.mail.MessagingException;
	public javax.mail.URLName getURLName() throws javax.mail.MessagingException;
	public synchronized int getUnreadMessageCount() throws javax.mail.MessagingException;
	public abstract boolean hasNewMessages() throws javax.mail.MessagingException;
	public abstract boolean isOpen();
	public boolean isSubscribed();
	public javax.mail.Folder[] list() throws javax.mail.MessagingException;
	public abstract javax.mail.Folder[] list(String) throws javax.mail.MessagingException;
	public javax.mail.Folder[] listSubscribed() throws javax.mail.MessagingException;
	public javax.mail.Folder[] listSubscribed(String) throws javax.mail.MessagingException;
	protected void notifyConnectionListeners(int);
	protected void notifyFolderListeners(int);
	protected void notifyFolderRenamedListeners(javax.mail.Folder);
	protected void notifyMessageAddedListeners(javax.mail.Message[]);
	protected void notifyMessageChangedListeners(int, javax.mail.Message);
	protected void notifyMessageRemovedListeners(boolean, javax.mail.Message[]);
	public abstract void open(int) throws javax.mail.MessagingException;
	public synchronized void removeConnectionListener(javax.mail.event.ConnectionListener);
	public synchronized void removeFolderListener(javax.mail.event.FolderListener);
	public synchronized void removeMessageChangedListener(javax.mail.event.MessageChangedListener);
	public synchronized void removeMessageCountListener(javax.mail.event.MessageCountListener);
	public abstract boolean renameTo(javax.mail.Folder) throws javax.mail.MessagingException;
	public javax.mail.Message[] search(javax.mail.search.SearchTerm) throws javax.mail.MessagingException;
	public javax.mail.Message[] search(javax.mail.search.SearchTerm, javax.mail.Message[]) throws javax.mail.MessagingException;
	public synchronized void setFlags(int, int, javax.mail.Flags, boolean) throws javax.mail.MessagingException;
	public synchronized void setFlags(int[], javax.mail.Flags, boolean) throws javax.mail.MessagingException;
	public synchronized void setFlags(javax.mail.Message[], javax.mail.Flags, boolean) throws javax.mail.MessagingException;
	public void setSubscribed(boolean) throws javax.mail.MessagingException;
	public String toString();
	public static final int HOLDS_FOLDERS=2;
	public static final int HOLDS_MESSAGES=1;
	public static final int READ_ONLY=1;
	public static final int READ_WRITE=2;
	protected int mode;
	protected javax.mail.Store store;
}

public class FolderClosedException extends javax.mail.MessagingException {
	public FolderClosedException(javax.mail.Folder);
	public FolderClosedException(javax.mail.Folder, String);
	public javax.mail.Folder getFolder();
}

public class FolderNotFoundException extends javax.mail.MessagingException {
	public FolderNotFoundException();
	public FolderNotFoundException(String, javax.mail.Folder);
	public FolderNotFoundException(javax.mail.Folder);
	public FolderNotFoundException(javax.mail.Folder, String);
	public javax.mail.Folder getFolder();
}

public class Header {
	public Header(String, String);
	public String getName();
	public String getValue();
	protected String name;
	protected String value;
}

public class IllegalWriteException extends javax.mail.MessagingException {
	public IllegalWriteException();
	public IllegalWriteException(String);
}

public abstract class Message implements javax.mail.Part {
	protected Message();
	protected Message(javax.mail.Folder, int);
	protected Message(javax.mail.Session);
	public abstract void addFrom(javax.mail.Address[]) throws javax.mail.MessagingException;
	public void addRecipient(javax.mail.Message.RecipientType, javax.mail.Address) throws javax.mail.MessagingException;
	public abstract void addRecipients(javax.mail.Message.RecipientType, javax.mail.Address[]) throws javax.mail.MessagingException;
	public javax.mail.Address[] getAllRecipients() throws javax.mail.MessagingException;
	public abstract javax.mail.Flags getFlags() throws javax.mail.MessagingException;
	public javax.mail.Folder getFolder();
	public abstract javax.mail.Address[] getFrom() throws javax.mail.MessagingException;
	public int getMessageNumber();
	public abstract java.util.Date getReceivedDate() throws javax.mail.MessagingException;
	public abstract javax.mail.Address[] getRecipients(javax.mail.Message.RecipientType) throws javax.mail.MessagingException;
	public javax.mail.Address[] getReplyTo() throws javax.mail.MessagingException;
	public abstract java.util.Date getSentDate() throws javax.mail.MessagingException;
	public abstract String getSubject() throws javax.mail.MessagingException;
	public boolean isExpunged();
	public boolean isSet(javax.mail.Flags.Flag) throws javax.mail.MessagingException;
	public boolean match(javax.mail.search.SearchTerm) throws javax.mail.MessagingException;
	public abstract javax.mail.Message reply(boolean) throws javax.mail.MessagingException;
	public abstract void saveChanges() throws javax.mail.MessagingException;
	protected void setExpunged(boolean);
	public void setFlag(javax.mail.Flags.Flag, boolean) throws javax.mail.MessagingException;
	public abstract void setFlags(javax.mail.Flags, boolean) throws javax.mail.MessagingException;
	public abstract void setFrom() throws javax.mail.MessagingException;
	public abstract void setFrom(javax.mail.Address) throws javax.mail.MessagingException;
	protected void setMessageNumber(int);
	public void setRecipient(javax.mail.Message.RecipientType, javax.mail.Address) throws javax.mail.MessagingException;
	public abstract void setRecipients(javax.mail.Message.RecipientType, javax.mail.Address[]) throws javax.mail.MessagingException;
	public void setReplyTo(javax.mail.Address[]) throws javax.mail.MessagingException;
	public abstract void setSentDate(java.util.Date) throws javax.mail.MessagingException;
	public abstract void setSubject(String) throws javax.mail.MessagingException;
	protected boolean expunged;
	protected javax.mail.Folder folder;
	protected int msgnum;
	protected javax.mail.Session session;

	public static class RecipientType implements java.io.Serializable {
		protected RecipientType(String);
		protected Object readResolve() throws java.io.ObjectStreamException;
		public String toString();
		public static final javax.mail.Message.RecipientType BCC;
		public static final javax.mail.Message.RecipientType CC;
		public static final javax.mail.Message.RecipientType TO;
		protected String type;
	}
}

public abstract interface MessageAware {
	public abstract javax.mail.MessageContext getMessageContext();
}

public class MessageContext {
	public MessageContext(javax.mail.Part);
	public javax.mail.Message getMessage();
	public javax.mail.Part getPart();
	public javax.mail.Session getSession();
}

public class MessageRemovedException extends javax.mail.MessagingException {
	public MessageRemovedException();
	public MessageRemovedException(String);
}

public class MessagingException extends Exception {
	public MessagingException();
	public MessagingException(String);
	public MessagingException(String, Exception);
	public synchronized Throwable getCause();
	public synchronized Exception getNextException();
	public synchronized boolean setNextException(Exception);
	public synchronized String toString();
}

public class MethodNotSupportedException extends javax.mail.MessagingException {
	public MethodNotSupportedException();
	public MethodNotSupportedException(String);
}

public abstract class Multipart {
	protected Multipart();
	public synchronized void addBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException;
	public synchronized void addBodyPart(javax.mail.BodyPart, int) throws javax.mail.MessagingException;
	public synchronized javax.mail.BodyPart getBodyPart(int) throws javax.mail.MessagingException;
	public String getContentType();
	public synchronized int getCount() throws javax.mail.MessagingException;
	public synchronized javax.mail.Part getParent();
	public synchronized void removeBodyPart(int) throws javax.mail.MessagingException;
	public synchronized boolean removeBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException;
	protected synchronized void setMultipartDataSource(javax.mail.MultipartDataSource) throws javax.mail.MessagingException;
	public synchronized void setParent(javax.mail.Part);
	public abstract void writeTo(java.io.OutputStream) throws java.io.IOException, javax.mail.MessagingException;
	protected String contentType;
	protected javax.mail.Part parent;
	protected java.util.Vector parts;
}

public abstract interface MultipartDataSource extends javax.activation.DataSource {
	public abstract javax.mail.BodyPart getBodyPart(int) throws javax.mail.MessagingException;
	public abstract int getCount();
}

public class NoSuchProviderException extends javax.mail.MessagingException {
	public NoSuchProviderException();
	public NoSuchProviderException(String);
}

public abstract interface Part {
	public abstract void addHeader(String, String) throws javax.mail.MessagingException;
	public abstract java.util.Enumeration getAllHeaders() throws javax.mail.MessagingException;
	public abstract Object getContent() throws java.io.IOException, javax.mail.MessagingException;
	public abstract String getContentType() throws javax.mail.MessagingException;
	public abstract javax.activation.DataHandler getDataHandler() throws javax.mail.MessagingException;
	public abstract String getDescription() throws javax.mail.MessagingException;
	public abstract String getDisposition() throws javax.mail.MessagingException;
	public abstract String getFileName() throws javax.mail.MessagingException;
	public abstract String[] getHeader(String) throws javax.mail.MessagingException;
	public abstract java.io.InputStream getInputStream() throws java.io.IOException, javax.mail.MessagingException;
	public abstract int getLineCount() throws javax.mail.MessagingException;
	public abstract java.util.Enumeration getMatchingHeaders(String[]) throws javax.mail.MessagingException;
	public abstract java.util.Enumeration getNonMatchingHeaders(String[]) throws javax.mail.MessagingException;
	public abstract int getSize() throws javax.mail.MessagingException;
	public abstract boolean isMimeType(String) throws javax.mail.MessagingException;
	public abstract void removeHeader(String) throws javax.mail.MessagingException;
	public abstract void setContent(Object, String) throws javax.mail.MessagingException;
	public abstract void setContent(javax.mail.Multipart) throws javax.mail.MessagingException;
	public abstract void setDataHandler(javax.activation.DataHandler) throws javax.mail.MessagingException;
	public abstract void setDescription(String) throws javax.mail.MessagingException;
	public abstract void setDisposition(String) throws javax.mail.MessagingException;
	public abstract void setFileName(String) throws javax.mail.MessagingException;
	public abstract void setHeader(String, String) throws javax.mail.MessagingException;
	public abstract void setText(String) throws javax.mail.MessagingException;
	public abstract void writeTo(java.io.OutputStream) throws java.io.IOException, javax.mail.MessagingException;
	public static final String ATTACHMENT="attachment";
	public static final String INLINE="inline";
}

public final class PasswordAuthentication {
	public PasswordAuthentication(String, String);
	public String getPassword();
	public String getUserName();
}

public class Provider {
	public Provider(javax.mail.Provider.Type, String, String, String, String);
	public String getClassName();
	public String getProtocol();
	public javax.mail.Provider.Type getType();
	public String getVendor();
	public String getVersion();
	public String toString();

	public static class Type {
		public String toString();
		public static final javax.mail.Provider.Type STORE;
		public static final javax.mail.Provider.Type TRANSPORT;
	}
}

public class Quota {
	public Quota(String);
	public void setResourceLimit(String, long);
	public String quotaRoot;
	public javax.mail.Quota.Resource[] resources;

	public static class Resource {
		public Resource(String, long, long);
		public long limit;
		public String name;
		public long usage;
	}
}

public abstract interface QuotaAwareStore {
	public abstract javax.mail.Quota[] getQuota(String) throws javax.mail.MessagingException;
	public abstract void setQuota(javax.mail.Quota) throws javax.mail.MessagingException;
}

public class ReadOnlyFolderException extends javax.mail.MessagingException {
	public ReadOnlyFolderException(javax.mail.Folder);
	public ReadOnlyFolderException(javax.mail.Folder, String);
	public javax.mail.Folder getFolder();
}

public class SendFailedException extends javax.mail.MessagingException {
	public SendFailedException();
	public SendFailedException(String);
	public SendFailedException(String, Exception);
	public SendFailedException(String, Exception, javax.mail.Address[], javax.mail.Address[], javax.mail.Address[]);
	public javax.mail.Address[] getInvalidAddresses();
	public javax.mail.Address[] getValidSentAddresses();
	public javax.mail.Address[] getValidUnsentAddresses();
	protected transient javax.mail.Address[] invalid;
	protected transient javax.mail.Address[] validSent;
	protected transient javax.mail.Address[] validUnsent;
}

public abstract class Service {
	protected Service(javax.mail.Session, javax.mail.URLName);
	public void addConnectionListener(javax.mail.event.ConnectionListener);
	public synchronized void close() throws javax.mail.MessagingException;
	public void connect() throws javax.mail.MessagingException;
	public synchronized void connect(String, int, String, String) throws javax.mail.MessagingException;
	public void connect(String, String) throws javax.mail.MessagingException;
	public void connect(String, String, String) throws javax.mail.MessagingException;
	protected void finalize() throws Throwable;
	public synchronized javax.mail.URLName getURLName();
	public synchronized boolean isConnected();
	protected void notifyConnectionListeners(int);
	protected boolean protocolConnect(String, int, String, String) throws javax.mail.MessagingException;
	protected void queueEvent(javax.mail.event.MailEvent, java.util.Vector);
	public void removeConnectionListener(javax.mail.event.ConnectionListener);
	protected synchronized void setConnected(boolean);
	protected synchronized void setURLName(javax.mail.URLName);
	public String toString();
	protected boolean debug;
	protected javax.mail.Session session;
	protected javax.mail.URLName url;
}

public final class Session {
	public synchronized void addProvider(javax.mail.Provider);
	public synchronized boolean getDebug();
	public synchronized java.io.PrintStream getDebugOut();
	public static javax.mail.Session getDefaultInstance(java.util.Properties);
	public static synchronized javax.mail.Session getDefaultInstance(java.util.Properties, javax.mail.Authenticator);
	public javax.mail.Folder getFolder(javax.mail.URLName) throws javax.mail.MessagingException;
	public static javax.mail.Session getInstance(java.util.Properties);
	public static javax.mail.Session getInstance(java.util.Properties, javax.mail.Authenticator);
	public javax.mail.PasswordAuthentication getPasswordAuthentication(javax.mail.URLName);
	public java.util.Properties getProperties();
	public String getProperty(String);
	public synchronized javax.mail.Provider getProvider(String) throws javax.mail.NoSuchProviderException;
	public synchronized javax.mail.Provider[] getProviders();
	public javax.mail.Store getStore() throws javax.mail.NoSuchProviderException;
	public javax.mail.Store getStore(String) throws javax.mail.NoSuchProviderException;
	public javax.mail.Store getStore(javax.mail.Provider) throws javax.mail.NoSuchProviderException;
	public javax.mail.Store getStore(javax.mail.URLName) throws javax.mail.NoSuchProviderException;
	public javax.mail.Transport getTransport() throws javax.mail.NoSuchProviderException;
	public javax.mail.Transport getTransport(String) throws javax.mail.NoSuchProviderException;
	public javax.mail.Transport getTransport(javax.mail.Address) throws javax.mail.NoSuchProviderException;
	public javax.mail.Transport getTransport(javax.mail.Provider) throws javax.mail.NoSuchProviderException;
	public javax.mail.Transport getTransport(javax.mail.URLName) throws javax.mail.NoSuchProviderException;
	public javax.mail.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress, int, String, String, String);
	public synchronized void setDebug(boolean);
	public synchronized void setDebugOut(java.io.PrintStream);
	public void setPasswordAuthentication(javax.mail.URLName, javax.mail.PasswordAuthentication);
	public synchronized void setProtocolForAddress(String, String);
	public synchronized void setProvider(javax.mail.Provider) throws javax.mail.NoSuchProviderException;
}

public abstract class Store extends javax.mail.Service {
	protected Store(javax.mail.Session, javax.mail.URLName);
	public synchronized void addFolderListener(javax.mail.event.FolderListener);
	public synchronized void addStoreListener(javax.mail.event.StoreListener);
	public abstract javax.mail.Folder getDefaultFolder() throws javax.mail.MessagingException;
	public abstract javax.mail.Folder getFolder(String) throws javax.mail.MessagingException;
	public abstract javax.mail.Folder getFolder(javax.mail.URLName) throws javax.mail.MessagingException;
	public javax.mail.Folder[] getPersonalNamespaces() throws javax.mail.MessagingException;
	public javax.mail.Folder[] getSharedNamespaces() throws javax.mail.MessagingException;
	public javax.mail.Folder[] getUserNamespaces(String) throws javax.mail.MessagingException;
	protected void notifyFolderListeners(int, javax.mail.Folder);
	protected void notifyFolderRenamedListeners(javax.mail.Folder, javax.mail.Folder);
	protected void notifyStoreListeners(int, String);
	public synchronized void removeFolderListener(javax.mail.event.FolderListener);
	public synchronized void removeStoreListener(javax.mail.event.StoreListener);
}

public class StoreClosedException extends javax.mail.MessagingException {
	public StoreClosedException(javax.mail.Store);
	public StoreClosedException(javax.mail.Store, String);
	public javax.mail.Store getStore();
}

public abstract class Transport extends javax.mail.Service {
	public Transport(javax.mail.Session, javax.mail.URLName);
	public synchronized void addTransportListener(javax.mail.event.TransportListener);
	protected void notifyTransportListeners(int, javax.mail.Address[], javax.mail.Address[], javax.mail.Address[], javax.mail.Message);
	public synchronized void removeTransportListener(javax.mail.event.TransportListener);
	public static void send(javax.mail.Message) throws javax.mail.MessagingException;
	public static void send(javax.mail.Message, javax.mail.Address[]) throws javax.mail.MessagingException;
	public abstract void sendMessage(javax.mail.Message, javax.mail.Address[]) throws javax.mail.MessagingException;
}

public abstract interface UIDFolder {
	public abstract javax.mail.Message getMessageByUID(long) throws javax.mail.MessagingException;
	public abstract javax.mail.Message[] getMessagesByUID(long, long) throws javax.mail.MessagingException;
	public abstract javax.mail.Message[] getMessagesByUID(long[]) throws javax.mail.MessagingException;
	public abstract long getUID(javax.mail.Message) throws javax.mail.MessagingException;
	public abstract long getUIDValidity() throws javax.mail.MessagingException;
	public static final long LASTUID=-1L;

	public static class FetchProfileItem extends javax.mail.FetchProfile.Item {
		protected FetchProfileItem(String);
		public static final javax.mail.UIDFolder.FetchProfileItem UID;
	}
}

public class URLName {
	public URLName(String);
	public URLName(String, String, int, String, String, String);
	public URLName(java.net.URL);
	public boolean equals(Object);
	public String getFile();
	public String getHost();
	public String getPassword();
	public int getPort();
	public String getProtocol();
	public String getRef();
	public java.net.URL getURL() throws java.net.MalformedURLException;
	public String getUsername();
	public int hashCode();
	protected void parseString(String);
	public String toString();
	protected String fullURL;
}

package javax.mail.event;

public abstract class ConnectionAdapter implements javax.mail.event.ConnectionListener {
	public ConnectionAdapter();
	public void closed(javax.mail.event.ConnectionEvent);
	public void disconnected(javax.mail.event.ConnectionEvent);
	public void opened(javax.mail.event.ConnectionEvent);
}

public class ConnectionEvent extends javax.mail.event.MailEvent {
	public ConnectionEvent(Object, int);
	public void dispatch(Object);
	public int getType();
	public static final int CLOSED=3;
	public static final int DISCONNECTED=2;
	public static final int OPENED=1;
	protected int type;
}

public abstract interface ConnectionListener extends java.util.EventListener {
	public abstract void closed(javax.mail.event.ConnectionEvent);
	public abstract void disconnected(javax.mail.event.ConnectionEvent);
	public abstract void opened(javax.mail.event.ConnectionEvent);
}

public abstract class FolderAdapter implements javax.mail.event.FolderListener {
	public FolderAdapter();
	public void folderCreated(javax.mail.event.FolderEvent);
	public void folderDeleted(javax.mail.event.FolderEvent);
	public void folderRenamed(javax.mail.event.FolderEvent);
}

public class FolderEvent extends javax.mail.event.MailEvent {
	public FolderEvent(Object, javax.mail.Folder, int);
	public FolderEvent(Object, javax.mail.Folder, javax.mail.Folder, int);
	public void dispatch(Object);
	public javax.mail.Folder getFolder();
	public javax.mail.Folder getNewFolder();
	public int getType();
	public static final int CREATED=1;
	public static final int DELETED=2;
	public static final int RENAMED=3;
	protected transient javax.mail.Folder folder;
	protected transient javax.mail.Folder newFolder;
	protected int type;
}

public abstract interface FolderListener extends java.util.EventListener {
	public abstract void folderCreated(javax.mail.event.FolderEvent);
	public abstract void folderDeleted(javax.mail.event.FolderEvent);
	public abstract void folderRenamed(javax.mail.event.FolderEvent);
}

public abstract class MailEvent extends java.util.EventObject {
	public MailEvent(Object);
	public abstract void dispatch(Object);
}

public class MessageChangedEvent extends javax.mail.event.MailEvent {
	public MessageChangedEvent(Object, int, javax.mail.Message);
	public void dispatch(Object);
	public javax.mail.Message getMessage();
	public int getMessageChangeType();
	public static final int ENVELOPE_CHANGED=2;
	public static final int FLAGS_CHANGED=1;
	protected transient javax.mail.Message msg;
	protected int type;
}

public abstract interface MessageChangedListener extends java.util.EventListener {
	public abstract void messageChanged(javax.mail.event.MessageChangedEvent);
}

public abstract class MessageCountAdapter implements javax.mail.event.MessageCountListener {
	public MessageCountAdapter();
	public void messagesAdded(javax.mail.event.MessageCountEvent);
	public void messagesRemoved(javax.mail.event.MessageCountEvent);
}

public class MessageCountEvent extends javax.mail.event.MailEvent {
	public MessageCountEvent(javax.mail.Folder, int, boolean, javax.mail.Message[]);
	public void dispatch(Object);
	public javax.mail.Message[] getMessages();
	public int getType();
	public boolean isRemoved();
	public static final int ADDED=1;
	public static final int REMOVED=2;
	protected transient javax.mail.Message[] msgs;
	protected boolean removed;
	protected int type;
}

public abstract interface MessageCountListener extends java.util.EventListener {
	public abstract void messagesAdded(javax.mail.event.MessageCountEvent);
	public abstract void messagesRemoved(javax.mail.event.MessageCountEvent);
}

public class StoreEvent extends javax.mail.event.MailEvent {
	public StoreEvent(javax.mail.Store, int, String);
	public void dispatch(Object);
	public String getMessage();
	public int getMessageType();
	public static final int ALERT=1;
	public static final int NOTICE=2;
	protected String message;
	protected int type;
}

public abstract interface StoreListener extends java.util.EventListener {
	public abstract void notification(javax.mail.event.StoreEvent);
}

public abstract class TransportAdapter implements javax.mail.event.TransportListener {
	public TransportAdapter();
	public void messageDelivered(javax.mail.event.TransportEvent);
	public void messageNotDelivered(javax.mail.event.TransportEvent);
	public void messagePartiallyDelivered(javax.mail.event.TransportEvent);
}

public class TransportEvent extends javax.mail.event.MailEvent {
	public TransportEvent(javax.mail.Transport, int, javax.mail.Address[], javax.mail.Address[], javax.mail.Address[], javax.mail.Message);
	public void dispatch(Object);
	public javax.mail.Address[] getInvalidAddresses();
	public javax.mail.Message getMessage();
	public int getType();
	public javax.mail.Address[] getValidSentAddresses();
	public javax.mail.Address[] getValidUnsentAddresses();
	public static final int MESSAGE_DELIVERED=1;
	public static final int MESSAGE_NOT_DELIVERED=2;
	public static final int MESSAGE_PARTIALLY_DELIVERED=3;
	protected transient javax.mail.Address[] invalid;
	protected transient javax.mail.Message msg;
	protected int type;
	protected transient javax.mail.Address[] validSent;
	protected transient javax.mail.Address[] validUnsent;
}

public abstract interface TransportListener extends java.util.EventListener {
	public abstract void messageDelivered(javax.mail.event.TransportEvent);
	public abstract void messageNotDelivered(javax.mail.event.TransportEvent);
	public abstract void messagePartiallyDelivered(javax.mail.event.TransportEvent);
}

package javax.mail.internet;

public class AddressException extends javax.mail.internet.ParseException {
	public AddressException();
	public AddressException(String);
	public AddressException(String, String);
	public AddressException(String, String, int);
	public int getPos();
	public String getRef();
	public String toString();
	protected int pos;
	protected String ref;
}

public class ContentDisposition {
	public ContentDisposition();
	public ContentDisposition(String) throws javax.mail.internet.ParseException;
	public ContentDisposition(String, javax.mail.internet.ParameterList);
	public String getDisposition();
	public String getParameter(String);
	public javax.mail.internet.ParameterList getParameterList();
	public void setDisposition(String);
	public void setParameter(String, String);
	public void setParameterList(javax.mail.internet.ParameterList);
	public String toString();
}

public class ContentType {
	public ContentType();
	public ContentType(String) throws javax.mail.internet.ParseException;
	public ContentType(String, String, javax.mail.internet.ParameterList);
	public String getBaseType();
	public String getParameter(String);
	public javax.mail.internet.ParameterList getParameterList();
	public String getPrimaryType();
	public String getSubType();
	public boolean match(String);
	public boolean match(javax.mail.internet.ContentType);
	public void setParameter(String, String);
	public void setParameterList(javax.mail.internet.ParameterList);
	public void setPrimaryType(String);
	public void setSubType(String);
	public String toString();
}

public class HeaderTokenizer {
	public HeaderTokenizer(String);
	public HeaderTokenizer(String, String);
	public HeaderTokenizer(String, String, boolean);
	public String getRemainder();
	public javax.mail.internet.HeaderTokenizer.Token next() throws javax.mail.internet.ParseException;
	public javax.mail.internet.HeaderTokenizer.Token peek() throws javax.mail.internet.ParseException;
	public static final String MIME="()<>@,;:\\\"\t []/?=";
	public static final String RFC822="()<>@,;:\\\"\t .[]";

	public static class Token {
		public Token(int, String);
		public int getType();
		public String getValue();
		public static final int ATOM=-1;
		public static final int COMMENT=-3;
		public static final int EOF=-4;
		public static final int QUOTEDSTRING=-2;
	}
}

public class InternetAddress extends javax.mail.Address implements Cloneable {
	public InternetAddress();
	public InternetAddress(String) throws javax.mail.internet.AddressException;
	public InternetAddress(String, boolean) throws javax.mail.internet.AddressException;
	public InternetAddress(String, String) throws java.io.UnsupportedEncodingException;
	public InternetAddress(String, String, String) throws java.io.UnsupportedEncodingException;
	public Object clone();
	public boolean equals(Object);
	public String getAddress();
	public javax.mail.internet.InternetAddress[] getGroup(boolean) throws javax.mail.internet.AddressException;
	public static javax.mail.internet.InternetAddress getLocalAddress(javax.mail.Session);
	public String getPersonal();
	public String getType();
	public int hashCode();
	public boolean isGroup();
	public static javax.mail.internet.InternetAddress[] parse(String) throws javax.mail.internet.AddressException;
	public static javax.mail.internet.InternetAddress[] parse(String, boolean) throws javax.mail.internet.AddressException;
	public static javax.mail.internet.InternetAddress[] parseHeader(String, boolean) throws javax.mail.internet.AddressException;
	public void setAddress(String);
	public void setPersonal(String) throws java.io.UnsupportedEncodingException;
	public void setPersonal(String, String) throws java.io.UnsupportedEncodingException;
	public String toString();
	public static String toString(javax.mail.Address[]);
	public static String toString(javax.mail.Address[], int);
	public String toUnicodeString();
	public void validate() throws javax.mail.internet.AddressException;
	protected String address;
	protected String encodedPersonal;
	protected String personal;
}

public class InternetHeaders {
	public InternetHeaders();
	public InternetHeaders(java.io.InputStream) throws javax.mail.MessagingException;
	public void addHeader(String, String);
	public void addHeaderLine(String);
	public java.util.Enumeration getAllHeaderLines();
	public java.util.Enumeration getAllHeaders();
	public String[] getHeader(String);
	public String getHeader(String, String);
	public java.util.Enumeration getMatchingHeaderLines(String[]);
	public java.util.Enumeration getMatchingHeaders(String[]);
	public java.util.Enumeration getNonMatchingHeaderLines(String[]);
	public java.util.Enumeration getNonMatchingHeaders(String[]);
	public void load(java.io.InputStream) throws javax.mail.MessagingException;
	public void removeHeader(String);
	public void setHeader(String, String);
	protected java.util.List headers;

	protected static final class InternetHeader extends javax.mail.Header {
		public InternetHeader(String);
		public InternetHeader(String, String);
		public String getValue();
	}
}

public class MailDateFormat extends java.text.SimpleDateFormat {
	public MailDateFormat();
	public StringBuffer format(java.util.Date, StringBuffer, java.text.FieldPosition);
	public java.util.Date parse(String, java.text.ParsePosition);
	public void setCalendar(java.util.Calendar);
	public void setNumberFormat(java.text.NumberFormat);
}

public class MimeBodyPart extends javax.mail.BodyPart implements javax.mail.internet.MimePart {
	public MimeBodyPart();
	public MimeBodyPart(java.io.InputStream) throws javax.mail.MessagingException;
	public MimeBodyPart(javax.mail.internet.InternetHeaders, byte[]) throws javax.mail.MessagingException;
	public void addHeader(String, String) throws javax.mail.MessagingException;
	public void addHeaderLine(String) throws javax.mail.MessagingException;
	public void attachFile(java.io.File) throws java.io.IOException, javax.mail.MessagingException;
	public void attachFile(String) throws java.io.IOException, javax.mail.MessagingException;
	public java.util.Enumeration getAllHeaderLines() throws javax.mail.MessagingException;
	public java.util.Enumeration getAllHeaders() throws javax.mail.MessagingException;
	public Object getContent() throws java.io.IOException, javax.mail.MessagingException;
	public String getContentID() throws javax.mail.MessagingException;
	public String[] getContentLanguage() throws javax.mail.MessagingException;
	public String getContentMD5() throws javax.mail.MessagingException;
	protected java.io.InputStream getContentStream() throws javax.mail.MessagingException;
	public String getContentType() throws javax.mail.MessagingException;
	public javax.activation.DataHandler getDataHandler() throws javax.mail.MessagingException;
	public String getDescription() throws javax.mail.MessagingException;
	public String getDisposition() throws javax.mail.MessagingException;
	public String getEncoding() throws javax.mail.MessagingException;
	public String getFileName() throws javax.mail.MessagingException;
	public String[] getHeader(String) throws javax.mail.MessagingException;
	public String getHeader(String, String) throws javax.mail.MessagingException;
	public java.io.InputStream getInputStream() throws java.io.IOException, javax.mail.MessagingException;
	public int getLineCount() throws javax.mail.MessagingException;
	public java.util.Enumeration getMatchingHeaderLines(String[]) throws javax.mail.MessagingException;
	public java.util.Enumeration getMatchingHeaders(String[]) throws javax.mail.MessagingException;
	public java.util.Enumeration getNonMatchingHeaderLines(String[]) throws javax.mail.MessagingException;
	public java.util.Enumeration getNonMatchingHeaders(String[]) throws javax.mail.MessagingException;
	public java.io.InputStream getRawInputStream() throws javax.mail.MessagingException;
	public int getSize() throws javax.mail.MessagingException;
	public boolean isMimeType(String) throws javax.mail.MessagingException;
	public void removeHeader(String) throws javax.mail.MessagingException;
	public void saveFile(java.io.File) throws java.io.IOException, javax.mail.MessagingException;
	public void saveFile(String) throws java.io.IOException, javax.mail.MessagingException;
	public void setContent(Object, String) throws javax.mail.MessagingException;
	public void setContent(javax.mail.Multipart) throws javax.mail.MessagingException;
	public void setContentID(String) throws javax.mail.MessagingException;
	public void setContentLanguage(String[]) throws javax.mail.MessagingException;
	public void setContentMD5(String) throws javax.mail.MessagingException;
	public void setDataHandler(javax.activation.DataHandler) throws javax.mail.MessagingException;
	public void setDescription(String) throws javax.mail.MessagingException;
	public void setDescription(String, String) throws javax.mail.MessagingException;
	public void setDisposition(String) throws javax.mail.MessagingException;
	public void setFileName(String) throws javax.mail.MessagingException;
	public void setHeader(String, String) throws javax.mail.MessagingException;
	public void setText(String) throws javax.mail.MessagingException;
	public void setText(String, String) throws javax.mail.MessagingException;
	public void setText(String, String, String) throws javax.mail.MessagingException;
	protected void updateHeaders() throws javax.mail.MessagingException;
	public void writeTo(java.io.OutputStream) throws java.io.IOException, javax.mail.MessagingException;
	protected byte[] content;
	protected java.io.InputStream contentStream;
	protected javax.activation.DataHandler dh;
	protected javax.mail.internet.InternetHeaders headers;
}

public class MimeMessage extends javax.mail.Message implements javax.mail.internet.MimePart {
	protected MimeMessage(javax.mail.Folder, int);
	protected MimeMessage(javax.mail.Folder, java.io.InputStream, int) throws javax.mail.MessagingException;
	protected MimeMessage(javax.mail.Folder, javax.mail.internet.InternetHeaders, byte[], int) throws javax.mail.MessagingException;
	public MimeMessage(javax.mail.Session);
	public MimeMessage(javax.mail.Session, java.io.InputStream) throws javax.mail.MessagingException;
	public MimeMessage(javax.mail.internet.MimeMessage) throws javax.mail.MessagingException;
	public void addFrom(javax.mail.Address[]) throws javax.mail.MessagingException;
	public void addHeader(String, String) throws javax.mail.MessagingException;
	public void addHeaderLine(String) throws javax.mail.MessagingException;
	public void addRecipients(javax.mail.Message.RecipientType, String) throws javax.mail.MessagingException;
	public void addRecipients(javax.mail.Message.RecipientType, javax.mail.Address[]) throws javax.mail.MessagingException;
	protected javax.mail.internet.InternetHeaders createInternetHeaders(java.io.InputStream) throws javax.mail.MessagingException;
	protected javax.mail.internet.MimeMessage createMimeMessage(javax.mail.Session) throws javax.mail.MessagingException;
	public java.util.Enumeration getAllHeaderLines() throws javax.mail.MessagingException;
	public java.util.Enumeration getAllHeaders() throws javax.mail.MessagingException;
	public javax.mail.Address[] getAllRecipients() throws javax.mail.MessagingException;
	public Object getContent() throws java.io.IOException, javax.mail.MessagingException;
	public String getContentID() throws javax.mail.MessagingException;
	public String[] getContentLanguage() throws javax.mail.MessagingException;
	public String getContentMD5() throws javax.mail.MessagingException;
	protected java.io.InputStream getContentStream() throws javax.mail.MessagingException;
	public String getContentType() throws javax.mail.MessagingException;
	public synchronized javax.activation.DataHandler getDataHandler() throws javax.mail.MessagingException;
	public String getDescription() throws javax.mail.MessagingException;
	public String getDisposition() throws javax.mail.MessagingException;
	public String getEncoding() throws javax.mail.MessagingException;
	public String getFileName() throws javax.mail.MessagingException;
	public synchronized javax.mail.Flags getFlags() throws javax.mail.MessagingException;
	public javax.mail.Address[] getFrom() throws javax.mail.MessagingException;
	public String[] getHeader(String) throws javax.mail.MessagingException;
	public String getHeader(String, String) throws javax.mail.MessagingException;
	public java.io.InputStream getInputStream() throws java.io.IOException, javax.mail.MessagingException;
	public int getLineCount() throws javax.mail.MessagingException;
	public java.util.Enumeration getMatchingHeaderLines(String[]) throws javax.mail.MessagingException;
	public java.util.Enumeration getMatchingHeaders(String[]) throws javax.mail.MessagingException;
	public String getMessageID() throws javax.mail.MessagingException;
	public java.util.Enumeration getNonMatchingHeaderLines(String[]) throws javax.mail.MessagingException;
	public java.util.Enumeration getNonMatchingHeaders(String[]) throws javax.mail.MessagingException;
	public java.io.InputStream getRawInputStream() throws javax.mail.MessagingException;
	public java.util.Date getReceivedDate() throws javax.mail.MessagingException;
	public javax.mail.Address[] getRecipients(javax.mail.Message.RecipientType) throws javax.mail.MessagingException;
	public javax.mail.Address[] getReplyTo() throws javax.mail.MessagingException;
	public javax.mail.Address getSender() throws javax.mail.MessagingException;
	public java.util.Date getSentDate() throws javax.mail.MessagingException;
	public int getSize() throws javax.mail.MessagingException;
	public String getSubject() throws javax.mail.MessagingException;
	public boolean isMimeType(String) throws javax.mail.MessagingException;
	public synchronized boolean isSet(javax.mail.Flags.Flag) throws javax.mail.MessagingException;
	protected void parse(java.io.InputStream) throws javax.mail.MessagingException;
	public void removeHeader(String) throws javax.mail.MessagingException;
	public javax.mail.Message reply(boolean) throws javax.mail.MessagingException;
	public void saveChanges() throws javax.mail.MessagingException;
	public void setContent(Object, String) throws javax.mail.MessagingException;
	public void setContent(javax.mail.Multipart) throws javax.mail.MessagingException;
	public void setContentID(String) throws javax.mail.MessagingException;
	public void setContentLanguage(String[]) throws javax.mail.MessagingException;
	public void setContentMD5(String) throws javax.mail.MessagingException;
	public synchronized void setDataHandler(javax.activation.DataHandler) throws javax.mail.MessagingException;
	public void setDescription(String) throws javax.mail.MessagingException;
	public void setDescription(String, String) throws javax.mail.MessagingException;
	public void setDisposition(String) throws javax.mail.MessagingException;
	public void setFileName(String) throws javax.mail.MessagingException;
	public synchronized void setFlags(javax.mail.Flags, boolean) throws javax.mail.MessagingException;
	public void setFrom() throws javax.mail.MessagingException;
	public void setFrom(javax.mail.Address) throws javax.mail.MessagingException;
	public void setHeader(String, String) throws javax.mail.MessagingException;
	public void setRecipients(javax.mail.Message.RecipientType, String) throws javax.mail.MessagingException;
	public void setRecipients(javax.mail.Message.RecipientType, javax.mail.Address[]) throws javax.mail.MessagingException;
	public void setReplyTo(javax.mail.Address[]) throws javax.mail.MessagingException;
	public void setSender(javax.mail.Address) throws javax.mail.MessagingException;
	public void setSentDate(java.util.Date) throws javax.mail.MessagingException;
	public void setSubject(String) throws javax.mail.MessagingException;
	public void setSubject(String, String) throws javax.mail.MessagingException;
	public void setText(String) throws javax.mail.MessagingException;
	public void setText(String, String) throws javax.mail.MessagingException;
	public void setText(String, String, String) throws javax.mail.MessagingException;
	protected void updateHeaders() throws javax.mail.MessagingException;
	protected void updateMessageID() throws javax.mail.MessagingException;
	public void writeTo(java.io.OutputStream) throws java.io.IOException, javax.mail.MessagingException;
	public void writeTo(java.io.OutputStream, String[]) throws java.io.IOException, javax.mail.MessagingException;
	protected byte[] content;
	protected java.io.InputStream contentStream;
	protected javax.activation.DataHandler dh;
	protected javax.mail.Flags flags;
	protected javax.mail.internet.InternetHeaders headers;
	protected boolean modified;
	protected boolean saved;

	public static class RecipientType extends javax.mail.Message.RecipientType {
		protected RecipientType(String);
		protected Object readResolve() throws java.io.ObjectStreamException;
		public static final javax.mail.internet.MimeMessage.RecipientType NEWSGROUPS;
	}
}

public class MimeMultipart extends javax.mail.Multipart {
	public MimeMultipart();
	public MimeMultipart(String);
	public MimeMultipart(javax.activation.DataSource) throws javax.mail.MessagingException;
	public synchronized void addBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException;
	public synchronized void addBodyPart(javax.mail.BodyPart, int) throws javax.mail.MessagingException;
	protected javax.mail.internet.InternetHeaders createInternetHeaders(java.io.InputStream) throws javax.mail.MessagingException;
	protected javax.mail.internet.MimeBodyPart createMimeBodyPart(java.io.InputStream) throws javax.mail.MessagingException;
	protected javax.mail.internet.MimeBodyPart createMimeBodyPart(javax.mail.internet.InternetHeaders, byte[]) throws javax.mail.MessagingException;
	public synchronized javax.mail.BodyPart getBodyPart(int) throws javax.mail.MessagingException;
	public synchronized javax.mail.BodyPart getBodyPart(String) throws javax.mail.MessagingException;
	public synchronized int getCount() throws javax.mail.MessagingException;
	public synchronized String getPreamble() throws javax.mail.MessagingException;
	public synchronized boolean isComplete() throws javax.mail.MessagingException;
	protected synchronized void parse() throws javax.mail.MessagingException;
	public void removeBodyPart(int) throws javax.mail.MessagingException;
	public boolean removeBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException;
	public synchronized void setPreamble(String) throws javax.mail.MessagingException;
	public synchronized void setSubType(String) throws javax.mail.MessagingException;
	protected synchronized void updateHeaders() throws javax.mail.MessagingException;
	public synchronized void writeTo(java.io.OutputStream) throws java.io.IOException, javax.mail.MessagingException;
	protected javax.activation.DataSource ds;
	protected boolean parsed;
}

public abstract interface MimePart extends javax.mail.Part {
	public abstract void addHeaderLine(String) throws javax.mail.MessagingException;
	public abstract java.util.Enumeration getAllHeaderLines() throws javax.mail.MessagingException;
	public abstract String getContentID() throws javax.mail.MessagingException;
	public abstract String[] getContentLanguage() throws javax.mail.MessagingException;
	public abstract String getContentMD5() throws javax.mail.MessagingException;
	public abstract String getEncoding() throws javax.mail.MessagingException;
	public abstract String getHeader(String, String) throws javax.mail.MessagingException;
	public abstract java.util.Enumeration getMatchingHeaderLines(String[]) throws javax.mail.MessagingException;
	public abstract java.util.Enumeration getNonMatchingHeaderLines(String[]) throws javax.mail.MessagingException;
	public abstract void setContentLanguage(String[]) throws javax.mail.MessagingException;
	public abstract void setContentMD5(String) throws javax.mail.MessagingException;
	public abstract void setText(String) throws javax.mail.MessagingException;
	public abstract void setText(String, String) throws javax.mail.MessagingException;
	public abstract void setText(String, String, String) throws javax.mail.MessagingException;
}

public class MimePartDataSource implements javax.activation.DataSource, javax.mail.MessageAware {
	public MimePartDataSource(javax.mail.internet.MimePart);
	public String getContentType();
	public java.io.InputStream getInputStream() throws java.io.IOException;
	public synchronized javax.mail.MessageContext getMessageContext();
	public String getName();
	public java.io.OutputStream getOutputStream() throws java.io.IOException;
	protected javax.mail.internet.MimePart part;
}

public class MimeUtility {
	public static java.io.InputStream decode(java.io.InputStream, String) throws javax.mail.MessagingException;
	public static String decodeText(String) throws java.io.UnsupportedEncodingException;
	public static String decodeWord(String) throws javax.mail.internet.ParseException, java.io.UnsupportedEncodingException;
	public static java.io.OutputStream encode(java.io.OutputStream, String) throws javax.mail.MessagingException;
	public static java.io.OutputStream encode(java.io.OutputStream, String, String) throws javax.mail.MessagingException;
	public static String encodeText(String) throws java.io.UnsupportedEncodingException;
	public static String encodeText(String, String, String) throws java.io.UnsupportedEncodingException;
	public static String encodeWord(String) throws java.io.UnsupportedEncodingException;
	public static String encodeWord(String, String, String) throws java.io.UnsupportedEncodingException;
	public static String fold(int, String);
	public static String getDefaultJavaCharset();
	public static String getEncoding(javax.activation.DataHandler);
	public static String getEncoding(javax.activation.DataSource);
	public static String javaCharset(String);
	public static String mimeCharset(String);
	public static String quote(String, String);
	public static String unfold(String);
	public static final int ALL=-1;
}

public class NewsAddress extends javax.mail.Address {
	public NewsAddress();
	public NewsAddress(String);
	public NewsAddress(String, String);
	public boolean equals(Object);
	public String getHost();
	public String getNewsgroup();
	public String getType();
	public int hashCode();
	public static javax.mail.internet.NewsAddress[] parse(String) throws javax.mail.internet.AddressException;
	public void setHost(String);
	public void setNewsgroup(String);
	public String toString();
	public static String toString(javax.mail.Address[]);
	protected String host;
	protected String newsgroup;
}

public class ParameterList {
	public ParameterList();
	public ParameterList(String) throws javax.mail.internet.ParseException;
	public String get(String);
	public java.util.Enumeration getNames();
	public void remove(String);
	public void set(String, String);
	public void set(String, String, String);
	public int size();
	public String toString();
	public String toString(int);
}

public class ParseException extends javax.mail.MessagingException {
	public ParseException();
	public ParseException(String);
}

public class PreencodedMimeBodyPart extends javax.mail.internet.MimeBodyPart {
	public PreencodedMimeBodyPart(String);
	public String getEncoding() throws javax.mail.MessagingException;
	protected void updateHeaders() throws javax.mail.MessagingException;
	public void writeTo(java.io.OutputStream) throws java.io.IOException, javax.mail.MessagingException;
}

public abstract interface SharedInputStream {
	public abstract long getPosition();
	public abstract java.io.InputStream newStream(long, long);
}

package javax.mail.search;

public abstract class AddressStringTerm extends javax.mail.search.StringTerm {
	protected AddressStringTerm(String);
	public boolean equals(Object);
	protected boolean match(javax.mail.Address);
}

public abstract class AddressTerm extends javax.mail.search.SearchTerm {
	protected AddressTerm(javax.mail.Address);
	public boolean equals(Object);
	public javax.mail.Address getAddress();
	public int hashCode();
	protected boolean match(javax.mail.Address);
	protected javax.mail.Address address;
}

public final class AndTerm extends javax.mail.search.SearchTerm {
	public AndTerm(javax.mail.search.SearchTerm, javax.mail.search.SearchTerm);
	public AndTerm(javax.mail.search.SearchTerm[]);
	public boolean equals(Object);
	public javax.mail.search.SearchTerm[] getTerms();
	public int hashCode();
	public boolean match(javax.mail.Message);
	protected javax.mail.search.SearchTerm[] terms;
}

public final class BodyTerm extends javax.mail.search.StringTerm {
	public BodyTerm(String);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public abstract class ComparisonTerm extends javax.mail.search.SearchTerm {
	public ComparisonTerm();
	public boolean equals(Object);
	public int hashCode();
	public static final int EQ=3;
	public static final int GE=6;
	public static final int GT=5;
	public static final int LE=1;
	public static final int LT=2;
	public static final int NE=4;
	protected int comparison;
}

public abstract class DateTerm extends javax.mail.search.ComparisonTerm {
	protected DateTerm(int, java.util.Date);
	public boolean equals(Object);
	public int getComparison();
	public java.util.Date getDate();
	public int hashCode();
	protected boolean match(java.util.Date);
	protected java.util.Date date;
}

public final class FlagTerm extends javax.mail.search.SearchTerm {
	public FlagTerm(javax.mail.Flags, boolean);
	public boolean equals(Object);
	public javax.mail.Flags getFlags();
	public boolean getTestSet();
	public int hashCode();
	public boolean match(javax.mail.Message);
	protected javax.mail.Flags flags;
	protected boolean set;
}

public final class FromStringTerm extends javax.mail.search.AddressStringTerm {
	public FromStringTerm(String);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public final class FromTerm extends javax.mail.search.AddressTerm {
	public FromTerm(javax.mail.Address);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public final class HeaderTerm extends javax.mail.search.StringTerm {
	public HeaderTerm(String, String);
	public boolean equals(Object);
	public String getHeaderName();
	public int hashCode();
	public boolean match(javax.mail.Message);
	protected String headerName;
}

public abstract class IntegerComparisonTerm extends javax.mail.search.ComparisonTerm {
	protected IntegerComparisonTerm(int, int);
	public boolean equals(Object);
	public int getComparison();
	public int getNumber();
	public int hashCode();
	protected boolean match(int);
	protected int number;
}

public final class MessageIDTerm extends javax.mail.search.StringTerm {
	public MessageIDTerm(String);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public final class MessageNumberTerm extends javax.mail.search.IntegerComparisonTerm {
	public MessageNumberTerm(int);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public final class NotTerm extends javax.mail.search.SearchTerm {
	public NotTerm(javax.mail.search.SearchTerm);
	public boolean equals(Object);
	public javax.mail.search.SearchTerm getTerm();
	public int hashCode();
	public boolean match(javax.mail.Message);
	protected javax.mail.search.SearchTerm term;
}

public final class OrTerm extends javax.mail.search.SearchTerm {
	public OrTerm(javax.mail.search.SearchTerm, javax.mail.search.SearchTerm);
	public OrTerm(javax.mail.search.SearchTerm[]);
	public boolean equals(Object);
	public javax.mail.search.SearchTerm[] getTerms();
	public int hashCode();
	public boolean match(javax.mail.Message);
	protected javax.mail.search.SearchTerm[] terms;
}

public final class ReceivedDateTerm extends javax.mail.search.DateTerm {
	public ReceivedDateTerm(int, java.util.Date);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public final class RecipientStringTerm extends javax.mail.search.AddressStringTerm {
	public RecipientStringTerm(javax.mail.Message.RecipientType, String);
	public boolean equals(Object);
	public javax.mail.Message.RecipientType getRecipientType();
	public int hashCode();
	public boolean match(javax.mail.Message);
}

public final class RecipientTerm extends javax.mail.search.AddressTerm {
	public RecipientTerm(javax.mail.Message.RecipientType, javax.mail.Address);
	public boolean equals(Object);
	public javax.mail.Message.RecipientType getRecipientType();
	public int hashCode();
	public boolean match(javax.mail.Message);
	protected javax.mail.Message.RecipientType type;
}

public class SearchException extends javax.mail.MessagingException {
	public SearchException();
	public SearchException(String);
}

public abstract class SearchTerm implements java.io.Serializable {
	public SearchTerm();
	public abstract boolean match(javax.mail.Message);
}

public final class SentDateTerm extends javax.mail.search.DateTerm {
	public SentDateTerm(int, java.util.Date);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public final class SizeTerm extends javax.mail.search.IntegerComparisonTerm {
	public SizeTerm(int, int);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

public abstract class StringTerm extends javax.mail.search.SearchTerm {
	protected StringTerm(String);
	protected StringTerm(String, boolean);
	public boolean equals(Object);
	public boolean getIgnoreCase();
	public String getPattern();
	public int hashCode();
	protected boolean match(String);
	protected boolean ignoreCase;
	protected String pattern;
}

public final class SubjectTerm extends javax.mail.search.StringTerm {
	public SubjectTerm(String);
	public boolean equals(Object);
	public boolean match(javax.mail.Message);
}

package javax.mail.util;

public class ByteArrayDataSource implements javax.activation.DataSource {
	public ByteArrayDataSource(byte[], String);
	public ByteArrayDataSource(java.io.InputStream, String) throws java.io.IOException;
	public ByteArrayDataSource(String, String) throws java.io.IOException;
	public String getContentType();
	public java.io.InputStream getInputStream() throws java.io.IOException;
	public String getName();
	public java.io.OutputStream getOutputStream() throws java.io.IOException;
	public void setName(String);
}

public class SharedByteArrayInputStream extends java.io.ByteArrayInputStream implements javax.mail.internet.SharedInputStream {
	public SharedByteArrayInputStream(byte[]);
	public SharedByteArrayInputStream(byte[], int, int);
	public long getPosition();
	public java.io.InputStream newStream(long, long);
	protected int start;
}

public class SharedFileInputStream extends java.io.BufferedInputStream implements javax.mail.internet.SharedInputStream {
	public SharedFileInputStream(java.io.File) throws java.io.IOException;
	public SharedFileInputStream(java.io.File, int) throws java.io.IOException;
	public SharedFileInputStream(String) throws java.io.IOException;
	public SharedFileInputStream(String, int) throws java.io.IOException;
	public synchronized int available() throws java.io.IOException;
	public void close() throws java.io.IOException;
	protected void finalize() throws Throwable;
	public long getPosition();
	public synchronized void mark(int);
	public boolean markSupported();
	public java.io.InputStream newStream(long, long);
	public synchronized int read() throws java.io.IOException;
	public synchronized int read(byte[], int, int) throws java.io.IOException;
	public synchronized void reset() throws java.io.IOException;
	public synchronized long skip(long) throws java.io.IOException;
	protected long bufpos;
	protected int bufsize;
	protected long datalen;
	protected java.io.RandomAccessFile in;
	protected long start;
}

//end of Java signature file
