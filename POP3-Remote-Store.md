POP3 Remote Store
=================

The POP3 Remote Store is an experimental Store that behaves much the
way a typical email client expects when using POP3. That is, all
messages and all folders are really stored locally (in this case using
the [Mbox Store](Mbox-Store)), and new messages are fetched
from the server using the POP3 protocol.

The POP3 Remote Store is not currently distributed with JavaMail, but
is included in the JavaMail source code in the mbox project.

XXX - still need to provide more information here.
