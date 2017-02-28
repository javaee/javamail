#Signature file v4.3
#Version 

CLSS public abstract javax.mail.Address
cons public <init>()
intf java.io.Serializable
meth public abstract boolean equals(java.lang.Object)
meth public abstract java.lang.String getType()
meth public abstract java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public javax.mail.AuthenticationFailedException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public abstract javax.mail.Authenticator
cons public <init>()
meth protected final int getRequestingPort()
meth protected final java.lang.String getDefaultUserName()
meth protected final java.lang.String getRequestingPrompt()
meth protected final java.lang.String getRequestingProtocol()
meth protected final java.net.InetAddress getRequestingSite()
meth protected javax.mail.PasswordAuthentication getPasswordAuthentication()
supr java.lang.Object
hfds requestingPort,requestingPrompt,requestingProtocol,requestingSite,requestingUserName

CLSS public abstract javax.mail.BodyPart
cons public <init>()
fld protected javax.mail.Multipart parent
intf javax.mail.Part
meth public javax.mail.Multipart getParent()
supr java.lang.Object

CLSS public abstract interface javax.mail.EncodingAware
meth public abstract java.lang.String getEncoding()

CLSS public javax.mail.FetchProfile
cons public <init>()
innr public static Item
meth public boolean contains(java.lang.String)
meth public boolean contains(javax.mail.FetchProfile$Item)
meth public java.lang.String[] getHeaderNames()
meth public javax.mail.FetchProfile$Item[] getItems()
meth public void add(java.lang.String)
meth public void add(javax.mail.FetchProfile$Item)
supr java.lang.Object
hfds headers,specials

CLSS public static javax.mail.FetchProfile$Item
 outer javax.mail.FetchProfile
cons protected <init>(java.lang.String)
fld public final static javax.mail.FetchProfile$Item CONTENT_INFO
fld public final static javax.mail.FetchProfile$Item ENVELOPE
fld public final static javax.mail.FetchProfile$Item FLAGS
fld public final static javax.mail.FetchProfile$Item SIZE
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public javax.mail.Flags
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(javax.mail.Flags$Flag)
cons public <init>(javax.mail.Flags)
innr public final static Flag
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean contains(java.lang.String)
meth public boolean contains(javax.mail.Flags$Flag)
meth public boolean contains(javax.mail.Flags)
meth public boolean equals(java.lang.Object)
meth public boolean retainAll(javax.mail.Flags)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public java.lang.String[] getUserFlags()
meth public javax.mail.Flags$Flag[] getSystemFlags()
meth public void add(java.lang.String)
meth public void add(javax.mail.Flags$Flag)
meth public void add(javax.mail.Flags)
meth public void clearSystemFlags()
meth public void clearUserFlags()
meth public void remove(java.lang.String)
meth public void remove(javax.mail.Flags$Flag)
meth public void remove(javax.mail.Flags)
supr java.lang.Object
hfds ANSWERED_BIT,DELETED_BIT,DRAFT_BIT,FLAGGED_BIT,RECENT_BIT,SEEN_BIT,USER_BIT,serialVersionUID,system_flags,user_flags

CLSS public final static javax.mail.Flags$Flag
 outer javax.mail.Flags
fld public final static javax.mail.Flags$Flag ANSWERED
fld public final static javax.mail.Flags$Flag DELETED
fld public final static javax.mail.Flags$Flag DRAFT
fld public final static javax.mail.Flags$Flag FLAGGED
fld public final static javax.mail.Flags$Flag RECENT
fld public final static javax.mail.Flags$Flag SEEN
fld public final static javax.mail.Flags$Flag USER
supr java.lang.Object
hfds bit

CLSS public abstract javax.mail.Folder
cons protected <init>(javax.mail.Store)
fld protected int mode
fld protected javax.mail.Store store
fld public final static int HOLDS_FOLDERS = 2
fld public final static int HOLDS_MESSAGES = 1
fld public final static int READ_ONLY = 1
fld public final static int READ_WRITE = 2
intf java.lang.AutoCloseable
meth protected void finalize() throws java.lang.Throwable
meth protected void notifyConnectionListeners(int)
meth protected void notifyFolderListeners(int)
meth protected void notifyFolderRenamedListeners(javax.mail.Folder)
meth protected void notifyMessageAddedListeners(javax.mail.Message[])
meth protected void notifyMessageChangedListeners(int,javax.mail.Message)
meth protected void notifyMessageRemovedListeners(boolean,javax.mail.Message[])
meth public abstract boolean create(int) throws javax.mail.MessagingException
meth public abstract boolean delete(boolean) throws javax.mail.MessagingException
meth public abstract boolean exists() throws javax.mail.MessagingException
meth public abstract boolean hasNewMessages() throws javax.mail.MessagingException
meth public abstract boolean isOpen()
meth public abstract boolean renameTo(javax.mail.Folder) throws javax.mail.MessagingException
meth public abstract char getSeparator() throws javax.mail.MessagingException
meth public abstract int getMessageCount() throws javax.mail.MessagingException
meth public abstract int getType() throws javax.mail.MessagingException
meth public abstract java.lang.String getFullName()
meth public abstract java.lang.String getName()
meth public abstract javax.mail.Flags getPermanentFlags()
meth public abstract javax.mail.Folder getFolder(java.lang.String) throws javax.mail.MessagingException
meth public abstract javax.mail.Folder getParent() throws javax.mail.MessagingException
meth public abstract javax.mail.Folder[] list(java.lang.String) throws javax.mail.MessagingException
meth public abstract javax.mail.Message getMessage(int) throws javax.mail.MessagingException
meth public abstract javax.mail.Message[] expunge() throws javax.mail.MessagingException
meth public abstract void appendMessages(javax.mail.Message[]) throws javax.mail.MessagingException
meth public abstract void close(boolean) throws javax.mail.MessagingException
meth public abstract void open(int) throws javax.mail.MessagingException
meth public boolean isSubscribed()
meth public int getDeletedMessageCount() throws javax.mail.MessagingException
meth public int getMode()
meth public int getNewMessageCount() throws javax.mail.MessagingException
meth public int getUnreadMessageCount() throws javax.mail.MessagingException
meth public java.lang.String toString()
meth public javax.mail.Folder[] list() throws javax.mail.MessagingException
meth public javax.mail.Folder[] listSubscribed() throws javax.mail.MessagingException
meth public javax.mail.Folder[] listSubscribed(java.lang.String) throws javax.mail.MessagingException
meth public javax.mail.Message[] getMessages() throws javax.mail.MessagingException
meth public javax.mail.Message[] getMessages(int,int) throws javax.mail.MessagingException
meth public javax.mail.Message[] getMessages(int[]) throws javax.mail.MessagingException
meth public javax.mail.Message[] search(javax.mail.search.SearchTerm) throws javax.mail.MessagingException
meth public javax.mail.Message[] search(javax.mail.search.SearchTerm,javax.mail.Message[]) throws javax.mail.MessagingException
meth public javax.mail.Store getStore()
meth public javax.mail.URLName getURLName() throws javax.mail.MessagingException
meth public void addConnectionListener(javax.mail.event.ConnectionListener)
meth public void addFolderListener(javax.mail.event.FolderListener)
meth public void addMessageChangedListener(javax.mail.event.MessageChangedListener)
meth public void addMessageCountListener(javax.mail.event.MessageCountListener)
meth public void close() throws javax.mail.MessagingException
meth public void copyMessages(javax.mail.Message[],javax.mail.Folder) throws javax.mail.MessagingException
meth public void fetch(javax.mail.Message[],javax.mail.FetchProfile) throws javax.mail.MessagingException
meth public void removeConnectionListener(javax.mail.event.ConnectionListener)
meth public void removeFolderListener(javax.mail.event.FolderListener)
meth public void removeMessageChangedListener(javax.mail.event.MessageChangedListener)
meth public void removeMessageCountListener(javax.mail.event.MessageCountListener)
meth public void setFlags(int,int,javax.mail.Flags,boolean) throws javax.mail.MessagingException
meth public void setFlags(int[],javax.mail.Flags,boolean) throws javax.mail.MessagingException
meth public void setFlags(javax.mail.Message[],javax.mail.Flags,boolean) throws javax.mail.MessagingException
meth public void setSubscribed(boolean) throws javax.mail.MessagingException
supr java.lang.Object
hfds connectionListeners,folderListeners,messageChangedListeners,messageCountListeners,q

CLSS public javax.mail.FolderClosedException
cons public <init>(javax.mail.Folder)
cons public <init>(javax.mail.Folder,java.lang.String)
cons public <init>(javax.mail.Folder,java.lang.String,java.lang.Exception)
meth public javax.mail.Folder getFolder()
supr javax.mail.MessagingException
hfds folder,serialVersionUID

CLSS public javax.mail.FolderNotFoundException
cons public <init>()
cons public <init>(java.lang.String,javax.mail.Folder)
cons public <init>(javax.mail.Folder)
cons public <init>(javax.mail.Folder,java.lang.String)
cons public <init>(javax.mail.Folder,java.lang.String,java.lang.Exception)
meth public javax.mail.Folder getFolder()
supr javax.mail.MessagingException
hfds folder,serialVersionUID

CLSS public javax.mail.Header
cons public <init>(java.lang.String,java.lang.String)
fld protected java.lang.String name
fld protected java.lang.String value
meth public java.lang.String getName()
meth public java.lang.String getValue()
supr java.lang.Object

CLSS public javax.mail.IllegalWriteException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.mail.MailSessionDefinition
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class javax.mail.MailSessionDefinitions)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String description() value= ""
meth public abstract !hasdefault java.lang.String from() value= ""
meth public abstract !hasdefault java.lang.String host() value= ""
meth public abstract !hasdefault java.lang.String password() value= ""
meth public abstract !hasdefault java.lang.String storeProtocol() value= ""
meth public abstract !hasdefault java.lang.String transportProtocol() value= ""
meth public abstract !hasdefault java.lang.String user() value= ""
meth public abstract !hasdefault java.lang.String[] properties() value= []
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation javax.mail.MailSessionDefinitions
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.mail.MailSessionDefinition[] value()

CLSS public abstract javax.mail.Message
cons protected <init>()
cons protected <init>(javax.mail.Folder,int)
cons protected <init>(javax.mail.Session)
fld protected boolean expunged
fld protected int msgnum
fld protected javax.mail.Folder folder
fld protected javax.mail.Session session
innr public static RecipientType
intf javax.mail.Part
meth protected void setExpunged(boolean)
meth protected void setMessageNumber(int)
meth public abstract java.lang.String getSubject() throws javax.mail.MessagingException
meth public abstract java.util.Date getReceivedDate() throws javax.mail.MessagingException
meth public abstract java.util.Date getSentDate() throws javax.mail.MessagingException
meth public abstract javax.mail.Address[] getFrom() throws javax.mail.MessagingException
meth public abstract javax.mail.Address[] getRecipients(javax.mail.Message$RecipientType) throws javax.mail.MessagingException
meth public abstract javax.mail.Flags getFlags() throws javax.mail.MessagingException
meth public abstract javax.mail.Message reply(boolean) throws javax.mail.MessagingException
meth public abstract void addFrom(javax.mail.Address[]) throws javax.mail.MessagingException
meth public abstract void addRecipients(javax.mail.Message$RecipientType,javax.mail.Address[]) throws javax.mail.MessagingException
meth public abstract void saveChanges() throws javax.mail.MessagingException
meth public abstract void setFlags(javax.mail.Flags,boolean) throws javax.mail.MessagingException
meth public abstract void setFrom() throws javax.mail.MessagingException
meth public abstract void setFrom(javax.mail.Address) throws javax.mail.MessagingException
meth public abstract void setRecipients(javax.mail.Message$RecipientType,javax.mail.Address[]) throws javax.mail.MessagingException
meth public abstract void setSentDate(java.util.Date) throws javax.mail.MessagingException
meth public abstract void setSubject(java.lang.String) throws javax.mail.MessagingException
meth public boolean isExpunged()
meth public boolean isSet(javax.mail.Flags$Flag) throws javax.mail.MessagingException
meth public boolean match(javax.mail.search.SearchTerm) throws javax.mail.MessagingException
meth public int getMessageNumber()
meth public javax.mail.Address[] getAllRecipients() throws javax.mail.MessagingException
meth public javax.mail.Address[] getReplyTo() throws javax.mail.MessagingException
meth public javax.mail.Folder getFolder()
meth public javax.mail.Session getSession()
meth public void addRecipient(javax.mail.Message$RecipientType,javax.mail.Address) throws javax.mail.MessagingException
meth public void setFlag(javax.mail.Flags$Flag,boolean) throws javax.mail.MessagingException
meth public void setRecipient(javax.mail.Message$RecipientType,javax.mail.Address) throws javax.mail.MessagingException
meth public void setReplyTo(javax.mail.Address[]) throws javax.mail.MessagingException
supr java.lang.Object

CLSS public static javax.mail.Message$RecipientType
 outer javax.mail.Message
cons protected <init>(java.lang.String)
fld protected java.lang.String type
fld public final static javax.mail.Message$RecipientType BCC
fld public final static javax.mail.Message$RecipientType CC
fld public final static javax.mail.Message$RecipientType TO
intf java.io.Serializable
meth protected java.lang.Object readResolve() throws java.io.ObjectStreamException
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface javax.mail.MessageAware
meth public abstract javax.mail.MessageContext getMessageContext()

CLSS public javax.mail.MessageContext
cons public <init>(javax.mail.Part)
meth public javax.mail.Message getMessage()
meth public javax.mail.Part getPart()
meth public javax.mail.Session getSession()
supr java.lang.Object
hfds part

CLSS public javax.mail.MessageRemovedException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public javax.mail.MessagingException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
meth public boolean setNextException(java.lang.Exception)
meth public java.lang.Exception getNextException()
meth public java.lang.String toString()
meth public java.lang.Throwable getCause()
supr java.lang.Exception
hfds next,serialVersionUID

CLSS public javax.mail.MethodNotSupportedException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public abstract javax.mail.Multipart
cons protected <init>()
fld protected java.lang.String contentType
fld protected java.util.Vector<javax.mail.BodyPart> parts
fld protected javax.mail.Part parent
meth protected void setMultipartDataSource(javax.mail.MultipartDataSource) throws javax.mail.MessagingException
meth public abstract void writeTo(java.io.OutputStream) throws java.io.IOException,javax.mail.MessagingException
meth public boolean removeBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException
meth public int getCount() throws javax.mail.MessagingException
meth public java.lang.String getContentType()
meth public javax.mail.BodyPart getBodyPart(int) throws javax.mail.MessagingException
meth public javax.mail.Part getParent()
meth public void addBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException
meth public void addBodyPart(javax.mail.BodyPart,int) throws javax.mail.MessagingException
meth public void removeBodyPart(int) throws javax.mail.MessagingException
meth public void setParent(javax.mail.Part)
supr java.lang.Object

CLSS public abstract interface javax.mail.MultipartDataSource
intf javax.activation.DataSource
meth public abstract int getCount()
meth public abstract javax.mail.BodyPart getBodyPart(int) throws javax.mail.MessagingException

CLSS public javax.mail.NoSuchProviderException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public abstract interface javax.mail.Part
fld public final static java.lang.String ATTACHMENT = "attachment"
fld public final static java.lang.String INLINE = "inline"
meth public abstract boolean isMimeType(java.lang.String) throws javax.mail.MessagingException
meth public abstract int getLineCount() throws javax.mail.MessagingException
meth public abstract int getSize() throws javax.mail.MessagingException
meth public abstract java.io.InputStream getInputStream() throws java.io.IOException,javax.mail.MessagingException
meth public abstract java.lang.Object getContent() throws java.io.IOException,javax.mail.MessagingException
meth public abstract java.lang.String getContentType() throws javax.mail.MessagingException
meth public abstract java.lang.String getDescription() throws javax.mail.MessagingException
meth public abstract java.lang.String getDisposition() throws javax.mail.MessagingException
meth public abstract java.lang.String getFileName() throws javax.mail.MessagingException
meth public abstract java.lang.String[] getHeader(java.lang.String) throws javax.mail.MessagingException
meth public abstract java.util.Enumeration<javax.mail.Header> getAllHeaders() throws javax.mail.MessagingException
meth public abstract java.util.Enumeration<javax.mail.Header> getMatchingHeaders(java.lang.String[]) throws javax.mail.MessagingException
meth public abstract java.util.Enumeration<javax.mail.Header> getNonMatchingHeaders(java.lang.String[]) throws javax.mail.MessagingException
meth public abstract javax.activation.DataHandler getDataHandler() throws javax.mail.MessagingException
meth public abstract void addHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public abstract void removeHeader(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setContent(java.lang.Object,java.lang.String) throws javax.mail.MessagingException
meth public abstract void setContent(javax.mail.Multipart) throws javax.mail.MessagingException
meth public abstract void setDataHandler(javax.activation.DataHandler) throws javax.mail.MessagingException
meth public abstract void setDescription(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setDisposition(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setFileName(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public abstract void setText(java.lang.String) throws javax.mail.MessagingException
meth public abstract void writeTo(java.io.OutputStream) throws java.io.IOException,javax.mail.MessagingException

CLSS public final javax.mail.PasswordAuthentication
cons public <init>(java.lang.String,java.lang.String)
meth public java.lang.String getPassword()
meth public java.lang.String getUserName()
supr java.lang.Object
hfds password,userName

CLSS public javax.mail.Provider
cons public <init>(javax.mail.Provider$Type,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
innr public static Type
meth public java.lang.String getClassName()
meth public java.lang.String getProtocol()
meth public java.lang.String getVendor()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public javax.mail.Provider$Type getType()
supr java.lang.Object
hfds className,protocol,type,vendor,version

CLSS public static javax.mail.Provider$Type
 outer javax.mail.Provider
fld public final static javax.mail.Provider$Type STORE
fld public final static javax.mail.Provider$Type TRANSPORT
meth public java.lang.String toString()
supr java.lang.Object
hfds type

CLSS public javax.mail.Quota
cons public <init>(java.lang.String)
fld public java.lang.String quotaRoot
fld public javax.mail.Quota$Resource[] resources
innr public static Resource
meth public void setResourceLimit(java.lang.String,long)
supr java.lang.Object

CLSS public static javax.mail.Quota$Resource
 outer javax.mail.Quota
cons public <init>(java.lang.String,long,long)
fld public java.lang.String name
fld public long limit
fld public long usage
supr java.lang.Object

CLSS public abstract interface javax.mail.QuotaAwareStore
meth public abstract javax.mail.Quota[] getQuota(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setQuota(javax.mail.Quota) throws javax.mail.MessagingException

CLSS public javax.mail.ReadOnlyFolderException
cons public <init>(javax.mail.Folder)
cons public <init>(javax.mail.Folder,java.lang.String)
cons public <init>(javax.mail.Folder,java.lang.String,java.lang.Exception)
meth public javax.mail.Folder getFolder()
supr javax.mail.MessagingException
hfds folder,serialVersionUID

CLSS public javax.mail.SendFailedException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.Exception)
cons public <init>(java.lang.String,java.lang.Exception,javax.mail.Address[],javax.mail.Address[],javax.mail.Address[])
fld protected javax.mail.Address[] invalid
fld protected javax.mail.Address[] validSent
fld protected javax.mail.Address[] validUnsent
meth public javax.mail.Address[] getInvalidAddresses()
meth public javax.mail.Address[] getValidSentAddresses()
meth public javax.mail.Address[] getValidUnsentAddresses()
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public abstract javax.mail.Service
cons protected <init>(javax.mail.Session,javax.mail.URLName)
fld protected boolean debug
fld protected javax.mail.Session session
fld protected volatile javax.mail.URLName url
intf java.lang.AutoCloseable
meth protected boolean protocolConnect(java.lang.String,int,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth protected void finalize() throws java.lang.Throwable
meth protected void notifyConnectionListeners(int)
meth protected void queueEvent(javax.mail.event.MailEvent,java.util.Vector<? extends java.util.EventListener>)
meth protected void setConnected(boolean)
meth protected void setURLName(javax.mail.URLName)
meth public boolean isConnected()
meth public java.lang.String toString()
meth public javax.mail.URLName getURLName()
meth public void addConnectionListener(javax.mail.event.ConnectionListener)
meth public void close() throws javax.mail.MessagingException
meth public void connect() throws javax.mail.MessagingException
meth public void connect(java.lang.String,int,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void connect(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void connect(java.lang.String,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void removeConnectionListener(javax.mail.event.ConnectionListener)
supr java.lang.Object
hfds connected,connectionListeners,q

CLSS public final javax.mail.Session
meth public boolean getDebug()
meth public java.io.PrintStream getDebugOut()
meth public java.lang.String getProperty(java.lang.String)
meth public java.util.Properties getProperties()
meth public javax.mail.Folder getFolder(javax.mail.URLName) throws javax.mail.MessagingException
meth public javax.mail.PasswordAuthentication getPasswordAuthentication(javax.mail.URLName)
meth public javax.mail.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public javax.mail.Provider getProvider(java.lang.String) throws javax.mail.NoSuchProviderException
meth public javax.mail.Provider[] getProviders()
meth public javax.mail.Store getStore() throws javax.mail.NoSuchProviderException
meth public javax.mail.Store getStore(java.lang.String) throws javax.mail.NoSuchProviderException
meth public javax.mail.Store getStore(javax.mail.Provider) throws javax.mail.NoSuchProviderException
meth public javax.mail.Store getStore(javax.mail.URLName) throws javax.mail.NoSuchProviderException
meth public javax.mail.Transport getTransport() throws javax.mail.NoSuchProviderException
meth public javax.mail.Transport getTransport(java.lang.String) throws javax.mail.NoSuchProviderException
meth public javax.mail.Transport getTransport(javax.mail.Address) throws javax.mail.NoSuchProviderException
meth public javax.mail.Transport getTransport(javax.mail.Provider) throws javax.mail.NoSuchProviderException
meth public javax.mail.Transport getTransport(javax.mail.URLName) throws javax.mail.NoSuchProviderException
meth public static javax.mail.Session getDefaultInstance(java.util.Properties)
meth public static javax.mail.Session getDefaultInstance(java.util.Properties,javax.mail.Authenticator)
meth public static javax.mail.Session getInstance(java.util.Properties)
meth public static javax.mail.Session getInstance(java.util.Properties,javax.mail.Authenticator)
meth public void addProvider(javax.mail.Provider)
meth public void setDebug(boolean)
meth public void setDebugOut(java.io.PrintStream)
meth public void setPasswordAuthentication(javax.mail.URLName,javax.mail.PasswordAuthentication)
meth public void setProtocolForAddress(java.lang.String,java.lang.String)
meth public void setProvider(javax.mail.Provider) throws javax.mail.NoSuchProviderException
supr java.lang.Object
hfds addressMap,authTable,authenticator,confDir,debug,defaultSession,logger,out,props,providers,providersByClassName,providersByProtocol,q

CLSS public abstract javax.mail.Store
cons protected <init>(javax.mail.Session,javax.mail.URLName)
meth protected void notifyFolderListeners(int,javax.mail.Folder)
meth protected void notifyFolderRenamedListeners(javax.mail.Folder,javax.mail.Folder)
meth protected void notifyStoreListeners(int,java.lang.String)
meth public abstract javax.mail.Folder getDefaultFolder() throws javax.mail.MessagingException
meth public abstract javax.mail.Folder getFolder(java.lang.String) throws javax.mail.MessagingException
meth public abstract javax.mail.Folder getFolder(javax.mail.URLName) throws javax.mail.MessagingException
meth public javax.mail.Folder[] getPersonalNamespaces() throws javax.mail.MessagingException
meth public javax.mail.Folder[] getSharedNamespaces() throws javax.mail.MessagingException
meth public javax.mail.Folder[] getUserNamespaces(java.lang.String) throws javax.mail.MessagingException
meth public void addFolderListener(javax.mail.event.FolderListener)
meth public void addStoreListener(javax.mail.event.StoreListener)
meth public void removeFolderListener(javax.mail.event.FolderListener)
meth public void removeStoreListener(javax.mail.event.StoreListener)
supr javax.mail.Service
hfds folderListeners,storeListeners

CLSS public javax.mail.StoreClosedException
cons public <init>(javax.mail.Store)
cons public <init>(javax.mail.Store,java.lang.String)
cons public <init>(javax.mail.Store,java.lang.String,java.lang.Exception)
meth public javax.mail.Store getStore()
supr javax.mail.MessagingException
hfds serialVersionUID,store

CLSS public abstract javax.mail.Transport
cons public <init>(javax.mail.Session,javax.mail.URLName)
meth protected void notifyTransportListeners(int,javax.mail.Address[],javax.mail.Address[],javax.mail.Address[],javax.mail.Message)
meth public abstract void sendMessage(javax.mail.Message,javax.mail.Address[]) throws javax.mail.MessagingException
meth public static void send(javax.mail.Message) throws javax.mail.MessagingException
meth public static void send(javax.mail.Message,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public static void send(javax.mail.Message,javax.mail.Address[]) throws javax.mail.MessagingException
meth public static void send(javax.mail.Message,javax.mail.Address[],java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void addTransportListener(javax.mail.event.TransportListener)
meth public void removeTransportListener(javax.mail.event.TransportListener)
supr javax.mail.Service
hfds transportListeners

CLSS public abstract interface javax.mail.UIDFolder
fld public final static long LASTUID = -1
fld public final static long MAXUID = 4294967295
innr public static FetchProfileItem
meth public abstract javax.mail.Message getMessageByUID(long) throws javax.mail.MessagingException
meth public abstract javax.mail.Message[] getMessagesByUID(long,long) throws javax.mail.MessagingException
meth public abstract javax.mail.Message[] getMessagesByUID(long[]) throws javax.mail.MessagingException
meth public abstract long getUID(javax.mail.Message) throws javax.mail.MessagingException
meth public abstract long getUIDNext() throws javax.mail.MessagingException
meth public abstract long getUIDValidity() throws javax.mail.MessagingException

CLSS public static javax.mail.UIDFolder$FetchProfileItem
 outer javax.mail.UIDFolder
cons protected <init>(java.lang.String)
fld public final static javax.mail.UIDFolder$FetchProfileItem UID
supr javax.mail.FetchProfile$Item

CLSS public javax.mail.URLName
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String)
cons public <init>(java.net.URL)
fld protected java.lang.String fullURL
meth protected void parseString(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getFile()
meth public java.lang.String getHost()
meth public java.lang.String getPassword()
meth public java.lang.String getProtocol()
meth public java.lang.String getRef()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public java.net.URL getURL() throws java.net.MalformedURLException
supr java.lang.Object
hfds caseDiff,doEncode,dontNeedEncoding,file,hashCode,host,hostAddress,hostAddressKnown,password,port,protocol,ref,username

CLSS public abstract javax.mail.event.ConnectionAdapter
cons public <init>()
intf javax.mail.event.ConnectionListener
meth public void closed(javax.mail.event.ConnectionEvent)
meth public void disconnected(javax.mail.event.ConnectionEvent)
meth public void opened(javax.mail.event.ConnectionEvent)
supr java.lang.Object

CLSS public javax.mail.event.ConnectionEvent
cons public <init>(java.lang.Object,int)
fld protected int type
fld public final static int CLOSED = 3
fld public final static int DISCONNECTED = 2
fld public final static int OPENED = 1
meth public int getType()
meth public void dispatch(java.lang.Object)
supr javax.mail.event.MailEvent
hfds serialVersionUID

CLSS public abstract interface javax.mail.event.ConnectionListener
intf java.util.EventListener
meth public abstract void closed(javax.mail.event.ConnectionEvent)
meth public abstract void disconnected(javax.mail.event.ConnectionEvent)
meth public abstract void opened(javax.mail.event.ConnectionEvent)

CLSS public abstract javax.mail.event.FolderAdapter
cons public <init>()
intf javax.mail.event.FolderListener
meth public void folderCreated(javax.mail.event.FolderEvent)
meth public void folderDeleted(javax.mail.event.FolderEvent)
meth public void folderRenamed(javax.mail.event.FolderEvent)
supr java.lang.Object

CLSS public javax.mail.event.FolderEvent
cons public <init>(java.lang.Object,javax.mail.Folder,int)
cons public <init>(java.lang.Object,javax.mail.Folder,javax.mail.Folder,int)
fld protected int type
fld protected javax.mail.Folder folder
fld protected javax.mail.Folder newFolder
fld public final static int CREATED = 1
fld public final static int DELETED = 2
fld public final static int RENAMED = 3
meth public int getType()
meth public javax.mail.Folder getFolder()
meth public javax.mail.Folder getNewFolder()
meth public void dispatch(java.lang.Object)
supr javax.mail.event.MailEvent
hfds serialVersionUID

CLSS public abstract interface javax.mail.event.FolderListener
intf java.util.EventListener
meth public abstract void folderCreated(javax.mail.event.FolderEvent)
meth public abstract void folderDeleted(javax.mail.event.FolderEvent)
meth public abstract void folderRenamed(javax.mail.event.FolderEvent)

CLSS public abstract javax.mail.event.MailEvent
cons public <init>(java.lang.Object)
meth public abstract void dispatch(java.lang.Object)
supr java.util.EventObject
hfds serialVersionUID

CLSS public javax.mail.event.MessageChangedEvent
cons public <init>(java.lang.Object,int,javax.mail.Message)
fld protected int type
fld protected javax.mail.Message msg
fld public final static int ENVELOPE_CHANGED = 2
fld public final static int FLAGS_CHANGED = 1
meth public int getMessageChangeType()
meth public javax.mail.Message getMessage()
meth public void dispatch(java.lang.Object)
supr javax.mail.event.MailEvent
hfds serialVersionUID

CLSS public abstract interface javax.mail.event.MessageChangedListener
intf java.util.EventListener
meth public abstract void messageChanged(javax.mail.event.MessageChangedEvent)

CLSS public abstract javax.mail.event.MessageCountAdapter
cons public <init>()
intf javax.mail.event.MessageCountListener
meth public void messagesAdded(javax.mail.event.MessageCountEvent)
meth public void messagesRemoved(javax.mail.event.MessageCountEvent)
supr java.lang.Object

CLSS public javax.mail.event.MessageCountEvent
cons public <init>(javax.mail.Folder,int,boolean,javax.mail.Message[])
fld protected boolean removed
fld protected int type
fld protected javax.mail.Message[] msgs
fld public final static int ADDED = 1
fld public final static int REMOVED = 2
meth public boolean isRemoved()
meth public int getType()
meth public javax.mail.Message[] getMessages()
meth public void dispatch(java.lang.Object)
supr javax.mail.event.MailEvent
hfds serialVersionUID

CLSS public abstract interface javax.mail.event.MessageCountListener
intf java.util.EventListener
meth public abstract void messagesAdded(javax.mail.event.MessageCountEvent)
meth public abstract void messagesRemoved(javax.mail.event.MessageCountEvent)

CLSS public javax.mail.event.StoreEvent
cons public <init>(javax.mail.Store,int,java.lang.String)
fld protected int type
fld protected java.lang.String message
fld public final static int ALERT = 1
fld public final static int NOTICE = 2
meth public int getMessageType()
meth public java.lang.String getMessage()
meth public void dispatch(java.lang.Object)
supr javax.mail.event.MailEvent
hfds serialVersionUID

CLSS public abstract interface javax.mail.event.StoreListener
intf java.util.EventListener
meth public abstract void notification(javax.mail.event.StoreEvent)

CLSS public abstract javax.mail.event.TransportAdapter
cons public <init>()
intf javax.mail.event.TransportListener
meth public void messageDelivered(javax.mail.event.TransportEvent)
meth public void messageNotDelivered(javax.mail.event.TransportEvent)
meth public void messagePartiallyDelivered(javax.mail.event.TransportEvent)
supr java.lang.Object

CLSS public javax.mail.event.TransportEvent
cons public <init>(javax.mail.Transport,int,javax.mail.Address[],javax.mail.Address[],javax.mail.Address[],javax.mail.Message)
fld protected int type
fld protected javax.mail.Address[] invalid
fld protected javax.mail.Address[] validSent
fld protected javax.mail.Address[] validUnsent
fld protected javax.mail.Message msg
fld public final static int MESSAGE_DELIVERED = 1
fld public final static int MESSAGE_NOT_DELIVERED = 2
fld public final static int MESSAGE_PARTIALLY_DELIVERED = 3
meth public int getType()
meth public javax.mail.Address[] getInvalidAddresses()
meth public javax.mail.Address[] getValidSentAddresses()
meth public javax.mail.Address[] getValidUnsentAddresses()
meth public javax.mail.Message getMessage()
meth public void dispatch(java.lang.Object)
supr javax.mail.event.MailEvent
hfds serialVersionUID

CLSS public abstract interface javax.mail.event.TransportListener
intf java.util.EventListener
meth public abstract void messageDelivered(javax.mail.event.TransportEvent)
meth public abstract void messageNotDelivered(javax.mail.event.TransportEvent)
meth public abstract void messagePartiallyDelivered(javax.mail.event.TransportEvent)

CLSS public javax.mail.internet.AddressException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.String)
cons public <init>(java.lang.String,java.lang.String,int)
fld protected int pos
fld protected java.lang.String ref
meth public int getPos()
meth public java.lang.String getRef()
meth public java.lang.String toString()
supr javax.mail.internet.ParseException
hfds serialVersionUID

CLSS public javax.mail.internet.ContentDisposition
cons public <init>()
cons public <init>(java.lang.String) throws javax.mail.internet.ParseException
cons public <init>(java.lang.String,javax.mail.internet.ParameterList)
meth public java.lang.String getDisposition()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String toString()
meth public javax.mail.internet.ParameterList getParameterList()
meth public void setDisposition(java.lang.String)
meth public void setParameter(java.lang.String,java.lang.String)
meth public void setParameterList(javax.mail.internet.ParameterList)
supr java.lang.Object
hfds disposition,list

CLSS public javax.mail.internet.ContentType
cons public <init>()
cons public <init>(java.lang.String) throws javax.mail.internet.ParseException
cons public <init>(java.lang.String,java.lang.String,javax.mail.internet.ParameterList)
meth public boolean match(java.lang.String)
meth public boolean match(javax.mail.internet.ContentType)
meth public java.lang.String getBaseType()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getPrimaryType()
meth public java.lang.String getSubType()
meth public java.lang.String toString()
meth public javax.mail.internet.ParameterList getParameterList()
meth public void setParameter(java.lang.String,java.lang.String)
meth public void setParameterList(javax.mail.internet.ParameterList)
meth public void setPrimaryType(java.lang.String)
meth public void setSubType(java.lang.String)
supr java.lang.Object
hfds list,primaryType,subType

CLSS public javax.mail.internet.HeaderTokenizer
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.String)
cons public <init>(java.lang.String,java.lang.String,boolean)
fld public final static java.lang.String MIME = "()<>@,;:\u005c\u0022\u0009 []/?="
fld public final static java.lang.String RFC822 = "()<>@,;:\u005c\u0022\u0009 .[]"
innr public static Token
meth public java.lang.String getRemainder()
meth public javax.mail.internet.HeaderTokenizer$Token next() throws javax.mail.internet.ParseException
meth public javax.mail.internet.HeaderTokenizer$Token next(char) throws javax.mail.internet.ParseException
meth public javax.mail.internet.HeaderTokenizer$Token next(char,boolean) throws javax.mail.internet.ParseException
meth public javax.mail.internet.HeaderTokenizer$Token peek() throws javax.mail.internet.ParseException
supr java.lang.Object
hfds EOFToken,currentPos,delimiters,maxPos,nextPos,peekPos,skipComments,string

CLSS public static javax.mail.internet.HeaderTokenizer$Token
 outer javax.mail.internet.HeaderTokenizer
cons public <init>(int,java.lang.String)
fld public final static int ATOM = -1
fld public final static int COMMENT = -3
fld public final static int EOF = -4
fld public final static int QUOTEDSTRING = -2
meth public int getType()
meth public java.lang.String getValue()
supr java.lang.Object
hfds type,value

CLSS public javax.mail.internet.InternetAddress
cons public <init>()
cons public <init>(java.lang.String) throws javax.mail.internet.AddressException
cons public <init>(java.lang.String,boolean) throws javax.mail.internet.AddressException
cons public <init>(java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
cons public <init>(java.lang.String,java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
fld protected java.lang.String address
fld protected java.lang.String encodedPersonal
fld protected java.lang.String personal
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isGroup()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getAddress()
meth public java.lang.String getPersonal()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.lang.String toUnicodeString()
meth public javax.mail.internet.InternetAddress[] getGroup(boolean) throws javax.mail.internet.AddressException
meth public static java.lang.String toString(javax.mail.Address[])
meth public static java.lang.String toString(javax.mail.Address[],int)
meth public static java.lang.String toUnicodeString(javax.mail.Address[])
meth public static java.lang.String toUnicodeString(javax.mail.Address[],int)
meth public static javax.mail.internet.InternetAddress getLocalAddress(javax.mail.Session)
meth public static javax.mail.internet.InternetAddress[] parse(java.lang.String) throws javax.mail.internet.AddressException
meth public static javax.mail.internet.InternetAddress[] parse(java.lang.String,boolean) throws javax.mail.internet.AddressException
meth public static javax.mail.internet.InternetAddress[] parseHeader(java.lang.String,boolean) throws javax.mail.internet.AddressException
meth public void setAddress(java.lang.String)
meth public void setPersonal(java.lang.String) throws java.io.UnsupportedEncodingException
meth public void setPersonal(java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
meth public void validate() throws javax.mail.internet.AddressException
supr javax.mail.Address
hfds allowUtf8,ignoreBogusGroupName,rfc822phrase,serialVersionUID,specialsNoDot,specialsNoDotNoAt,useCanonicalHostName

CLSS public javax.mail.internet.InternetHeaders
cons public <init>()
cons public <init>(java.io.InputStream) throws javax.mail.MessagingException
cons public <init>(java.io.InputStream,boolean) throws javax.mail.MessagingException
fld protected java.util.List<javax.mail.internet.InternetHeaders$InternetHeader> headers
innr protected final static InternetHeader
meth public java.lang.String getHeader(java.lang.String,java.lang.String)
meth public java.lang.String[] getHeader(java.lang.String)
meth public java.util.Enumeration<java.lang.String> getAllHeaderLines()
meth public java.util.Enumeration<java.lang.String> getMatchingHeaderLines(java.lang.String[])
meth public java.util.Enumeration<java.lang.String> getNonMatchingHeaderLines(java.lang.String[])
meth public java.util.Enumeration<javax.mail.Header> getAllHeaders()
meth public java.util.Enumeration<javax.mail.Header> getMatchingHeaders(java.lang.String[])
meth public java.util.Enumeration<javax.mail.Header> getNonMatchingHeaders(java.lang.String[])
meth public void addHeader(java.lang.String,java.lang.String)
meth public void addHeaderLine(java.lang.String)
meth public void load(java.io.InputStream) throws javax.mail.MessagingException
meth public void load(java.io.InputStream,boolean) throws javax.mail.MessagingException
meth public void removeHeader(java.lang.String)
meth public void setHeader(java.lang.String,java.lang.String)
supr java.lang.Object
hfds ignoreWhitespaceLines
hcls MatchEnum,MatchHeaderEnum,MatchStringEnum

CLSS protected final static javax.mail.internet.InternetHeaders$InternetHeader
 outer javax.mail.internet.InternetHeaders
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.String)
meth public java.lang.String getValue()
supr javax.mail.Header
hfds line

CLSS public javax.mail.internet.MailDateFormat
cons public <init>()
meth public java.lang.StringBuffer format(java.util.Date,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.util.Date get2DigitYearStart()
meth public java.util.Date parse(java.lang.String,java.text.ParsePosition)
meth public javax.mail.internet.MailDateFormat clone()
meth public void applyLocalizedPattern(java.lang.String)
meth public void applyPattern(java.lang.String)
meth public void set2DigitYearStart(java.util.Date)
meth public void setCalendar(java.util.Calendar)
meth public void setDateFormatSymbols(java.text.DateFormatSymbols)
meth public void setNumberFormat(java.text.NumberFormat)
supr java.text.SimpleDateFormat
hfds LEAP_SECOND,LOGGER,PATTERN,UNKNOWN_DAY_NAME,UTC,serialVersionUID
hcls AbstractDateParser,Rfc2822LenientParser,Rfc2822StrictParser

CLSS public javax.mail.internet.MimeBodyPart
cons public <init>()
cons public <init>(java.io.InputStream) throws javax.mail.MessagingException
cons public <init>(javax.mail.internet.InternetHeaders,byte[]) throws javax.mail.MessagingException
fld protected byte[] content
fld protected java.io.InputStream contentStream
fld protected java.lang.Object cachedContent
fld protected javax.activation.DataHandler dh
fld protected javax.mail.internet.InternetHeaders headers
intf javax.mail.internet.MimePart
meth protected java.io.InputStream getContentStream() throws javax.mail.MessagingException
meth protected void updateHeaders() throws javax.mail.MessagingException
meth public boolean isMimeType(java.lang.String) throws javax.mail.MessagingException
meth public int getLineCount() throws javax.mail.MessagingException
meth public int getSize() throws javax.mail.MessagingException
meth public java.io.InputStream getInputStream() throws java.io.IOException,javax.mail.MessagingException
meth public java.io.InputStream getRawInputStream() throws javax.mail.MessagingException
meth public java.lang.Object getContent() throws java.io.IOException,javax.mail.MessagingException
meth public java.lang.String getContentID() throws javax.mail.MessagingException
meth public java.lang.String getContentMD5() throws javax.mail.MessagingException
meth public java.lang.String getContentType() throws javax.mail.MessagingException
meth public java.lang.String getDescription() throws javax.mail.MessagingException
meth public java.lang.String getDisposition() throws javax.mail.MessagingException
meth public java.lang.String getEncoding() throws javax.mail.MessagingException
meth public java.lang.String getFileName() throws javax.mail.MessagingException
meth public java.lang.String getHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public java.lang.String[] getContentLanguage() throws javax.mail.MessagingException
meth public java.lang.String[] getHeader(java.lang.String) throws javax.mail.MessagingException
meth public java.util.Enumeration<java.lang.String> getAllHeaderLines() throws javax.mail.MessagingException
meth public java.util.Enumeration<java.lang.String> getMatchingHeaderLines(java.lang.String[]) throws javax.mail.MessagingException
meth public java.util.Enumeration<java.lang.String> getNonMatchingHeaderLines(java.lang.String[]) throws javax.mail.MessagingException
meth public java.util.Enumeration<javax.mail.Header> getAllHeaders() throws javax.mail.MessagingException
meth public java.util.Enumeration<javax.mail.Header> getMatchingHeaders(java.lang.String[]) throws javax.mail.MessagingException
meth public java.util.Enumeration<javax.mail.Header> getNonMatchingHeaders(java.lang.String[]) throws javax.mail.MessagingException
meth public javax.activation.DataHandler getDataHandler() throws javax.mail.MessagingException
meth public void addHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void addHeaderLine(java.lang.String) throws javax.mail.MessagingException
meth public void attachFile(java.io.File) throws java.io.IOException,javax.mail.MessagingException
meth public void attachFile(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException,javax.mail.MessagingException
meth public void attachFile(java.lang.String) throws java.io.IOException,javax.mail.MessagingException
meth public void attachFile(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException,javax.mail.MessagingException
meth public void removeHeader(java.lang.String) throws javax.mail.MessagingException
meth public void saveFile(java.io.File) throws java.io.IOException,javax.mail.MessagingException
meth public void saveFile(java.lang.String) throws java.io.IOException,javax.mail.MessagingException
meth public void setContent(java.lang.Object,java.lang.String) throws javax.mail.MessagingException
meth public void setContent(javax.mail.Multipart) throws javax.mail.MessagingException
meth public void setContentID(java.lang.String) throws javax.mail.MessagingException
meth public void setContentLanguage(java.lang.String[]) throws javax.mail.MessagingException
meth public void setContentMD5(java.lang.String) throws javax.mail.MessagingException
meth public void setDataHandler(javax.activation.DataHandler) throws javax.mail.MessagingException
meth public void setDescription(java.lang.String) throws javax.mail.MessagingException
meth public void setDescription(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setDisposition(java.lang.String) throws javax.mail.MessagingException
meth public void setFileName(java.lang.String) throws javax.mail.MessagingException
meth public void setHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setText(java.lang.String) throws javax.mail.MessagingException
meth public void setText(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setText(java.lang.String,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void writeTo(java.io.OutputStream) throws java.io.IOException,javax.mail.MessagingException
supr javax.mail.BodyPart
hfds allowutf8,cacheMultipart,decodeFileName,encodeFileName,ignoreMultipartEncoding,setContentTypeFileName,setDefaultTextCharset
hcls EncodedFileDataSource,MimePartDataHandler

CLSS public javax.mail.internet.MimeMessage
cons protected <init>(javax.mail.Folder,int)
cons protected <init>(javax.mail.Folder,java.io.InputStream,int) throws javax.mail.MessagingException
cons protected <init>(javax.mail.Folder,javax.mail.internet.InternetHeaders,byte[],int) throws javax.mail.MessagingException
cons public <init>(javax.mail.Session)
cons public <init>(javax.mail.Session,java.io.InputStream) throws javax.mail.MessagingException
cons public <init>(javax.mail.internet.MimeMessage) throws javax.mail.MessagingException
fld protected boolean modified
fld protected boolean saved
fld protected byte[] content
fld protected java.io.InputStream contentStream
fld protected java.lang.Object cachedContent
fld protected javax.activation.DataHandler dh
fld protected javax.mail.Flags flags
fld protected javax.mail.internet.InternetHeaders headers
innr public static RecipientType
intf javax.mail.internet.MimePart
meth protected java.io.InputStream getContentStream() throws javax.mail.MessagingException
meth protected javax.mail.internet.InternetHeaders createInternetHeaders(java.io.InputStream) throws javax.mail.MessagingException
meth protected javax.mail.internet.MimeMessage createMimeMessage(javax.mail.Session) throws javax.mail.MessagingException
meth protected void parse(java.io.InputStream) throws javax.mail.MessagingException
meth protected void updateHeaders() throws javax.mail.MessagingException
meth protected void updateMessageID() throws javax.mail.MessagingException
meth public boolean isMimeType(java.lang.String) throws javax.mail.MessagingException
meth public boolean isSet(javax.mail.Flags$Flag) throws javax.mail.MessagingException
meth public int getLineCount() throws javax.mail.MessagingException
meth public int getSize() throws javax.mail.MessagingException
meth public java.io.InputStream getInputStream() throws java.io.IOException,javax.mail.MessagingException
meth public java.io.InputStream getRawInputStream() throws javax.mail.MessagingException
meth public java.lang.Object getContent() throws java.io.IOException,javax.mail.MessagingException
meth public java.lang.String getContentID() throws javax.mail.MessagingException
meth public java.lang.String getContentMD5() throws javax.mail.MessagingException
meth public java.lang.String getContentType() throws javax.mail.MessagingException
meth public java.lang.String getDescription() throws javax.mail.MessagingException
meth public java.lang.String getDisposition() throws javax.mail.MessagingException
meth public java.lang.String getEncoding() throws javax.mail.MessagingException
meth public java.lang.String getFileName() throws javax.mail.MessagingException
meth public java.lang.String getHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public java.lang.String getMessageID() throws javax.mail.MessagingException
meth public java.lang.String getSubject() throws javax.mail.MessagingException
meth public java.lang.String[] getContentLanguage() throws javax.mail.MessagingException
meth public java.lang.String[] getHeader(java.lang.String) throws javax.mail.MessagingException
meth public java.util.Date getReceivedDate() throws javax.mail.MessagingException
meth public java.util.Date getSentDate() throws javax.mail.MessagingException
meth public java.util.Enumeration<java.lang.String> getAllHeaderLines() throws javax.mail.MessagingException
meth public java.util.Enumeration<java.lang.String> getMatchingHeaderLines(java.lang.String[]) throws javax.mail.MessagingException
meth public java.util.Enumeration<java.lang.String> getNonMatchingHeaderLines(java.lang.String[]) throws javax.mail.MessagingException
meth public java.util.Enumeration<javax.mail.Header> getAllHeaders() throws javax.mail.MessagingException
meth public java.util.Enumeration<javax.mail.Header> getMatchingHeaders(java.lang.String[]) throws javax.mail.MessagingException
meth public java.util.Enumeration<javax.mail.Header> getNonMatchingHeaders(java.lang.String[]) throws javax.mail.MessagingException
meth public javax.activation.DataHandler getDataHandler() throws javax.mail.MessagingException
meth public javax.mail.Address getSender() throws javax.mail.MessagingException
meth public javax.mail.Address[] getAllRecipients() throws javax.mail.MessagingException
meth public javax.mail.Address[] getFrom() throws javax.mail.MessagingException
meth public javax.mail.Address[] getRecipients(javax.mail.Message$RecipientType) throws javax.mail.MessagingException
meth public javax.mail.Address[] getReplyTo() throws javax.mail.MessagingException
meth public javax.mail.Flags getFlags() throws javax.mail.MessagingException
meth public javax.mail.Message reply(boolean) throws javax.mail.MessagingException
meth public javax.mail.Message reply(boolean,boolean) throws javax.mail.MessagingException
meth public void addFrom(javax.mail.Address[]) throws javax.mail.MessagingException
meth public void addHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void addHeaderLine(java.lang.String) throws javax.mail.MessagingException
meth public void addRecipients(javax.mail.Message$RecipientType,java.lang.String) throws javax.mail.MessagingException
meth public void addRecipients(javax.mail.Message$RecipientType,javax.mail.Address[]) throws javax.mail.MessagingException
meth public void removeHeader(java.lang.String) throws javax.mail.MessagingException
meth public void saveChanges() throws javax.mail.MessagingException
meth public void setContent(java.lang.Object,java.lang.String) throws javax.mail.MessagingException
meth public void setContent(javax.mail.Multipart) throws javax.mail.MessagingException
meth public void setContentID(java.lang.String) throws javax.mail.MessagingException
meth public void setContentLanguage(java.lang.String[]) throws javax.mail.MessagingException
meth public void setContentMD5(java.lang.String) throws javax.mail.MessagingException
meth public void setDataHandler(javax.activation.DataHandler) throws javax.mail.MessagingException
meth public void setDescription(java.lang.String) throws javax.mail.MessagingException
meth public void setDescription(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setDisposition(java.lang.String) throws javax.mail.MessagingException
meth public void setFileName(java.lang.String) throws javax.mail.MessagingException
meth public void setFlags(javax.mail.Flags,boolean) throws javax.mail.MessagingException
meth public void setFrom() throws javax.mail.MessagingException
meth public void setFrom(java.lang.String) throws javax.mail.MessagingException
meth public void setFrom(javax.mail.Address) throws javax.mail.MessagingException
meth public void setHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setRecipients(javax.mail.Message$RecipientType,java.lang.String) throws javax.mail.MessagingException
meth public void setRecipients(javax.mail.Message$RecipientType,javax.mail.Address[]) throws javax.mail.MessagingException
meth public void setReplyTo(javax.mail.Address[]) throws javax.mail.MessagingException
meth public void setSender(javax.mail.Address) throws javax.mail.MessagingException
meth public void setSentDate(java.util.Date) throws javax.mail.MessagingException
meth public void setSubject(java.lang.String) throws javax.mail.MessagingException
meth public void setSubject(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setText(java.lang.String) throws javax.mail.MessagingException
meth public void setText(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void setText(java.lang.String,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public void writeTo(java.io.OutputStream) throws java.io.IOException,javax.mail.MessagingException
meth public void writeTo(java.io.OutputStream,java.lang.String[]) throws java.io.IOException,javax.mail.MessagingException
supr javax.mail.Message
hfds allowutf8,answeredFlag,mailDateFormat,strict

CLSS public static javax.mail.internet.MimeMessage$RecipientType
 outer javax.mail.internet.MimeMessage
cons protected <init>(java.lang.String)
fld public final static javax.mail.internet.MimeMessage$RecipientType NEWSGROUPS
meth protected java.lang.Object readResolve() throws java.io.ObjectStreamException
supr javax.mail.Message$RecipientType
hfds serialVersionUID

CLSS public javax.mail.internet.MimeMultipart
cons public !varargs <init>(java.lang.String,javax.mail.BodyPart[]) throws javax.mail.MessagingException
cons public !varargs <init>(javax.mail.BodyPart[]) throws javax.mail.MessagingException
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(javax.activation.DataSource) throws javax.mail.MessagingException
fld protected boolean allowEmpty
fld protected boolean complete
fld protected boolean ignoreExistingBoundaryParameter
fld protected boolean ignoreMissingBoundaryParameter
fld protected boolean ignoreMissingEndBoundary
fld protected boolean parsed
fld protected java.lang.String preamble
fld protected javax.activation.DataSource ds
meth protected javax.mail.internet.InternetHeaders createInternetHeaders(java.io.InputStream) throws javax.mail.MessagingException
meth protected javax.mail.internet.MimeBodyPart createMimeBodyPart(java.io.InputStream) throws javax.mail.MessagingException
meth protected javax.mail.internet.MimeBodyPart createMimeBodyPart(javax.mail.internet.InternetHeaders,byte[]) throws javax.mail.MessagingException
meth protected void initializeProperties()
meth protected void parse() throws javax.mail.MessagingException
meth protected void updateHeaders() throws javax.mail.MessagingException
meth public boolean isComplete() throws javax.mail.MessagingException
meth public boolean removeBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException
meth public int getCount() throws javax.mail.MessagingException
meth public java.lang.String getPreamble() throws javax.mail.MessagingException
meth public javax.mail.BodyPart getBodyPart(int) throws javax.mail.MessagingException
meth public javax.mail.BodyPart getBodyPart(java.lang.String) throws javax.mail.MessagingException
meth public void addBodyPart(javax.mail.BodyPart) throws javax.mail.MessagingException
meth public void addBodyPart(javax.mail.BodyPart,int) throws javax.mail.MessagingException
meth public void removeBodyPart(int) throws javax.mail.MessagingException
meth public void setPreamble(java.lang.String) throws javax.mail.MessagingException
meth public void setSubType(java.lang.String) throws javax.mail.MessagingException
meth public void writeTo(java.io.OutputStream) throws java.io.IOException,javax.mail.MessagingException
supr javax.mail.Multipart

CLSS public abstract interface javax.mail.internet.MimePart
intf javax.mail.Part
meth public abstract java.lang.String getContentID() throws javax.mail.MessagingException
meth public abstract java.lang.String getContentMD5() throws javax.mail.MessagingException
meth public abstract java.lang.String getEncoding() throws javax.mail.MessagingException
meth public abstract java.lang.String getHeader(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public abstract java.lang.String[] getContentLanguage() throws javax.mail.MessagingException
meth public abstract java.util.Enumeration<java.lang.String> getAllHeaderLines() throws javax.mail.MessagingException
meth public abstract java.util.Enumeration<java.lang.String> getMatchingHeaderLines(java.lang.String[]) throws javax.mail.MessagingException
meth public abstract java.util.Enumeration<java.lang.String> getNonMatchingHeaderLines(java.lang.String[]) throws javax.mail.MessagingException
meth public abstract void addHeaderLine(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setContentLanguage(java.lang.String[]) throws javax.mail.MessagingException
meth public abstract void setContentMD5(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setText(java.lang.String) throws javax.mail.MessagingException
meth public abstract void setText(java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public abstract void setText(java.lang.String,java.lang.String,java.lang.String) throws javax.mail.MessagingException

CLSS public javax.mail.internet.MimePartDataSource
cons public <init>(javax.mail.internet.MimePart)
fld protected javax.mail.internet.MimePart part
intf javax.activation.DataSource
intf javax.mail.MessageAware
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public java.lang.String getContentType()
meth public java.lang.String getName()
meth public javax.mail.MessageContext getMessageContext()
supr java.lang.Object
hfds context

CLSS public javax.mail.internet.MimeUtility
fld public final static int ALL = -1
meth public static java.io.InputStream decode(java.io.InputStream,java.lang.String) throws javax.mail.MessagingException
meth public static java.io.OutputStream encode(java.io.OutputStream,java.lang.String) throws javax.mail.MessagingException
meth public static java.io.OutputStream encode(java.io.OutputStream,java.lang.String,java.lang.String) throws javax.mail.MessagingException
meth public static java.lang.String decodeText(java.lang.String) throws java.io.UnsupportedEncodingException
meth public static java.lang.String decodeWord(java.lang.String) throws java.io.UnsupportedEncodingException,javax.mail.internet.ParseException
meth public static java.lang.String encodeText(java.lang.String) throws java.io.UnsupportedEncodingException
meth public static java.lang.String encodeText(java.lang.String,java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
meth public static java.lang.String encodeWord(java.lang.String) throws java.io.UnsupportedEncodingException
meth public static java.lang.String encodeWord(java.lang.String,java.lang.String,java.lang.String) throws java.io.UnsupportedEncodingException
meth public static java.lang.String fold(int,java.lang.String)
meth public static java.lang.String getDefaultJavaCharset()
meth public static java.lang.String getEncoding(javax.activation.DataHandler)
meth public static java.lang.String getEncoding(javax.activation.DataSource)
meth public static java.lang.String javaCharset(java.lang.String)
meth public static java.lang.String mimeCharset(java.lang.String)
meth public static java.lang.String quote(java.lang.String,java.lang.String)
meth public static java.lang.String unfold(java.lang.String)
supr java.lang.Object
hfds ALL_ASCII,MOSTLY_ASCII,MOSTLY_NONASCII,allowUtf8,decodeStrict,defaultJavaCharset,defaultMIMECharset,encodeEolStrict,foldEncodedWords,foldText,ignoreUnknownEncoding,java2mime,mime2java,nonAsciiCharsetMap

CLSS public javax.mail.internet.NewsAddress
cons public <init>()
cons public <init>(java.lang.String)
cons public <init>(java.lang.String,java.lang.String)
fld protected java.lang.String host
fld protected java.lang.String newsgroup
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHost()
meth public java.lang.String getNewsgroup()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public static java.lang.String toString(javax.mail.Address[])
meth public static javax.mail.internet.NewsAddress[] parse(java.lang.String) throws javax.mail.internet.AddressException
meth public void setHost(java.lang.String)
meth public void setNewsgroup(java.lang.String)
supr javax.mail.Address
hfds serialVersionUID

CLSS public javax.mail.internet.ParameterList
cons public <init>()
cons public <init>(java.lang.String) throws javax.mail.internet.ParseException
meth public int size()
meth public java.lang.String get(java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.util.Enumeration<java.lang.String> getNames()
meth public void combineSegments()
meth public void remove(java.lang.String)
meth public void set(java.lang.String,java.lang.String)
meth public void set(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds applehack,decodeParameters,decodeParametersStrict,encodeParameters,hex,lastName,list,multisegmentNames,parametersStrict,slist,splitLongParameters,windowshack
hcls LiteralValue,MultiValue,ParamEnum,ToStringBuffer,Value

CLSS public javax.mail.internet.ParseException
cons public <init>()
cons public <init>(java.lang.String)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public javax.mail.internet.PreencodedMimeBodyPart
cons public <init>(java.lang.String)
meth protected void updateHeaders() throws javax.mail.MessagingException
meth public java.lang.String getEncoding() throws javax.mail.MessagingException
meth public void writeTo(java.io.OutputStream) throws java.io.IOException,javax.mail.MessagingException
supr javax.mail.internet.MimeBodyPart
hfds encoding

CLSS public abstract interface javax.mail.internet.SharedInputStream
meth public abstract java.io.InputStream newStream(long,long)
meth public abstract long getPosition()

CLSS public abstract javax.mail.search.AddressStringTerm
cons protected <init>(java.lang.String)
meth protected boolean match(javax.mail.Address)
meth public boolean equals(java.lang.Object)
supr javax.mail.search.StringTerm
hfds serialVersionUID

CLSS public abstract javax.mail.search.AddressTerm
cons protected <init>(javax.mail.Address)
fld protected javax.mail.Address address
meth protected boolean match(javax.mail.Address)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public javax.mail.Address getAddress()
supr javax.mail.search.SearchTerm
hfds serialVersionUID

CLSS public final javax.mail.search.AndTerm
cons public <init>(javax.mail.search.SearchTerm,javax.mail.search.SearchTerm)
cons public <init>(javax.mail.search.SearchTerm[])
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public javax.mail.search.SearchTerm[] getTerms()
supr javax.mail.search.SearchTerm
hfds serialVersionUID,terms

CLSS public final javax.mail.search.BodyTerm
cons public <init>(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.StringTerm
hfds serialVersionUID

CLSS public abstract javax.mail.search.ComparisonTerm
cons public <init>()
fld protected int comparison
fld public final static int EQ = 3
fld public final static int GE = 6
fld public final static int GT = 5
fld public final static int LE = 1
fld public final static int LT = 2
fld public final static int NE = 4
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr javax.mail.search.SearchTerm
hfds serialVersionUID

CLSS public abstract javax.mail.search.DateTerm
cons protected <init>(int,java.util.Date)
fld protected java.util.Date date
meth protected boolean match(java.util.Date)
meth public boolean equals(java.lang.Object)
meth public int getComparison()
meth public int hashCode()
meth public java.util.Date getDate()
supr javax.mail.search.ComparisonTerm
hfds serialVersionUID

CLSS public final javax.mail.search.FlagTerm
cons public <init>(javax.mail.Flags,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean getTestSet()
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public javax.mail.Flags getFlags()
supr javax.mail.search.SearchTerm
hfds flags,serialVersionUID,set

CLSS public final javax.mail.search.FromStringTerm
cons public <init>(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.AddressStringTerm
hfds serialVersionUID

CLSS public final javax.mail.search.FromTerm
cons public <init>(javax.mail.Address)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.AddressTerm
hfds serialVersionUID

CLSS public final javax.mail.search.HeaderTerm
cons public <init>(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public java.lang.String getHeaderName()
supr javax.mail.search.StringTerm
hfds headerName,serialVersionUID

CLSS public abstract javax.mail.search.IntegerComparisonTerm
cons protected <init>(int,int)
fld protected int number
meth protected boolean match(int)
meth public boolean equals(java.lang.Object)
meth public int getComparison()
meth public int getNumber()
meth public int hashCode()
supr javax.mail.search.ComparisonTerm
hfds serialVersionUID

CLSS public final javax.mail.search.MessageIDTerm
cons public <init>(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.StringTerm
hfds serialVersionUID

CLSS public final javax.mail.search.MessageNumberTerm
cons public <init>(int)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.IntegerComparisonTerm
hfds serialVersionUID

CLSS public final javax.mail.search.NotTerm
cons public <init>(javax.mail.search.SearchTerm)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public javax.mail.search.SearchTerm getTerm()
supr javax.mail.search.SearchTerm
hfds serialVersionUID,term

CLSS public final javax.mail.search.OrTerm
cons public <init>(javax.mail.search.SearchTerm,javax.mail.search.SearchTerm)
cons public <init>(javax.mail.search.SearchTerm[])
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public javax.mail.search.SearchTerm[] getTerms()
supr javax.mail.search.SearchTerm
hfds serialVersionUID,terms

CLSS public final javax.mail.search.ReceivedDateTerm
cons public <init>(int,java.util.Date)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.DateTerm
hfds serialVersionUID

CLSS public final javax.mail.search.RecipientStringTerm
cons public <init>(javax.mail.Message$RecipientType,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public javax.mail.Message$RecipientType getRecipientType()
supr javax.mail.search.AddressStringTerm
hfds serialVersionUID,type

CLSS public final javax.mail.search.RecipientTerm
cons public <init>(javax.mail.Message$RecipientType,javax.mail.Address)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
meth public int hashCode()
meth public javax.mail.Message$RecipientType getRecipientType()
supr javax.mail.search.AddressTerm
hfds serialVersionUID,type

CLSS public javax.mail.search.SearchException
cons public <init>()
cons public <init>(java.lang.String)
supr javax.mail.MessagingException
hfds serialVersionUID

CLSS public abstract javax.mail.search.SearchTerm
cons public <init>()
intf java.io.Serializable
meth public abstract boolean match(javax.mail.Message)
supr java.lang.Object
hfds serialVersionUID

CLSS public final javax.mail.search.SentDateTerm
cons public <init>(int,java.util.Date)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.DateTerm
hfds serialVersionUID

CLSS public final javax.mail.search.SizeTerm
cons public <init>(int,int)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.IntegerComparisonTerm
hfds serialVersionUID

CLSS public abstract javax.mail.search.StringTerm
cons protected <init>(java.lang.String)
cons protected <init>(java.lang.String,boolean)
fld protected boolean ignoreCase
fld protected java.lang.String pattern
meth protected boolean match(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean getIgnoreCase()
meth public int hashCode()
meth public java.lang.String getPattern()
supr javax.mail.search.SearchTerm
hfds serialVersionUID

CLSS public final javax.mail.search.SubjectTerm
cons public <init>(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean match(javax.mail.Message)
supr javax.mail.search.StringTerm
hfds serialVersionUID

CLSS public javax.mail.util.ByteArrayDataSource
cons public <init>(byte[],java.lang.String)
cons public <init>(java.io.InputStream,java.lang.String) throws java.io.IOException
cons public <init>(java.lang.String,java.lang.String) throws java.io.IOException
intf javax.activation.DataSource
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public java.lang.String getContentType()
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds data,len,name,type
hcls DSByteArrayOutputStream

CLSS public javax.mail.util.SharedByteArrayInputStream
cons public <init>(byte[])
cons public <init>(byte[],int,int)
fld protected int start
intf javax.mail.internet.SharedInputStream
meth public java.io.InputStream newStream(long,long)
meth public long getPosition()
supr java.io.ByteArrayInputStream

CLSS public javax.mail.util.SharedFileInputStream
cons public <init>(java.io.File) throws java.io.IOException
cons public <init>(java.io.File,int) throws java.io.IOException
cons public <init>(java.lang.String) throws java.io.IOException
cons public <init>(java.lang.String,int) throws java.io.IOException
fld protected int bufsize
fld protected java.io.RandomAccessFile in
fld protected long bufpos
fld protected long datalen
fld protected long start
intf javax.mail.internet.SharedInputStream
meth protected void finalize() throws java.lang.Throwable
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public java.io.InputStream newStream(long,long)
meth public long getPosition()
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.BufferedInputStream
hfds defaultBufferSize,master,sf
hcls SharedFile

